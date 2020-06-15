package wechaty.helper

import java.util.function.Consumer

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.schemas.Event.{EventLoginPayload, EventLogoutPayload, EventMessagePayload, EventScanPayload}
import wechaty.user.{Contact, ContactSelf, Message}

import scala.language.implicitConversions

/**
  * convert event payload to user class
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-04
  */
object ImplicitHelper {
  private[wechaty] implicit def toEventScanPayload(eventScanListener: Consumer[EventScanPayload])(implicit puppet: PuppetResolver): EventScanPayload=> Unit = {
    eventScanPayload: EventScanPayload => { eventScanListener.accept(eventScanPayload) }
  }
  private[wechaty] implicit def toMessage(messageListener: Consumer[Message])(implicit puppet: PuppetResolver): EventMessagePayload => Unit = {
    messagePayload: EventMessagePayload => { messageListener.accept(new Message(messagePayload.messageId)) }
  }

  private [wechaty] implicit def toMessage(messageId: String)(implicit puppetResolver: PuppetResolver) = new Message(messageId)

  private[wechaty] implicit def toContactSelf(contactListener: Consumer[ContactSelf])(implicit puppet: PuppetResolver): EventLoginPayload => Unit = {
    payload: EventLoginPayload => { contactListener.accept(new ContactSelf(payload.contactId)) }
  }
  private[wechaty] implicit def toContact(contactListener: Consumer[Contact])(implicit puppet: PuppetResolver): EventLoginPayload => Unit = {
    payload: EventLoginPayload => { contactListener.accept(new Contact(payload.contactId)) }
  }

  private[wechaty] implicit def logoutToContact(contactListener: Consumer[Contact])(implicit puppet: PuppetResolver): EventLogoutPayload => Unit = {
    payload: EventLogoutPayload => { contactListener.accept(new Contact(payload.contactId)) }
  }

}
