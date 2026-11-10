package draco.draketarget

import draco._

trait DefinitionText extends Surface

object DefinitionText extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DefinitionText", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[DefinitionText] = Type[DefinitionText] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  def apply (
    _typeSection: => String = "",
    _usesSection: => String = "",
    _domainSection: => String = "",
    _ruleSection: => String = "",
    _actorSection: => String = "",
    _codecSection: => String = ""
  ) : DefinitionText = new DefinitionText {
    lazy val sections: Seq[String] = Seq(_typeSection, _usesSection, _domainSection, _ruleSection, _actorSection, _codecSection)
    override lazy val value: String = (sections.filter(section => section.nonEmpty)).mkString("\n") ++ "\n"
    override lazy val typeDefinition: TypeDefinition = DefinitionText.typeDefinition
  }

  lazy val Null: DefinitionText = apply()


}
