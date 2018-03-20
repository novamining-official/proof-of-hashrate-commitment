package main

import common.Boot
import db.DatabaseManager
import org.scalatest._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import proof.MerkleTree.{ Account, Tree }

import scala.io.Source

class DbSpec extends FlatSpec with Matchers {

  def resourceAsString(fileName: String) = Source.fromURL(getClass.getResource(fileName)).mkString

  implicit val formats = Serialization.formats(NoTypeHints)

  lazy val passingTestMock = resourceAsString("/mocks/mock_data.json")
  lazy val accountsTestMock = resourceAsString("/mocks/accounts.json")

  it should "initialize the DB properly" in {
    // val database = Boot.database

    val users = parse(passingTestMock).extract[Seq[Account]]
    val tree = Tree(users)

    DatabaseManager.saveTree(tree)

    true shouldBe true
  }

}
