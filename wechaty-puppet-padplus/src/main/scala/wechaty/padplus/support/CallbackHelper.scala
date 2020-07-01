package wechaty.padplus.support

import java.util.concurrent.TimeUnit

import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import wechaty.padplus.grpc.PadPlusServerOuterClass.StreamResponse
import wechaty.padplus.schemas.ModelContact.{PadplusContactPayload, PadplusConversation}
import wechaty.padplus.schemas.ModelRoom.{PadplusRoomMemberMap, PadplusRoomPayload}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-07-01
  */
object CallbackHelper {
  private def createPool[T]: Cache[String, T]={
    Caffeine.newBuilder().maximumSize(1000)
      .expireAfterWrite(1, TimeUnit.MINUTES)
      .build()
      .asInstanceOf[Cache[String, T]]
  }
  type TraceRequestCallback = StreamResponse => Unit
  lazy val traceCallbacks: Cache[String, TraceRequestCallback] = createPool[TraceRequestCallback]

  type ContactCallback = PadplusConversation => Unit
  lazy val contactCallbacks: Cache[String, List[ContactCallback]] = createPool[List[ContactCallback]]

  type ContactAliasCallback = () =>  Unit
  lazy val contactAliasCallbacks: Cache[String, Map[String, ContactAliasCallback]] = createPool[Map[String,ContactAliasCallback]]


  type RoomTopicCallback= ()=>Unit
  lazy val roomTopicCallbacks: Cache[String, Map[String, RoomTopicCallback]] = createPool[Map[String,RoomTopicCallback]]

  type AcceptFriendCallback= () =>Unit
  lazy val acceptFriendCallbacks = createPool[List[AcceptFriendCallback]]

  type RoomMemberCallback= PadplusRoomMemberMap =>Unit
  lazy val roomMemberCallbacks = createPool[List[RoomMemberCallback]]

  def pushCallbackToPool (traceId: String, callback: TraceRequestCallback){
    traceCallbacks.put(traceId,callback)
  }
  def resolveCallBack (traceId: String,response:StreamResponse):Unit={
    val callback = traceCallbacks.getIfPresent(traceId)
    if(callback!=null){
      callback(response)
    }
    traceCallbacks.invalidate(traceId)
  }

  def removeCallback (traceId: String): Unit = {
    traceCallbacks.invalidate(traceId)
  }

  private def pushListCallback[T](cache:Cache[String,List[T]],key:String,callback:T): Unit ={
      val callbackList = cache.getIfPresent(key)
      if(callbackList == null){
        cache.put(key,List(callback))
      }else{
        cache.put(key,callbackList :+ callback)
      }
  }

  def pushContactCallback ( contactId: String, callback: ContactCallback) {
    pushListCallback(contactCallbacks,contactId,callback)
  }

  def resolveContactCallBack (contactId: String, data: PadplusContactPayload) {
    val callbackList = contactCallbacks.getIfPresent(contactId)
    if(callbackList != null){
      callbackList.foreach(f=>f(data))
    }

    this.resolveContactAliasCallback(contactId, data.remark)
    this.resolveAcceptFriendCallback(contactId)
    contactCallbacks.invalidate(contactId)
  }

  def resolveRoomCallBack (roomId: String, data: PadplusRoomPayload) {
    val callbackList = contactCallbacks.getIfPresent(roomId)
    if(callbackList != null){
      callbackList.foreach(f=>f(data))
    }
    this.resolveRoomTopicCallback(data.chatroomId, data.nickName)
    contactCallbacks.invalidate(roomId)
  }

  private def pushMapCallback[T](cache:Cache[String,Map[String,T]],key:String,mapKey:String,callback:T): Unit ={
    val callbacks = cache.getIfPresent(key)
    if(callbacks == null){
      cache.put(key,Map(mapKey->callback))
    }else{
      cache.put(key,callbacks + (mapKey->callback))
    }
  }
  def pushContactAliasCallback (contactId: String, alias: String, callback:ContactAliasCallback): Unit = {
    pushMapCallback(contactAliasCallbacks,contactId,alias,callback)
  }
  private def resolveMapCallback(cache:Cache[String,Map[String,()=>Unit]],key: String, mapKey: String) {
    val callbacks = cache.getIfPresent(key)
    if(callbacks != null){
      val callbackOpt = callbacks.get(mapKey)
      callbackOpt.foreach{f=>
        f()
        if(callbacks.size == 1)
          cache.invalidate(key)
        else
          cache.put(key,callbacks.filterNot{case (key,_) => key == mapKey})
      }
    }
  }
  private def resolveContactAliasCallback (contactId: String, alias: String) {
    resolveMapCallback(contactAliasCallbacks,contactId,alias)
  }

  def pushRoomTopicCallback (roomId: String, topic: String, callback: RoomTopicCallback) {
    pushMapCallback(roomTopicCallbacks,roomId,topic,callback)
  }

  private def resolveRoomTopicCallback (roomId: String, topic: String) {
    resolveMapCallback(roomTopicCallbacks,roomId,topic)
  }

  def pushAcceptFriendCallback (contactId: String, callback: AcceptFriendCallback) {
    pushListCallback(acceptFriendCallbacks,contactId,callback)
  }

  private def resolveAcceptFriendCallback (contactId: String) {
    val callbacks = acceptFriendCallbacks.getIfPresent(contactId)
    if(callbacks != null){
      callbacks.foreach(cb=>cb())
      acceptFriendCallbacks.invalidate(contactId)
    }
  }

  def pushRoomMemberCallback (roomId: String, callback: RoomMemberCallback): Unit ={
    pushListCallback(roomMemberCallbacks,roomId,callback)
  }

  def resolveRoomMemberCallback (roomId: String, memberList: PadplusRoomMemberMap) {
    val callbacks = roomMemberCallbacks.getIfPresent(roomId)
    if(callbacks != null){
      callbacks.foreach(cb=>cb(memberList))
      roomMemberCallbacks.invalidate(roomId)
    }
  }
}
