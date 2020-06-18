package wechaty

import wechaty.user.Room

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object DingDongBotDebug {
  def main(args: Array[String]): Unit = {
    val option = new WechatyOptions
    implicit val bot = Wechaty.instance(option)
    bot
      .onScan(payload => {
        println("Scan QR Code to login: %s\nhttps://api.qrserver.com/v1/create-qr-code/?data=%s\n".format(payload.status, payload.qrcode))
      }).onLogin(payload => {
        println("User %s logined\n".format(payload.id))
        Room.findAll().foreach(x=>{
          println(x.id,x.payload.topic)
        })
      }).onMessage(message=>{
        //only for test
        if(message.from.id== sys.props.get("DEBUG_WEIXIN").get) {
          message.forward(message.from)
        }
      })

    bot.start()

    Thread.currentThread().join()
  }
}
