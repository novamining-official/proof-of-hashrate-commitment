package api

import akka.http.scaladsl.server.Directives
import common.JsonSupport
import proof.MerkleTree.Account

trait TreeApi extends JsonSupport with Directives {

  val treeApiRoute = get {
    path("tree") {
      complete(Account("Bob", 14, "cat"))
    }
  }

}
