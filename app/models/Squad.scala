package models

import play.api.libs.json.Json
import org.joda.time.DateTime

case class SquadType(name: String, roles: Array[String])

object SquadTypes {
  val STANDARD = SquadType("Standard",Array(
    Roles.HA,
    Roles.MEDIC,
    Roles.HA,
    Roles.MEDIC,
    Roles.ENGY,
    Roles.HA,
    Roles.INF,
    Roles.HA,
    Roles.MEDIC,
    Roles.HA,
    Roles.MEDIC,
    Roles.HA)
  )

  val SUPPORT = SquadType("Support",Array(
    Roles.MAX,
    Roles.ENGY,
    Roles.MAX,
    Roles.ENGY,
    Roles.MEDIC,
    Roles.MAX,
    Roles.HA,
    Roles.MEDIC,
    Roles.HA,
    Roles.ENGY,
    Roles.HA,
    Roles.INF)
  )

  val JETPACK = SquadType("Jetpack",Array(
    Roles.LA,
    Roles.LA,
    Roles.LA,
    Roles.LA,
    Roles.LA,
    Roles.LA,
    Roles.LA,
    Roles.LA,
    Roles.LA,
    Roles.LA,
    Roles.LA,
    Roles.LA)
  )
}

case class Assignment(
  name: String,
  given: DateTime)

case class Squad(
  stype: SquadType, 
  leader: MemberDetail,
  members: List[MemberDetail],
  assignments: Map[CharacterId,String]) {

  def place(new_member: MemberDetail) = {
    val updated_members = new_member :: members
    copy(members=updated_members,assignments=Squad.doAssignments(stype,updated_members))
  }

  def remove(cid: CharacterId) = {
    val updated_members = members.filter(_.id != cid)
    copy(members=updated_members,assignments=Squad.doAssignments(stype,updated_members))
  }
  
  def getRole(cid: CharacterId): Option[String] = assignments.get(cid)//assignments.find(_._1.id == cid).map(_._2)
}

object Squad {
  def make(stype: SquadType,leader: MemberDetail): Squad = {
    Squad(stype,leader,List(leader),doAssignments(stype,List(leader)))
  }

  def score(member: MemberDetail, role: String): Int = {
    member.preferences.get(role).getOrElse(0)
  }

  def doAssignments(stype: SquadType,input: List[MemberDetail]): Map[CharacterId,String] = {
    var unassigned = input
    stype.roles.take(input.size).foldLeft(Map[CharacterId,String]()) { case (acc,role) =>
      val ordered = unassigned.map(m => (score(m,role),m)).sortBy(_._1).reverse
      val (_,selected) = ordered.head
      unassigned = ordered.tail.map(_._2)
      acc + (selected.id->role)
    }
  }
}

object JSFormat {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  implicit val CharacterIdFormat = Json.format[CharacterId]
  implicit val MemberDetailFormat = Json.format[MemberDetail]
  implicit val TupleFormat = (__(0).format[MemberDetail] and __(1).format[String]).tupled
  implicit val SquadTypeFormat = Json.format[SquadType]
  //implicit val SquadFormat = Json.format[Squad]
}
