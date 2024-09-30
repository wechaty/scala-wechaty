package xcoin.core.services

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl
import org.w3c.dom.ls.{LSInput, LSResourceResolver}

import java.io.{InputStream, InputStreamReader}
import java.nio.charset.StandardCharsets
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.bind.util.ValidationEventCollector
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

object XmlHelper {
  def parseXML[T <: Object](is: InputStream, xsd: Option[InputStream])(implicit m: Manifest[T]): T = {
    val vec = new ValidationEventCollector()
    try {
      //obtain type parameter
      val clazz        = m.runtimeClass.asInstanceOf[Class[T]]
      //create io reader
      val reader       = new InputStreamReader(is, StandardCharsets.UTF_8)
      val context      = JAXBContext.newInstance(clazz)
      //unmarshal xml
      val unmarshaller = context.createUnmarshaller()
      //.unmarshal(reader).asInstanceOf[T]
      if (xsd.isDefined) {
        val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        sf.setResourceResolver(new LSResourceResolver {
          override def resolveResource(`type`: String, namespaceURI: String, publicId: String, systemId: String, baseURI: String): LSInput = {
            val input = new DOMInputImpl()
            if (systemId.endsWith("monad.xsd")) {
              //仅仅处理系统的文件 TODO 调整为能够自动识别文件路径
              input.setByteStream(getClass.getResourceAsStream("/monad.xsd"))
            }
            input
          }
        })
        val schemaSource = new StreamSource(xsd.get, "xml")
        val schema       = sf.newSchema(schemaSource)
        unmarshaller.setSchema(schema)
        unmarshaller.setEventHandler(vec)
      }
      unmarshaller.unmarshal(reader).asInstanceOf[T]
    } finally {
      if (vec.hasEvents) {
        val veOption = vec.getEvents.headOption
        if (veOption.isDefined) {
          val ve  = veOption.get
          val vel = ve.getLocator
          throw new RuntimeException("line %s column %s :%s".format(vel.getLineNumber, vel.getColumnNumber, ve.getMessage))
        }
      }
    }
  }

}
