package proof

import java.security.MessageDigest
import proof.ProofOfHashrate.Proof
import proof.domain._
import CHAIN_ID._

object MerkleTree {

  case class Tree(
    chainId: CHAIN_ID.Value,
    accounts: Seq[Account],
    root: Node
  ) {

    def rootDigest = root.id

    def totalBalance = root.totalValue

    def hasProofFor(account: Account): Boolean = findProofByAccount(account).isDefined

    def findProofByAccount(account: Account): Option[Proof] = {
      mkProofPath(Some(root), account).map(node => Proof(chainId, node))
    }

    private def mkProofPath(n: Option[Node], account: Account): Option[Node] = n match {
      case None => None
      case Some(node) =>
        if (node.isLeaf && node.id == Node.mkLeafId(account)) {
          return Some(node.copy())
        }

        if (!node.isLeaf) {
          val leftBranch = mkProofPath(node.left, account)

          if (leftBranch.isDefined)
            return Some(node.copy(left = leftBranch, right = node.right.map(r => Node(r.id))))

          val rightBranch = mkProofPath(node.right, account)

          if (rightBranch.isDefined)
            return Some(node.copy(left = node.left.map(l => Node(l.id)), right = rightBranch))

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

    //
    def fromArray(array: Seq[Option[Node]]): Option[Node] = fromArrayRec(array, 0)

    def getLeft(array: Seq[Option[Node]], currentNodeIndex: Int) = {
      if (currentNodeIndex * 2 + 1 >= array.size)
        None
      else
        array(currentNodeIndex * 2 + 1)
    }

    def getRight(array: Seq[Option[Node]], currentNodeIndex: Int) = {
      if (currentNodeIndex * 2 + 2 >= array.size)
        None
      else
        array(currentNodeIndex * 2 + 2)
    }

    private def fromArrayRec(array: Seq[Option[Node]], indexAt: Int): Option[Node] = {

      if (indexAt < 0 || indexAt >= array.size)
        return None

      array(indexAt).map { node =>
        val leftChild = fromArrayRec(array, indexAt * 2 + 1)
        val rightChild = fromArrayRec(array, indexAt * 2 + 2)

        node.copy(
          leftHash = leftChild.map(_.id),
          left = leftChild,
          rightHash = rightChild.map(_.id),
          right = rightChild
        )
      }
    }

    def toArray(tree: Tree): Array[Option[Node]] = {
      toArray(tree.root)
    }

    def toArray(root: Node): Array[Option[Node]] = {
      //create dummy tree to compute the max depth from its root node
      val treeMaxDepth = Tree(BITCOIN_CHAIN, Seq.empty, root).maxDepth
      //the index of the last node at level N is
      //last(N) = 2^(N+1) - 2
      val size = math.pow(2, treeMaxDepth + 1).toInt - 2
      val array = Array.fill[Option[Node]](size)(None)
      toArrayNode(Some(root), 0, array)
      array
    }

    private def toArrayNode(node: Option[Node], indexAt: Int, array: Array[Option[Node]]): Unit = node match {
      case None =>
      case Some(n) =>
        // blank out the nested references, we know them via the implicit ordering of the binary heap
        array(indexAt) = Some(n.copy(leftHash = None, rightHash = None, left = None, right = None))

        val leftChild = (n.leftHash, n.left) match {
          case (Some(_), Some(_)) => n.left
          case (Some(hash), None) => Some(Node(id = hash, leftValue = n.leftValue))
          case _                  => None
        }

        val rightChild = (n.rightHash, n.right) match {
          case (Some(_), Some(_)) => n.right
          case (Some(hash), None) => Some(Node(id = hash, leftValue = n.rightValue))
          case _                  => None
        }

        toArrayNode(leftChild, indexAt * 2 + 1, array)
        toArrayNode(rightChild, indexAt * 2 + 2, array)

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
        case false => s"NODE [$id  $totalValue  leftHash:$leftHash rightHash:$rightHash \nleft: ${left.map(_.toString)} \nright: ${right.map(_.toString)}]"
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
