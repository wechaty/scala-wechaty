package xcoin.core.services

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.util.ValidationEventCollector
import xcoin.core.services.XCoinException.XInvalidParameterException

import java.io.{InputStream, InputStreamReader}
import java.nio.charset.StandardCharsets
import javax.xml.XMLConstants
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
          throw XInvalidParameterException("line %s column %s :%s".format(vel.getLineNumber, vel.getColumnNumber, ve.getMessage))
        }
      }
    }
  }

}
