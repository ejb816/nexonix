package draco

import io.circe.{Json, JsonObject, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** Closes the JSON -> drake -> JSON loop for the draco corpus: where
 *  DrakeGenTest gates the emitter (JSON -> drake), this gates its inverse,
 *  `Drake.parse` (drake -> JSON).
 *
 *  Two gates, because the drake surface is not yet information-complete:
 *
 *  1. SURFACE round-trip — `emit(parse(text)) == text`. Exact, and the
 *     strongest claim the surface itself supports: the parser reconstructs
 *     everything the emitter spells.
 *
 *  2. JSON round-trip — `parse(emit(td)) == td`, over everything the surface
 *     carries. Three things it does not carry are normalized away before comparing,
 *     and each is a language finding rather than a parser bug:
 *       - EXPRESSION FORM. A value is either host-opaque source text (a JSON string)
 *         or an expression tree, and both render to the same words:
 *         `kvMap.iterator` is a String while `draco.rete.RhsContext -> Unit` is a
 *         `->` tree. The parser reads flat values as strings, so the comparison
 *         normalizes every value to its rendered surface. The tail closes as the
 *         corpus converts to trees (drake.dlt: isString = host-opaque tail).
 *       - EMPTY-COLLECTION SPELLING. `Drake.defaultValue` collapses `Seq.empty` and the
 *         legacy `Seq()` onto the one surface form `[]`, so the parser cannot know
 *         which spelling it came from and canonicalizes to the tree. The comparison
 *         runs both sides through `Drake.defaultValue`; the loss goes to zero once the
 *         three remaining legacy spellings in the corpus become trees.
 *       - REFERENCE PACKAGES. Nearly closed: a `from`/`modules` reference is now bare
 *         only when it lives in the referring type's OWN package, and qualified
 *         otherwise, so its namePackage comes back for all but one case in the corpus
 *         — a reference with no package at all (`Map`, a host type outside every draco
 *         domain) is indistinguishable from a bare same-package one and returns owning
 *         the referring package. The normalization stays only for that residual; drop
 *         it, and this gate verifies packages outright, once `Map` is settled.
 *         (`domain`, `super` and `extensible` always carried theirs.)
 *     The third test measures all three, so the tail is a number rather than a note.
 *
 *  Covered: the plain-type template plus the rule and actor aspects, over TWO corpora
 *  — the draco types and the mods actors. Only the codec aspect is held back —
 *  `Drake.parse` rejects it loudly — and the held-back types are listed by the report
 *  test so the next increment's worklist stays visible. */
class DrakeParseTest extends AnyFunSuite with PersistentTestLog {

  private def deriveDrakePath(resourcePath: String): String =
    resourcePath.stripSuffix(".json") + ".drake"

  private def discoverResourcePaths(): Seq[String] = {
    val resourceRoot = Paths.get(Main.roots.sourceRoot)
    val dracoDir     = resourceRoot.resolve("draco")
    Using.resource(Files.walk(dracoDir)) { stream =>
      stream.iterator.asScala
        .filter(p => Files.isRegularFile(p))
        .map(p => resourceRoot.relativize(p).toString.replace('\\', '/'))
        .filter(_.endsWith(".json"))
        .toList
        .sorted
    }
  }

  private def loadTypeDefinition(rp: String): TypeDefinition = {
    val sourceContent = SourceContent(Main.roots.sourceRoot, rp)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
  }

  private def readDrake(drakePath: String): Option[String] = {
    val path = Paths.get(Main.roots.sourceRoot).resolve(drakePath)
    if (Files.isRegularFile(path)) Some(new String(Files.readAllBytes(path))) else None
  }

  /** Same normalization as DrakeGenTest: strip trailing whitespace, collapse
   *  blank-line runs, trim leading/trailing blank lines. */
  private def normalize(source: String): String = {
    val lines = source.replace("\r\n", "\n").split('\n').map(_.replaceAll("\\s+$", "")).toSeq
    val collapsed = lines.foldLeft(Seq.empty[String]) { (acc, line) =>
      if (line.isEmpty && acc.lastOption.contains("")) acc else acc :+ line
    }
    collapsed.dropWhile(_.isEmpty).reverse.dropWhile(_.isEmpty).reverse.mkString("\n")
  }

  private def diffReport(left: String, right: String, leftLabel: String, rightLabel: String): String = {
    val leftLines  = left.split('\n').toSeq
    val rightLines = right.split('\n').toSeq
    val rows = (0 until math.max(leftLines.size, rightLines.size)).map { i =>
      val l = leftLines.lift(i).getOrElse("")
      val r = rightLines.lift(i).getOrElse("")
      f"${if (l == r) "  " else "!!"} ${i + 1}%3d | $l%-60s | $r"
    }
    f"\n        $leftLabel%-60s | $rightLabel\n" + rows.mkString("\n")
  }

  // --- Scope: everything but the codec aspect ---

  private def carriesCodecAspect(td: TypeDefinition): Boolean =
    !CodecAspect.isEmpty(td.codecAspect)

  private val allPaths: Seq[String] = discoverResourcePaths()

  /** A path is in scope when its JSON carries no codec aspect and it has a .drake. */
  private val parsedPaths: Seq[String] =
    allPaths.filterNot(rp => carriesCodecAspect(loadTypeDefinition(rp)))
      .filter(rp => readDrake(deriveDrakePath(rp)).isDefined)

  /** DrakeGenTest's own exclusions: hand-authored surface deliberately AHEAD of the
   *  JSON. Their .drake carries present-empty rule/actor heads their JSON does not,
   *  so the emitter is not their spec and the SOURCE round-trip has no fixed point
   *  to hit. Their JSON round-trip is unaffected and stays in gate 2. */
  private val authoredAhead: Set[String] = Set("draco/BodyElement.json", "draco/ActorAspect.json")

  // --- Gate 1: the surface round-trip ---

  parsedPaths.filterNot(authoredAhead).foreach { rp =>
    val drakePath = deriveDrakePath(rp)
    test(s"$drakePath: emit(parse(source)) reproduces the source") {
      val authored = readDrake(drakePath).getOrElse(fail(s"missing $drakePath"))
      val emitted  = Drake.emit(Drake.parse(authored))
      val (handNorm, roundNorm) = (normalize(authored), normalize(emitted))
      if (handNorm != roundNorm) {
        log.info(s"\n--- $drakePath: authored ---\n$handNorm")
        log.info(s"\n--- $drakePath: parsed and re-emitted ---\n$roundNorm")
        fail(s"$drakePath: re-emitted drake differs from the authored source." +
          diffReport(handNorm, roundNorm, "authored", "round-tripped"))
      }
    }
  }

  // --- Gate 2: the JSON round-trip, over what the surface carries ---

  /** Normalize away the two things the drake surface does not carry — see the class
   *  comment. Everything else is compared verbatim. */
  private def surfaceCarried(json: Json): Json =
    json.arrayOrObject(
      json,
      array => Json.fromValues(array.map(surfaceCarried)),
      obj => Json.fromJsonObject(JsonObject.fromIterable(obj.toIterable.map {
        case ("value", v) => "value" -> Json.fromString(Drake.defaultValue(Drake.expression(v)))
        case (key, v) if key == "derivation" || key == "modules" =>
          key -> Json.fromValues(v.asArray.getOrElse(Vector.empty).map(_.mapObject(_.remove("namePackage"))))
        case (key, v) => key -> surfaceCarried(v)
      })))

  parsedPaths.foreach { rp =>
    test(s"$rp: parse(emit(td)) reproduces the definition") {
      val td       = loadTypeDefinition(rp)
      val parsed   = Drake.parse(Drake.emit(td))
      val expected = surfaceCarried(TypeDefinition.encoder(td)).spaces2
      val actual   = surfaceCarried(TypeDefinition.encoder(parsed)).spaces2
      if (expected != actual)
        fail(s"$rp: round-tripped definition differs from the source JSON." +
          diffReport(expected, actual, "source json", "round-tripped"))
    }
  }

  // --- The mods actors: the same two gates, on the second corpus ---
  //
  // The draco corpus carries neither named arguments nor call chains — both live in
  // the mods actors, which stayed outside this walk until a nested argument list
  // could terminate itself. It can now: an argument whose value is an application is
  // an OPENER and brackets itself (drake.dlt BRACKETS, GitHub #60), so the `par` that
  // ends an inner list is no longer the `par` that opens the enclosing list's next
  // argument. aerial/Input — three named arguments, each a two-call chain — is the
  // case that was blocked, and it is now gated as authored rather than in miniature.

  private val modsResourceRoot =
    Paths.get(Main.roots.sourceRoot.resolve(java.net.URI.create("../../mods/resources/")))

  private def loadModsTypeDefinition(rp: String): TypeDefinition =
    parser.parse(new String(Files.readAllBytes(modsResourceRoot.resolve(rp))))
      .flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)

  private def readModsDrake(drakePath: String): Option[String] = {
    val path = modsResourceRoot.resolve(drakePath)
    if (Files.isRegularFile(path)) Some(new String(Files.readAllBytes(path))) else None
  }

  /** Every mods definition with an authored .drake and no codec aspect — the same
   *  scope rule the draco walk uses. */
  private val modsPaths: Seq[String] = {
    val domainsDir = modsResourceRoot.resolve("domains")
    if (!Files.isDirectory(domainsDir)) Seq.empty
    else Using.resource(Files.walk(domainsDir)) { stream =>
      stream.iterator.asScala
        .filter(p => Files.isRegularFile(p))
        .map(p => modsResourceRoot.relativize(p).toString.replace('\\', '/'))
        .filter(_.endsWith(".json"))
        .toList
        .sorted
    }.filter(rp => readModsDrake(deriveDrakePath(rp)).isDefined)
      .filterNot(rp => carriesCodecAspect(loadModsTypeDefinition(rp)))
  }

  /** Held out of gate 2 only. A TUPLE argument carrying an application inside it —
   *  `par ("type", io.circe.Json.fromString parameters par "LOCATION")` — renders
   *  INLINE, and an inline `( … )` group is a single token to the lexer, so the
   *  application within comes back as host-opaque text instead of the `()` tree it
   *  went out as. The SURFACE still round-trips exactly, so gate 1 covers the file;
   *  what is lost is structure the surface never spelled apart in the first place.
   *  Unlike the nested argument list this is not an ambiguity — nothing inside the
   *  group is even visible to the parser — so brackets do not address it; a
   *  multi-line tuple form would, and that is a surface question of its own. */
  private val inlineTupleArgument: Set[String] = Set("domains/terrestrial/Output.json")

  modsPaths.foreach { rp =>
    val drakePath = deriveDrakePath(rp)
    test(s"mods $drakePath: emit(parse(source)) reproduces the source") {
      val authored = readModsDrake(drakePath).getOrElse(fail(s"missing $drakePath"))
      val emitted  = Drake.emit(Drake.parse(authored))
      val (handNorm, roundNorm) = (normalize(authored), normalize(emitted))
      if (handNorm != roundNorm) {
        log.info(s"\n--- mods $drakePath: authored ---\n$handNorm")
        log.info(s"\n--- mods $drakePath: parsed and re-emitted ---\n$roundNorm")
        fail(s"mods $drakePath: re-emitted drake differs from the authored source." +
          diffReport(handNorm, roundNorm, "authored", "round-tripped"))
      }
    }
  }

  modsPaths.filterNot(inlineTupleArgument).foreach { rp =>
    test(s"mods $rp: parse(emit(td)) reproduces the definition") {
      val td       = loadModsTypeDefinition(rp)
      val parsed   = Drake.parse(Drake.emit(td))
      val expected = surfaceCarried(TypeDefinition.encoder(td)).spaces2
      val actual   = surfaceCarried(TypeDefinition.encoder(parsed)).spaces2
      if (expected != actual)
        fail(s"mods $rp: round-tripped definition differs from the source JSON." +
          diffReport(expected, actual, "source json", "round-tripped"))
    }
  }

  // --- Named arguments and call chains, asserted structurally ---
  //
  // aerial/Input gates these as authored; this gates the SHAPE, so a named argument
  // silently degrading to a positional one fails here as a wrong tree rather than as
  // a diff. The smallest surface that exhibits both: one named, bracketed argument
  // whose value is a two-call chain.

  test("named argument and call chain round-trip on the surface") {
    val authored =
      """type Probe
        |  elements
        |    fix position domains.aerial.Position Position parameters
        |      par = latitude [ cursor
        |        .get[Double] parameters par "latitude"
        |        .getOrElse parameters par 0.0
        |      ]
        |domain draco Draco
        |""".stripMargin
    val parsed = Drake.parse(authored)
    val value  = parsed.dracoAspect.elements.head.value
    assert(Expression.namedArgument(Expression.operands(value)(1)).map(_._1).contains("latitude"),
      s"argument did not come back named: ${value.noSpaces}")
    val (handNorm, roundNorm) = (normalize(authored), normalize(Drake.emit(parsed)))
    if (handNorm != roundNorm)
      fail("named-argument / chain surface did not round-trip." +
        diffReport(handNorm, roundNorm, "authored", "round-tripped"))
  }

  // --- The measured tail: where the surface is not yet information-complete ---

  test("drake surface losses, unnormalized (report only)") {
    // A `value` is compared WHOLE, never descended into: its whole point is that the
    // same expression may be a string on one side and a tree on the other, and
    // descending would scatter one difference across the tree's leaves — the `.value`
    // key absent on one side, its operands unaccounted for on the other.
    def leafPaths(json: Json, path: String): Seq[(String, String)] =
      if (path.endsWith(".value")) Seq(path -> json.noSpaces)
      else json.arrayOrObject(
        Seq(path -> json.noSpaces),
        array => array.zipWithIndex.flatMap { case (v, i) => leafPaths(v, s"$path[$i]") }.toSeq,
        obj => obj.toIterable.flatMap { case (k, v) => leafPaths(v, s"$path.$k") }.toSeq)

    // Both corpora, measured together: the mods actors are the only source of named
    // arguments, call chains and inline tuples, so leaving them out would understate
    // the tail on exactly the forms the surface is newest at.
    val measured =
      parsedPaths.map(rp => rp -> loadTypeDefinition(rp)) ++
        modsPaths.map(rp => s"mods $rp" -> loadModsTypeDefinition(rp))
    val losses = measured.flatMap { case (rp, td) =>
      val source     = leafPaths(TypeDefinition.encoder(td), "").toMap
      val roundTrip  = leafPaths(TypeDefinition.encoder(Drake.parse(Drake.emit(td))), "").toMap
      (source.keySet ++ roundTrip.keySet).toSeq.sorted.collect {
        case key if source.get(key) != roundTrip.get(key) =>
          (rp, key, source.getOrElse(key, "<absent>"), roundTrip.getOrElse(key, "<absent>"))
      }
    }
    val emptyCollection = Set("\"Seq()\"", "\"Set()\"", "\"Seq.empty\"", "\"Set.empty\"", """{".":["Seq","empty"]}""", """{".":["Set","empty"]}""")
    val byKind = losses.groupBy { case (_, key, was, now) =>
      if (key.endsWith(".value") && emptyCollection.contains(was) && emptyCollection.contains(now)) "empty-collection spelling"
      else if (key.endsWith(".value")) "expression form (string vs tree)"
      else if (key.contains(".derivation") || key.contains(".modules")) "reference package"
      else "OTHER — unaccounted"
    }
    log.info(s"drake surface losses over ${measured.size} types: ${losses.size} fields")
    byKind.toSeq.sortBy(-_._2.size).foreach { case (kind, entries) =>
      log.info(s"  $kind: ${entries.size}")
      entries.foreach { case (rp, key, was, now) => log.info(s"    $rp $key: $was -> $now") }
    }
    console.info(s"drake surface losses: ${losses.size} fields across ${measured.size} types " +
      byKind.map { case (kind, entries) => s"[$kind: ${entries.size}]" }.mkString(" "))
    succeed
  }

  test("types held back from Drake.parse (report only)") {
    val codecAspect = allPaths.filter(rp => carriesCodecAspect(loadTypeDefinition(rp)))
    val noDrake     = allPaths.filter(rp => readDrake(deriveDrakePath(rp)).isEmpty)
    log.info(s"carrying a codec aspect (${codecAspect.size}): ${codecAspect.mkString(", ")}")
    log.info(s"lacking a .drake (${noDrake.size}): ${noDrake.mkString(", ")}")
    log.info(s"mods in scope (${modsPaths.size}): ${modsPaths.mkString(", ")}")
    log.info(s"mods held out of the JSON gate (${inlineTupleArgument.size}): ${inlineTupleArgument.mkString(", ")}")
    console.info(s"Drake.parse scope: ${parsedPaths.size} draco + ${modsPaths.size} mods types in, " +
      s"${codecAspect.size} codec-aspect + ${noDrake.size} drake-less held back")
    succeed
  }
}
