package xcoin.blockchain.internal.tron

import org.bouncycastle.util.encoders.Hex
import org.tron.trident.api.GrpcAPI.AccountAddressMessage
import org.tron.trident.core.ApiWrapper.parseAddress
import org.tron.trident.proto.Chain.Transaction.Contract.{ContractType, parser}
import org.tron.trident.proto.{Common, Contract}
import org.tron.trident.utils.Base58Check
import reactor.core.publisher.Mono
import xcoin.blockchain.internal.tron.TronPermissionHelper.TronPermission
import xcoin.blockchain.services.TronApi.{AccountSupport, TronPermission, TronPermissionKey, TronPermissionType}
import xcoin.core.services.XCoinException
import xcoin.core.services.XCoinException.InvalidParameter

trait TNCAccountSupport extends AccountSupport{
  self:TronNodeClient =>
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

        implicit def toLong(d: Double): Long = d.toLong

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
object TronPermissionHelper{
  implicit class TronPermissionWrapper(tronPermission:TronPermission){
    /**
     * 是否有某项合约权限
     * @param contractType 合约类型
     * @return
     */
    private[tron] def hasPermission(contractType:ContractType):Boolean={
      val operations = tronPermission.operations
      if (operations.length != 32) {
        throw new XCoinException(InvalidParameter("operations",operations))
      }

      val dataIndex = contractType.getNumber >> 3
      val positionIndex = contractType.getNumber - (dataIndex << 3)
      val data = 1 << positionIndex

      (operations(dataIndex) & data )  == data
    }
    def hasPermission(address:String,contractType: ContractType):Boolean={
      hasPermission(contractType) && {
        val opt = tronPermission.keys.find(x=> x.address == address)
        opt.isDefined && opt.get.weight >= tronPermission.threshold
      }
    }
  }
  object TronPermission{
    def apply(permission:Common.Permission):TronPermission={
      val tronPermission = new TronPermission
      tronPermission.name = permission.getPermissionName
      tronPermission.`type` = {
        permission.getType match {
          case Common.Permission.PermissionType.Owner => TronPermissionType.OWNER
          case Common.Permission.PermissionType.Witness => TronPermissionType.WITNESS
          case Common.Permission.PermissionType.Active => TronPermissionType.ACTIVE
        }
      }
      tronPermission.operations = permission.getOperations.toByteArray
      tronPermission.threshold= permission.getThreshold
      tronPermission.keys = permission.getKeysList.stream().map[TronPermissionKey]{k=>
        val key = new TronPermissionKey
        key.address = Base58Check.bytesToBase58(k.getAddress.toByteArray)
        key.weight = k.getWeight
        key
      }.toArray(size=>new Array[TronPermissionKey](size))

      tronPermission
    }
  }
}
