package wechaty.plugins

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.StrictLogging
import wechaty.puppet.schemas.Friendship.{FriendshipPayloadConfirm, FriendshipPayloadReceive, FriendshipPayloadVerify}
import wechaty.puppet.schemas.Puppet._
import wechaty.user.Contact
import wechaty.{Wechaty, WechatyPlugin}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-19
  */
case class FriendshipAcceptorConfig( var greeting:String = "we are friends now!",var waitSeconds:Int=0, var keywordOpt:Option[String]=None)
class FriendshipAcceptor(config:FriendshipAcceptorConfig) extends WechatyPlugin with StrictLogging{
  private def isMatchKeyword(str: String): Boolean ={
    logger.debug("keyword:{} hello:{} ",config.keywordOpt,str)
    config.keywordOpt match{
        //use contains simply
        //TODO use complex function or regexp to match string
        // see https://github.com/wechaty/wechaty-plugin-contrib/blob/05cfec3606480d2f2a544ac4e742a967bcaec2b4/src/matchers/string-matcher.ts#L9
      case Some(keyword) =>
        if(isBlank(str)){false}
        else str.contains(keyword) //simple use contains
      case _ =>
        true
    }
  }
  private def doGreeting(contact: Contact): Unit ={
    contact.say(config.greeting)
  }
  override def install(wechaty: Wechaty): Unit = {
    wechaty.onFriendAdd(friendship => {
      PluginHelper.executeWithNotThrow("FriendshipAcceptor") {

        friendship.payload match {
          case _: FriendshipPayloadReceive =>
            val hello = friendship.hello()
            if (isMatchKeyword(hello)) {
              if(config.waitSeconds >0)
                Thread.sleep(TimeUnit.SECONDS.toMillis(config.waitSeconds))
              friendship.accept()
            }
          case _: FriendshipPayloadConfirm =>
            val contact = friendship.contact()
            doGreeting(contact)
          case _: FriendshipPayloadVerify =>
          // This is for when we send a message to others, but they did not accept us as a friend.
          case _ =>
            throw new Error("friendshipType unknown: " + friendship.`type`)
        }
      }
    })
  }
}

