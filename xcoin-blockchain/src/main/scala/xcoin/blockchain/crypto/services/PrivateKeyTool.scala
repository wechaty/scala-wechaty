package xcoin.blockchain.crypto.services

import com.google.protobuf.ByteString
import org.bouncycastle.util.encoders.Hex
import org.springframework.util.StreamUtils
import org.tron.trident.core.key.KeyPair
import xcoin.blockchain.crypto.proto.CryptoDataOuterClass.CryptoData

import java.io.{ByteArrayInputStream, InputStream}
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import javax.crypto.{Cipher, CipherInputStream}

object PrivateKeyTool {
  private final val key               = Hex.decode("24E545FC309F1CC92B0223FAFA8C84F4")
  private final val iv                = Hex.decode("6D1F24B8E29268D6EFBF55CAFB27D3BF")
  private final val version           = 7
  private final val AES_CBC_ALGORITHM = "AES/CBC/PKCS5PADDING"
  private final val AES_NAME          = "AES"


  private def encode(data: InputStream): InputStream = {
    val cipher    = Cipher.getInstance(AES_CBC_ALGORITHM)
    val keySpec   = new SecretKeySpec(key, AES_NAME)
    val paramSpec = new IvParameterSpec(iv)
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, paramSpec)
    new CipherInputStream(data, cipher)
  }

  private def decode(data: InputStream): InputStream = {
    val cipher    = Cipher.getInstance(AES_CBC_ALGORITHM)
    val keySpec   = new SecretKeySpec(key, AES_NAME)
    val paramSpec = new IvParameterSpec(iv)
    cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec)
    new CipherInputStream(data, cipher)
  }

  def encodeKey(key: KeyPair): String = {
    val privateKey  = key.getRawPair.getPrivateKey.getEncoded
    val result      = encode(new ByteArrayInputStream(privateKey))
    val data        = CryptoData.newBuilder()
    val finalResult = data.setVersion(version)
      .setEncryptData(ByteString.readFrom(result))
      .build().toByteArray
    Hex.toHexString(finalResult)
  }

  def decodeKey(data:String):String={
    val bytes = Hex.decode(data)
    val cryptoData = CryptoData.parseFrom(bytes)
    val is = cryptoData.getEncryptData.newInput()
    val dataDecoded = decode(is)
    Hex.toHexString(StreamUtils.copyToByteArray(dataDecoded))


    /*
    val privateKey = key.getRawPair.getPrivateKey.getEncoded
    val result = encode(new ByteArrayInputStream(privateKey))
    val data = CryptoData.newBuilder()
    val finalResult = data.setVersion(version)
      .setEncryptData(ByteString.readFrom(result))
      .build().toByteArray
    Hex.toHexString(finalResult)

     */
  }
}
