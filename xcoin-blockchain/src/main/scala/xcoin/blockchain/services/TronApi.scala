package xcoin.blockchain.services

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty}
import org.tron.trident.abi.datatypes
import org.tron.trident.api.ReactorWalletGrpc.ReactorWalletStub
import org.tron.trident.api.ReactorWalletSolidityGrpc.ReactorWalletSolidityStub
import org.tron.trident.core.transaction.TransactionBuilder
import org.tron.trident.proto.Chain.Transaction
import org.tron.trident.proto.{Chain, Response}
import org.tron.trident.proto.Response.TransactionExtention
import reactor.core.publisher.{Flux, Mono}
import xcoin.blockchain.internal.tron.TronNodeClient
import xcoin.blockchain.services.TronApi.{ContractSupport, ResourceSupport, SimpleTronPermission, TransactionSupport, TronNodeClientNetwork, USDTSupport, VoteSupport}

import scala.util.Try

trait TronApi
  extends TransactionSupport
    with VoteSupport
    with ContractSupport
    with USDTSupport
    with ResourceSupport
    {
}
object TronApi {
  trait TronNodeClientBuilder {
    def buildTronNodeClient(walletStub: ReactorWalletStub, walletSolidityStub: ReactorWalletSolidityStub): TronApi

    def network(network: TronNodeClientNetwork.Type): Unit

    def apiKeys(keys: Array[String]): Unit

    def buildReactorWalletStub(): ReactorWalletStub

    def buildReactorWalletSolidityStub(): ReactorWalletSolidityStub
  }



  trait TronNodeClientCustomizer {
    def customize(tronNodeClientBuilder: TronNodeClientBuilder)
  }

  trait TransactionSupport {
    def transactionByHash(txnHash: String): Mono[TransactionInfoPayload]

    class TransactionInfoPayload {
      var result: Try[String] = _

      var id         : String                 = _
      @JsonProperty("blockNumber")
      var blockNumber: Int                    = _
      var receipt    : TransactionInfoReceipt = new TransactionInfoReceipt

      override def toString: String = {
        s"$id $blockNumber $receipt"
      }
    }

    class TransactionInfoReceipt {
      var energy_fee        : Long = _ // 燃烧的能量trx
      var net_fee           : Long = _
      var energy_usage      : Long = _
      var energy_usage_total: Long = _ // 消耗的总能量
      var net_usage         : Long = _ // 消耗的带宽

      override def toString: String = {
        s"$energy_usage_total $net_usage"
      }
    }
  }

  trait ContractSupport {
    def contractTriggerConstant(owner: String, contractAddress: String, function: org.tron.trident.abi.datatypes.Function, valueOpt: Option[Long] = None): Mono[Response.TransactionExtention];

    def contractTriggerCallBuilder(owner: String, contractAddress: String, function: datatypes.Function, valueOpt: Option[Long]): Mono[TransactionBuilder]

    def contractSign(permission: SimpleTronPermission, transaction: Chain.Transaction): TransactionSigned

    def contractBroadcast(txn: TransactionSigned): Mono[String]


    def contractSignAndBroadcast(permission: SimpleTronPermission, transaction: Transaction): Mono[String]
  }
  trait USDTSupport{
    def usdtBalanceOf(owner:String):Mono[Long]
  }

  trait VoteSupport {
    def voteList(topN:Int=127): Flux[Witness]

    class Witness {
      var address  : String  = _
      var voteCount: Long    = _
      var voterRate: Int     = _
      var isSR     : Boolean = false

      var reward: BigDecimal = _
      var apr   : BigDecimal = _

      override def toString: String = {
        s"$address:$apr"
      }
    }
  }

  trait AccountSupport {
    def accountGet(address: String): Mono[TronAccount]

    class TronAccount {
      var bandwidthRecoverTime: Long = _
      var energyRecoverTime   : Long = _

      var address                           : String = _
      // 余额
      var balanceSun                        : Long   = _
      // 创建时间
      var createdTime                       : Long   = _
      // 已使用带宽
      var bandwidthUsed                     : Long   = _
      // 由其他人委托的带宽
      var bandwidthDelegatedByOthersAmount  : Long   = _
      var bandwidthDelegatedByOthersV1Amount: Long   = _
      // 带宽委托给其他人
      var bandwidthDelegatedToOthersAmount  : Long   = _
      // 带宽冻结
      //      var bandwidthFrozen             : Long                  = _
      // 带宽冻结的TRX数
      var bandwidthFrozenAmount             : Long   = _

      // 账户质押获取能量的金额
      def bandwidthStakedAmount: Long = {
        bandwidthDelegatedToOthersAmount + bandwidthFrozenAmount
      }

      // 已使用带宽
      var energyUsed                     : Long = _
      // 能量委托给其他人
      var energyDelegatedToOthersAmount  : Long = _
      // 能量由其他人委托的
      var energyDelegatedByOthersAmount  : Long = _
      var energyDelegatedByOthersV1Amount: Long = _
      // 能量带宽冻结
      //      var energyFrozen                : Long                  = _
      // 能量带宽冻结的TRX
      var energyFrozenAmount             : Long = _
      @JsonIgnore
      private[xcoin] var rateMono: Mono[ResourceRate] = _

      // 账户质押获取能量的金额
      def energyStakedAmount: Long = {
        energyDelegatedToOthersAmount + energyFrozenAmount
      }

      // 账户权限
      var ownerPermission  : TronPermission        = _
      var witnessPermission: TronPermission        = _
      var activePermission : Array[TronPermission] = _

      // 可委托带宽
      def canDelegateBandwidth(): Mono[Long] = {
        canDelegateBandwidthAmount().flatMap { a =>
          rateMono.map { rate =>
            (a / rate.bandwidthRate).longValue
          }
        }
      }

      def canDelegateBandwidthAmount(): Mono[Long] = {
        availableBandwidthV2Amount().map { a =>
          math.min(bandwidthFrozenAmount, a)
        }
      }

      // 可委托能量
      def canDelegateEnergy(): Mono[Long] = {
        canDelegateEnergyAmount().flatMap { a =>
          rateMono.map { rate =>
            (a / rate.energyRate).longValue
          }
        }
      }

      def canDelegateEnergyAmount(): Mono[Long] = {
        availableEnergyV2Amount().map(x => math.min(energyFrozenAmount, x))
      }

      // 可用带宽
      def availableBandwidthV2Amount(): Mono[Long] = {
        rateMono.map { rate =>
          (bandwidthFrozenAmount + bandwidthDelegatedByOthersAmount - bandwidthUsed * rate.bandwidthRate).longValue
        }
      }


      //所有可用带宽，包含v1
      def availableBandwidthAll(): Mono[Long] = {
        availableBandwidthV2Amount().map { a =>
          a + bandwidthDelegatedByOthersV1Amount
        }
      }

      //所有可用能量，包含v1
      def availableEnergyAllAmount() = {
        availableEnergyV2Amount().map(_ + energyDelegatedByOthersV1Amount)
      }

      def availableEnergyV2Amount(): Mono[Long] = {
        rateMono.map { rate =>
          (energyFrozenAmount + energyDelegatedByOthersAmount - energyUsed * rate.energyRate).longValue
        }
      }

      // 总能量上限
      def limitEnergyAmount() = {
        energyFrozenAmount + energyDelegatedByOthersAmount
      }

      def limitEnergyAllAmount() = {
        energyFrozenAmount + energyDelegatedByOthersAmount + energyDelegatedByOthersV1Amount
      }

      def limitBandwidthAllAmount() = {
        bandwidthFrozenAmount + bandwidthDelegatedByOthersAmount + bandwidthDelegatedByOthersV1Amount
      }

      def limitBandwidthAmount() = {
        bandwidthFrozenAmount + bandwidthDelegatedByOthersAmount
      }

      override def toString: String = {
        //        s"energy: used:${energyUsed} toOthers:${energyDelegatedToOthers} byOthers:${energyDelegatedByOthers} frozen:${energyFrozen} available:${availableEnergyAll()} " +
        //          s"bandwidth: used:${bandwidthUsed} toOthers:${bandwidthDelegatedToOthers} byOthers:${bandwidthDelegatedByOthers} frozen:${bandwidthFrozen} available:${availableBandwidthAll()}"
        ""
      }
    }

    def accountBalanceOfUSDT(owner: String): Mono[Long]

    def accountTransferTRX(owner: String, target: String, amountSun: Long): Mono[Transaction]
  }
  trait ResourceSupport{
    def resourceRate():Mono[ResourceRate]

  }

  /**
   * 记录资源的比率
   *
   * @param energyRate    1能量需要质押的TRX(单位为SUN)
   * @param bandwidthRate 1带宽需要质押的TRX(单位为SUN)
   */
  case class ResourceRate(/** 1能量需要质押的TRX(单位为SUN) * */
                          energyRate: Double, bandwidthRate: Double)

  object TronNodeClientNetwork extends Enumeration {
    type Type = Value
    val MAIN       : Type = Value(0)
    val TEST_NILE  : Type = Value(1)
    val TEST_SHASTA: Type = Value(2)
  }

  class TronPermission{
    var name: String = _
    @JsonProperty("type")
    var `type`: TronPermissionType.Type = _
    var id: Int = _
    var threshold: Long = _
    var operations: Array[Byte]= _
    @JsonProperty("keys")
    var keys: Array[TronPermissionKey] = _
  }
  object TronPermissionType extends Enumeration {
    type Type=Value
    val OWNER=Value(0)
    val WITNESS=Value(1)
    val ACTIVE=Value(2)
  }
  class TronPermissionKey{
    var address:String = _
    var weight:Long= _
  }
  class SimpleTronPermission{
    var key:String = _
    var idOpt:Option[Int] = None
  }
  case class TransactionSigned(txnId: String, transaction: Transaction)
}
