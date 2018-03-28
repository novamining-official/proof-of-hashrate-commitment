package db

import java.nio.file.{ Files, Path, Paths }
import common.Config._
import com.typesafe.scalalogging.LazyLogging
import common.JsonSupport
import proof.MerkleTree.{ CHAIN_ID, Tree }
import org.json4s.jackson.Serialization._
import org.json4s.jackson.JsonMethods._
import scala.collection.JavaConverters._

object TreeStore extends JsonSupport with LazyLogging {

  //this needs to be thread safe, got to use a Set backed by synchronized hash map
  private lazy val inMemoryTreeStore = new scala.collection.mutable.MutableList[Tree]

  lazy val storeDir = Paths.get(dbDirectory)
  logger.info(s"DB directory: ${storeDir.toAbsolutePath}")

  //check if dir exists
  if (!Files.isDirectory(storeDir)) {
    logger.info(s"DB directory not found, creating one")
    Files.createDirectory(storeDir)
  }

  private def treeToFileName(tree: Tree): String = treeToFileName(tree.chainId, tree.rootDigest)
  private def treeToFileName(chainId: CHAIN_ID.Value, rootDigest: String): String = s"${chainId}_${rootDigest.take(8)}.json"

  //Should always be invoked after creating a new tree
  def saveTree(tree: Tree) = {
    val filePath = Paths.get(storeDir.toString, treeToFileName(tree))
    val jsTree = write(tree)

    if (Files.exists(filePath))
      logger.warn(s"overwriting $filePath")

    Files.write(filePath, jsTree.getBytes)
  }

  //Automatically called by this class at startup
  def loadTree(filePath: Path): Option[Tree] = {
    if (Files.notExists(filePath))
      return None

    val jsTree = Files.readAllLines(filePath).asScala.reduce(_ + _)

    parseOpt(jsTree).map(_.extractOpt[Tree]).flatten
  }

  def loadTree(chainId: CHAIN_ID.Value, rootDigest: String): Option[Tree] = {
    loadTree(Paths.get(storeDir.toString, treeToFileName(chainId, rootDigest)))
  }

  def setup() = for {
    file <- Files.list(storeDir).iterator.asScala.toList
    tree <- loadTree(file)
  } yield {
    logger.info(s"Found: ${file.getFileName.toString}")
    inMemoryTreeStore += tree
  }

  //Interface method to retrieve the trees from the store
  def findTree(chainId: CHAIN_ID.Value, digest: String) = {
    inMemoryTreeStore.find(t => t.rootDigest == digest && t.chainId == chainId)
  }

  def allTrees(): Seq[Tree] = {
    inMemoryTreeStore.clone().iterator.toSeq
  }

}
