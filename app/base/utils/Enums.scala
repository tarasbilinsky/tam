package base.utils

import java.lang.reflect.Method

import play.api.libs.json._

import scala.reflect.ClassTag

object Enums {

  def values(x: Class[_]) = {
    val valuesMethod: Method = x.getDeclaredMethod("values")
    val values: Array[Enum[_]] = valuesMethod.invoke(null).asInstanceOf[Array[Enum[_]]]
    values
  }

  def javaEnumFormat[E <: Enum[E] : ClassTag] = new Format[E] {
    override def reads(json: JsValue): JsResult[E] = json.validate[String] match {
      case JsSuccess(value, _) => try {
        val clazz = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
        JsSuccess(Enum.valueOf(clazz, value))
      } catch {
        case _: IllegalArgumentException => JsError("enumeration.unknown.value")
      }
      case JsError(_) => JsError("enumeration.expected.string")
    }

    override def writes(o: E): JsValue = JsString(o.toString)
  }

}
