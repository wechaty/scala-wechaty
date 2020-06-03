package wechaty.puppet

import com.github.benmanes.caffeine.cache.Caffeine
import wechaty.puppet.support.{ContactSupport, MessageSupport}

/**
  * abstract puppet interface
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-03
  */
trait Puppet extends MessageSupport with ContactSupport{
  self:LoggerSupport =>

  protected def createCache()= {
    //TODO optimize lru cache
    Caffeine.newBuilder().maximumSize(100).build()
  }



}
