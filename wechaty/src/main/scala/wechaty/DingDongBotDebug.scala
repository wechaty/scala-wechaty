package wechaty

import com.typesafe.scalalogging.LazyLogging
import wechaty.plugins.{RoomJoinHello, RoomJoinHelloConfig}
import wechaty.user.Room

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
        .use(new RoomJoinHello(RoomJoinHelloConfig(Array("18911721443@chatroom"))))
      .onScan(payload => {
        println("Scan QR Code to login: %s\nhttps://api.qrserver.com/v1/create-qr-code/?data=%s\n".format(payload.status, payload.qrcode))
      }).onLogin(payload => {
      println("User %s logined\n".format(payload.id))
      Room.findAll().foreach(x => {
        println(x.id, x.payload.topic)
      })
    })

    bot.start()
        bot.logout()

    Thread.currentThread().join()
  }

  val mpJson = "{\"appid\":\"wx7c54357940c1d76a\",\"description\":\"\",\"iconUrl\":\"http://mmbiz.qpic.cn/mmbiz_png/lQLdvqlFfCYNMSG6vEPoFNGPvIES1mwEOibI5fCH2p0hS7S9L6xvSOppdQByj3u6764j3nRrMFSfcyyibV9JKouw/640?wx_fmt=pngwxfrom=200\",\"pagePath\":\"pages/detail/index.html?item_id=619257404163&pid=65&commerce_type=1\",\"shareId\":\"20a3b1336c2807932df41a0193618e04\",\"thumbKey\":\"0f13237ce8339c569330119c049e4736\",\"thumbUrl\":\"304f02010004483046020100020448529f2302033d0af802046c34feb602045eeb85ab0421777875706c6f61645f66696c6568656c7065723630345f313539323439333438330204010400030201000400\",\"title\":\"【IP联名集合】妖精的口袋情侣印花短袖t恤女2020夏新款宽松上衣\",\"username\":\"gh_1c0dae5a5b83@app\"}"
}
