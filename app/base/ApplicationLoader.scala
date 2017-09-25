package base

import play.api.{ApplicationLoader => PlayAppLoader}
import play.api.Configuration
import play.api.inject._
import play.api.inject.guice._

class ApplicationLoader extends GuiceApplicationLoader() {
  override def builder(context: PlayAppLoader.Context): GuiceApplicationBuilder = {
    val extra = Configuration()//("a" -> 1)
    initialBuilder
      .disableCircularProxies()
      .in(context.environment)
      .loadConfig(extra ++ context.initialConfiguration)
      .overrides(overrides(context): _*)
  }
}