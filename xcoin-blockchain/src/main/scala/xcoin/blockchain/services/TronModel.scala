package xcoin.blockchain.services

import org.tron.trident.proto.Common.ResourceCode

object TronModel {
  object CoinType extends Enumeration {
    type Type = Value
    val USDT = Value(1)
    val TRX  = Value(2)
  }
  object ResourceType extends Enumeration {
    type Type = Value
    val ENERGY = Value(1)
    val BANDWIDTH = Value(2)
    implicit class ResourceTypWrapper(v:Type) {
      def toTronResourceCode = {
        this match {
          case ENERGY => ResourceCode.ENERGY
          case BANDWIDTH => ResourceCode.BANDWIDTH
        }
      }
    }
  }
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

  abstract class TransferChangeEvent(val coinType: CoinType.Type, val address: String, val amountSun: Long, val relativeAddress: String) extends AddressChangeEvent {
  }

  case class TransferOutEvent(override val coinType: CoinType.Type, override val address: String, override val amountSun: Long, override val relativeAddress: String) extends TransferChangeEvent(coinType, address, amountSun, relativeAddress) {}

  case class TransferInEvent(override val coinType: CoinType.Type, override val address: String, override val amountSun: Long, override val relativeAddress: String) extends TransferChangeEvent(coinType, address, amountSun, relativeAddress) {}

  case class TriggerSmartContractEvent(address: String, contractAddress: String) extends AddressChangeEvent

}