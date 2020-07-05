package wechaty.padplus.support

import com.typesafe.scalalogging.LazyLogging
import wechaty.padplus.grpc.PadPlusServerOuterClass.ApiType
import wechaty.padplus.schemas.ModelContact.{GetContactSelfInfoGrpcResponse, PadplusContactPayload}
import wechaty.puppet.schemas.Contact.ContactGender
import wechaty.puppet.support.ContactSelfSupport

import scala.concurrent.Future

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-22
  */
trait ContactSelfRawSupport {
  self :GrpcSupport with LazyLogging with ContactSelfSupport =>
  /**
    *
    * ContactSelf
    *
    */
  override def contactSelfName(name: String): Unit = ???

  override def contactSelfQRCode(): String = ???

  override def contactSelfSignature(signature: String): Unit = ???

  override def logout(): Unit = ???

  def contactSelfInfo():Future[PadplusContactPayload]={
    asyncRequest[GetContactSelfInfoGrpcResponse](ApiType.GET_CONTACT_SELF_INFO).map(response =>{
      val payload = new PadplusContactPayload
      payload.alias = response.alias;
      payload.bigHeadUrl = response.bigHeadImg;
      payload.city = response.city
      payload.contactFlag = 3
      payload.contactType = 0
      payload.country = response.country
      payload.nickName = response.nickName
      payload.province = response.province
      payload.remark = ""
      payload.sex = ContactGender(response.sex)
      payload.signature = response.signature
      payload.smallHeadUrl = response.smallHeadImg
      payload.stranger = ""
      payload.tagList = ""
      payload.ticket = ""
      payload.userName = response.userName
      payload.verifyFlag = 0
      payload
    })
  }
}
