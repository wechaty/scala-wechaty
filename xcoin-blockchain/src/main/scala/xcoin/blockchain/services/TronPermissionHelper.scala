package xcoin.blockchain.services

import org.tron.trident.proto.Chain.Transaction.Contract.ContractType
import org.tron.trident.proto.Common
import org.tron.trident.utils.Base58Check
import xcoin.blockchain.services.TronApi.{TronPermission, TronPermissionKey, TronPermissionType}
import xcoin.core.services.XCoinException.XInvalidParameterException

object TronPermissionHelper {
  implicit class TronPermissionWrapper(tronPermission: TronPermission) {
    /**
     * 是否有某项合约权限
     *
     * @param contractType 合约类型
     * @return
     */
    private[services] def hasPermission(contractType: ContractType): Boolean = {
      val operations = tronPermission.operations
      if (operations.length != 32) {
        throw XInvalidParameterException("operations", operations)
      }

      val dataIndex     = contractType.getNumber >> 3
      val positionIndex = contractType.getNumber - (dataIndex << 3)
      val data          = 1 << positionIndex

      (operations(dataIndex) & data) == data
    }

    def hasPermission(address: String, contractType: ContractType): Boolean = {
      hasPermission(contractType) && {
        val opt = tronPermission.keys.find(x => x.address == address)
        opt.isDefined && opt.get.weight >= tronPermission.threshold
      }
    }
  }

  object TronPermission {
    def apply(permission: Common.Permission): TronPermission = {
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
      tronPermission.threshold = permission.getThreshold
      tronPermission.keys = permission.getKeysList.stream().map[TronPermissionKey] { k =>
        val key = new TronPermissionKey
        key.address = Base58Check.bytesToBase58(k.getAddress.toByteArray)
        key.weight = k.getWeight
        key
      }.toArray(size => new Array[TronPermissionKey](size))

      tronPermission
    }
  }
}
