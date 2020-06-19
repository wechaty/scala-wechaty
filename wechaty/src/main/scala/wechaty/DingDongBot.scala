package wechaty

import wechaty.plugins.{DingDongPlugin, DingDongPluginConfig}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object DingDongBot {
  def main(args: Array[String]): Unit = {
    val option = new WechatyOptions
    val bot = Wechaty.instance(option)

    bot.use(new DingDongPlugin(DingDongPluginConfig(self = false,dm = false,at = false)))
      .onScan(payload => {
        println("Scan QR Code to login: %s\nhttps://api.qrserver.com/v1/create-qr-code/?data=%s\n".format(payload.status, payload.qrcode))
      })
      .onLogin(payload => {
        println("User %s logined\n".format(payload.id))
      })

    bot.start()

    Thread.currentThread().join()
  }
}
