package api

import akka.http.scaladsl.server.Directives
import common.JsonSupport
import proof.MerkleTree.Account

trait TreeApi extends JsonSupport with Directives {

  def treeRoute = get {
    path("tree") {
      complete(Account("Bob", 14, "cat"))
    }
  }

}
