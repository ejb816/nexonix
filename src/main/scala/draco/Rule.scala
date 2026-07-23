package draco

import java.util.function.Consumer
import org.evrete.KnowledgeService
import org.evrete.api.Knowledge
import org.evrete.api.RhsContext

trait Rule[T] extends RuleType {
  val pattern: Consumer[Knowledge]
  val action: Consumer[RhsContext]
}

object Rule extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Rule", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Rule[_]] = Type[Rule[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply[T] (
    _pattern: Consumer[Knowledge],
    _action: Consumer[RhsContext]
  ) : Rule[T] = new Rule[T] {
    override lazy val pattern: Consumer[Knowledge] = _pattern
    override lazy val action: Consumer[RhsContext] = _action
    override lazy val typeDefinition: TypeDefinition = Rule.typeDefinition
  }

  lazy val Null: Rule[_] = apply[Nothing](
    _pattern = null.asInstanceOf[Consumer[Knowledge]],
    _action = null.asInstanceOf[Consumer[RhsContext]]
  )

  lazy val knowledgeService: KnowledgeService = new KnowledgeService ()
}
