package wechaty.padplus.support

import com.google.protobuf.ByteString
import wechaty.padplus.PuppetPadplus
import wechaty.padplus.internal.LocalStore
import wechaty.padplus.schemas.ModelContact.PadplusContactPayload
import wechaty.padplus.schemas.ModelMessage.PadplusMessagePayload
import wechaty.padplus.schemas.ModelRoom.{PadplusRoomMemberMap, PadplusRoomPayload}
import wechaty.puppet.schemas.Puppet.objectMapper

import scala.language.implicitConversions

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-22
  */
trait LocalStoreSupport {
  self:PuppetPadplus=>
  protected var store:LocalStore =  _
  private val uinKey = ByteString.copyFromUtf8("uin")
  private val messageKeyFormat="MS_%s"
  private val contactKeyFormat="CO_%s"
  private val roomMemberKeyFormat="RM_%s"
  protected def saveUin(uin:ByteString): Unit ={
    if(!uin.isEmpty){
      store.put(uinKey,uin)
    }
  }
  protected def getUin: Option[String]={
    store.get(uinKey).map(_.toStringUtf8)
  }
  protected def deleteUin():Unit ={
    store.delete(uinKey)
  }
  protected def savePadplusContactPayload(payload:PadplusContactPayload): Unit ={
    store.put(contactKeyFormat.format(payload.userName),payload)
  }
  protected def getPadplusContactPayload(contactId:String):Option[PadplusContactPayload] ={
    store.getObject[PadplusContactPayload](contactKeyFormat.format(contactId))
  }
  protected def getPadplusMessagePayload(messageId:String): Option[PadplusMessagePayload]={
    store.getObject[PadplusMessagePayload](messageKeyFormat.format(messageId))
  }
  protected def savePadplusMessagePayload(padplusMessagePayload: PadplusMessagePayload): Unit ={
    store.put(messageKeyFormat.format(padplusMessagePayload.msgId),padplusMessagePayload)
  }
  protected def savePadplusRoomPayload(roomPayload:PadplusRoomPayload): Unit ={
    store.put(roomPayload.chatroomId,roomPayload)
  }
  protected def getPadplusRoomPayload(roomId:String):Option[PadplusRoomPayload]={
    store.getObject[PadplusRoomPayload](roomId)
  }
  protected def savePadplusRoomMembers(roomId:String,padplusRoomMemberMap: PadplusRoomMemberMap): Unit ={
    store.put(roomMemberKeyFormat.format(roomId),padplusRoomMemberMap)
  }
  protected def getPadplusRoomMembers(roomId:String): Option[PadplusRoomMemberMap]={
    store.getObject[PadplusRoomMemberMap](roomMemberKeyFormat.format(roomId))
  }
  private implicit def payloadToString[T](value:T):String={
    objectMapper.writeValueAsString(value)
  }

  protected def startLocalStore(): Unit ={
    store = new LocalStore(storePath)
    store.start()
  }
  protected def stopLocalStore(): Unit ={
    if(store != null)
      store.close()
  }
}
