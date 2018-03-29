package api

import akka.http.scaladsl.server.Directives
import common.JsonSupport
import db.{ TreeManager, TreeStore }
import proof.MerkleTree.{ Account, Tree }
import proof.MerkleTree.CHAIN_ID._

trait TreeApi extends JsonSupport with Directives {

  def treeRoute = pathPrefix("tree") {
    get {
      pathPrefix(Segment) { rootDigest =>
        pathEnd {
          complete(TreeManager.findTree(BITCOIN_CHAIN, rootDigest))
        } ~ path("accounts") {
          complete(TreeManager.findTree(BITCOIN_CHAIN, rootDigest).map(_.accounts))
        }
      }
    } ~ put {
      pathEnd {
        entity(as[Seq[Account]]) { accounts =>
          complete(TreeManager.createAndSaveTree(BITCOIN_CHAIN, accounts))
        }
      }
    }
  }

}
