package draco

import io.circe.{Json, Printer, parser}

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** CLI wrapper around Drake — bulk `.drake` <-> `.json` operations over a resource
 *  tree, the drake-side peer of GeneratorCLI. Built into the sbt-assembly fat JAR:
 *
 *    java -cp target/scala-2.13/draco-<version>.jar draco.DrakeCLI <subcommand> <path>...
 *
 *  Subcommands:
 *    emit   <resource-root> [--write]   re-render every `<name>.drake` from its
 *                                       `<name>.json`. Reports what would change;
 *                                       writes only with --write.
 *    parse  <drake-path>                parse one .drake and print the TypeDefinition
 *                                       as canonical JSON
 *    check  <resource-root>             round-trip every pair in both directions —
 *                                       emit(parse(text)) == text and
 *                                       parse(emit(td)) == td (modulo what the surface
 *                                       carries) — and report the drift
 *
 *  WHY `emit` EXISTS. The corpus is emitter-canonical, so a surface change is never
 *  a text transformation over the `.drake` files — it is a change to Drake.emit
 *  followed by re-emission. #52 (brackets moved from the container to the opener)
 *  was applied as an ad-hoc rewrite for want of this subcommand; `emit --write` is
 *  how the next one should be done.
 *
 *  Exit codes:
 *    0 ok
 *    1 usage error
 *    2 read/parse/decode error
 *    5 drift detected (emit without --write, or check)
 */
object DrakeCLI {

  /** Same canonical printer as GeneratorCLI: `"key": value`, matching the corpus. */
  private val canonicalJsonPrinter: Printer = Printer.spaces2.copy(colonLeft = "")

  private def usage(): Nothing = {
    System.err.println(
      """usage: draco.DrakeCLI <subcommand> <path>...
        |  emit  <resource-root> [--write]   re-render every .drake from its .json
        |  parse <drake-path>                print one .drake as TypeDefinition JSON
        |  check <resource-root>             round-trip every pair, report drift""".stripMargin)
    sys.exit(1)
  }

  private def fail(message: String, code: Int): Nothing = {
    System.err.println(message)
    sys.exit(code)
  }

  private def read(path: Path): String =
    if (Files.isRegularFile(path)) new String(Files.readAllBytes(path))
    else fail(s"not a file: $path", 2)

  private def definitionAt(path: Path): TypeDefinition =
    parser.parse(read(path)).flatMap(_.as[TypeDefinition])
      .fold(e => fail(s"$path: ${e.getMessage}", 2), identity)

  /** Every `.json` under `root`, sorted, as (json, sibling .drake) pairs. */
  private def pairs(root: Path): Seq[(Path, Path)] = {
    if (!Files.isDirectory(root)) fail(s"not a directory: $root", 2)
    Using.resource(Files.walk(root)) { stream =>
      stream.iterator.asScala
        .filter(Files.isRegularFile(_))
        .filter(_.toString.endsWith(".json"))
        .toList.sortBy(_.toString)
    }.map(json => json -> Paths.get(json.toString.stripSuffix(".json") + ".drake"))
  }

  /** Whitespace normalization shared with the gen tests: trailing space stripped,
    * blank runs collapsed, leading/trailing blanks trimmed. */
  private def normalize(source: String): String = {
    val lines = source.replace("\r\n", "\n").split('\n').map(_.replaceAll("\\s+$", "")).toSeq
    lines.foldLeft(Seq.empty[String])((acc, l) => if (l.isEmpty && acc.lastOption.contains("")) acc else acc :+ l)
      .dropWhile(_.isEmpty).reverse.dropWhile(_.isEmpty).reverse.mkString("\n")
  }

  /** Run `body`, returning its result, or the failure message when Drake rejects the
    * definition — an aspect it does not yet cover is a SKIP, not an error. */
  private def attempt[A](body: => A): Either[String, A] =
    try Right(body) catch { case e: RuntimeException => Left(Option(e.getMessage).getOrElse(e.toString)) }

  private def emit(root: Path, write: Boolean): Unit = {
    var changed, skipped = 0
    pairs(root).foreach { case (json, drake) =>
      attempt(Drake.emit(definitionAt(json))) match {
        case Left(why) => skipped += 1; println(s"skip   $drake — $why")
        case Right(rendered) =>
          val existing = if (Files.isRegularFile(drake)) Some(new String(Files.readAllBytes(drake))) else None
          if (existing.contains(rendered)) ()
          else {
            changed += 1
            println(s"${if (write) "write " else "differs"} $drake${if (existing.isEmpty) " (new)" else ""}")
            if (write) Files.write(drake, rendered.getBytes("UTF-8"))
          }
      }
    }
    println(s"${if (write) "wrote" else "would rewrite"} $changed, skipped $skipped")
    if (!write && changed > 0) sys.exit(5)
  }

  private def parse(drake: Path): Unit =
    println(canonicalJsonPrinter.print(TypeDefinition.encoder(Drake.parse(read(drake)))))

  private def check(root: Path): Unit = {
    var surfaceDrift, jsonDrift, skipped = 0
    pairs(root).foreach { case (json, drake) =>
      val td = definitionAt(json)
      attempt {
        val rendered = Drake.emit(td)
        if (Files.isRegularFile(drake) && normalize(read(drake)) != normalize(rendered)) {
          surfaceDrift += 1
          println(s"surface $drake — authored text is not what Drake.emit produces")
        }
        // The JSON side is compared through the surface, since the drake spelling of a
        // value is all the parser can recover — the same contract DrakeParseTest states.
        def carried(j: Json): Json = j.arrayOrObject(j,
          a => Json.fromValues(a.map(carried)),
          o => Json.fromJsonObject(io.circe.JsonObject.fromIterable(o.toIterable.map {
            case ("value", v) => "value" -> Json.fromString(Drake.defaultValue(Drake.expression(v)))
            case (k, v) if k == "derivation" || k == "modules" =>
              k -> Json.fromValues(v.asArray.getOrElse(Vector.empty).map(_.mapObject(_.remove("namePackage"))))
            case (k, v) => k -> carried(v)
          })))
        if (carried(TypeDefinition.encoder(Drake.parse(rendered))) != carried(TypeDefinition.encoder(td))) {
          jsonDrift += 1
          println(s"json    $json — parse(emit(td)) does not reproduce the definition")
        }
      }.left.foreach { why => skipped += 1; println(s"skip    $json — $why") }
    }
    println(s"surface drift $surfaceDrift, json drift $jsonDrift, skipped $skipped")
    if (surfaceDrift + jsonDrift > 0) sys.exit(5)
  }

  def main(args: Array[String]): Unit = args.toList match {
    case "emit"  :: root :: rest if rest.forall(_ == "--write") => emit(Paths.get(root), rest.contains("--write"))
    case "parse" :: path :: Nil                                 => parse(Paths.get(path))
    case "check" :: root :: Nil                                 => check(Paths.get(root))
    case _                                                      => usage()
  }
}
