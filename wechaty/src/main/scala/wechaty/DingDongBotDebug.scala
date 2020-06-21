package wechaty

import com.typesafe.scalalogging.LazyLogging
import wechaty.puppet.schemas.Message.MessageType
import wechaty.puppet.schemas.MiniProgram.MiniProgramPayload
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.Puppet.objectMapper
import wechaty.user.{MiniProgram, Room}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object DingDongBotDebug extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val option = new WechatyOptions
    implicit val bot: Wechaty = Wechaty.instance(option)
    bot
      .onScan(payload => {
        println("Scan QR Code to login: %s\nhttps://api.qrserver.com/v1/create-qr-code/?data=%s\n".format(payload.status, payload.qrcode))
      }).onLogin(payload => {
      println("User %s logined\n".format(payload.id))
      Room.findAll().foreach(x => {
        println(x.id, x.payload.topic)
      })
//      Room.load("21822642010@chatroom").get.payload.memberIdList.map(new Contact(_)).foreach(x=>
//        println(x.name,x.id)
//      )
    }).onMessage(message => {
      //only for test
      logger.debug("mssage received:{}", message)
      if (message.from.id == sys.props.get("DEBUG_WEIXIN").get) {
        message.forward(message.from)
      }
      if (message.`type` == MessageType.MiniProgram) {
        message.toMiniProgram()
        //构建
        //TODO FIXME,How to send MiniProgram
        val miniProgramPayload = Puppet.objectMapper.readValue(mpJson, classOf[MiniProgramPayload])
        miniProgramPayload.thumbUrl = "https://avatars0.githubusercontent.com/u/25162437?s=200&v=4"
        miniProgramPayload.thumbKey = null
        println("===>", objectMapper.writeValueAsString(miniProgramPayload))
        val miniProgram = new MiniProgram(miniProgramPayload)
        message.from.say(miniProgram)
      }
    })

    bot.start()
//        bot.logout()

    Thread.currentThread().join()
  }

  val mpJson = "{\"appid\":\"wx7c54357940c1d76a\",\"description\":\"\",\"iconUrl\":\"http://mmbiz.qpic.cn/mmbiz_png/lQLdvqlFfCYNMSG6vEPoFNGPvIES1mwEOibI5fCH2p0hS7S9L6xvSOppdQByj3u6764j3nRrMFSfcyyibV9JKouw/640?wx_fmt=pngwxfrom=200\",\"pagePath\":\"pages/detail/index.html?item_id=619257404163&pid=65&commerce_type=1\",\"shareId\":\"20a3b1336c2807932df41a0193618e04\",\"thumbKey\":\"0f13237ce8339c569330119c049e4736\",\"thumbUrl\":\"304f02010004483046020100020448529f2302033d0af802046c34feb602045eeb85ab0421777875706c6f61645f66696c6568656c7065723630345f313539323439333438330204010400030201000400\",\"title\":\"【IP联名集合】妖精的口袋情侣印花短袖t恤女2020夏新款宽松上衣\",\"username\":\"gh_1c0dae5a5b83@app\"}"
}
