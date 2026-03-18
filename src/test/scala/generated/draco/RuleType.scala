
package generated.draco

import draco._
import java.util.function.Consumer
import org.evrete.api.Knowledge
import org.evrete.api.RhsContext

trait RuleType extends DracoType {
  val ruleDefinition: RuleDefinition
}

object RuleType extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("RuleType", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[RuleType] = Type[RuleType] (typeDefinition)

  def apply (
    __ruleDefinition: RuleDefinition,
    __pattern: Consumer[Knowledge],
    __action: Consumer[RhsContext]
  ) : RuleType = new RuleType {
    override val _ruleDefinition: RuleDefinition = __ruleDefinition
    override val _pattern: Consumer[Knowledge] = __pattern
    override val _action: Consumer[RhsContext] = __action
    override lazy val typeInstance: DracoType = RuleType.typeInstance
    override lazy val typeDefinition: TypeDefinition = RuleType.typeDefinition
  }

  lazy val Null: RuleType = apply(
    __ruleDefinition = null.asInstanceOf[RuleDefinition],
    __pattern = null.asInstanceOf[Consumer[Knowledge]],
    __action = null.asInstanceOf[Consumer[RhsContext]]
  )


}
