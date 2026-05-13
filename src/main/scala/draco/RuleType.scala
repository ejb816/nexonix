package draco

import java.util.function.Consumer
import org.evrete.api.Knowledge
import org.evrete.api.RhsContext

trait RuleType extends DracoType {
  val ruleDefinition: TypeDefinition
  val action: Consumer[RhsContext]
  val pattern: Consumer[Knowledge]
}

object RuleType extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("RuleType", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[RuleType] = Type[RuleType] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _ruleDefinition: TypeDefinition,
    _pattern: Consumer[Knowledge],
    _action: Consumer[RhsContext]
  ) : RuleType = new RuleType {
    override lazy val ruleDefinition: TypeDefinition = _ruleDefinition
    override lazy val typeDefinition: TypeDefinition = _ruleDefinition
    override lazy val action: Consumer[RhsContext] = _action
    override lazy val pattern: Consumer[Knowledge] = _pattern
  }

  lazy val Null: RuleType = apply(
    _ruleDefinition = null.asInstanceOf[TypeDefinition],
    _pattern = null.asInstanceOf[Consumer[Knowledge]],
    _action = null.asInstanceOf[Consumer[RhsContext]]
  )

}
