package controllers

import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import akka.channels._
import play.api.libs.concurrent.Akka
import actors._
import scala.concurrent.duration.Duration
import play.api.libs.json._

import models.Format._

object Application extends Controller {
  import play.api.Play.current
  implicit val timeout = akka.util.Timeout(Duration(5,"seconds"))

  val temp = ChannelExt(Akka.system).actorOf(new SoeCensusSupervisor(),"soe-census")

  def index = Action.async {
    (temp <-?- GetOnlineMembers).mapTo[OnlineMembers].map(a => Ok(Json.toJson(a.members)))
  }

}