package wechaty.puppet

import java.io._
import java.net.{HttpURLConnection, URL}
import java.util.Base64

import javax.activation.MimeType
import org.apache.commons.io.IOUtils

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
  private class UrlResourceBox(url:String) extends AbstractResourceBox{
    override def toStream: InputStream = {
      val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(5000)
      connection.setReadTimeout(5000)
      connection.setRequestMethod("GET")
      connection.setRequestProperty("User-Agent", "wechaty/scala")
      connection.getInputStream
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
}
