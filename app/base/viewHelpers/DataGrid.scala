package base.viewHelpers

import javax.swing.text.Highlighter.Highlight

import base.models.enums.FormatType
import base.models.{ModelBase, ModelBaseScala}
import net.oltiv.scalaebean.ModelField
import play.api.libs.json._
import play.twirl.api.Html

import scala.reflect.ClassTag
import scala.util.Try

case class FieldFormat(formatType:FormatType, formatParameter: String)

class RecordFields [+T <: ModelBase] (val fields:List[(ModelField[T],FieldFormat)])(implicit tag: ClassTag[T]){
  def this(field: ModelField[T])(implicit tag: ClassTag[T]) = this(
    List(
      (
        field,
        ModelBaseScala.getField(tag.runtimeClass,field.name).flatMap{
          f =>
            Option(f.getAnnotation(classOf[base.models.annotations.FieldFormat]))
        }.fold(
          FieldFormat(FormatType.Undefined,"")
        ) {f =>
            FieldFormat(f.value(),f.param())
        }
      )
    )
  )
}



class DataGridColumn [T <: ModelBase] (
                                        val name: String,
                                        var contentTemplate: Array[Html],
                                        var contendSelectorField: Option[ModelField[T]],
                                        var fields: RecordFields[T],
                                        var title: Option[String] = None,
                                        var styles: Seq[String] = Nil,
                                        var stylesTitle: Seq[String] = Nil,
                                        var sortable: Boolean = true,
                                        var sorted: Boolean = false,
                                        var sortedDesc: Boolean = false
                                      )(implicit tag: ClassTag[T]){
  def this(name:String, contentTemplateOne:Html, fields: RecordFields[T])(implicit tag: ClassTag[T]) = this(name,Array(contentTemplateOne),None,fields)

  def contentTemplate(html: Html):DataGridColumn[T] = {this.contentTemplate = Array(html); this}
  def stylesTitle(s:String):DataGridColumn[T] = {this.stylesTitle = s.split(' '); this}
  def stylesTitle(s:String*):DataGridColumn[T] = {this.stylesTitle = s; this}

  def styles(s:String):DataGridColumn[T] = {this.styles = s.split(' ').toList; this}

  def this(field: ModelField[T],contentTemplateOne:Html,name:String)(implicit tag: ClassTag[T]) = {
    this(if (name.isEmpty) field.name else name, Array(contentTemplateOne), None, new RecordFields[T](field))
  }

  def this(field: ModelField[T])(implicit tag: ClassTag[T]) = this(field,Html(""),"")


  /*def getContents(record: ModelBase):Html = {
    def getValueView(f: ModelField[T]) = BoundField(f).getValueView(record)
    val ct = contendSelectorField.fold(contentTemplate.headOption){mf: ModelField[T]=>
      record.get(mf) match {
        case i: Integer => Some(contentTemplate(i))
        case _ => None
      }
    }
    var res = ct.fold(fields.fields.headOption.fold("")(getValueView))(_.toString())

    val ff = fields.fields

    if((res.isEmpty || res=="$1$") && ff.nonEmpty) res = getValueView(ff.head)
    for(i <- ff.indices) res.replaceAll("\\Q$"+i+"$\\E",getValueView(ff(i)))

    Html(res)
  }*/

  def getTitle: String = title.getOrElse{
    fields.fields.map{ case (x,_) =>
      BoundField(x).getTitle
    }.mkString(" ")
  }

  def getStyles(m: T): Option[String] = if(styles.isEmpty && fields.fields.length==1){Option(BoundField(fields.fields.head._1).getAlign(m))map(_.toString.toLowerCase)}  else if(styles.isEmpty) None else Some(styles.mkString(" "))

  def getStyleTitle:Option[String] = if(stylesTitle.isEmpty) None else Some(stylesTitle.mkString(" "))

}

/*
class DataGridRow [T <: ModelBase] private [viewHelpers] (record: T, columns: Traversable[DataGridColumn[T]], highlight:Highlight = Highlight.none){
  def columnsContents:Traversable[Html] = columns.map(_.getContents(record))
}*/

case class GDataField(columnNumber:Int,formatType:FormatType, formatParameter: String){
  def this(columnNumber:Int,format: FieldFormat) = this(columnNumber,format.formatType,format.formatParameter)
}
case class GColumn[T <: ModelBase](name: String, title: String, fields: Array[GDataField], contentTemplate: Array[String], contendSelectorField: Option[Int], style: Option[String], styleTitle:Option[String]){
  def this(g: DataGridColumn[T],fNamesWithNumbers:Seq[(ModelField[T],Int)]) = this(
    g.name,
    g.getTitle,
    g.fields.fields.flatMap{f =>
      val fName = fNamesWithNumbers.find{case(n,_) => n.name==f._1.name}
      fName.map{case (_,i) => new GDataField(i,f._2)}
    }.toArray,
    g.contentTemplate.map(_.toString()),
    g.contendSelectorField.flatMap(f => fNamesWithNumbers.find{case(n,_) => n.name==f.name}.map(_._2)),
    if(g.styles.isEmpty) None else Some(g.styles.mkString(" ")),
    Some(g.stylesTitle.mkString(" ")+(if(g.sorted) " sorted sorted-"+(if(g.sortedDesc)"desc"else "asc") else "")+(if(g.sortable) " sortable" else ""))
  )
}

class DataGrid[T <: ModelBase](items: Seq[T],fNames: Seq[ModelField[T]], var columns: Seq[DataGridColumn[T]], idField:Option[ModelField[T]] = None, var highlightField:Option[DataGridColumn[T]] = None) {
  implicit val writesFormatType = new Writes[FormatType]{
    override def writes(o: FormatType): JsValue = JsString(o.toString)
  }
  implicit val writesFieldFormat: OWrites[FieldFormat] = Json.writes[FieldFormat]
  implicit val writesGDataField: OWrites[GDataField] = Json.writes[GDataField]
  implicit val writesGColumn: OWrites[GColumn[T]] = Json.writes[GColumn[T]]

  def dataJsonHtml:Html = Html(Json.stringify(ModelBaseScala.writeToCompactJson(items,fNames)))
  def columnsJsonHtml:Html = Html(Json.stringify(columnsJson))
  def highlightFieldJsonHtml:Html = Html(highlightFieldNumber.map(h=>Json.stringify(Json.toJson(h))).getOrElse("undefined"))
  def idColumnNumber:Int = idField.flatMap(f=>fNamesWithNumbers.find(_._1.name==f.name).map(_._2)).getOrElse(0)


  def get(column:ModelField[T]):Option[DataGridColumn[T]] = columns.find(_.name==column.name)
  def get(name:String):Option[DataGridColumn[T]] = columns.find(_.name==name)

  def update(mf: ModelField[T])(a: DataGridColumn[T]=>Unit):Unit = update(Seq(mf))(a)
  def update(mf: Seq[ModelField[T]])(a: DataGridColumn[T]=>Unit):Unit = for(f<-mf;g<-get(f)) a(g)
  def delete(mf: Seq[ModelField[T]]):Unit = columns = columns.filter{c => c.fields.fields.size==1 && !mf.exists(f => c.fields.fields.head._1.name==f.name)}
  def setHighlight(mf: Seq[ModelField[T]])(implicit tag: ClassTag[T]):Unit = highlightField = mf.headOption.map(new DataGridColumn(_))

  def add(name: String, title:String, contentTemplate:Html=Html(""), styles: Seq[String] = Nil, stylesTitle: Seq[String] = Nil)(implicit tag: ClassTag[T]):Unit = columns = columns :+ new DataGridColumn(name,Array(contentTemplate),None,new RecordFields[T](List()),Some(title),styles,stylesTitle,false)

  private val fNamesWithNumbers: Seq[(ModelField[T], Int)] = fNames.zipWithIndex
  private def columnsJson:JsValue = {
    val res = columns.map(new GColumn(_,fNamesWithNumbers)).toArray
    Json.toJson(res)
  }
  private def highlightFieldNumber: Option[GDataField] = highlightField.flatMap{ hf => hf.fields.fields.headOption.flatMap { f =>
    val fName = fNamesWithNumbers.find { case (n, _) => n.name == f._1.name }
    fName.map { case (_, i) => new GDataField(i, f._2) }
  }}
}

object DataGrid{
  def apply[T <: ModelBase](items: Seq[T],fNames: Seq[ModelField[T]], columns: Seq[DataGridColumn[T]], highlightField:Option[DataGridColumn[T]],idField:Option[ModelField[T]]):DataGrid[T] = new DataGrid[T](items,fNames,columns,idField,highlightField)
  def apply[T <: ModelBase](items: Seq[T],fNames: Seq[ModelField[T]], highlightField:Option[ModelField[T]]=None,idField:Option[ModelField[T]] = None)(implicit tag: ClassTag[T]): DataGrid[T] = new DataGrid[T](items,fNames,fNames.map(new DataGridColumn[T](_)),idField,highlightField.map(new DataGridColumn[T](_)))
}
