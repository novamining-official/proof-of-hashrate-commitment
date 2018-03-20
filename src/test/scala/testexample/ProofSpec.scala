package testexample

import java.nio.file.{ Files, Paths }

import common.JsonSupport
import scala.io.Source
import proof.MerkleTree.{ Account, Tree }
import proof.MerkleTree._
import proof.MerkleTree.CHAIN_ID._
import org.scalatest._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import proof.ProofOfHashrate

import scala.util.Random

class ProofSpec extends FlatSpec with Matchers with JsonSupport {

  def resourceAsString(fileName: String) = Source.fromURL(getClass.getResource(s"/$fileName")).mkString
  def writeToFile(data: String, fileName: String) = Files.write(
    Paths.get(s"src/test/resources/$fileName"),
    data.getBytes
  )

  lazy val passingTestMock = resourceAsString("mocks/mock_data.json")
  lazy val accountsTestMock = resourceAsString("mocks/accounts.json")

  lazy val randomAccounts: Stream[Account] = Account(
    user = Random.alphanumeric.take(6).mkString,
    balance = Random.nextInt,
    nonce = Random.alphanumeric.take(4).mkString
  ) #:: randomAccounts

  /**
   *  The merkle tree is balanced and contain the input data on the leaves,
   *  we can compute the number of nodes by getting the smaller power
   *  of 2 which is bigger than the input size; from that we subtract the rest due to
   *  the 'empty' space left on the right hand side of the tree (the tree levels fills left to right).
   *  If the tree is balanced its max depth should be at most log2(numNodes)
   */
  private def checkTreeMetrics(tree: Tree, users: Seq[Account]) = {

    val expectedTotalBalance = users.map(_.balance).sum
    val expectedNumNodes = 1 - (2 * (1 - users.size))
    val expectedMaxDepth = math.log(expectedNumNodes) / math.log(2)

    tree.numNodes shouldBe expectedNumNodes
    tree.maxDepth shouldBe expectedMaxDepth.toInt +- 1
    tree.totalBalance shouldBe expectedTotalBalance

  }

  it should "Construct a tree and a valid proof" in {

    val users = parse(passingTestMock).extract[Seq[Account]]

    val tree = Tree.build(accounts = users)
    val rootDigest = tree.rootDigest

    tree.numNodes shouldBe 15
    tree.maxDepth shouldBe 4
    tree.totalBalance shouldBe 387

    val existingAccount = Account("Alice", 38, "rhino")
    val nonExistingAccount = Account("Mallory", 31, "cat")
    val Some(proof) = tree.findProofByAccount(existingAccount)

    tree.findProofByAccount(nonExistingAccount) shouldBe None
    proof.isValid(rootDigest, existingAccount) shouldBe true

  }

  it should "validate the proof from external mock data account.json" in {
    val users = parse(accountsTestMock).extract[List[Account]]

    val expectedNumNodes = 33

    val tree = Tree.build(accounts = users)
    val rootDigest = tree.rootDigest

    val accountToCheck = Account("mark", 462, "falcon")
    val Some(proof) = tree.findProofByAccount(accountToCheck)

    tree.numNodes shouldBe expectedNumNodes
    tree.totalBalance shouldBe 37618

    proof.isValid(rootDigest, accountToCheck) shouldBe true

    //Also the validation should fail if the account name, nonce  or balance is incorrect
    proof.isValid(rootDigest, Account("mark", 666, "falcon")) shouldBe false
    proof.isValid(rootDigest, Account("mark", 666, "cheetah")) shouldBe false
    proof.isValid(rootDigest, Account("markzz", 462, "falcon")) shouldBe false

  }

  it should "not find a proof if the tree does not contain a certain user" in {
    val users = parse(passingTestMock).extract[Seq[Account]]
    val tree = Tree.build(accounts = users)
    tree.hasProofFor(Account("nope", 12, "cheetah")) shouldBe false
  }

  it should "validate a proof correctly (failing) given a wrong root digest" in {
    val users = parse(passingTestMock).extract[Seq[Account]]
    val tree = Tree.build(accounts = users)
    val Some(proof) = tree.findProofByAccount(Account("Bob", 108, "raccoon"))

    val correctRootDigest = tree.rootDigest
    val wrongDigest = Node.sha256("Yo")

    proof.isValid(correctRootDigest, Account("Bob", 108, "raccoon")) shouldBe true
    proof.isValid(wrongDigest, Account("Bob", 108, "raccoon")) shouldBe false

  }

  it should "read a proof from file and check it against the root digest for user Bob" in {

    //digest from mock_data.json
    val rootDigest = "f61070df851b2fa44eb9f0bc63b69147229796068dd55676265f147d71b25ced"
    val bobProof = read[ProofOfHashrate.Proof](resourceAsString("mocks/bob_proof.json"))

    bobProof.isValid(rootDigest, Account("Bob", 108, "raccoon")) shouldBe true
    bobProof.isValid(rootDigest, Account("Bob", 108, "rhino")) shouldBe false
    bobProof.isValid(rootDigest, Account("Bobby", 108, "raccoon")) shouldBe false
    bobProof.isValid(rootDigest, Account("Bob", 107, "raccoon")) shouldBe false

  }

  it should "add an account and recompute the tree accordingly" in {

    val users = parse(passingTestMock).extract[Seq[Account]]
    val tree = Tree.build(accounts = users)

    val accountToAdd = Account("Diana", 223, "panther")

    tree.hasProofFor(accountToAdd) shouldBe false

    val updatedTree = tree.addAccount(accountToAdd)
    updatedTree.rootDigest != tree.rootDigest shouldBe true
    updatedTree.numNodes shouldBe tree.numNodes +- 2 //2 because if the tree is complete we go one level deeper and add 2 nodes
    updatedTree.totalBalance shouldBe tree.totalBalance + accountToAdd.balance
    updatedTree.hasProofFor(accountToAdd) shouldBe true

    val Some(proof) = updatedTree.findProofByAccount(accountToAdd)
    proof.isValid(updatedTree.rootDigest, accountToAdd) shouldBe true

  }

  it should "be a balanced tree" in {

    //with power of two
    val eightUsers = randomAccounts.take(8).toList
    checkTreeMetrics(Tree.build(accounts = eightUsers), eightUsers)

    //with power of two - 1
    val fourteen = randomAccounts.take(16).toList
    checkTreeMetrics(Tree.build(accounts = fourteen), fourteen)

    //with power of two + 1
    val seventeen = randomAccounts.take(17).toList
    checkTreeMetrics(Tree.build(accounts = seventeen), seventeen)

    //with a lot of users
    val manyUsers = randomAccounts.take(4712).toList
    checkTreeMetrics(Tree.build(accounts = manyUsers), manyUsers)

  }

  it should "serialize to an array using binary heap" in {
    val users = parse(passingTestMock).extract[Seq[Account]]
    val tree = Tree.build(accounts = users)

    val array = Tree.toArray(tree)
    val Some(rootNode) = array(0)

    array.filter(_.isDefined).size shouldBe tree.numNodes
    rootNode.leftValue + rootNode.rightValue shouldBe tree.totalBalance
    rootNode.id shouldBe tree.rootDigest

    //compare with serialized version to spot breaking changes
    val jsTree = writePretty(tree)
    val storedTree = resourceAsString("mocks/stored_array.json")

    jsTree shouldBe storedTree

  }

  it should "de-serialize from an array" in {
    val users = parse(passingTestMock).extract[Seq[Account]]

    val tree = parse(resourceAsString("mocks/stored_array.json")).extract[Tree]
    val buildTree = Tree.build(accounts = users)

    buildTree.numNodes shouldBe tree.numNodes
    buildTree.maxDepth shouldBe tree.maxDepth
    buildTree.totalBalance shouldBe tree.totalBalance
  }
}
