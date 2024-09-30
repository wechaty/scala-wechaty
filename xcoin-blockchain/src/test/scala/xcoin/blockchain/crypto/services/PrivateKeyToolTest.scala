package xcoin.blockchain.crypto.services

import org.junit.jupiter.api.{Assertions, Test}
import org.tron.trident.core.key.KeyPair

class PrivateKeyToolTest {
  @Test
  def testEncode(): Unit = {

    val keyPair    = new KeyPair("ddee5b31d12b83ea789a3420623b551196bfc73a6246abe0610a43cf4f96fbfe")//.generate
    val address    = keyPair.toBase58CheckAddress
    val privateKey = keyPair.toPrivateKey
    val str = PrivateKeyTool.encodeKey(keyPair)
    println(str)
    val result = PrivateKeyTool.decodeKey(str)
    Assertions.assertEquals(privateKey,result)
  }

}
