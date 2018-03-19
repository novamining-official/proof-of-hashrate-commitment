package db

import common.Boot
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object NodeDbTable {

  import Boot.database.profile.api._

  lazy val db = Boot.database.db

  case class StoredNode(
    id: String,
    leftValue: Double,
    rightValue: Double,
    leftHash: Option[String] = None,
    rightHash: Option[String] = None
  )

  class NodeTable(tag: Tag) extends Table[StoredNode](tag, "NODES") {

    def id = column[String]("id", O.PrimaryKey)
    def leftValue = column[Double]("leftValue")
    def rightValue = column[Double]("rightValue")
    def leftHash = column[Option[String]]("leftHash")
    def rightHash = column[Option[String]]("rightHash")

    override def * = (id, leftValue, rightValue, leftHash, rightHash) <> (StoredNode.tupled, StoredNode.unapply)

  }

  private val nodesTable = TableQuery[NodeTable]

  def insertStoredNodes(nodes: Seq[StoredNode]): Future[Int] = db.run {
    nodesTable ++= nodes
  }.map(_.getOrElse(0))

  def storedNodeById(hash: String): Future[Option[StoredNode]] = db.run {
    nodesTable
      .filter(_.id === hash)
      .result
      .headOption
  }

  def storedNodesByIdSet(ids: Set[String]): Future[Seq[StoredNode]] = db.run {
    nodesTable
      .filter(_.id inSet ids)
      .result
  }

}
