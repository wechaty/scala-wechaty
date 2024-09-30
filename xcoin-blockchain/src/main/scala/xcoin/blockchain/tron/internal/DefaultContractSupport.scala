package xcoin.blockchain.tron.internal

import com.google.protobuf.{ByteString, Message}
import org.bouncycastle.util.encoders.Hex
import org.tron.trident.abi.{FunctionEncoder, datatypes}
import org.tron.trident.api.GrpcAPI.EmptyMessage
import org.tron.trident.core.ApiWrapper.{calculateTransactionHash, parseAddress, parseHex}
import org.tron.trident.core.key.KeyPair
import org.tron.trident.core.transaction.{TransactionBuilder, TransactionCapsule}
import org.tron.trident.core.utils.Utils
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType
import org.tron.trident.proto.Contract.TriggerSmartContract
import org.tron.trident.proto.{Chain, Response}
import reactor.core.publisher.Mono
import xcoin.blockchain.tron.services.TronApi.{ContractSupport, SimpleTronPermission, TransactionSigned}
import xcoin.core.services.XCoinException
import xcoin.core.services.XCoinException.XInvalidReturnException

trait DefaultContractSupport extends ContractSupport {
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
      else throw XInvalidReturnException("invalid contract result")
    }
  }
  override def contractTriggerCallBuilder(owner: String, contractAddress: String, function: datatypes.Function, valueOpt: Option[Long]): Mono[TransactionBuilder] = {
    contractTriggerConstant(owner, contractAddress, function, valueOpt).map(x=>new TransactionBuilder(x.getTransaction))
  }
  override def contractSign(permission:SimpleTronPermission,transaction:Chain.Transaction):TransactionSigned={
    permission.idOpt match {
      case Some(permissionId) =>
        //        logger.debug("set permissionId for {}",transaction)
        // 参考 http://acuilab.com:8080/articles/2022/06/18/1655532993522.html
        // https://github.com/tronprotocol/wallet-cli/blob/develop/src/main/java/org/tron/common/utils/TransactionUtils.java
        val raw      = transaction.getRawData().toBuilder();
        val contract = raw.getContract(0).toBuilder().setPermissionId(permissionId);
        raw.clearContract();
        raw.addContract(contract);

        val newTransaction = Chain.Transaction.newBuilder().setRawData(raw).build();
        signTransaction(newTransaction, permission.key)
      case _ =>
        signTransaction(transaction, permission.key)
    }
  }
  override def contractBroadcast(txn:TransactionSigned):Mono[String]={
    stub.broadcastTransaction(txn.transaction).map[String] { ret =>
      if (!ret.getResult) {
        val message = this.resolveResultCode(ret.getCodeValue) + ", " + ret.getMessage.toStringUtf8
        throw XInvalidReturnException(message)
      } else {
        txn.txnId
      }
    }
  }
  override def contractSignAndBroadcast(permission:SimpleTronPermission,transaction:Chain.Transaction):Mono[String]={
    contractBroadcast(contractSign(permission, transaction))
  }

  private def resolveResultCode(code: Int) = {
    var responseCode = ""
    code match {
      case 0 =>
        responseCode = "SUCCESS"
      case 1 =>
        responseCode = "SIGERROR"
      case 2 =>
        responseCode = "CONTRACT_VALIDATE_ERROR"
      case 3 =>
        responseCode = "CONTRACT_EXE_ERROR"
      case 4 =>
        responseCode = "BANDWITH_ERROR"
      case 5 =>
        responseCode = "DUP_TRANSACTION_ERROR"
      case 6 =>
        responseCode = "TAPOS_ERROR"
      case 7 =>
        responseCode = "TOO_BIG_TRANSACTION_ERROR"
      case 8 =>
        responseCode = "TRANSACTION_EXPIRATION_ERROR"
      case 9 =>
        responseCode = "SERVER_BUSY"
      case 10 =>
        responseCode = "NO_CONNECTION"
      case 11 =>
        responseCode = "NOT_ENOUGH_EFFECTIVE_CONNECTION"
      case 20 =>
        responseCode = "OTHER_ERROR"
      case 12 =>
      case 13 =>
      case 14 =>
      case 15 =>
      case 16 =>
      case 17 =>
      case 18 =>
      case 19 =>
      case _ =>
    }
    responseCode
  }

  private def signTransaction(txn: Chain.Transaction, keyPair: String): TransactionSigned= {
    val txId      = calculateTransactionHash(txn)
    val myKeyPair = new KeyPair(keyPair)
    val signature = KeyPair.signTransaction(txId, myKeyPair)
    val t =  txn.toBuilder.addSignature(ByteString.copyFrom(signature)).build
    TransactionSigned(Hex.toHexString(txId), t)
  }

  @throws[Exception]
  protected def contractCreateTransaction(message: Message, contractType: ContractType): Mono[TransactionCapsule] = {
    solidityStub.getNowBlock2(EmptyMessage.getDefaultInstance).flatMap(solidHeadBlock => {
      this.stub.getNowBlock2(EmptyMessage.getDefaultInstance).map { headBlock =>
        createTransactionCapsuleWithoutValidate(message, contractType, solidHeadBlock, headBlock)
      }
    })
  }

  @throws[Exception]
  private def createTransactionCapsuleWithoutValidate(message: Message, contractType: ContractType, solidHeadBlock: Response.BlockExtention, headBlock: Response.BlockExtention): TransactionCapsule = {
    val transactionCapsule     = new TransactionCapsule(message, contractType)
    var percent = 0L
    if (contractType == ContractType.CreateSmartContract) {
      val contract = Utils.getSmartContractFromTransaction(transactionCapsule.getTransaction)
      percent = contract.getNewContract.getConsumeUserResourcePercent
      if (percent < 0L || percent > 100L) throw new Exception("percent must be >= 0 and <= 100")
    }
    transactionCapsule.setTransactionCreate(false)
    val blockHash = Utils.getBlockId(solidHeadBlock).getBytes
    transactionCapsule.setReference(solidHeadBlock.getBlockHeader.getRawData.getNumber, blockHash)
    percent = headBlock.getBlockHeader.getRawData.getTimestamp + 60000L
    transactionCapsule.setExpiration(percent)
    transactionCapsule.setTimestamp()
    transactionCapsule
  }

}
