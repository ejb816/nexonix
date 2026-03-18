
package generated.draco

import draco._
import java.util.function.Consumer
import org.evrete.KnowledgeService
import org.evrete.api.Knowledge
import org.evrete.api.RhsContext

trait Rule[T] extends RuleType {
  val pattern: Consumer[Knowledge]
  val action: Consumer[RhsContext]
}

object Rule extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Rule", _namePackage = Seq("draco"), _typeParameters = Seq("T")))
  lazy val typeInstance: Type[Rule[_]] = Type[Rule[_]] (typeDefinition)

  def apply[T] (
    _ruleDefinition: RuleDefinition = RuleDefinition.Null,
    _pattern: Consumer[Knowledge],
    _action: Consumer[RhsContext]
  ) : Rule[T] = new Rule[T] {
    override val ruleDefinition: RuleDefinition = _ruleDefinition
    override val pattern: Consumer[Knowledge] = _pattern
    override val action: Consumer[RhsContext] = _action
    override lazy val typeInstance: DracoType = Rule.typeInstance
    override lazy val typeDefinition: TypeDefinition = Rule.typeDefinition
  }

  lazy val Null: Rule[_] = apply[Nothing](
    _ruleDefinition = RuleDefinition.Null,
    _pattern = null.asInstanceOf[Consumer[Knowledge]],
    _action = null.asInstanceOf[Consumer[RhsContext]]
  )

  lazy val knowledgeService: KnowledgeService = new KnowledgeService ()
}
