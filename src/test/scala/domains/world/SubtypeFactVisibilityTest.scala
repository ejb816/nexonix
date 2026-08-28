package domains.world

import io.circe.Json
import org.evrete.KnowledgeService
import org.evrete.api.{ActivationMode, Knowledge, RhsContext}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

/** PROBE — disposable. Does the rete layer honour draco's inheritance?
 *
 *  `WorldHierarchyTest` establishes the type level: `FixReport <: Marine <: World`, and
 *  `FixReport <: JSON` besides. This asks the question one level down: when ONE fact is
 *  inserted, which declared types can see it?
 *
 *  Every rule in both corpora declares its fact at exactly one level, so the corpus has
 *  never had to answer. The design that motivates the question needs a message to be
 *  matchable at whatever level a transform is written for.
 *
 *  TWO MEASUREMENTS, because the first run of this probe returned something none of the
 *  anticipated outcomes covered — with four rules declared at four levels of one
 *  hierarchy, NOTHING fired, not even the rule at the fact's own type. That is either
 *  ambiguity (several declared types fit one fact, so the resolver picks none) or a
 *  defect in the probe, and the two are told apart by running each level ALONE:
 *
 *    SOLO      one knowledge, one rule, one insert — per level.
 *              Establishes the baseline: can a rule at this level EVER see this fact?
 *    COMBINED  one knowledge, all four rules, one insert.
 *              The design question proper.
 *
 *  Reading the matrix:
 *    solo all four + combined all four  inheritance honoured; declare rules anywhere.
 *    solo all four + combined none      CO-DECLARATION is the poison, not the hierarchy.
 *                                       Each level works alone; declaring two levels of
 *                                       one hierarchy in one knowledge silently drops the
 *                                       fact. Worst case for implicit matching, and the
 *                                       strongest argument for dispatching in the actor.
 *    solo FixReport only                no inheritance at all; the exact type is required.
 *    solo none                          the probe is wrong, not the engine — start there.
 */
class SubtypeFactVisibilityTest extends AnyFunSuite with draco.PersistentTestLog {

  /** One `FixReport`, as WorldHierarchyTest builds them: an anonymous implementing
   *  class, so the runtime class is not even the declared trait. */
  private def fixReport: domains.marine.FixReport = new domains.marine.FixReport {
    override lazy val typeDefinition: draco.TypeDefinition = domains.marine.FixReport.typeDefinition
    override val json: Json = Json.obj("probe" -> Json.fromString("fix"))
  }

  private val levels: Seq[(String, Class[_])] = Seq(
    "World"     -> classOf[World],
    "Marine"    -> classOf[domains.marine.Marine],
    "FixReport" -> classOf[domains.marine.FixReport],
    "JSON"      -> classOf[draco.format.json.JSON]
  )

  /** Insert one FixReport into a session whose knowledge declares exactly `declared`,
   *  and return the labels that fired. */
  private def run(declared: Seq[(String, Class[_])]): Seq[String] = {
    val service = new KnowledgeService()
    val fired   = ListBuffer.empty[String]
    try {
      val knowledge: Knowledge = service.newKnowledge("Probe")
      declared.foreach { case (label, cls) =>
        knowledge
          .builder()
          .newRule(s"probe.$label")
          .forEach("$f", cls)
          .execute((_: RhsContext) => fired += label)
          .build()
      }
      val session = knowledge.newStatefulSession(ActivationMode.CONTINUOUS)
      try {
        session.insert(Seq(fixReport): _*)
        session.fire()
      } finally session.close()
    } finally service.shutdown()
    fired.toList
  }

  test("which declared types see one inserted fact (report only)") {
    val solo = levels.map { case (label, cls) =>
      val fired = try run(Seq(label -> cls)) catch { case e: Throwable => Seq(s"<error: ${e.getClass.getSimpleName}>") }
      label -> fired
    }
    val combined = try run(levels) catch { case e: Throwable => Seq(s"<error: ${e.getMessage}>") }

    log.info("inserted: one anonymous domains.marine.FixReport (FixReport <: Marine <: World, and <: JSON)")
    log.info("SOLO — one knowledge, one rule:")
    solo.foreach { case (label, fired) =>
      log.info(f"    declared at $label%-10s -> ${if (fired.contains(label)) "FIRED" else s"silent ($fired)"}")
    }
    log.info(s"COMBINED — all four declared together -> ${if (combined.isEmpty) "<none fired>" else combined.mkString(", ")}")

    val soloSeen = solo.collect { case (l, f) if f.contains(l) => l }
    val verdict =
      if (soloSeen.size == levels.size && combined.size == levels.size)
        "inheritance honoured — a fact is visible at every type it is"
      else if (soloSeen.size == levels.size && combined.isEmpty)
        "CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing"
      else if (soloSeen == List("FixReport"))
        "no inheritance — only the fact's own type sees it"
      else if (soloSeen.isEmpty)
        "nothing fires even solo — suspect the probe before the engine"
      else
        s"mixed — solo: ${soloSeen.mkString(",")}; combined: ${combined.mkString(",")}"

    log.info(s"verdict: $verdict")
    console.info(s"rete subtype visibility: $verdict")
    succeed
  }
}
