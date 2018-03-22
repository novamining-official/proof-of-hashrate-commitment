package proof

import com.typesafe.scalalogging.LazyLogging
import proof.MerkleTree._

object ProofOfHashrate extends LazyLogging {

  //TODO is chainId here necessary?
  case class Proof(chainId: CHAIN_ID.Value, root: Node) {

    def isValid(rootDigest: String, account: Account): Boolean = {
      rootDigest == root.id && checkSubtreeProof(root, account, 0)._2
    }

  }

  def checkNodeId(node: Node): Boolean = {
    !node.isLeaf && node.id == Node.mkIdHash(node.leftHash.get, node.rightHash.get, node.totalValue)
  }

  //Check if the given account matches any leaf at the deepest level
  private def checkSubtreeProof(node: Node, account: Account, level: Int): (Int, Boolean) = {

    if (node.isLeaf)
      return (level, node.id == Node.mkLeafId(account))

    if (!checkNodeId(node))
      return (level, false)

    val leftMaxTruth: Option[(Int, Boolean)] = node.left.map { leftNode =>
      checkSubtreeProof(leftNode, account, level + 1)
    }

    val rightMaxTruth: Option[(Int, Boolean)] = node.right.map { rightNode =>
      checkSubtreeProof(rightNode, account, level + 1)
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

  }

}
