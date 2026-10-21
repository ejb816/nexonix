package draco

import draco.drake.Absent
import draco.drake.Presence
import draco.drake.Present
import draco.format.dracon.Dracon

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait TypeName extends DracoType {
  val name: String
  val namePackage: Seq[String]
  val typeParameters: Seq[Dracon]
  val namePath: String
  val resourcePath: String
}

object TypeName extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeName", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeName] = Type[TypeName] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[TypeName] = Encoder.instance { x =>
    val fields = Seq(
      Some("name" -> x.name.asJson),
      if (x.namePackage.nonEmpty) Some("namePackage" -> x.namePackage.asJson) else None,
      if (x.typeParameters.nonEmpty) Some("typeParameters" -> Json.fromValues(x.typeParameters.map(draco.scalatarget.CirceCodecs.encodePayload.apply))) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[TypeName] = Decoder.instance { cursor =>
    for {
      _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
      _namePackage <- cursor.downField("namePackage").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _typeParameters <- cursor.downField("typeParameters").as[Option[Seq[Json]]].map(_.getOrElse(Seq.empty)).flatMap(value => io.circe.Decoder.decodeSeq(draco.scalatarget.CirceCodecs.decodePayload).decodeJson(value.asJson))
    } yield TypeName (_name, _namePackage, _typeParameters)
  }

  def apply (
    _name: => String,
    _namePackage: => Seq[String] = Seq.empty,
    _typeParameters: => Seq[Dracon] = Seq.empty
  ) : TypeName = new TypeName {
    override lazy val name: String = _name
    override lazy val namePackage: Seq[String] = _namePackage
    override lazy val typeParameters: Seq[Dracon] = _typeParameters
    override lazy val namePath: String = (namePackage ++ Seq(name)).mkString(".")
    override lazy val resourcePath: String = "/" ++ (namePackage ++ Seq(name ++ ".json")).mkString("/")
    override def equals(other: Any): Boolean = {
      other match {
        case tn: TypeName =>
          name == tn.name && namePackage == tn.namePackage && typeParameters == tn.typeParameters
        case _ =>
          false
      }
    }
    override def hashCode: Int = (name, namePackage, typeParameters).hashCode
    override lazy val typeDefinition: TypeDefinition = TypeName.typeDefinition
  }

  lazy val Null: TypeName = apply(
    _name = "",
    _namePackage = Seq.empty,
    _typeParameters = Seq.empty
  )

  lazy val read: Dracon => Presence[TypeName] = source => (if (source.isObject) Present[Unit](()) else Absent[Unit]()).ifThenElse(source.field("name").textOrEmpty.fold(Absent(), name => source.field("namePackage").textsOrEmpty.fold(Absent(), parts => source.field("typeParameters").itemsOrEmpty.fold(Absent(), arguments => Present(TypeName(name, parts, arguments))))), Absent())
  lazy val write: TypeName => Dracon = name => Dracon.record((if (name.name.nonEmpty) Present[Unit](()) else Absent[Unit]()).ifThenElse(Seq(("name", Dracon.string(name.name))), Seq.empty) ++ (if (name.namePackage.nonEmpty) Present[Unit](()) else Absent[Unit]()).ifThenElse(Seq(("namePackage", Dracon.array(name.namePackage.map(Dracon.string)))), Seq.empty) ++ (if (name.typeParameters.nonEmpty) Present[Unit](()) else Absent[Unit]()).ifThenElse(Seq(("typeParameters", Dracon.array(name.typeParameters))), Seq.empty))
}
