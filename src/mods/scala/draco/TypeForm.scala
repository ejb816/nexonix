package draco

import io.circe.Json

/** Shape accessors over a VALUE TYPE, which since 2026-09-16 is a `Json` on the same
  * terms as a TypeElement's `value` (drake.dlt VALUE-TYPES): a string is the type's
  * text as authored, host-opaque; an object will be a type-form tree — Atomic /
  * Objective / Parametric / Morphic, in the expression-tree convention — once the
  * drake parser trees them (the next increment). Nothing in the corpus is a tree yet,
  * so `text` is total over what exists and loud about what does not.
  *
  * Belongs beside `Expression` for the same reason: a question of the tree, not of
  * either target's spelling. The Scala spelling of a type stays the engine's
  * (`DracoGenerator.scalaTypeExpression`, applied once by `targetTypes`). */
object TypeForm {

  /** The text of a value type: a string leaf verbatim, absent as "". A tree has no
    * text until the type forms render; naming the gap beats a silent toString. */
  def text (valueType: Json) : String =
    if (valueType == null || valueType.isNull) ""
    else valueType.asString.getOrElse (
      sys.error (s"TypeForm.text: a type-form tree has no text yet (next increment): ${valueType.noSpaces}"))

  /** `e.valueType.text` at every consumer that reads the type as text. */
  implicit class ValueTypeText (private val valueType: Json) extends AnyVal {
    def text: String = TypeForm.text (valueType)
  }
}
