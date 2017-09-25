package base.types


import java.util.{Currency, Locale}

import play.api.data.FormError

import scala.runtime.{RichDouble, ScalaNumberProxy}
import scala.util.{Success, Try}

final class Money(val self: Double) extends AnyVal with ScalaNumberProxy[Double]{
  override protected implicit def num: Numeric[Double] =  scala.math.Numeric.DoubleIsFractional
  override def isWhole(): Boolean = new RichDouble(self).isWhole()
  override protected def ord: Ordering[Double] = scala.math.Ordering.Double
  override def toString(): String = if(self.isNaN) "" else {
    val f = java.text.NumberFormat.getCurrencyInstance(new Locale("en","US"))
    f.format(self)
  }
}

object Money{
  def apply(d: java.lang.Double) = Option(d).fold(new Money(Double.NaN))(d=>new Money(d))
}

object Nulls{
  val nullDouble:java.lang.Double = null
}

object Formats {
  import play.api.data.format.Formats._
  import play.api.data.format.Formatter

  private def parsing[T](parse: String => T, errMsg: String, errArgs: Seq[Any])(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
    stringFormat.bind(key, data).right.flatMap { s =>
      scala.util.control.Exception.allCatch[T]
        .either(parse(s))
        .left.map(e => Seq(FormError(key, errMsg, errArgs)))
    }
  }

  /**
    * @throws java.lang.NumberFormatException  - If the string does not contain a parsable double.
    */
  private def parseMoneyInner(s: String):Money = new Money(s.replace('$',' ').trim().toDouble)

  def parseMoney(s: String):Option[Money] = Try(parseMoneyInner(s)) match {
    case Success(m) => Some(m)
    case _ => None
  }

  implicit def moneyFormat: Formatter[Money] =  {
    val (formatString, errorString) = ("format.numeric", "error.number")
    new Formatter[Money] {
      override val format = Some(formatString -> Nil)
      def bind(key: String, data: Map[String, String]):Either[Seq[FormError], Money] = parsing(parseMoneyInner, errorString, Nil)(key, data)
      def unbind(key: String, value: Money) = Map(key -> value.toString)
    }
  }
}