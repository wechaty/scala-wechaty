package xcoin.blockchain.internal.tron

import org.bouncycastle.util.encoders.Hex
import org.tron.trident.abi.{FunctionEncoder, datatypes}
import org.tron.trident.core.ApiWrapper.{parseAddress, parseHex}
import org.tron.trident.proto.Contract.TriggerSmartContract
import org.tron.trident.proto.Response
import reactor.core.publisher.Mono
import xcoin.blockchain.services.TronApi.ContractSupport
import xcoin.core.services.XCoinException
import xcoin.core.services.XCoinException.InvalidReturn

trait TNCContractSupport extends ContractSupport {
  self: TronNodeClient =>
  override def contractTriggerConstant(owner: String, contractAddress: String, function: datatypes.Function, valueOpt: Option[Long]): Mono[Response.TransactionExtention] = {
    val rawFrom            = parseAddress(owner)
    val rawContractAddress = parseAddress(contractAddress)
    val encodedHex         = FunctionEncoder.encode(function)

    val triggerBuilder = TriggerSmartContract.newBuilder.setOwnerAddress(rawFrom)
      .setContractAddress(rawContractAddress)
      .setData(parseHex(encodedHex))

    valueOpt.foreach(triggerBuilder.setCallValue)

    stub.triggerConstantContract(triggerBuilder.build()).map { txn =>
      logger.debug("txn:{}",Hex.toHexString(txn.toByteArray))
      if(txn.getResult.getResult) txn
      else throw new XCoinException(InvalidReturn("invalid contract result"))
    }
  }
}
