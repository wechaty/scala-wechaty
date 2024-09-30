package xcoin.blockchain.services

import com.fasterxml.jackson.annotation.JsonProperty
import org.tron.trident.abi.datatypes
import org.tron.trident.api.ReactorWalletGrpc.ReactorWalletStub
import org.tron.trident.api.ReactorWalletSolidityGrpc.ReactorWalletSolidityStub
import org.tron.trident.core.transaction.TransactionBuilder
import org.tron.trident.proto.Chain.Transaction
import org.tron.trident.proto.{Chain, Response}
import reactor.core.publisher.{Flux, Mono}
import xcoin.blockchain.services.TronApi._
import xcoin.blockchain.services.TronBridge.ResourceType
import xcoin.blockchain.services.TronModel.TronAccount

import java.time.Duration
import scala.util.Try

trait TronApi
  extends TransactionSupport
    with VoteSupport
    with AccountSupport
    with ContractSupport
    with USDTSupport
    with BlockSupport
    with ResourceSupport
    {
}
object TronApi {
  trait TronNodeClientBuilder {
    def buildTronNodeClient(walletStub: ReactorWalletStub, walletSolidityStub: ReactorWalletSolidityStub): TronApi

    def network(network: TronNodeClientNetwork.Type): Unit

    def apiKeys(keys: Array[String]): Unit
    def quickNodeKey(key:String):Unit

    def buildReactorWalletStub(): ReactorWalletStub

    def buildReactorWalletSolidityStub(): ReactorWalletSolidityStub
  }



  trait TronNodeClientCustomizer {
    def customize(tronNodeClientBuilder: TronNodeClientBuilder):Unit
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


    def accountBalanceOfUSDT(owner: String): Mono[Long]

    def accountTransferTRX(owner: String, target: String, amountSun: Long): Mono[Transaction]

    def accountActivate(owner: String, accountAddress: String): Mono[Transaction]
    def accountIsNormalActivated(accountAddress:String):Mono[Boolean]
  }

  trait BlockSupport {
    def blockLatestId(): Mono[Long]

    /**
     * 从某一个区块开始读取链上的事件，该方法无限输出
     * @param start 开始的位置
     * @param blockSegmentSize 每次读取的块长度
     * @return 区块上的时间
     */
    def blockEventStream(start: Long, blockSegmentSize: Int = 10): Flux[TronModel.BlockEvent]

    /**
     * 得到某一个区块的事件
     * @param blockId 区块的ID
     * @return 区块的事件
     */
    def blockEvent(blockId: Long): Flux[TronModel.BlockEvent]
  }

  trait ResourceSupport {
    /**
     * 得到1资源需要质押的TRX(单位为SUN)
     *
     * @return 资源比例
     * @see [[ResourceRate]]
     */
    def resourceRate(): Mono[ResourceRate]

    /**
     * 代理资源
     *
     * @param owner        资源所属地址
     * @param receiver     资源接受地址
     * @param stakedTRXSun 资源对应需要质押的TRX数(单位sun)
     * @param resourceType 资源类型
     * @param lockDuration 锁定的时间段
     * @return 交易数据
     */
    def resourceDelegate(owner: String, receiver: String, stakedTRXSun: Long, resourceType: ResourceType, lockDuration: Duration = Duration.ZERO): Mono[Transaction]

    /**
     * 代理资源
     *
     * @param owner        资源所属地址
     * @param receiver     资源接受地址
     * @param stakedTRXSun 资源对应需要质押的TRX数(单位sun)
     * @param resourceType 资源类型
     * @return 交易数据
     */
    def resourceReclaim(owner: String, receiver: String, stakedTRXSun: Long, resourceType: ResourceType): Mono[Transaction]
  }

  /**
   * 记录资源的比率
   *
   * @param energyRate    1能量需要质押的TRX(单位为SUN)
   * @param bandwidthRate 1带宽需要质押的TRX(单位为SUN)
   */
  case class ResourceRate(/** 1能量需要质押的TRX(单位为SUN) * */
                          energyRate: Double,
                          /** 1带宽需要质押的TRX(单位为SUN) * */
                          bandwidthRate: Double)

  object TronNodeClientNetwork extends Enumeration {
    type Type = Value
    val MAIN       : Type = Value(0)
    val TEST_NILE  : Type = Value(1)
    val TEST_SHASTA: Type = Value(2)
    val MAIN_QUICK_NODE: Type = Value(3)
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
