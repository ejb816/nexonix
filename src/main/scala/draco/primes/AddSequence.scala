package draco.primes

import org.evrete.api.{Knowledge, RhsContext}


object AddSequence {
  val rule: Knowledge => Unit = knowledge => {
    knowledge
      .builder()
      .newRule ("AddSequence")
      .forEach (
        "$prd", classOf[PrimesRuleData],
        "$start", classOf[Integer]
      )
      .where("$prd.textList.length == 0")
      .execute ((context: RhsContext) => {
        val prd: PrimesRuleData = context.get[PrimesRuleData]("$prd")
        val start: Integer = context.get[Integer]("$start")
        val text: Seq[String] = Seq(s"Add Sequence 2 to ${prd.primes.naturalSequence.length + 1}\n")
        context.delete(start)
        context.getRuntime.insert(prd.primes.naturalSequence: _*)
        PrimesRuleData(prd.primes, text)
      })
      .build()
  }
}
