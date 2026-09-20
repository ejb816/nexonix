package draco.primes

import draco._
import org.evrete.api.{Knowledge, RhsContext}

trait AddNaturalSequence

object AddNaturalSequence extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("AddNaturalSequence", _namePackage = Seq ("draco", "primes")))
  lazy val dracoType: Type[AddNaturalSequence] = Type[AddNaturalSequence] (typeDefinition)
  lazy val domainType: Domain[Primes] = Domain[Primes] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val accumulator: Accumulator = ctx.get[Accumulator]("$accumulator")
      val i: Integer = ctx.get[Integer]("$i")
      accumulator.primeSet.addOne(i)
      accumulator.naturalSet.addOne(i)
      lazy val text: String = s"Added $i to primeSet and naturalSet."
      accumulator.intervalTextSet.addOne((System.nanoTime(), text))
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.primes.AddNaturalSequence")
    .forEach (
      "$accumulator", classOf[Accumulator],
      "$i", classOf[Integer]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[AddNaturalSequence] (
    _pattern = pattern,
    _action = action
  )
}
