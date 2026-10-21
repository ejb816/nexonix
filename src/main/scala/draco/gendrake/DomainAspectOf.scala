package draco.gendrake

import draco.format.dracon.Dracon

import draco._
import draco.draketarget._

trait DomainAspectOf extends DomainAspectText

object DomainAspectOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DomainAspectOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[DomainAspectOf] = Type[DomainAspectOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => draco.TypeDefinition,
    _parameterText: => Dracon => String
  ) : DomainAspectOf = new DomainAspectOf {
    lazy val definition: draco.TypeDefinition = _definition
    lazy val parameterText: Dracon => String = _parameterText
    lazy val reference: draco.TypeName => String = name => (if (name.name.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", (name.namePackage ++ Seq(name.name ++ (if (name.typeParameters.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", "(" ++ name.typeParameters.map(parameterText).mkString(", ") ++ ")"))).mkString(" "))
    lazy val aspect: draco.DomainAspect = definition.domainAspect
    lazy val rendered: draco.draketarget.DomainAspectText = draco.draketarget.DomainAspectText(reference(aspect.typeName), reference(definition.dracoAspect.superDomain), reference(aspect.source), reference(aspect.target), aspect.elementTypeNames, definition.typeName.name.isEmpty)
    override lazy val value: String = rendered.value
    override lazy val typeDefinition: TypeDefinition = DomainAspectOf.typeDefinition
  }

  lazy val Null: DomainAspectOf = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition],
    _parameterText = null.asInstanceOf[Dracon => String]
  )


}
