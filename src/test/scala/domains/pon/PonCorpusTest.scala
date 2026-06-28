package domains.pon

import draco.PersistentTestLog
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.collection.mutable.ListBuffer
import scala.util.Using

/** Validates the `Pon` evaluator against the agreed values, then turns it loose on
  * the "Prime ordinal 78" corpus and reports every expression that does not
  * evaluate to its declared natural number.
  *
  * The self-test is a hard gate (the notation rules must hold). The corpus walk is
  * report-only, mirroring `ExampleDomainsGenTest` — discrepancies are the document's
  * "few errors or inconsistencies"; they go to the suite log, with a headline count
  * on the console, rather than failing the build.
  */
class PonCorpusTest extends AnyFunSuite with PersistentTestLog {

  test("Pon evaluates the agreed PON values") {
    val expect = Map(
      ":"            -> 1,  "()"          -> 2,  "(())"        -> 3,
      "(:())"        -> 4,  "((()))"      -> 5,  "()(())"      -> 6,
      "((:()))"      -> 7,  "(:(()))"     -> 8,  "(():())"     -> 9,
      "()((()))"     -> 10, "(((())))"    -> 11, "(:())(())"   -> 12,
      "(()(()))"     -> 13, "((():()))"   -> 23, "(:())(():())" -> 36,
      "."            -> 0,  "().()"       -> 4   // Goldbach: 2 + 2
    )
    val wrong = expect.toSeq.sortBy(_._2).flatMap { case (s, v) =>
      val got = Pon.valueOf(s)
      log.info(f"  ${if (got == v) "ok  " else "FAIL"}  $s%-14s -> $got  (want $v)")
      if (got == BigInt(v)) None else Some(s"$s -> $got (want $v)")
    }
    assert(wrong.isEmpty, s"PON evaluator disagrees with agreed values: ${wrong.mkString("; ")}")
  }

  test("Prime ordinal 78 corpus: every expression evaluates to its index (report only)") {
    // Corpus lives in the repo (test resources), read from the classpath — no
    // access to the user's home/Downloads, so no OS consent prompt and fully
    // reproducible on any machine.
    val resource = "/domains/pon/prime-ordinal-78.md"
    val raw = Option(getClass.getResourceAsStream(resource)) match {
      case None =>
        console.info("  PON CORPUS: resource not found — skipped")
        cancel(s"corpus resource $resource not on the test classpath")
      case Some(stream) =>
        Using.resource(scala.io.Source.fromInputStream(stream, "UTF-8"))(_.getLines().toList)
    }

    // Keep only the data lines: pure PON glyphs after stripping markdown backticks.
    // Carry each line's 1-based source line number so discrepancies are locatable.
    val dataLine = "[():.\\s]+".r
    val located = raw.zipWithIndex
      .map { case (l, i) => (l.replace("`", "").trim, i + 1) }
      .filter { case (l, _) => l.nonEmpty && dataLine.pattern.matcher(l).matches() }

    var currentN  = -1
    var expectedN = 0
    var primaries = 0
    var checked   = 0
    val problems  = ListBuffer[String]()

    def check(n: Int, exprs: Seq[String], ln: Int): Unit = exprs.foreach { e =>
      try {
        val v = Pon.valueOf(e)
        checked += 1
        if (v != BigInt(n)) problems += f"L$ln%-4d N=$n%-3d  $e%-30s = $v"
      } catch {
        case ex: Throwable => problems += f"L$ln%-4d N=$n%-3d  $e%-30s PARSE: ${ex.getMessage}"
      }
    }

    located.foreach { case (l, ln) =>
      try {
        if (l.startsWith("(")) {
          val close = matchParen(l, 0)
          val n     = binaryToInt(l.substring(1, close))
          if (n != expectedN) problems += f"L$ln%-4d binary sequence: got N=$n, expected N=$expectedN"
          primaries += 1
          currentN  = n
          expectedN = n + 1
          val exprs = splitTop(l.substring(close + 1), ':').map(_.trim).filter(_.nonEmpty)
          check(n, exprs, ln)
        } else {
          val exprs = splitTop(l, ':').map(_.trim).filter(_.nonEmpty)
          check(currentN, exprs, ln)
        }
      } catch {
        case ex: Throwable => problems += f"L$ln%-4d  '${l.take(40)}…' : ${ex.getMessage}"
      }
    }

    problems.foreach(p => log.info(s"  [X] $p"))
    log.info(f"%nSUMMARY: $primaries%d numbers, $checked%d expressions checked, ${problems.size}%d discrepancies")
    console.info(f"  PON CORPUS: $primaries%d numbers, $checked%d expressions, ${problems.size}%d discrepancies — details in the log")
  }

  test("Pon.encode round-trips through eval (encode then eval is identity)") {
    val bad = (1 to 256).filter(n => Pon.eval(Pon.encode(BigInt(n))) != BigInt(n))
    bad.foreach(n => log.info(f"  round-trip FAIL: $n -> ${Pon.canonical(BigInt(n))} -> ${Pon.eval(Pon.encode(BigInt(n)))}"))
    assert(bad.isEmpty, s"encode/eval round-trip failed for: ${bad.mkString(", ")}")
  }

  test("generated canonical vs corpus canonicals (report only)") {
    val resource = "/domains/pon/prime-ordinal-78.md"
    val raw = Option(getClass.getResourceAsStream(resource)) match {
      case None        => cancel(s"corpus resource $resource not on the test classpath")
      case Some(stream) =>
        Using.resource(scala.io.Source.fromInputStream(stream, "UTF-8"))(_.getLines().toList)
    }
    val dataLine = "[():.\\s]+".r
    val primaries = raw.zipWithIndex
      .map { case (l, i) => (l.replace("`", "").trim, i + 1) }
      .filter { case (l, _) => l.nonEmpty && dataLine.pattern.matcher(l).matches() && l.startsWith("(") }

    var diffs = 0
    primaries.foreach { case (l, ln) =>
      val close    = matchParen(l, 0)
      val n        = binaryToInt(l.substring(1, close))
      val docCanon = splitTop(l.substring(close + 1), ':').map(_.trim).filter(_.nonEmpty).headOption.getOrElse("")
      val gen      = Pon.canonical(BigInt(n))
      if (docCanon != gen) {
        diffs += 1
        val docVal = try Pon.valueOf(docCanon).toString catch { case e: Throwable => s"PARSE(${e.getMessage})" }
        log.info(f"  L$ln%-4d N=$n%-3d doc=$docCanon%-24s ($docVal)   gen=$gen")
      }
    }
    console.info(f"  CANONICAL CHECK: ${primaries.size}%d numbers, $diffs%d differ from generated canonical — details in the log")
  }

  test("Pon generates exhaustive prime decompositions, all evaluating to N") {
    val bad = ListBuffer[String]()
    var total = 0
    for (n <- 2 to 200; r <- Pon.representations(n)) {
      total += 1
      if (Pon.valueOf(r) != BigInt(n)) bad += s"N=$n  $r = ${Pon.valueOf(r)}"
    }
    bad.foreach(b => log.info(s"  GEN FAIL: $b"))
    log.info(s"  generated $total representations over 2..200; all evaluate to their N: ${bad.isEmpty}")
    log.info(s"  sample N=23 -> ${Pon.representations(23).mkString("   ")}")
    log.info(s"  sample N=40 -> ${Pon.representations(40).mkString("   ")}")
    assert(bad.isEmpty, s"generated representation evaluated wrong: ${bad.take(5).mkString("; ")}")
  }

  test("emit regenerated PON document 0..79 (correct by construction)") {
    // Self-verify: every representation of every number must evaluate to it.
    (0 to 79).foreach { n =>
      Pon.representations(n).foreach(r =>
        assert(Pon.valueOf(r) == BigInt(n), s"emitted N=$n expr '$r' != $n"))
    }

    // One markdown section + fenced code block per number: canonical PON first,
    // then every exhaustive prime decomposition, each on its own line.
    val sb = new StringBuilder
    sb ++= "# Prime Ordinal Notation 0..79 — regenerated from domains.pon.Pon\n\n"
    sb ++= "Each block lists a number's PON sequences: canonical form first, then every\n"
    sb ++= "exhaustive prime decomposition (2-prime sums for even N, 3-prime sums for odd).\n\n"
    (0 to 79).foreach { n =>
      sb ++= s"$n `(${Pon.binary(n)})`\n\n"
      sb ++= "```\n"
      Pon.representations(n).foreach(r => { sb ++= r; sb ++= "\n" })
      sb ++= "```\n\n"
    }
    val path = Paths.get("src/test/resources/domains/pon/prime-ordinal-generated.md")
    Files.write(path, sb.toString.getBytes("UTF-8"))
    console.info(s"  EMITTED $path (80 numbers, one code block each)")
  }

  // --- local copies of the depth-aware helpers (Pon's are private) ---
  private def matchParen(s: String, open: Int): Int = {
    var depth = 0; var i = open
    while (i < s.length) {
      s.charAt(i) match {
        case '(' => depth += 1
        case ')' => depth -= 1; if (depth == 0) return i
        case _   =>
      }
      i += 1
    }
    sys.error(s"unbalanced parens in '$s'")
  }

  private def splitTop(s: String, sep: Char): Seq[String] = {
    val out = ListBuffer[String](); val cur = new StringBuilder; var depth = 0
    s.foreach {
      case '('                         => depth += 1; cur += '('
      case ')'                         => depth -= 1; cur += ')'
      case c if c == sep && depth == 0 => out += cur.toString; cur.clear()
      case c                           => cur += c
    }
    out += cur.toString
    out.toSeq
  }

  /** Binary cardinal: ':' = 1, '.' = 0. */
  private def binaryToInt(s: String): Int =
    Integer.parseInt(s.map { case ':' => '1'; case '.' => '0'; case c => c }, 2)
}
