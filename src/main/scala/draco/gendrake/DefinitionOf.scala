package draco.gendrake

import draco.format.dracon.Dracon

import draco._
import draco.draketarget._

trait DefinitionOf extends DefinitionText

object DefinitionOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DefinitionOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[DefinitionOf] = Type[DefinitionOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => draco.TypeDefinition,
    _parameterText: => Dracon => String,
    _formText: => Dracon => String,
    _slotText: => Dracon => String,
    _elementText: => (draco.TypeElement, Int) => Seq[String]
  ) : DefinitionOf = new DefinitionOf {
    lazy val definition: draco.TypeDefinition = _definition
    lazy val parameterText: Dracon => String = _parameterText
    lazy val formText: Dracon => String = _formText
    lazy val slotText: Dracon => String = _slotText
    lazy val elementText: (draco.TypeElement, Int) => Seq[String] = _elementText
    lazy val typeSection: DracoAspectOf = DracoAspectOf(definition, parameterText, formText, slotText, elementText)
    lazy val usesSection: UsesOf = UsesOf(definition)
    lazy val domainSection: DomainAspectOf = DomainAspectOf(definition, parameterText)
    lazy val ruleSection: RuleAspectOf = RuleAspectOf(definition, elementText)
    lazy val actorSection: ActorAspectOf = ActorAspectOf(definition, parameterText, elementText)
    lazy val codecSection: CodecAspectOf = CodecAspectOf(definition)
    lazy val rendered: draco.draketarget.DefinitionText = draco.draketarget.DefinitionText(typeSection.value, usesSection.value, domainSection.value, ruleSection.value, actorSection.value, codecSection.value)
    override lazy val value: String = rendered.value
    override lazy val typeDefinition: TypeDefinition = DefinitionOf.typeDefinition
  }

  lazy val Null: DefinitionOf = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition],
    _parameterText = null.asInstanceOf[Dracon => String],
    _formText = null.asInstanceOf[Dracon => String],
    _slotText = null.asInstanceOf[Dracon => String],
    _elementText = null.asInstanceOf[(draco.TypeElement, Int) => Seq[String]]
  )


}
