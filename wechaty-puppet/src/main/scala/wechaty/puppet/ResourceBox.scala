package wechaty.puppet

import java.io._
import java.net.{HttpURLConnection, URL}
import java.util.Base64

import javax.activation.MimeType
import org.apache.commons.io.IOUtils
import wechaty.puppet.schemas.Puppet.objectMapper

/**
  * wrap stream,support file stream and url stream
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-08
  */
object ResourceBox {
  def fromFile(file: File): ResourceBox={
    new FileResourceBox(file)
  }
  def fromUrl(url: String): ResourceBox={
    new UrlResourceBox(url)
  }
  def fromBase64(base64: String): ResourceBox={
    new Base64ResourceBox(base64)
  }
  def fromJson(json: String): ResourceBox={
    val root = objectMapper.readTree(json)
    val boxTypeInt = root.get("boxType").asInt()
    val boxType = ResourceBoxType.apply(boxTypeInt)
    boxType match{
      case ResourceBoxType.Base64 =>
        fromBase64(root.get("base64").asText)
      case ResourceBoxType.Url =>
        fromUrl(root.get("remoteUrl").asText)
      case _ =>
        throw  new UnsupportedOperationException("boxType %s unsupported".format(boxType))
    }
  }
  object ResourceBoxType extends Enumeration {
    type Type = Value
    val Unknown: Type = Value(0)

    /**
      * Serializable by toJSON()
      */
    val Base64  :Type = Value(1)
    val Url     :Type = Value(2)
    val QRCode  :Type = Value(3)

    /**
      * Not serializable by toJSON()
      * Need to convert to FileBoxType.Base64 before call toJSON()
      */
    val Buffer  :Type = Value(4)
    val File    :Type = Value(5)
    val Stream  :Type = Value(6)
  }
  private class UrlResourceBox(url:String) extends AbstractResourceBox{
    override def toStream: InputStream = {
      val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(5000)
      connection.setReadTimeout(5000)
      connection.setRequestMethod("GET")
      connection.setRequestProperty("User-Agent", "wechaty/scala")
      connection.getInputStream
    }

    override def toJson(): String = {
      val objectNode = objectMapper.createObjectNode
      objectNode.put("name",url.substring(url.lastIndexOf("/")+1))
      objectNode.put("boxType",ResourceBoxType.Url.id)
      objectNode.put("remoteUrl",url)
      objectNode.toString

    }
  }
  private class Base64ResourceBox(base64:String) extends AbstractResourceBox{
    override def toStream: InputStream = {
      //decode base64 as byte array input stream
      new ByteArrayInputStream(Base64.getDecoder.decode(base64))
    }

    override def toBase64: String = base64

    override def toJson(): String = {
      val objectNode = objectMapper.createObjectNode
      objectNode.put("boxType",ResourceBoxType.Url.id)
      objectNode.put("base64",base64)
      objectNode.toString
    }
  }
  private class StreamResourceBox(stream:InputStream) extends AbstractResourceBox{
    override def toStream: InputStream = stream
    override protected def using[T <: Closeable, R](resource: T)(block: T => R): R = {
      block(resource) //don't close the stream.must be closed by creator
    }

  }
  private class FileResourceBox(file:File) extends AbstractResourceBox{
    override def toStream: InputStream = new FileInputStream(file)
  }
  private trait AbstractResourceBox extends ResourceBox {
    override def toBase64: String = {
      using(toStream) { fi =>
        val byteArrayOutputStream = new ByteArrayOutputStream()
        val base64Out = Base64.getEncoder.wrap(byteArrayOutputStream)
        IOUtils.copy(fi, base64Out)
        base64Out.close()
        byteArrayOutputStream.close()
        byteArrayOutputStream.toString()
      }
    }

    override def toDataURL(mimeType: MimeType): String = {
      Array("data:", mimeType, ";base64,", toBase64).mkString("")
    }

    protected def using[T <: Closeable, R](resource: T)(block: T => R): R = {
      try {
        block(resource)
      } finally {
        if (resource != null) resource.close()
      }
    }
  }
}
trait ResourceBox {
  def toStream:InputStream
  def toBase64:String
  def toDataURL(mimeType: MimeType):String
  def toJson():String = ???
}
