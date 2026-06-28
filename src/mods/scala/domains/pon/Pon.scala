package domains.pon

/** Prime Ordinal Notation (PON) — a recursive notation for the naturals.
  *
  * {{{
  *   :              1                 -- the multiplicative unit
  *   .              0  / addition     -- the additive operator; a bare "." is 0
  *   (index:power)  p_index ^ power   -- index and power are themselves PON
  *   (index)        p_index           -- power = 1 (colon omitted)
  *   (:power)       p_1 ^ power        -- index = 1 (empty before the colon)
  *   A B …          A · B · …         -- juxtaposition = product (ascending base index)
  *   A . B [. C]    A + B [+ C]        -- '.' = sum (Goldbach n-sums)
  * }}}
  *
  * where `p_n` is the n-th prime (1-indexed: `p_1 = 2`). The empty PON is 1.
  *
  * This Scala model is the SEED of a draco `pon` type-definition family — the
  * cases below (Zero / One / PrimePower / Product / Sum) mirror the `TypeElement`
  * sealed family and would promote to JSON TypeDefinitions once stable. Parsing
  * and evaluation stay in Scala until the declarative rule layer can express them;
  * that migration is the convergence target, not a permanent home.
  */
sealed trait Pon

object Pon {

  case object Zero extends Pon                                    // .
  case object One  extends Pon                                    // :
  final case class PrimePower(index: Pon, power: Pon) extends Pon // (index:power)
  final case class Product(factors: Seq[Pon]) extends Pon        // juxtaposition
  final case class Sum(terms: Seq[Pon]) extends Pon              // '.'-joined

  // ---- primes (1-indexed: prime(1) = 2) ----
  private val primes = scala.collection.mutable.ArrayBuffer[Int](2, 3)
  def prime(n: Int): Int = {
    require(n >= 1, s"prime index $n < 1")
    while (primes.length < n) {
      var c = primes.last + 2
      while (!isPrime(c)) c += 2
      primes += c
    }
    primes(n - 1)
  }
  private def isPrime(c: Int): Boolean =
    primes.iterator.takeWhile(p => p * p <= c).forall(c % _ != 0)

  // ---- evaluation: PON term -> natural number ----
  def eval(p: Pon): BigInt = p match {
    case Zero               => BigInt(0)
    case One                => BigInt(1)
    case PrimePower(i, pw)  => BigInt(prime(eval(i).toInt)).pow(eval(pw).toInt)
    case Product(fs)        => fs.foldLeft(BigInt(1))(_ * eval(_))
    case Sum(ts)            => ts.foldLeft(BigInt(0))(_ + eval(_))
  }

  def valueOf(s: String): BigInt = eval(parse(s))

  // ---- encoding: natural number -> canonical PON ----
  // The inverse of `eval` for the multiplicative layer. A number is encoded by
  // recursive prime factorization: each factor p_index^power becomes a
  // PrimePower whose index and power are *themselves* encoded, bottoming out at
  // 1 (the empty PON). Factors are emitted in ascending prime-index order, so
  // the rendering is the unique canonical form.
  def encode(n: BigInt): Pon = {
    require(n >= 0, s"cannot encode $n")
    if (n == 0) Zero
    else if (n == 1) One
    else {
      var m   = n
      var idx = 1
      val powers = scala.collection.mutable.ListBuffer[Pon]()
      while (m > 1) {
        val p = BigInt(prime(idx))
        if (m % p == 0) {
          var e = 0
          while (m % p == 0) { m /= p; e += 1 }
          powers += PrimePower(encode(BigInt(idx)), encode(BigInt(e)))
        }
        idx += 1
      }
      if (powers.lengthCompare(1) == 0) powers.head else Product(powers.toSeq)
    }
  }

  /** Render a Pon back to its surface string. For a PrimePower the index is
    * elided when it is 1 (empty before the colon) and the colon is dropped when
    * the power is 1 — the same conventions `parse` consumes. */
  def render(p: Pon): String = p match {
    case Zero => "."
    case One  => ":"
    case PrimePower(idx, pow) =>
      val i = if (idx == One) "" else render(idx)
      if (pow == One) s"($i)" else s"($i:${render(pow)})"
    case Product(fs) => fs.map(render).mkString
    case Sum(ts)     => ts.map(render).mkString(".")
  }

  /** The canonical PON string for a natural number. */
  def canonical(n: BigInt): String = render(encode(n))

  // ---- additive layer: exhaustive prime decompositions (Goldbach) ----
  private def isPrimeValue(k: Int): Boolean =
    k >= 2 && {
      var d = 2
      while (d.toLong * d <= k) { if (k % d == 0) return false; d += 1 }
      true
    }

  private def primesUpTo(n: Int): Seq[Int] = (2 to n).filter(isPrimeValue)

  /** All prime decompositions of `n`, ascending: every 2-prime sum for even `n`;
    * for odd `n` the `2 + (n-2)` sum when `n-2` is prime, then every 3-prime sum.
    * Each summand is itself encoded canonically; terms within a sum ascend. */
  def sums(n: Int): Seq[Pon] = {
    val ps = primesUpTo(n)
    if (n % 2 == 0)
      for { p <- ps; q = n - p if q >= p && isPrimeValue(q) }
        yield Sum(Seq(encode(p), encode(q)))
    else {
      val two = if (n - 2 >= 2 && isPrimeValue(n - 2)) Seq(Sum(Seq(encode(2), encode(n - 2)))) else Seq.empty
      val three = for {
        i <- ps.indices.toSeq
        p = ps(i)
        j <- i until ps.length
        q = ps(j)
        r = n - p - q if r >= q && isPrimeValue(r)
      } yield Sum(Seq(encode(p), encode(q), encode(r)))
      two ++ three
    }
  }

  /** The full PON entry for `n`: canonical form first, then every prime sum. */
  def representations(n: Int): Seq[String] =
    (encode(BigInt(n)) +: sums(n)).map(render)

  /** Binary cardinal column: ':' = 1, '.' = 0. */
  def binary(n: Int): String =
    if (n == 0) "." else n.toBinaryString.map(c => if (c == '1') ':' else '.')

  // ---- parsing ----
  def parse(s: String): Pon = parseExpr(s.trim)

  /** A value expression: '.'-separated sum of product terms. Bare "." == 0. */
  private def parseExpr(s: String): Pon = {
    val terms = splitTop(s, '.').map(_.trim).filter(_.nonEmpty)
    if (terms.isEmpty) Zero
    else if (terms.lengthCompare(1) == 0) parseProduct(terms.head)
    else Sum(terms.map(parseProduct))
  }

  /** A product: juxtaposed prime-power factors (and standalone units). */
  private def parseProduct(s: String): Pon = parseFactors(s) match {
    case Seq()       => One
    case Seq(single) => single
    case many        => Product(many)
  }

  private def parseFactors(s: String): Seq[Pon] = {
    val out = scala.collection.mutable.ListBuffer[Pon]()
    var i = 0
    while (i < s.length) {
      s.charAt(i) match {
        case ':'                 => out += One; i += 1
        case '('                 =>
          val close = matchParen(s, i)
          out += parseGroup(s.substring(i + 1, close))
          i = close + 1
        case c if c.isWhitespace => i += 1
        case c                   => sys.error(s"unexpected '$c' in product '$s'")
      }
    }
    out.toSeq
  }

  /** Inside a (...) — a prime power. index and power are PON; empty == One. */
  private def parseGroup(inner: String): Pon = {
    val ci = topColon(inner)
    if (ci < 0) PrimePower(parseProductOrOne(inner), One)
    else PrimePower(parseProductOrOne(inner.substring(0, ci)),
                    parseProductOrOne(inner.substring(ci + 1)))
  }

  private def parseProductOrOne(s: String): Pon =
    if (s.trim.isEmpty) One else parseProduct(s)

  // ---- depth-aware string helpers ----
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
    sys.error(s"unbalanced parens from $open in '$s'")
  }

  private def topColon(s: String): Int = {
    var depth = 0; var i = 0
    while (i < s.length) {
      s.charAt(i) match {
        case '('               => depth += 1
        case ')'               => depth -= 1
        case ':' if depth == 0 => return i
        case _                 =>
      }
      i += 1
    }
    -1
  }

  private def splitTop(s: String, sep: Char): Seq[String] = {
    val out = scala.collection.mutable.ListBuffer[String]()
    val cur = new StringBuilder
    var depth = 0
    s.foreach {
      case '('                          => depth += 1; cur += '('
      case ')'                          => depth -= 1; cur += ')'
      case c if c == sep && depth == 0  => out += cur.toString; cur.clear()
      case c                            => cur += c
    }
    out += cur.toString
    out.toSeq
  }
}
