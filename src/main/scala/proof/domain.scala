package proof

import proof.ProofOfHashrate.Proof

import scala.math.Ordered

package object domain {

  case class TreeDigestChainID(
    chainId: CHAIN_ID.Value,
    digest: String
  )

  case class ProofAccountCheck(
    proof: Proof,
    account: Account
  )

  case class Account(
    user: String,
    balance: Double,
    nonce: String
  ) extends Ordered[Account] {
    //lexicographical ordering
    def compare(that: Account): Int = this.user.compareTo(that.user)

  }

  case object CHAIN_ID extends Enumeration {
    type CHAIN_ID = Value

    val BITCOIN_CHAIN = Value("BITCOIN_CHAIN")
    val ETHEREUM_CHAIN = Value("ETHEREUM_CHAIN")
  }

}
