package draco

import io.circe.{Json, parser}
import io.circe.syntax._

import java.nio.file.{Files, Paths}

/** CLI wrapper around Generator. Built into the sbt-assembly fat JAR,
 *  invoked by Claude via:
 *
 *    bin/draco-gen <subcommand> <json-path>
 *
 *  or directly:
 *
 *    java -cp target/scala-2.13/draco-<version>.jar draco.GeneratorCLI <subcommand> <json-path>
 *
 *  Subcommands (MVP):
 *    generate       <json-path>             print Scala source that Generator.generate emits
 *    compile        <json-path>             generate + RuntimeCompiler.compile a single source
 *    compile-multi  <json-path>...          generate each + RuntimeCompiler.compileMulti
 *                                           (use when sources have inter-file dependencies,
 *                                            e.g. sub-domain extends super-domain)
 *    inspect        <json-path>             print parsed TypeDefinition as pretty JSON
 *
 *  Exit codes:
 *    0 ok
 *    1 usage error
 *    2 JSON read/parse/decode error
 *    3 compile failure (compile / compile-multi)
 */
object GeneratorCLI {

  private def usage(): Nothing = {
    System.err.println(
      """usage: draco-gen <subcommand> <json-path>...
        |
        |subcommands:
        |  generate       <json>           print Scala emitted by Generator.generate(td)
        |  compile        <json>           single-source compile via RuntimeCompiler.compile
        |  compile-multi  <json>...        multi-source compile (one unit) via compileMulti;
        |                                  use for sources with inter-file dependencies
        |  inspect        <json>           print parsed TypeDefinition as pretty JSON
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
    RuntimeCompiler.compile(source, s"$name.scala") match {
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

    RuntimeCompiler.compileMulti(sourcesForCompiler) match {
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

  def main(args: Array[String]): Unit = args.toList match {
    case "generate"      :: p :: Nil           => runGenerate(p)
    case "compile"       :: p :: Nil           => runCompile(p)
    case "compile-multi" :: ps if ps.nonEmpty  => runCompileMulti(ps)
    case "inspect"       :: p :: Nil           => runInspect(p)
    case _                                     => usage()
  }
}
