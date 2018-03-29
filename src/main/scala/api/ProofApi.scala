package api

import akka.http.scaladsl.server.Directives
import com.typesafe.scalalogging.LazyLogging
import common.JsonSupport
import proof.MerkleTree.CHAIN_ID._
import db.TreeManager
import proof.MerkleTree.Account

trait ProofApi extends JsonSupport with Directives with LazyLogging {

  def proofRoute = post {
    pathPrefix("proof" / Segment) { rootDigest =>
      entity(as[Account]) { account =>
        pathEnd {
          complete(TreeManager.findProof(BITCOIN_CHAIN, rootDigest, account))
        } ~ path("verify") {
          complete("")
        }
      }
    }
  }

}
