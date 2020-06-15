package wechaty.user

import wechaty.puppet.schemas.MiniProgram.MiniProgramPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
class MiniProgram(val payload: MiniProgramPayload) {
  def appid: String = this.payload.appid

  def title: String = this.payload.title

  def pagePath: String = this.payload.pagePath

  def username: String = this.payload.username

  def description: String = this.payload.description

  def thumbUrl: String = this.payload.thumbUrl

  def thumbKey: String = this.payload.thumbKey

}
