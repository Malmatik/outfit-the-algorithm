package actors

import models._
import akka.actor._
import akka.channels._

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import fakedata.FakeSquadType

sealed trait AlgoRequest
//case object GetOnlineMembers extends AlgoRequest
case class SetOnlineStatus(cid: CharacterId, status: Boolean) extends AlgoRequest
case class LookupCharacterList(partial: String) extends AlgoRequest
case class JoinSquad(mem: MemberDetail) extends AlgoRequest
case object GetSquadData extends AlgoRequest
case class RoleChange(cid: CharacterId,role: String) extends AlgoRequest
case class CommandSocket(cid: CharacterId) extends AlgoRequest
case object WatTick extends AlgoRequest

sealed trait AlgoResult
case class OnlineMembers(members: List[Member]) extends AlgoResult
case class LookupCharacterListResponse(refs: List[CharacterRef]) extends AlgoResult
case class JoinSquadResult(success: Boolean) extends AlgoResult
case class SquadDataResult(squad: Option[Squad], online: List[CharacterId]) extends AlgoResult
case class CommandSocketResponse(iteratee: Iteratee[JsValue,_], enumeratee: Enumerator[JsValue]) extends AlgoResult

class TheAlgorithm extends Actor with Channels[TNil,(AlgoRequest,AlgoResult) :+: TNil] {

  implicit val timeout = akka.util.Timeout(Duration(5,"seconds"))

  var members = Set.empty[MemberId]
  val (algoEnumerator, algoChannel) = Concurrent.broadcast[JsValue]

  var soe_supervisor: Option[ChannelRef[
    (UpdateOnlineCharacter,Nothing) :+: (CensusRequest,CensusResult)  :+: TNil]] = None

  var member_supervisor: Option[ChannelRef[
    (MemberRequest,MemberResult) :+: TNil]] = None

  //Dater
  var squad_actor: Option[ChannelRef[
    (AlgoRequest,AlgoResult) :+: (SquadCommand,Nothing) :+: TNil]] = None

  override def preStart() = {
    soe_supervisor = Some(createChild(new SoeCensusSupervisor()))
    member_supervisor = Some(createChild(new MemberSupervisor()))
    squad_actor = Some(createChild(new SquadActor(selfChannel.narrow)))
    context.system.scheduler.scheduleOnce(Duration(5,"seconds"), self, WatTick)
  }

  channel[AlgoRequest] {

    case (LookupCharacterList(partial),snd) => {
      (soe_supervisor.get <-?- Lookup(partial)).map {
        case LookupResult(refs) => snd <-!- LookupCharacterListResponse(refs)
      }
    }

    case (JoinSquad(mem),snd) => {
      (squad_actor.get <-?- JoinSquad(mem)) -!-> snd
    }

    case (SetOnlineStatus(cid,status),snd) => (squad_actor.get <-!- SetActivity(cid,status))

    case (GetSquadData,snd) => {
      /*val online = tmp_squad.get.members.filter(m => tmp_online_status.get(m.id).getOrElse(false)).map(_.id)
      snd <-!- GetSquadDataResponse(tmp_squad,online)*/
      (squad_actor.get <-?- GetSquadData).map { snd <-!- _ }
    }

    case (RoleChange(cid,role),snd) => {
      algoChannel.push(Json.obj("role_change"->cid.id,"role"->role))
    }

    case (CommandSocket(mid),snd) => {
      val iteratee = Iteratee.foreach[JsValue] { event =>
        println(event)
        if(event == Json.obj("command"->"reset")) {
          (squad_actor.get <-!- ResetSquad)
          algoChannel.push(Json.obj("command"->"reset"))
        }
        if(event == Json.obj("change"->"change")) {
          /*
          import scala.util.Random
          tmp_squad = tmp_squad.map { old =>
            println("CHANGE!")
            val new_leader = Random.shuffle(old.members).head
            old.copy(leader=new_leader)
          }
          */
          (squad_actor.get <-!- RandomizeLeader)
        }
        algoChannel.push(Json.obj("foo"->"bar"))
      }.map { _ => println("AlgoSocket -- Quitting") }
      snd <-!- CommandSocketResponse(iteratee,algoEnumerator)
    }

    case (WatTick,snd) => {
      algoChannel.push(Json.obj("tick"->"tock"))
      context.system.scheduler.scheduleOnce(Duration(5,"seconds"),self,WatTick)
    }
  }
}
