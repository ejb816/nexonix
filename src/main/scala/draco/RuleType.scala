package draco

import org.evrete.api.Knowledge
import org.evrete.api.RhsContext

trait RuleType extends DracoType {
  val action: RhsContext => Unit
  val pattern: Knowledge => Unit
}

object RuleType extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("RuleType", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[RuleType] = Type[RuleType] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
