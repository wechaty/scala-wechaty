package xcoin.blockchain.internal.tron

import org.tron.trident.api.GrpcAPI.AccountAddressMessage
import org.tron.trident.core.ApiWrapper.parseAddress
import reactor.core.publisher.Mono
import xcoin.blockchain.services.TronApi.{ResourceRate, ResourceSupport}

trait TNCResourceSupport extends ResourceSupport {
  self: TronNodeClient =>
  private val DEFAULT_DETECTOR_ADDRESS = "TUfAMQM81RLMdquBSaFytsXxEet7AKKKKK"

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
