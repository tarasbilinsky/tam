package controllers

import javax.inject._

import play.api.mvc._
import services.Counter

@Singleton
class CountController @Inject() (cc: ControllerComponents,
                                 counter: Counter) extends AbstractController(cc) {

  def count = Action { Ok(counter.nextCount().toString) }

}
