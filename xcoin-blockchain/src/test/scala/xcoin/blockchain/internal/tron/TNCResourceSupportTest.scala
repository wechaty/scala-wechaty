package xcoin.blockchain.internal.tron

import org.junit.jupiter.api.{Assertions, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestConfiguration]))
class TNCResourceSupportTest {
  @Autowired
  private val tronNodeClient: TronNodeClient = null

  @Test
  def test_resourceRate(): Unit = {
    StepVerifier.create(tronNodeClient.resourceRate())
      .assertNext{rate=>
        Assertions.assertTrue(rate.energyRate > 0)
        Assertions.assertTrue(rate.bandwidthRate > 0)
      }
      .verifyComplete()
  }
}
