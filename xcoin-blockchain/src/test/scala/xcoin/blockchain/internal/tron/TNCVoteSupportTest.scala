package xcoin.blockchain.internal.tron

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestNileConfiguration


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestNileConfiguration]))
class TNCVoteSupportTest {
  @Autowired
  private val nodeClient: TronNodeClient = null

  @Test
  def testList(): Unit = {
    val list      = nodeClient.voteList(5).collectList().block()
    list.forEach(println(_))
  }
}
