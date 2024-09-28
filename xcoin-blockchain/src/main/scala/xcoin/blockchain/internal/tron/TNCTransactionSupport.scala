package xcoin.blockchain.internal.tron

import org.bouncycastle.util.encoders.Hex
import org.springframework.util.{Assert, StringUtils}
import org.tron.trident.api.GrpcAPI.BytesMessage
import org.tron.trident.core.ApiWrapper.{parseAddress, parseHex}
import org.tron.trident.proto.Response.TransactionInfo
import reactor.core.publisher.Mono
import xcoin.blockchain.services.TronApi.TransactionSupport
import xcoin.core.services.XCoinException
import xcoin.core.services.XCoinException.{XFailRequestException, XResourceNotFoundException}

import scala.util.{Failure, Success}

trait TNCTransactionSupport extends TransactionSupport {
  self:TronNodeClient=>
  override def transactionByHash(txnId: String): Mono[TransactionInfoPayload] = {
    val bsTxid  = parseAddress(txnId)
    val request = BytesMessage.newBuilder.setValue(bsTxid).build

    stub
      .getTransactionInfoById(request)
      .map{txn=>
        logger.debug("txn:{}",Hex.toHexString(txn.toByteArray))
        val payload = new TransactionInfoPayload

        val txnHash  = new String(Hex.encode(txn.getId.toByteArray))
        payload.result = {
          txn.getResult match {
            case TransactionInfo.code.SUCESS => Success(txnHash)
            case TransactionInfo.code.FAILED => Failure(XFailRequestException(txn.getResMessage.toStringUtf8))
          }
        }

        if(!StringUtils.hasText(txnHash)) payload.result = Failure(XResourceNotFoundException(txnId))

        payload.id = txnHash
        payload.blockNumber = txn.getBlockNumber.intValue
        payload.receipt.energy_fee = txn.getReceipt.getEnergyFee
        payload.receipt.net_fee = txn.getReceipt.getNetFee

        payload.receipt.energy_usage_total = txn.getReceipt.getEnergyUsageTotal
        payload.receipt.energy_usage = txn.getReceipt.getEnergyUsage
        payload.receipt.net_usage = txn.getReceipt.getNetUsage

        payload
      }
  }
}
