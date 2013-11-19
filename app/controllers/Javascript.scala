package controllers

import play.api.mvc._
import play.api.Routes

object JavascriptRoutes extends Controller {
  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(Application.confirmCharacter)
    ).as("text/javascript")
  }
}