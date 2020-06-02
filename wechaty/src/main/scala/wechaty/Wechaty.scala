package wechaty

import wechaty.hostie.PuppetHostie
import wechaty.puppet.events.EventEmitter
import wechaty.puppet.schemas.Contact.ContactPayload
import wechaty.puppet.schemas.Events.{EventLoginPayload, EventName, EventScanPayload}
import wechaty.puppet.schemas.Message.MessagePayload
import wechaty.puppet.schemas.Puppet.PuppetOptions
import wechaty.puppet.{LoggerSupport, PuppetOption}
import wechaty.user.Message
import scala.language.implicitConversions


/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-01
  */
object Wechaty {
  def instance(options: WechatyOptions): Wechaty = {
    new Wechaty(options)
  }
}

class WechatyOptions {
  var name: String = "Wechaty"
  var puppet: String = "wechaty-puppet-hostie"
  var puppetOptions: Option[PuppetOptions] = None
  var ioToken: Option[String] = None
}

class Wechaty(options: WechatyOptions) extends LoggerSupport{
  private var hostie:PuppetHostie = _

  def onScan(listener: EventScanPayload=>Unit): Wechaty = {
    EventEmitter.addListener(EventName.PuppetEventNameScan,listener)
    this
  }
  def onLogin(listener:ContactPayload =>Unit):Wechaty={
    EventEmitter.addListener(EventName.PuppetEventNameLogin,listener)
    this
  }
  def onMessage(listener:Message =>Unit):Wechaty={
    EventEmitter.addListener[MessagePayload](EventName.PuppetEventNameMessage,listener)
    this
  }
  def onLogout(listener:EventLoginPayload=>Unit):Wechaty={
    EventEmitter.addListener(EventName.PuppetEventNameLogout,listener)
    this
  }

  def start(): Unit = {
    val option = new PuppetOption
    this.hostie = new PuppetHostie(option)
    this.hostie.start()
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit ={
        info("stop puppet...")
        Wechaty.this.hostie.stop()
      }
    }))

  }
  implicit def toMessage(messageListener: Message=>Unit):MessagePayload=>Unit ={
    messagePayload:MessagePayload => messageListener(new Message(messagePayload,this.hostie))
  }
}


