package wechaty.hostie

import wechaty.hostie.support.{ContactRawSupport, GrpcEventSupport, GrpcSupport, MessageRawSupport}
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.Puppet.PuppetOptions
import wechaty.puppet.{LoggerSupport, Puppet}

import scala.io.Source

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
class PuppetHostie(option:PuppetOptions) extends Puppet
  with GrpcSupport
  with LoggerSupport
  with ContactRawSupport
  with MessageRawSupport
  with GrpcEventSupport {

  init()
  private def init(): Unit ={
    if(option.token.isEmpty){
      option.token = Configuration.WECHATY_PUPPET_HOSTIE_TOKEN
    }
    if(option.endPoint.isEmpty){
      option.endPoint = Configuration.WECHATY_PUPPET_HOSTIE_ENDPOINT
    }
    if(option.endPoint.isEmpty){
      option.endPoint= discoverHostieEndPoint()
    }
    if(option.endPoint.isEmpty)
      throw new IllegalStateException("hostie endpoint not found")
  }
  def start(): Unit ={
    startGrpc(option.endPoint.get)
  }
  def stop(): Unit = {
    stopGrpc()
  }
  private def discoverHostieEndPoint(): Option[String] = {
    val hostieEndpoint = "https://api.chatie.io/v0/hosties/%s"
//    val content = scala.io.Source.fromURL(hostieEndpoint.format(option.token.get)).mkString
    val content = get(hostieEndpoint.format(option.token.get)).mkString
    val json = Puppet.objectMapper.readTree(content)
    Some(json.get("ip").asText()+":"+json.get("port"))
  }
  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  private def get(url: String,
          connectTimeout: Int = 5000,
          readTimeout: Int = 5000,
          requestMethod: String = "GET") =
  {
    import java.net.{HttpURLConnection, URL}
    val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    connection.setRequestProperty("User-Agent", "wechaty/scala")
    val inputStream = connection.getInputStream
    val content = Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close()
    content
  }

}
