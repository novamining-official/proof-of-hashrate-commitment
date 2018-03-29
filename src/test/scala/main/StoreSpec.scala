package main

import java.nio.file.Files

import common.JsonSupport
import db.TreeStore
import main.Helpers._
import scala.collection.JavaConverters._
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.{ BeforeAndAfter, FlatSpec, Matchers }
import proof.MerkleTree.{ Account, Tree }
import proof.MerkleTree.CHAIN_ID._

//TODO use config to make the test db point to test directory
class StoreSpec extends FlatSpec with Matchers with JsonSupport with BeforeAndAfter {

  lazy val mockAccounts = parse(accountsTestMock).extract[Seq[Account]]

  //Clean the test store dir every time we're about to run the test
  before {
    Files.list(TreeStore.storeDir).iterator.asScala.map { file =>
      Files.deleteIfExists(file)
    }
  }

  it should "save the tree to a file" in {
    val tree = Tree.build(accounts = mockAccounts)
    TreeStore.addTree(tree)

    val optPath = Files
      .list(TreeStore.storeDir)
      .iterator.asScala
      .find(_.getFileName.toString.contains(tree.rootDigest.take(8)))

    optPath.isDefined shouldBe true

  }

  it should "load the tree from a file" in {

    val optTree = TreeStore.findTree(BITCOIN_CHAIN, "f61070df851b2fa44eb9f0bc63b69147229796068dd55676265f147d71b25ced")

    optTree.isDefined shouldBe true

    val Some(tree) = optTree

    tree.rootDigest shouldBe "f61070df851b2fa44eb9f0bc63b69147229796068dd55676265f147d71b25ced"
    tree.chainId shouldBe BITCOIN_CHAIN
    tree.numNodes shouldBe 15
    tree.totalBalance shouldBe 387

  }

}
