package wechaty.puppet.schemas

object Image {

  object ImageType extends Enumeration {
    type Type = Value
    val Unknown: Type = Value(0)
    val Thumbnail: Type = Value(1)
    val HD: Type = Value(2)
    val Artwork: Type = Value(3)
  }

}
