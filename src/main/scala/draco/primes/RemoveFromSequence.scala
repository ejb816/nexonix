package draco.primes

import draco.Rule
import org.evrete.api.{Knowledge, RhsContext}

trait RemoveFromSequence extends Rule

object RemoveFromSequence {

  val rule: Knowledge => Unit = knowledge => {
    knowledge
      .builder()
      .newRule("RemoveFromSequence")
      .forEach(
        "$prd", classOf[PrimesRuleData],
        "$i1", classOf[Integer],
        "$i2", classOf[Integer],
        "$i3", classOf[Integer]
      )
      .where("$i1 * $i2 == $i3")
      .where("$prd.textList.length > 0")
      .execute((context: RhsContext) => {
        val prd: PrimesRuleData = context.get[PrimesRuleData]("$prd")
        val i1: Integer = context.get[Integer]("$i1")
        val i2: Integer = context.get[Integer]("$i2")
        val i3: Integer = context.get[Integer]("$i3")
        val n: Integer = prd.textList.length
        context.delete(i3)
        context.delete(prd)
        val prefix: String = if (n > 1) "" else s"Remove non-primes after 2 up to $n from sequence:\n"
        val newTextList: Seq[String] = prd.textList ++ Seq(s"$prefix${prd.textList.length} -> $i1 * $i2 = $i3")
        context.insert(PrimesRuleData(prd.primes, newTextList))
      })
      .build()
  }
}
