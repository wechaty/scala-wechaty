package wechaty.padplus.support

import java.io.{File, FileOutputStream}
import java.util
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.{BinaryBitmap, DecodeHintType, MultiFormatReader}
import com.typesafe.scalalogging.LazyLogging
import io.grpc.stub.StreamObserver
import javax.imageio.ImageIO
import org.apache.commons.io.IOUtils
import wechaty.padplus.grpc.PadPlusServerOuterClass.{ResponseType, StreamResponse}
import wechaty.padplus.schemas.ModelUser.ScanData
import wechaty.padplus.schemas.PadplusEnums.QrcodeStatus
import wechaty.puppet.schemas.Event.EventScanPayload
import wechaty.puppet.schemas.Puppet.{PuppetEventName, isBlank, objectMapper}
import wechaty.puppet.{Puppet, ResourceBox}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-21
  */
trait GrpcEventSupport extends StreamObserver[StreamResponse]{
  self: GrpcSupport with LocalStoreSupport with Puppet with LazyLogging=>

  private val countDownLatch = new CountDownLatch(1)


  protected def awaitStreamStart()=countDownLatch.await(10,TimeUnit.SECONDS)
  override def onNext(response: StreamResponse): Unit = {
    logger.debug("stream response:{}",response)
    countDownLatch.countDown()

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
        case _ =>
          saveUin(response.getUinBytes)
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
