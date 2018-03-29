package db

import proof.MerkleTree.{ Account, CHAIN_ID, Tree }
import proof.MerkleTree.CHAIN_ID.CHAIN_ID
import proof.ProofOfHashrate.Proof
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TreeManager {

  def createAndSaveTree(chainId: CHAIN_ID, accounts: Seq[Account]): Future[String] = Future {
    val tree = Tree.build(chainId, accounts)
    TreeStore.addTree(tree)
    tree.rootDigest
  }

  def findTree(chainId: CHAIN_ID.Value, digest: String): Option[Tree] = {
    TreeStore.findTree(chainId, digest)
  }

  def findProof(chainId: CHAIN_ID, digest: String, account: Account): Option[Proof] = {
    findTree(chainId, digest).flatMap(_.findProofByAccount(account))
  }

  def checkProof(digest: String, proof: Proof, account: Account): Boolean = {
    proof.isValid("", account)
  }

}