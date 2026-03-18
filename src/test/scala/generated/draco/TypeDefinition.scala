
package generated.draco

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait TypeDefinition  {
  val typeName: TypeName
  val modules: Seq[TypeName]
  val derivation: Seq[TypeName]
  val elements: Seq[TypeElement]
  val factory: Factory
  val globalElements: Seq[BodyElement]
}

object TypeDefinition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("TypeDefinition", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[TypeDefinition] = Type[TypeDefinition] (typeDefinition)

  implicit lazy val encoder: Encoder[TypeDefinition] = Encoder.instance { x =>
    val fields = Seq(
      Some("typeName" -> x.typeName.asJson),
      if (x.modules.nonEmpty) Some("modules" -> x.modules.asJson) else None,
      if (x.derivation.nonEmpty) Some("derivation" -> x.derivation.asJson) else None,
      if (x.elements.nonEmpty) Some("elements" -> x.elements.asJson) else None,
      if (x.factory.valueType.nonEmpty) Some("factory" -> x.factory.asJson) else None,
      if (x.globalElements.nonEmpty) Some("globalElements" -> x.globalElements.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName <- cursor.downField("typeName").as[TypeName]
      _modules <- cursor.downField("modules").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _derivation <- cursor.downField("derivation").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _elements <- cursor.downField("elements").as[Option[Seq[TypeElement]]].map(_.getOrElse(Seq.empty))
      _factory <- cursor.downField("factory").as[Option[Factory]].map(_.getOrElse(Factory.Null))
      _globalElements <- cursor.downField("globalElements").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
    } yield TypeDefinition (_typeName, _modules, _derivation, _elements, _factory, _globalElements)
  }

  def apply (
    _typeName: TypeName,
    _modules: Seq[TypeName] = Seq.empty,
    _derivation: Seq[TypeName] = Seq.empty,
    _elements: Seq[TypeElement] = Seq.empty,
    _factory: Factory = Factory.Null,
    _globalElements: Seq[BodyElement] = Seq.empty
  ) : TypeDefinition = new TypeDefinition {
    override val typeName: TypeName = _typeName
    override val modules: Seq[TypeName] = _modules
    override val derivation: Seq[TypeName] = _derivation
    override val elements: Seq[TypeElement] = _elements
    override val factory: Factory = _factory
    override val globalElements: Seq[BodyElement] = _globalElements
    override lazy val typeInstance: DracoType = TypeDefinition.typeInstance
    override lazy val typeDefinition: TypeDefinition = TypeDefinition.typeDefinition
  }

  lazy val Null: TypeDefinition = apply(
    _typeName = null.asInstanceOf[TypeName],
    _modules = Seq.empty,
    _derivation = Seq.empty,
    _elements = Seq.empty,
    _factory = Factory.Null,
    _globalElements = Seq.empty
  )


}
