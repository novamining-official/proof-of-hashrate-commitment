package proof

import proof.MerkleTree._

object ProofOfHashrate {

  //TODO is chainId here necessary?
  case class Proof(chainId: CHAIN_ID.Value, root: Node) {

    def isValid(rootDigest: String, account: Account): Boolean = {
      rootDigest == root.id && checkSubtreeProof(root, account)
    }

  }

  def checkNodeId(node: Node): Boolean = {
    !node.isLeaf && node.id == Node.mkIdHash(node.leftHash.get, node.rightHash.get, node.totalValue)
  }

  //TODO check for non decreasing node values ?
  private def checkSubtreeProof(node: Node, account: Account): Boolean = {

    if (node.isLeaf)
      return node.id == Node.mkLeafId(account)

    if (node.right.isDefined)
      return checkNodeId(node) && checkSubtreeProof(node.right.get, account)

    if (node.left.isDefined)
      return checkNodeId(node) && checkSubtreeProof(node.left.get, account)

    false
  }

}
