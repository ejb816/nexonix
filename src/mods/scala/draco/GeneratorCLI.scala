package draco

import io.circe.{Printer, parser}
import io.circe.syntax._

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** CLI wrapper around Generator. Built into the sbt-assembly fat JAR,
 *  invoked by Claude via:
 *
 *    bin/draco-gen <subcommand> <path>...
 *
 *  or directly:
 *
 *    java -cp target/scala-2.13/draco-<version>.jar draco.GeneratorCLI <subcommand> <path>...
 *
 *  Subcommands:
 *    generate       <json-path>             print Scala source that Generator.generate emits
 *    compile        <json-path>             generate + Generator.compile a single source
 *    compile-multi  <json-path>...          generate each + Generator.compileMulti
 *                                           (use when sources have inter-file dependencies,
 *                                            e.g. sub-domain extends super-domain)
 *    inspect        <json-path>             print parsed TypeDefinition as pretty JSON
 *    discover       <domain-json> [--force] scan the domain JSON's package directory and
 *                                           rewrite its `elementTypeNames` to match the
 *                                           sibling type JSONs on disk. Refuses dirty/untracked
 *                                           JSON without --force.
 *    verify         <domain-json>           read-only check that the domain JSON's
 *                                           `elementTypeNames` matches its package siblings.
 *                                           Exits 5 on drift; reports what's missing or extra.
 *
 *  Exit codes:
 *    0 ok
 *    1 usage error
 *    2 read/parse/decode error
 *    3 compile failure (compile / compile-multi)
 *    4 safety refusal (discover dirty JSON without --force)
 *    5 verify drift detected
 */
object GeneratorCLI {

  /** Canonical pretty printer for emitted JSON. Matches the dominant
    * `"key": value` (tight colon) style of the existing corpus rather than
    * circe's `Printer.spaces2` default, which puts a space before the colon. */
  private val canonicalJsonPrinter: Printer = Printer.spaces2.copy(colonLeft = "")

  private def usage(): Nothing = {
    System.err.println(
      """usage: draco-gen <subcommand> <path>...
        |
        |subcommands:
        |  generate       <json>            print Scala emitted by Generator.generate(td)
        |  generate-multi <json>...         print one Scala source for a sealed family via
        |                                   Generator.generate(Seq[TypeDefinition])
        |  compile        <json>            single-source compile via Generator.compile
        |  compile-multi  <json>...         multi-source compile (one unit) via compileMulti;
        |                                   use for sources with inter-file dependencies
        |  inspect        <json>            print parsed TypeDefinition as pretty JSON
        |  discover       <json> [--force]  rewrite domain JSON's elementTypeNames from
        |                                   sibling files on disk (refuses dirty without --force)
        |  verify         <json>            read-only check that domain elementTypeNames
        |                                   matches package siblings; exits 5 on drift
        |""".stripMargin)
    sys.exit(1)
  }

  private def loadTypeDefinition(path: String): TypeDefinition = {
    val p = Paths.get(path)
    if (!Files.isRegularFile(p)) {
      System.err.println(s"error: not a regular file: $path")
      sys.exit(2)
    }
    val jsonText = new String(Files.readAllBytes(p))
    parser.parse(jsonText) match {
      case Left(err) =>
        System.err.println(s"error: JSON parse failure in $path: ${err.message}")
        sys.exit(2)
      case Right(json) =>
        json.as[TypeDefinition] match {
          case Left(err) =>
            System.err.println(s"error: TypeDefinition decode failure in $path: ${err.message}")
            sys.exit(2)
          case Right(td) => td
        }
    }
  }

  private def runGenerate(path: String): Unit = {
    val td = loadTypeDefinition(path)
    val source = Generator.generate(td)
    print(source)
    if (!source.endsWith("\n")) println()
  }

  /** Multi-type generation: one Scala source for a sealed family, emitted by the
    * `Generator.generate(Seq[TypeDefinition])` overload. Distinct from
    * `compile-multi`, which generates each type separately and compiles them as
    * one unit. Order of paths is preserved; the Generator topologically sorts. */
  private def runGenerateMultiple(paths: Seq[String]): Unit = {
    val tds    = paths.map(loadTypeDefinition)
    val source = Generator.generate(tds)
    print(source)
    if (!source.endsWith("\n")) println()
  }

  private def runCompile(path: String): Unit = {
    val td     = loadTypeDefinition(path)
    val source = Generator.generate(td)
    val name   = td.typeName.name.replaceAll("\\[.*", "")
    Generator.compile(source, s"$name.scala") match {
      case Right(classDir) =>
        println(s"OK  $name  (classes in ${classDir.getAbsolutePath})")
      case Left(errors) =>
        System.err.println(s"FAIL  $name")
        errors.foreach(e => System.err.println(s"  $e"))
        System.err.println()
        System.err.println("--- Generated source ---")
        System.err.println(source)
        sys.exit(3)
    }
  }

  private def runInspect(path: String): Unit = {
    val td = loadTypeDefinition(path)
    println(td.asJson.spaces2)
  }

  private def runCompileMulti(paths: Seq[String]): Unit = {
    val pairs: Seq[(String, String, String)] = paths.map { p =>
      val td     = loadTypeDefinition(p)
      val source = Generator.generate(td)
      val name   = td.typeName.name.replaceAll("\\[.*", "")
      (name, source, s"$name.scala")
    }
    val sourcesForCompiler = pairs.map { case (_, src, fn) => (src, fn) }
    val displayNames       = pairs.map(_._1).mkString(", ")

    Generator.compileMulti(sourcesForCompiler) match {
      case Right(classDir) =>
        println(s"OK  [$displayNames]  (classes in ${classDir.getAbsolutePath})")
      case Left(errors) =>
        System.err.println(s"FAIL  [$displayNames]")
        errors.foreach(e => System.err.println(s"  $e"))
        System.err.println()
        pairs.foreach { case (name, src, _) =>
          System.err.println(s"--- Generated source: $name ---")
          System.err.println(src)
        }
        sys.exit(3)
    }
  }

  /** True iff `path` refers to a regular file that is tracked by git AND has no
    * uncommitted modifications. Any error (not in a git repo, git not on PATH,
    * file untracked, etc.) returns false — caller treats that as "not safe to
    * silently overwrite." */
  private def isGitClean(path: String): Boolean = {
    import scala.sys.process._
    val nullLogger = ProcessLogger(_ => (), _ => ())
    try {
      val tracked = Process(Seq("git", "ls-files", "--error-unmatch", path)).!(nullLogger) == 0
      if (!tracked) return false
      val porcelain = Process(Seq("git", "status", "--porcelain", "--", path)).!!.trim
      porcelain.isEmpty
    } catch {
      case _: Throwable => false
    }
  }

  /** True iff this TypeDefinition declares itself as the domain (its
    * `domainAspect.typeName` equals its own `typeName`). Discover/verify
    * are meaningful only for domain JSONs. */
  private def isDomainJson(td: TypeDefinition): Boolean = {
    val daName = td.domainAspect.typeName
    daName != null && daName.name.nonEmpty && daName.namePath == td.typeName.namePath
  }

  /** Sort key for an element: types first (0), then rules (1), then actors (2),
    * each group alphabetical. Matches the order used in the existing hand-authored
    * elementTypeNames lists. Rule-/actor-ness is aspect presence, not a name suffix. */
  private def elementCategory(td: TypeDefinition): Int =
    if (!RuleAspect.isEmpty(td.ruleAspect)) 1
    else if (!ActorAspect.isEmpty(td.actorAspect)) 2
    else 0

  /** Scan the domain JSON's parent directory for sibling type JSONs and
    * return the canonical elementTypeNames list. Excludes the domain JSON
    * itself, subdirectories, and non-JSON files. */
  private def discoverElementTypeNames(domainPath: String): Seq[String] = {
    val p   = Paths.get(domainPath)
    val dir = p.getParent
    val selfFile = p.getFileName.toString
    Using.resource(Files.list(dir)) { stream =>
      stream.iterator.asScala
        .filter(Files.isRegularFile(_))
        .map(_.getFileName.toString)
        .filter(_.endsWith(".json"))
        .filterNot(_ == selfFile)
        .map(_.stripSuffix(".json"))
        .toSeq
    }.sortBy(name => (elementCategory(loadTypeDefinition(dir.resolve(name + ".json").toString)), name))
  }

  private def runDiscover(path: String, force: Boolean): Unit = {
    if (!path.endsWith(".json")) {
      System.err.println(s"error: expected a .json path, got: $path")
      sys.exit(2)
    }
    val td = loadTypeDefinition(path)
    if (!isDomainJson(td)) {
      System.err.println(s"error: $path is not a domain JSON (domainAspect.typeName must equal typeName)")
      sys.exit(2)
    }

    val newElements = discoverElementTypeNames(path)
    val updated = TypeDefinition(
      _typeName     = td.typeName,
      _dracoAspect  = td.dracoAspect,
      _domainAspect = DomainAspect(
        _typeName         = td.domainAspect.typeName,
        _elementTypeNames = newElements
      ),
      _ruleAspect   = td.ruleAspect,
      _actorAspect  = td.actorAspect
    )

    val newJsonText = canonicalJsonPrinter.print(updated.asJson) + "\n"
    val jsonP       = Paths.get(path)
    val existing    = new String(Files.readAllBytes(jsonP))
    if (existing == newJsonText) {
      println(s"unchanged  $path  (${newElements.size} elementTypeNames)")
      return
    }
    if (!force && !isGitClean(path)) {
      System.err.println(s"refused: $path has uncommitted changes or is untracked.")
      System.err.println(s"   commit or stash them first, or pass --force to overwrite.")
      System.err.println(s"   recovery after --force: git checkout -- $path")
      sys.exit(4)
    }
    Files.write(jsonP, newJsonText.getBytes)
    println(s"OK  $path  (${newElements.size} elementTypeNames; recovery: git checkout -- $path)")
  }

  private def runVerify(path: String): Unit = {
    if (!path.endsWith(".json")) {
      System.err.println(s"error: expected a .json path, got: $path")
      sys.exit(2)
    }
    val td = loadTypeDefinition(path)
    if (!isDomainJson(td)) {
      System.err.println(s"error: $path is not a domain JSON (domainAspect.typeName must equal typeName)")
      sys.exit(2)
    }

    val expected = discoverElementTypeNames(path)
    val actual   = td.domainAspect.elementTypeNames

    if (expected == actual) {
      println(s"OK  $path  (${actual.size} elementTypeNames in sync)")
    } else {
      val expectedSet = expected.toSet
      val actualSet   = actual.toSet
      val missing     = (expectedSet -- actualSet).toSeq.sorted
      val extra       = (actualSet -- expectedSet).toSeq.sorted
      System.err.println(s"DRIFT  $path")
      if (missing.nonEmpty) System.err.println(s"   missing in JSON: ${missing.mkString(", ")}")
      if (extra.nonEmpty)   System.err.println(s"   extra in JSON:   ${extra.mkString(", ")}")
      if (missing.isEmpty && extra.isEmpty) System.err.println(s"   order differs")
      System.err.println(s"   run: bin/draco-gen discover $path")
      sys.exit(5)
    }
  }

  def main(args: Array[String]): Unit = args.toList match {
    case "generate"       :: p :: Nil           => runGenerate(p)
    case "generate-multi" :: ps if ps.nonEmpty  => runGenerateMultiple(ps)
    case "compile"        :: p :: Nil           => runCompile(p)
    case "compile-multi"  :: ps if ps.nonEmpty  => runCompileMulti(ps)
    case "inspect"        :: p :: Nil           => runInspect(p)
    case "discover"       :: rest if rest.nonEmpty =>
      val force = rest.contains("--force")
      val paths = rest.filterNot(_ == "--force")
      paths match {
        case p :: Nil => runDiscover(p, force)
        case _        => usage()
      }
    case "verify"         :: p :: Nil           => runVerify(p)
    case _                                      => usage()
  }
}
