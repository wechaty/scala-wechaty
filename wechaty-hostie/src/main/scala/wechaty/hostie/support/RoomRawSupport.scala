package wechaty.hostie.support

import java.util

import com.google.protobuf.StringValue
import io.github.wechaty.grpc.puppet.Room
import wechaty.puppet.schemas.Room.RoomPayload
import wechaty.puppet.support.RoomSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait RoomRawSupport {
  self: RoomSupport with GrpcSupport =>
  override def roomAdd(roomId: String, contactId: String): Unit = {
    val request = Room.RoomAddRequest.newBuilder()
      .setContactId(contactId)
      .setId(roomId)
      .build()

    grpcClient.roomAdd(request)
  }

  override def roomCreate(contactIdList: Array[String], topic: String): String = {
    val request = Room.RoomCreateRequest.newBuilder()
      .setTopic(topic)
      .addAllContactIds(util.Arrays.asList(contactIdList: _*))
      .build()

    val response = grpcClient.roomCreate(request)
    response.getId
  }

  override def roomDel(roomId: String, contactId: String): Unit = {
    val request = Room.RoomDelRequest.newBuilder()
      .setId(roomId)
      .setContactId(contactId)
      .build()

    grpcClient.roomDel(request)
  }

  override def roomList(): Array[String] = {
    val request = Room.RoomListRequest.newBuilder().build()

    val response = grpcClient.roomList(request)
    response.getIdsList.toArray(Array[String]())
  }

  override def roomQRCode(roomId: String): String = {
    val request = Room.RoomQRCodeRequest.newBuilder()
      .setId(roomId)
      .build()


    val response = grpcClient.roomQRCode(request)
    response.getQrcode
  }

  override def roomQuit(roomId: String): Unit = {
    val request = Room.RoomQuitRequest.newBuilder()
      .setId(roomId)
      .build()

    grpcClient.roomQuit(request)
  }

  override def roomTopic(roomId: String): String = {
    val request = Room.RoomTopicRequest.newBuilder()
      .setId(roomId)
      .build()

    val response = grpcClient.roomTopic(request)
    response.getTopic.getValue
  }

  override def roomTopic(roomId: String, topic: String): Unit = {
    val value = StringValue.newBuilder().setValue(topic)

    val request = Room.RoomTopicRequest.newBuilder()
      .setId(roomId)
      .setTopic(value)
      .build()

    grpcClient.roomTopic(request)
  }

  override protected def roomRawPayload(roomId: String): RoomPayload = {
    val request = Room.RoomPayloadRequest.newBuilder()
      .setId(roomId)
      .build()

    val response = grpcClient.roomPayload(request)
    val payload = new RoomPayload

    payload.id = response.getId
    payload.adminIdList = response.getAdminIdsList.toArray(Array[String]())
    payload.avatar = response.getAvatar
    payload.memberIdList = response.getMemberIdsList().toArray(Array[String]())
    payload.ownerId = response.getOwnerId
    payload.topic = response.getTopic
    payload
  }
}
