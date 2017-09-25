package base

import java.io.{PrintWriter, StringWriter}

import base.controllers.EnvironmentAll
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.{AmazonS3Client, AmazonS3ClientBuilder}
import org.apache.commons.lang3.StringEscapeUtils
import base.MyConfigImplicit.MyConfig
import com.amazonaws.regions.Regions

package object utils {
  def printStackTrace(e: Exception) = {
    val stack = new StringWriter()
    e.printStackTrace(new PrintWriter(stack))
    stack.toString
  }
  def formatNlToBr(s: String) = s.replace("\n","<br>")

  def formatJs(s: String) = StringEscapeUtils.escapeEcmaScript(s)

  def getS3Client(implicit env: EnvironmentAll) = AmazonS3ClientBuilder.standard.withRegion(Regions.fromName(env.config.aws.region)).withCredentials(new AWSStaticCredentialsProvider((new BasicAWSCredentials(env.config.aws.keyId, env.config.aws.key)))).build

  def sqlString(s: String) = s""" '${s.replace("'","\\'")}' """

  def withExceptionLogging[A](a: =>A)(implicit env: EnvironmentAll = null):A = {
    try{
      a
    } catch {
      case e:Throwable =>
        play.api.Logger.error("Error",e)
        throw e
    }
  }

  def emptyStringToNone(s: String):Option[String] = if(s.isEmpty) None else Some(s)

  def optionFromString(s: String):Option[String] = {
    if(s == null || s.trim.isEmpty) None else Some(s)
  }

  def nullStringToEmpty(s: String) = if(s==null) "" else s
}
