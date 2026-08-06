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
      s"${expression(ops.head)} parameters ${ops.tail.map(a => s"par ${inlineValue(a)}").mkString(" ")}"
    } else expression(value)

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
    case Some((name, v)) => s"par $name ${expression(v)}"
    case None            => s"par ${expression(arg)}"
  }

  private def parLines (indent: String, arg: Json) : Seq[String] = Expression.namedArgument(arg) match {
    case Some((name, v)) => valueLines(s"${indent}par $name", indent, v)
    case None            => valueLines(s"${indent}par", indent, arg)
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

  /** Split a type expression on top-level " => " function arrows (arrows nested
    * in [ ], ( ), { } belong to an inner type). One segment = no arrow. */
  private def splitTopArrow (s: String) : Seq[String] = {
    val parts = Seq.newBuilder[String]
    var depth, start, i = 0
    while (i < s.length) {
      s(i) match {
        case '[' | '(' | '{' => depth += 1
        case ']' | ')' | '}' => depth -= 1
        case ' ' if depth == 0 && s.startsWith(" => ", i) =>
          parts += s.substring(start, i); start = i + 4; i += 3
        case _ =>
      }
      i += 1
    }
    parts += s.substring(start)
    parts.result()
  }

  /** JSON valueType string -> drake type expression (drake.dlt VALUE-TYPES, inverted):
    * Seq[T] -> [T], Set[T] -> {T}, mutable.Set[T] -> mut {T}, F[A, B] -> F(A, B),
    * (A, B) tuple unchanged (components recursed), A => B arrows recursed on each
    * side, plain names verbatim. */
  private def typeExpression (valueType: String) : String = {
    val s = valueType.trim
    if (s.isEmpty) s
    else if (splitTopArrow(s).size > 1)
      splitTopArrow(s).map(typeExpression).mkString(" => ")
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
    if (splitTopArrow(valueType.trim).size > 1) s"($converted)" else converted
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
    val fromClause = da.derivation.map(typeRef) match {
      case Seq()           => ""
      case Seq("DracoType") => ""  // the universal root alone is inferable
      case refs            => s" from ${refs.mkString(" ")}"
    }
    // The drake surface names the bare concept (AddNaturalSequence); rule-/actor-ness
    // is carried by the ruleAspect/actorAspect, never by the type name.
    val header = s"type ${td.typeName.name}$typeParameters$fromClause"

    val modules =
      if (da.modules.isEmpty) Seq.empty
      else nameListLines("modules", da.modules.map(typeRef))
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
        "  factory" +: (parameters ++ body)
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
  // Increment 1 covers the plain-type template (header / modules / extensible /
  // elements / factory / globals / domain). The rule and actor aspects, and
  // value-position applications, are rejected loudly rather than silently dropped —
  // the same convention emit() uses for the codec aspect.

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
    def take (): Token         = { val t = tokens (index); index += 1; t }
    def takeText (): String         = take ().text
    def expect (text: String): Unit =
      if (at (text)) take ()
      else sys.error (s"Drake.parse: expected '$text' but found '${peek.getOrElse ("<end>")}'")
  }

  /** The value slot: every token up to the next reserved keyword, returned as the
    * raw source span so internal spacing survives verbatim. */
  private def span (c: Cursor) : String =
    if (c.exhausted || c.atReserved) ""
    else {
      val first = c.take ()
      var end   = first.end
      while (!c.exhausted && !c.atReserved) end = c.take ().end
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
    * anonymous-construction form), which `anonymousHead` supplies.
    *
    * Arguments are read as positional. The NAMED form `{"=": [name, value]}` renders
    * as `par <name> <value>`, which is not distinguishable on the surface from a
    * positional argument whose expression happens to be two words — so it waits for
    * the increment where it occurs (rule and actor bodies). Nothing is guessed here:
    * a named argument reaching this path fails the round-trip loudly. */
  private def parseValue (c: Cursor, anonymousHead: String) : Json = {
    val head = span (c)
    if (!c.at ("parameters")) leafValue (head)
    else {
      c.take ()
      val function = if (head.nonEmpty) head else anonymousHead
      if (function.isEmpty)
        sys.error ("Drake.parse: `parameters` with no function and no declared value type to supply one")
      val arguments = Seq.newBuilder[Json]
      while (c.at ("par")) { c.take (); arguments += parseValue (c, "") }
      Json.obj ("()" -> Json.fromValues (Json.fromString (function) +: arguments.result ()))
    }
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
    * mut {T} -> mutable.Set[T], F(A, B) -> F[A, B], tuples and arrows recursed,
    * plain names verbatim. A sole parenthesized member is the arrow slot
    * typeExpressionSlot wraps, not a one-tuple, so it unwraps. */
  private def parseTypeExpression (expr: String) : String = {
    val s = expr.trim
    if (s.isEmpty) s
    else if (splitTopArrow (s).size > 1) splitTopArrow (s).map (parseTypeExpression).mkString (" => ")
    else if (s.startsWith ("mut {") && s.endsWith ("}")) s"mutable.Set[${parseTypeExpression (s.substring (5, s.length - 1))}]"
    else if (s.startsWith ("[") && s.endsWith ("]"))     s"Seq[${parseTypeExpression (s.substring (1, s.length - 1))}]"
    else if (s.startsWith ("{") && s.endsWith ("}"))     s"Set[${parseTypeExpression (s.substring (1, s.length - 1))}]"
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
    * surface carries NO package for these (typeRef emits the bare name), so
    * namePackage comes back empty — see DrakeParseTest, which measures that loss. */
  private def parseRef (token: String) : TypeName = {
    val (name, typeParameters) = splitApplied (token)
    TypeName (name, _typeParameters = typeParameters)
  }

  /** A package-qualified reference (`domain draco Draco`, `super …`, `extensible …`):
    * space-separated package words, then the reference itself.
    *
    * The reserved set cannot bound this scan — a package word may BE a keyword, and
    * `extensible org apache pekko actor typed ExtensibleBehavior(T)` is the live
    * case (`actor`). Case bounds it instead: package words are lower-case, the
    * reference is upper-case, so the first upper-case word ends the path. */
  private def takeQualifiedRef (c: Cursor) : TypeName = {
    val words = Seq.newBuilder[String]
    while (!c.exhausted && c.peek.exists (_.headOption.exists (_.isLower))) words += c.takeText ()
    val namePackage = words.result ()
    if (c.exhausted) sys.error (s"Drake.parse: qualified reference '${namePackage.mkString (" ")}' has no type name")
    val ref = parseRef (c.takeText ())
    TypeName (ref.name, namePackage, ref.typeParameters)
  }

  /** A bracketed name list — `modules [ … ]` / `types [ … ]`. nameListLines always
    * brackets these (their members carry no bounding keyword). */
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

  /** One member of a list-block. `dyn` may open its own sub-block (parameters,
    * statements, `=` result); every other keyword is a single leaf. */
  private def parseMember (c: Cursor) : TypeElement = {
    c.takeText () match {
      case "mon" => Monadic (parseValue (c))
      case "con" => Condition (Seq.empty, parseValue (c))
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
    * Increment 1: the plain-type template. */
  def parse (source: String) : TypeDefinition = {
    val c = new Cursor (source, lex (source))
    c.expect ("type")
    val (name, typeParameters) = splitApplied (c.takeText ())
    val derivation = Seq.newBuilder[TypeName]
    if (c.at ("from")) {
      c.take ()
      while (!c.exhausted && !c.atReserved) derivation += parseRef (c.takeText ())
    }

    var modules          = Seq.empty[TypeName]
    var extensible       = TypeName.Null
    var elements         = Seq.empty[TypeElement]
    var factory          = Factory.Null
    var globalElements   = Seq.empty[BodyElement]
    var superDomain      = TypeName.Null
    var domainName       = TypeName.Null
    var elementTypeNames = Seq.empty[String]

    while (!c.exhausted) {
      c.takeText () match {
        case "modules"    => modules = parseNameList (c).map (parseRef)
        case "extensible" => extensible = takeQualifiedRef (c)
        case "elements"   => elements = parseBlock (c, declarationKeywords)
        case "globals"    => globalElements = parseBlock (c, declarationKeywords).map (_.asInstanceOf[BodyElement])
        case "factory"    =>
          factory = Factory (
            factoryValueType (name, typeParameters),
            parseSection (c, "parameters", Set ("par")).map (_.asInstanceOf[Parameter]),
            parseSection (c, "body", declarationKeywords).map (_.asInstanceOf[BodyElement]))
        case "domain"     => domainName = takeQualifiedRef (c)
        case "super"      => superDomain = takeQualifiedRef (c)
        case "types"      => elementTypeNames = parseNameList (c)
        case aspect @ ("rule" | "actor" | "codec") =>
          sys.error (s"Drake.parse: '$aspect' aspect not yet parsed (next increment): $name")
        case other => sys.error (s"Drake.parse: unexpected section '$other' in $name")
      }
    }

    // drake.dlt: the domain line's package also sets the type's own namePackage.
    TypeDefinition (
      _typeName = TypeName (name, domainName.namePackage, typeParameters),
      _dracoAspect = DracoAspect (
        _superDomain    = superDomain,
        _modules        = modules,
        _extensible     = extensible,
        _derivation     = derivation.result (),
        _elements       = elements,
        _factory        = factory,
        _globalElements = globalElements),
      _domainAspect = DomainAspect (domainName, elementTypeNames))
  }
}
