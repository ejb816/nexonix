package draco

import io.circe.Json

/** Shape accessors over a VALUE TYPE, which is a `Json` on the same terms as a
  * TypeElement's `value` (drake.dlt VALUE-TYPES): a string is the type's text as
  * authored, host-opaque; an object is a TYPE-FORM TREE in the expression-tree
  * convention — Atomic a string leaf, Objective `{"(,)": [...]}`, Parametric
  * `{"()": [F, A ...]}`, the collection brackets `{"[]": [T]}` and `{"{}": [T]}` /
  * `{"{}": [K, V]}` (neutral — no host head, 2026-09-18), Morphic `{"->": [S, T]}` binary
  * and right-associative, the bounds `{"<:": [p, b]}` / `{">:": [p, b]}` parameter
  * first. The drake parser trees the four forms since 2026-09-17; a string leaf
  * inside a tree is still host text (the `mut {T}` tail is the one the corpus keeps).
  *
  * Belongs beside `Expression` for the same reason: a question of the tree, not of
  * either target's spelling. Each projection renders the tree its own way —
  * `Drake` to the surface, `DracoGenerator.targetType` to Scala; `neutral` below is
  * drake's own notation, which is what a type-form tree's TEXT is. */
object TypeForm {

  /** True iff the value type is a type-form tree rather than a string leaf. */
  def isTree (valueType: Json) : Boolean = valueType != null && valueType.isObject

  /** The `{op: [operands]}` node a tree is; None for a leaf. */
  def node (valueType: Json) : Option[(String, Vector[Json])] = Expression.node (valueType)

  /** The text of a value type: a string leaf verbatim, absent as "", a tree in its
    * neutral spelling. Every consumer that reads a type as text reads this. */
  def text (valueType: Json) : String =
    if (valueType == null || valueType.isNull) ""
    else valueType.asString.getOrElse (neutral (valueType))

  /** drake's own spelling of a tree: `[T]` `{T}` `{K, V}` for the bracket nodes,
    * `F(A, B)` an application, `(A, B)` a tuple, `S -> T` an arrow with a
    * Morphic LEFT operand parenthesized (the arrow groups to the right, so only a
    * left-nested one needs them), `p <: b` / `p >: b` the bounds. String leaves stay
    * verbatim — they are host text, and only the surface knows how to respell them. */
  def neutral (valueType: Json) : String = node (valueType) match {
    case None                              => text (valueType)
    case Some (("->", Vector (s, t)))      => s"${morphicOperand (s)} -> ${neutral (t)}"
    case Some (("(,)", members))           => members.map (neutral).mkString ("(", ", ", ")")
    case Some (("[]", Vector (a)))         => s"[${neutral (a)}]"
    case Some (("{}", members))            => members.map (neutral).mkString ("{", ", ", "}")
    case Some (("[]+", Vector (a)))        => s"[${neutral (a)}]+"
    case Some (("{}+", members))           => members.map (neutral).mkString ("{", ", ", "}+")
    case Some (("()", f +: arguments))     => s"${text (f)}(${arguments.map (neutral).mkString (", ")})"
    case Some ((op @ ("<:" | ">:"), Vector (p, b))) => s"${neutral (p)} $op ${neutral (b)}"
    case Some ((op, _)) => sys.error (s"TypeForm.neutral: not a type form: '$op' in ${valueType.noSpaces}")
  }

  private def morphicOperand (s: Json) : String =
    if (node (s).exists (_._1 == "->")) s"(${neutral (s)})" else neutral (s)

  /** The head and arguments of a Parametric type, tree or host string: a tree
    * `{"()": [F, A ...]}` directly, a string `F[A]` split at its outer brackets. What
    * a reader of `ActorRef[M]` asks, without caring which era the type is from. */
  def applied (valueType: Json) : Option[(String, Seq[Json])] = node (valueType) match {
    case Some (("()", f +: arguments)) => Some ((text (f), arguments))
    case Some (_)                      => None
    case None =>
      val s   = text (valueType).trim
      val idx = s.indexOf ('[')
      if (idx <= 0 || !s.endsWith ("]")) None
      else Some ((s.substring (0, idx), Seq (Json.fromString (s.substring (idx + 1, s.length - 1).trim))))
  }

  /** `e.valueType.text` at every consumer that reads the type as text. */
  implicit class ValueTypeText (private val valueType: Json) extends AnyVal {
    def text: String = TypeForm.text (valueType)
  }
}
