package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait TypeName extends DracoType {
  val name: String
  val namePackage: Seq[String]
  val typeParameters: Seq[draco.format.dracon.Dracon]
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
    _typeParameters: => Seq[draco.format.dracon.Dracon] = Seq.empty
  ) : TypeName = new TypeName {
    override lazy val name: String = _name
    override lazy val namePackage: Seq[String] = _namePackage
    override lazy val typeParameters: Seq[draco.format.dracon.Dracon] = _typeParameters
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

  lazy val read: draco.format.dracon.Dracon => draco.drake.Presence[TypeName] = source => (if (source.isObject) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(source.field("name").textOrEmpty.fold(draco.drake.Absent(), name => source.field("namePackage").textsOrEmpty.fold(draco.drake.Absent(), parts => source.field("typeParameters").itemsOrEmpty.fold(draco.drake.Absent(), arguments => draco.drake.Present(TypeName(name, parts, arguments))))), draco.drake.Absent())
  lazy val write: TypeName => draco.format.dracon.Dracon = name => draco.format.dracon.Dracon.record((if (name.name.nonEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq(("name", draco.format.dracon.Dracon.string(name.name))), Seq.empty) ++ (if (name.namePackage.nonEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq(("namePackage", draco.format.dracon.Dracon.array(name.namePackage.map(draco.format.dracon.Dracon.string)))), Seq.empty) ++ (if (name.typeParameters.nonEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq(("typeParameters", draco.format.dracon.Dracon.array(name.typeParameters))), Seq.empty))
}
