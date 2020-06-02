package wechaty.hostie

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object Configuration {
  lazy val WechatyPuppetHostieToken = sys.props.get("WECHATY_PUPPET_HOSTIE_TOKEN")
  lazy val WechatyPuppetHostieEndpoint= sys.props.get("WECHATY_PUPPET_HOSTIE_ENDPOINT")
}
