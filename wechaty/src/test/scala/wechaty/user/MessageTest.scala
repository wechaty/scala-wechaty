package wechaty.user

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Contact.ContactPayloadResponse
import io.github.wechaty.grpc.puppet.Message.{MessagePayloadResponse, MessageType}
import org.grpcmock.GrpcMock.{stubFor, unaryMethod}
import org.junit.jupiter.api.{Assertions, Test}
import wechaty.TestBase

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-08
  */
class MessageTest extends TestBase{
  @Test
  def testMention: Unit ={
    val wexinText = "hello @member1\u2005@member2\u0020@member3"
    val response = MessagePayloadResponse.newBuilder()
      .setText(wexinText)
      .setType(MessageType.MESSAGE_TYPE_TEXT)
      .setRoomId("roomId")
      .build()
    stubFor(unaryMethod(PuppetGrpc.getMessagePayloadMethod)
      .willReturn(response))

    val member1Resposne = ContactPayloadResponse.newBuilder()
      .setName("member1")
      .build()
    val member2Resposne = ContactPayloadResponse.newBuilder()
      .setName("member2")
      .build()
    val member3Resposne = ContactPayloadResponse.newBuilder()
      .setName("member3")
      .build()
    stubFor(unaryMethod(PuppetGrpc.getContactPayloadMethod)
      .willReturn(member1Resposne)
      .nextWillReturn(member2Resposne)
      .nextWillReturn(member3Resposne)
    )


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
    val response = MessagePayloadResponse.newBuilder()
      .setText(wexinText)
      .setType(MessageType.MESSAGE_TYPE_TEXT)
      .setRoomId("roomId")
      .build()
    stubFor(unaryMethod(PuppetGrpc.getMessagePayloadMethod)
      .willReturn(response))

    val member1Resposne = ContactPayloadResponse.newBuilder()
      .setName("member1")
      .build()
    val member2Resposne = ContactPayloadResponse.newBuilder()
      .setName("member2")
      .build()
    val member3Resposne = ContactPayloadResponse.newBuilder()
      .setName("member3")
      .build()
    stubFor(unaryMethod(PuppetGrpc.getContactPayloadMethod)
      .willReturn(member1Resposne)
      .nextWillReturn(member2Resposne)
      .nextWillReturn(member3Resposne)
    )


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
    val response = MessagePayloadResponse.newBuilder()
      .setText(wexinText)
      .setType(MessageType.MESSAGE_TYPE_TEXT)
      .setRoomId("roomId")
      .build()
    stubFor(unaryMethod(PuppetGrpc.getMessagePayloadMethod)
      .willReturn(response))

    val member1Resposne = ContactPayloadResponse.newBuilder()
      .setName("mem(|&ber1")
      .build()
    val member2Resposne = ContactPayloadResponse.newBuilder()
      .setName("member2")
      .build()
    val member3Resposne = ContactPayloadResponse.newBuilder()
      .setName("member3")
      .build()
    stubFor(unaryMethod(PuppetGrpc.getContactPayloadMethod)
      .willReturn(member1Resposne)
      .nextWillReturn(member2Resposne)
      .nextWillReturn(member3Resposne)
    )


    val message = new Message("messageId")

    val mentionList = message.mentionList
    Assertions.assertEquals(3,message.mentionList.size)
    Assertions.assertEquals("mem(|&ber1",mentionList(0).id)
    Assertions.assertEquals("member2",mentionList(1).id)
    Assertions.assertEquals("member3",mentionList(2).id)
    Assertions.assertEquals("中文中文测试",message.mentionText())
  }
}
