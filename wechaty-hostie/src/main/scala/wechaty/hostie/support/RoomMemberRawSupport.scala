package wechaty.hostie.support

import com.google.protobuf.StringValue
import io.github.wechaty.grpc.puppet.{Room, RoomMember}
import wechaty.puppet.schemas.Room.RoomMemberPayload
import wechaty.puppet.{Puppet, schemas}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait RoomMemberRawSupport {
  self: Puppet with GrpcSupport =>
  /**
    *
    * RoomMember
    *
    */
  override def roomAnnounce(roomId: String): String = {
    val request = Room.RoomAnnounceRequest.newBuilder()
      .setId(roomId)
      .build()

    val response = grpcClient.roomAnnounce(request)
    response.getText.getValue
  }

  override def roomAnnounce(roomId: String, text: String): Unit = {
    val value = StringValue.newBuilder().setValue(text)

    val request = Room.RoomAnnounceRequest.newBuilder()
      .setId(roomId)
      .setText(value)
      .build()

    grpcClient.roomAnnounce(request)
  }

  override def roomMemberList(roomId: String): Array[String] = {
    val request = RoomMember.RoomMemberListRequest.newBuilder()
      .setId(roomId)
      .build()

    val response = grpcClient.roomMemberList(request)
    response.getMemberIdsList.toArray(Array[String]())
  }

  override protected def roomMemberRawPayload(roomId: String, contactId: String): schemas.Room.RoomMemberPayload = {
    val request = RoomMember.RoomMemberPayloadRequest.newBuilder()
      .setId(roomId)
      .setMemberId(contactId)
      .build()

    val response = grpcClient.roomMemberPayload(request)
    val payload = new RoomMemberPayload()

    payload.avatar = response.getAvatar
    payload.id = response.getId
    payload.inviterId = response.getInviterId
    payload.name = response.getName
    payload.roomAlias = response.getRoomAlias
    payload
  }
}
