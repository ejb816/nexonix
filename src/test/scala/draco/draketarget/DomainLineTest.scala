package draco.draketarget

import draco._
import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** `DomainLine` is the first TRANSFORM TYPE on the drake target side: a substitution
 *  string with a mapping factory — `"domain " ++ join " " package ++ " " ++ name` —
 *  authored as a `++` tree and projected. It is the first `++` and the first declared
 *  symbol (`join`) to COMPILE AND RUN in generated Scala; the parser test before it
 *  could only render.
 *
 *  The oracle is the corpus: every committed `.drake` under `src/main` carries one
 *  `domain` line, and `DomainLine` built from the definition's own domain pointer must
 *  reproduce it byte for byte. A domain line carrying type parameters (`Format(F)`) is
 *  outside `DomainLine`'s shape — its name is Text without type arguments — and is counted, not compared.
 *  The mapping from a definition to the two arguments is `gendrake.DomainLineOf`, the
 *  first member of the transform domain: this test hands it the definition and only
 *  compares. */
class DomainLineTest extends AnyFunSuite with PersistentTestLog {

  private val dracoRoot = Paths.get("src/main/resources/draco")

  private def jsonPaths: Seq[Path] =
    Using.resource(Files.walk(dracoRoot)) { s =>
      s.iterator.asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".json")).toList.sorted
    }

  private def definition(p: Path): Option[TypeDefinition] =
    io.circe.parser.parse(new String(Files.readAllBytes(p))).flatMap(_.as[TypeDefinition]).toOption

  private def drakeBeside(p: Path): Path = p.resolveSibling(p.getFileName.toString.stripSuffix(".json") + ".drake")

  private def domainLineOf(p: Path): Option[String] =
    Using.resource(scala.io.Source.fromFile(drakeBeside(p).toFile))(_.getLines().find(_.startsWith("domain ")))

  test("DomainLine renders the one line it is built from") {
    val text = Json.fromString("draco.drake.Text")
    val surface = TypeLoader.loadType(TypeName("Surface", Seq("draco", "draketarget")))
    val line = TypeLoader.loadType(TypeName("DomainLine", Seq("draco", "draketarget")))
    val mapped = TypeLoader.loadType(TypeName("DomainLineOf", Seq("draco", "gendrake")))
    assert(DracoAspect.parents(surface.dracoAspect).find(_.name == "Primal").get.typeParameters == Seq(text))
    assert(surface.dracoAspect.factory.parameters.map(_.valueType) == Seq(text))
    assert(line.dracoAspect.factory.parameters.map(_.valueType) ==
      Seq(Json.obj("[]" -> Json.arr(text)), text))
    Seq(line, mapped).foreach { td =>
      assert(td.dracoAspect.factory.body.find(_.name == "value").get.valueType == text)
    }
    assert(Surface("\u00e9").value == "\u00e9")
    assert(DomainLine(Seq("draco", "draketarget"), "DrakeTarget").value == "domain draco draketarget DrakeTarget")
  }

  test("DomainLineOf reproduces every committed domain line in the corpus") {
    val pairs = jsonPaths.flatMap(p => if (Files.isRegularFile(drakeBeside(p))) definition(p).map(td => (p, td)) else None)
    assume(pairs.nonEmpty, s"no definitions under $dracoRoot")

    val (parameterized, plain) = pairs.partition { case (_, td) => td.domainAspect.typeName.typeParameters.nonEmpty }
    val wrong = plain.flatMap { case (p, td) =>
      val expected = domainLineOf(p)
      val got      = draco.gendrake.DomainLineOf(td).value
      if (expected.contains(got)) None
      else Some(s"${p.getFileName}: expected ${expected.getOrElse("<no domain line>")}, got $got")
    }
    wrong.foreach(w => log.info(s"  $w"))
    parameterized.foreach { case (p, _) => log.info(s"  skipped (parameterized): ${p.getFileName} — ${domainLineOf(p).getOrElse("")}") }
    console.info(s"DomainLine over the corpus: ${plain.size - wrong.size} of ${plain.size} domain lines reproduced, " +
      s"${parameterized.size} parameterized skipped, ${wrong.size} wrong")
    assert(wrong.isEmpty, s"${wrong.size} domain lines not reproduced — see the log")
  }
}
