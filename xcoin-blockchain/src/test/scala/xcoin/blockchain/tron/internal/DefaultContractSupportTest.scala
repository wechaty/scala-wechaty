package xcoin.blockchain.tron.internal

import com.typesafe.scalalogging.Logger
import org.junit.jupiter.api.{Assertions, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.tron.trident.abi.TypeReference
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.generated.Uint256
import reactor.test.StepVerifier
import TronNodeClientTest.TestNileConfiguration
import xcoin.blockchain.tron.services.TronApi.SimpleTronPermission
import TronNodeClientTest.TestNileConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestNileConfiguration]))
class DefaultContractSupportTest {
  private val logger = Logger[DefaultContractSupportTest]

  @Autowired
  private val tronApi: TronNodeClient = null

  @Test
  def testConstant(): Unit = {
    val owner     = "TUfAMQM81RLMdquBSaFytsXxEet7AKKKKK"
    val balanceOf = new org.tron.trident.abi.datatypes.Function("balanceOf",
                                                                java.util.Arrays.asList(new Address(owner)),
                                                                                        java.util.Arrays.asList(new TypeReference[Uint256]() {}))
    tronApi.contractTriggerConstant("TUfAMQM81RLMdquBSaFytsXxEet7AKKKKK", "TXLAQ63Xg1NAzckPwKHvzw7CSEmLMEqcdj", balanceOf).block()
  }
  @Test
  def testTransfer():Unit={
    val owner  = "TSKnicj3bfk7M7KTjtecHXLz2jvdveFtqq"
    val target="TUfAMQM81RLMdquBSaFytsXxEet7AKKKKK"
    val permission = new SimpleTronPermission
    permission.key = "151d076dc152a0749837034b758e89063e304061af1f2d169daa6fbdb6bcd341"
    permission.idOpt=Some(2)
    StepVerifier.create(
    tronApi.accountTransferTRX(owner, target, 1)
      .flatMap {tx=>
        tronApi.contractSignAndBroadcast(permission,tx)
      }
    ).assertNext{id=>
      logger.debug("transfer id:{}",id)
      Assertions.assertNotNull(id)
    }.verifyComplete()
  }
}
