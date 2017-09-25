package base.controllers

import javax.inject.Inject

import play.api.http.FileMimeTypes
import play.api.libs.json.Json
import play.api.mvc._

abstract class ControllerBase (implicit val env: EnvironmentAll) extends AbstractController(env.controllerComponents){
  type EA = EssentialAction
  implicit val mimeTypes: FileMimeTypes = env.mimeTypes
  val actionInsecure: ActionBuilder[Request, AnyContent] = controllerComponents.actionBuilder
  val emptyJson = Json.obj()
}
