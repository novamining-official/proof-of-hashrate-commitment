package db

import common.Boot
import proof.MerkleTree.Node
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class NodeDbComponent {

  import Boot.database.profile.api._

  lazy val db = Boot.db

  case class StoredNode(
    id: String,
    leftValue: Double,
    rightValue: Double,
    leftHash: Option[String] = None,
    rightHash: Option[String] = None
  )

  def fromTreeNode(node: Node): StoredNode = StoredNode(node.id, node.leftValue, node.rightValue, node.leftHash, node.rightHash)

  def toTreeNode(storedNode: StoredNode, left: Option[Node] = None, right: Option[Node] = None) =
    Node(storedNode.id, storedNode.leftValue, storedNode.rightValue, storedNode.leftHash, storedNode.rightHash, left, right)

  class NodeTable(tag: Tag) extends Table[StoredNode](tag, "NODES") {

    def id = column[String]("id", O.PrimaryKey)
    def leftValue = column[Double]("leftValue")
    def rightValue = column[Double]("rightValue")
    def leftHash = column[Option[String]]("leftHash")
    def rightHash = column[Option[String]]("rightHash")

    override def * = (id, leftValue, rightValue, leftHash, rightHash) <> (StoredNode.tupled, StoredNode.unapply)

  }

  val nodesTable = TableQuery[NodeTable]

  private def insertStoredNodes(nodes: Seq[StoredNode]): Future[Int] = db.run {
    nodesTable ++= nodes
  }.map(_.getOrElse(0))

  private def storedNodeById(hash: String): Future[Option[StoredNode]] = db.run {
    nodesTable
      .filter(_.id === hash)
      .result
      .headOption
  }

  private def storedNodesByIdSet(ids: Set[String]): Future[Seq[StoredNode]] = db.run {
    nodesTable
      .filter(_.id inSet ids)
      .result
  }

  def insertNodeAndSibilings(root: Node): Future[Int] = {
    val storedNodes = treeToSeq(Some(root)).map(fromTreeNode)
    insertStoredNodes(storedNodes)
  }

  private def treeToSeq(node: Option[Node]): Seq[Node] = node match {
    case Some(n) => Seq(n) ++ treeToSeq(n.left) ++ treeToSeq(n.right)
    case None    => Seq.empty
  }

  def nodeById(hash: String): Future[Option[Node]] = {
    storedNodeById(hash).map { optNode =>
      optNode.map { storedNode =>



        for {
          left <- storedNode.leftHash.map(nodeById)
          right <- storedNode.rightHash.map(nodeById)
        } yield {

        }

        toTreeNode(storedNode)
      }
    }
  }

}
