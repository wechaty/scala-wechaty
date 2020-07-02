package wechaty.padplus.support

import java.util.concurrent.TimeUnit

import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import wechaty.padplus.grpc.PadPlusServerOuterClass.StreamResponse
import wechaty.padplus.schemas.ModelContact.PadplusContactPayload
import wechaty.padplus.schemas.ModelRoom.{PadplusRoomMemberMap, PadplusRoomPayload}

import scala.concurrent.Promise
import scala.util.{Success, Try}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-07-01
  */
object CallbackHelper {
  private def createPool[T]: Cache[String, T]={
    Caffeine.newBuilder().maximumSize(1000)
      .expireAfterWrite(5, TimeUnit.MINUTES) //按照高原建议设置5~10分钟
      .build()
      .asInstanceOf[Cache[String, T]]
  }
  type TraceRequestCallback = Promise[StreamResponse]
  lazy val traceCallbacks: Cache[String, TraceRequestCallback] = createPool[TraceRequestCallback]

  type ContactPromise= Promise[PadplusContactPayload]
  lazy val contactCallbacks: Cache[String, List[ContactPromise]] = createPool[List[ContactPromise]]
  type RoomPromise= Promise[PadplusRoomPayload]
  lazy val roomCallbacks: Cache[String, List[RoomPromise]] = createPool[List[RoomPromise]]

  type ContactAliasCallback = Promise[Unit]
  lazy val contactAliasCallbacks: Cache[String, Map[String, ContactAliasCallback]] = createPool[Map[String,ContactAliasCallback]]


  type RoomTopicCallback= Promise[Unit]
  lazy val roomTopicCallbacks: Cache[String, Map[String, RoomTopicCallback]] = createPool[Map[String,RoomTopicCallback]]

  type AcceptFriendCallback= Promise[Unit]
  lazy val acceptFriendCallbacks: Cache[String, List[AcceptFriendCallback]] = createPool[List[AcceptFriendCallback]]

  type RoomMemberCallback= Promise[PadplusRoomMemberMap]
  lazy val roomMemberCallbacks: Cache[String, List[RoomMemberCallback]] = createPool[List[RoomMemberCallback]]

  def pushCallbackToPool (traceId: String, callback: TraceRequestCallback){
    traceCallbacks.put(traceId,callback)
  }
  def resolveCallBack (traceId: String,response:StreamResponse):Unit={
    val callback = traceCallbacks.getIfPresent(traceId)
    if(callback!=null){
      callback.success(response)
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
  private def resolveListCallBack[T] (cache:Cache[String,List[Promise[T]]],key: String, data: Try[T]) {
    val callbackList = cache.getIfPresent(key)
    if (callbackList != null) {
      callbackList.foreach(f => f.complete(data))
      cache.invalidate(key)
    }
  }

  def pushContactCallback ( contactId: String, callback:ContactPromise) {
    pushListCallback(contactCallbacks,contactId,callback)
  }

  def resolveContactCallBack (contactId: String, data: Try[PadplusContactPayload]) {
    resolveListCallBack(contactCallbacks,contactId,data)

    data match{
      case Success(value) =>
        this.resolveContactAliasCallback(contactId, value.remark)
        this.resolveAcceptFriendCallback(contactId)
      case _ =>
    }
  }

  def pushRoomCallback ( roomId: String, callback:RoomPromise) {
    pushListCallback(roomCallbacks,roomId,callback)
  }
  def resolveRoomCallBack (roomId: String, data: Try[PadplusRoomPayload]) {
    resolveListCallBack(roomCallbacks,roomId,data)
    data match{
      case Success(value) =>
        this.resolveRoomTopicCallback(value.chatroomId, value.nickName)
      case _ => //do nothing
    }
  }

  private def pushMapCallback[T](cache:Cache[String,Map[String,Promise[T]]],key:String,mapKey:String,callback:Promise[T]): Unit ={
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
  private def resolveMapCallback(cache:Cache[String,Map[String,Promise[Unit]]],key: String, mapKey: String) {
    val callbacks = cache.getIfPresent(key)
    if(callbacks != null){
      val callbackOpt = callbacks.get(mapKey)
      callbackOpt.foreach{f=>
        f.success({})
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
    resolveListCallBack(acceptFriendCallbacks,contactId,Try[Unit]({}))
  }

  def pushRoomMemberCallback (roomId: String, callback: RoomMemberCallback): Unit ={
    pushListCallback(roomMemberCallbacks,roomId,callback)
  }

  def resolveRoomMemberCallback (roomId: String, memberList: Try[PadplusRoomMemberMap]) {
    resolveListCallBack(roomMemberCallbacks,roomId,memberList)
  }
}
