package wechaty.user

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.LoggerSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-08
  */
class Room(roomId:String)(implicit resolver:PuppetResolver) extends Conversation(roomId) with  LoggerSupport{

  def alias(member:Contact):String={
    //TODO
    member.name
//    throw new UnsupportedOperationException
  }
  def memberList (): Array[Contact] ={
    val memberIdList = resolver.puppet.roomMemberList(this.roomId)
    memberIdList.map(new Contact(_))
  }
}
