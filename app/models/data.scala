package models

import play.api.libs.json._

object Roles {
  val HA = "Heavy Assault"
  val MEDIC = "Medic"
  val ENGY = "Engineer"
  val LA = "Light Assault"
  val INF = "Infiltrator"
  val MAX = "MAX"
}

object Fireteams {
  val ONE = "Fireteam One"
  val TWO = "Fireteam Two"
  val THREE = "Fireteam Three"
  val DRIVER = "Driver Team"
  val GUNNER = "Gunner Team"
}

object Special {
  val POINT = "Pointman"
}

//case class MemberId(id: String)

case class CharacterId(id: String)


case class CharacterRef(
  cid: CharacterId,
  name: String
)


case class Resources(
  infantry: Int,
  armor: Int,
  air: Int
)
/*
case class Member (
  id: MemberId,
  name: String
)
*/

case class PreferenceData(
  cid:String,
  name:String,
  leader:String,
  point:String,
  ha:Int,
  medic:Int,
  engy:Int,
  la:Int,
  inf:Int,
  magrider:Int,
  harasser:Int,
  sunderer:Int,
  lightning:Int,
  galaxy:Int,
  scythe:Int,
  liberator:Int
)

case class MemberDetail(
  id: CharacterId,
  name: String,
  leader: String,
  point: String,
  prefs: Map[String,Int]
) {
  override def hashCode = id.hashCode() + 17

  override def equals(other: Any) = {
    other match {
      case m: MemberDetail => m.id == id
      case _ => false
    }
  }
}

object Format {
  implicit val FormatCharId = Json.format[CharacterId]
  implicit val FormatCharRef = Json.format[CharacterRef]
  //implicit val FormatMemberId = Json.format[MemberId]
  //implicit val FormatMember = Json.format[Member]
  implicit val FormatResources = Json.format[Resources]
  implicit val prefFormat = Json.format[PreferenceData]
}
