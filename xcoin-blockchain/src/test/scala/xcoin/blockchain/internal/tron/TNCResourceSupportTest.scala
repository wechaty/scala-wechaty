package xcoin.blockchain.internal.tron

import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.{Assertions, Test}
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.tron.trident.api.GrpcAPI.AccountAddressMessage
import org.tron.trident.api.ReactorWalletGrpc.ReactorWalletStub
import org.tron.trident.proto.Response
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestConfiguration]))
class TNCResourceSupportTest {
  @Autowired
  private val tronNodeClient: TronNodeClient = null
  @MockBean
  private val stub: ReactorWalletStub = null

  @Test
  def test_resourceRate(): Unit = {
    given(stub.getAccountResource(any[AccountAddressMessage]())).willReturn {
      val hex     = "10d8043880e0aef7a001409bf1adf52d7880b8c9e5ae04800187fd988a09"
      val message = Response.AccountResourceMessage.parseFrom(Hex.decode(hex))
      Mono.just(message)
    }

    StepVerifier.create(tronNodeClient.resourceRate())
      .assertNext{rate=>
        Assertions.assertTrue(rate.energyRate > 0)
        Assertions.assertTrue(rate.bandwidthRate > 0)
        Assertions.assertEquals(16248,rate.energyRate.intValue)
        Assertions.assertEquals(285317,rate.bandwidthRate.intValue)
      }
      .verifyComplete()
  }
}
