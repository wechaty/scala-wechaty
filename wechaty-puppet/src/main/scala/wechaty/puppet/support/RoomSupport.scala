package wechaty.puppet.support

import wechaty.puppet.schemas.Room.RoomPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait RoomSupport {
 def roomAdd (roomId: String, contactId: String)          : Unit
//   def roomAvatar (roomId: String)                          : FileBox>
   def roomCreate (contactIdList: Array[String], topic: String) : String
   def roomDel (roomId: String, contactId: String)          : Unit
   def roomList ()                                          : Array[String]
   def roomQRCode (roomId: String)                          : String
   def roomQuit (roomId: String)                            : Unit
   def roomTopic (roomId: String)                           : String
   def roomTopic (roomId: String, topic: String)            : Unit

  protected def roomRawPayload (roomId: String) : RoomPayload
}
