package wechaty.user

import com.typesafe.scalalogging.LazyLogging
import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.ResourceBox
import wechaty.puppet.schemas.Image.ImageType

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-15
  */
class Image(imageId:String)(implicit resolver:PuppetResolver) extends LazyLogging{
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
