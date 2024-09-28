package xcoin.blockchain.internal.tron

import org.bouncycastle.util.encoders.Hex
import org.tron.trident.abi.{FunctionReturnDecoder, TypeReference}
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.generated.Uint256
import reactor.core.publisher.Mono
import xcoin.blockchain.internal.tron.USDTSupport.{MAIN_USDT_CONTRACT_ADDRESS, NILE_USDT_CONTRACT_ADDRESS, SHASTA_USDT_CONTRACT_ADDRESS}
import xcoin.blockchain.services.TronApi.{TronNodeClientNetwork, USDTSupport}

import java.math.BigInteger

trait TNCUSDTSupport extends USDTSupport {
  self:TronNodeClient =>
  override def usdtBalanceOf(owner: String): Mono[Long] ={
    val balanceOf      = new org.tron.trident.abi.datatypes.Function("balanceOf", java.util.Arrays.asList(new Address(owner)), java.util.Arrays.asList(new TypeReference[Uint256]() {}))
    contractTriggerConstant(owner,usdtContractAddress(),balanceOf).map{txn=>
      logger.debug("txn:{}",Hex.toHexString(txn.toByteArray))
      val result = Hex.toHexString(txn.getConstantResult(0).toByteArray)
      val balance = FunctionReturnDecoder.decode(result, balanceOf.getOutputParameters)
        .get(0).getValue.asInstanceOf[BigInteger].longValue()
      logger.info("get {} balance is {}", owner, balance)
      balance
    }
  }
  private def usdtContractAddress(): String = {
    network match {
      case TronNodeClientNetwork.MAIN=> MAIN_USDT_CONTRACT_ADDRESS
      case TronNodeClientNetwork.TEST_NILE=> NILE_USDT_CONTRACT_ADDRESS
      case TronNodeClientNetwork.TEST_SHASTA=> SHASTA_USDT_CONTRACT_ADDRESS
    }

  }
}
object USDTSupport{
  final         val SHASTA_USDT_CONTRACT_ADDRESS   = "TG3XXyExBkPp9nzdajDZsozEu4BkaSJozs"
  private final val SHASTA_USDT_TRANSFER_METHOD_ID = "a9059cbb"
  //线上网络的UDST合约地址以及转账方法ID
  final         val MAIN_USDT_CONTRACT_ADDRESS     = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
  private final val MAIN_USDT_TRANSFER_METHOD_ID        = "a9059cbb"
  private final val MAIN_USDT_TRANSFER_FROM_METHOD_ID   = "23b872dd"
  final         val NILE_USDT_CONTRACT_ADDRESS   = "TXLAQ63Xg1NAzckPwKHvzw7CSEmLMEqcdj"

}
