package wechaty

import wechaty.puppet.schemas.Message.MessageType

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object Example {
  def main(args: Array[String]): Unit = {
    val option = new WechatyOptions
    val bot = Wechaty.instance(option)
    bot
      .onScan(payload => {
        println("Scan QR Code to login: %s\nhttps://api.qrserver.com/v1/create-qr-code/?data=%s\n".format(payload.status, payload.qrcode))
      })
      .onLogin(payload => {
        println("User %s logined\n".format(payload.id))
      })
      .onMessage(message=>{
        if(message.payload.`type` != MessageType.MessageTypeText || message.payload.text != "#ding" ){
          println("Message discarded because it does not match #ding")
        }else {
          message.say("dong")
          println("dong")
        }
      })


    bot.start()

    Thread.currentThread().join()
  }
}
