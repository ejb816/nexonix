
package org.nexonix.rules.rete.rules

import draco._
import org.nexonix.rules.rete._
import org.nexonix.rules._
import org.nexonix._
import org._

trait TupleFact

object TupleFact extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("TupleFact", _namePackage = Seq ("org", "nexonix", "rules", "rete", "rules")))
  lazy val dracoType: Type[TupleFact] = Type[TupleFact] (typeDefinition)
}
