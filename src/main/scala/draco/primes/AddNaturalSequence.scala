
package draco.primes

import draco._
import io.circe.{Json, parser}
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait AddNaturalSequence extends RuleInstance

object AddNaturalSequence extends App with RuleInstance {
  private lazy val ruleDefinition: TypeDefinition = parser.parse("""{
  "typeName" : {
    "name" : "AddNaturalSequence",
    "namePackage" : [
      "draco",
      "primes"
    ]
  },
  "variables" : [
    {
      "kind" : "Variable",
      "name" : "accumulator",
      "valueType" : "Accumulator"
    },
    {
      "kind" : "Variable",
      "name" : "i",
      "valueType" : "Integer"
    }
  ],
  "action" : {
    "kind" : "Action",
    "name" : "ctx",
    "valueType" : "org.evrete.api.RHSContext => Unit",
    "body" : [
      {
        "kind" : "Monadic",
        "valueType" : "Unit",
        "value" : "accumulator.primeSet.addOne(i)"
      },
      {
        "kind" : "Monadic",
        "valueType" : "Unit",
        "value" : "accumulator.naturalSet.addOne(i)"
      },
      {
        "kind" : "Fixed",
        "name" : "text",
        "valueType" : "String",
        "value" : "s\"Added $i to primeSet and naturalSet.\""
      },
      {
        "kind" : "Monadic",
        "valueType" : "Unit",
        "value" : "accumulator.intervalTextSet.addOne((System.nanoTime(), text))"
      }
    ]
  }
}""").flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)

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
    .newRule ("draco.primes.AddNaturalSequence")
    .forEach (
      "$accumulator", classOf[Accumulator],
      "$i", classOf[Integer]
    )

    .execute (action)
    .build()
  }

  lazy val ruleInstance: RuleType = Rule[AddNaturalSequence] (
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

  lazy val typeInstance: DracoType = Type[AddNaturalSequence] (typeDefinition)
}
