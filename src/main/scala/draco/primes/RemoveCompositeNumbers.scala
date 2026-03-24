
package draco.primes

import draco._
import io.circe.{Json, parser}
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait RemoveCompositeNumbers extends RuleInstance

object RemoveCompositeNumbers extends App with RuleInstance {
  private lazy val ruleDefinition: TypeDefinition = parser.parse("""{
  "typeName" : {
    "name" : "RemoveCompositeNumbers",
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
      "valueType" : "Int"
    },
    {
      "kind" : "Variable",
      "name" : "i2",
      "valueType" : "Int"
    },
    {
      "kind" : "Variable",
      "name" : "i3",
      "valueType" : "Int"
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
          "valueType" : "Int"
        },
        {
          "kind" : "Parameter",
          "name" : "i2",
          "valueType" : "Int"
        },
        {
          "kind" : "Parameter",
          "name" : "i3",
          "valueType" : "Int"
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
        "valueType" : "String",
        "value" : "s\"Composite number $i3 removed -> $i1 * $i2 == $i3\""
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
        "value" : "accumulator.intervalTextSet.addOne((System.nanoTime(), newText))"
      }
    ]
  }
}""").flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)
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
    .newRule ("draco.primes.RemoveCompositeNumbers")
    .forEach (
      "$accumulator", classOf[Accumulator],
      "$i1", classOf[Int],
      "$i2", classOf[Int],
      "$i3", classOf[Int]
    )
    .where("draco.primes.RemoveCompositeNumbers.w0($i1, $i2, $i3)")
    .execute (action)
    .build()
  }

  lazy val ruleInstance: RuleType = Rule[RemoveCompositeNumbers] (
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

  lazy val typeInstance: DracoType = Type[RemoveCompositeNumbers] (typeDefinition)
}
