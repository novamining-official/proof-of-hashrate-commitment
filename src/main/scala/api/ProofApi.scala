package api

import akka.http.scaladsl.server.Directives
import com.typesafe.scalalogging.LazyLogging
import common.JsonSupport
import db.TreeManager
import proof.domain._

trait ProofApi extends JsonSupport with Directives with LazyLogging {

  def proofRoute = post {
    parameter('chainId) { chainId =>
      pathPrefix("proof" / Segment) { rootDigest =>
        entity(as[Account]) { account =>
          pathEnd {
            complete(TreeManager.findProof(CHAIN_ID.withName(chainId), rootDigest, account))
          } ~ path("verify") {
            entity(as[ProofAccountCheck]) { accCheck =>
              complete(s"${TreeManager.checkProof(rootDigest, accCheck.proof, accCheck.account)}")
            }
          }
        }
      }
    }
  }

}
