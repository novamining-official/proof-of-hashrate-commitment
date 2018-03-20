package common

import org.json4s.JsonAST.{ JArray, JNull, JObject }
import org.json4s.{ CustomSerializer, JDouble, JField, JString, NoTypeHints, _ }
import org.json4s.jackson.Serialization
import proof.MerkleTree.{ Account, CHAIN_ID, Node, Tree }
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

trait JsonSupport {

  implicit val formats = Serialization.formats(NoTypeHints) ++ Seq(ChainIdSerializer, NodesSerializer)

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
      case JArray(jFields) =>
        val fieldsToTree: List[Option[Node]] = jFields.map { jValue =>
          jValue match {
            case JObject(List(
              ("id", JString(id)),
              ("leftValue", JDouble(leftValue)),
              ("rightValue", JDouble(rightValue))
              )) => Some(Node(id, leftValue, rightValue))
          }
        }
        Tree.fromArray(fieldsToTree.toArray[Option[Node]]).get

    },
    {
      case root: Node =>
        val nodesArray = Tree.toArray(root, 1024).toList.filter(_.isDefined).map {
          case Some(node) => JObject(
            JField("id", JString(node.id)),
            JField("leftValue", JDouble(node.leftValue)),
            JField("rightValue", JDouble(node.rightValue))
          )
        }
        JArray(nodesArray)
    }
  ))

}
