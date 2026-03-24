package draco

import org.evrete.KnowledgeService
import org.evrete.api.{Knowledge, RhsContext}

import java.util.function.Consumer

trait Rule[T] extends RuleType

object Rule extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Rule",
      _namePackage = Seq ("draco"),
      _typeParameters = Seq ("T")
    ),
    _elements = Seq (
      Fixed ("pattern", "Consumer[Knowledge]"),
      Fixed ("action", "Consumer[RhsContext]")
    )
  )
  lazy val typeInstance: Type[Rule[_]] = Type[Rule[_]] (typeDefinition)

  lazy val knowledgeService: KnowledgeService = new KnowledgeService ()
  def apply[T] (
      _ruleDefinition: TypeDefinition = TypeDefinition.Null,
      _pattern: Consumer[Knowledge],
      _action: Consumer[RhsContext]
    ) : Rule[T] = new Rule[T] {
    override val ruleDefinition: TypeDefinition = _ruleDefinition
    override val typeDefinition: TypeDefinition = _ruleDefinition
    override val action: Consumer[RhsContext] = _action
    override val pattern: Consumer[Knowledge] = _pattern
  }
}