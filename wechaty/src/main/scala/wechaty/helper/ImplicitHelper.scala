package wechaty.helper

import wechaty.puppet.Puppet
import wechaty.puppet.schemas.Events.{EventLoginPayload, EventLogoutPayload, EventMessagePayload}
import wechaty.user.{Contact, Message}

import scala.language.implicitConversions

/**
  * convert event payload to user class
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-04
  */
object ImplicitHelper {
  private[wechaty] implicit def toMessage(messageListener: Message => Unit)(implicit puppet: Puppet): EventMessagePayload => Unit = {
    messagePayload: EventMessagePayload => messageListener(new Message(messagePayload.messageId))
  }

  private[wechaty] implicit def toContact(contactListener: Contact => Unit)(implicit puppet: Puppet): EventLoginPayload => Unit = {
    payload: EventLoginPayload => contactListener(new Contact(payload.contactId))
  }

  private[wechaty] implicit def logoutToContact(contactListener: Contact => Unit)(implicit puppet: Puppet): EventLogoutPayload => Unit = {
    payload: EventLogoutPayload => contactListener(new Contact(payload.contactId))
  }

}
