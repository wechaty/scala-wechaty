package wechaty.puppet.support

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait ContactSelfSupport {
  /**
    *
    * ContactSelf
    *
    */
  def contactSelfName(name: String): Unit

  def contactSelfQRCode(): String

  /* QR Code Value */
  def contactSelfSignature(signature: String): Unit

  def logout(): Unit

}
