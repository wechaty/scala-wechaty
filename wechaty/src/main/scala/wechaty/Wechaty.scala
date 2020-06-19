package wechaty

import java.util.function.Consumer

import com.typesafe.scalalogging.LazyLogging
import wechaty.Wechaty.PuppetResolver
import wechaty.helper.ImplicitHelper._
import wechaty.hostie.PuppetHostie
import wechaty.puppet.schemas.Event._
import wechaty.puppet.schemas.Puppet.{PuppetEventName, PuppetOptions}
import wechaty.puppet.Puppet
import wechaty.user._

import scala.language.implicitConversions;

/**
  **
  * Main bot class.
  *
  * A `Bot` is a WeChat client depends on which puppet you use.
  *
  * See more:
  * - [What is a Puppet in Wechaty](https://github.com/wechaty/wechaty-getting-started/wiki/FAQ-EN#31-what-is-a-puppet-in-wechaty)
  *
  * > If you want to know how to send message, see [Message](#Message) <br>
  * > If you want to know how to get contact, see [Contact](#Contact)
  *
  * @example <caption>The World's Shortest ChatBot Code: 6 lines of Scala</caption>
  * val options = new WechaytOptions
  * val bot = Wechaty.instance(options)
  * bot.onScan(payload => println("['https://api.qrserver.com/v1/create-qr-code/?data=',encodeURIComponent(qrcode),'&size=220x220&margin=20',].join('')"))
  * bot.onLogin(user => println("User ${user} logged in"))
  * bot.onMessage(message => println("Message: ${message}"))
  * bot.start()
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-01
  */
object Wechaty {
  private var globalInstance:Wechaty = _
  def instance(options: WechatyOptions): Wechaty = {
    if (options != null && globalInstance != null) throw new Error("instance can be only initialized once by options !")
    if (globalInstance == null) globalInstance = new Wechaty(options)
    globalInstance
  }
  trait PuppetResolver{
    def puppet:Puppet
  }
}

class WechatyOptions {
  var name: String = "Wechaty"
  var puppet: String = "wechaty-puppet-hostie"
  var puppetOptions: Option[PuppetOptions] = None
  var ioToken: Option[String] = None
}

class Wechaty(private val options: WechatyOptions) extends LazyLogging with PuppetResolver{
  private var hostie:PuppetHostie = _
  private implicit val puppetResolver: PuppetResolver = this

  initHostie()

  def use(plugin:WechatyPlugin): Wechaty ={
    plugin.install(this)
    this
  }
  def onScan(listener: Consumer[EventScanPayload]): Wechaty = {
    puppet.addListener[EventScanPayload](PuppetEventName.SCAN,listener)
    this
  }
  def onLogin(listener:Consumer[ContactSelf]):Wechaty={
    puppet.addListener[EventLoginPayload](PuppetEventName.LOGIN,listener)
    this
  }
  def onMessage(listener:Consumer[Message]):Wechaty={
    puppet.addListener[EventMessagePayload](PuppetEventName.MESSAGE,listener)
    this
  }
  def onLogout(listener:Consumer[Contact]):Wechaty={
    puppet.addListener[EventLogoutPayload](PuppetEventName.LOGOUT,listener)
    this
  }
  def onFriendAdd(listener:Consumer[Friendship]):Wechaty={
    puppet.addListener[EventFriendshipPayload](PuppetEventName.FRIENDSHIP,listener)
    this
  }
  def onReset(listener:EventResetPayload => Unit):Wechaty={
    puppet.addListener[EventResetPayload](PuppetEventName.RESET,listener)
    this
  }

  override def puppet: Puppet = this.hostie

  private def initHostie(): Unit = {
    val option = options.puppetOptions match {
      case Some(o) => o
      case _ => new PuppetOptions
    }
    this.hostie = new PuppetHostie(option)
    //room message
    this.hostie.addListener[EventMessagePayload](PuppetEventName.MESSAGE, Room.messageEvent)
    this.hostie.addListener[EventRoomJoinPayload](PuppetEventName.ROOM_JOIN, Room.roomJoinEvent)
    this.hostie.addListener[EventRoomLeavePayload](PuppetEventName.ROOM_LEAVE, Room.roomLeaveEvent)
    this.hostie.addListener[EventRoomTopicPayload](PuppetEventName.ROOM_TOPIC, Room.roomTopicEvent)
  }
  def start():Unit= {
    this.hostie.start()
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit ={
        logger.info("stop puppet...")
        Wechaty.this.hostie.stop()
      }
    }))

  }
  def stop(): Unit ={
    this.hostie.stop()
    Wechaty.globalInstance = null
  }
}


