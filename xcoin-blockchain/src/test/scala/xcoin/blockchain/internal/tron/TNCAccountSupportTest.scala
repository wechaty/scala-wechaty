package xcoin.blockchain.internal.tron

import com.typesafe.scalalogging.Logger
import org.junit.jupiter.api.{Assertions, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestConfiguration]))
class TNCAccountSupportTest {
  private val logger = Logger[TNCAccountSupportTest]
  @Autowired
  private val tronNodeClient:TronNodeClient = null
  @Test
  def test_getAccount(): Unit = {
    val account = tronNodeClient.accountGet("TSKnicj3bfk7M7KTjtecHXLz2jvdveFtqq").block()
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
  }
}
