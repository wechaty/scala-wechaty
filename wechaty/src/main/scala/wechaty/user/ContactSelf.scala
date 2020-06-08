package wechaty.user

import wechaty.Wechaty.PuppetResolver

/**
  * 
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-08
  */
class ContactSelf(contactId:String)(implicit resolver:PuppetResolver) extends Contact(contactId){
}
