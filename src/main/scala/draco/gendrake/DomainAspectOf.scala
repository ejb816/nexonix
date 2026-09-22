package draco.gendrake

import draco._
import draco.draketarget._
import io.circe.Json

trait DomainAspectOf extends DomainAspectText

object DomainAspectOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DomainAspectOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[DomainAspectOf] = Type[DomainAspectOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => TypeDefinition,
    _parameterText: => Json => String
  ) : DomainAspectOf = new DomainAspectOf {
    lazy val definition: TypeDefinition = _definition
    lazy val parameterText: Json => String = _parameterText
    lazy val reference: TypeName => String = name => (if (name.name.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", (name.namePackage ++ Seq(name.name ++ (if (name.typeParameters.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", "(" ++ name.typeParameters.map(parameterText).mkString(", ") ++ ")"))).mkString(" "))
    lazy val aspect: DomainAspect = definition.domainAspect
    lazy val rendered: DomainAspectText = DomainAspectText(reference(aspect.typeName), reference(definition.dracoAspect.superDomain), reference(aspect.source), reference(aspect.target), aspect.elementTypeNames, definition.typeName.name.isEmpty)
    override lazy val value: String = rendered.value
    override lazy val typeDefinition: TypeDefinition = DomainAspectOf.typeDefinition
  }

  lazy val Null: DomainAspectOf = apply(
    _definition = null.asInstanceOf[TypeDefinition],
    _parameterText = null.asInstanceOf[Json => String]
  )


}
