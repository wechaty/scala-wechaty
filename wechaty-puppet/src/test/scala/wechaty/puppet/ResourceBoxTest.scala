package wechaty.puppet

import java.io.{ByteArrayOutputStream, File}
import java.util.Base64

import javax.activation.MimeType
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.{Assertions, Test}


/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-08
  */
class ResourceBoxTest {

  @Test
  def test_file: Unit ={
    val file = new File("pom.xml")
    val box = ResourceBox.fromFile(file)
    Assertions.assertNotNull(box.toBase64)
    Assertions.assertNotNull(box.toDataURL(new MimeType("images/jpg")))
    val urlBox= ResourceBox.fromUrl("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png")
    Assertions.assertNotNull(urlBox.toBase64)
    Assertions.assertNotNull(urlBox.toDataURL(new MimeType("images/jpg")))
  }
  @Test
  def test_base64: Unit ={
    val originString = "base64String"
    val base64String = Base64.getEncoder.encodeToString(originString.getBytes())
    val box = ResourceBox.fromBase64("name",base64String)
    Assertions.assertEquals(base64String,box.toBase64)
    Assertions.assertTrue(box.toJson().indexOf(base64String)>0)
    val byteArrayOutputStream = new ByteArrayOutputStream()
    IOUtils.copy(box.toStream,byteArrayOutputStream)
    Assertions.assertEquals(originString,byteArrayOutputStream.toString)
    Assertions.assertNotNull(box.toDataURL(new MimeType("images/jpg")))
  }
}
