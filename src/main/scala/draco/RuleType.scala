package draco

import java.util.function.Consumer
import org.evrete.api.Knowledge
import org.evrete.api.RhsContext

trait RuleType extends DracoType {
  val action: Consumer[RhsContext]
  val pattern: Consumer[Knowledge]
}

object RuleType extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("RuleType", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[RuleType] = Type[RuleType] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
