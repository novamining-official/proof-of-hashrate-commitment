package api

import akka.http.scaladsl.server.Directives
import common.JsonSupport
import db.{ TreeManager, TreeStore }
import proof.MerkleTree.Tree
import proof.domain._

trait TreeApi extends JsonSupport with Directives {

  def treeRoute = pathPrefix("tree") {
    parameter('chainId) { chainId =>
      get {
        pathPrefix(Segment) { rootDigest =>
          pathEnd {
            complete(TreeManager.findTree(CHAIN_ID.withName(chainId), rootDigest))
          } ~ path("accounts") {
            complete(TreeManager.findTree(CHAIN_ID.withName(chainId), rootDigest).map(_.accounts))
          }
        }
      } ~ put {
        pathEnd {
          entity(as[Seq[Account]]) { accounts =>
            complete(TreeManager.createAndSaveTree(CHAIN_ID.withName(chainId), accounts))
          }
        }
      }
    } ~ get {
      path("allDigest") {
        complete(TreeManager.allTreesDigest())
      }
    }
  }

}
