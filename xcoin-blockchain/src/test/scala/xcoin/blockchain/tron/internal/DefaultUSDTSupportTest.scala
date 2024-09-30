package xcoin.blockchain.tron.internal

import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.{Assertions, Test}
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.tron.trident.abi.TypeReference
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.generated.Uint256
import org.tron.trident.api.ReactorWalletGrpc.ReactorWalletStub
import org.tron.trident.api.WalletGrpc.WalletStub
import org.tron.trident.proto.Contract.TriggerSmartContract
import org.tron.trident.proto.Response
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import TronNodeClientTest.TestNileConfiguration
import TronNodeClientTest.TestNileConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes=Array(classOf[TestNileConfiguration]))
class DefaultUSDTSupportTest {

  @Autowired
  private val tronApi   : TronNodeClient    = null
  @MockBean
  private val walletStub: ReactorWalletStub = null

  @Test
  def testBalance(): Unit = {
    given(walletStub.triggerConstantContract(any[TriggerSmartContract]())).willReturn {
      val hex = "0ab2010aad010a028a99220853278484eefa418c4088d492bba3325a8e01081f1289010a31747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e54726967676572536d617274436f6e747261637412540a1541cd00b0ec89911351b5c8471adcd944d1901accd1121541ea51342dabbb928ae1e576bd39eff8aaf070a8c6222470a08231000000000000000000000000cd00b0ec89911351b5c8471adcd944d1901accd170bf928fbba3322a001220434ac7674acc4ea1237c4dccf9d27731d6d89a47c8c93dae133ef580a2f87d211a200000000000000000000000000000000000000000000000000000000ba4ade4e022020801288b05"
      val r   = Response.TransactionExtention.parseFrom(Hex.decode(hex))
      Mono.just(r)
    }
    StepVerifier.create(tronApi.usdtBalanceOf("TUfAMQM81RLMdquBSaFytsXxEet7AKKKKK"))
      .assertNext { balance =>
        Assertions.assertEquals(50007500000L, balance)
      }
      .verifyComplete()
  }

}
