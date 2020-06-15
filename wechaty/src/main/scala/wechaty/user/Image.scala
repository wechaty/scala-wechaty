package wechaty.user

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.schemas.Image.ImageType
import wechaty.puppet.{LoggerSupport, ResourceBox}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-15
  */
class Image(imageId:String)(implicit resolver:PuppetResolver) extends LoggerSupport{
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
