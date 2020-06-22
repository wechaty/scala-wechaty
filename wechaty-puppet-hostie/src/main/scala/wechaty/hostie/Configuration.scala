package wechaty.hostie

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object Configuration {
  lazy val WECHATY_PUPPET_HOSTIE_TOKEN: Option[String] = {
    var value=sys.props.get("WECHATY_PUPPET_HOSTIE_TOKEN")
    if(value.isEmpty) value = sys.env.get("WECHATY_PUPPET_HOSTIE_TOKEN")
    value
  }
  lazy val WECHATY_PUPPET_HOSTIE_ENDPOINT: Option[String] = {
    var value = sys.props.get("WECHATY_PUPPET_HOSTIE_ENDPOINT")
    if(value.isEmpty) value = sys.env.get("WECHATY_PUPPET_HOSTIE_ENDPOINT")
    value
  }
}
