package draco.domain.primes

import draco.Value
import draco.domain.DomainRule
import io.circe.Json
import org.evrete.api.Knowledge


trait AddSequence extends DomainRule

object AddSequence {

  val rule: Knowledge => Unit = knowledge => {
    knowledge
      .builder()
      .newRule("AddSequence")
      .forEach("$fp", classOf[Json])


      .execute((context: org.evrete.api.RhsContext) => {
        val fp: Json = context.get[Json]("$fp")
        val delta: Value = Value("delta", fp, Seq("delta"))
        val maximum: Value = Value("maximum", fp, Seq("maximum"))
        for (i <- 2 to maximum.value[Int]) {
          context.insert(i)
        }
        context.insert(FindPrimes(maximum.value[Int], delta.value[Int], 0, 0))
      })
      .build()
  }
}
