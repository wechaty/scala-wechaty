package wechaty.puppet

import java.io.File

import javax.activation.MimeType
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
}
