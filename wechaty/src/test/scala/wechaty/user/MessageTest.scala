package wechaty.user

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Contact.ContactPayloadResponse
import io.github.wechaty.grpc.puppet.Message.{MessageFileResponse, MessagePayloadResponse, MessageSendFileResponse, MessageType}
import io.github.wechaty.grpc.puppet.Room.RoomPayloadResponse
import io.github.wechaty.grpc.puppet.RoomMember.RoomMemberPayloadResponse
import org.grpcmock.GrpcMock.{stubFor, unaryMethod}
import org.junit.jupiter.api.{Assertions, Test}
import wechaty.TestBase

import scala.io.Source

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-08
  */
class MessageTest extends TestBase{
  private def constructMessage(message:String,memberIds:String*): Unit ={
    val roomId = "roomId"
    val response = MessagePayloadResponse.newBuilder()
      .setText(message)
      .setType(MessageType.MESSAGE_TYPE_VIDEO) //TODO because RPC return wrong messageType
      .setRoomId(roomId)
      .build()
    stubFor(unaryMethod(PuppetGrpc.getMessagePayloadMethod)
      .willReturn(response))

    memberIds.toList match{
      case head::remain=>
        val method = unaryMethod(PuppetGrpc.getContactPayloadMethod)
        val response = ContactPayloadResponse.newBuilder()
          .setName(head)
          .build()
        val next = method.willReturn(response)
        remain.foldLeft(next){(n,id)=>{
          val response = ContactPayloadResponse.newBuilder()
            .setName(id)
            .build()
          n.nextWillReturn(response)
        }}
        stubFor(method)
      case Nil =>
    }

    val roomMemberPayloadResponse = RoomMemberPayloadResponse.newBuilder().build()
    stubFor(unaryMethod(PuppetGrpc.getRoomMemberPayloadMethod)
      .willReturn(roomMemberPayloadResponse)
    )
    val roomPayloadResponse = RoomPayloadResponse.newBuilder()
    roomPayloadResponse.setId(roomId)
    stubFor(unaryMethod(PuppetGrpc.getRoomPayloadMethod)
      .willReturn(roomPayloadResponse.build())
    )
  }
  @Test
  def testMention: Unit ={
    constructMessage("hello @member1\u2005@member2\u0020@member3",
      "member1","member2","member3")

    val message = new Message("messageId")

    val mentionList = message.mentionList
    Assertions.assertEquals(3,message.mentionList.size)
    Assertions.assertEquals("member1",mentionList(0).id)
    Assertions.assertEquals("member2",mentionList(1).id)
    Assertions.assertEquals("member3",mentionList(2).id)
    Assertions.assertEquals("hello",message.mentionText())
  }
  @Test
  def testMention2: Unit ={
    val wexinText = "中文@member1\u2005@member2\u0020@member3\u0020中文测试"
    constructMessage(wexinText,"member1","member2","member3")

    val message = new Message("messageId")

    val mentionList = message.mentionList
    Assertions.assertEquals(3,message.mentionList.size)
    Assertions.assertEquals("member1",mentionList(0).id)
    Assertions.assertEquals("member2",mentionList(1).id)
    Assertions.assertEquals("member3",mentionList(2).id)
    Assertions.assertEquals("中文中文测试",message.mentionText())
  }
  @Test
  def testMention3: Unit ={
    //测试名称特殊字符
    val wexinText = "中文@mem(|&ber1\u2005@member2\u0020@member3\u0020中文测试"
    constructMessage(wexinText,"mem(|&ber1","member2","member3")

    val message = new Message("messageId")

    val mentionList = message.mentionList
    Assertions.assertEquals(3,message.mentionList.size)
    Assertions.assertEquals("mem(|&ber1",mentionList(0).id)
    Assertions.assertEquals("member2",mentionList(1).id)
    Assertions.assertEquals("member3",mentionList(2).id)
    Assertions.assertEquals("中文中文测试",message.mentionText())
  }
  @Test
  def testMentionAll: Unit ={
    //测试名称特殊字符
    val wexinText = "@所有人\u2005hello"
    constructMessage(wexinText,"所有人")

    val message = new Message("messageId")

    val mentionList = message.mentionList
    Assertions.assertEquals(1,message.mentionList.size)
    Assertions.assertEquals("hello",message.mentionText())
  }
  @Test
  def testForward: Unit ={
    val roomId = "roomId"
    val response = MessagePayloadResponse.newBuilder()
      .setType(MessageType.MESSAGE_TYPE_TEXT) //TODO because RPC return wrong messageType
      .setRoomId(roomId)
      .build()
    stubFor(unaryMethod(PuppetGrpc.getMessagePayloadMethod)
      .willReturn(response))

    val roomPayloadResponse = RoomPayloadResponse.newBuilder()
    roomPayloadResponse.setId(roomId)
    stubFor(unaryMethod(PuppetGrpc.getRoomPayloadMethod)
      .willReturn(roomPayloadResponse.build())
    )

    val json = Source.fromInputStream(getClass.getResourceAsStream("/filebox1.json")).mkString
    val messageFileResponse = MessageFileResponse.newBuilder()
      .setFilebox(json)
    stubFor(unaryMethod(PuppetGrpc.getMessageFileMethod)
      .willReturn(messageFileResponse.build())
    )

    val messageSendFileResponse = MessageSendFileResponse.newBuilder()
    stubFor(unaryMethod(PuppetGrpc.getMessageSendFileMethod)
      .willReturn(messageSendFileResponse.build())
    )

    val resourceBox = instance.puppet.messageFile("messageId")
    Assertions.assertEquals(" f2ebcd57ccf602d7a15f480100855974.jpg",resourceBox.name.trim)
    val message = new Message("messageId")
    val room = Room.load("roomId").get
    message.forward(room)
  }
}
