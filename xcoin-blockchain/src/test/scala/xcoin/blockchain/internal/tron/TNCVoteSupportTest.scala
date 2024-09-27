package xcoin.blockchain.internal.tron

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestConfiguration

import scala.jdk.CollectionConverters.ListHasAsScala

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestConfiguration]))
class TNCVoteSupportTest {
  @Autowired
  private val nodeClient: TronNodeClient = null

  @Test
  def testList(): Unit = {
    val list      = nodeClient.voteList().collectList().block()
    list.forEach(println(_))
  }
}
