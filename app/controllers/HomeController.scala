package controllers

import javax.inject._

import base.controllers.{ControllerBase, EnvironmentAll}
import play.api.mvc._

@Singleton
class HomeController @Inject()(implicit env: EnvironmentAll) extends ControllerBase {

  def index = actionInsecure {implicit request =>
    Ok(views.html.indexAlt("xxx"))
  }

}
