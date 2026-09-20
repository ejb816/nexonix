package draco

import org.evrete.KnowledgeService
import org.evrete.api.Knowledge
import org.evrete.api.RhsContext

trait Rule[T] extends RuleType {
  val pattern: Knowledge => Unit
  val action: RhsContext => Unit
}

object Rule extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Rule", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Rule[_]] = Type[Rule[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply[T] (
    _pattern: => Knowledge => Unit,
    _action: => RhsContext => Unit
  ) : Rule[T] = new Rule[T] {
    override lazy val pattern: Knowledge => Unit = _pattern
    override lazy val action: RhsContext => Unit = _action
    override lazy val typeDefinition: TypeDefinition = Rule.typeDefinition
  }

  lazy val Null: Rule[_] = apply[Nothing](
    _pattern = null.asInstanceOf[Knowledge => Unit],
    _action = null.asInstanceOf[RhsContext => Unit]
  )

  lazy val knowledgeService: KnowledgeService = new KnowledgeService ()
}
