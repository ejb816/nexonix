package draco

import io.circe.{Json, Printer, parser}
import io.circe.syntax._
import io.circe.yaml.{parser => yamlParser, printer => yamlPrinter}

import java.nio.file.{Files, Paths}

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
 *    from-yaml      <yaml-path> [--force]   convert YAML to canonical JSON sibling.
 *                                           Refuses to overwrite a JSON with uncommitted git
 *                                           changes (or untracked JSON) unless --force is
 *                                           passed. Recovery after --force: git checkout.
 *    to-yaml        <json-path>             convert JSON to YAML sibling. Always safe —
 *                                           YAML is non-normative and freely regenerable.
 *
 *  Exit codes:
 *    0 ok
 *    1 usage error
 *    2 read/parse/decode error
 *    3 compile failure (compile / compile-multi)
 *    4 safety refusal (from-yaml dirty JSON without --force)
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
        |  compile        <json>            single-source compile via Generator.compile
        |  compile-multi  <json>...         multi-source compile (one unit) via compileMulti;
        |                                   use for sources with inter-file dependencies
        |  inspect        <json>            print parsed TypeDefinition as pretty JSON
        |  from-yaml      <yaml> [--force]  convert YAML to canonical JSON sibling
        |                                   (refuses dirty JSON overwrite without --force)
        |  to-yaml        <json>            convert JSON to YAML sibling (always safe)
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

  /** Read JSON text from a path, parsing through circe; abort on parse error. */
  private def readJsonAst(path: String): Json = {
    val p = Paths.get(path)
    if (!Files.isRegularFile(p)) {
      System.err.println(s"error: not a regular file: $path")
      sys.exit(2)
    }
    val text = new String(Files.readAllBytes(p))
    parser.parse(text) match {
      case Left(err) =>
        System.err.println(s"error: JSON parse failure in $path: ${err.message}")
        sys.exit(2)
      case Right(j) => j
    }
  }

  /** Read YAML text from a path, parsing through circe-yaml to a Json AST. */
  private def readYamlAst(path: String): Json = {
    val p = Paths.get(path)
    if (!Files.isRegularFile(p)) {
      System.err.println(s"error: not a regular file: $path")
      sys.exit(2)
    }
    val text = new String(Files.readAllBytes(p))
    yamlParser.parse(text) match {
      case Left(err) =>
        System.err.println(s"error: YAML parse failure in $path: ${err.getMessage}")
        sys.exit(2)
      case Right(j) => j
    }
  }

  private def runFromYaml(yamlPath: String, force: Boolean): Unit = {
    if (!yamlPath.endsWith(".yaml")) {
      System.err.println(s"error: expected a .yaml path, got: $yamlPath")
      sys.exit(2)
    }
    val json = readYamlAst(yamlPath)
    // Validate the YAML decodes as a TypeDefinition before we touch any JSON.
    json.as[TypeDefinition] match {
      case Left(err) =>
        System.err.println(s"error: TypeDefinition decode failure in $yamlPath: ${err.message}")
        sys.exit(2)
      case Right(td) =>
        // Canonicalize via the encoder — guarantees field order matches the rest of the
        // corpus and silently drops any non-schema keys the user may have added in YAML.
        val newJsonText = canonicalJsonPrinter.print(td.asJson) + "\n"
        val jsonPath    = yamlPath.stripSuffix(".yaml") + ".json"
        val jsonP       = Paths.get(jsonPath)

        if (Files.isRegularFile(jsonP)) {
          val existing = new String(Files.readAllBytes(jsonP))
          if (existing == newJsonText) {
            println(s"unchanged  $jsonPath")
            return
          }
          if (!force && !isGitClean(jsonPath)) {
            System.err.println(s"refused: $jsonPath has uncommitted changes or is untracked.")
            System.err.println(s"   commit or stash them first, or pass --force to overwrite.")
            System.err.println(s"   recovery after --force: git checkout -- $jsonPath")
            sys.exit(4)
          }
        }

        Files.write(jsonP, newJsonText.getBytes)
        println(s"OK  $jsonPath  (recovery: git checkout -- $jsonPath)")
    }
  }

  private def runToYaml(jsonPath: String): Unit = {
    if (!jsonPath.endsWith(".json")) {
      System.err.println(s"error: expected a .json path, got: $jsonPath")
      sys.exit(2)
    }
    val json = readJsonAst(jsonPath)
    // Validate as TypeDefinition so we don't emit YAML for something that can't be
    // re-decoded — catches corrupt input rather than propagating it into YAML.
    json.as[TypeDefinition] match {
      case Left(err) =>
        System.err.println(s"error: TypeDefinition decode failure in $jsonPath: ${err.message}")
        sys.exit(2)
      case Right(td) =>
        val yamlText = yamlPrinter.print(td.asJson)
        val yamlPath = jsonPath.stripSuffix(".json") + ".yaml"
        val yamlP    = Paths.get(yamlPath)
        Files.write(yamlP, yamlText.getBytes)
        println(s"OK  $yamlPath")
    }
  }

  def main(args: Array[String]): Unit = args.toList match {
    case "generate"      :: p :: Nil           => runGenerate(p)
    case "compile"       :: p :: Nil           => runCompile(p)
    case "compile-multi" :: ps if ps.nonEmpty  => runCompileMulti(ps)
    case "inspect"       :: p :: Nil           => runInspect(p)
    case "from-yaml"     :: rest if rest.nonEmpty =>
      val force = rest.contains("--force")
      val paths = rest.filterNot(_ == "--force")
      paths match {
        case p :: Nil => runFromYaml(p, force)
        case _        => usage()
      }
    case "to-yaml"       :: p :: Nil           => runToYaml(p)
    case _                                     => usage()
  }
}
