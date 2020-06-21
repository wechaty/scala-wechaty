package wechaty.puppet.support

import com.github.benmanes.caffeine.cache.Caffeine

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-21
  */
trait CacheSupport {
  protected def createCache()= {
    //TODO optimize lru cache
    Caffeine.newBuilder().maximumSize(100).build()
  }
}
