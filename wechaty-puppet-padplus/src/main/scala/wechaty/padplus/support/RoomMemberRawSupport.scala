package wechaty.padplus.support

import java.util.concurrent.TimeUnit

import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import com.typesafe.scalalogging.LazyLogging
import wechaty.padplus.grpc.PadPlusServerOuterClass.{ApiType, ResponseType, StreamResponse}
import wechaty.padplus.schemas.ModelRoom.{GrpcRoomMemberList, GrpcRoomMemberPayload, PadplusRoomMemberMap, PadplusRoomMemberPayload}
import wechaty.puppet.schemas.Puppet.objectMapper
import wechaty.puppet.schemas.Room
import wechaty.puppet.schemas.Room.RoomMemberPayload
import wechaty.puppet.support.RoomMemberSupport

import scala.concurrent.Promise

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-24
  */
trait RoomMemberRawSupport {
  self:RoomMemberSupport with GrpcSupport with LocalStoreSupport with LazyLogging=>
  protected lazy val roomMemberPayloadPromises: Cache[String, List[Promise[PadplusRoomMemberMap]]] = {
    Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(1, TimeUnit.MINUTES).build()
      .asInstanceOf[Cache[String, List[Promise[PadplusRoomMemberMap]]]]
  }

  /**
    *
    * RoomMember
    *
    */
  override def roomAnnounce(roomId: String): String = ???

  override def roomAnnounce(roomId: String, text: String): Unit = ???

  override def roomMemberList(roomId: String): Array[String] = ???

  override protected def roomMemberRawPayload(roomId: String, contactId: String): Room.RoomMemberPayload = {
    getPadplusRoomMembers(roomId) match{
      case Some(padplusRoomMembers) =>
        padplusRoomMembers.members.get(contactId).map(convertToPuppetRoomMember).orNull
      case _ =>
          val json = objectMapper.createObjectNode()
          json.put("OpType", "UPDATE")
          json.put("type", "GET_MEMBER")
          json.put("roomId", roomId)
        val map = syncRequest[PadplusRoomMemberMap](ApiType.ROOM_OPERATION, Some(json.toString))
        savePadplusRoomMembers(roomId,map)
        map.members.get(contactId).map(convertToPuppetRoomMember).orNull
    }
  }
  private def convertToPuppetRoomMember(input: PadplusRoomMemberPayload): RoomMemberPayload = {
    val result = new  RoomMemberPayload
    result.avatar     = input.smallHeadUrl
    result.id         = input.contactId
    result.inviterId  = input.inviterId   // 'wxid_7708837087612',
    result.name       = input.nickName
    result.roomAlias  = input.displayName   // '李佳芮-群里设置的备注', `chatroom_nick_name`
    result
  }
  private def convertToPadplusRoomMemberPayload(grpcRoomMemberPayload: GrpcRoomMemberPayload) = {
    val padplusRoomMemberPayload=new PadplusRoomMemberPayload
    padplusRoomMemberPayload.bigHeadUrl= grpcRoomMemberPayload.HeadImgUrl
    padplusRoomMemberPayload.contactId= grpcRoomMemberPayload.UserName
    padplusRoomMemberPayload.displayName= grpcRoomMemberPayload.DisplayName
    padplusRoomMemberPayload.inviterId= ""
    padplusRoomMemberPayload.nickName= grpcRoomMemberPayload.NickName
    padplusRoomMemberPayload.smallHeadUrl= grpcRoomMemberPayload.HeadImgUrl

    padplusRoomMemberPayload
  }
  protected def roomMemberPartialFunction(response:StreamResponse):PartialFunction[ResponseType,Unit]={
    case ResponseType.ROOM_MEMBER_LIST =>
      val roomMemberList= objectMapper.readValue(response.getData,classOf[GrpcRoomMemberList])
      val roomId = roomMemberList.roomId
      val membersStr = roomMemberList.membersJson
      val membersList =objectMapper.readValue(membersStr,classOf[Array[GrpcRoomMemberPayload]])
      val data=membersList.map(x=> x.UserName-> convertToPadplusRoomMemberPayload(x)).toMap
      val padplusRoomMemberMap = new PadplusRoomMemberMap
      padplusRoomMemberMap.members = data
      savePadplusRoomMembers(roomId,padplusRoomMemberMap)

      val promises = roomMemberPayloadPromises.getIfPresent(roomId)
      if(promises != null){
        promises.map(_.success(padplusRoomMemberMap))
      }
  }
}
