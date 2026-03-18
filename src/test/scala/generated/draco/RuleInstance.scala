
package generated.draco

import draco._

trait RuleInstance extends TypeInstance {
  val ruleInstance: RuleType
}

object RuleInstance extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("RuleInstance", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[RuleInstance] = Type[RuleInstance] (typeDefinition)
}
