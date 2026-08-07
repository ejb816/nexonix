package draco

import io.circe.Json

/** Shape accessors over the normative expression TREE — the `{op: [operands]}`
  * form a TypeElement `value` may take (drake.dlt EXPRESSIONS).
  *
  * These belong to NEITHER projection. `Generator` reads them to decide how to
  * render a construction in Scala; `Drake` reads them to render and to parse the
  * drake surface. They lived inside the drake emitter only because that projection
  * was written second, which made the drake half look like a dependency of the
  * Scala half when it never was.
  *
  * Rendering is deliberately not here — each projection spells the same tree its
  * own way (`Generator.expression`, `Drake.expression`, and the factored
  * `ExpressionRenderer` + `SourceTemplates` prototype). Only the questions that
  * have one answer regardless of target live here. */
object Expression {

  /** True iff `value` is a single-key `()` application tree — the node the drake
    * surface spells as `<fn> parameters` / `par`, and Scala as `fn(a, b)`. */
  def isApplication (value: Json) : Boolean =
    value != null && value.asObject.exists(o => o.size == 1 && o.contains("()"))

  /** Operands of an application value: [fn, arg1, arg2, ...]. */
  def operands (value: Json) : Vector[Json] =
    value.asObject.flatMap(_("()")).flatMap(_.asArray).getOrElse(Vector.empty)

  /** A named argument `{"=": [name, value]}` -> (name, value); None when the
    * argument is positional. */
  def namedArgument (value: Json) : Option[(String, Json)] =
    value.asObject.filter(o => o.size == 1 && o.contains("=")).flatMap(_("=")).flatMap(_.asArray)
      .filter(_.size == 2).flatMap(arr => arr(0).asString.map(_ -> arr(1)))

  /** The ROOT names an expression mentions: the identifier that BEGINS each path, so
    * `td` in `td.dracoAspect` but not `dracoAspect`, which is a selection on it.
    *
    * This belongs here for the same reason the shape accessors do — it asks a question
    * of the tree, not of either target's spelling, and it walks the tree rather than
    * rendering first. A `.` node contributes only its first operand's roots; every
    * other operator contributes all of them. A string leaf is HOST-OPAQUE source text,
    * so there it falls back to a lexical scan, which is as far as anything can see into
    * a value drake does not yet parse.
    *
    * Its consumer is the rule model: a condition's parameters ARE the pattern variables
    * its guard mentions, which is why they are derived rather than declared. */
  def rootNames (value: Json) : Set[String] =
    if (value == null || value.isNull) Set.empty
    else value.asString.map(lexicalRoots).getOrElse {
      value.asObject.map(_.toList) match {
        case Some((op, operands) :: Nil) =>
          val args = operands.asArray.getOrElse(Vector(operands))
          // A path SELECTS through its tail; only its head is a reference.
          if (op == ".") args.headOption.map(rootNames).getOrElse(Set.empty)
          else args.flatMap(rootNames).toSet
        case _ => Set.empty
      }
    }

  /** Root names of host-opaque source text: identifiers not reached through a dot.
    * Quoted literals are skipped whole — a name appearing inside a string is text,
    * not a reference. */
  private def lexicalRoots (source: String) : Set[String] = {
    val names = Set.newBuilder[String]
    var i = 0
    while (i < source.length) {
      val ch = source (i)
      if (ch == '"') {
        i += 1
        while (i < source.length && source (i) != '"') {
          if (source (i) == '\\') i += 1
          i += 1
        }
        i += 1
      } else if (ch.isLetter || ch == '_') {
        val start = i
        while (i < source.length && (source (i).isLetterOrDigit || source (i) == '_')) i += 1
        if (start == 0 || source (start - 1) != '.') names += source.substring (start, i)
      } else i += 1
    }
    names.result ()
  }
}
