package base.models

import java.lang.reflect.{Field, Method}

import base.models.annotations.FieldMeta
import net.oltiv.scalaebean.ModelField
import play.api.libs.functional.FunctionalBuilder

import scala.util.Success
import play.api.libs.json._
import play.api.libs.functional.syntax._
import sun.management.snmp.AdaptorBootstrap.PropertyNames

import scala.reflect.ClassTag
import scala.util.Try

object ModelBaseScala {
  private def getReflectionField(mClass: Class[_], n: String):Option[Field] = Try(mClass.getField(n)).toOption.filter(f=>Option(f.getAnnotation(classOf[FieldMeta])).forall(_.serialize()))

  def writes[M <: ModelBase](m: M, propertyNames: Seq[ModelField[M]]=Nil):Writes[M] = {
    val mClass = m.getClass
    val propertyNamesToWrite = if(propertyNames.isEmpty) m.getPropertyNames.flatMap(p=>ModelField(m.getClass,p)).toSeq else propertyNames
    val res = new Writes[M]{
       override def writes(o: M): JsValue = {
          JsObject(
            propertyNamesToWrite.map(_.name.replaceAll("(.*)\\..*","$1")).distinct.flatMap{ n =>
              getReflectionField(mClass,n).flatMap { f =>
                val p = JsPath \ n
                val oo = Option(o.get(n))
                oo.collect {
                  case str: String => JsString(str.toString)
                  case m: ModelBase =>
                    ModelBaseScala.writes(m,propertyNamesToWrite.filter(_.name.startsWith(n+'.')).flatMap(p=>ModelField(m.getClass,p.name.replaceAll(s"^\\Q$n.\\E","")))).writes(m)
                  case l: java.lang.Long => JsNumber(BigDecimal(l))
                  case i: java.lang.Integer => JsNumber(BigDecimal(i))
                  case c: java.lang.Character => JsString(c.toString)
                  case b: java.lang.Boolean => JsBoolean(b)
                  case enum: Enum[_] => JsString(enum.toString)
                  case x @ _ => JsString(x.toString)
                }.map( (n,_))
              }
            }
          )
       }
    }
    res
  }
  def reads[M <: ModelBase](m: M, propertyNames: Seq[ModelField[M]]=Nil):Reads[M] = {
    val mClass = m.getClass
    val mRes = mClass.newInstance()
    val propertyNamesToRead = if(propertyNames.isEmpty) mRes.getPropertyNames.flatMap(p=>ModelField(m.getClass,p)).toSeq else propertyNames
    val res = new Reads[M]{
      override def reads(json: JsValue): JsResult[M] = {
        import play.api.libs.json.JsValue._
        propertyNamesToRead.map(_.name.replaceAll("(.*)\\..*","$1")).distinct.foreach{ n =>
          def mSet(o: Any) = mRes.set(n,o)
          getReflectionField(mClass,n).foreach{ f =>
            val t = f.getType

            val j = json match {
              case o: JsObject => Try(o.apply(n)).toOption
              case _ => None
            }
            j.foreach{ v =>
              t match {
                case str if str == classOf[String] => v match {
                  case JsString(vStr) =>
                    mSet(vStr)
                  case _ =>
                }
                case mm if classOf[ModelBase].isAssignableFrom(mm) => v match {
                  case JsObject(vObj) =>
                    val xnmI = mm.newInstance().asInstanceOf[ModelBase]
                    v.validate(
                      ModelBaseScala.reads(xnmI,propertyNamesToRead.filter(_.name.startsWith(n+'.')).flatMap(p=>ModelField(xnmI.getClass,p.name.replaceAll(s"^\\Q$n.\\E",""))))
                    ).map(mSet(_))
                  case _ =>
                }
                case l if l == classOf[java.lang.Long] => v match {
                  case JsNumber(d) =>
                    mSet(d.longValue())
                  case _ =>
                }
                case l if l == classOf[java.lang.Integer] => v match {
                  case JsNumber(d) => mSet(d.intValue())
                  case _ =>
                }
                case c if c == classOf[java.lang.Character] => v match {
                  case JsString(d) => mSet(d.charAt(0))
                  case _ =>
                }
                case c if c == classOf[java.lang.Boolean] => v match {
                  case JsBoolean(b) => mSet(b)
                  case _ =>
                }
                case c if c == classOf[Enum[_]] => v match {
                  case JsNumber(d) =>
                    val m2 = c.getDeclaredMethod("values")
                    val a = m2.invoke(null).asInstanceOf[Array[AnyRef]]
                    val vv = a(d.intValue())
                    mSet(vv)
                  case _ =>
                }
              }
            }
          }
        }
        JsSuccess(mRes)
      }
    }
    res
  }

  def writeToCompactJson[M<:ModelBase](items: Seq[M], fNames: Seq[ModelField[M]]):JsArray = {
    val data: Seq[Seq[AnyRef]] = items.map{ item =>
      fNames.map{ n =>val nn = n.name
        if(nn.contains('.')){
          var nnn = nn.split('.')
          var mi = item.get(nnn.head)
          nnn = nnn.tail
          while (!nnn.isEmpty && mi !=null && classOf[ModelBase].isAssignableFrom(mi.getClass)){
            mi match {
              case m: ModelBase =>
                mi = m.get(nnn.head)
                nnn = nnn.tail
              case _ =>
                nnn = Array()
            }
          }
          mi
        } else item.get(nn)
      }
    }

    val d: JsArray = JsArray(items.map{ item => JsArray(fNames.map { n =>
      val nn = n.name

      val (f,v) = if(nn.contains('.')){
        var nnn = nn.split('.')
        var mi = item.get(nnn.head)
        var ff = item.getClass.getField(nnn.head)
        nnn = nnn.tail
        while (!nnn.isEmpty){
          mi match {
            case null =>
              nnn = Array()
            case m: ModelBase =>
              mi = m.get(nnn.head)
              ff = m.getClass.getField(nnn.head)
              nnn = nnn.tail
            case _ =>
              nnn = Array()
          }
        }
        (ff, mi)
      } else {
        val ff = item.getClass.getField(nn)
        (ff,item.get(nn))
      }

      val res: JsValue = v match {
        case null => JsNull
        case str: String => JsString(str.toString)
        case l: java.lang.Long => JsNumber(BigDecimal(l))
        case i: java.lang.Integer => JsNumber(BigDecimal(i))
        case c: java.lang.Character => JsString(c.toString)
        case b: java.lang.Boolean => JsBoolean(b)
        case enum: Enum[_] => JsString(enum.toString)
        case x @ _ => JsString(x.toString)
      }

      res
    })})
    d
  }


  def getField(clazz: Class[_],name:String):Option[java.lang.reflect.Field] = {
    var nn = name.split('.')
    var cls = clazz
    var f:java.lang.reflect.Field = null
    while(nn.nonEmpty){
      val n = nn.head
      nn = nn.tail
      f = cls.getField(n)
      cls = f.getType
    }
    Option(f)
  }
}
