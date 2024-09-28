package xcoin.blockchain.internal.tron

import com.typesafe.scalalogging.Logger
import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.{Assertions, Test}
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.tron.trident.api.GrpcAPI.AccountAddressMessage
import org.tron.trident.api.ReactorWalletGrpc.ReactorWalletStub
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType
import org.tron.trident.proto.Response
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestConfiguration
import xcoin.blockchain.internal.tron.TronPermissionHelper.TronPermissionWrapper
import xcoin.blockchain.services.TronApi.TronPermission

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestConfiguration]))
class TNCAccountSupportTest {
  private val logger = Logger[TNCAccountSupportTest]
  @Autowired
  private val tronNodeClient:TronNodeClient = null
  @MockBean
  private val stub:ReactorWalletStub = null
  @Test
  def test_getAccount(): Unit = {

    given(stub.getAccount(any[AccountAddressMessage]())).willReturn {
      val accountHex = "1a1541b3667a16622b8fd6540c196607567dbcf26316d420d8fddd9f194888c3ebfd8d32508080e5d4a132a8018080e5d4a132b001d0addcdd9032c00180e8dd0dc80101d2011a18a0a08496a0324880e8dd0d50eeb2cc893558dfb088e0106001fa01241a056f776e657220013a190a1541b3667a16622b8fd6540c196607567dbcf26316d410018a024b080210021a06616374697665200132207fff1fc0033efb0f0000000000000000000000000000000000000000000000003a190a1541b3667a16622b8fd6540c196607567dbcf26316d410018a024b080210031a063130304341542001322000000000000000060000000000000000000000000000000000000000000000003a190a1541e42ba324ae52226376e2e1469058b0dbf139e2d8100192020610d9e2dd810692020808011092d6db87069202020802a002a7a6be940ba802cee986fd18e00301"
      val account    = Response.Account.parseFrom(Hex.decode(accountHex))
      Mono.just(account)
    }
    `given`(stub.getAccountResource(any[AccountAddressMessage]())).willReturn{
      val hex="10d8043880e0aef7a001409bf1adf52d7880b8c9e5ae04800187fd988a09"
      val message = Response.AccountResourceMessage.parseFrom(Hex.decode(hex))
      Mono.just(message)
    }

    val account = tronNodeClient.accountGet("TSKnicj3bfk7M7KTjtecHXLz2jvdveFtqq").block()
    Assertions.assertTrue(account.activePermission.length > 0)

    StepVerifier
      .create(account.canDelegateBandwidth())
      .assertNext { a =>
        Assertions.assertTrue(a > 0)
        logger.debug("bandwidth:{}",a)
      }.verifyComplete()

    StepVerifier
      .create(account.canDelegateBandwidthAmount())
      .assertNext { a =>
        Assertions.assertTrue(a > 0)
      }.verifyComplete()

    StepVerifier
      .create(account.canDelegateEnergy())
      .assertNext { a =>
        logger.debug("energy:{}",a)
        Assertions.assertTrue(a > 0)
      }.verifyComplete()
    StepVerifier
      .create(account.canDelegateEnergyAmount())
      .assertNext{a=>
        Assertions.assertTrue(a > 0)
      }.verifyComplete()

    val catPermission = account.activePermission(1)
    Assertions.assertEquals("100CAT",catPermission.name)
    Assertions.assertEquals(1,catPermission.threshold)
    Assertions.assertTrue(catPermission.hasPermission(ContractType.DelegateResourceContract))
    Assertions.assertFalse(catPermission.hasPermission("123",ContractType.DelegateResourceContract))
    Assertions.assertTrue(catPermission.hasPermission("TWmfLmUSuTgm8m92ZbkgxT6BJfjdmx2QxS",ContractType.DelegateResourceContract))
    Assertions.assertTrue(catPermission.hasPermission("TWmfLmUSuTgm8m92ZbkgxT6BJfjdmx2QxS",ContractType.UnDelegateResourceContract))
    Assertions.assertFalse(catPermission.hasPermission("TWmfLmUSuTgm8m92ZbkgxT6BJfjdmx2QxS",ContractType.UpdateBrokerageContract))
  }
  @Test
  def testPermission(): Unit = {
    val tronPermission = new TronPermission
//    val activate1="7fff1fc0033efb0f000000000000000000000000000000000000000000000000"
    val activate2="0000000000000006000000000000000000000000000000000000000000000000"
    tronPermission.operations = Hex.decode(activate2)
    Assertions.assertTrue(tronPermission.hasPermission(ContractType.DelegateResourceContract))
    Assertions.assertFalse(tronPermission.hasPermission(ContractType.AccountCreateContract))
    Assertions.assertFalse(tronPermission.hasPermission(ContractType.UpdateBrokerageContract))
    Assertions.assertTrue(tronPermission.hasPermission(ContractType.UnDelegateResourceContract))
  }
}
