package wechaty.user

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.{JacksonXmlProperty, JacksonXmlRootElement}
import com.typesafe.scalalogging.LazyLogging
import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.{ResourceBox, schemas}
import wechaty.puppet.schemas.Image.ImageType
import wechaty.user.Image.{CdnImage, Msg}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-15
  */
object Image{
  @JacksonXmlRootElement(localName = "msg")
  class Msg{
    @JacksonXmlProperty
    var img:CdnImage = _
  }
  class CdnImage{
    @JacksonXmlProperty(isAttribute = true)
    var aeskey:String = _
    @JacksonXmlProperty(isAttribute = true)
    var encryver :String = _
    @JacksonXmlProperty(isAttribute = true)
    var cdnthumblength :Int= _
    @JacksonXmlProperty(isAttribute = true)
    var cdnthumbheight:Int= _
    @JacksonXmlProperty(isAttribute = true)
    var cdnthumbwidth:Int= _
    @JacksonXmlProperty(isAttribute = true)
    var cdnmidheight:Int= _
    @JacksonXmlProperty(isAttribute = true)
    var cdnmidwidth:Int= _
    @JacksonXmlProperty(isAttribute = true)
    var cdnhdheight:Int= _
    @JacksonXmlProperty(isAttribute = true)
    var cdnhdwidth:Int= _
    @JacksonXmlProperty(isAttribute = true)
    var cdnmidimgurl:String = _
    @JacksonXmlProperty(isAttribute = true)
    var length:Int= _
    @JacksonXmlProperty(isAttribute = true)
    var cdnbigimgurl:String = _
    @JacksonXmlProperty(isAttribute = true)
    var hdlength:String = _
    @JacksonXmlProperty(isAttribute = true)
    var md5:String = _
    @JacksonXmlProperty(isAttribute = true)
    var cdnthumbaeskey:String = _
    @JacksonXmlProperty(isAttribute = true)
    var cdnthumburl:String = _
  }
}
class Image(imageId:String)(implicit resolver:PuppetResolver) extends LazyLogging{
  lazy val payload: schemas.Message.MessagePayload = {
    resolver
      .puppet
      .messagePayload(imageId)
  }
  def getCdnImage: CdnImage ={
    val mapper = new XmlMapper()
    mapper.readValue(payload.text,classOf[Msg]).img

  }

  def thumbnail : ResourceBox = {
    resolver.puppet.messageImage(this.imageId, ImageType.Thumbnail)
  }
  def hd: ResourceBox= {
    resolver.puppet.messageImage(this.imageId, ImageType.HD)
  }
  def artwork : ResourceBox =  {
    resolver.puppet.messageImage(this.imageId, ImageType.Artwork)
  }
}
