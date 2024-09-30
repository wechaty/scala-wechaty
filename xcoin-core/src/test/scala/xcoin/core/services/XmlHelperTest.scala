package xcoin.core.services

import jakarta.xml.bind.annotation.{XmlElement, XmlRootElement}
import org.junit.jupiter.api.{Assertions, Test}
import xcoin.core.services.XmlHelperTest.AClass

import java.io.ByteArrayInputStream
import scala.util.{Try, Using}

class XmlHelperTest {

  @Test
  def test_parse(): Unit = {
    val xml= "<root><e>hello</e></root>"

    Using.resource(getClass.getResourceAsStream("/test.xsd")){xsd=>
      val a = XmlHelper.parseXML[AClass](new ByteArrayInputStream(xml.toString.getBytes()), Some(xsd))
      Assertions.assertEquals("hello", a.e)
    }
    val result = Try {
      val xml2= "<root><b>hello</b></root>"
      Using.resource(getClass.getResourceAsStream("/test.xsd")) { xsd =>
        val a = XmlHelper.parseXML[AClass](new ByteArrayInputStream(xml2.getBytes()), Some(xsd))
        Assertions.assertEquals("hello", a.e)
      }
    }
    Assertions.assertTrue(result.isFailure)
  }
}
object XmlHelperTest{
  @XmlRootElement(name="root")
  class AClass {
    @XmlElement(name = "e")
    var e: String = _
  }
}
