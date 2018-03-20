package proof

import java.security.MessageDigest
import proof.Proof.ProofOfLiability
import scala.math._

object MerkleTree {

  case object CHAIN_ID extends Enumeration {
    type CHAIN_ID = Value

    val BITCOIN_CHAIN = Value("BITCOIN_CHAIN")
    val ETHEREUM_CHAIN = Value("ETHEREUM_CHAIN")
  }

  import CHAIN_ID._

  case class Account(
    user: String,
    balance: Double,
    nonce: String
  ) extends Ordered[Account] {
    //lexicographical ordering
    def compare(that: Account): Int = this.user.compareTo(that.user)

  }

  case class Tree(
    chainId: CHAIN_ID.Value,
    accounts: Seq[Account],
    private[proof] val root: Node
  ) {

    def rootDigest = root.id

    def totalBalance = root.totalValue

    def hasProofFor(account: Account): Boolean = findProofByAccount(account).isDefined

    def findProofByAccount(account: Account): Option[ProofOfLiability] = {
      mkProofPath(root, account).map(node => ProofOfLiability(Tree(chainId, Seq(account), node)))
    }

    private def mkProofPath(node: Node, account: Account): Option[Node] = {
      if (node.isLeaf && node.id == Node.mkLeafId(account)) {
        return Some(node.copy())
      }

      if (!node.isLeaf) {
        val leftBranch = mkProofPath(node.left.get, account)

        if (leftBranch.isDefined)
          return Some(node.copy(left = leftBranch, right = None))

        val rightBranch = mkProofPath(node.right.get, account)

        if (rightBranch.isDefined)
          return Some(node.copy(left = None, right = rightBranch))

      }

      None
    }

    def numNodes: Int = nodesCountNode(root)

    private def nodesCountNode(node: Node): Int = node.isLeaf match {
      case true => 1
      case false =>
        val leftNodes = node.left.map(nodesCountNode).getOrElse(0)
        val rightNodes = node.right.map(nodesCountNode).getOrElse(0)
        leftNodes + rightNodes + 1
    }

    def maxDepth: Int = maxDepthNode(Some(root))

    private def maxDepthNode(optNode: Option[Node]): Int = optNode match {
      case None                       => 0
      case Some(node) if node.isLeaf  => 1
      case Some(node) if !node.isLeaf => math.max(maxDepthNode(node.left), maxDepthNode(node.right)) + 1
    }

    def addAccount(account: Account): Tree = {
      Tree.build(chainId, accounts :+ account)
    }

  }

  object Tree {
    //TODO scramble account ordering?
    def build(chainId: CHAIN_ID = BITCOIN_CHAIN, accounts: Seq[Account]): Tree = Tree(chainId, accounts, mkTree(accounts.sorted))

    def toArray(tree: Tree): Array[Option[Node]] = {
      //FIXME use tighter size for the array
      val array = Array.fill[Option[Node]](math.pow(2, tree.accounts.size).toInt + 1)(None)
      toArrayNode(Some(tree.root), 0, array)
      array
    }

    def fromArray(chainId: CHAIN_ID, accounts: Seq[Account], array: Array[Option[Node]]): Tree = {
      Tree(chainId, accounts, fromArrayRec(array, 0).get)
    }

    private def toArrayNode(node: Option[Node], indexAt: Int, array: Array[Option[Node]]): Unit = node match {
      case None =>
      case Some(n) =>
        // blank out the nested references, we know them via the implicit ordering of the binary heap
        array(indexAt) = Some(n.copy(leftHash = None, rightHash = None, left = None, right = None))
        toArrayNode(n.left, indexAt * 2 + 1, array)
        toArrayNode(n.right, indexAt * 2 + 2, array)
    }

    private def fromArrayRec(array: Array[Option[Node]], indexAt: Int): Option[Node] = {

      if (indexAt < 0 || indexAt >= array.size)
        return None

      array(indexAt).map { node =>
        node.copy(
          left = fromArrayRec(array, indexAt * 2 + 1),
          right = fromArrayRec(array, indexAt * 2 + 2)
        )
      }
    }

  }

  case class Node(
    // Hash of the concatenation of child hashes + total balance
    id: String,
    // The value of the subtree on the left
    leftValue: Double = 0,
    // The value of the subtree on the right
    rightValue: Double = 0,
    // Hash pointer to left child ID's
    leftHash: Option[String] = None,
    // Hash pointer to left child ID's
    rightHash: Option[String] = None,
    // left child
    left: Option[Node] = None,
    // right child
    right: Option[Node] = None
  ) {

    def totalValue = leftValue + rightValue

    def isLeaf = left.isEmpty && right.isEmpty

    override def toString: String = {
      isLeaf match {
        case true  => s"LEAF [$id  $totalValue]"
        case false => s"NODE [$id  $totalValue left ${leftHash.map(_.toString)} right: ${rightHash.map(_.toString)}]"
      }
    }
  }

  object Node {

    def mkId(left: Node, right: Node): String =
      mkIdHash(left.id, right.id, left.totalValue + right.totalValue)

    def mkIdHash(leftHash: String, rightHash: String, totalValue: Double): String =
      sha256(s"$leftHash | $rightHash | $totalValue")

    def mkLeafId(account: Account): String =
      sha256(s"${account.user} | ${account.balance} | ${account.nonce}")

    lazy val md = MessageDigest.getInstance("SHA-256")

    def sha256(msg: String): String = {
      md.update(msg.getBytes)
      md.digest.map("%02x".format(_)).mkString
    }

  }

  private def mkTree(accounts: Seq[Account]): Node = accounts match {
    //Leaf
    case singleton :: Nil => Node(
      id = Node.mkLeafId(singleton),
      leftValue = singleton.balance
    )
    //Node
    case moreThanOne =>
      val leftChild = mkTree(accounts.take(accounts.length / 2))
      val rightChild = mkTree(accounts.drop(accounts.length / 2))
      Node(
        id = Node.mkId(leftChild, rightChild),
        leftValue = leftChild.totalValue,
        rightValue = rightChild.totalValue,
        leftHash = Some(leftChild.id),
        rightHash = Some(rightChild.id),
        left = Some(leftChild),
        right = Some(rightChild)
      )
  }

}
