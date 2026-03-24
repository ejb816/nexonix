
package draco.primes

import draco._
import io.circe.{Json, parser}
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait PrimesFromNaturalSequence extends RuleInstance

object PrimesFromNaturalSequence extends App with RuleInstance {
  private lazy val ruleDefinition: TypeDefinition = parser.parse("""{
  "typeName" : {
    "name" : "PrimesFromNaturalSequence",
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
      "name" : "i1",
      "valueType" : "Integer"
    },
    {
      "kind" : "Variable",
      "name" : "i2",
      "valueType" : "Integer"
    },
    {
      "kind" : "Variable",
      "name" : "i3",
      "valueType" : "Integer"
    }
  ],
  "conditions" : [
    {
      "kind" : "Condition",
      "valueType" : "Boolean",
      "value" : "i1 * i2 == i3",
      "parameters" : [
        {
          "kind" : "Parameter",
          "name" : "i1",
          "valueType" : "Integer"
        },
        {
          "kind" : "Parameter",
          "name" : "i2",
          "valueType" : "Integer"
        },
        {
          "kind" : "Parameter",
          "name" : "i3",
          "valueType" : "Integer"
        }
      ]
    }
  ],
  "action" : {
    "kind" : "Action",
    "name" : "ctx",
    "valueType" : "org.evrete.api.RHSContext => Unit",
    "body" : [
      {
        "kind" : "Fixed",
        "name" : "newText",
        "valueType" : "(Long, String)",
        "value" : "(System.nanoTime(), s\" Remove $i3 ->\\t$i3 == $i1 * $i2\")"
      },
      {
        "kind" : "Monadic",
        "valueType" : "Unit",
        "value" : "ctx.delete(i3)"
      },
      {
        "kind" : "Monadic",
        "valueType" : "Unit",
        "value" : "accumulator.primeSet.remove(i3)"
      },
      {
        "kind" : "Monadic",
        "valueType" : "Unit",
        "value" : "accumulator.compositeSet.addOne(i3)"
      },
      {
        "kind" : "Monadic",
        "valueType" : "Unit",
        "value" : "accumulator.intervalTextSet.addOne(newText)"
      }
    ]
  }
}""").flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)
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
    .where("draco.primes.PrimesFromNaturalSequence.w0($i1, $i2, $i3)")
    .execute (action)
    .build()
  }

  lazy val ruleInstance: RuleType = Rule[PrimesFromNaturalSequence] (
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

  lazy val typeInstance: DracoType = Type[PrimesFromNaturalSequence] (typeDefinition)
}
