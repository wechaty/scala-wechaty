package wechaty.puppet.schemas
object Contact {
object ContactGender extends Enumeration {
 type Type = Value
  val Unknown:Type =  Value(0)
  val Male:Type =  Value(1)
  val Female:Type =  Value(2)
}

/**
 * Huan(202004) TODO: Lock the ENUM number (like protobuf) ?
 */
object ContactType extends Enumeration {
 type Type = Value
  val Unknown:Type =  Value(0)
  val Individual:Type =  Value(1)
  val Official:Type =  Value(2)

  /**
   * Huan(202004):
   * @deprecated: use Individual instead
   */
  val Personal:Type =  Individual
}

class ContactQueryFilter {
  var alias:String = _
  var id:String = _
  var name:String = _
  var weixin:String = _
}

class ContactPayload {
  var id:String = _
  var gender:ContactGender.Type = _
  var `type`:ContactType.Type = _
  var name:String = _
  var avatar:String = _

  var address:String = _ //   // Huan(202001): what's this for?
  var alias:String = _
  var city:String = _
  var friend:Boolean = _
  var province:String = _
  var signature:String = _
  var star:Boolean = _
  var weixin:String = _
}

/** @hidden */
type ContactPayloadFilterFunction = ContactPayload => Boolean

/** @hidden */
type ContactPayloadFilterFactory = ContactQueryFilter => ContactPayloadFilterFunction
 } 
