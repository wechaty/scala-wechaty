package xcoin.blockchain.internal.tron

import com.typesafe.scalalogging.Logger
import org.bouncycastle.util.encoders.Hex
import org.tron.trident.abi.TypeDecoder
import org.tron.trident.abi.datatypes.generated.Uint256
import org.tron.trident.api.GrpcAPI.EmptyMessage
import org.tron.trident.proto.{Common, Contract}
import org.tron.trident.proto.Contract.{TransferContract, TriggerSmartContract}
import org.tron.trident.utils.Base58Check.bytesToBase58
import reactor.core.publisher.Mono
import xcoin.blockchain.internal.tron.USDTSupport.{MAIN_USDT_CONTRACT_ADDRESS, MAIN_USDT_TRANSFER_FROM_METHOD_ID, MAIN_USDT_TRANSFER_METHOD_ID, SHASTA_USDT_CONTRACT_ADDRESS, SHASTA_USDT_TRANSFER_METHOD_ID}
import xcoin.blockchain.services.TronApi.{BlockSupport, TronNodeClientNetwork}
import xcoin.blockchain.services.TronModel._
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
object BlockSupport{
  private val logger = Logger(getClass)
  def parseTransferContract(transferContract: TransferContract): List[TransferChangeEvent] = {
    val from   = bytesToBase58(transferContract.getOwnerAddress.toByteArray)
    val to     = bytesToBase58(transferContract.getToAddress.toByteArray)
    val amount = transferContract.getAmount
    List(TransferOutEvent(CoinType.TRX, from, amount, to), TransferInEvent(CoinType.TRX, to, amount, from))
  }

  def parse(delegateResource: Contract.DelegateResourceContract): List[DelegateEnergyEvent] = {
    if (delegateResource.getResource == Common.ResourceCode.ENERGY) {
      val from   = bytesToBase58(delegateResource.getOwnerAddress.toByteArray)
      val to     = bytesToBase58(delegateResource.getReceiverAddress.toByteArray)
      val amount = delegateResource.getBalance
      List(DelegateEnergyOutEvent(from, amount, to), DelegateEnergyInEvent(to, amount, from))
    } else List()
  }

  def parse(delegateResource: Contract.UnDelegateResourceContract): List[ReclaimEnergyEvent] = {
    if (delegateResource.getResource == Common.ResourceCode.ENERGY) {
      val from   = bytesToBase58(delegateResource.getOwnerAddress.toByteArray)
      val to     = bytesToBase58(delegateResource.getReceiverAddress.toByteArray)
      val amount = delegateResource.getBalance
      List(ReclaimEnergyOutEvent(from, amount, to), ReclaimEnergyInEvent(to, amount, from))
    } else if (delegateResource.getResource == Common.ResourceCode.BANDWIDTH) {
      val from   = bytesToBase58(delegateResource.getOwnerAddress.toByteArray)
      val to     = bytesToBase58(delegateResource.getReceiverAddress.toByteArray)
      val amount = delegateResource.getBalance
      List(ReclaimBandwidthOutEvent(from, amount, to), ReclaimBandwidthInEvent(to, amount, from))
    }
    else List()
  }

  /**
   * 解析USDT转账的合约
   *
   * @param contract
   * @return
   */
  //https://developers.tron.network/docs/parameter-encoding-and-decoding
  def parseUSDTSmartTrigger(smartContract: TriggerSmartContract, txnHash: String, network:TronNodeClientNetwork.Type): List[TransferChangeEvent] = {
    val DATA            = new String(Hex.encode(smartContract.getData.toByteArray))
    val contractAddress = bytesToBase58(smartContract.getContractAddress.toByteArray)
    val ownerAddress    = bytesToBase58(smartContract.getOwnerAddress.toByteArray)

    //USDT的转账参数为136位长度
    if (DATA.length < 136) {
      return List.empty
    }
    var list = List[TransferChangeEvent]()

    val methodId = DATA.substring(0, 8);

    val isMain = network == TronNodeClientNetwork.MAIN

    if (
    //非主网络
      (!isMain && contractAddress == SHASTA_USDT_CONTRACT_ADDRESS && methodId == SHASTA_USDT_TRANSFER_METHOD_ID) ||
        //主网络
        (isMain && contractAddress == MAIN_USDT_CONTRACT_ADDRESS && methodId == MAIN_USDT_TRANSFER_METHOD_ID)
    ) {
      try {
        val rawRecipient   = TypeDecoder.decodeAddress(DATA.substring(8, 72)) //, 0, new TypeReference[Address]() {}); //recipient address
        val receiveAddress = rawRecipient.getValue //.toString;
        val rawAmount      = TypeDecoder.decodeNumeric[Uint256](DATA.substring(72, 136),classOf[Uint256])
        val amount         = rawAmount.getValue;
        list :+= TransferOutEvent(CoinType.USDT, ownerAddress, amount.longValue(), receiveAddress)
        list :+= TransferInEvent(CoinType.USDT, receiveAddress, amount.longValue(), ownerAddress)
      } catch {
        case e: Throwable =>
          //FIXME
          logger.error("fail to parse block transaction %s,data:%s smart contract:%s".format(txnHash, DATA, Hex.toHexString(smartContract.toByteArray)), e)
      }
    }
    //授权转账的模式
    //https://tronscan.io/#/transaction/aa50e19142345f872a9d5546e6d280bcfc8693f47e674994a3c5bedeb6466dcf
    else if (
    //主网络
      (isMain && contractAddress == MAIN_USDT_CONTRACT_ADDRESS && methodId == MAIN_USDT_TRANSFER_FROM_METHOD_ID)
    ) {
      try {
        val rawFrom        = TypeDecoder.decodeAddress(DATA.substring(8, 8 + 64)) //, 0, new TypeReference[Address]() {}); //recipient address
        val fromAddress    = rawFrom.getValue //.toString;
        val rawRecipient   = TypeDecoder.decodeAddress(DATA.substring(8 + 64, 8 + 64 + 64)) //, 0, new TypeReference[Address]() {}); //recipient address
        val receiveAddress = rawRecipient.getValue //.toString;
        val rawAmount      = TypeDecoder.decodeNumeric[Uint256](DATA.substring(8 + 64 + 64, 8 + 64 + 64 + 64),classOf[Uint256])
        val amount         = rawAmount.getValue;
        list :+= TransferOutEvent(CoinType.USDT, fromAddress, amount.longValue(), receiveAddress)
        list :+= TransferInEvent(CoinType.USDT, receiveAddress, amount.longValue(), fromAddress)
      } catch {
        case e: Throwable =>
          //FIXME
          logger.error("fail to parse block transaction %s,data:%s smart contract:%s".format(txnHash, DATA, Hex.toHexString(smartContract.toByteArray)), e)
      }
    }

    list
  }
}
