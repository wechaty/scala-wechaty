package xcoin.blockchain.tron.internal

import com.typesafe.scalalogging.Logger
import org.bouncycastle.util.encoders.Hex
import org.tron.trident.abi.TypeDecoder
import org.tron.trident.abi.datatypes.generated.Uint256
import org.tron.trident.api.GrpcAPI.{BlockLimit, EmptyMessage}
import org.tron.trident.proto.Chain.Transaction
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType
import org.tron.trident.proto.Contract._
import org.tron.trident.proto.{Chain, Common, Contract, Response}
import org.tron.trident.utils.Base58Check.bytesToBase58
import reactor.core.publisher.{Flux, Mono}
import USDTSupport._
import xcoin.blockchain.tron.services.TronApi.{BlockSupport, TronNodeClientNetwork}
import xcoin.blockchain.tron.services.TronBridge.CoinType
import xcoin.blockchain.tron.services.TronModel._
import xcoin.core.services.XCoinException.{XInvalidReturnException, XInvalidStateException}

import java.time.Duration

trait TNCBlockSupport extends BlockSupport {
  self: TronNodeClient =>
  override def blockLatestId(): Mono[Long] = {
    stub.getNowBlock(EmptyMessage.getDefaultInstance)
      .map { block =>
//        dumpProtobufMessage(block)
        if (block.hasBlockHeader) block.getBlockHeader.getRawData.getNumber
        else throw XInvalidReturnException("Fail to get latest block.")
      }
  }
  override def blockEvent(blockId:Long):Flux[BlockEvent]={
    val blockLimit = BlockLimit.newBuilder().setStartNum(blockId).setEndNum(blockId + 1).build();
    stub
      .getBlockByLimitNext2(blockLimit)
      .flatMapMany(parseBlockList(_))
  }
  override def blockEventStream(start:Long, blockSegmentSize:Int = 10):Flux[BlockEvent]={
    def getBlockList(startNum: Long): Mono[(Long, Response.BlockListExtention)] = {
      val endNum     = startNum + blockSegmentSize
      if (endNum - startNum > 100) {
        throw XInvalidStateException("The difference between startNum and endNum cannot be greater than 100, please check it.");
      }
      val blockLimit = BlockLimit.newBuilder().setStartNum(startNum).setEndNum(endNum).build();
      logger.debug("loop block from {}", startNum)
      this.stub.getBlockByLimitNext2(blockLimit).map[(Long, Response.BlockListExtention)]((startNum, _))
        .onErrorResume { (e: Throwable) =>
          logger.error("fail get block data", e)
          Mono.just((startNum, Response.BlockListExtention.getDefaultInstance))
        }
    }

    getBlockList(start)
      .expand { case (lastStartNum: Long, blockListExtention: Response.BlockListExtention) =>
        val nextBlockId = {
          if (blockListExtention.getBlockCount > 0) {
            blockListExtention.getBlock(blockListExtention.getBlockCount - 1).getBlockHeader.getRawData.getNumber + 1
          } else lastStartNum
        }
        if (blockListExtention.getBlockList.size() != blockSegmentSize) {
          //说明已经抓取完全，需要等待一下
          Mono.delay(Duration.ofSeconds(2)).flatMap { _ =>
            getBlockList(nextBlockId)
          }
        } else {
          getBlockList(nextBlockId)
        }
      }
      .concatMap { case (_, blockList: Response.BlockListExtention) =>
        parseBlockList(blockList)
      }
  }
  private def parseBlockList(blockExtension: Response.BlockListExtention):Flux[BlockEvent]={
    logger.whenDebugEnabled{
      logger.debug("get block size:{}", blockExtension.getBlockList.size())

      /*
      if(blockExtension.getBlockCount > 0) {
        val blockId = blockExtension.getBlock(0).getBlockHeader.getRawData.getNumber
        Using.resource(new FileOutputStream(new File(s"block_${blockId}.bin"))) { os =>
          os.write(blockExtension.toByteArray)
        }
      }
      logger.debug("block data:{}", Hex.toHexString(blockExtension.toByteArray))
       */
    }
    var blockId = 0L
    Flux.fromIterable(blockExtension.getBlockList).concatMap { block =>
      blockId = block.getBlockHeader.getRawData.getNumber
      val blockTimestamp = block.getBlockHeader.getRawData.getTimestamp
      logger.info("process block:{}", blockId)
      Flux.fromIterable(block.getTransactionsList)
        .filter(_.getResult.getResult)
        .concatMap[BlockEvent] { (txn:Response.TransactionExtention) =>
          val rawData  = txn.getTransaction.getRawData
          val noteData = txn.getTransaction.getRawData.getData
          val noteOpt  = if (noteData.isEmpty) None else Some(noteData.toStringUtf8)
          val txnHash  = new String(Hex.encode(txn.getTxid.toByteArray))
          //确保交易成功
          val success  = txn.getResult.getResult &&
            (txn.getTransaction.getRetCount > 0) &&
            txn.getTransaction.getRet(0).getContractRet == Transaction.Result.contractResult.SUCCESS
          //          if (!success) {
          //            logger.debug("transaction {} not successful,result:{}", txnHash, txn.getTransaction.getRetList)
          //          }
          Flux.fromIterable[Chain.Transaction.Contract](rawData.getContractList)
            .filter(_ => success)
            .concatMap { (contract: Chain.Transaction.Contract) =>
              logger.whenDebugEnabled {
                //                logger.debug("contract: {}", Hex.toHexString(contract.getParameter.getValue.toByteArray))
              }
              var list = List[BlockEvent]()
              try {
                contract.getType match {
                  case ContractType.AccountCreateContract =>
                    val accountCreateContract = AccountCreateContract.parseFrom(contract.getParameter.getValue)
                    val ownerAddress          = bytesToBase58(accountCreateContract.getOwnerAddress.toByteArray)
                    val activatedAccount      = bytesToBase58(accountCreateContract.getAccountAddress.toByteArray)
                    list = List(AccountActivatedEvent(ownerAddress, activatedAccount))
                  case ContractType.TransferContract =>
                    //TRX转账
                    val transferContract = TransferContract.parseFrom(contract.getParameter.getValue)
                    //                    transferResultOpt = Some(TronBlockChainHelper.parseTransferContract(transferContract))
                    list = BlockSupport.parseTransferContract(transferContract)
                  case ContractType.TriggerSmartContract =>
                    val triggerSmartContract = TriggerSmartContract.parseFrom(contract.getParameter.getValue)
                    //只解析出来USDT的智能合约，其他合约忽略
                    list = BlockSupport.parseUSDTSmartTrigger(triggerSmartContract, txnHash, network)

                    if (list.isEmpty) {
                      val ownerAddress    = bytesToBase58(triggerSmartContract.getOwnerAddress.toByteArray)
                      val contractAddress = bytesToBase58(triggerSmartContract.getContractAddress.toByteArray)
                      list = List(TriggerSmartContractEvent(ownerAddress, contractAddress))
                    }

                  case ContractType.DelegateResourceContract =>
                    //解析代理的合约
                    val delegateResource = DelegateResourceContract.parseFrom(contract.getParameter.getValue)
                    list = BlockSupport.parse(delegateResource)
                  case ContractType.UnDelegateResourceContract =>
                    //解析代理的合约
                    val delegateResource = UnDelegateResourceContract.parseFrom(contract.getParameter.getValue)
                    list = BlockSupport.parse(delegateResource)
                  case other =>
                  //                    logger.debug("contract({}) not supported", other)
                }
              } catch {
                case e: Throwable =>
                  logger.error(e.toString, e)
              }
              Flux.fromArray(list.toArray).map {
                case transferResult: AddressChangeEvent =>
                  transferResult.blockId = blockId
                  transferResult.blockTimestamp = blockTimestamp
                  transferResult.txnHash = txnHash
                  transferResult.note = noteOpt
                  if (txn.getTransaction.getRetCount > 0)
                    transferResult.fee = txn.getTransaction.getRet(0).getFee
                  transferResult
                case event => event
              }
            }
        }
        .concatWithValues(BlockProcessedEvent(blockId,blockTimestamp))
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


    def parseTransferData(): Unit = {
      try {
        val rawRecipient   = TypeDecoder.decodeAddress(DATA.substring(8, 72)) //, 0, new TypeReference[Address]() {}); //recipient address
        val receiveAddress = rawRecipient.getValue //.toString;
        val rawAmount      = TypeDecoder.decodeNumeric[Uint256](DATA.substring(72, 136), classOf[Uint256])
        val amount         = rawAmount.getValue;
        list :+= TransferOutEvent(CoinType.USDT, ownerAddress, amount.longValue(), receiveAddress)
        list :+= TransferInEvent(CoinType.USDT, receiveAddress, amount.longValue(), ownerAddress)
      } catch {
        case e: Throwable =>
          //FIXME
          logger.error("fail to parse block transaction %s,data:%s smart contract:%s".format(txnHash, DATA, Hex.toHexString(smartContract.toByteArray)), e)
      }
    }
    def parseTransferFromData(): Unit = {
      try {
        val rawFrom        = TypeDecoder.decodeAddress(DATA.substring(8, 8 + 64)) //, 0, new TypeReference[Address]() {}); //recipient address
        val fromAddress    = rawFrom.getValue //.toString;
        val rawRecipient   = TypeDecoder.decodeAddress(DATA.substring(8 + 64, 8 + 64 + 64)) //, 0, new TypeReference[Address]() {}); //recipient address
        val receiveAddress = rawRecipient.getValue //.toString;
        val rawAmount      = TypeDecoder.decodeNumeric[Uint256](DATA.substring(8 + 64 + 64, 8 + 64 + 64 + 64), classOf[Uint256])
        val amount         = rawAmount.getValue;
        list :+= TransferOutEvent(CoinType.USDT, fromAddress, amount.longValue(), receiveAddress)
        list :+= TransferInEvent(CoinType.USDT, receiveAddress, amount.longValue(), fromAddress)
      } catch {
        case e: Throwable =>
          //FIXME
          logger.error("fail to parse block transaction %s,data:%s smart contract:%s".format(txnHash, DATA, Hex.toHexString(smartContract.toByteArray)), e)
      }
    }

    network match {
      case TronNodeClientNetwork.MAIN if contractAddress == MAIN_USDT_CONTRACT_ADDRESS && methodId == MAIN_USDT_TRANSFER_METHOD_ID =>
        parseTransferData()
      case TronNodeClientNetwork.MAIN if contractAddress == MAIN_USDT_CONTRACT_ADDRESS && methodId == MAIN_USDT_TRANSFER_FROM_METHOD_ID =>
        parseTransferFromData()
      case TronNodeClientNetwork.TEST_SHASTA if contractAddress == SHASTA_USDT_CONTRACT_ADDRESS && methodId == SHASTA_USDT_TRANSFER_METHOD_ID =>
        parseTransferData()
      case TronNodeClientNetwork.TEST_SHASTA if contractAddress == SHASTA_USDT_CONTRACT_ADDRESS && methodId == SHASTA_USDT_TRANSFER_FROM_METHOD_ID =>
        parseTransferFromData()
      case TronNodeClientNetwork.TEST_NILE if contractAddress == NILE_USDT_CONTRACT_ADDRESS && methodId == NILE_USDT_TRANSFER_METHOD_ID =>
        parseTransferData()
      case TronNodeClientNetwork.TEST_NILE if contractAddress == NILE_USDT_CONTRACT_ADDRESS && methodId == NILE_USDT_TRANSFER_FROM_METHOD_ID =>
        parseTransferFromData()
      case other =>
//        logger.warn("not supported for method:{}#{}@{} for {}",contractAddress,methodId,other,txnHash)
    }
    list
  }
}
