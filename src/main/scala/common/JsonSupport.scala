package common

import org.json4s.JsonAST.JNull
import org.json4s.{ CustomSerializer, JString, NoTypeHints }
import org.json4s.jackson.Serialization
import proof.MerkleTree.CHAIN_ID

trait JsonSupport {

  implicit val formats = Serialization.formats(NoTypeHints) ++ Seq(ChainIdSerializer)

  case object ChainIdSerializer extends CustomSerializer[CHAIN_ID.Value](format => (
    {
      case JString(str) => CHAIN_ID.withName(str)
      case JNull        => throw new IllegalStateException(s"Found null chainId")
    },
    {
      case chainId: CHAIN_ID.Value => JString(chainId.toString)
    }
  ))

}
