
package draco.primes

import draco._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait PrimesFromNaturalSequenceRule extends RuleInstance

object PrimesFromNaturalSequenceRule extends App with RuleInstance {
  private lazy val ruleDefinition: TypeDefinition = draco.Generator.loadRuleType(TypeName ("PrimesFromNaturalSequence", _namePackage = Seq("draco", "primes")))
  def w0(i1: Integer, i2: Integer, i3: Integer): Boolean = i1 * i2 == i3
  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val accumulator: Accumulator = ctx.get[Accumulator]("$accumulator")
      val i1: Integer = ctx.get[Integer]("$i1")
      val i2: Integer = ctx.get[Integer]("$i2")
      val i3: Integer = ctx.get[Integer]("$i3")
      val newText: (Long, String) = (System.nanoTime(), s" Remove $i3 ->\t$i3 == $i1 * $i2")
      ctx.delete(i3)
      accumulator.primeSet.remove(i3)
      accumulator.compositeSet.addOne(i3)
      accumulator.intervalTextSet.addOne(newText)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.primes.PrimesFromNaturalSequence")
    .forEach (
      "$accumulator", classOf[Accumulator],
      "$i1", classOf[Integer],
      "$i2", classOf[Integer],
      "$i3", classOf[Integer]
    )
    .where("draco.primes.PrimesFromNaturalSequenceRule.w0($i1, $i2, $i3)")
    .execute (action)
    .build()
  }

  lazy val ruleInstance: RuleType = Rule[PrimesFromNaturalSequenceRule] (
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

  lazy val typeInstance: DracoType = Type[PrimesFromNaturalSequenceRule] (typeDefinition)
}
