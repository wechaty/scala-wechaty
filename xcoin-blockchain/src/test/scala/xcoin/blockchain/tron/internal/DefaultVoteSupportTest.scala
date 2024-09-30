package xcoin.blockchain.tron.internal

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import TronNodeClientTest.TestNileConfiguration
import TronNodeClientTest.TestNileConfiguration


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestNileConfiguration]))
class DefaultVoteSupportTest {
  @Autowired
  private val nodeClient: TronNodeClient = null

  @Test
  def testList(): Unit = {
    val list      = nodeClient.voteList(5).collectList().block()
    list.forEach(println(_))
  }
}
