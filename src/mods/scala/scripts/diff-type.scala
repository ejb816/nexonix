//> using scala 2.13

// diff-type — compare Generator.generate(td) against the hand-written .scala.
//
// Surfaces drift outside the test harness — same whitespace-normalized comparison
// that DracoGenTest performs, but on demand for a single type. Useful when iterating
// on a Generator change or a JSON edit and you want quick visual feedback without
// spinning up the full test suite.
//
// File lookup (relative to PWD):
//   typeName.name "X"         → src/{main,test}/scala/<pkg>/X.scala
//   typeName.name "X.rule"    → src/{main,test}/scala/<pkg>/XRule.scala
//   typeName.name "X.actor"   → src/{main,test}/scala/<pkg>/XActor.scala
//
// Usage:   bin/draco-sc diff-type <name> [namePackage...]
// Example: bin/draco-sc diff-type Primes draco primes
//          bin/draco-sc diff-type AddNaturalSequence.rule draco primes

package scripts

import draco._
import java.nio.file.{Files, Paths}

object DiffType {
  // Map a type to the conventional hand-written filename. Rule-ness is aspect
  // presence, not a name suffix: a ruleAspect emits a `Rule`-suffixed object;
  // plain types and actors keep the bare name.
  private def scalaFilename(name: String, td: TypeDefinition): String =
    if (!RuleAspect.isEmpty(td.ruleAspect)) name + "Rule.scala"
    else                                    name + ".scala"

  // DracoGenTest.normalize equivalent: strip trailing whitespace per line, collapse
  // blank-line runs to one, drop leading/trailing blanks. Preserves indentation and
  // token order; substantive differences survive.
  private def normalize(source: String): String = {
    val lines     = source.replace("\r\n", "\n").split('\n').map(_.replaceAll("\\s+$", "")).toSeq
    val collapsed = lines.foldLeft(Seq.empty[String]) { (acc, line) =>
      if (line.isEmpty && acc.lastOption.contains("")) acc else acc :+ line
    }
    collapsed.dropWhile(_.isEmpty).reverse.dropWhile(_.isEmpty).reverse.mkString("\n")
  }

  // Side-by-side diff. Mismatching lines marked with `!!`. Returns (diffCount, report).
  private def diffReport(handNorm: String, genNorm: String): (Int, String) = {
    val handLines = handNorm.split('\n').toSeq
    val genLines  = genNorm.split('\n').toSeq
    val maxLen    = math.max(handLines.size, genLines.size)
    val rows      = (0 until maxLen).map { i =>
      val h = handLines.lift(i).getOrElse("")
      val g = genLines.lift(i).getOrElse("")
      val marker = if (h == g) "  " else "!!"
      f"$marker ${i + 1}%4d │ $h%-60s │ $g"
    }
    val header = "       hand-written" + (" " * 49) + "│ generated"
    val diffCount = rows.count(_.startsWith("!!"))
    (diffCount, header + "\n" + rows.mkString("\n"))
  }

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      System.err.println("usage: diff-type <name> [namePackage...]")
      System.err.println("example: diff-type Primes draco primes")
      sys.exit(2)
    }

    val name = args(0)
    val pkg  = args.drop(1).toSeq

    def loaded(td: TypeDefinition): Boolean =
      !DracoAspect.isEmpty(td.dracoAspect) ||
      !DomainAspect.isEmpty(td.domainAspect) ||
      !RuleAspect.isEmpty(td.ruleAspect) ||
      !ActorAspect.isEmpty(td.actorAspect)

    val td = Generator.loadType(TypeName(name, _namePackage = pkg))
    if (!loaded(td)) {
      System.err.println(s"error: no TypeDefinition found for ${pkg.mkString(".")}.$name")
      sys.exit(2)
    }

    val filename = scalaFilename(name, td)
    val pathParts: Array[String] = (pkg :+ filename).toArray
    val mainPath = Paths.get("src", ("main" +: "scala" +: pathParts): _*)
    val testPath = Paths.get("src", ("test" +: "scala" +: pathParts): _*)

    val handPath =
      if (Files.isRegularFile(mainPath))      mainPath
      else if (Files.isRegularFile(testPath)) testPath
      else {
        System.err.println(s"error: hand-written .scala not found at either path:")
        System.err.println(s"   $mainPath")
        System.err.println(s"   $testPath")
        System.err.println(s"(run from the repo root)")
        sys.exit(2)
      }

    val handText = new String(Files.readAllBytes(handPath))
    val genText  = Generator.generate(td)

    val handNorm = normalize(handText)
    val genNorm  = normalize(genText)

    if (handNorm == genNorm) {
      println(s"OK  $handPath  (no drift — generator emission matches hand-written)")
    } else {
      val (n, report) = diffReport(handNorm, genNorm)
      println(s"DRIFT  $handPath  ($n differing line(s))")
      println(report)
      sys.exit(1)
    }
  }
}
