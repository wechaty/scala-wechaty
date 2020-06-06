package wechaty.hostie.support

import io.github.wechaty.grpc.puppet.RoomInvitation
import wechaty.puppet.Puppet
import wechaty.puppet.schemas.RoomInvitation.RoomInvitationPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait RoomInvitationRawSupport {
  self: Puppet with GrpcSupport =>
  override def roomInvitationAccept(roomInvitationId: String): Unit = {
    val request = RoomInvitation.RoomInvitationAcceptRequest.newBuilder()
      .setId(roomInvitationId)
      .build()

    grpcClient.roomInvitationAccept(request)
  }

  override protected def roomInvitationRawPayload(roomInvitationId: String): wechaty.puppet.schemas.RoomInvitation.RoomInvitationPayload = {
    val request = RoomInvitation.RoomInvitationPayloadRequest.newBuilder()
      .setId(roomInvitationId)
      .build()

    val response = grpcClient.roomInvitationPayload(request)

    val payload = new RoomInvitationPayload()

    payload.avatar = response.getAvatar
    payload.id = response.getId
    payload.invitation = response.getInvitation
    payload.inviterId = response.getInviterId
    payload.memberCount = response.getMemberCount
    payload.memberIdList = response.getMemberIdsList.toArray(Array[String]())
    payload.receiverId = response.getReceiverId
    payload.timestamp = response.getTimestamp
    payload.topic = response.getTopic

    payload
  }
}
