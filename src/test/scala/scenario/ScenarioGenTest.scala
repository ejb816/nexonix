package scenario

import draco.{Drake, Generator, TypeDefinition}
import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.jdk.CollectionConverters._
import scala.util.{Try, Using}

/** The second scenario gate: does the forest PROJECT?
 *
 *  [[ScenarioDrakeTest]] asked whether the scenario can be SAID in today's drake, and
 *  answered yes for all 22 definitions — every one parses and round-trips. Saying it is
 *  not the same as meaning it: a surface can carry a construct faithfully and still have
 *  no projection behind it. This test asks the next question, in the only way draco has
 *  of asking it — hand each parsed `TypeDefinition` to `Generator.generate` and compile
 *  what comes out.
 *
 *  Four constructs in the scenario have no precedent anywhere in the draco corpus or the
 *  example domains, so none of them has ever been through the Generator:
 *
 *  1. `super` — `dracoAspect.superDomain`. Zero definitions outside the scenario carry
 *     one, so no emitted Scala has ever had to account for it. The place it has to show
 *     up is an actor's `Knowledge`: `Forest` owns the transform rules, and an actor in
 *     `Ash` has to see them.
 *  2. The transform domain's DELIBERATE package/type-name break — `Ash_Birch` living in
 *     package `scenario.ash.birch`. Everywhere else in the corpus a domain's package tail
 *     and its name agree.
 *  3. A cross-package derivation INTO the target domain — `Ash_Birch.Potency` derives
 *     `scenario.birch.Micromolar` while its factory takes `scenario.ash.Micromolar`. That
 *     is the leaf conversion, and it is the whole point of a transform domain.
 *  4. An underscore in a type name — `Ash_Birch`, direction spelled into the name.
 *
 *  Three gates, in this order:
 *
 *  1. GENERATES — `Generator.generate(td)` returns source rather than throwing. A failure
 *     is a hard gap: the emitter has no case for the construct.
 *  2. COMPILES — all 22 emitted sources compile together as one unit. A failure is the
 *     softer, more interesting gap: something was emitted, but what it means in the target
 *     is wrong. Compiling the whole scenario at once is deliberate — the cross-package
 *     references ARE the subject, so compiling any file alone would measure the wrong thing.
 *  3. MEANS IT — compilation is a weak gate on its own. An actor whose `Knowledge` accepts
 *     no rules compiles, runs, and matches nothing, with no error anywhere to show for it,
 *     so a green gate 2 is exactly what hides a construct that has no projection. Gate 3
 *     counts the rules each actor can actually reach beside the ones its super-domain owns.
 *
 *  Report-only, for the same reason [[ScenarioDrakeTest]] is: the point is a worklist. A
 *  red suite here would say only "the scenario is ahead of the Generator", which we already
 *  know; the useful output is WHICH construct is ahead and by how much.
 *
 *  == Why this test writes JSON ==
 *
 *  The Generator does not work from one definition in isolation. It follows the derivation
 *  chain (`chainHits`, to decide whether a companion is a `DracoType`), reads a domain's
 *  member list to build an actor's `Knowledge`, and resolves inherited elements when it
 *  emits a codec — all through `TypeLoader`, which resolves a `TypeName` to a `.json`
 *  resource on the definition path. The scenario has no `.json`: it is drake-only.
 *
 *  Left alone, every one of those lookups returns a typeName-only stub and the emitted
 *  Scala is degraded in ways that have nothing to do with the four constructs — which would
 *  make the measurement worthless. So the test materializes the trio's normative member:
 *  each parsed `TypeDefinition` is written as JSON to the classpath root it was loaded
 *  from, at the resource path its own `TypeName` names, and removed again afterwards.
 *  Nothing under `src/` is touched, and no other suite reads `scenario/`. */
class ScenarioGenTest extends AnyFunSuite with draco.PersistentTestLog {

  private val root: Path = Paths.get("src/test/resources/scenario")

  /** The four definitions carrying a construct with no precedent in the corpus. Their
    * generated source is logged in full — for these, the source IS the finding. */
  private val novel: Map[String, String] = Map(
    "ash/Ash.drake"              -> "1a. `super` — the domain that carries it",
    "ash/RootInterface.drake"    -> "1b. `super` — what the actor's Knowledge does with it",
    "ash/birch/Ash_Birch.drake"  -> "2. package/type-name break + 4. underscore in the name",
    "ash/birch/Marker.drake"     -> "3a. cross-package derivation into the target domain",
    "ash/birch/Potency.drake"    -> "3b. cross-package derivation into the target domain"
  )

  private def scenarioFiles: Seq[Path] =
    if (!Files.isDirectory(root)) Seq.empty
    else Using.resource(Files.walk(root)) { s =>
      s.iterator.asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".drake")).toList.sorted
    }

  /** The classpath root this test class was loaded from — in an sbt run,
    * `target/scala-2.13/test-classes`, which is also where `src/test/resources` is copied
    * and therefore one of `DefinitionPath.hostRoots`. */
  private def definitionRoot: Option[Path] =
    Try(Paths.get(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)).toOption.filter(Files.isDirectory(_))

  private def writeDefinition(rootDir: Path, td: TypeDefinition): Path = {
    val target = rootDir.resolve(td.typeName.resourcePath.stripPrefix("/"))
    Files.createDirectories(target.getParent)
    Files.write(target, td.asJson.spaces2.getBytes("UTF-8"),
      StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
    target
  }

  /** Flatten `ash/birch/Potency.drake` to `ash_birch_Potency.scala`: `compileMulti` writes
    * every source into one flat directory, and four of the scenario's simple names occur
    * in two packages each. */
  private def sourceFileName(rel: String): String =
    rel.stripSuffix(".drake").replace('/', '_').replace('\\', '_') + ".scala"

  test("the forest through the Generator: what emits, what compiles (report only)") {
    val files = scenarioFiles
    assume(files.nonEmpty, s"no scenario files under $root")

    val parsed: Seq[(String, TypeDefinition)] = files.flatMap { p =>
      val rel = root.relativize(p).toString
      Try(Drake.parse(new String(Files.readAllBytes(p)))).toOption.map(rel -> _)
    }
    log.info(s"parsed ${parsed.size} of ${files.size} scenario definitions")

    val rootDir = definitionRoot
    if (rootDir.isEmpty)
      log.info("NOTE: no directory classpath root found — cross-definition lookups will stub out " +
        "and the emitted source is degraded independently of the four constructs.")

    val written: Seq[Path] = rootDir.toSeq.flatMap(dir => parsed.map { case (_, td) => writeDefinition(dir, td) })
    rootDir.foreach(dir => log.info(s"materialized ${written.size} normative definitions under $dir (removed at the end of this test)"))

    try {
      // --- Gate 1: generate ---
      val emitted: Seq[(String, Either[String, String])] = parsed.map { case (rel, td) =>
        rel -> Try(Generator.generate(td)).toEither.left
          .map(e => Option(e.getMessage).getOrElse(e.getClass.getName))
          .flatMap(s => if (s == null || s.isEmpty) Left("Generator produced empty source") else Right(s))
      }
      val failedToEmit = emitted.collect { case (rel, Left(err)) => rel -> err }
      val sources      = emitted.collect { case (rel, Right(src)) => rel -> src }

      // Dump every generated source so a compile error naming a symbol can be traced to a file.
      val dumpDir = Paths.get("target", "test-output", "scenario-generated")
      Try {
        Files.createDirectories(dumpDir)
        sources.foreach { case (rel, src) => Files.write(dumpDir.resolve(sourceFileName(rel)), src.getBytes("UTF-8")) }
      }.toOption.foreach(_ => log.info(s"generated source dumped to $dumpDir"))

      // --- Gate 2: compile, all 22 at once ---
      val compiled: Either[Seq[String], Unit] =
        if (sources.isEmpty) Left(Seq("nothing generated"))
        else Generator.compileMulti(sources.map { case (rel, src) => (src, sourceFileName(rel)) }).map(_ => ())

      // --- Report ---
      log.info("")
      log.info("GATE 1 — generate:")
      emitted.foreach {
        case (rel, Right(src)) => log.info(f"  EMITTED    $rel%-30s ${src.linesIterator.size} lines")
        case (rel, Left(err))  => log.info(f"  FAILED     $rel%-30s $err")
      }

      log.info("")
      log.info("GATE 2 — compile (all generated sources as one unit):")
      compiled match {
        case Right(_)   => log.info("  COMPILED   all of it")
        case Left(errs) =>
          log.info(s"  FAILED     ${errs.size} error(s):")
          errs.foreach(e => log.info(s"    $e"))
      }

      // --- Gate 3: what compiles without meaning what it says ---
      //
      // Compilation is a weak gate on its own. An actor whose Knowledge accepts no rules
      // compiles, runs, and matches nothing, and no error anywhere reports it — so the
      // one construct with no projection at all is the one a green GATE 2 hides. Count
      // the rules each actor can actually reach, beside the rules its super-domain owns.
      val byName: Map[String, TypeDefinition] = parsed.map { case (_, td) => td.typeName.namePath -> td }.toMap
      def rulesIn(domain: TypeDefinition): Seq[String] =
        domain.domainAspect.elementTypeNames.flatMap { n =>
          byName.get((domain.typeName.namePackage :+ n).mkString("."))
        }.filter(m => !draco.RuleAspect.isEmpty(m.ruleAspect)).map(_.typeName.name)

      val actors = parsed.filter { case (_, td) => !draco.ActorAspect.isEmpty(td.actorAspect) }
      log.info("")
      log.info("GATE 3 — an actor's reachable rules (compiles either way):")
      if (actors.isEmpty) log.info("  no actors in the scenario")
      actors.foreach { case (rel, td) =>
        val ownDomain = byName.get(td.domainAspect.typeName.namePath)
        val own       = ownDomain.map(rulesIn).getOrElse(Seq.empty)
        val superTd   = ownDomain.flatMap(d => byName.get(d.dracoAspect.superDomain.namePath))
        val above     = superTd.map(rulesIn).getOrElse(Seq.empty)
        log.info(f"  $rel%-30s own domain ${td.domainAspect.typeName.namePath}%-22s reachable=${own.size} ${own.mkString("[", ", ", "]")}")
        superTd.foreach { sd =>
          log.info(f"  ${""}%-30s super       ${sd.typeName.namePath}%-22s UNREACHABLE=${above.size} ${above.mkString("[", ", ", "]")}")
        }
      }

      // The four constructs, in full. For these the emitted source is the finding, so it
      // is logged whether or not the corpus compiled.
      log.info("")
      log.info("THE FOUR NOVEL CONSTRUCTS — emitted source:")
      novel.toSeq.sortBy(_._2).foreach { case (rel, what) =>
        log.info("")
        log.info(s"--- $rel : $what ---")
        emitted.collectFirst { case (`rel`, e) => e } match {
          case Some(Right(src)) => log.info(src)
          case Some(Left(err))  => log.info(s"<generate failed: $err>")
          case None             => log.info("<not parsed>")
        }
      }

      val unreachable = actors.map { case (_, td) =>
        byName.get(td.domainAspect.typeName.namePath)
          .flatMap(d => byName.get(d.dracoAspect.superDomain.namePath))
          .map(rulesIn).getOrElse(Seq.empty).size
      }.sum
      console.info(s"forest through the Generator: ${parsed.size} definitions, " +
        s"${failedToEmit.size} failed to emit, " +
        (compiled match { case Right(_) => "all of it compiled" case Left(e) => s"${e.size} compile error(s)" }) +
        s", $unreachable super-domain rule(s) an actor cannot reach")
      succeed
    } finally {
      written.foreach(p => Try(Files.deleteIfExists(p)))
    }
  }
}
