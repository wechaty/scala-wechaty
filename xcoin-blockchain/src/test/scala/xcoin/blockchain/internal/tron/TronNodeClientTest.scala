package xcoin.blockchain.internal.tron


import org.junit.jupiter.api.{Assertions, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import reactor.test.StepVerifier
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestConfiguration
import xcoin.blockchain.services.TronApi.TronNodeClientNetwork
import xcoin.blockchain.services.{TronApi, TronNodeClientBuilder, TronNodeClientCustomizer}

import scala.util.Failure

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestConfiguration]))
class TronNodeClientTest {
  @Autowired
  private val tronNodeClient:TronNodeClient = null
  @Test
  def test_getTransaction(): Unit = {
    val correctTransferUSDTTxnId = "dbcfdb635e6943f4deb7820164247538f5dfa6a6a494be38d689a6808fa755f6"
    StepVerifier
      .create(tronNodeClient.transactionByHash(correctTransferUSDTTxnId))
      .assertNext{payload=>
        Assertions.assertTrue(payload.result.isSuccess)
        Assertions.assertEquals(correctTransferUSDTTxnId,payload.id)
        Assertions.assertEquals(50597226,payload.blockNumber)
        Assertions.assertEquals(346,payload.receipt.net_usage)
        Assertions.assertEquals(29650,payload.receipt.energy_usage_total)
      }.verifyComplete()

    val failTxnId = "959b9d3180c8dc4692a5c30288fdacfe3bf3966cb4f79e289b8cb20252a86a57"
    StepVerifier
      .create(tronNodeClient.transactionByHash(failTxnId))
      .assertNext { payload =>
        Assertions.assertTrue(payload.result.isFailure)
        Assertions.assertEquals(failTxnId, payload.id)
        Assertions.assertEquals(50596941, payload.blockNumber)
        Assertions.assertEquals(0, payload.receipt.net_usage)
        Assertions.assertEquals(552065, payload.receipt.energy_usage_total)
        Assertions.assertEquals(42, payload.receipt.energy_usage)
      }.verifyComplete()

    val emptyTxnId= "123456"
    StepVerifier
      .create(tronNodeClient.transactionByHash(emptyTxnId))
      .assertNext { payload =>
        Assertions.assertTrue(payload.result.isFailure)
      }.verifyComplete()

  }
}
object TronNodeClientTest{
  @EnableAutoConfiguration
  class TestConfiguration{
    @Bean
    def customizeTronNodeClient(): TronNodeClientCustomizer = {
      new TronNodeClientCustomizer {
        override def customize(tronNodeClientBuilder: TronNodeClientBuilder): Unit = {
          tronNodeClientBuilder.network(TronNodeClientNetwork.TEST_NILE)
        }
      }
    }
  }
}
