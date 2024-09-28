package xcoin.blockchain.internal.tron

import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.{Assertions, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.tron.trident.abi.TypeDecoder
import org.tron.trident.abi.datatypes.generated.Uint256
import org.tron.trident.proto.Contract.{DelegateResourceContract, TransferContract, TriggerSmartContract}
import reactor.test.StepVerifier
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestConfiguration
import xcoin.blockchain.services.TronApi.TronNodeClientNetwork

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

  @Test
  def test_ParseDelegate(): Unit = {
    //https://shasta.tronscan.org/#/transaction/390842b3ddc1f986a52bcb44c7044a0e6ed639e19636547af467e0e829966e11
    val hex      = "0a1541cd00b0ec89911351b5c8471adcd944d1901accd1100118cfae99ac01221541477df627ff9b046328fb45fb4d8fb984ef6dde38"
    val contract = DelegateResourceContract.parseFrom(Hex.decode(hex))
    val result   = BlockSupport.parse(contract)
    Assertions.assertEquals("TUfAMQM81RLMdquBSaFytsXxEet7AKKKKK", result.head.address)
    Assertions.assertEquals("TGVDse7geUTDT6CRTeE7dqpU5MrFuQMCTA", result.apply(1).address)
    Assertions.assertEquals(361125711, result.head.stakeAmountSun)
  }

  @Test
  def test_ParseTransferContract(): Unit = {
    //https://shasta.tronscan.org/#/transaction/a9f863d5da0b7297cf2bbdf32d573591fa449d7fe2b57446bb44695acc8df008
    val hex      = "0a1541420e7c2914ab255ca60629f45a6de3d9ecb01780121541cd00b0ec89911351b5c8471adcd944d1901accd11880b6dc05"
    val contract = TransferContract.parseFrom(Hex.decode(hex))
    val result   = BlockSupport.parseTransferContract(contract)
    Assertions.assertEquals("TFzUx9xwzD1UqhZaDDnAiQqWwACNPRXsFr", result.head.address)
    Assertions.assertEquals("TUfAMQM81RLMdquBSaFytsXxEet7AKKKKK", result.apply(1).address)
    Assertions.assertEquals(12_000_000, result.head.amountSun)
  }

  @Test
  def test_ParseHackUSDTContract(): Unit = {
    //https://tronscan.io/#/transaction/642b6f5b537e9cc1c0b1964ad9b7997f69256de739b7096d9fca13d7ff1de90e
    val DATA           = "a9059cbb000000000000000000000041ec0d6ca0029dc43aa6bd1898112e0bf5d4f0fe2500000000000000000000000000000000000000000000000000000000000000989680"
    val rawRecipient   = TypeDecoder.decodeAddress(DATA.substring(8, 72)) //, 0, new TypeReference[Address]() {}); //recipient address
    val receiveAddress = rawRecipient.getValue //.toString;
    val rawAmount      = TypeDecoder.decodeNumeric[Uint256](DATA.substring(72, 136),classOf[Uint256])
    val amount         = rawAmount.getValue;
    Assertions.assertEquals(152, amount.intValue())
  }

  @Test
  def test_ParseUSDTContract(): Unit = {
    //https://shasta.tronscan.org/#/transaction/a0cda8fb6ac0d644133941098fb1cded6e9df550a242f95786a5bac2d725d76a
    val hex      = "0a1541420e7c2914ab255ca60629f45a6de3d9ecb0178012154142a1e39aefa49290f2b3f9ed688d7cecf86cd6e02244a9059cbb000000000000000000000000477df627ff9b046328fb45fb4d8fb984ef6dde38000000000000000000000000000000000000000000000000000000000754d4c0"
    val contract = TriggerSmartContract.parseFrom(Hex.decode(hex))
    val result   = BlockSupport.parseUSDTSmartTrigger(contract, "test", TronNodeClientNetwork.TEST_SHASTA)
    Assertions.assertEquals("TFzUx9xwzD1UqhZaDDnAiQqWwACNPRXsFr", result.head.address)
    Assertions.assertEquals("TGVDse7geUTDT6CRTeE7dqpU5MrFuQMCTA", result(1).address)
    Assertions.assertEquals(123_000_000, result.head.amountSun)
  }

  @Test
  def test_ParseComplexUSDTContract(): Unit = {
    //https://tronscan.org/#/transaction/cd8f71299e97ddfc35170a79414db6d4d879327fdd368a5ebb9c47e0d0bfc9fd
    val hex      = "0a1541afc2773399d04d04670431d683cc43ab2d4002c2121541a614f803b6fd780986a42c78ec9c7f77e6ded13c2244a9059cbb000000008cad2cef099fcfa65b6907386224d796acd2ddb9af120c5196e0b1c40000000000000000000000000000000000000000000000000000000000e7ef00"
    val contract = TriggerSmartContract.parseFrom(Hex.decode(hex))
    val result   = BlockSupport.parseUSDTSmartTrigger(contract, "test", TronNodeClientNetwork.MAIN)
    Assertions.assertEquals("TRzYDLa86HcG7xPQFZjRyhNhDy5XETadW6", result.head.address)
    Assertions.assertEquals("TJJYK18kh99YuZj5G2bLPDUQQ7tyk9kWZs", result(1).address)
    Assertions.assertEquals(15200000, result.head.amountSun)
  }


}
