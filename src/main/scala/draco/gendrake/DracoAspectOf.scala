package draco.gendrake

import draco._
import draco.draketarget._

trait DracoAspectOf extends DracoAspectText

object DracoAspectOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DracoAspectOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[DracoAspectOf] = Type[DracoAspectOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => TypeDefinition,
    _parameterText: => draco.format.dracon.Dracon => String,
    _formText: => draco.format.dracon.Dracon => String,
    _slotText: => draco.format.dracon.Dracon => String,
    _elementText: => (TypeElement, Int) => Seq[String]
  ) : DracoAspectOf = new DracoAspectOf {
    lazy val definition: TypeDefinition = _definition
    lazy val parameterText: draco.format.dracon.Dracon => String = _parameterText
    lazy val formText: draco.format.dracon.Dracon => String = _formText
    lazy val slotText: draco.format.dracon.Dracon => String = _slotText
    lazy val elementText: (TypeElement, Int) => Seq[String] = _elementText
    lazy val aspect: DracoAspect = definition.dracoAspect
    lazy val appliedName: TypeName => String = name => name.name ++ (if (name.typeParameters.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", "(" ++ name.typeParameters.map(parameterText).mkString(", ") ++ ")")
    lazy val reference: TypeName => String = name => (if (name.namePackage == definition.typeName.namePackage) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(appliedName(name), (name.namePackage ++ Seq(appliedName(name))).mkString(" "))
    lazy val root: TypeName => Boolean = name => name.name == "DracoType" && name.namePackage == Seq("draco")
    lazy val rootRestored: Boolean = DracoAspect.parents(aspect).filterNot(root).isEmpty
    lazy val parents: Seq[String] = aspect.derivation.flatMap(parent => (if (parent.has("name")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(TypeName.read(parent).fold(Seq.empty, name => (if (rootRestored && root(name)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(reference(name)))), Seq(formText(parent))))
    lazy val name: String = (if (definition.typeName.name.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", appliedName(definition.typeName))
    lazy val extensible: String = (if (aspect.extensible.name.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", (aspect.extensible.namePackage ++ Seq(appliedName(aspect.extensible))).mkString(" "))
    lazy val factoryType: draco.format.dracon.Dracon = (if (definition.typeName.typeParameters.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(draco.format.dracon.Dracon.string(definition.typeName.name), draco.format.dracon.Dracon.record(Seq(("()", draco.format.dracon.Dracon.array(Seq(draco.format.dracon.Dracon.string(definition.typeName.name)) ++ definition.typeName.typeParameters)))))
    lazy val factoryPresent: Boolean = slotText(aspect.factory.valueType).nonEmpty
    lazy val factoryResult: String = (if (factoryPresent) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse((if (slotText(aspect.factory.valueType) == slotText(factoryType)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", slotText(aspect.factory.valueType)), "")
    lazy val rendered: DracoAspectText = DracoAspectText(name, parents, aspect.modules.map(reference), extensible, aspect.elements.flatMap(element => elementText(element, 2)), factoryPresent, factoryResult, aspect.factory.parameters.flatMap(element => elementText(element, 3)), aspect.factory.body.flatMap(element => elementText(element, 3)), aspect.globalElements.flatMap(element => elementText(element, 2)))
    override lazy val value: String = rendered.value
    override lazy val typeDefinition: TypeDefinition = DracoAspectOf.typeDefinition
  }

  lazy val Null: DracoAspectOf = apply(
    _definition = null.asInstanceOf[TypeDefinition],
    _parameterText = null.asInstanceOf[draco.format.dracon.Dracon => String],
    _formText = null.asInstanceOf[draco.format.dracon.Dracon => String],
    _slotText = null.asInstanceOf[draco.format.dracon.Dracon => String],
    _elementText = null.asInstanceOf[(TypeElement, Int) => Seq[String]]
  )


}
