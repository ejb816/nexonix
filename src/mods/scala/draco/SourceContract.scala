package draco

import io.circe.Json

/** PROTOTYPE (hand-written, staging toward a `draco.Source` definition) — the
  * expression-rendering contract, factored out of the twin renderers
  * `Generator.expression` (Scala) and `Generator.drakeExpression` (drake).
  *
  * The thesis (Dev's north star): a `Source` (the neutral source-projection supertype)
  * carries the generation *engine* (the language-invariant traversal); a specific
  * projection provides only *template slots* (substitution strings). `ExpressionRenderer.render`
  * is the engine; `SourceTemplates` is the slot set; `ScalaTemplates`/`DrakeTemplates` are
  * two instances. Haskell would be a third — and, being Haskell-derived like drake, its
  * expression slots are nearly identical to Drake's (arrow `->`, `if…then…else`, `\…->`),
  * so the expression layer costs almost nothing for Haskell.
  *
  * FINDING (the honest boundary): `arrow` and `conditional` reduce to pure substitution
  * strings; `lambda` does NOT — Scala's "bare param when single, else parenthesise"
  * rule is structural logic, not a template. This is the same structural-vs-surface
  * seam that shows up larger at the declaration layer (trait+companion vs typeclass).
  *
  * Validated against the production renderers by `SourceContractTest`. */
trait SourceTemplates {
  /** Infix token joining the operands of an `->` (function/type arrow) node. */
  def arrow: String

  /** Render an `if` node. Pure template: Scala `if (c) t else e`, drake `if c then t else e`. */
  def conditional (cond: String, thenBranch: String, elseBranch: String) : String

  /** Render a `\` (lambda) node from its already-rendered params and body. Resists
    * pure templating: Scala parenthesises a multi-param list but not a single param. */
  def lambda (params: Seq[String], body: String) : String
}

/** The language-invariant traversal. Recurses operands, dispatches on the operator;
  * the shared operators render identically for every projection, the three variable
  * ones defer to the `SourceTemplates` slots. Mirrors `Generator.expression` /
  * `Generator.drakeExpression` exactly (minus their sys.error message text). */
object ExpressionRenderer {
  def render (value: Json, t: SourceTemplates) : String = {
    if (value == null || value.isNull) ""
    else value.asString.getOrElse {
      value.asObject.map(_.toList) match {
        case Some((op, operands) :: Nil) =>
          val args = operands.asArray.getOrElse(Vector(operands)).map(render(_, t))
          op match {
            case "."        => args.mkString(".")
            case "()"       => s"${args.head}(${args.tail.mkString(", ")})"
            case "(,)"      => args.mkString("(", ", ", ")")
            case "="        => s"${args(0)} = ${args(1)}"
            case "*" | "==" | "!=" => args.mkString(s" $op ")
            case "->"       => args.mkString(t.arrow)
            case "if"       => t.conditional(args(0), args(1), args(2))
            case "\\"       => t.lambda(args.init, args.last)
            case _          => sys.error(s"ExpressionRenderer: unknown operator '$op' in ${value.noSpaces}")
          }
        case _ => sys.error(s"ExpressionRenderer: unrenderable value ${value.noSpaces}")
      }
    }
  }
}

/** Scala token set — reproduces `Generator.expression`. */
object ScalaTemplates extends SourceTemplates {
  val arrow: String = " => "
  def conditional (cond: String, thenBranch: String, elseBranch: String) : String =
    s"if ($cond) $thenBranch else $elseBranch"
  def lambda (params: Seq[String], body: String) : String = {
    val rendered = if (params.size == 1) params.head else params.mkString("(", ", ", ")")
    s"$rendered => $body"
  }
}

/** Drake token set — reproduces `Generator.drakeExpression`. */
object DrakeTemplates extends SourceTemplates {
  val arrow: String = " -> "
  def conditional (cond: String, thenBranch: String, elseBranch: String) : String =
    s"if $cond then $thenBranch else $elseBranch"
  def lambda (params: Seq[String], body: String) : String =
    s"\\${params.mkString(" ")} -> $body"
}
