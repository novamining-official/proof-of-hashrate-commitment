package api

import akka.http.scaladsl.server.Directives
import com.typesafe.scalalogging.LazyLogging
import common.JsonSupport

trait ProofApi extends JsonSupport with Directives with LazyLogging {

  def proofRoute = get {
    path("proof" / LongNumber) { proofNum =>
      complete(s"Got $proofNum")
    }
  }

}
