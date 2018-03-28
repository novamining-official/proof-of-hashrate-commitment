package api

import akka.http.scaladsl.server.Directives
import common.JsonSupport
import db.TreeStore
import proof.MerkleTree.CHAIN_ID._

trait TreeApi extends JsonSupport with Directives {

  def treeRoute = get {
    pathPrefix("tree" / Segment) { rootDigest =>
      path("accounts") {
        complete(TreeStore.findTree(BITCOIN_CHAIN, rootDigest).map(_.accounts))
      }
    }
  }

}
