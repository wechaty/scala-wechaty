package wechaty.puppet.schemas

object MiniProgram {

  class MiniProgramPayload {
    var appid: String = _ //    // optional, appid, get from wechat (mp.weixin.qq.com)
    var description: String = _ //    // optional, mini program title
    var pagePath: String = _ //    // optional, mini program page path
    var iconUrl: String = _ //    // optional, mini program icon url
    var shareId: String = _ //    // optional, the unique userId for who share this mini program
    var thumbUrl: String = _ //    // optional, default picture, convert to thumbnail
    var title: String = _ //    // optional, mini program title
    var username: String = _ //    // original ID, get from wechat (mp.weixin.qq.com)
    var thumbKey: String = _ //    // original, thumbnailurl and thumbkey will make the headphoto of mini-program better
  }

}
