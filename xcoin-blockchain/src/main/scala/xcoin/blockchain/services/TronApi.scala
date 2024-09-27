package xcoin.blockchain.services

import com.fasterxml.jackson.annotation.JsonProperty
import reactor.core.publisher.{Flux, Mono}
import xcoin.blockchain.services.TronApi.TronNodeClientNetwork

import scala.util.Try

trait TronApi extends TransactionSupport with VoteSupport {
}
trait TronNodeClientBuilder{
  def network(network:TronNodeClientNetwork.Type): Unit
  def apiKeys(keys:Array[String]): Unit
  def build():TronApi
}

trait TronNodeClientCustomizer{
  def customize(tronNodeClientBuilder: TronNodeClientBuilder)
}
trait TransactionSupport {
  def transactionByHash(txnHash: String): Mono[TronApi.TransactionInfoPayload]
}
trait VoteSupport{
  def voteList():Flux[Witness]
  class Witness{
    var address:String = _
    var voteCount:Long = _
    var voterRate:Int = _
    var isSR:Boolean = false

    var reward:BigDecimal = _
    var apr:BigDecimal = _

    override def toString: String = {
      s"$address:$apr"
    }
  }
}
object TronApi {
  object TronNodeClientNetwork extends Enumeration {
    type Type = Value
    val MAIN: Type = Value(0)
    val TEST_NILE:Type = Value(1)
    val TEST_SHASTA:Type= Value(2)
  }

    class TransactionInfoPayload {
      var result:Try[String] = _

      var id         : String                 = _
      @JsonProperty("blockNumber")
      var blockNumber: Int                    = _
      var receipt    : TransactionInfoReceipt = new TransactionInfoReceipt

      override def toString: String = {
        s"$id $blockNumber $receipt"
      }
    }

  class ContractState {
    var update_cycle : Int  = _
    var energy_usage : Long = _
    var energy_factor: Int  = _
  }

  class TransactionInfoReceipt {
    var energy_fee        : Long  = _ // 燃烧的能量trx
    var net_fee           : Long  = _
    var energy_usage      : Long  = _
    var energy_usage_total: Long  = _ // 消耗的总能量
    var net_usage         : Long  = _ // 消耗的带宽

    override def toString: String = {
      s"$energy_usage_total $net_usage"
    }
  }
}
