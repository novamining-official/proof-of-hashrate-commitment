package db

import java.nio.file.{ Files, Paths }

import proof.MerkleTree.Tree

object TreeStore {

  //  val config = com.typesafe.config.ConfigFactory.load()

  lazy val storeDir = Paths.get("./db")
  println(s"DB directory: ${storeDir.toAbsolutePath}")

  //check if dir exists
  if (!Files.isDirectory(storeDir)) {
    println(s"DB directory not found, creating one")
    Files.createDirectory(storeDir)
  }

  def saveTree(tree: Tree) = {

  }

}
