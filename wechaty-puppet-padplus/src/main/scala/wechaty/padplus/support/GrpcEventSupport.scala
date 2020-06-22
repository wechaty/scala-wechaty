package wechaty.padplus.support

import java.io.{File, FileOutputStream}
import java.util
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.{BinaryBitmap, DecodeHintType, MultiFormatReader}
import io.grpc.stub.StreamObserver
import javax.imageio.ImageIO
import org.apache.commons.io.IOUtils
import wechaty.padplus.PuppetPadplus
import wechaty.padplus.grpc.PadPlusServerOuterClass.{ApiType, ResponseType, StreamResponse}
import wechaty.padplus.schemas.GrpcSchemas.{GrpcMessagePayload, GrpcQrCodeLogin}
import wechaty.padplus.schemas.ModelContact.PadplusContactPayload
import wechaty.padplus.schemas.ModelUser.ScanData
import wechaty.padplus.schemas.PadplusEnums.QrcodeStatus
import wechaty.puppet.ResourceBox
import wechaty.puppet.schemas.Contact.ContactGender
import wechaty.puppet.schemas.Event.{EventLoginPayload, EventMessagePayload, EventScanPayload}
import wechaty.puppet.schemas.Puppet.{PuppetEventName, isBlank, objectMapper}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-21
  */
trait GrpcEventSupport extends StreamObserver[StreamResponse]{
  self: PuppetPadplus =>

  protected var selfId:Option[String] = None
  private val countDownLatch = new CountDownLatch(1)


  protected def awaitStreamStart()=countDownLatch.await(10,TimeUnit.SECONDS)
  override def onNext(response: StreamResponse): Unit = {
    logger.debug("stream response:{}",response)
    countDownLatch.countDown()
    saveUin(response.getUinBytes)

    val traceId = response.getTraceId
    if(!isBlank(traceId)){
      val callback = callbackPool.get(traceId)
      if(callback != null){
        callback(response)
      }
    }else {
      val responseType = response.getResponseType
      responseType match {
        case ResponseType.QRCODE_SCAN =>
          onQrcodeScan(response)
        case ResponseType.LOGIN_QRCODE =>
          val qrcodeData = objectMapper.readTree(response.getData)
          println(response.getData)

          val base64=qrcodeData.get("qrcode").asText().replaceAll("\r|\n","")
          val fileBox = ResourceBox.fromBase64(s"qrcode${(Math.random() * 10000).intValue()}.png",base64)
          //解析二维码的类
          val multiFormatReader=new MultiFormatReader();
          //要解析的二维码的图片
          IOUtils.copy(fileBox.toStream,new FileOutputStream(new File("test.png")))
          val bufferedImage= ImageIO.read(fileBox.toStream);
          val binaryBitmap=new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
          //二维码的参数设置
          val hints=new util.HashMap[DecodeHintType,String]();
          hints.put(DecodeHintType.CHARACTER_SET,"utf-8");//设置二维码的编码
          //得到解析结果,
          val result= multiFormatReader.decode(binaryBitmap,hints);
          val payload = new EventScanPayload
          payload.qrcode = result.getText
          logger.debug("Scan QR Code to login: %s\nhttps://api.qrserver.com/v1/create-qr-code/?data=%s\n".format(payload.status, payload.qrcode))
          emit(PuppetEventName.SCAN,payload)
        case ResponseType.MESSAGE_RECEIVE =>
          val rawMessageStr = response.getData()
          val payload = objectMapper.readValue(rawMessageStr,classOf[GrpcMessagePayload])
          val eventMessagePayload: EventMessagePayload = new EventMessagePayload
          eventMessagePayload.messageId = payload.MsgId
          saveRawMessagePayload(payload.MsgId,rawMessageStr)
          this.emit(PuppetEventName.MESSAGE, eventMessagePayload)
        case ResponseType.QRCODE_LOGIN =>
          val loginData = objectMapper.readValue(response.getData,classOf[GrpcQrCodeLogin])
          val padplusContactPayload=new PadplusContactPayload
          padplusContactPayload.alias=loginData.alias
          padplusContactPayload.bigHeadUrl=loginData.headImgUrl
          padplusContactPayload.nickName=loginData.nickName
          padplusContactPayload.sex=ContactGender.Unknown
          padplusContactPayload.userName=loginData.userName
          padplusContactPayload.verifyFlag=0
          saveRawContactPayload(padplusContactPayload.userName,padplusContactPayload)
          val eventLoginPayload = new EventLoginPayload
          eventLoginPayload.contactId = padplusContactPayload.userName
          emit(PuppetEventName.LOGIN, eventLoginPayload)
          request(ApiType.GET_CONTACT, )


        case ResponseType.AUTO_LOGIN =>
          logger.debug("response data:{}",response.getData)
          val autoLoginData = objectMapper.readTree(response.getData)
          if(autoLoginData.get("online").asBoolean()) {
            val wechatUser = autoLoginData.get("wechatUser")

            if (wechatUser != null) {
              val rawContactPayload = new PadplusContactPayload
              if (wechatUser.has("alias"))
                rawContactPayload.alias = wechatUser.get("alias").asText("")
              rawContactPayload.bigHeadUrl = wechatUser.get("headImgUrl").asText()
              rawContactPayload.nickName = wechatUser.get("nickName").asText()
              rawContactPayload.sex = ContactGender.Unknown
              rawContactPayload.userName = wechatUser.get("userName").asText()
              saveRawContactPayload(rawContactPayload.userName, rawContactPayload)
              // "{\"uin\":1213374243,\"online\":true,\"wechatUser\":{\"headImgUrl\":\"http://wx.qlogo.cn/mmhead/ver_1/iag5D2R2U9ibgTW2eh7XUbPTHqpEMP2DhSpXSBeQYzEPWgEmLIx5IDibwicGh4fTh4IibkL4hNianoiaTzXmVORnm1O4ZjhxfPosKzkMPSwic8Iicylk/0\",\"nickName\":\"\351\230\277\350\224\241\",\"uin\":1213374243,\"userName\":\"wxid_gbk03zsepqny22\",\"alias\":\"\",\"verifyFlag\":0}}"
              selfId = Some(rawContactPayload.userName)
            }

            contactSelfInfo { padplusContact =>
              selfId = Some(padplusContact.userName)
              logger.debug("contactSelf:{}", padplusContact)
              saveRawContactPayload(padplusContact.userName, padplusContact)
              val eventLoginPayload = new EventLoginPayload
              eventLoginPayload.contactId = padplusContact.userName
              emit(PuppetEventName.LOGIN, eventLoginPayload)
            }
          }else{
            deleteUin()
            request(ApiType.GET_QRCODE)
          }
        case _ =>
//          val user = objectMapper.readTree(response.getData())
//          val userName = user.get("userName").asText()

      }
    }
 }

  def onQrcodeScan(response: StreamResponse): Unit = {
    val scanRawData = response.getData()
    val scanData = objectMapper.readValue(scanRawData,classOf[ScanData])
    QrcodeStatus(scanData.status.intValue()) match{
      case QrcodeStatus.Scanned =>

      case QrcodeStatus.Confirmed =>
        contactSelfInfo { padplusContact =>
          selfId = Some(padplusContact.userName)
          logger.debug("contactSelf:{}", padplusContact)
          saveRawContactPayload(padplusContact.userName, padplusContact)
          val eventLoginPayload = new EventLoginPayload
          eventLoginPayload.contactId = padplusContact.userName
          emit(PuppetEventName.LOGIN, eventLoginPayload)
        }
      case QrcodeStatus.Canceled | QrcodeStatus.Expired=>

    }
  }

  override def onError(throwable: Throwable): Unit = {
    logger.error(throwable.getMessage,throwable)
  }

  override def onCompleted(): Unit = {
    logger.info("completed")
  }
}
