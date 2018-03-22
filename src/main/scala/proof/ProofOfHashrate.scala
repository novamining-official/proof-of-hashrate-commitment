package proof

import com.typesafe.scalalogging.LazyLogging
import proof.MerkleTree._

object ProofOfHashrate extends LazyLogging {

  //TODO is chainId here necessary?
  case class Proof(chainId: CHAIN_ID.Value, root: Node) {

    def isValid(rootDigest: String, account: Account): Boolean = {
      rootDigest == root.id && checkSubtreeProofDeep(root, account, 0)._2
    }

  }

  def checkNodeId(node: Node): Boolean = {
    !node.isLeaf && node.id == Node.mkIdHash(node.leftHash.get, node.rightHash.get, node.totalValue)
  }

  //FIXME check only the leaves at the deepest level
  private def checkSubtreeProof(node: Node, account: Account): Boolean = {

    if (node.isLeaf)
      return node.id == Node.mkLeafId(account)

    if (node.right.isDefined)
      return checkNodeId(node) && checkSubtreeProof(node.right.get, account)

    if (node.left.isDefined)
      return checkNodeId(node) && checkSubtreeProof(node.left.get, account)

    false
  }

  //
  private def checkSubtreeProofDeep(node: Node, account: Account, level: Int): (Int, Boolean) = {

    if (node.isLeaf)
      return (level, node.id == Node.mkLeafId(account))

    if (!checkNodeId(node))
      return (level, false)

    val leftMaxTruth: Option[(Int, Boolean)] = node.left.map { leftNode =>
      checkSubtreeProofDeep(leftNode, account, level + 1)
    }

    val rightMaxTruth: Option[(Int, Boolean)] = node.right.map { rightNode =>
      checkSubtreeProofDeep(rightNode, account, level + 1)
    }

    (leftMaxTruth, rightMaxTruth) match {
      case (Some((leftLevel, leftFound)), Some((rightLevel, rightFound))) =>
        if (leftLevel == rightLevel)
          (leftLevel, leftFound || rightFound)
        else if (leftLevel > rightLevel)
          (leftLevel, leftFound)
        else
          (rightLevel, rightFound)

      case _ =>
        (level, false)
    }

    //
    //    (level, false)
  }

}
