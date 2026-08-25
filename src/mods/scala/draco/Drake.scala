package draco

import io.circe.Json

/** The DRAKE projection: the emitter that renders a TypeDefinition to .drake
  * surface, and the parser that reads it back. They are mutual inverses, and the
  * emitter is the spec — for every corpus type `emit(td)` is the canonical input
  * `parse` must invert, gated by DrakeParseTest's surface round-trip.
  *
  * Sibling of the ScalaTarget projection in `Generator`. What the two share — the
  * shape of the expression tree itself — lives in `Expression`; what they each
  * decide is how to spell it.
  *
  * `draco.drake.Drake` is the definition-backed domain type this serves, and it
  * delegates its `generator` here. Same pairing as `draco.Generator` (this
  * imperative engine) with `draco.generator.Generator[L]` (the typed peer). */
object Drake {
  // --- Emission (JSON TypeDefinition -> .drake surface) ---
  //
  // Where Generator.expression renders a value tree to Scala's spelling, the
  // expression() below renders the same tree to its drake surface (Haskell forms —
  // \ lambda, if-then-else, -> arrow). emit() writes the drake.dlt TEMPLATE: bare
  // `factory`, `globals` keyword, keyword blocks unbracketed with brackets on the
  // OPENER (a dyn-with-body delimits its own body), `from` omitted when the
  // derivation is DracoType alone. Covers the plain-type template + the rule and
  // actor aspects; the codec aspect is a later increment and is rejected loudly.

  /** Render a TypeElement `value` to its drake surface form. Same tree contract
    * as Generator.expression: a string is host-opaque source text passed through
    * verbatim; {op: [operands]} applies the operator. Haskell-form spellings:
    * "->" renders " -> ", "\" renders \p1 p2 -> body, "if" renders
    * if c then t else e. A tree in a String-typed slot needs no quoting here —
    *
    * TOTAL over the tree language, which includes the two ARGUMENT-POSITION nodes:
    * a tuple "(,)" and a named argument "=". drake's multi-line surface spells the
    * latter `par = <name> <value>` (see namedPrefix) and this flat one spells it
    * `<name> = <value>`; same tree, two spellings, exactly as the projections differ
    * from each other. `=` only ever appears inside a "()" operand list, so nothing
    * reached it until the mods corpus — the first to carry named arguments — came
    * under DrakeParseTest.
    * the drake surface carries the expression itself (Action.drake's unquoted
    * arrow), quoting is the ScalaTarget projection's concern. */
  def expression (value: Json) : String = {
    if (value == null || value.isNull) ""
    else value.asString.getOrElse {
      value.asObject.map(_.toList) match {
        case Some((op, operands) :: Nil) =>
          val args = operands.asArray.getOrElse(Vector(operands)).map(expression)
          op match {
            case "."        => args.mkString(".")
            case "->"       => args.mkString(" -> ")
            case "()"       => s"${args.head}(${args.tail.mkString(", ")})"
            case "\\"       => s"\\${args.init.mkString(" ")} -> ${args.last}"
            case "if"       => s"if ${args(0)} then ${args(1)} else ${args(2)}"
            case "(,)"      => args.mkString("(", ", ", ")")
            case "="        => args.mkString(" = ")
            case "*" | "==" | "!=" | "||" => args.mkString(s" $op ")
            case _          => sys.error(s"Drake.expression: unknown operator '$op' in ${value.noSpaces}")
          }
        case _ => sys.error(s"Drake.expression: unrenderable value ${value.noSpaces}")
      }
    }
  }

  /** A chain is an application whose function is a `.`-path whose receiver (first
    * path element) is itself an application — i.e. two or more calls composed
    * (`a.f(x).g(y)`). A lone call (`Json.obj(...)`, `newKnowledge("Primes")`) is not
    * a chain: its full function path stays on one line. */
  private def isChain (value: Json) : Boolean =
    Expression.isApplication(value) &&
      Expression.operands(value).head.asObject.flatMap(_(".")).flatMap(_.asArray)
        .exists(_.headOption.exists(Expression.isApplication))

  /** Unfold a chain into (base receiver, ordered calls). Each call is
    * (member, args): the `.member` applied to `args`. Recurses through the nested
    * `()`/`.` spine down to the base variable/path. */
  private def unfoldChain (value: Json) : (Json, Seq[(String, Vector[Json])]) = {
    val ops       = Expression.operands(value)
    val pathElems = ops.head.asObject.flatMap(_(".")).flatMap(_.asArray).getOrElse(Vector.empty)
    val recv      = pathElems.head
    val member    = pathElems.tail.map(expression).mkString(".")
    if (Expression.isApplication(recv)) {
      val (base, prior) = unfoldChain(recv)
      (base, prior :+ (member, ops.tail))
    } else {
      // recv is a plain path/leaf: base = path minus its last element, member = last.
      val base = if (pathElems.size <= 2) recv else Json.obj("." -> Json.fromValues(pathElems.init))
      (base, Seq(expression(pathElems.last) -> ops.tail))
    }
  }

  /** True iff `value` is a tuple node `{"(,)": [a, b, …]}`. */
  private def isTuple (value: Json) : Boolean =
    value.asObject.exists(o => o.size == 1 && o.contains("(,)"))

  /** Render a value to a single inline line: a tuple as `(e1, e2)`; a lone call as
    * `<fn> parameters par <arg> …`; anything else flat. Used inside a `par (…)`
    * tuple, where every element sits on the one line. */
  private def inlineValue (value: Json) : String =
    if (isTuple(value))
      value.asObject.flatMap(_("(,)")).flatMap(_.asArray).getOrElse(Vector.empty)
        .map(inlineValue).mkString("(", ", ", ")")
    else if (Expression.isApplication(value)) {
      val ops = Expression.operands(value)
      s"${expression(ops.head)} parameters ${ops.tail.map(inlineArgument).mkString(" ")}"
    } else expression(value)

  /** One argument on an inline `parameters` run. Named arguments carry the `=`
    * marker (see namedPrefix). */
  private def inlineArgument (arg: Json) : String = Expression.namedArgument(arg) match {
    case Some((name, v)) => s"par = $name ${inlineValue(v)}"
    case None            => s"par ${inlineValue(arg)}"
  }

  /** The opening of an argument line. A NAMED argument (`{"=": [name, value]}`)
    * spells its marker BEFORE the name — `par = latitude …` — not after it.
    *
    * `par <name> = <value>` would read better, but it is ambiguous: `=` also opens a
    * dyn-with-body's result line, so `… parameters par a` followed by `= result`
    * parses equally well as a named argument `a = result` with the dyn's result
    * missing. Nothing distinguishes the two, and no amount of look-ahead settles it.
    * Moving the marker in front of the name removes the overlap outright — `par`
    * immediately followed by `=` occurs nowhere else, because an argument list that
    * has run out of `par`s has already stopped. Same principle as the brackets on
    * the opener (#52): close the grammar rather than tie-break it.
    *
    * The overlap is with a construct that is itself transitional (drake.dlt
    * DIVERGENCES: the dyn result marker goes once expressions parse), so this
    * position is worth revisiting when it does — `par <name> = <value>` costs nothing
    * once no other `=` exists. */
  private def namedPrefix (name: String) : String = s"par = $name"

  /** Render a value expression whose first token follows `prefix` on the same line;
    * any continuation lines indent under `contIndent`. Shapes: a tuple or leaf/flat
    * expression renders inline; a chain unfolds to the base receiver + one
    * `.member parameters …` line per call; a lone call renders `<fn> parameters …`. */
  private def valueLines (prefix: String, contIndent: String, value: Json) : Seq[String] = {
    if (isTuple(value)) Seq(s"$prefix ${inlineValue(value)}")
    else if (!Expression.isApplication(value)) Seq(s"$prefix ${expression(value)}")
    else if (isChain(value)) {
      val (base, calls) = unfoldChain(value)
      val callIndent    = contIndent + "  "
      s"$prefix ${expression(base)}" +:
        calls.flatMap { case (member, args) => applyLines(s"$callIndent.$member", callIndent, args) }
    } else applyLines(s"$prefix ${expression(Expression.operands(value).head)}", contIndent, Expression.operands(value).tail)
  }

  /** Emit a `parameters` block: `<prefix> parameters` then its arguments. A single
    * leaf argument sits inline (`… parameters par x`); two or more (or a non-leaf
    * single arg) each get their own `par` line one level deeper. */
  private def applyLines (prefix: String, indent: String, args: Vector[Json]) : Seq[String] = {
    if (args.isEmpty) Seq(s"$prefix parameters")
    else if (args.size == 1 && inlineableArg(args.head)) Seq(s"$prefix parameters ${inlinePar(args.head)}")
    else s"$prefix parameters" +: args.flatMap(a => parLines(indent + "  ", a))
  }

  /** An argument inlines when its value is a leaf (not itself an application). */
  private def inlineableArg (arg: Json) : Boolean =
    !Expression.isApplication(Expression.namedArgument(arg).map(_._2).getOrElse(arg))

  private def inlinePar (arg: Json) : String = Expression.namedArgument(arg) match {
    case Some((name, v)) => s"${namedPrefix(name)} ${expression(v)}"
    case None            => s"par ${expression(arg)}"
  }

  /** One argument on its own line — and, when its value is itself an application,
    * BRACKETED (drake.dlt BRACKETS, GitHub #60).
    *
    * A nested argument list is the one place where a keyword block cannot bound
    * itself: `parameters` admits `par`, so the inner list and the enclosing one admit
    * the same word and the `par` that ends the inner is the `par` that opens the
    * outer's next argument. Both readings are legal, and nothing but indentation —
    * which drake does not read — tells them apart. So the argument is an OPENER and
    * brackets itself, exactly as a dyn-with-body brackets its body; as there, the `[`
    * is also what tells a leaf argument from a block one, which is why a leaf `par`
    * carries none. It falls out that an argument of a CHAIN call may now be a chain
    * itself: the `]` ends it before the enclosing chain can claim the next `.member`. */
  private def parLines (indent: String, arg: Json) : Seq[String] = {
    val (prefix, value) = Expression.namedArgument(arg) match {
      case Some((name, v)) => (s"$indent${namedPrefix(name)}", v)
      case None            => (s"${indent}par", arg)
    }
    if (!Expression.isApplication(value)) valueLines(prefix, indent, value)
    else valueLines(s"$prefix [", indent, value) match {
      case Seq(single) => Seq(s"$single ]")
      case lines       => lines :+ s"$indent]"
    }
  }

  /** Split a type-expression argument list on top-level commas only
    * (commas nested in [ ], ( ), { } belong to an inner expression). */
  private def splitTypeArguments (s: String) : Seq[String] = {
    val args = Seq.newBuilder[String]
    val current = new StringBuilder
    var depth = 0
    s.foreach {
      case c @ ('[' | '(' | '{') => depth += 1; current.append(c)
      case c @ (']' | ')' | '}') => depth -= 1; current.append(c)
      case ',' if depth == 0     => args += current.result().trim; current.clear()
      case c                     => current.append(c)
    }
    val last = current.result().trim
    if (last.nonEmpty) args += last
    args.result()
  }

  /** The function arrow, on each side of the projection. The JSON valueType is a
    * SCALA type string, so that is the spelling to split it on; drake's own arrow is
    * `->` — the same token `Drake.expression` already renders for a `->` VALUE node,
    * and the one drake.dlt names as the "function/type arrow" in Haskell form.
    *
    * These were the same token until now: `typeExpression` converted `Seq[T]` to `[T]`
    * but passed the arrow through, so the drake surface spelled types with Scala's
    * arrow and values with drake's. Emitter and parser were symmetrically wrong, so the
    * round-trip never noticed — the corpus is the only place it showed. */
  private val scalaArrow = " => "
  private val drakeArrow = " -> "

  /** Split a type expression on top-level `arrow` occurrences (arrows nested in
    * [ ], ( ), { } belong to an inner type). One segment = no arrow. */
  private def splitTopArrow (s: String, arrow: String) : Seq[String] = {
    val parts = Seq.newBuilder[String]
    var depth, start, i = 0
    while (i < s.length) {
      s(i) match {
        case '[' | '(' | '{' => depth += 1
        case ']' | ')' | '}' => depth -= 1
        case ' ' if depth == 0 && s.startsWith(arrow, i) =>
          parts += s.substring(start, i); start = i + arrow.length; i += arrow.length - 1
        case _ =>
      }
      i += 1
    }
    parts += s.substring(start)
    parts.result()
  }

  /** JSON valueType string -> drake type expression (drake.dlt VALUE-TYPES, inverted):
    * Seq[T] -> [T], Set[T] -> {T}, mutable.Set[T] -> mut {T}, F[A, B] -> F(A, B),
    * (A, B) tuple unchanged (components recursed), A => B arrows recursed on each side
    * and respelled A -> B, plain names verbatim. */
  private def typeExpression (valueType: String) : String = {
    val s = valueType.trim
    if (s.isEmpty) s
    else if (splitTopArrow(s, scalaArrow).size > 1)
      splitTopArrow(s, scalaArrow).map(typeExpression).mkString(drakeArrow)
    else if (s.startsWith("{") && s.endsWith("}"))
      // ALREADY NEUTRAL: the corpus states this one in drake's own notation, so there
      // is nothing to convert but the members. See parseTypeExpression for why the
      // brace forms are currently split by arity.
      splitTypeArguments(s.substring(1, s.length - 1)).map(typeExpression).mkString("{", ", ", "}")
    else if (s.startsWith("(") && s.endsWith(")"))
      splitTypeArguments(s.substring(1, s.length - 1)).map(typeExpression).mkString("(", ", ", ")")
    else {
      val idx = s.indexOf('[')
      if (idx < 0 || !s.endsWith("]")) s
      else {
        val head = s.substring(0, idx)
        val args = splitTypeArguments(s.substring(idx + 1, s.length - 1)).map(typeExpression)
        head match {
          case "Seq" if args.size == 1          => s"[${args.head}]"
          case "Set" if args.size == 1          => s"{${args.head}}"
          case "mutable.Set" if args.size == 1  => s"mut {${args.head}}"
          case _                                => s"$head(${args.mkString(", ")})"
        }
      }
    }
  }

  /** ValueType as it sits in a name-valueType-value line: a top-level function
    * arrow is parenthesized so the value-type reads as one token-group between
    * the name and the value (fix isEmpty (RuleAspect => Boolean) ra => ...);
    * an arrow nested inside a type application needs none (Map(String, [String] => Unit)). */
  private def typeExpressionSlot (valueType: String) : String = {
    val converted = typeExpression(valueType)
    if (splitTopArrow(valueType.trim, scalaArrow).size > 1) s"($converted)" else converted
  }

  /** Element name to drake surface: a method type-parameter rides the name via
    * the ( ) convention — JSON "updated[V1 >: V]" -> drake updated(V1 >: V). */
  private def elementName (name: String) : String = {
    val idx = name.indexOf('[')
    if (idx < 0 || !name.endsWith("]")) name
    else name.substring(0, idx) + "(" + name.substring(idx + 1, name.length - 1) + ")"
  }

  /** TypeName reference on the drake surface: name with type parameters in the
    * ( ) type-application form — Map[K, V]'s TypeName -> Map(K, V). */
  private def typeRef (tn: TypeName) : String =
    if (tn.typeParameters.isEmpty) tn.name
    else s"${tn.name}(${tn.typeParameters.mkString(", ")})"

  /** The universal root, as TypeLoader.rooted spells it. */
  private def isRoot (tn: TypeName) : Boolean =
    tn.name == "DracoType" && tn.namePackage == Seq ("draco")

  /** A FOREIGN reference: a parent that lives in no draco domain. Every draco type
    * is declared inside one, so an EMPTY namePackage says the referent is outside
    * draco's graph entirely — Dictionary's map parent is the corpus's only one.
    *
    * It is spelled as a TYPE EXPRESSION rather than as a name, so Dictionary reads
    * `from {K, V}` — the same notation its own `kvMap` element already uses, one
    * concept spelled one way in one file. The operator carries what the name used
    * to, which is also what closes the round trip: a bare NAME in reference position
    * means "my own package" and so cannot come back package-less, while an operator
    * has no package to lose and nothing to resolve.
    *
    * This is typeExpression's counterpart in reference position, and it differs on
    * exactly one case: a map arrives here NAMED, because a reference is nominal,
    * where a valueType arrives already neutral ({K, V} in the JSON since 0acf2da).
    * `mut {T}` has no spelling here — `mut` is a member keyword, so it bounds the
    * clause rather than opening a reference — and no derivation asks for one. */
  private def foreignReference (tn: TypeName) : String = {
    val arguments = tn.typeParameters.map (typeExpression)
    (tn.name, arguments.size) match {
      case ("Map", 2)          => arguments.mkString ("{", ", ", "}")
      case ("Set", 1)          => s"{${arguments.head}}"
      case ("Seq", 1)          => s"[${arguments.head}]"
      case (name, 0)           => name
      case (name, _)           => s"$name(${arguments.mkString (", ")})"
    }
  }

  /** A dyn-with-body opens its own sub-block; its container needs [ ]. */
  private def opensBlock (element: TypeElement) : Boolean = element match {
    case d: Dynamic => d.parameters.nonEmpty || d.body.nonEmpty
    case _          => false
  }

  /** Empty-collection default in the value position of a name-valueType-value
    * line: [] = Seq.empty, {} = Set.empty. Both host spellings a JSON value may
    * carry (the Seq.empty tree renders "Seq.empty"; legacy strings say "Seq()")
    * collapse to the drake surface form. */
  def defaultValue (rendered: String) : String = rendered match {
    case "Seq.empty" | "Seq()" => "[]"
    case "Set.empty" | "Set()" => "{}"
    case other                 => other
  }

  /** One leaf element line: `kw name value-type value?`. A `mon` (Unit effect)
    * and a `con` (rule condition predicate) carry a value only — no name or
    * value-type; a `con`'s value is its boolean expression tree. */
  private def leafLines (indent: String, keyword: String, element: TypeElement) : Seq[String] = {
    element match {
      case _: Monadic | _: Condition =>
        valueLines(s"$indent$keyword", indent, element.value)
      case e =>
        val vtSlot = typeExpressionSlot(e.valueType)
        val prefix = s"$indent$keyword ${elementName(e.name)} $vtSlot"
        if (Expression.isApplication(e.value)) {
          // Anonymous construction of the declared value type (a factory-less type,
          // e.g. `new LocationReport { … }`): the function IS the value type, so the
          // head is redundant — emit just `<valueType> parameters` + the overrides.
          if (!isChain(e.value) && expression(Expression.operands(e.value).head) == vtSlot)
            applyLines(prefix, indent, Expression.operands(e.value).tail)
          else valueLines(prefix, indent, e.value)
        } else if (isTuple(e.value)) valueLines(prefix, indent, e.value)
        else {
          val value = defaultValue(expression(e.value))
          Seq(if (value.nonEmpty) s"$prefix $value" else prefix)
        }
    }
  }

  private def keyword (element: TypeElement) : String = element match {
    case _: Fixed     => "fix"
    case _: Mutable   => "mut"
    case _: Dynamic   => "dyn"
    case _: Parameter => "par"
    case _: Local     => "loc"
    case _: Monadic   => "mon"
    case _: Variable  => "var"
    case _: Condition => "con"
    case e            => sys.error(s"Drake.emit: no drake keyword for element '${e.name}' (${e.getClass.getSimpleName})")
  }

  /** Render one element at `level` (2 spaces per level). A leaf renders one line;
    * a dyn-with-body BRACKETS its own body (drake.dlt BRACKETS) — its parameters
    * block, its statements, and its result on an `=` line (absent for Unit methods).
    *
    * The brackets sit on the OPENER, not on the container. Bracketing the container
    * marks where the container ends, but the overlap that actually needs resolving
    * is between an opener's last sub-block and the container's NEXT member — so a
    * container-bracketed corpus can still be ambiguous, and drake's one such case
    * was CLI's `dyn drake` followed by `fix commands`. A construct that delimits
    * itself closes the grammar with no appeal to indentation. */
  private def elementLines (element: TypeElement, level: Int) : Seq[String] = {
    val indent = "  " * level
    if (!opensBlock(element)) leafLines(indent, keyword(element), element)
    else {
      val d = element.asInstanceOf[Dynamic]
      val header = s"$indent${keyword(d)} ${elementName(d.name)} ${typeExpressionSlot(d.valueType)} ["
      val parameters =
        if (d.parameters.isEmpty) Seq.empty
        else sectionLines("parameters", d.parameters, level + 1)
      val statementLines = d.body.flatMap(elementLines(_, level + 1))
      val result = expression(d.value) match {
        case ""       => Seq.empty
        case rendered => Seq(s"$indent  = $rendered")
      }
      (header +: (parameters ++ statementLines ++ result)) :+ s"$indent]"
    }
  }

  /** A list-block: head keyword, members one level deeper, never bracketed — each
    * member is bounded by its own keyword, and an opener bounds itself. Only a
    * NAME list needs brackets (nameListLines: its members carry no keyword). */
  private def sectionLines (keyword: String, members: Seq[TypeElement], level: Int) : Seq[String] =
    s"${"  " * level}$keyword" +: members.flatMap(elementLines(_, level + 1))

  /** A bracketed name list (modules / types — both top-level type sections):
    * names carry no bounding keyword, so the [ ] are always required. */
  private def nameListLines (keyword: String, names: Seq[String]) : Seq[String] =
    (s"  $keyword [" +: names.map(n => s"    $n")) :+ "  ]"

  /** Emit the .drake surface for a TypeDefinition (the drake.dlt TEMPLATE:
    * plain-type sections + domain + rule + actor). The codec aspect is the next
    * increment and is rejected loudly rather than silently dropped. */
  def emit (td: TypeDefinition) : String = {
    if (!CodecAspect.isEmpty(td.codecAspect))
      sys.error(s"Drake.emit: codec aspect not yet emitted (next increment): ${td.typeName.name}")

    val da = td.dracoAspect
    val typeParameters =
      if (td.typeName.typeParameters.isEmpty) ""
      else s"(${td.typeName.typeParameters.mkString(", ")})"

    /** A `from` / `modules` reference: BARE when it lives in the referring type's own
      * package, QUALIFIED otherwise. The package of a same-package reference is not
      * information — it is the package we are already in — and drake.dlt INFERENCE says
      * explicit iff not reconstructable. Measured over the corpus: 67 of 112 references
      * are same-package, so spelling every one of them would add words to the majority
      * of references purely to restate where they already are.
      *
      * `domain` / `super` / `extensible` stay unconditionally qualified: each names
      * something OUTSIDE the type being declared (its domain, that domain's parent, the
      * host base it extends), so there is no "own package" for them to be inferred from.
      *
      * A reference with NO package is FOREIGN — outside every draco domain — and is
      * spelled as a type expression instead (foreignReference). */
    def reference (tn: TypeName) : String =
      if (tn.namePackage.isEmpty) foreignReference(tn)
      else if (tn.namePackage == td.typeName.namePackage) typeRef(tn)
      else (tn.namePackage :+ typeRef(tn)).mkString(" ")

    // The universal root is spelled only where it is NOT reconstructable, which is
    // drake.dlt INFERENCE applied to the one reference that is never authored:
    // TypeLoader.rooted appends DracoType to any definition carrying no draco-domain
    // parent, so the root alone — and the root beside a FOREIGN parent, which is
    // Dictionary — comes back on its own. Beside a draco parent it would not, so
    // there it stays on the surface.
    val rootRestored = !da.derivation.exists(tn => !isRoot(tn) && tn.namePackage.nonEmpty)
    val spelled      = if (rootRestored) da.derivation.filterNot(isRoot) else da.derivation
    val fromClause   = if (spelled.isEmpty) "" else s" from ${spelled.map(reference).mkString(" ")}"
    // The drake surface names the bare concept (AddNaturalSequence); rule-/actor-ness
    // is carried by the ruleAspect/actorAspect, never by the type name.
    val header = s"type ${td.typeName.name}$typeParameters$fromClause"

    val modules =
      if (da.modules.isEmpty) Seq.empty
      else nameListLines("modules", da.modules.map(reference))
    val extensible =
      if (da.extensible.name.isEmpty) Seq.empty
      else Seq(s"  extensible ${(da.extensible.namePackage :+ typeRef(da.extensible)).mkString(" ")}")
    val elements =
      if (da.elements.isEmpty) Seq.empty
      else sectionLines("elements", da.elements, 1)
    val factory =
      if (da.factory.valueType.isEmpty) Seq.empty
      else {
        val parameters =
          if (da.factory.parameters.isEmpty) Seq.empty
          else sectionLines("parameters", da.factory.parameters, 2)
        val body =
          if (da.factory.body.isEmpty) Seq.empty
          else sectionLines("body", da.factory.body, 2)
        // A factory normally constructs the enclosing type, and that is what makes its
        // value-type elidable (drake.dlt CONVENTIONS: `factory` takes no name). When it
        // constructs something else it is NOT reconstructable and has to be spelled —
        // the live case is the actor-minting factory, whose ActorType value-type is what
        // tells the Scala projection to mint an actor rather than an instance of the type.
        val head =
          if (da.factory.valueType == factoryValueType(td.typeName.name, td.typeName.typeParameters)) "  factory"
          else s"  factory ${typeExpressionSlot(da.factory.valueType)}"
        head +: (parameters ++ body)
      }
    val globals =
      if (da.globalElements.isEmpty) Seq.empty
      else sectionLines("globals", da.globalElements, 1)

    val domain =
      if (td.domainAspect.typeName.name.nonEmpty) {
        // typeRef, not the bare name: a domain may be PARAMETERIZED
        // (draco.format.Format(F)), and its type parameters are load-bearing —
        // the Scala projection emits Domain[Format[_]] from them. Spelling the
        // reference bare here dropped them from the surface entirely.
        val head = s"domain ${(td.domainAspect.typeName.namePackage :+ typeRef(td.domainAspect.typeName)).mkString(" ")}"
        val superDomain =
          if (da.superDomain.name.isEmpty) Seq.empty
          else Seq(s"  super ${(da.superDomain.namePackage :+ typeRef(da.superDomain)).mkString(" ")}")
        val types =
          if (td.domainAspect.elementTypeNames.isEmpty) Seq.empty
          else nameListLines("types", td.domainAspect.elementTypeNames)
        head +: (superDomain ++ types)
      } else Seq.empty

    // rule aspect (drake.dlt: `rule` head, then `pattern` { variables, conditions }
    // and `action` body). variables/conditions are LEAF blocks (var/con open no
    // sub-block) so sectionLines emits them bracket-free; the action body sits
    // directly under `action`, one indent level deeper.
    val rule =
      if (RuleAspect.isEmpty(td.ruleAspect)) Seq.empty
      else {
        val ra = td.ruleAspect
        val variables =
          if (ra.pattern.variables.isEmpty) Seq.empty
          else sectionLines("variables", ra.pattern.variables, 2)
        val conditions =
          if (ra.pattern.conditions.isEmpty) Seq.empty
          else sectionLines("conditions", ra.pattern.conditions, 2)
        val pattern =
          if (variables.isEmpty && conditions.isEmpty) Seq.empty
          else "  pattern" +: (variables ++ conditions)
        val action =
          if (ra.action.body.isEmpty) Seq.empty
          else "  action" +: ra.action.body.flatMap(elementLines(_, 2))
        "rule" +: (pattern ++ action)
      }

    // actor aspect (drake.dlt: `actor` head, then start / message / signal action
    // blocks — construction / receive / receiveSignal). Each block's body sits one
    // indent level deeper, same shape as the rule `action` body. A block elides when
    // its action carries no body (an actor typically populates only some of the three).
    val actor =
      if (ActorAspect.isEmpty(td.actorAspect)) Seq.empty
      else {
        val aa = td.actorAspect
        def block(keyword: String, action: Action): Seq[String] =
          if (action.body.isEmpty) Seq.empty
          else s"  $keyword" +: action.body.flatMap(elementLines(_, 2))
        // The message type is the actor's `T`, carried by the ASPECT rather than by
        // an Actor[T] derivation. It must reach the drake surface or the projection
        // loses it entirely once the transitional derivation is dropped.
        val messageType =
          if (aa.messageType.name.isEmpty) Seq.empty
          else Seq(s"  messageType ${typeRef(aa.messageType)}")
        "actor" +: (messageType ++ block("start", aa.start) ++ block("message", aa.message) ++ block("signal", aa.signal))
      }

    ((header +: (modules ++ extensible ++ elements ++ factory ++ globals)) ++ domain ++ rule ++ actor).mkString("", "\n", "\n")
  }

  // --- Parsing (.drake surface -> JSON TypeDefinition) ---
  //
  // The inverse of emit() above. The emitter is the spec: for every corpus type
  // emit(td) is the canonical input parse must invert, so the gate is the
  // surface round-trip emit(parse(text)) == text (DrakeParseTest), with the
  // JSON round-trip parse(emit(td)) == td asserted over everything the
  // surface actually carries.
  //
  // Covered: the plain-type template (header / modules / extensible / elements /
  // factory / globals / domain), the rule aspect (pattern / variables / conditions /
  // action) and the actor aspect (messageType / start / message / signal), plus
  // value-position applications with positional or named arguments and unfolded call
  // chains. The codec aspect is the remaining increment and is rejected loudly rather
  // than silently dropped — the same convention emit() uses for it.

  // Plain vals: unlike the App companions in the model, `Drake` is a bare object, so
  // there is no DelayedInit to defer them past first use.
  private val memberKeywords: Set[String] =
    Set ("fix", "mut", "dyn", "par", "loc", "mon", "var", "con")

  /** The member keywords that can open a STATEMENT inside a dyn-with-body. */
  private val statementKeywords: Set[String] = Set ("fix", "mut", "loc", "mon")

  /** Every token that bounds a member — the reserved words a value slot stops at.
    * drake.dlt's whitespace-insignificance rests on this set: a value runs until
    * the next reserved keyword, never until end-of-line. */
  private val reserved: Set[String] =
    memberKeywords ++ Set (
      "type", "from", "domain", "super", "types", "rule", "actor", "codec",
      "modules", "extensible", "elements", "factory", "parameters", "body", "globals",
      "pattern", "action", "variables", "conditions",
      "start", "message", "signal", "messageType",
      "=", "[", "]")

  /** One drake token: its source text and its span.
    *
    * A bracketed group GLUES onto the word before it when no space separates them
    * (`Map(K, V)`, `classOf[Main].getResource("/")`), so a type expression or a
    * host-opaque value reads as ONE token; a quoted literal is skipped whole, so
    * brackets and spaces inside it never affect nesting. A lone `[` (one followed
    * by whitespace) is a list-block bracket rather than a Seq type — the lexical
    * rule that keeps drake.dlt's `[ ]` blocks apart from its `[T]` value types.
    *
    * The span lets a value slot recover its RAW source text instead of re-joining
    * tokens, so internal spacing survives verbatim. Nothing here records a line or
    * a column: drake is whitespace-insignificant, and the parser reads no layout. */
  private final case class Token (text: String, start: Int, end: Int)

  private def lex (source: String) : Vector[Token] = {
    val tokens = Vector.newBuilder[Token]
    var i = 0
    while (i < source.length) {
      val ch = source (i)
      if (ch.isWhitespace) i += 1
      else {
        val start = i
        val blockBracket =
          ch == ']' || (ch == '[' && (i + 1 >= source.length || source (i + 1).isWhitespace))
        if (blockBracket) i += 1
        else {
          var depth = 0
          var done  = false
          while (!done && i < source.length) {
            source (i) match {
              case '"' =>
                i += 1
                while (i < source.length && source (i) != '"') {
                  if (source (i) == '\\') i += 1
                  i += 1
                }
                i += 1
              case '(' | '[' | '{'                   => depth += 1; i += 1
              case ')' | ']' | '}'                   => depth -= 1; i += 1
              case c if c.isWhitespace && depth <= 0 => done = true
              case _                                 => i += 1
            }
          }
        }
        tokens += Token (source.substring (start, i), start, i)
      }
    }
    tokens.result ()
  }

  private final class Cursor (val source: String, tokens: Vector[Token]) {
    private var index = 0
    def exhausted: Boolean          = index >= tokens.length
    def peek: Option[String]        = tokens.lift (index).map (_.text)
    def at (text: String): Boolean  = peek.contains (text)
    def atReserved: Boolean         = peek.exists (reserved.contains)

    /** A CHAIN MEMBER: a token opening with `.`, which valueLines writes at the head
      * of each `.member parameters …` continuation line of an unfolded call chain.
      * Nothing else can start with a dot — a path inside one expression (`a.b.c`,
      * `session.insert(…)`) is glued into a single token by the lexer — so this is a
      * purely lexical boundary, and like every other boundary in drake it consults no
      * layout. A value slot stops here the same way it stops at a reserved word. */
    def atChainMember: Boolean      = peek.exists (t => t.length > 1 && t.startsWith ("."))
    def take (): Token         = { val t = tokens (index); index += 1; t }
    def takeText (): String         = take ().text
    def expect (text: String): Unit =
      if (at (text)) take ()
      else sys.error (s"Drake.parse: expected '$text' but found '${peek.getOrElse ("<end>")}'")
  }

  /** The value slot: every token up to the next reserved keyword or chain member,
    * returned as the raw source span so internal spacing survives verbatim. */
  private def span (c: Cursor) : String =
    if (c.exhausted || c.atReserved || c.atChainMember) ""
    else {
      val first = c.take ()
      var end   = first.end
      while (!c.exhausted && !c.atReserved && !c.atChainMember) end = c.take ().end
      c.source.substring (first.start, end)
    }

  /** Invert defaultValue + expression for a LEAF value: the empty-collection
    * surface forms recover their canonical trees, everything else is carried as
    * host-opaque source text.
    *
    * A flat expression cannot be told from the tree that renders to the same
    * surface — `kvMap.iterator` is a String in the JSON while
    * `draco.rete.RhsContext -> Unit` is a `->` tree, and both are just words on the
    * drake surface. That tail is MEASURED by DrakeParseTest rather than guessed at;
    * it closes as the corpus converts to trees (drake.dlt: isString = host-opaque
    * tail, isObject = drake-native). */
  private def leafValue (rendered: String) : Json = rendered match {
    case ""    => Json.Null
    case "[]"  => Json.obj ("." -> Json.arr (Json.fromString ("Seq"), Json.fromString ("empty")))
    case "{}"  => Json.obj ("." -> Json.arr (Json.fromString ("Set"), Json.fromString ("empty")))
    case other => Json.fromString (other)
  }

  private def parseValue (c: Cursor) : Json = parseValue (c, "")

  /** The value slot. A `parameters` keyword following the head turns the value into
    * an application tree — applyLines's inverse, `<fn> parameters par <arg> …`. When
    * no head precedes it the function IS the declared value type (leafLines's
    * anonymous-construction form), which `anonymousHead` supplies. Whatever the head
    * resolved to, any `.member parameters …` continuations that follow fold onto it
    * as a call chain. */
  private def parseValue (c: Cursor, anonymousHead: String) : Json =
    chainCalls (c, applied (c, span (c), anonymousHead))

  /** `head` applied to a `parameters` list, or the bare leaf when no list follows. */
  private def applied (c: Cursor, head: String, anonymousHead: String) : Json =
    if (!c.at ("parameters")) leafValue (head)
    else {
      c.take ()
      val function = if (head.nonEmpty) head else anonymousHead
      if (function.isEmpty)
        sys.error ("Drake.parse: `parameters` with no function and no declared value type to supply one")
      Json.obj ("()" -> Json.fromValues (Json.fromString (function) +: arguments (c, chained = false)))
    }

  /** The arguments of a `parameters` list: `par <value>` positionally, `par = <name>
    * <value>` named (see namedPrefix for why the marker precedes the name). The list
    * ends where the `par`s do — an argument value stops at the next reserved word, so
    * nothing here consults layout either.
    *
    * A BRACKETED argument — parLines's inverse — is one whose value opens a
    * `parameters` list of its own. Inner and outer list admit the same `par`, so the
    * inner one cannot bound itself; the brackets close it, and inside them the value
    * is read whole (chains included). The `[` is purely local: `par [` is a block
    * argument, `par x` a leaf one, and no look-ahead separates them.
    *
    * `chained` says this list belongs to a CHAIN CALL, and it decides who claims a
    * `.member` arriving after an UNBRACKETED argument. Both readings are legal on the
    * surface — `.g` may continue the chain, or start a chain on the argument — and
    * they differ only by indentation, which drake does not read. The chain wins: a
    * pending chain is nearer than the argument it just passed, which is what
    * `cursor .get[Double] parameters par "latitude" .getOrElse parameters par 0.0`
    * means. An argument that wants its own chain says so with its brackets. */
  private def arguments (c: Cursor, chained: Boolean) : Seq[Json] = {
    val collected = Seq.newBuilder[Json]
    def argument () : Json =
      if (c.at ("[")) { c.take (); val value = parseValue (c); c.expect ("]"); value }
      else if (chained) applied (c, span (c), "")
      else parseValue (c)
    while (c.at ("par")) {
      c.take ()
      collected +=
        (if (!c.at ("=")) argument ()
         else {
           c.take ()
           Json.obj ("=" -> Json.arr (Json.fromString (c.takeText ()), argument ()))
         })
    }
    collected.result ()
  }

  /** Fold the `.member parameters …` continuations onto a receiver — unfoldChain's
    * inverse. Each call re-nests: the receiver so far becomes the head of the member's
    * `.` path, and that path is applied to the call's arguments, so `a` + `.f(x)` +
    * `.g(y)` rebuilds a.f(x).g(y) exactly as the tree spelled it. A chain call always
    * writes its `parameters`, empty argument list included, so its absence is an error
    * rather than a shorter form. */
  private def chainCalls (c: Cursor, receiver: Json) : Json = {
    var value = receiver
    while (c.atChainMember) {
      val member = splitPath (c.takeText ().substring (1))
      val path   = Json.obj ("." -> Json.fromValues (value +: member.map (Json.fromString)))
      c.expect ("parameters")
      value = Json.obj ("()" -> Json.fromValues (path +: arguments (c, chained = true)))
    }
    value
  }

  /** Split a chain member on its top-level dots — `get[scala.Int]` is one path
    * element, `a.b` is two. */
  private def splitPath (member: String) : Seq[String] = {
    val elements = Seq.newBuilder[String]
    val current  = new StringBuilder
    var depth    = 0
    member.foreach {
      case c @ ('[' | '(' | '{') => depth += 1; current.append (c)
      case c @ (']' | ')' | '}') => depth -= 1; current.append (c)
      case '.' if depth == 0     => elements += current.result (); current.clear ()
      case c                     => current.append (c)
    }
    (elements += current.result ()).result ().filter (_.nonEmpty)
  }

  /** Split an applied surface name into its head and its ( ) arguments:
    * "Dictionary(K, V)" -> ("Dictionary", Seq("K", "V")); a bare name has none. */
  private def splitApplied (token: String) : (String, Seq[String]) = {
    val idx = token.indexOf ('(')
    if (idx < 0 || !token.endsWith (")")) (token, Seq.empty)
    else (token.substring (0, idx), splitTypeArguments (token.substring (idx + 1, token.length - 1)))
  }

  /** Invert elementName: a method type-parameter rides the name via ( ) on the
    * drake surface — `updated(V1 >: V)` -> JSON "updated[V1 >: V]". */
  private def parseElementName (token: String) : String = {
    val idx = token.indexOf ('(')
    if (idx < 0 || !token.endsWith (")")) token
    else token.substring (0, idx) + "[" + token.substring (idx + 1, token.length - 1) + "]"
  }

  /** Invert typeExpression (drake.dlt VALUE-TYPES): [T] -> Seq[T], {T} -> Set[T],
    * mut {T} -> mutable.Set[T], F(A, B) -> F[A, B], A -> B back to A => B, tuples
    * recursed, plain names verbatim. A sole parenthesized member is the arrow slot
    * typeExpressionSlot wraps, not a one-tuple, so it unwraps. */
  private def parseTypeExpression (expr: String) : String = {
    val s = expr.trim
    if (s.isEmpty) s
    else if (splitTopArrow (s, drakeArrow).size > 1) splitTopArrow (s, drakeArrow).map (parseTypeExpression).mkString (scalaArrow)
    else if (s.startsWith ("mut {") && s.endsWith ("}")) s"mutable.Set[${parseTypeExpression (s.substring (5, s.length - 1))}]"
    else if (s.startsWith ("[") && s.endsWith ("]"))     s"Seq[${parseTypeExpression (s.substring (1, s.length - 1))}]"
    // THE BRACE FAMILY, SPLIT BY ARITY — and the split is transitional, not a rule of
    // the notation. drake spells a set {T} and a map {K, V}; what differs is how far
    // each has moved out of the host's syntax. A map is already NEUTRAL in the JSON,
    // so it is carried through as it stands; a set is still `Set[T]` there, so it is
    // converted back. When the set follows the map, both arities become verbatim and
    // this case — with its opposite number in typeExpression — disappears from both
    // converters, which is what "the corpus states the type in drake's own notation"
    // will finally mean. Until then, a two-member brace that came back as Set[K, V]
    // was the standing wrong answer this replaces.
    else if (s.startsWith ("{") && s.endsWith ("}")) {
      val members = splitTypeArguments (s.substring (1, s.length - 1)).map (parseTypeExpression)
      if (members.size == 1) s"Set[${members.head}]" else members.mkString ("{", ", ", "}")
    }
    else if (s.startsWith ("(") && s.endsWith (")")) {
      val members = splitTypeArguments (s.substring (1, s.length - 1))
      if (members.size == 1) parseTypeExpression (members.head)
      else members.map (parseTypeExpression).mkString ("(", ", ", ")")
    }
    else {
      val idx = s.indexOf ('(')
      if (idx < 0 || !s.endsWith (")")) s
      else {
        val args = splitTypeArguments (s.substring (idx + 1, s.length - 1)).map (parseTypeExpression)
        s"${s.substring (0, idx)}[${args.mkString (", ")}]"
      }
    }
  }

  /** Consume a value-type slot. `mut {T}` is the one two-token form. */
  private def takeValueType (c: Cursor) : String = {
    val first = c.takeText ()
    parseTypeExpression (if (first == "mut") s"mut ${c.takeText ()}" else first)
  }

  /** A type reference as typeRef spells it: name plus ( ) type parameters. The
    * surface carries no package here — a bare reference means the referring type's
    * own package, and `resolved` supplies it once the domain line has been read. */
  private def parseRef (token: String) : TypeName = {
    val (name, typeParameters) = splitApplied (token)
    TypeName (name, _typeParameters = typeParameters)
  }

  /** A reference the surface spells with an OPERATOR carries no package, so it must
    * not be resolved against the referring type: it is foreign by construction. The
    * test is the emitter's own spelling rather than a second list of primitive names,
    * so the two sides cannot drift apart. */
  private def operatorCarried (tn: TypeName) : Boolean =
    opensTypeExpression (foreignReference (tn))

  /** True of a token that opens a TYPE EXPRESSION rather than a name — the two
    * bracket operators foreignReference spells. `[` is only ever a Seq here: a lone
    * `[` (one followed by whitespace) lexes as a block bracket and is reserved, so
    * it bounds the clause instead of reaching this. A foreign type with no operator
    * still spells its bare name and takes the name path, which is the one reference
    * this notation cannot tell from a same-package one; the corpus has none. */
  private def opensTypeExpression (token: String) : Boolean =
    token.length > 1 && (token.head == '{' || token.head == '[')

  /** Invert foreignReference: an operator-carried reference names a type OUTSIDE
    * every draco domain, so it comes back with no package — which is what it went
    * out as, and why nothing has to be resolved against the referring type. The
    * arguments are ordinary type expressions and convert as such. */
  private def foreignRef (token: String) : TypeName = {
    val s         = token.trim
    val arguments = splitTypeArguments (s.substring (1, s.length - 1)).map (parseTypeExpression)
    if (s.startsWith ("[")) TypeName ("Seq", _typeParameters = arguments)
    else TypeName (if (arguments.size == 1) "Set" else "Map", _typeParameters = arguments)
  }

  /** A package-qualified reference (`domain draco Draco`, `super …`, `extensible …`):
    * space-separated package words, then the reference itself.
    *
    * The reserved set cannot bound this scan — a package word may BE a keyword, and
    * `extensible org apache pekko actor typed ExtensibleBehavior(T)` is the live
    * case (`actor`). Case bounds it instead: package words are lower-case, the
    * reference is upper-case, so the first upper-case word ends the path.
    *
    * A reference that opens a TYPE EXPRESSION has no package words to scan — the
    * operator carries the name, and the referent is foreign. */
  private def takeQualifiedRef (c: Cursor) : TypeName =
    if (c.peek.exists (opensTypeExpression)) foreignRef (c.takeText ())
    else {
      val words = Seq.newBuilder[String]
      while (!c.exhausted && c.peek.exists (_.headOption.exists (_.isLower))) words += c.takeText ()
      val namePackage = words.result ()
      if (c.exhausted) sys.error (s"Drake.parse: qualified reference '${namePackage.mkString (" ")}' has no type name")
      val ref = parseRef (c.takeText ())
      TypeName (ref.name, namePackage, ref.typeParameters)
    }

  /** A bracketed list of REFERENCES — `modules [ … ]`. Each member may carry package
    * words, so it is read with takeQualifiedRef rather than as one token; a bare member
    * comes back with no package and is resolved against the referring type. */
  private def parseRefList (c: Cursor) : Seq[TypeName] = {
    c.expect ("[")
    val refs = Seq.newBuilder[TypeName]
    while (!c.exhausted && !c.at ("]")) refs += takeQualifiedRef (c)
    c.expect ("]")
    refs.result ()
  }

  /** A bracketed name list — `types [ … ]`. Its members are bare names WITHIN the
    * domain being declared, never references elsewhere, so they stay one token each. */
  private def parseNameList (c: Cursor) : Seq[String] = {
    c.expect ("[")
    val names = Seq.newBuilder[String]
    while (!c.exhausted && !c.at ("]")) names += c.takeText ()
    c.expect ("]")
    names.result ()
  }

  /** A dyn-with-body's statement list, inside the dyn's own brackets: statement
    * keywords until the `=` result or the closing `]`. Neither is a member keyword,
    * so the list bounds itself — no layout is consulted. */
  private def statements (c: Cursor) : Seq[BodyElement] = {
    val collected = Seq.newBuilder[BodyElement]
    while (c.peek.exists (statementKeywords.contains))
      collected += parseMember (c).asInstanceOf[BodyElement]
    collected.result ()
  }

  /** The members of a bare list-block: it runs while the cursor sits on a keyword
    * this block ADMITS. The admitted set is what bounds it — `parameters` takes only
    * `par`, so a `loc` after it is the enclosing dyn's first statement, not a
    * parameter. A member that opens a sub-block brackets itself, so no bare block
    * ever has to guess where a nested one ended. */
  private def parseBlock (c: Cursor, admits: Set[String]) : Seq[TypeElement] = {
    val members = Seq.newBuilder[TypeElement]
    while (c.peek.exists (admits.contains)) members += parseMember (c)
    members.result ()
  }

  /** A named block-section (`parameters`, `body`, …) when the cursor sits on its
    * head keyword; empty otherwise. */
  private def parseSection (c: Cursor, keyword: String, admits: Set[String]) : Seq[TypeElement] =
    if (!c.at (keyword)) Seq.empty else { c.take (); parseBlock (c, admits) }

  /** The member keywords a declaration block admits (elements / factory body /
    * globals): every BodyElement form, `dyn` included. */
  private val declarationKeywords: Set[String] = Set ("fix", "mut", "dyn", "loc")

  /** What an ACTION body admits (a rule's `action`, an actor's start / message /
    * signal): the declaration forms plus `mon`, since an action is mostly effects. */
  private val actionKeywords: Set[String] = declarationKeywords + "mon"

  /** One member of a list-block. `dyn` may open its own sub-block (parameters,
    * statements, `=` result); every other keyword is a single leaf. */
  private def parseMember (c: Cursor) : TypeElement = {
    c.takeText () match {
      case "mon" => Monadic (parseValue (c))
      case "con" => Condition (parseValue (c))
      case "var" =>
        val name = parseElementName (c.takeText ())
        Variable (name, takeValueType (c))
      case "dyn" =>
        val name      = parseElementName (c.takeText ())
        val valueType = takeValueType (c)
        // A dyn-with-body opens with `[`; a leaf dyn carries its result inline.
        if (!c.at ("[")) Dynamic (name, valueType, Seq.empty, Seq.empty, parseValue (c))
        else {
          c.take ()
          val parameters = parseSection (c, "parameters", Set ("par")).map (_.asInstanceOf[Parameter])
          val body       = statements (c)
          val result     = if (!c.at ("=")) Json.Null else { c.take (); parseValue (c) }
          c.expect ("]")
          Dynamic (name, valueType, parameters, body, result)
        }
      case keyword @ ("fix" | "mut" | "loc" | "par") =>
        val name      = parseElementName (c.takeText ())
        val valueType = takeValueType (c)
        val value     = parseValue (c, typeExpressionSlot (valueType))
        keyword match {
          case "fix" => Fixed (name, valueType, value)
          case "mut" => Mutable (name, valueType, value)
          case "loc" => Local (name, valueType, value)
          case _     => Parameter (name, valueType, value)
        }
      case other => sys.error (s"Drake.parse: unknown member keyword '$other'")
    }
  }

  /** The factory's valueType is the enclosing type — the drake surface leaves it
    * implicit (drake.dlt CONVENTIONS: `factory` takes no name). */
  private def factoryValueType (name: String, typeParameters: Seq[String]) : String =
    if (typeParameters.isEmpty) name else s"$name[${typeParameters.mkString (", ")}]"

  /** Parse a .drake source into its TypeDefinition — the inverse of emit().
    * The plain-type template plus the rule and actor aspects; codec is the remaining
    * increment and is rejected loudly rather than silently dropped. */
  def parse (source: String) : TypeDefinition = {
    val c = new Cursor (source, lex (source))
    c.expect ("type")
    val (name, typeParameters) = splitApplied (c.takeText ())
    val derivation = Seq.newBuilder[TypeName]
    if (c.at ("from")) {
      c.take ()
      // Each reference may carry package words. The case rule bounds the SEQUENCE as
      // well as each member: lower-case words are package, the first upper-case token
      // ends the reference, and the next lower-case word starts the following one —
      // so `from draco Dictionary(K, V) draco DracoType` needs no separator.
      while (!c.exhausted && !c.atReserved) derivation += takeQualifiedRef (c)
    }

    var modules          = Seq.empty[TypeName]
    var extensible       = TypeName.Null
    var elements         = Seq.empty[TypeElement]
    var factory          = Factory.Null
    var globalElements   = Seq.empty[BodyElement]
    var superDomain      = TypeName.Null
    var domainName       = TypeName.Null
    var elementTypeNames = Seq.empty[String]
    // The role aspects. `rule` / `actor` are read as FLAGS rather than as recursive
    // sections: each of their sub-sections carries a reserved head of its own
    // (pattern / variables / conditions / action; messageType / start / message /
    // signal), so the flat loop below bounds them exactly as it bounds domain / super
    // / types. What the head keyword contributes is presence — a rule with a wholly
    // default pattern and action is still a rule.
    var isRule           = false
    var isActor          = false
    var variables        = Seq.empty[Variable]
    var conditions       = Seq.empty[Condition]
    var action           = Action.Null
    var messageType      = TypeName.Null
    var start            = Action.Null
    var message          = Action.Null
    var signal           = Action.Null

    def actionBody () : Action = Action (Seq.empty, parseBlock (c, actionKeywords).map (_.asInstanceOf[BodyElement]))

    while (!c.exhausted) {
      c.takeText () match {
        case "modules"     => modules = parseRefList (c)
        case "extensible"  => extensible = takeQualifiedRef (c)
        case "elements"    => elements = parseBlock (c, declarationKeywords)
        case "globals"     => globalElements = parseBlock (c, declarationKeywords).map (_.asInstanceOf[BodyElement])
        case "factory"     =>
          // A named value-type follows `factory` only when it is not the enclosing type.
          val valueType =
            if (c.exhausted || c.atReserved) factoryValueType (name, typeParameters)
            else takeValueType (c)
          factory = Factory (
            valueType,
            parseSection (c, "parameters", Set ("par")).map (_.asInstanceOf[Parameter]),
            parseSection (c, "body", declarationKeywords).map (_.asInstanceOf[BodyElement]))
        case "domain"      => domainName = takeQualifiedRef (c)
        case "super"       => superDomain = takeQualifiedRef (c)
        case "types"       => elementTypeNames = parseNameList (c)

        case "rule"        => isRule = true
        case "pattern"     => ()   // its variables / conditions head themselves
        case "variables"   => variables = parseBlock (c, Set ("var")).map (_.asInstanceOf[Variable])
        case "conditions"  => conditions = parseBlock (c, Set ("con")).map (_.asInstanceOf[Condition])
        case "action"      => action = actionBody ()

        case "actor"       => isActor = true
        case "messageType" => messageType = parseRef (c.takeText ())
        case "start"       => start = actionBody ()
        case "message"     => message = actionBody ()
        case "signal"      => signal = actionBody ()

        case "codec"       => sys.error (s"Drake.parse: 'codec' aspect not yet parsed (next increment): $name")
        case other         => sys.error (s"Drake.parse: unexpected section '$other' in $name")
      }
    }

    // drake.dlt: the domain line's package also sets the type's own namePackage —
    // which is also what a BARE `from` / `modules` reference resolves against. The
    // resolution happens here rather than at the reference, because `from` precedes
    // `domain` on the surface and the owning package is not known until the whole
    // source has been read.
    def resolved (tn: TypeName) : TypeName =
      if (tn.namePackage.nonEmpty || operatorCarried (tn)) tn
      else TypeName (tn.name, domainName.namePackage, tn.typeParameters)

    TypeDefinition (
      _typeName = TypeName (name, domainName.namePackage, typeParameters),
      _dracoAspect = DracoAspect (
        _superDomain    = superDomain,
        _modules        = modules.map (resolved),
        _extensible     = extensible,
        _derivation     = derivation.result ().map (resolved),
        _elements       = elements,
        _factory        = factory,
        _globalElements = globalElements),
      _domainAspect = DomainAspect (domainName, elementTypeNames),
      _ruleAspect   = if (!isRule) RuleAspect.Null else RuleAspect (Pattern (variables, conditions), action),
      _actorAspect  = if (!isActor) ActorAspect.Null else ActorAspect (message, messageType, signal, start))
  }
}
