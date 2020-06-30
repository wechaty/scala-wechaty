package wechaty.padplus.support

import com.typesafe.scalalogging.LazyLogging
import wechaty.padplus.grpc.PadPlusServerOuterClass.ApiType
import wechaty.padplus.schemas.ModelContact.{GetContactSelfInfoGrpcResponse, PadplusContactPayload}
import wechaty.puppet.schemas.Contact.ContactGender
import wechaty.puppet.support.ContactSelfSupport

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

  def contactSelfInfo(callback:PadplusContactPayload=>Unit):Unit={
//    asyncRequest[GetContactSelfInfoGrpcResponse](ApiType.GET_CONTACT_SELF_INFO)
    asyncRequest[GetContactSelfInfoGrpcResponse](ApiType.GET_CONTACT_SELF_INFO).map{ contactPayload =>
        val payload = new PadplusContactPayload
        payload.alias = contactPayload.alias;
        payload.bigHeadUrl = contactPayload.bigHeadImg;
        payload.city = contactPayload.city
        payload.contactFlag = 3
        payload.contactType = 0
        payload.country = contactPayload.country
        payload.nickName = contactPayload.nickName
        payload.province = contactPayload.province
        payload.remark = ""
        payload.sex = ContactGender(contactPayload.sex)
        payload.signature = contactPayload.signature
        payload.smallHeadUrl = contactPayload.smallHeadImg
        payload.stranger = ""
        payload.tagList = ""
        payload.ticket = ""
        payload.userName = contactPayload.userName
        payload.verifyFlag = 0
        callback(payload)
    }
  }
}
