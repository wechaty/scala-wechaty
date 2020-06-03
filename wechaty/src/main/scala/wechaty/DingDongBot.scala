package wechaty

import wechaty.puppet.schemas.Message.MessageType

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object DingDongBot {
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
        println(message)
        if(message.payload.`type` != MessageType.MessageTypeText || message.payload.text != "#ding" ){
          println("Message discarded because it does not match #ding")
        }else {
          println("send message to ",message.payload.fromId)
          message.say("dong")
          println("dong")
        }
      })


    bot.start()

    Thread.currentThread().join()
  }
}
