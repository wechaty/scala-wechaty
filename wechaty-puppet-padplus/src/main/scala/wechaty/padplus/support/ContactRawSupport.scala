package wechaty.padplus.support

import java.util
import java.util.concurrent.TimeUnit

import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import com.google.protobuf.ByteString
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.{BinaryBitmap, DecodeHintType, MultiFormatReader}
import javax.imageio.ImageIO
import wechaty.padplus.PuppetPadplus
import wechaty.padplus.grpc.PadPlusServerOuterClass.{ApiType, ResponseType, StreamResponse}
import wechaty.padplus.schemas.GrpcSchemas.GrpcQrCodeLogin
import wechaty.padplus.schemas.ModelContact.{GrpcContactPayload, PadplusContactPayload}
import wechaty.padplus.schemas.ModelRoom.GrpcRoomPayload
import wechaty.padplus.schemas.ModelUser.ScanData
import wechaty.padplus.schemas.PadplusEnums.QrcodeStatus
import wechaty.puppet.ResourceBox
import wechaty.puppet.schemas.Contact.{ContactGender, ContactPayload, ContactType}
import wechaty.puppet.schemas.Event.{EventLoginPayload, EventScanPayload}
import wechaty.puppet.schemas.Puppet._
import wechaty.puppet.schemas.{Contact, Puppet}

import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.util.Try

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-21
  */
trait ContactRawSupport {
  self: PuppetPadplus =>
  protected lazy val contactPromises: Cache[String, List[Promise[PadplusContactPayload]]] = {
    Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(1, TimeUnit.MINUTES).build().asInstanceOf[Cache[String, List[Promise[PadplusContactPayload]]]]
  }

  /**
    *
    * Contact
    *
    */
  override def contactAlias(contactId: String): String = ???

  override def contactAlias(contactId: String, alias: String): Unit = ???

  override def contactAvatar(contactId: String): ResourceBox = ???

  override def contactAvatar(contactId: String, file: ResourceBox): ResourceBox = ???

  override def contactList(): Array[String] = ???

  /**
    * contact
    */
  override protected def contactRawPayload(contactId: String): Contact.ContactPayload = {
    this.getContact(contactId)
  }

  protected def getContact(contactId: String): PadplusContactPayload = {
    val json = objectMapper.createObjectNode()
    json.put("userName", contactId)
    syncRequest[GrpcContactPayload](ApiType.GET_CONTACT,Some(json.toString))
  }

  protected def loginPartialFunction(response: StreamResponse): PartialFunction[ResponseType, Unit] = {
    case ResponseType.QRCODE_SCAN =>
      onQrcodeScan(response)
    case ResponseType.LOGIN_QRCODE =>
      val qrcodeData = objectMapper.readTree(response.getData)
      logger.debug("qrcode:",response.getData)

      val base64            = qrcodeData.get("qrcode").asText().replaceAll("\r|\n", "")
      val fileBox           = ResourceBox.fromBase64(s"qrcode${(Math.random() * 10000).intValue()}.png", base64)
      //解析二维码的类
      val multiFormatReader = new MultiFormatReader();
      //要解析的二维码的图片
      val bufferedImage     = ImageIO.read(fileBox.toStream);
      val binaryBitmap      = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
      //二维码的参数设置
      val hints             = new util.HashMap[DecodeHintType, String]();
      hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); //设置二维码的编码
    //得到解析结果,
    val result  = multiFormatReader.decode(binaryBitmap, hints);
    val payload = new EventScanPayload
      payload.qrcode = result.getText
      logger.debug("Scan QR Code to login: %s\nhttps://wechaty.github.io/qrcode/%s\n".format(payload.status, payload.qrcode))
      emit(PuppetEventName.SCAN, payload)
    case ResponseType.QRCODE_LOGIN =>
      val loginData             = objectMapper.readValue(response.getData, classOf[GrpcQrCodeLogin])
      this.selfId = Some(loginData.userName)
      this.saveUin(ByteString.copyFromUtf8(loginData.uin))
      val padplusContactPayload = new PadplusContactPayload
      padplusContactPayload.alias = loginData.alias
      padplusContactPayload.bigHeadUrl = loginData.headImgUrl
      padplusContactPayload.nickName = loginData.nickName
      padplusContactPayload.sex = ContactGender.Unknown
      padplusContactPayload.userName = loginData.userName
      padplusContactPayload.verifyFlag = 0
      savePadplusContactPayload( padplusContactPayload)
      val eventLoginPayload = new EventLoginPayload
      eventLoginPayload.contactId = padplusContactPayload.userName
      emit(PuppetEventName.LOGIN, eventLoginPayload)

      Future {
        getContact(padplusContactPayload.userName)
      }
    case ResponseType.AUTO_LOGIN =>
      logger.debug("response data:{}", response.getData)
      val autoLoginData = objectMapper.readTree(response.getData)
      if (autoLoginData.get("online").asBoolean()) {
        val wechatUser = autoLoginData.get("wechatUser")

        if (wechatUser != null) {
          val rawContactPayload = new PadplusContactPayload
          if (wechatUser.has("alias"))
            rawContactPayload.alias = wechatUser.get("alias").asText("")
          rawContactPayload.bigHeadUrl = wechatUser.get("headImgUrl").asText()
          rawContactPayload.nickName = wechatUser.get("nickName").asText()
          rawContactPayload.sex = ContactGender.Unknown
          rawContactPayload.userName = wechatUser.get("userName").asText()
          savePadplusContactPayload(rawContactPayload)
          // "{\"uin\":1213374243,\"online\":true,\"wechatUser\":{\"headImgUrl\":\"http://wx.qlogo.cn/mmhead/ver_1/iag5D2R2U9ibgTW2eh7XUbPTHqpEMP2DhSpXSBeQYzEPWgEmLIx5IDibwicGh4fTh4IibkL4hNianoiaTzXmVORnm1O4ZjhxfPosKzkMPSwic8Iicylk/0\",\"nickName\":\"\351\230\277\350\224\241\",\"uin\":1213374243,\"userName\":\"wxid_gbk03zsepqny22\",\"alias\":\"\",\"verifyFlag\":0}}"
          selfId = Some(rawContactPayload.userName)
        }

//        contactSelfInfo { padplusContact =>
//          selfId = Some(padplusContact.userName)
//          logger.debug("contactSelf:{}", padplusContact)
//          savePadplusContactPayload(padplusContact)
//          val eventLoginPayload = new EventLoginPayload
//          eventLoginPayload.contactId = padplusContact.userName
//          emit(PuppetEventName.LOGIN, eventLoginPayload)
//        }
      } else {
        deleteUin()
        asyncRequest(ApiType.GET_QRCODE)
      }
    case ResponseType.CONTACT_LIST | ResponseType.CONTACT_MODIFY =>
      val data = response.getData
      if(!isBlank(data)){
        val root = objectMapper.readTree(data)
        val userName = root.get("UserName").asText()
        if(isRoomId(userName)){
          val roomTry=Try {
            val grpcRoomPayload = objectMapper.readValue(data, classOf[GrpcRoomPayload])
            val roomPayload     = convertRoomFromGrpc(grpcRoomPayload)
            /* //TODO process room members
            const roomMembers = briefRoomMemberParser(roomPayload.members)
            const _roomMembers = await this.cacheManager.getRoomMember(roomPayload.chatroomId)
            if (!_roomMembers) {
              await this.cacheManager.setRoomMember(roomPayload.chatroomId, roomMembers)
            }
            await this.cacheManager.setRoom(roomPayload.chatroomId, roomPayload)
          } else {
            throw new PadplusError(PadplusErrorType.NO_CACHE, `CONTACT_MODIFY`)
          }
           */
            savePadplusRoomPayload(roomPayload)

            roomPayload
          }
          val roomCallback=roomPromises.getIfPresent(userName)
          if(roomCallback != null)
            roomCallback.foreach(_.complete(roomTry))

        }else{
          val result:Try[PadplusContactPayload] = Try {
            val contact               = objectMapper.readValue(data, classOf[GrpcContactPayload])
            val padplusContactPayload = convertFromGrpcContact(contact)
            savePadplusContactPayload(padplusContactPayload)
            padplusContactPayload
          }
          val callbacks =contactPromises.getIfPresent(userName)
          if(callbacks!=null){
            callbacks.foreach(_.complete(result))
          }
        }
      }
  }

  private def onQrcodeScan(response: StreamResponse): Unit = {
    val scanRawData = response.getData()
    val scanData    = objectMapper.readValue(scanRawData, classOf[ScanData])
    QrcodeStatus(scanData.status.intValue()) match {
      case QrcodeStatus.Scanned =>

      case QrcodeStatus.Confirmed =>
//        contactSelfInfo { padplusContact =>
//          selfId = Some(padplusContact.userName)
//          logger.debug("contactSelf:{}", padplusContact)
//          savePadplusContactPayload(padplusContact)
//          val eventLoginPayload = new EventLoginPayload
//          eventLoginPayload.contactId = padplusContact.userName
//          emit(PuppetEventName.LOGIN, eventLoginPayload)
//        }
      case QrcodeStatus.Canceled | QrcodeStatus.Expired =>

    }
  }

  implicit def convertFromGrpcContact (contactPayload: GrpcContactPayload): PadplusContactPayload = {
    val payload = new PadplusContactPayload
    payload.alias            = contactPayload.Alias
    payload.bigHeadUrl       = contactPayload.BigHeadImgUrl
    payload.city             = contactPayload.City
    payload.contactFlag      = contactPayload.ContactFlag
    payload.contactType      = if(Puppet.isBlank(contactPayload.ContactType)) 0 else contactPayload.ContactType.toInt
    payload.country          = ""
    payload.nickName         = contactPayload.NickName
    payload.province         = contactPayload.Province
    payload.remark           = contactPayload.RemarkName
    payload.sex              = Contact.ContactGender(contactPayload.Sex.intValue())
    payload.signature        = contactPayload.Signature
    payload.smallHeadUrl     = contactPayload.SmallHeadImgUrl
    payload.stranger         = contactPayload.EncryptUsername
    payload.tagList          = contactPayload.LabelLists
    payload.ticket           = ""
    payload.userName         = contactPayload.UserName
    payload.verifyFlag       = contactPayload.VerifyFlag

    payload
  }
  implicit def convertPadplusContactToContactPayload(rawPayload:PadplusContactPayload): ContactPayload ={
    if (isRoomId(rawPayload.userName)) {
      throw new Error("Room Object instead of Contact!")
    }

    var contactType = ContactType.Unknown
    if (isContactOfficialId(rawPayload.userName) || rawPayload.verifyFlag != 0) {
      contactType = ContactType.Official
    } else {
      contactType = ContactType.Personal
    }
    var friend = false
    if (rawPayload.contactFlag > 0 && rawPayload.contactFlag != 0 && rawPayload.verifyFlag == 0) {
      friend = true
    }
    val payload = new ContactPayload
      payload.alias     = rawPayload.remark
      payload.avatar    = rawPayload.bigHeadUrl
      payload.city      = rawPayload.city
      payload.friend    = friend
      payload.gender    = rawPayload.sex
      payload.id        = rawPayload.userName
      payload.name      = rawPayload.nickName
      payload.province  = rawPayload.province
      payload.signature = (rawPayload.signature).replace("+", "")          // Stay+Foolis
      payload.`type`      = contactType
      payload.weixin    = rawPayload.alias

    payload
  }
}
