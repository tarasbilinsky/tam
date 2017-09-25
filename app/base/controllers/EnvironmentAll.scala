package base.controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.Materializer
import base.mailer.Mailer
import models.Node
import play.Logger
import play.api.cache.SyncCacheApi
import play.api.http.FileMimeTypes
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import play.api.mvc.ControllerComponents
import play.api.{Configuration, Environment, Mode}
import play.cache.NamedCache

@Singleton
class EnvironmentAll @Inject() (
  val env: Environment,
  val config: Configuration,
  val mailerClient: Mailer,
  val lifecycle: ApplicationLifecycle,
  val akka: ActorSystem,
  val wsClient: WSClient,
  val materializer: Materializer,
  val mimeTypes: FileMimeTypes,
  val controllerComponents: ControllerComponents,
  @NamedCache("user-cache") val userCache: SyncCacheApi
){
  val isDevMode: Boolean = env.mode == Mode.Dev
  import base.MyConfigImplicit._
  val isStagingMode: Boolean = config.isStagingMode
}