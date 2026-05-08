
package draco.primes

import draco._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait AddNaturalSequenceRule extends Extensible

object AddNaturalSequenceRule extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadRuleType(TypeName ("AddNaturalSequence", _namePackage = Seq("draco", "primes")))

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

  lazy val ruleType: RuleType = Rule[AddNaturalSequenceRule] (
    typeDefinition,
    _pattern = pattern,
    _action = action
  )
}
