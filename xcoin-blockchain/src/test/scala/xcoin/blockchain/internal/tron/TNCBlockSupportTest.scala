package xcoin.blockchain.internal.tron

import org.junit.jupiter.api.{Assertions, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestConfiguration]))
class TNCBlockSupportTest {

  @Autowired
  private val tronApi: TronNodeClient = null

  @Test
  def testConstant(): Unit = {
    StepVerifier.create(tronApi.blockLatestId())
      .assertNext{id=>
        Assertions.assertTrue(id>0)
      }
      .verifyComplete()
  }
}
