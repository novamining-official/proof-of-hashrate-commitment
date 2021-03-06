package main

import common.JsonSupport
import proof.MerkleTree.Tree
import proof.MerkleTree._
import org.json4s.jackson.Serialization._
import org.scalatest._
import org.json4s.jackson.JsonMethods._
import Helpers._
import proof.ProofOfHashrate.Proof
import proof.domain.Account

class ProofSpec extends FlatSpec with Matchers with JsonSupport {

  lazy val users = parse(passingTestMock).extract[Seq[Account]]

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

  it should "construct a tree with only one account inside" in {
    val account = Account("firstUser", 1, "cheetah")
    val tree = Tree.build(accounts = Seq(account))

    tree.totalBalance shouldBe 1

    val Some(proof) = tree.findProofByAccount(account)

    proof.isValid(tree.rootDigest, account) shouldBe true

  }

  it should "be able to use data-rich account information as specified in the XNM whitepaper" in {

    val user_hashrate = 123
    val return_address = "35DM46fq45M7iU7GPVxLyRjuiE1EAReZn6"
    val locktime_contract = "10000"
    val coinbase_field = s"Using the XNM for user $return_address"
    val upgrade_versionfield = 0

    val accountString = s"$return_address | $locktime_contract | $coinbase_field | $upgrade_versionfield"

    val richAccount = Account(accountString, user_hashrate, "nonce-here")
    val accountList = richAccount :: randomAccounts.take(12).toList

    val tree = Tree.build(accounts = accountList)
    val Some(proof) = tree.findProofByAccount(richAccount)

    proof.isValid(tree.rootDigest, richAccount) shouldBe true
    richAccount.user.contains(return_address) shouldBe true
    richAccount.user.contains(coinbase_field) shouldBe true
    richAccount.user.contains(locktime_contract) shouldBe true

  }

  it should "construct a tree and a valid proof" in {
    val tree = Tree.build(accounts = users)
    val rootDigest = tree.rootDigest

    tree.numNodes shouldBe 15
    tree.maxDepth shouldBe 4
    tree.totalBalance shouldBe 387

    val existingAccount = Account("Satoshi", 21, "turtle")
    val nonExistingAccount = Account("Mallory", 31, "cat")
    val Some(proof) = tree.findProofByAccount(existingAccount)

    tree.findProofByAccount(nonExistingAccount) shouldBe None
    proof.isValid(rootDigest, existingAccount) shouldBe true

  }

  it should "validate the proof from external mock data account.json" in {
    val accounts = parse(accountsTestMock).extract[List[Account]]
    val expectedNumNodes = 33

    val tree = Tree.build(accounts = accounts)
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
    val tree = Tree.build(accounts = users)
    tree.hasProofFor(Account("nope", 12, "cheetah")) shouldBe false
  }

  it should "validate a proof correctly (failing) given a wrong root digest" in {
    val tree = Tree.build(accounts = users)
    val Some(proof) = tree.findProofByAccount(Account("Bob", 108, "raccoon"))

    val correctRootDigest = tree.rootDigest
    val wrongDigest = Node.sha256("Yo")

    proof.isValid(correctRootDigest, Account("Bob", 108, "raccoon")) shouldBe true
    proof.isValid(wrongDigest, Account("Bob", 108, "raccoon")) shouldBe false

  }

  it should "add an account and recompute the tree accordingly" in {
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

    //with a lot of mockAccounts
    val manyUsers = randomAccounts.take(4712).toList
    checkTreeMetrics(Tree.build(accounts = manyUsers), manyUsers)

  }

}
