package xcoin.blockchain.internal.tron

import org.bouncycastle.util.encoders.Hex
import org.tron.trident.api.GrpcAPI.AccountAddressMessage
import org.tron.trident.core.ApiWrapper.{createAccountCreateContract, parseAddress}
import org.tron.trident.proto.Chain.Transaction
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType
import org.tron.trident.proto.Common
import org.tron.trident.proto.Contract.TransferContract
import org.tron.trident.utils.Base58Check
import reactor.core.publisher.Mono
import xcoin.blockchain.services.TronPermissionHelper.TronPermission
import xcoin.blockchain.services.TronApi.{AccountSupport, TronPermission, TronPermissionKey, TronPermissionType}
import xcoin.core.services.XCoinException.XInvalidParameterException

trait TNCAccountSupport extends AccountSupport{
  self:TronNodeClient =>
  override def accountBalanceOfUSDT(owner:String):Mono[Long]={
    usdtBalanceOf(owner)
  }

  override def accountTransferTRX(owner:String, target:String, amountSun:Long):Mono[Transaction]= {
    val rawFrom = parseAddress(owner)
    val rawTo   = parseAddress(target)
    val req     = TransferContract.newBuilder
      .setOwnerAddress(rawFrom).setToAddress(rawTo).setAmount(amountSun).build
    contractCreateTransaction(req, ContractType.TransferContract).map(_.getTransaction)
  }

  override def accountActivate(owner: String, accountAddress: String): Mono[Transaction] = {
    val bsOwnerAddress   = parseAddress(owner)
    val bsAccountAddress = parseAddress(accountAddress)
    val contract         = createAccountCreateContract(bsOwnerAddress, bsAccountAddress)

    contractCreateTransaction(contract, ContractType.AccountCreateContract).map(_.getTransaction)
  }


  override def accountGet(address: String): Mono[TronAccount] = {
    val bsAddress             = parseAddress(address)
    val accountAddressMessage = AccountAddressMessage.newBuilder.setAddress(bsAddress).build

    stub.getAccount(accountAddressMessage)
      .map{account=>
        println(Hex.toHexString(account.toByteArray))
        val tronAccount = new TronAccount
        tronAccount.address = address
        tronAccount.balanceSun = account.getBalance
        tronAccount.createdTime = account.getCreateTime


        tronAccount.bandwidthUsed = account.getNetUsage
        tronAccount.bandwidthDelegatedByOthersAmount = account.getAcquiredDelegatedFrozenV2BalanceForBandwidth
        tronAccount.bandwidthDelegatedToOthersAmount = account.getDelegatedFrozenV2BalanceForBandwidth
        tronAccount.bandwidthDelegatedByOthersV1Amount = account.getAcquiredDelegatedFrozenBalanceForBandwidth


        tronAccount.energyUsed = account.getAccountResource.getEnergyUsage
        tronAccount.energyDelegatedByOthersAmount = account.getAccountResource.getAcquiredDelegatedFrozenV2BalanceForEnergy
        tronAccount.energyDelegatedToOthersAmount = account.getAccountResource.getDelegatedFrozenV2BalanceForEnergy
        tronAccount.energyDelegatedByOthersV1Amount = account.getAccountResource.getAcquiredDelegatedFrozenBalanceForEnergy

        account.getFrozenV2List.stream().filter(_.getType == Common.ResourceCode.ENERGY).findFirst().ifPresent(x => {
          tronAccount.energyFrozenAmount = x.getAmount
          //          tronAccount.energyFrozen = x.getAmount / rate.energyRate
        })
        //第一个是带宽
        account.getFrozenV2List.stream().findFirst().ifPresent { x =>
          tronAccount.bandwidthFrozenAmount = x.getAmount
          //          tronAccount.bandwidthFrozen = x.getAmount / rate.bandwidthRate
        }
        //        tronAccount.energyStakedAmount = (tronAccount.energyDelegatedToOthers + tronAccount.energyFrozen) * rate.energyRate
        //        tronAccount.bandwidthStakedAmount = (tronAccount.bandwidthDelegatedToOthers + tronAccount.bandwidthFrozen) * rate.bandwidthRate

        //计算能量和带宽的恢复时间
        var bandwidthTime = account.getNetWindowSize
        if (account.getNetWindowOptimized) bandwidthTime *= 3
        tronAccount.bandwidthRecoverTime = bandwidthTime
        var energyTime = account.getAccountResource.getEnergyWindowSize
        if (account.getAccountResource.getEnergyWindowOptimized) energyTime *= 3
        tronAccount.energyRecoverTime = energyTime

        //权限信息
        tronAccount.ownerPermission = TronPermission(account.getOwnerPermission)
        tronAccount.witnessPermission = TronPermission(account.getWitnessPermission)
        tronAccount.activePermission = account.getActivePermissionList.stream().map(TronPermission(_)).toArray(size=>new Array[TronPermission](size))

        //        println(Hex.toHexString(account.getWitnessPermission.getOperations.toByteArray))
        //        println(Hex.toHexString(account.getActivePermission(1).getOperations.toByteArray))

        tronAccount.rateMono = resourceRate().share()
        tronAccount

      }

  }
}

