package wechaty.helper

import java.util.function.Consumer

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.schemas.Event.{EventFriendshipPayload, EventLoginPayload, EventLogoutPayload, EventMessagePayload, EventRoomInvitePayload, EventRoomJoinPayload, EventScanPayload}
import wechaty.user.{Contact, ContactSelf, Friendship, Message, Room, RoomInvitation}
import wechaty.puppet.schemas.Puppet._

import scala.concurrent.Future
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
  private[wechaty] implicit def toMessage(messageListener: Message => Unit)(implicit puppet: PuppetResolver): EventMessagePayload => Unit = {
    messagePayload: EventMessagePayload => { messageListener(new Message(messagePayload.messageId)) }
  }

  private [wechaty] implicit def toMessage(messageId: String)(implicit puppetResolver: PuppetResolver) = new Message(messageId)
  private [wechaty] implicit def toMessage(messageId: Future[String])(implicit puppetResolver: PuppetResolver):Future[Message] = messageId.map(toMessage)

  private[wechaty] implicit def toContactSelf(contactListener: Consumer[ContactSelf])(implicit puppet: PuppetResolver): EventLoginPayload => Unit = {
    payload: EventLoginPayload => { contactListener.accept(new ContactSelf(payload.contactId)) }
  }
  private[wechaty] implicit def toContact(contactListener: Consumer[Contact])(implicit puppet: PuppetResolver): EventLoginPayload => Unit = {
    payload: EventLoginPayload => { contactListener.accept(new Contact(payload.contactId)) }
  }
  private[wechaty] implicit def toFriendship(friendshipListener: Consumer[Friendship])(implicit puppet: PuppetResolver): EventFriendshipPayload => Unit = {
    payload: EventFriendshipPayload => { friendshipListener.accept(new Friendship(payload.friendshipId)) }
  }
  private[wechaty] implicit def toRoomJoinPayload(roomJoinListener: (Option[Room],Contact,Array[Contact])=>Unit)(implicit puppet: PuppetResolver): EventRoomJoinPayload => Unit = {
    payload: EventRoomJoinPayload => {
      val inviteeList = payload.inviteeIdList.map(new Contact(_))
      roomJoinListener(Room.load(payload.roomId),new Contact(payload.inviterId),inviteeList)
    }
  }

  private[wechaty] implicit def toInvite(roomInvitation: RoomInvitation =>Unit)(implicit puppet: PuppetResolver): EventRoomInvitePayload => Unit = {
    payload: EventRoomInvitePayload => {
      roomInvitation.apply(new RoomInvitation(payload.roomInvitationId))
    }
  }

  private[wechaty] implicit def logoutToContact(contactListener: Consumer[Contact])(implicit puppet: PuppetResolver): EventLogoutPayload => Unit = {
    payload: EventLogoutPayload => { contactListener.accept(new Contact(payload.contactId)) }
  }

}
