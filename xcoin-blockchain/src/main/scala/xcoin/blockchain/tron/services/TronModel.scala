package xcoin.blockchain.tron.services

import com.fasterxml.jackson.annotation.JsonIgnore
import org.tron.trident.proto.Common.{AccountType, ResourceCode}
import reactor.core.publisher.Mono
import TronApi.{ResourceRate, TronPermission}
import TronBridge.{CoinType, ResourceType}

object TronModel {
  trait BlockEvent
  case class BlockProcessedEvent(blockId:Long,blockTimestamp:Long) extends BlockEvent
  trait AddressChangeEvent extends BlockEvent {
    var blockId       : Long           = _
    var blockTimestamp: Long           = _
    var txnHash       : String         = _
    var note          : Option[String] = _

    var fee: Long = _ //手续费
  }

  case class AccountActivatedEvent(address: String, activatedAddress: String) extends AddressChangeEvent {}

  abstract class DelegateEnergyEvent(val address: String, val stakeAmountSun: Long, val relativeAddress: String) extends AddressChangeEvent

  case class DelegateEnergyOutEvent(override val address: String, override val stakeAmountSun: Long, override val relativeAddress: String) extends DelegateEnergyEvent(address, stakeAmountSun, relativeAddress)

  case class DelegateEnergyInEvent(override val address: String, override val stakeAmountSun: Long, override val relativeAddress: String) extends DelegateEnergyEvent(address, stakeAmountSun, relativeAddress)

  abstract class ReclaimEnergyEvent(val address: String, val stakeAmountSun: Long, val relativeAddress: String) extends AddressChangeEvent

  case class ReclaimEnergyOutEvent(override val address: String, override val stakeAmountSun: Long, override val relativeAddress: String) extends ReclaimEnergyEvent(address, stakeAmountSun, relativeAddress)

  case class ReclaimEnergyInEvent(override val address: String, override val stakeAmountSun: Long, override val relativeAddress: String) extends ReclaimEnergyEvent(address, stakeAmountSun, relativeAddress)

  abstract class ReclaimBandwidthEvent(val address: String, val stakeAmountSun: Long, val relativeAddress: String) extends AddressChangeEvent

  case class ReclaimBandwidthOutEvent(override val address: String, override val stakeAmountSun: Long, override val relativeAddress: String) extends ReclaimEnergyEvent(address, stakeAmountSun, relativeAddress)

  case class ReclaimBandwidthInEvent(override val address: String, override val stakeAmountSun: Long, override val relativeAddress: String) extends ReclaimEnergyEvent(address, stakeAmountSun, relativeAddress)

  abstract class TransferChangeEvent(val coinType: CoinType, val address: String, val amountSun: Long, val relativeAddress: String) extends AddressChangeEvent {
  }

  case class TransferOutEvent(override val coinType: CoinType, override val address: String, override val amountSun: Long, override val relativeAddress: String) extends TransferChangeEvent(coinType, address, amountSun, relativeAddress) {}

  case class TransferInEvent(override val coinType: CoinType, override val address: String, override val amountSun: Long, override val relativeAddress: String) extends TransferChangeEvent(coinType, address, amountSun, relativeAddress) {}

  case class TriggerSmartContractEvent(address: String, contractAddress: String) extends AddressChangeEvent

  class TronAccount {
    var `type`: AccountType = AccountType.Normal

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

  implicit class ResourceTypWrapper(v: ResourceType) {
    def toTronResourceCode = {
      v match {
        case ResourceType.ENERGY => ResourceCode.ENERGY
        case ResourceType.BANDWIDTH => ResourceCode.BANDWIDTH
      }
    }
  }
}
