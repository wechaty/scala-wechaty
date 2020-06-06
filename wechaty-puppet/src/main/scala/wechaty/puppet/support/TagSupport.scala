package wechaty.puppet.support

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait TagSupport {
  def tagContactAdd(tagId: String, contactId: String): Unit

  def tagContactDelete(tagId: String): Unit

  def tagContactList(contactId: String): Array[String]

  def tagContactList(): Array[String]

  def tagContactRemove(tagId: String, contactId: String): Unit
}
