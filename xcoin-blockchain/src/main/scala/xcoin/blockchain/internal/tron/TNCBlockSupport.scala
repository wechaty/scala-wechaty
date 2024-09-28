package xcoin.blockchain.internal.tron

import org.tron.trident.api.GrpcAPI.EmptyMessage
import reactor.core.publisher.Mono
import xcoin.blockchain.services.TronApi.BlockSupport
import xcoin.core.services.XCoinException.XInvalidReturnException

trait TNCBlockSupport extends BlockSupport {
  self: TronNodeClient =>
  override def blockLatestId(): Mono[Long] = {
    stub.getNowBlock(EmptyMessage.getDefaultInstance)
      .map { block =>
        if (block.hasBlockHeader) block.getBlockHeader.getRawData.getNumber
        else throw XInvalidReturnException("Fail to get latest block.")
      }
  }
}
