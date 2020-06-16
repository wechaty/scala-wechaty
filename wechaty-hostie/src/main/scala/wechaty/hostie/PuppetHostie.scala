package wechaty.hostie

import wechaty.hostie.support._
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.Puppet.PuppetOptions
import wechaty.puppet.{LoggerSupport, Puppet}

import scala.io.Source

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
class PuppetHostie(val option:PuppetOptions) extends Puppet
  with GrpcSupport
  with LoggerSupport
  with ContactRawSupport
  with MessageRawSupport
  with ContactSelfRawSupport
  with FriendshipRawSupport
  with TagRawSupport
  with RoomInvitationRawSupport
  with RoomMemberRawSupport
  with RoomRawSupport
  with GrpcEventSupport {

  private var stopped = false
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
    if(!stopped){
      stopGrpc()
      stopped = true
    }
  }

  override def selfIdOpt(): Option[String] = this.idOpt

  private def discoverHostieEndPoint(): Option[String] = {
    val hostieEndpoint = "https://api.chatie.io/v0/hosties/%s"
//    val content = scala.io.Source.fromURL(hostieEndpoint.format(option.token.get)).mkString
    try {
      val content = get(hostieEndpoint.format(option.token.get)).mkString
      val json = Puppet.objectMapper.readTree(content)
      Some(json.get("ip").asText() + ":" + json.get("port"))
    }catch{
      case e:Throwable=>
        error(e.getMessage,e)
        Some("23.97.48.212:8788")
    }
  }
  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  private def get(url: String,
          connectTimeout: Int = 10000,
          readTimeout: Int = 10000,
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
