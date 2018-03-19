package db

import common.Boot
import proof.MerkleTree.Tree

import scala.concurrent.Future

object TreeDbTable {

  import Boot.database.profile.api._

  lazy val db = Boot.database.db

  case class StoredTree(
    rootDigest: String,
    chainId: String = "BITCOIN"
  )

  class TreeTable(tag: Tag) extends Table[StoredTree](tag, "TREES") {

    def rootDigest = column[String]("rootDigest", O.PrimaryKey)
    def chainId = column[String]("chainId")

    override def * = (rootDigest, chainId) <> (StoredTree.tupled, StoredTree.unapply)

  }

  private val treeTable = TableQuery[TreeTable]

  def insertTree(tree: StoredTree): Future[Int] = db.run {
    treeTable += tree
  }

}
