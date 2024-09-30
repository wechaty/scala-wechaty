package xcoin.blockchain.tron.internal

import org.tron.trident.api.GrpcAPI.AccountAddressMessage
import org.tron.trident.core.ApiWrapper.parseAddress
import org.tron.trident.proto.Chain.Transaction
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType
import org.tron.trident.proto.Contract.{DelegateResourceContract, UnDelegateResourceContract}
import reactor.core.publisher.Mono
import xcoin.blockchain.tron.services.TronApi.{ResourceRate, ResourceSupport}
import xcoin.blockchain.tron.services.TronBridge.ResourceType
import xcoin.blockchain.tron.services.TronModel.ResourceTypWrapper

import java.time.Duration

trait DefaultResourceSupport extends ResourceSupport {
  self: TronNodeClient =>
  private val DEFAULT_DETECTOR_ADDRESS = "TUfAMQM81RLMdquBSaFytsXxEet7AKKKKK"

  override def resourceDelegate(owner:String,receiver:String,stakedTRXSun:Long,resourceType:ResourceType,lockDuration:Duration=Duration.ZERO):Mono[Transaction]={
    val rawOwner                 = parseAddress(owner)
    val rawReceiver              = parseAddress(receiver)
    var delegateResourceContract = DelegateResourceContract.newBuilder
      .setOwnerAddress(rawOwner)
      .setBalance(stakedTRXSun)
      .setReceiverAddress(rawReceiver)
      .setResourceValue(resourceType.toTronResourceCode.getNumber).build
    if (lockDuration.toSeconds > 0)
      delegateResourceContract =
        delegateResourceContract.toBuilder.setLockPeriod(lockDuration.toSeconds / 3).build

    contractCreateTransaction(delegateResourceContract, ContractType.DelegateResourceContract)
      .map(_.getTransaction)
  }

  override def resourceReclaim(owner: String, receiver: String, stakedTRXSun: Long, resourceType: ResourceType): Mono[Transaction] = {
    val rawOwner                   = parseAddress(owner)
    val rawReceiver                = parseAddress(receiver)
    val unDelegateResourceContract = UnDelegateResourceContract.newBuilder
      .setOwnerAddress(rawOwner)
      .setBalance(stakedTRXSun)
      .setReceiverAddress(rawReceiver)
      .setResourceValue(resourceType.toTronResourceCode.getNumber).build

    contractCreateTransaction(unDelegateResourceContract, ContractType.UnDelegateResourceContract)
      .map(_.getTransaction)
  }

  override def resourceRate(): Mono[ResourceRate] = {
    val bsAddress = parseAddress(DEFAULT_DETECTOR_ADDRESS)
    val account   = AccountAddressMessage.newBuilder.setAddress(bsAddress).build
    stub
      .getAccountResource(account)
      .map { accountResource =>
        val rate = accountResource.getTotalEnergyWeight * 1_000_000.0 / accountResource.getTotalEnergyLimit
        logger.info("new energy rate {} from {} {}", rate, accountResource.getTotalEnergyLimit, accountResource.getTotalEnergyWeight)
        val bandwidthRate = accountResource.getTotalNetWeight * 1_000_000.0 / accountResource.getTotalNetLimit
        logger.info("new bandwidth rate {} from {} {}", bandwidthRate, accountResource.getTotalNetLimit, accountResource.getTotalNetWeight)
        ResourceRate(rate, bandwidthRate)
      }
  }
}
