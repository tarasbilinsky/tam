package base

import javax.inject._

import base.controllers.EnvironmentAll
import base.utils.Throttle
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.libs.mailer.Email
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import MyConfigImplicit.MyConfig
import base.mailer.Mailer

class ErrorHandler @Inject() (environment: Environment, mailerClient: Mailer, config: Configuration, sourceMapper: OptionalSourceMapper, router: Provider[Router]) extends DefaultHttpErrorHandler(environment, config, sourceMapper, router) {


  val logger = Logger(this.getClass)


  private def emailWithThrottle(throttle: Throttle)(email: => Email) = mailer.emailWithThrottle(mailerClient, throttle)(email)

  val emailServerErrorThrottle = new Throttle(config.errorMonitoring.throttle.e500, 1 hour)

  private def emailServerError(request: RequestHeader, exception: UsefulException) = {
    emailWithThrottle(emailServerErrorThrottle) {
      Email(
        s"${config.errorMonitoring.subject} 500 ${exception.id} ${config.hostIp}",
        config.errorMonitoring.from,
        Seq(config.errorMonitoring.to),
        bodyHtml = Some(
          s"""
              ${request.uri} <br>
              Cookies: ${utils.formatNlToBr(request.cookies.toString)} <br>
              Headers: ${utils.formatNlToBr(request.headers.toString)} <br>
              Exception: <small> ${exception.description} <br>
              ${utils.formatNlToBr(utils.printStackTrace(exception))}
              </small>
           """
        )
      )
    }
  }

  val emailClientErrorThrottle = new Throttle(config.errorMonitoring.throttle.e404, 1 hour)

  private def emailClientError(request: RequestHeader, statusCode: Int, message: String) = {
    emailWithThrottle(emailClientErrorThrottle) {
      Email(
        s"${config.errorMonitoring.subject} $statusCode ${config.hostIp}",
        config.errorMonitoring.from,
        Seq(config.errorMonitoring.to),
        bodyHtml = Some(s"${request.uri} <br> $message")
      )
    }
  }


  override def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    emailServerError(request, exception)
    super.onProdServerError(request, exception)

    //TODO 3 Custom Error Page
  }


  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    if(environment.mode == Mode.Prod) {
      emailClientError(request, statusCode, message)
      //TODO 3 Custom Error Page
    }
    super.onClientError(request,statusCode,message)
  }
}