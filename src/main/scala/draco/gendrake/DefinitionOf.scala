package draco.gendrake

import draco._
import draco.draketarget._

trait DefinitionOf extends DefinitionText

object DefinitionOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DefinitionOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[DefinitionOf] = Type[DefinitionOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => draco.TypeDefinition
  ) : DefinitionOf = new DefinitionOf {
    lazy val definition: draco.TypeDefinition = _definition
    lazy val typeSection: DracoAspectOf = DracoAspectOf(definition)
    lazy val usesSection: UsesOf = UsesOf(definition)
    lazy val domainSection: DomainAspectOf = DomainAspectOf(definition)
    lazy val ruleSection: RuleAspectOf = RuleAspectOf(definition)
    lazy val actorSection: ActorAspectOf = ActorAspectOf(definition)
    lazy val codecSection: CodecAspectOf = CodecAspectOf(definition)
    lazy val rendered: draco.draketarget.DefinitionText = draco.draketarget.DefinitionText(typeSection.value, usesSection.value, domainSection.value, ruleSection.value, actorSection.value, codecSection.value)
    override lazy val value: String = rendered.value
    override lazy val typeDefinition: TypeDefinition = DefinitionOf.typeDefinition
  }

  lazy val Null: DefinitionOf = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition]
  )


}
