package db

import java.nio.file.{ Files, Paths }
import com.typesafe.scalalogging.LazyLogging
import common.JsonSupport
import proof.MerkleTree.{ CHAIN_ID, Tree }
import org.json4s.jackson.Serialization._
import org.json4s.jackson.JsonMethods._
import scala.collection.JavaConverters._

object TreeStore extends JsonSupport with LazyLogging {

  //  val config = com.typesafe.config.ConfigFactory.load()

  //private lazy val inMemoryTreeStore = scala.collection.mutable.Seq[Tree]

  lazy val storeDir = Paths.get("db/")
  logger.info(s"DB directory: ${storeDir.toAbsolutePath}")

  //check if dir exists
  if (!Files.isDirectory(storeDir)) {
    logger.info(s"DB directory not found, creating one")
    Files.createDirectory(storeDir)
  }

  private def treeToFileName(tree: Tree): String = treeToFileName(tree.chainId, tree.rootDigest)
  private def treeToFileName(chainId: CHAIN_ID.Value, rootDigest: String): String = s"${chainId}_${rootDigest.take(8)}.json"

  def saveTree(tree: Tree) = {
    val filePath = Paths.get(storeDir.toString, treeToFileName(tree))
    val jsTree = write(tree)

    if (Files.exists(filePath))
      logger.warn(s"overwriting $filePath")

    Files.write(filePath, jsTree.getBytes)
  }

  def loadTree(chainId: CHAIN_ID.Value, rootDigest: String): Option[Tree] = {
    val filePath = Paths.get(storeDir.toString, treeToFileName(chainId, rootDigest))
    if (Files.notExists(filePath))
      return None

    val jsTree = Files.readAllLines(filePath).asScala.reduce(_ + _)

    parseOpt(jsTree).map(_.extractOpt[Tree]).flatten
  }

}
