package main

import api.TreeApi
import org.scalatest.{ FlatSpec, Matchers }
import akka.http.scaladsl.testkit._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.RouteResult.Rejected
import db.TreeManager
import main.Helpers.{ CleanDBStore, accountsTestMock }
import org.json4s.jackson.JsonMethods.parse
import proof.MerkleTree.Tree
import proof.domain.Account
import proof.domain.CHAIN_ID.BITCOIN_CHAIN

import scala.concurrent.duration._
import scala.concurrent.Await

class TreeApiSpec extends FlatSpec with Matchers with ScalatestRouteTest with CleanDBStore {

  trait mockedScope extends TreeApi {
    lazy val mockAccountsTest = parse(accountsTestMock).extract[Seq[Account]]
    val rootDigest = Await.result(TreeManager.createAndSaveTree(BITCOIN_CHAIN, mockAccountsTest), 3 seconds)
  }

  it should "give serve a tree when invoked" in new mockedScope {

    Get(s"/tree/$rootDigest?chainId=$BITCOIN_CHAIN") ~> treeRoute ~> check {

      handled shouldBe true
      status shouldBe OK
      val tree = entityAs[Tree]

      tree.rootDigest shouldBe rootDigest

    }

  }

  it should "reject a request if no or wrong chainId is specified" in new mockedScope {

    //TODO improve behavior
    Get(s"/tree/$rootDigest?chainId=SOMETHING_HERE") ~> treeRoute ~> check {

      handled shouldBe true
      status shouldBe InternalServerError

    }

  }

}
