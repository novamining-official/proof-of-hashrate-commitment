package db

import com.typesafe.scalalogging.LazyLogging
import db.NodeDbTable.StoredNode
import db.TreeDbTable.StoredTree
import proof.MerkleTree.{ Node, Tree }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DatabaseManager extends LazyLogging {

  def saveTree(tree: Tree) = {
    val storedTree = StoredTree(tree.rootDigest)
    val storedNodes = treeToSeq(Some(tree.root))

    for {
      nodeRowInserted <- NodeDbTable.insertStoredNodes(storedNodes)
      treeRowInserted <- TreeDbTable.insertTree(storedTree)
    } yield {
      logger.info(s"inserted ${nodeRowInserted} rows in NODES table")
      logger.info(s"inserted ${treeRowInserted} rows in TREES table")
      nodeRowInserted > 0 && treeRowInserted > 0
    }

  }

  private def treeToSeq(node: Option[Node]): Seq[StoredNode] = node match {
    case Some(n) => Seq(fromTreeNode(n)) ++ treeToSeq(n.left) ++ treeToSeq(n.right)
    case None    => Seq.empty
  }

  def fromTreeNode(node: Node): StoredNode = StoredNode(node.id, node.leftValue, node.rightValue, node.leftHash, node.rightHash)

  def toTreeNode(storedNode: StoredNode, left: Option[Node] = None, right: Option[Node] = None) =
    Node(storedNode.id, storedNode.leftValue, storedNode.rightValue, storedNode.leftHash, storedNode.rightHash, left, right)

}
