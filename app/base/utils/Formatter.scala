package base.utils

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import base.controllers.EnvironmentAll
import base.MyConfigImplicit._
import org.apache.commons.lang3.StringEscapeUtils
import play.twirl.api.{Html, JavaScript}

object Formatter {
  def date(d: Date, format: String = "MM/dd/yyyy hh:mm:ss aa")(implicit env: EnvironmentAll):String = {
    Option(d).fold{
      ""
    } { date =>
      val f = new SimpleDateFormat(format)
      f.setTimeZone(TimeZone.getTimeZone(env.config.appTimeZone))
      f.format(date)
    }
  }

  def dateShort(d: Date)(implicit env:EnvironmentAll):String = date(d,"MM/dd/YY hh:mm aa")

  def date(dateAsLong: Long)(implicit env: EnvironmentAll):String = date(new Date(dateAsLong))(env)

  def time(x: Long):String = {
    Option(x).fold{
      ""
    } { x =>
      val hours = x / 3600
      val secondsLeft = x % 3660
      val minutes = secondsLeft / 60
      val seconds = secondsLeft % 60
      s"$hours:$minutes:$seconds"
    }
  }

  def jsTemplate(x: Html):Html = Html(JavaScript(x.toString.replace('\n',' ')).toString)

  def htmlFormatN2Br(s: String) = Option(s).fold(""){ s=>
    StringEscapeUtils.escapeHtml4(s).replace("'", "&#39;").replaceAll("\\n", "<br/>")
  }

  def replaceInHtmlTemplate(s: String, token: String, value: String): String = s.replace("$" + token + "$", htmlFormatN2Br(value))

  def conditionalReplaceInHtmlTemplate(s: String, token: String, value: String): String = {
    if (Option(value).forall(!_.isEmpty))
      s.replaceAll("(?s)\\Q$" + token + "${\\E.*?\\Q}\\E", "")
    else
      s.replaceAll("(?s)\\Q$" + token + "${\\E(.*?)\\Q}\\E", "$1").replace("$" + token + "$", htmlFormatN2Br(value))
  }

}



