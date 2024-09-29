package xcoin.blockchain.services

import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.{Assertions, Test}
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType
import xcoin.blockchain.services.TronApi.TronPermission
import xcoin.blockchain.services.TronPermissionHelper.TronPermissionWrapper

class TronPermissionHelperTest {

  @Test
  def test_Helper(): Unit = {
    val tronPermission = new TronPermission
    //    val activate1="7fff1fc0033efb0f000000000000000000000000000000000000000000000000"
    val activate2      = "0000000000000006000000000000000000000000000000000000000000000000"
    tronPermission.operations = Hex.decode(activate2)
    Assertions.assertTrue(tronPermission.hasPermission(ContractType.DelegateResourceContract))
    Assertions.assertFalse(tronPermission.hasPermission(ContractType.AccountCreateContract))
    Assertions.assertFalse(tronPermission.hasPermission(ContractType.UpdateBrokerageContract))
    Assertions.assertTrue(tronPermission.hasPermission(ContractType.UnDelegateResourceContract))

  }
}
