package api

import akka.http.scaladsl.server.Directives
import common.JsonSupport
import db.TreeStore
import proof.MerkleTree.{Account, Tree}
import proof.MerkleTree.CHAIN_ID._

trait TreeApi extends JsonSupport with Directives {

  def treeRoute = pathPrefix("tree") {
    get {
      pathPrefix(Segment) { rootDigest =>
        path("accounts") {
          complete(TreeStore.findTree(BITCOIN_CHAIN, rootDigest).map(_.accounts))
        }
      }
    } ~ put {
      pathEnd {
        entity(as[Seq[Account]]) { accounts =>
          complete {
            val tree = Tree.build(BITCOIN_CHAIN, accounts)
            TreeStore.saveTree(tree)
            tree.rootDigest
          }
        }
      }
    }
  }

}
