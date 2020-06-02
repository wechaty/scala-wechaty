package wechaty.puppet.schemas

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object Contact {


  object ContactGender extends Enumeration{
    type Type = Value
    val ContactGenderUnknown: Value = Value(0)
    val ContactGenderMale: Value= Value(1)
    val ContactGenderFemale: Value = Value(2)
  }


  object ContactType extends Enumeration {
    type Type = Value
    val ContactTypeUnknown: Value = Value(0)
    val ContactTypePersonal: Value = Value(1)
    val ContactTypeOfficial: Value = Value(2)
  }


  class ContactPayload {
    var id: String = _
    var gender: ContactGender.Type = _
    var `type`: ContactType.Type = _
    var name: String = _
    var avatar: String = _
    var address: String = _
    var alias: String = _
    var city: String = _
    var friend: Boolean = _
    var province: String = _
    var signature: String = _
    var star: Boolean = _
    var weiXin: String = _
  }

}
