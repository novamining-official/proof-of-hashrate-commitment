package main

import common.JsonSupport
import db.TreeStore
import main.Helpers.passingTestMock
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.{ FlatSpec, Matchers }
import proof.MerkleTree.{ Account, Tree }
import proof.MerkleTree.CHAIN_ID._

class StoreSpec extends FlatSpec with Matchers with JsonSupport {

  lazy val usersSerializationTest = parse(passingTestMock).extract[Seq[Account]]

  it should "save the tree to a file" in {
    val tree = Tree.build(accounts = usersSerializationTest)
    TreeStore.saveTree(tree)

    1 shouldBe 1

  }

  it should "load the tree from a file" in {

    val optTree = TreeStore.loadTree(BITCOIN_CHAIN, "f61070df851b2fa44eb9f0bc63b69147229796068dd55676265f147d71b25ced")

    optTree.isDefined shouldBe true

    val Some(tree) = optTree

    tree.rootDigest shouldBe "f61070df851b2fa44eb9f0bc63b69147229796068dd55676265f147d71b25ced"
    tree.chainId shouldBe BITCOIN_CHAIN
    tree.numNodes shouldBe 15
    tree.totalBalance shouldBe 387

  }

}
