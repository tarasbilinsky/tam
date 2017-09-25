package base

import play.api.Configuration

object MyConfigImplicit {

  implicit class MyConfig(c: Configuration) {

    val appName:String = c.get[String]("application.name")

    val appNodeId:Int = c.get[Int]("application.nodeId")

    val appDeployGroup:Int = c.get[Int]("application.deploy-group")

    val appUrl:String = c.get[String]("application.url")

    val appAssetsUrl:String = c.getOptional[String]("application.assetsUrl").getOrElse(appUrl+"/")

    val appUrlEmails:String = c.getOptional[String]("application.url-emails").getOrElse(appUrl)

    val appVersion:String = c.get[String]("application.autoversion")

    val appTempAssetsTimeoutSeconds:Int = c.get[Int]("application.temp-assets-timeout-seconds")

    val isStagingMode:Boolean = c.getOptional[Boolean]("application.staging").getOrElse(false)

    val isMockMessaging:Boolean = c.getOptional[Boolean]("play.mailer.mock").getOrElse(false)

    val appTestEmail:String = c.getOptional[String]("application.dev-test-email").getOrElse("")
    val appTestPhoneNumber:String = c.getOptional[String]("application.dev-test-phone").getOrElse("")

    val (hostIp, remotingOn) =
      c.getOptional[String]("akka.remote.netty.tcp.hostname")
        .filter(v => v!=null && !v.isEmpty && v!= "127.0.0.1" && v.compareToIgnoreCase("localhost")!=0)
        .map((_,c.getOptional[String]("akka.actor.provider").contains("akka.remote.RemoteActorRefProvider")))
        .getOrElse( ("127.0.0.1",false) )
    val hostPort:Int = c.getOptional[Int]( "akka.remote.netty.tcp.port").getOrElse(9001)

    val appTimeZone:String = c.getOptional[String]("application.timezone").getOrElse("PST")

    class EmailMonitoringConfigThrottle(val js: Int, val e500: Int, val e404: Int)
     val errorMonitoringThrottle: EmailMonitoringConfigThrottle = {
      val g = (s: String) => c.get[String]("errorMonitoring.throttle." + s).toInt
      new EmailMonitoringConfigThrottle(g("js"),g("e500"),g("e404"))
    }
    class ErrorMonitoringConfig(val subject: String, val from: String, val to: String, val throttle: EmailMonitoringConfigThrottle, val jsErrorMonitoringRoute: String)
     val errorMonitoring: ErrorMonitoringConfig = {
      val g = (s: String) => c.get[String]("errorMonitoring." + s)
      new ErrorMonitoringConfig(g("subject"), g("from"), g("to"),errorMonitoringThrottle,g("jsErrorMonitoringRoute"))
    }

    class AwsConfig(
                     val keyId: String,
                     val key: String,
                     val s3BucketMain: String,
                     val disableUploads: Boolean,
                      val assetsBucket: String,
                   val region: String
                     )
     val aws: AwsConfig = {
      val g = (s: String) => c.get[String]("aws." + s)
      new AwsConfig(
        g("access_key_id"),
        g("secret_access_key"),
        g("bucket.main"),
        c.get[Boolean]("aws.disableUploads"),
        g("bucket.assets"),
        g("region")
      )
    }


    class GoogleAnalytics(val number: String, val domain: String)
     val googleAnalytics: GoogleAnalytics = {
      val g = (s: String) => c.get[String]("googleAnalytics." + s)
      new GoogleAnalytics(
        g("number"),
        g("domain")
      )
    }


    class TwilioConf(val sid:String, val authKey: String, val phoneNumber: String, val messagingServiceSid: String)
    val twilio: TwilioConf = {
      val prefix = "twilio."
      new TwilioConf(
        c.get[String](prefix+"sid"),
        c.get[String](prefix+"authToken"),
        c.get[String](prefix+"phoneNumber"),
        c.get[String](prefix+"messagingServiceSid")
      )
    }
  }

}
