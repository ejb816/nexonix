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
}
