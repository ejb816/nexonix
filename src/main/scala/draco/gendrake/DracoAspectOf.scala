package draco.gendrake

import draco.format.dracon.Dracon

import draco._
import draco.draketarget._

trait DracoAspectOf extends DracoAspectText

object DracoAspectOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DracoAspectOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[DracoAspectOf] = Type[DracoAspectOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => draco.TypeDefinition
  ) : DracoAspectOf = new DracoAspectOf {
    lazy val definition: draco.TypeDefinition = _definition
    lazy val elementText: (draco.TypeElement, Int) => Seq[String] = (element, level) => ElementOf(element, level).lines
    lazy val aspect: draco.DracoAspect = definition.dracoAspect
    lazy val typed: Dracon => String = form => TypeFormOf(form).value
    lazy val slot: Dracon => String = form => SlotOf(form).value
    lazy val appliedName: draco.TypeName => String = name => name.name ++ (if (name.typeParameters.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", "(" ++ name.typeParameters.map(typed).mkString(", ") ++ ")")
    lazy val reference: draco.TypeName => String = name => (if (name.namePackage == definition.typeName.namePackage) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(appliedName(name), (name.namePackage ++ Seq(appliedName(name))).mkString(" "))
    lazy val root: draco.TypeName => Boolean = name => name.name == "DracoType" && name.namePackage == Seq("draco")
    lazy val rootRestored: Boolean = draco.DracoAspect.parents(aspect).filterNot(root).isEmpty
    lazy val parents: Seq[String] = aspect.derivation.flatMap(parent => (if (parent.has("name")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(draco.TypeName.read(parent).fold(Seq.empty, name => (if (rootRestored && root(name)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(reference(name)))), Seq(typed(parent))))
    lazy val formalText: draco.TypeParameter => String = p => FormalOf(p).value
    lazy val name: String = (if (definition.typeName.name.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", definition.typeName.name ++ (if (aspect.typeParameters.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", "(" ++ aspect.typeParameters.map(formalText).mkString(", ") ++ ")"))
    lazy val extensible: String = (if (aspect.extensible.name.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", (aspect.extensible.namePackage ++ Seq(appliedName(aspect.extensible))).mkString(" "))
    lazy val factoryType: Dracon = (if (aspect.typeParameters.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Dracon.string(definition.typeName.name), Dracon.record(Seq(("()", Dracon.array(Seq(Dracon.string(definition.typeName.name)) ++ aspect.typeParameters.map(p => Dracon.string(p.name)))))))
    lazy val factoryPresent: Boolean = slot(aspect.factory.valueType).nonEmpty
    lazy val factoryResult: String = (if (factoryPresent) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse((if (slot(aspect.factory.valueType) == slot(factoryType)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", slot(aspect.factory.valueType)), "")
    lazy val rendered: draco.draketarget.DracoAspectText = draco.draketarget.DracoAspectText(name, parents, aspect.modules.map(reference), extensible, aspect.elements.flatMap(element => elementText(element, 2)), factoryPresent, factoryResult, aspect.factory.parameters.flatMap(element => elementText(element, 3)), aspect.factory.body.flatMap(element => elementText(element, 3)), aspect.globalElements.flatMap(element => elementText(element, 2)))
    override lazy val value: String = rendered.value
    override lazy val typeDefinition: TypeDefinition = DracoAspectOf.typeDefinition
  }

  lazy val Null: DracoAspectOf = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition]
  )


}
