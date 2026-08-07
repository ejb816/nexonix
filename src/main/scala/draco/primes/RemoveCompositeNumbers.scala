package draco.primes

import draco._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait RemoveCompositeNumbers

object RemoveCompositeNumbers extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("RemoveCompositeNumbers", _namePackage = Seq ("draco", "primes")))
  lazy val dracoType: Type[RemoveCompositeNumbers] = Type[RemoveCompositeNumbers] (typeDefinition)
  lazy val domainType: Domain[Primes] = Domain[Primes] (typeDefinition)
  def w0(i1: Integer, i2: Integer, i3: Integer): Boolean = i1 * i2 == i3
  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val accumulator: Accumulator = ctx.get[Accumulator]("$accumulator")
      val i1: Integer = ctx.get[Integer]("$i1")
      val i2: Integer = ctx.get[Integer]("$i2")
      val i3: Integer = ctx.get[Integer]("$i3")
      val newText: String = s"Composite number $i3 removed -> $i1 * $i2 == $i3"
      ctx.delete(i3)
      accumulator.primeSet.remove(i3)
      accumulator.compositeSet.addOne(i3)
      accumulator.intervalTextSet.addOne((System.nanoTime(), newText))
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.primes.RemoveCompositeNumbers")
    .forEach (
      "$accumulator", classOf[Accumulator],
      "$i1", classOf[Integer],
      "$i2", classOf[Integer],
      "$i3", classOf[Integer]
    )
    .where("draco.primes.RemoveCompositeNumbers.w0($i1, $i2, $i3)")
    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[RemoveCompositeNumbers] (
    _pattern = pattern,
    _action = action
  )
}
