package wechaty.user

import org.junit.jupiter.api.{Assertions, Test}
import wechaty.TestBase

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-24
  */
class ImageTest extends TestBase{
  val test="<?xml version=\"1.0\"?>\n<msg>\n\t<img aeskey=\"1e1f1fa09fd3c197d544834fe0428f01\" encryver=\"1\" cdnthumbaeskey=\"1e1f1fa09fd3c197d544834fe0428f01\" cdnthumburl=\"304e0201000447304502010002047fc1d03902032f50e502049131227502045ef2c8e9042030303031393766623832303364373463373630353831643333353138376335300204010828010201000400\" cdnthumblength=\"5674\" cdnthumbheight=\"150\" cdnthumbwidth=\"150\" cdnmidheight=\"0\" cdnmidwidth=\"0\" cdnhdheight=\"0\" cdnhdwidth=\"0\" cdnmidimgurl=\"304e0201000447304502010002047fc1d03902032f50e502049131227502045ef2c8e9042030303031393766623832303364373463373630353831643333353138376335300204010828010201000400\" length=\"14193\" cdnbigimgurl=\"304e0201000447304502010002047fc1d03902032f50e502049131227502045ef2c8e9042030303031393766623832303364373463373630353831643333353138376335300204010828010201000400\" hdlength=\"14195\" md5=\"3df5c8f7130c71cf16ac79b6f49a6684\" />\n</msg>\n"
  @Test
  def test_cdn: Unit ={
    mockRoomMessage(test)
    val image = new Image("messageId")
    val cdnImage = image.getCdnImage
    Assertions.assertEquals("1e1f1fa09fd3c197d544834fe0428f01",cdnImage.aeskey)
    Assertions.assertEquals("1e1f1fa09fd3c197d544834fe0428f01",cdnImage.cdnthumbaeskey)
  }
}
