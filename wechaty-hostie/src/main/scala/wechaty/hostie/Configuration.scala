package wechaty.hostie

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object Configuration {
  lazy val WECHATY_PUPPET_HOSTIE_TOKEN: Option[String] = sys.props.get("WECHATY_PUPPET_HOSTIE_TOKEN")
  lazy val WECHATY_PUPPET_HOSTIE_ENDPOINT: Option[String] = sys.props.get("WECHATY_PUPPET_HOSTIE_ENDPOINT")
}
