package wechaty.padplus.support

import com.google.protobuf.ByteString
import wechaty.padplus.PuppetPadplus
import wechaty.padplus.internal.LocalStore
import wechaty.padplus.schemas.GrpcSchemas.GrpcMessagePayload
import wechaty.padplus.schemas.ModelContact.PadplusContactPayload
import wechaty.padplus.schemas.ModelRoom.PadplusRoomPayload
import wechaty.puppet.schemas.Puppet.objectMapper

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-22
  */
trait LocalStoreSupport {
  self:PuppetPadplus=>
  protected var store:LocalStore =  _
  private val uinKey = ByteString.copyFromUtf8("uin")
  private val messageKeyFormat="MSG_%s"
  private val contactKeyFormat="CON_%s"
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
  protected def saveRawContactPayload(contactId:String,payload:PadplusContactPayload): Unit ={
    store.put(contactKeyFormat.format(contactId),objectMapper.writeValueAsString(payload))
  }
  protected def getRawContactPayload(contactId:String):Option[PadplusContactPayload] ={
    store.get(contactKeyFormat.format(contactId)).map(str=>{
      objectMapper.readValue(str.toStringUtf8,classOf[PadplusContactPayload])
    })
  }
  protected def saveRawMessagePayload(messageId:String,rawMessage:String): Unit ={
    store.put(messageKeyFormat.format(messageId),rawMessage)
  }
  protected def saveRoom(roomPayload:PadplusRoomPayload): Unit ={
    store.put(roomPayload.chatroomId,objectMapper.writeValueAsString(roomPayload))
  }
  protected def getGrpcMessagePayload(messageId:String):Option[GrpcMessagePayload] ={
    store.get(messageKeyFormat.format(messageId)).map(str=>{
      objectMapper.readValue(str.toStringUtf8,classOf[GrpcMessagePayload])
    })
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
