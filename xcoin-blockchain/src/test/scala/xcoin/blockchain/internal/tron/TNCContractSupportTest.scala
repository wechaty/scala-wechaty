package xcoin.blockchain.internal.tron

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.tron.trident.abi.datatypes.Address
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestConfiguration]))
class TNCContractSupportTest {

  @Autowired
  private val tronApi: TronNodeClient = null

  @Test
  def testConstant(): Unit = {
    val owner     = "TUfAMQM81RLMdquBSaFytsXxEet7AKKKKK"
    val balanceOf = new org.tron.trident.abi.datatypes.Function("balanceOf",
                                                                java.util.Arrays.asList(new Address(owner)),
    tronApi.contractTriggerConstant("TUfAMQM81RLMdquBSaFytsXxEet7AKKKKK", "TXLAQ63Xg1NAzckPwKHvzw7CSEmLMEqcdj", balanceOf).block()
  }
}
