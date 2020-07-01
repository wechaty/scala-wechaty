package wechaty

import wechaty.plugins.{DingDongPlugin, DingDongConfig}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object DingDongBot {
  def main(args: Array[String]): Unit = {
    val option = new WechatyOptions
    val bot = Wechaty.instance(option)

    bot.use(new DingDongPlugin(DingDongConfig(self = false,dm = false,at = false)))
      .onScan(payload => {
        println("Scan QR Code to login: %s\nhttps://wechaty.github.io/qrcode/%s\n".format(payload.status, payload.qrcode))
      })
      .onLogin(payload => {
        println("User %s logined\n".format(payload.id))
      })

    bot.start()

    Thread.currentThread().join()
  }
}
