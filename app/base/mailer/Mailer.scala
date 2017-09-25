package base.mailer

import javax.inject.{Inject, Singleton}

import base.controllers.EnvironmentAll
import play.api.inject.Module
import play.api.libs.mailer.{Email, MockMailer, SMTPConfiguration, SMTPMailer, MailerClient => PlayMailer}
import play.api.{Configuration, Environment}
import base.MyConfigImplicit._
import base.utils
import base.utils.Throttle

trait Mailer extends PlayMailer{
  def debugMessage(subject:String, s:String): String = {
    send(
      Email(
        subject = subject,
        from="tb@intteh.com",
        to=Seq("tarasbilinsky@gmail.com"),
        bodyText = Some(s))
    )
  }
  def reportErrorToEmail(subject: String, from:String, to:String, description:String , exception: Exception, throttle: Throttle) = {
    emailWithThrottle(this,throttle){
      Email(subject,from,Seq(to),Some(description+"\n"+utils.printStackTrace(exception)))
    }
  }
}

@Singleton
class MailerImpl @Inject()(mailerConfig: Configuration) extends Mailer{
  private val mock = mailerConfig.isMockMessaging
  private val testEmail = mailerConfig.appTestEmail
  private val mockInstance = new MockMailer()
  private val prodInstance: PlayMailer = {
     val path = "play.mailer."
    val smtpHost:String = mailerConfig.getOptional[String](path+"host").getOrElse(throw new RuntimeException("Mailer config error play.mailer.host is required"))
      val smtpPort = mailerConfig.getOptional[ Int](path+"port").getOrElse(25)
      val smtpSsl = mailerConfig.getOptional[Boolean](path+"ssl").getOrElse(false)
      val smtpTls = mailerConfig.getOptional[Boolean](path+"tls").getOrElse(false)
      val smtpUser = mailerConfig.getOptional[String](path+"user")
      val smtpPassword = mailerConfig.getOptional[String](path+"password")
      val debugMode = mailerConfig.getOptional[Boolean](path+"debug").getOrElse(false)
      val smtpTimeout = mailerConfig.getOptional[Int](path+"timeout")
      val smtpConnectionTimeout = mailerConfig.getOptional[Int](path+"connectiontimeout")

      val smtpAllConfig = new SMTPConfiguration(
        smtpHost,
        smtpPort,
        smtpSsl,
        smtpTls,
        false,
        smtpUser,
        smtpPassword,
        debugMode,
        smtpTimeout,
        smtpConnectionTimeout,
        false
      )
      new SMTPMailer(smtpAllConfig)

  }

  override def send(data: Email): String = (if(mock && !data.to.forall(_==testEmail))mockInstance else prodInstance).send(data)
}

class MailerModule extends Module {
  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[Mailer].to[MailerImpl]
  )
}

