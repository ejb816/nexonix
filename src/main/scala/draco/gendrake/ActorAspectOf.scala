package draco.gendrake

import draco.format.dracon.Dracon

import draco._
import draco.draketarget._

trait ActorAspectOf extends ActorAspectText

object ActorAspectOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ActorAspectOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[ActorAspectOf] = Type[ActorAspectOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => draco.TypeDefinition,
    _parameterText: => Dracon => String,
    _elementText: => (draco.TypeElement, Int) => Seq[String]
  ) : ActorAspectOf = new ActorAspectOf {
    lazy val definition: draco.TypeDefinition = _definition
    lazy val parameterText: Dracon => String = _parameterText
    lazy val elementText: (draco.TypeElement, Int) => Seq[String] = _elementText
    lazy val present: Boolean = definition.actorAspect.fold(false, aspect => true)
    lazy val aspect: draco.ActorAspect = definition.actorAspect.fold(draco.ActorAspect.Null, aspect => aspect)
    lazy val appliedName: draco.TypeName => String = name => name.name ++ (if (name.typeParameters.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", "(" ++ name.typeParameters.map(parameterText).mkString(", ") ++ ")")
    lazy val reference: draco.TypeName => String = name => (if (name.name.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", (if (name.namePackage == definition.typeName.namePackage) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(appliedName(name), (name.namePackage ++ Seq(appliedName(name))).mkString(" ")))
    lazy val rendered: draco.draketarget.ActorAspectText = draco.draketarget.ActorAspectText(reference(aspect.messageType), aspect.start.body.flatMap(element => elementText(element, 2)), aspect.message.body.flatMap(element => elementText(element, 2)), aspect.signal.body.flatMap(element => elementText(element, 2)), present)
    override lazy val value: String = rendered.value
    override lazy val typeDefinition: TypeDefinition = ActorAspectOf.typeDefinition
  }

  lazy val Null: ActorAspectOf = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition],
    _parameterText = null.asInstanceOf[Dracon => String],
    _elementText = null.asInstanceOf[(draco.TypeElement, Int) => Seq[String]]
  )


}
