import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import api.{ ProofApi, TreeApi }
import com.typesafe.scalalogging.LazyLogging
import common.Config._
import db.TreeStore

object Boot extends App with LazyLogging {

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  TreeStore.setup()
  logger.info(s"Done loading db")

  val routeContainer = new {} with TreeApi with ProofApi {
    val route = treeRoute ~ proofRoute
  }

  bindOrFail(routeContainer.route, apiHost, apiPort)

  def bindOrFail(handler: Route, host: String, port: Int): Unit = {
    Http().bindAndHandle(handler, host, port) map { binding =>
      logger.info(s"HTTP API bound to ${binding.localAddress}")
    } recover {
      case ex =>
        logger.error(s"Interface could not bind to $host:$port", ex)
        throw ex
    }
  }

}
