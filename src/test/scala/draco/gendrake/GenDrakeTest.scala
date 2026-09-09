package draco.gendrake

import draco._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** GenDrake RUNS: the first generator transform fired as rules, against the corpus.
 *
 *  `GenDrake` is the transform `Draco -> DrakeTarget`. Its member `Emit` is a rule —
 *  a `TypeDefinition` in working memory becomes an `Emission`, a `Surface` — and the
 *  super-domain `generator.Generator` owns `SurfaceReceived`, the rule that gives an
 *  inserted `Surface` a working-memory node and hands it to whoever set "received" on
 *  the session. The actor `Emitter` wires both into one Knowledge; the run here builds
 *  that Knowledge by hand, the same way the forest's gate does, so the chain is fired
 *  without an actor system.
 *
 *  The ORACLE is the corpus, not the emitter: every `.drake` under `src/main/resources/
 *  draco` is `DrakeGenTest`-pinned to `Drake.emit` of its `.json`, so a `Surface` that
 *  equals the file (whitespace-normalized) is a surface the rule-driven path produced
 *  byte-for-byte as the hand-written emitter would have. The two files that are
 *  deliberately AHEAD of the emitter (`DrakeGenTest.authoredAhead`) are left out of
 *  the insert, so nothing here is compared against `Drake.emit` itself.
 *
 *  Two gates, both asserting:
 *
 *  1. MEANS IT — the actor's projected Knowledge accepts every rule its domain chain
 *     owns: `Emit` (its own) and `SurfaceReceived` (its super's). Counted in the
 *     projected source, not the definitions, for the reason the forest's gate 3 gives.
 *  2. RUNS — insert every definition, fire, and the received surfaces ARE the corpus. */
class GenDrakeTest extends AnyFunSuite with PersistentTestLog {

  private val resourceRoot = Paths.get("src/main/resources")
  private val dracoRoot    = resourceRoot.resolve("draco")

  /** Authored ahead of the emitter — the same pair `DrakeGenTest` excludes. */
  private val authoredAhead: Set[String] = Set("BodyElement", "ActorAspect")

  private def jsonPaths: Seq[Path] =
    Using.resource(Files.walk(dracoRoot)) { s =>
      s.iterator.asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".json")).toList.sorted
    }

  private def definition(p: Path): Option[TypeDefinition] =
    io.circe.parser.parse(new String(Files.readAllBytes(p))).flatMap(_.as[TypeDefinition]).toOption

  private def drakeBeside(p: Path): Path = p.resolveSibling(p.getFileName.toString.stripSuffix(".json") + ".drake")

  private def normalize(source: String): String = {
    val lines = source.replace("\r\n", "\n").split('\n').map(_.replaceAll("\\s+$", "")).toSeq
    lines.foldLeft(Seq.empty[String]) { (acc, l) => if (l.isEmpty && acc.lastOption.contains("")) acc else acc :+ l }
      .dropWhile(_.isEmpty).reverse.dropWhile(_.isEmpty).reverse.mkString("\n")
  }

  // ---- Gate 1 ----

  test("the Emitter accepts every rule its domain chain owns") {
    val projected = Paths.get("src/main/scala/draco/gendrake/Emitter.scala")
    val accepted  = Using.resource(scala.io.Source.fromFile(projected.toFile))(
      _.getLines().filter(_.contains(".ruleType.pattern.accept(")).toList)
    accepted.foreach(l => log.info(s"  accepts: ${l.trim}"))
    assert(accepted.exists(_.contains("Emit.ruleType")), "Emitter does not accept GenDrake's own rule Emit")
    assert(accepted.exists(_.contains("SurfaceReceived.ruleType")), "Emitter does not accept its super-domain's rule SurfaceReceived")
    assert(accepted.size == 2, s"expected exactly 2 accepted rules, got ${accepted.size}")
  }

  // ---- Gate 2 ----

  test("GenDrake runs: every definition crosses to a Surface that is its own .drake") {
    val pairs = jsonPaths.flatMap { p =>
      val name = p.getFileName.toString.stripSuffix(".json")
      if (authoredAhead.contains(name) || !Files.isRegularFile(drakeBeside(p))) None
      else definition(p).map(td => (td, normalize(new String(Files.readAllBytes(drakeBeside(p))))))
    }
    assume(pairs.nonEmpty, s"no definitions under $dracoRoot")

    val received  = new java.util.ArrayList[draco.draketarget.Surface]()
    val knowledge = Rule.knowledgeService.newKnowledge("genDrake")
    // The same two rules the Emitter's Knowledge accepts, in the same order — and the
    // DEFAULT activation mode, because this is a CHAIN: Emit's output is what
    // SurfaceReceived matches.
    Emit.ruleType.pattern.accept(knowledge)
    draco.generator.SurfaceReceived.ruleType.pattern.accept(knowledge)

    val session = knowledge.newStatefulSession()
    try {
      session.set("received", received)
      session.insert(pairs.map(_._1): _*)
      session.fire()
    } finally session.close()

    val expected = pairs.map(_._2).toSet
    val got      = received.asScala.map(s => normalize(s.value)).toSet
    val missing  = expected diff got
    val extra    = got diff expected
    missing.foreach(m => log.info(s"\n--- expected, not received ---\n$m"))
    extra.foreach(e => log.info(s"\n--- received, not in corpus ---\n$e"))
    console.info(s"GenDrake runs: ${received.size} of ${pairs.size} definitions emitted, ${missing.size} missing, ${extra.size} not in the corpus")

    assert(received.size == pairs.size, s"expected one Surface per definition, got ${received.size} of ${pairs.size}")
    assert(missing.isEmpty && extra.isEmpty, s"${missing.size} surfaces missing, ${extra.size} not in the corpus — see the log")
  }
}
