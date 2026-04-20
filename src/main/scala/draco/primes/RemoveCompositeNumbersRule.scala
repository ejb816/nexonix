
package draco.primes

import draco._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait RemoveCompositeNumbersRule extends RuleInstance

object RemoveCompositeNumbersRule extends App with RuleInstance {
  private lazy val ruleDefinition: TypeDefinition = draco.Generator.loadRuleType(TypeName ("RemoveCompositeNumbers", _namePackage = Seq("draco", "primes")))
  def w0(i1: Int, i2: Int, i3: Int): Boolean = i1 * i2 == i3
  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val accumulator: Accumulator = ctx.get[Accumulator]("$accumulator")
      val i1: Int = ctx.get[Int]("$i1")
      val i2: Int = ctx.get[Int]("$i2")
      val i3: Int = ctx.get[Int]("$i3")
      val newText: String = s"Composite number $i3 removed -> $i1 * $i2 == $i3"
      ctx.delete(i3)
      accumulator.primeSet.remove(i3)
      accumulator.compositeSet.addOne(i3)
      accumulator.intervalTextSet.addOne((System.nanoTime(), newText))
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.primes.RemoveCompositeNumbers.rule")
    .forEach (
      "$accumulator", classOf[Accumulator],
      "$i1", classOf[Int],
      "$i2", classOf[Int],
      "$i3", classOf[Int]
    )
    .where("draco.primes.RemoveCompositeNumbersRule.w0($i1, $i2, $i3)")
    .execute (action)
    .build()
  }

  lazy val ruleInstance: RuleType = Rule[RemoveCompositeNumbersRule] (
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

  lazy val typeInstance: DracoType = Type[RemoveCompositeNumbersRule] (typeDefinition)
}
