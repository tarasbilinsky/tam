package controllers

import java.util.Date
import javax.inject.Inject

import base.MyConfigImplicit._
import base.controllers.{ControllerBase, EnvironmentAll, Secure}
import base.utils.{Formatter, Throttle}
import models.feedback.Feedback
import models.{ModelPlaceholders => PH, _}
import net.oltiv.scalaebean.Shortcuts._
import org.joda.time.format.DateTimeFormat
import play.api.data.Forms._
import play.api.data._
import play.api.libs.mailer.Email
import play.api.mvc.{AnyContent, Request}
import play.twirl.api.Html
import views.html

import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

object LoginForm{
  val loginForm = Form(
    mapping
    ("userName" -> text, "password" -> text, "rememberMe" -> boolean)
    (
      (userName: String,password: String,rememberMe: Boolean) =>
        (
          query(PH.user,PH.user.name==userName && PH.user.password==password && PH.user.active, PH.user).one,
          rememberMe
        )
    )
    (_=>None)
      .verifying(
        "Invalid user name or password",
        _._1.isDefined
      )
  )
}

class Application @Inject() (implicit env: EnvironmentAll) extends ControllerBase with Secure[UserSession,User,UserRole,UserPermission] {
  import LoginForm._
  def login:EA = actionInsecure{ implicit request: Request[AnyContent] =>
    Ok(views.html.application.login(loginForm))
  }

  def logout:EA = actionInsecure{ implicit request: Request[AnyContent] =>
    getSession(request).foreach{_.close()}
    Redirect("login").withSession()
  }

  def auth:EA = actionInsecure{ implicit request: Request[AnyContent] =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.application.login(formWithErrors)),
      userAndRememberMe => {
        val (Some(user), _) = userAndRememberMe
        Redirect("/").withSession(initSession(user)) //TODO implement remember me
      }
    )
  }

  def authIntegration(token: String):EA = actionInsecure{ implicit request: Request[AnyContent] =>
    val user:Option[User] =  Option(UserSessionIntegration.find(token))
    user.fold(BadRequest("incorrect token")){ u =>
      Redirect("/").withSession(initSession(u)) //TODO implement remember me
    }
  }

  val emailJSErrorThrottle = new Throttle(env.config.errorMonitoring.throttle.js, 1 hour)

  val reportJsErrorForm = Form(
    tuple
    (
      "message" -> optional(text),
      "user" -> optional(text),
      "url" -> optional(text),
      "lineNumber" -> optional(text),
      "userAgent" -> optional(text),
      "referrer" -> optional(text)
    )
  )
  def reportJSError = actionInsecure{ implicit request: Request[AnyContent] =>
    reportJsErrorForm.bindFromRequest.fold(
      formWithErrors => {
        throw new RuntimeException(formWithErrors.errors.map(_.toString).mkString(";"))
      },
      info =>
        base.mailer.emailWithThrottle(env.mailerClient, emailJSErrorThrottle) {
          Email(
            s"${env.config.errorMonitoring.subject} JS ",
            env.config.errorMonitoring.from,
            Seq(env.config.errorMonitoring.to),
            bodyHtml = Some(
              s"""
              ${info._3.getOrElse("")} <br>
              Line: ${info._4.getOrElse("")} <br>
              ${info._1.getOrElse("")} <br>
              ${info._2.getOrElse("")} <br>
              User agent: ${info._5.getOrElse("")} <br>
              Referrer: ${info._6.getOrElse("")}
           """
            )
          )
        }
    )
    Ok
  }


  def feedback = actionInsecure{ implicit request: Request[AnyContent] =>
    val feedbackForm = Form(tuple(
      "url" -> text,
      "userId" -> optional(longNumber),
      "areaId"-> optional(longNumber),
      "type" -> optional(longNumber),
      "typeOther" -> text,
      "description" -> text,
      "timestamp" -> longNumber,
      "sessionId" -> optional(text)
    ))
    feedbackForm.bindFromRequest.fold(
      formWithErrors => {
        throw new RuntimeException(formWithErrors.errors.map(_.toString).mkString(";"))
      },
      f => {
        val (url,userIdOption,areaIdOption,typeOption,typeOther,description,timestamp,sessionIdOption):(String,Option[Long],Option[Long],Option[Long],String,String,Long,Option[String]) = f

        val user = userIdOption.flatMap(id =>query(PH.user, PH.user.id==id,PH.user.id,PH.user.name).one)
        val area = areaIdOption.flatMap(id =>query(PH.feedbackArea, PH.feedbackArea.id==id,PH.feedbackArea.id, PH.feedbackArea.title).one)
        val feedbackType = typeOption.flatMap(id =>query(PH.feedbackType, PH.feedbackType.id==id,PH.feedbackType.id,PH.feedbackType.title).one)
        val time = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").print(timestamp)
        base.mailer.emailWithThrottle(env.mailerClient, emailJSErrorThrottle) {
          Email(
            s"VIDOSEE feedback $timestamp",
            env.config.errorMonitoring.from,
            Seq(env.config.errorMonitoring.to),
            bodyHtml = Some(
              s"""
                 ${sessionIdOption.map(s=>s"<a href='https://tokbox.com/developer/tools/inspector/account/3128972/project/45648612/session/$s'>$s</a>").getOrElse("")} <br>
                 url~$url <br>
                 user-time~$time GMT <br>
                 user~${userIdOption.getOrElse(0)}~ ${user.map(_.name).getOrElse("no user")}<br>
                 area~${areaIdOption.getOrElse(0)}~ ${area.map(_.title).getOrElse("")}<br>
                 type~${typeOption.getOrElse(0)}~ ${feedbackType.map(_.title).getOrElse("")} $typeOther<br>
                 desc~ ${Html(Formatter.htmlFormatN2Br(description))}
               """
            )
          )
        }

        val feedback = new Feedback(new Date(timestamp), url, typeOther, description)
        user.foreach(feedback.user=_)
        area.foreach(feedback.area = _)
        sessionIdOption.foreach(feedback.sessionId = _)
        feedbackType.foreach(feedback.feedbackType = _)
        feedback.save()
      }
    )
    Ok
  }

  def version:EA = actionInsecure{ implicit request: Request[AnyContent] =>
    Ok(
      s"""
         |version = ${env.config.appVersion}
         |x-forwarded-server = ${request.headers.get("x-forwarded-server")}
         |java version = ${System.getProperty("java.version")},
         |OS =	${System.getProperty("os.name")} ${System.getProperty("os.version")}
       """.stripMargin
    )
  }

}
