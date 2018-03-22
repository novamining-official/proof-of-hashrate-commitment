package common

import com.typesafe.scalalogging.LazyLogging
import org.json4s.JsonAST.{ JArray, JNull, JObject }
import org.json4s.{ CustomSerializer, JDouble, JField, JString, NoTypeHints, _ }
import org.json4s.jackson.Serialization
import proof.MerkleTree.{ Account, CHAIN_ID, Node, Tree }
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

trait JsonSupport extends LazyLogging {

  implicit val formats = Serialization.formats(NoTypeHints) ++ Seq(ChainIdSerializer, NodesSerializer)

  //TODO use json4s facilities
  case object ChainIdSerializer extends CustomSerializer[CHAIN_ID.Value](formats => (
    {
      case JString(str) => CHAIN_ID.withName(str)
      case JNull        => throw new IllegalStateException(s"Found null chainId")
    },
    {
      case chainId: CHAIN_ID.Value => JString(chainId.toString)
    }
  ))

  case object NodesSerializer extends CustomSerializer[Node](formats => (
    {
      case JArray(jObjectsList) =>
        val nodes = jObjectsList.map {
          _ match {
            case JObject(List(
              ("id", JString(id)),
              ("leftValue", JDouble(leftValue)),
              ("rightValue", JDouble(rightValue))
              )) => Some(Node(id, leftValue, rightValue))
            case _ => None
          }
        }

        Tree.fromArray(nodes.toArray[Option[Node]]) match {
          case Some(tree) => tree
          case None       => throw new IllegalArgumentException(s"Unable to parse Tree from ${(JArray(jObjectsList)).toString}")
        }
    },
    {
      case root: Node =>
        val nodesArray = Tree.toArray(root, 1024).map {
          case None => JNull
          case Some(node) => JObject(
            JField("id", JString(node.id)),
            JField("leftValue", JDouble(node.leftValue)),
            JField("rightValue", JDouble(node.rightValue))
          )
        }
        JArray(nodesArray.toList)
    }
  ))

}
