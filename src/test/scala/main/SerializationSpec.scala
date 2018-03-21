package main

import common.JsonSupport
import main.Helpers._
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization._
import org.scalatest.{ FlatSpec, Matchers }
import proof.MerkleTree.{ Account, Tree }
import proof.ProofOfHashrate.Proof

class SerializationSpec extends FlatSpec with Matchers with JsonSupport {

  lazy val usersSerializationTest = parse(passingTestMock).extract[Seq[Account]]

  //  it should "serialize to an array using binary heap" in {
  //    val tree = Tree.build(accounts = usersSerializationTest)
  //    val array = Tree.toArray(tree)
  //    val Some(rootNode) = array(0)
  //
  //    array.filter(_.isDefined).size shouldBe tree.numNodes
  //    rootNode.leftValue + rootNode.rightValue shouldBe tree.totalBalance
  //    rootNode.id shouldBe tree.rootDigest
  //
  //    //compare with serialized version to spot breaking changes
  //    val jsTree = writePretty(tree)
  //    val storedTree = resourceAsString("mocks/stored_array.json")
  //
  //    jsTree shouldBe storedTree
  //
  //  }
  //
  //  it should "de-serialize from an array" in {
  //    val tree = Tree.build(accounts = usersSerializationTest)
  //    val deserializedTree = read[Tree](resourceAsString("mocks/stored_array.json"))
  //
  //    tree.numNodes shouldBe deserializedTree.numNodes
  //    tree.maxDepth shouldBe deserializedTree.maxDepth
  //    tree.totalBalance shouldBe deserializedTree.totalBalance
  //  }

  it should "de-serialize a proof from file and check it against the root digest for user Bob" in {
    val tree = Tree.build(accounts = usersSerializationTest)
    val Some(proof) = tree.findProofByAccount(Account("Bob", 108, "raccoon"))
    writeToFile(writePretty(proof), "mocks/bob_proof.json")

    //digest from mock_data.json
    val rootDigest = "f61070df851b2fa44eb9f0bc63b69147229796068dd55676265f147d71b25ced"
    val bobProof = read[Proof](resourceAsString("mocks/bob_proof.json"))

    writePretty(bobProof) shouldBe writePretty(proof)

    bobProof.isValid(rootDigest, Account("Bob", 108, "raccoon")) shouldBe true
    bobProof.isValid(rootDigest, Account("Bob", 108, "rhino")) shouldBe false
    bobProof.isValid(rootDigest, Account("Bobby", 108, "raccoon")) shouldBe false
    bobProof.isValid(rootDigest, Account("Bob", 107, "raccoon")) shouldBe false

  }

}
