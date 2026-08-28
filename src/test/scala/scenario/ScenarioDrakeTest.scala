package scenario

import draco.{Drake, TypeDefinition}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.util.{Try, Using}

/** Can the abstract rules/facts/inheritance scenario be said in TODAY'S drake?
 *
 *  The scenario (GitHub #63) is a `Forest` root with tree species as message domains
 *  under it and the mycorrhizal hand-off between them as transform domains. Message
 *  domains are not a de jure concept in draco — there is no `message domain` construct —
 *  so the question is whether they can be built DE FACTO out of what drake already has.
 *  Where they cannot, the syntax has to grow, and this test is what says which is which.
 *
 *  The subject matter is load-bearing rather than decorative. Each species carries its own
 *  metabolic fingerprint, so a signal cannot cross unchanged: `Ash.Micromolar` is not
 *  `Birch.Micromolar`, and the fungal network standardizes between them. That IS the
 *  leaf-conversion pattern — `Ash_Birch.Potency` derives Birch's primal and takes Ash's as
 *  its factory argument — and the tree case makes the transform domain's contents obvious
 *  where an abstract one made them arbitrary.
 *
 *  Two gates, deliberately in this order:
 *
 *  1. PARSES — `Drake.parse` accepts the file at all. A failure here is a hard gap: the
 *     surface cannot carry the construct.
 *  2. ROUND-TRIPS — `emit(parse(source))` reproduces the source. A failure here is a
 *     softer gap: the construct is accepted but not carried faithfully, which usually
 *     means the emitter has no spelling for something the parser tolerated.
 *
 *  Report-only. The point is a worklist, not a red suite: everything that fails is a
 *  line item for the grammar, and the count is the measure of how far drake already
 *  reaches. What we already expect to be carried rather than expressed is the dispatch
 *  in `ash/RootInterface.drake` — a Scala `match` smuggled through as host-opaque
 *  text, single-line because a value must be. It will parse. That is precisely the
 *  problem: parsing it proves nothing, since the text projects to no other target. */
class ScenarioDrakeTest extends AnyFunSuite with draco.PersistentTestLog {

  private val root: Path = Paths.get("src/test/resources/scenario")

  private def scenarioFiles: Seq[Path] =
    if (!Files.isDirectory(root)) Seq.empty
    else Using.resource(Files.walk(root)) { s =>
      s.iterator.asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".drake")).toList.sorted
    }

  private def normalize(source: String): String = {
    val lines = source.replace("\r\n", "\n").split('\n').map(_.replaceAll("\\s+$", "")).toSeq
    lines.foldLeft(Seq.empty[String]) { (acc, l) => if (l.isEmpty && acc.lastOption.contains("")) acc else acc :+ l }
      .dropWhile(_.isEmpty).reverse.dropWhile(_.isEmpty).reverse.mkString("\n")
  }

  test("the scenario in today's drake: what parses, what round-trips (report only)") {
    val files = scenarioFiles
    assume(files.nonEmpty, s"no scenario files under $root")

    case class Outcome(path: String, parsed: Option[TypeDefinition], error: Option[String], roundTripped: Boolean)

    val outcomes = files.map { p =>
      val rel    = root.relativize(p).toString
      val source = new String(Files.readAllBytes(p))
      Try(Drake.parse(source)) match {
        case scala.util.Failure(e) =>
          Outcome(rel, None, Some(Option(e.getMessage).getOrElse(e.getClass.getSimpleName)), roundTripped = false)
        case scala.util.Success(td) =>
          val emitted = Try(Drake.emit(td)).toOption
          val same    = emitted.exists(e => normalize(e) == normalize(source))
          if (!same) {
            log.info(s"\n--- $rel: authored ---\n${normalize(source)}")
            log.info(s"\n--- $rel: re-emitted ---\n${emitted.map(normalize).getOrElse("<emit failed>")}")
          }
          Outcome(rel, Some(td), None, same)
      }
    }

    val rejected = outcomes.filter(_.error.isDefined)
    val drifted  = outcomes.filter(o => o.error.isEmpty && !o.roundTripped)

    log.info(s"scenario files: ${outcomes.size}")
    rejected.foreach(o => log.info(s"  REJECTED   ${o.path}: ${o.error.get}"))
    drifted.foreach(o => log.info(s"  DRIFTED    ${o.path} (parsed, but emit(parse(x)) != x — see the pair above)"))
    outcomes.filter(o => o.error.isEmpty && o.roundTripped).foreach(o => log.info(s"  OK         ${o.path}"))

    // What each definition actually became, so a silently-wrong parse is visible as a
    // wrong shape rather than as a pass.
    log.info("\nas parsed:")
    outcomes.flatMap(o => o.parsed.map(o.path -> _)).foreach { case (path, td) =>
      val da = td.dracoAspect
      log.info(f"  $path%-34s from=${da.derivation.map(_.namePath).mkString(",")}%-28s " +
        f"super=${da.superDomain.namePath}%-26s domain=${td.domainAspect.typeName.namePath}%-26s " +
        s"elements=${da.elements.size} types=${td.domainAspect.elementTypeNames.size} " +
        s"rule=${!draco.RuleAspect.isEmpty(td.ruleAspect)} actor=${!draco.ActorAspect.isEmpty(td.actorAspect)}")
    }

    console.info(s"scenario in today's drake: ${outcomes.size} files, " +
      s"${rejected.size} rejected, ${drifted.size} parsed-but-drifted, " +
      s"${outcomes.count(o => o.error.isEmpty && o.roundTripped)} clean")
    succeed
  }
}
