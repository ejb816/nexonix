package draco.format.dracon

import draco.drake.Presence

import draco.format._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Dracon extends Format[Dracon] {
  val json: Json
  lazy val value: Dracon = this
  def isNull: Boolean = json.isNull
  def isObject: Boolean = json.isObject
  def textOrEmpty: Presence[String] = ((if (json.isNull) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Some(""), json.asString) match { case Some(v) => draco.drake.Present(v); case None => draco.drake.Absent() })
  def itemsOrEmpty: Presence[Seq[Dracon]] = ((if (json.isNull) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Some(Seq.empty[Dracon]), json.asArray.map(_.map(Dracon(_)).toSeq)) match { case Some(v) => draco.drake.Present(v); case None => draco.drake.Absent() })
  def textsOrEmpty: Presence[Seq[String]] = ((if (json.isNull) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Some(Seq.empty[String]), json.asArray.flatMap(values => if (values.forall(_.isString)) Some(values.flatMap(_.asString).toSeq) else None)) match { case Some(v) => draco.drake.Present(v); case None => draco.drake.Absent() })
  def has(_name: => String): Boolean = {
    lazy val name: String = _name
    json.asObject.exists(_.contains(name))
  }
  def field(_name: => String): Dracon = {
    lazy val name: String = _name
    select(Seq(name)).fold(Dracon.Null, value => value)
  }
  def select(_pathElements: => Seq[String]): Presence[Dracon] = {
    lazy val pathElements: Seq[String] = _pathElements
    lazy val selected: Option[Json] = pathElements.foldLeft(Option(json))((current, key) => current.flatMap(node => (if (node.isArray) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(key.toIntOption.filter(index => index >= 0).flatMap(index => node.hcursor.downN(index).focus), node.hcursor.downField(key).focus)))
    (selected.map(node => Dracon(node)) match { case Some(v) => draco.drake.Present(v); case None => draco.drake.Absent() })
  }
}

object Dracon extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Dracon", _namePackage = Seq ("draco", "format", "dracon")))
  lazy val dracoType: Type[Dracon] = Type[Dracon] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Decoder", "Encoder", "Value")

  implicit lazy val encoder: Encoder[Dracon] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Dracon] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield Dracon (_json)
  }

  lazy val domainType: Domain[Dracon] = Domain[Dracon] (typeDefinition)
  def apply (
    _json: => Json
  ) : Dracon = new Dracon {
    override lazy val json: Json = _json
    override def equals(other: Any): Boolean = {
      other match {
        case value: Dracon =>
          json == value.json
        case _ =>
          false
      }
    }
    override def hashCode: Int = json.hashCode
    override lazy val typeDefinition: TypeDefinition = Dracon.typeDefinition
  }

  lazy val Null: Dracon = apply(
    _json = Json.Null
  )

  def string(text: String): Dracon = Dracon(Json.fromString(text))
  def array(_values: => Seq[Dracon]): Dracon = {
    lazy val values: Seq[Dracon] = _values
    Dracon(Json.fromValues(values.map(_.json)))
  }
  def record(_fields: => Seq[(String, Dracon)]): Dracon = {
    lazy val fields: Seq[(String, Dracon)] = _fields
    Dracon(Json.obj(fields.map { case (name, value) => name -> value.json }: _*))
  }
}
