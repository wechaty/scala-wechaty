package xcoin.blockchain.internal.tron

import org.tron.trident.api.GrpcAPI.AccountAddressMessage
import org.tron.trident.core.ApiWrapper.parseAddress
import org.tron.trident.proto.Common
import reactor.core.publisher.Mono
import xcoin.blockchain.services.TronApi.AccountSupport

trait TNCAccountSupport extends AccountSupport{
  self:TronNodeClient =>
  override def accountGet(address: String): Mono[TronAccount] = {
    val bsAddress             = parseAddress(address)
    val accountAddressMessage = AccountAddressMessage.newBuilder.setAddress(bsAddress).build

    stub.getAccount(accountAddressMessage)
      .map{account=>
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
//        tronAccount.ownerPermission = TronPermission.toTronPermission(account.getOwnerPermission)
//        tronAccount.witnessPermission = TronPermission.toTronPermission(account.getWitnessPermission)
//        tronAccount.activePermission = account.getActivePermissionList.asScala.map(TronPermission.toTronPermission).toArray

        tronAccount.rateMono = resourceRate().share()
        tronAccount

      }

  }
}
