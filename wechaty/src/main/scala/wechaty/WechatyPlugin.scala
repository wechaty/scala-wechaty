package wechaty

/**
  * wechaty plugin
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-19
  */
trait WechatyPlugin {
  def install(wechaty: Wechaty)
}
