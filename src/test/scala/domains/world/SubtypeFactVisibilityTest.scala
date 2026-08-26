package domains.world

import io.circe.Json
import org.evrete.KnowledgeService
import org.evrete.api.{ActivationMode, Knowledge, RhsContext}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

/** PROBE — disposable. Does the rete layer honour draco's inheritance?
 *
 *  `WorldHierarchyTest` establishes the type level: `FixReport <: Marine <: World`,
 *  and `FixReport <: JSON` besides. This asks the question one level down, which the
 *  corpus has never had to answer: when ONE fact is inserted, which declared types
 *  can see it?
 *
 *  Every rule in both corpora declares its fact at exactly one level — a rule for
 *  `PositionReport` receives `PositionReport` — so a fact has never needed to be
 *  visible at two. The design that motivates this asks for exactly that: an actor
 *  receives a message typed anywhere in the tree, and a rule matches it at whatever
 *  level the transform is written for. That only works if a fact is visible at every
 *  type it IS, not at one type chosen for it.
 *
 *  Four declared levels over a single inserted `FixReport`:
 *
 *    World      the domain root       — two levels up, via Marine
 *    Marine     its own domain        — one level up
 *    FixReport  its own type          — exact (via an anonymous implementing class)
 *    JSON       a second parent       — the format shell, not the domain chain
 *
 *  Read the headline, not a pass/fail — this measures rather than asserts, because
 *  the answer is not known in advance and each outcome means something different:
 *
 *    ALL FOUR   inheritance is honoured, including multiple parents. The design
 *               works as stated and nothing further is needed at this layer.
 *    EXACT ONLY facts are resolved to their runtime class alone. Every rule must
 *               then declare the concrete type, and "as high or low as required"
 *               needs a type resolver or an explicit insert per level.
 *    ONE OF     the resolver picks a single type per fact. Then WHICH one it picks
 *               is the whole design — first declared, most specific, or arbitrary —
 *               and a rule at another level silently never fires, which is the
 *               failure mode worth knowing about before building on it.
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

  test("which declared types see one inserted fact (report only)") {
    val service = new KnowledgeService()
    val fired   = ListBuffer.empty[String]
    try {
      val knowledge: Knowledge = service.newKnowledge("SubtypeVisibility")
      levels.foreach { case (label, cls) =>
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

    val saw     = fired.toList
    val missing = levels.map(_._1).filterNot(saw.contains)
    val verdict =
      if (missing.isEmpty)            "ALL FOUR — inheritance honoured, multiple parents included"
      else if (saw == List("FixReport")) "EXACT ONLY — a fact is visible at its own type alone"
      else                            s"PARTIAL — saw ${saw.mkString(", ")}; blind at ${missing.mkString(", ")}"

    log.info(s"inserted: one anonymous domains.marine.FixReport (FixReport <: Marine <: World, and <: JSON)")
    log.info(s"rules declared at: ${levels.map(_._1).mkString(", ")}")
    log.info(s"rules that fired : ${if (saw.isEmpty) "<none>" else saw.mkString(", ")}")
    log.info(s"verdict: $verdict")
    console.info(s"rete subtype visibility: $verdict")
    succeed
  }
}
