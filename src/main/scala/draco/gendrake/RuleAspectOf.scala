package draco.gendrake

import draco._
import draco.draketarget._

trait RuleAspectOf extends RuleAspectText

object RuleAspectOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("RuleAspectOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[RuleAspectOf] = Type[RuleAspectOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => draco.TypeDefinition
  ) : RuleAspectOf = new RuleAspectOf {
    lazy val definition: draco.TypeDefinition = _definition
    lazy val elementText: (draco.TypeElement, Int) => Seq[String] = (element, level) => ElementOf(element, level).lines
    lazy val present: Boolean = definition.ruleAspect.fold(false, aspect => true)
    lazy val aspect: draco.RuleAspect = definition.ruleAspect.fold(draco.RuleAspect.Null, aspect => aspect)
    lazy val rendered: draco.draketarget.RuleAspectText = draco.draketarget.RuleAspectText(aspect.pattern.variables.flatMap(variable => elementText(variable, 3)), aspect.pattern.conditions.flatMap(condition => elementText(condition, 3)), aspect.action.body.flatMap(element => elementText(element, 2)), present)
    override lazy val value: String = rendered.value
    override lazy val typeDefinition: TypeDefinition = RuleAspectOf.typeDefinition
  }

  lazy val Null: RuleAspectOf = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition]
  )


}
