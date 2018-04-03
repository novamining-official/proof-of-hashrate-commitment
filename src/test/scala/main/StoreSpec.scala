package main

import java.nio.file.Files

import common.JsonSupport
import db.{ TreeManager, TreeStore }
import main.Helpers._
import scala.collection.JavaConverters._
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.{ BeforeAndAfter, FlatSpec, Matchers }
import proof.domain.Account
import proof.domain.CHAIN_ID._
import scala.concurrent.duration._
import scala.concurrent.Await

class StoreSpec extends FlatSpec with Matchers with JsonSupport with CleanDBStore {

  lazy val mockAccountsTest = parse(accountsTestMock).extract[Seq[Account]]
  lazy val passingTestMockAccounts = parse(passingTestMock).extract[Seq[Account]]

  it should "save the tree to a file" in {
    val rootDigest = Await.result(TreeManager.createAndSaveTree(BITCOIN_CHAIN, mockAccountsTest), 3 seconds)

    val optPath = Files
      .list(TreeStore.storeDir)
      .iterator.asScala
      .find(_.getFileName.toString.contains(rootDigest.take(8)))

    optPath.isDefined shouldBe true

  }

  it should "load the tree from a file" in {
    val rootDigest = Await.result(TreeManager.createAndSaveTree(BITCOIN_CHAIN, passingTestMockAccounts), 3 seconds)
    val optTree = TreeManager.findTree(BITCOIN_CHAIN, "f61070df851b2fa44eb9f0bc63b69147229796068dd55676265f147d71b25ced")

    optTree.isDefined shouldBe true

    val Some(tree) = optTree

    tree.rootDigest shouldBe "f61070df851b2fa44eb9f0bc63b69147229796068dd55676265f147d71b25ced"
    tree.chainId shouldBe BITCOIN_CHAIN
    tree.numNodes shouldBe 15
    tree.totalBalance shouldBe 387

  }

}
