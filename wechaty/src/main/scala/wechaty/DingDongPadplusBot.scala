package wechaty

import wechaty.plugins.{DingDongConfig, DingDongPlugin}
import wechaty.puppet.schemas.Puppet.PuppetOptions

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object DingDongPadplusBot {
  def main(args: Array[String]): Unit = {
    var tokenOpt=sys.props.get("WECHATY_PUPPET_PADPLUS_TOKEN")
    if(tokenOpt.isEmpty)
      tokenOpt = sys.env.get("WECHATY_PUPPET_PADPLUS_TOKEN")
    val endpoint="padplus.juzibot.com:50051"

    val option = new WechatyOptions
    option.puppet="wechaty-puppet-padplus"
    val puppetOptions = new PuppetOptions
    puppetOptions.endPoint=Some(endpoint)
    puppetOptions.token=tokenOpt
    option.puppetOptions=Some(puppetOptions)
    val bot = Wechaty.instance(option)

    bot.use(new DingDongPlugin(DingDongConfig()))
      .onMessage(message=>{
        println(message.text)
      })
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
