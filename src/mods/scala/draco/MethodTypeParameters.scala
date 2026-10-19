package draco

/** Validation shared by the two projections of method formal declarations. */
private[draco] object MethodTypeParameters {
  def validate(method: Dynamic): Unit = {
    require(!method.name.exists(c => "[]()".contains(c)),
      s"Method '${method.name}' must declare type parameters separately")
    val names = method.typeParameters.map(_.name)
    require(names.distinct.size == names.size, s"Duplicate type parameters on '${method.name}'")
    method.typeParameters.foreach { p =>
      require(p.name.matches("[A-Za-z_][A-Za-z0-9_]*"), s"Invalid type parameter '${p.name}'")
      Seq(p.lowerBound, p.upperBound).filterNot(_.isNull).foreach { bound =>
        def valid(value: io.circe.Json): Boolean =
          value.asString.exists(_.trim.nonEmpty) || TypeForm.node(value).exists { case (op, operands) =>
            val arity = op match {
              case "[]" | "[]+" => operands.size == 1
              case "{}" | "{}+" => operands.size == 1 || operands.size == 2
              case "->" => operands.size == 2
              case "()" | "(,)" => operands.size >= 2
              case _ => false
            }
            arity && operands.forall(valid)
          }
        require(valid(bound.json), s"Invalid type-form bound for '${p.name}'")
        val text = TypeForm.text(bound.json)
        require(text.nonEmpty && !text.contains("<:") && !text.contains(">:") && !text.contains(": "),
          s"Invalid bound for '${p.name}': $text")
      }
    }
  }
}
