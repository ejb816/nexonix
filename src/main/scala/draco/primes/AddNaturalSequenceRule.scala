
package draco.primes

import draco._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait AddNaturalSequence.ruleRule extends RuleInstance

object AddNaturalSequence.ruleRule extends App with RuleInstance {
  private lazy val ruleDefinition: TypeDefinition = draco.Generator.loadRuleType(TypeName ("AddNaturalSequence.rule", _namePackage = Seq("draco", "primes")))

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val accumulator: Accumulator = ctx.get[Accumulator]("$accumulator")
      val i: Integer = ctx.get[Integer]("$i")
      accumulator.primeSet.addOne(i)
      accumulator.naturalSet.addOne(i)
      val text: String = s"Added $i to primeSet and naturalSet."
      accumulator.intervalTextSet.addOne((System.nanoTime(), text))
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.primes.AddNaturalSequence.rule")
    .forEach (
      "$accumulator", classOf[Accumulator],
      "$i", classOf[Integer]
    )

    .execute (action)
    .build()
  }

  lazy val ruleInstance: RuleType = Rule[AddNaturalSequence.ruleRule] (
    ruleDefinition,
    _pattern = pattern,
    _action = action
  )

  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = ruleDefinition.typeName,
    _derivation = Seq (
      RuleInstance.typeInstance.typeDefinition.typeName
    )
  )

  lazy val typeInstance: DracoType = Type[AddNaturalSequence.ruleRule] (typeDefinition)
}
