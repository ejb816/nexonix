package draco

import io.circe.Json
import draco.TypeForm.ValueTypeText

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
  * imperative engine) with the generator transform domains (GenScala, GenDrake). */
object Drake {
  // --- Emission (JSON TypeDefinition -> .drake surface) ---
  //
  // Where DracoGenerator.expression renders a value tree to Scala's spelling, the
  // expression() below renders the same tree to its drake surface (Haskell forms —
  // \ lambda, -> arrow). emit() writes the drake.dlt TEMPLATE: bare
  // `factory`, `globals` keyword, keyword blocks unbracketed with brackets on the
  // OPENER (a dyn-with-body delimits its own body), `from` omitted when the
  // derivation is DracoType alone. Covers the plain-type template + the rule and
  // actor aspects; the codec aspect is a later increment and is rejected loudly.

  /** Render a TypeElement `value` to its drake surface form. Same tree contract
    * as DracoGenerator.expression: a string is host-opaque source text passed through
    * verbatim; {op: [operands]} applies the operator. Haskell-form spellings:
    * "->" renders " -> ", "\" renders \p1 p2 -> body. A tree in a String-typed slot needs no quoting here —
    * the drake surface carries the expression itself (Action.drake's unquoted
    * arrow), quoting is the ScalaTarget projection's concern.
    *
    * TOTAL over the tree language, which includes the two ARGUMENT-POSITION nodes: a
    * tuple "(,)" and a named argument "=". A call is `f(a, b)` — positional arguments
    * first, then `name:value` (drake.dlt APPLICATION SURFACE) — and it is the ONLY
    * spelling: since 2026-09-16 there is no multi-line `parameters` / `par` form to
    * unfold into, so this flat rendering is what every value position gets. The tree
    * keeps `=` as its named-argument key; the surface spells the colon. */
  def expression (value: Json) : String = render (value, None, 0, 1)

  /** Render `value` standing as operand `index` of `count` under the node `parent` (None at
    * the top), with the MINIMAL parenthesization (2026-09-21): a tree is already
    * associated, so a pair is written only where the surface would otherwise read the tree
    * differently — see `parenthesized`. */
  private def render (value: Json, parent: Option[String], index: Int, count: Int) : String = {
    if (value == null || value.isNull) ""
    else value.asString.getOrElse {
      value.asObject.map(_.toList) match {
        case Some((op, operands) :: Nil) =>
          val all  = operands.asArray.getOrElse(Vector(operands))
          val args = all.zipWithIndex.map { case (a, i) => render(a, Some(op), i, all.size) }
          val text = op match {
            case "."        => args.mkString(".")
            case "->"       => args.mkString(" -> ")
            case "()"       => s"${args.head}(${args.tail.mkString(", ")})"
            case "\\"       => s"\\${args.init.mkString(" ")} -> ${args.last}"
            // no "if": a conditional is `ifThenElse` on a Presence, a call like any other
            // (retired from the tree language 2026-09-21 with Value's last two)
            case "(,)"      => args.mkString("(", ", ", ")")
            case "[]"       => args.mkString("[", ", ", "]")   // a sequence literal, `[]` when empty
            case "{}"       => args.mkString("{", ", ", "}")   // a set literal
            case "="        => s"${args(0)}:${args(1)}"
            // every declared infix operator (the fixity table) spells itself, spaced
            case _ if fixity.contains(op) => args.mkString(s" $op ")
            case _          => sys.error(s"Drake.expression: unknown operator '$op' in ${value.noSpaces}")
          }
          if (parent.exists(p => parenthesized(p, op, index, count))) s"($text)" else text
        case _ => sys.error(s"Drake.expression: unrenderable value ${value.noSpaces}")
      }
    }
  }

  /** Whether the node `child`, operand `index` of `count` under `parent`, needs its pair on
    * the DRAKE surface — by drake's own fixity table, since the parse reads it back by the
    * same table. Under an infix parent: a looser child always; an equally tight child on
    * the side its associativity does not cover (left-grouping: any but the first; right-
    * grouping: any but the last; non-associative: either); a lambda only when something
    * follows it, since otherwise it runs to the end as the arrow's fixity says. Under a
    * path or an application: the receiver or head when it is any of those. Arguments,
    * tuple and literal members, a lambda's own body: never. */
  private def parenthesized (parent: String, child: String, index: Int, count: Int) : Boolean =
    (fixity.get(parent), fixity.get(child)) match {
      case (Some(p), Some(c)) =>
        c.precedence < p.precedence ||
          (c.precedence == p.precedence && (p.associativity match {
            case 'l' => index > 0
            case 'r' => index < count - 1
            case _   => true
          }))
      case (Some(_), None) => child == "\\" && index < count - 1
      case (None, _)       => (parent == "." || parent == "()") && index == 0 &&
                                (fixity.contains(child) || child == "\\")
    }

  /** Render a value into the slot after `prefix`, on ONE line. Every value form — a
    * leaf, a call, a tuple, a `++` run — is a single line now that a call is one
    * token (drake.dlt APPLICATION SURFACE): its parentheses bound it, so nothing has
    * to unfold onto chain lines, bracket itself inside a `++`, or spell a `par` list.
    * The empty-collection defaults collapse to their surface forms (defaultValue). */
  private def valueLine (prefix: String, value: Json) : String = {
    val rendered = defaultValue(expression(value))
    if (rendered.isEmpty) prefix else s"$prefix $rendered"
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
  private def typeExpressionSlot (valueType: Json) : String =
    if (TypeForm.isTree(valueType)) {
      val rendered = drakeType(valueType)
      if (TypeForm.node(valueType).exists(_._1 == "->")) s"($rendered)" else rendered
    } else {
      val text      = valueType.text
      val converted = typeExpression(text)
      if (splitTopArrow(text.trim, scalaArrow).size > 1) s"($converted)" else converted
    }

  /** A type-form TREE on the drake surface: TypeForm.neutral's spelling, except that
    * a string LEAF — host text, the `mut {T}` tail — is respelled through
    * typeExpression as any string value type is. The two renderers agree on every
    * node; only the leaf differs, and only because the surface owns the respelling. */
  private def drakeType (form: Json) : String = TypeForm.node(form) match {
    case None                          => typeExpression(form.text)
    case Some(("->", Vector(s, t)))    =>
      val left = drakeType(s)
      s"${if (TypeForm.node(s).exists(_._1 == "->")) s"($left)" else left} -> ${drakeType(t)}"
    case Some(("(,)", members))        => members.map(drakeType).mkString("(", ", ", ")")
    case Some(("[]", Vector(a)))       => s"[${drakeType(a)}]"
    case Some(("{}", members))         => members.map(drakeType).mkString("{", ", ", "}")
    case Some(("[]+", Vector(a)))      => s"[${drakeType(a)}]+"
    case Some(("{}+", members))        => members.map(drakeType).mkString("{", ", ", "}+")
    case Some(("()", f +: arguments))  => s"${f.text}(${arguments.map(drakeType).mkString(", ")})"
    case Some((op @ ("<:" | ">:"), Vector(p, b))) => s"${drakeType(p)} $op ${drakeType(b)}"
    case Some((op, _)) => sys.error(s"Drake.emit: not a type form: '$op' in ${form.noSpaces}")
  }

  /** The value-type slot as the surface spells it, for a string or a tree — public
    * so DrakeParseTest can compare the two eras by their one surface. */
  def typeSurface (valueType: Json) : String = typeExpressionSlot(valueType)

  /** Element name to drake surface: a method type-parameter rides the name via
    * the ( ) convention — JSON "updated[V1 >: V]" -> drake updated(V1 >: V). */
  private def elementName (name: String) : String = {
    val idx = name.indexOf('[')
    if (idx < 0 || !name.endsWith("]")) name
    else name.substring(0, idx) + "(" + name.substring(idx + 1, name.length - 1) + ")"
  }

  /** A TYPE PARAMETER on the surface: a tree spells as any type form does (the parser
    * builds one for every header parameter and reference argument since 2026-09-17);
    * a string — the JSON corpus until it re-canonicalizes — spells VERBATIM, as a
    * TypeName's parameters have always been written. */
  private[draco] def typeParameterSurface (parameter: Json) : String =
    if (TypeForm.isTree(parameter)) drakeType(parameter) else parameter.text

  /** TypeName reference on the drake surface: name with type parameters in the
    * ( ) type-application form — Map[K, V]'s TypeName -> Map(K, V). */
  private def typeRef (tn: TypeName) : String =
    if (tn.typeParameters.isEmpty) tn.name
    else s"${tn.name}(${tn.typeParameters.map(typeParameterSurface).mkString(", ")})"

  /** The universal root, as TypeLoader.rooted spells it. */
  private def isRoot (tn: TypeName) : Boolean =
    tn.name == "DracoType" && tn.namePackage == Seq ("draco")

  /** A dyn-with-body opens its own sub-block; its container needs [ ]. */
  private def opensBlock (element: TypeElement) : Boolean = element match {
    case d: Dynamic => d.parameters.nonEmpty || d.body.nonEmpty
    case _: Case    => true
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
    * value-type; a `con`'s value is its boolean expression tree. A construction of
    * the declared value type spells its head like any other call (`fix report T T(…)`):
    * the elision that once dropped the head before `parameters` went with that form,
    * because `(…)` alone is a tuple. */
  private def leafLines (indent: String, keyword: String, element: TypeElement) : Seq[String] =
    element match {
      case _: Monadic | _: Condition => Seq(valueLine(s"$indent$keyword", element.value))
      case e => Seq(valueLine(s"$indent$keyword ${elementName(e.name)} ${typeExpressionSlot(e.valueType)}", e.value))
    }

  /** The element's keyword, prefixed `now ` when it is marked strict (drake.dlt
    * EVALUATION): the override reads before the keyword, as it is parsed. */
  private def keyword (element: TypeElement) : String = (if (element.now) "now " else "") + bareKeyword (element)

  private def bareKeyword (element: TypeElement) : String = element match {
    case _: Fixed     => "fix"
    case _: Mutable   => "mut"
    case _: Dynamic   => "dyn"
    case _: Parameter => "par"
    case _: Local     => "loc"
    case _: Monadic   => "mon"
    case _: Variable  => "var"
    case _: Condition => "con"
    case _: Case      => "case"
    case e            => sys.error(s"Drake.emit: no drake keyword for element '${e.name}' (${e.getClass.getSimpleName})")
  }

  /** Render one element at `level` (2 spaces per level). A leaf renders one line;
    * a dyn-with-body BRACKETS its own body (drake.dlt BRACKETS) — its parameters
    * block and its body, whose element named `value` is the result.
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
    else element match {
      case c: Case => caseLines(c, indent, level)
      case d: Dynamic =>
      val header = s"$indent${keyword(d)} ${elementName(d.name)} ${typeExpressionSlot(d.valueType)} ["
      val parameters =
        if (d.parameters.isEmpty) Seq.empty
        else sectionLines("parameters", d.parameters, level + 1)
      // The body is a `body` section like the factory's, and the RESULT is its element
      // named `value` (drake.dlt DYN-WITH-BODY) — a block dyn never carries a value of
      // its own; the retired `=` marker was that field made visible.
      if (!d.value.isNull && expression(d.value).nonEmpty)
        sys.error(s"Drake.emit: block dyn '${d.name}' carries a value outside its body — the result is the body element named `value`")
      val body =
        if (d.body.isEmpty) Seq.empty
        else sectionLines("body", d.body, level + 1)
      (header +: (parameters ++ body)) :+ s"$indent]"
      case e => sys.error(s"Drake.emit: '${e.name}' opens a block but has no block form (${e.getClass.getSimpleName})")
    }
  }

  /** A case-branch (drake.dlt CASE-BRANCH) brackets itself: `case`, an optional
    * binder, an optional branch type, `[`, its statements, `]`. Its result, when it
    * has one, is its statement named `value`, as in a dyn-with-body. Case alone tells
    * the four head forms apart — a lower-case token is the binder, an upper-case one
    * the type — so an absent binder or type leaves no marker on the surface. */
  private def caseLines (c: Case, indent: String, level: Int) : Seq[String] = {
    val head = Seq(s"$indent${keyword(c)}") ++
      (if (c.name.nonEmpty) Seq(elementName(c.name)) else Seq.empty) ++
      (if (c.valueType.text.nonEmpty) Seq(typeExpressionSlot(c.valueType)) else Seq.empty)
    if (!c.value.isNull && expression(c.value).nonEmpty)
      sys.error(s"Drake.emit: case-branch carries a value outside its body — the result is the body element named `value`")
    val statementLines = c.body.flatMap(elementLines(_, level + 1))
    ((head :+ "[").mkString(" ") +: statementLines) :+ s"$indent]"
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
      else s"(${td.typeName.typeParameters.map(typeParameterSurface).mkString(", ")})"

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
      if (tn.namePackage == td.typeName.namePackage) typeRef(tn)
      else (tn.namePackage :+ typeRef(tn)).mkString(" ")

    // The universal root is spelled only where it is NOT reconstructable, which is
    // drake.dlt INFERENCE applied to the one reference that is never authored:
    // TypeLoader.rooted appends DracoType to any definition carrying no draco-domain
    // parent, so the root alone — and the root beside a FOREIGN parent, which is
    // Dictionary — comes back on its own. Beside a draco parent it would not, so
    // there it stays on the surface.
    // A derivation entry is a NAME (a draco parent) or a TYPE FORM (a foreign parent), and the
    // surface spells each as what it is: the name by reference, the form by drakeType.
    val parents      = DracoAspect.parents(da)
    val rootRestored = !parents.exists(tn => !isRoot(tn))
    val spelled      = da.derivation.flatMap { j =>
      if (j.hcursor.downField("name").succeeded) j.as[TypeName].toOption.filterNot(tn => rootRestored && isRoot(tn)).map(reference)
      else Some(drakeType(j))
    }
    val fromClause   = if (spelled.isEmpty) "" else s" from ${spelled.mkString(" ")}"
    // The drake surface names the bare concept (AddNaturalSequence); rule-/actor-ness
    // is carried by the ruleAspect/actorAspect, never by the type name.
    // The NAMELESS domain (draco's default package, 2026-09-20) has no header: its anchor
    // definition is the one line `domain`.
    val header = if (td.typeName.name.isEmpty) "" else s"type ${td.typeName.name}$typeParameters$fromClause"

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
      if (da.factory.valueType.text.isEmpty) Seq.empty
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
          if (typeSurface(da.factory.valueType) == typeSurface(factoryValueType(td.typeName.name, td.typeName.typeParameters))) "  factory"
          else s"  factory ${typeExpressionSlot(da.factory.valueType)}"
        head +: (parameters ++ body)
      }
    val globals =
      if (da.globalElements.isEmpty) Seq.empty
      else sectionLines("globals", da.globalElements, 1)

    // Section layout and aspect mapping are definition-backed. Type-parameter
    // spelling remains an explicit dependency on this target's type-form renderer.
    val domainText = draco.gendrake.DomainAspectOf(td, typeParameterSurface(_)).value
    val domain = if (domainText.isEmpty) Seq.empty else Seq(domainText)

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
          else Seq(s"  messageType ${reference(aa.messageType)}")
        "actor" +: (messageType ++ block("start", aa.start) ++ block("message", aa.message) ++ block("signal", aa.signal))
      }

    ((Seq(header).filter(_.nonEmpty) ++ (modules ++ extensible ++ elements ++ factory ++ globals)) ++ domain ++ rule ++ actor).mkString("", "\n", "\n")
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
  // value-position calls `f(a, b)` with positional then named arguments, tuples, and
  // `++` runs. The codec aspect is the remaining increment and is rejected loudly
  // rather than silently dropped — the same convention emit() uses for it.

  // Plain vals: unlike the App companions in the model, `Drake` is a bare object, so
  // there is no DelayedInit to defer them past first use.
  private val memberKeywords: Set[String] =
    Set ("fix", "mut", "dyn", "par", "loc", "mon", "var", "con")

  /** The member keywords that can open a STATEMENT inside a dyn-with-body — the
    * bindings and effects, and a case-branch (drake.dlt CASE-BRANCH), which is
    * admitted in a dyn body and in an actor's `message`, nowhere else. */
  private val statementKeywords: Set[String] = Set ("fix", "mut", "loc", "mon", "case", "now")

  /** Every token that bounds a member — the reserved words a value slot stops at.
    * drake.dlt's whitespace-insignificance rests on this set: a value runs until
    * the next reserved keyword, never until end-of-line. */
  private val reserved: Set[String] =
    memberKeywords ++ Set (
      "type", "from", "domain", "super", "source", "target", "types", "rule", "actor", "codec",
      "modules", "extensible", "elements", "factory", "parameters", "body", "globals",
      "pattern", "action", "variables", "conditions",
      "start", "message", "signal", "messageType",
      // `case` (drake.dlt CASE-BRANCH) — reserved from 3b on, once the last definition
      // carrying the word inside host-opaque text (TypeName.equals) had converted.
      "case",
      // No operator is reserved. `++` was, from 2026-09-10 until the operator layer landed
      // (2026-09-21): reserving it closed the operand before it at the cursor, which is now
      // `value`'s job for every operator alike — a span runs to the next KEYWORD and the
      // operators inside it are read by fixity. `=` is not here either: the dyn result
      // marker retired with a5d2f5b and the named-argument marker with the call syntax.
      // `now` (drake.dlt EVALUATION) — the strictness override, a modifier before the
      // element's own keyword, as `mut` is a keyword before a name.
      "now",
      "[", "]")

  /** One drake token: its source text and its span.
    *
    * A bracketed group GLUES onto the word before it when no space separates them
    * (`Map(K, V)`, `classOf[Main].getResource("/")`), and whitespace INSIDE a group
    * never ends the token, so a type expression, a host-opaque value and — since
    * 2026-09-16 — a whole call `f(a, b)` each read as ONE token however many lines
    * the author spreads the arguments over; a quoted literal is skipped whole, so
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
    case "[]"  => Json.obj ("[]" -> Json.arr ())   // the empty sequence, neutral (2026-09-18)
    case "{}"  => Json.obj ("{}" -> Json.arr ())   // the empty set
    case other => Json.fromString (other)
  }

  /** The value slot: every token up to the next reserved keyword, read as ONE
    * expression (drake.dlt EXPRESSIONS) — its operators by fixity, a lambda to the end. */
  private def parseValue (c: Cursor) : Json = value (span (c))

  /** True iff the bracket opening `token` closes at its last character — the token is one
    * group and nothing else. Quoted text is skipped whole. */
  private def closesWhole (token: String) : Boolean = {
    var depth = 0; var i = 0; var quoted = false
    while (i < token.length) {
      val ch = token (i)
      if (quoted) { if (ch == '"' && token (i - 1) != '\\') quoted = false }
      else ch match {
        case '"' => quoted = true
        case '(' | '[' | '{' => depth += 1
        case ')' | ']' | '}' => depth -= 1; if (depth == 0 && i != token.length - 1) return false
        case _ =>
      }
      i += 1
    }
    depth == 0
  }

  /** Value text as it stands INSIDE a call's parentheses, a tuple, a collection literal,
    * or a top-level span. The parentheses bound it, so the reserved words that bound a
    * top-level slot are ordinary text here — `LazyList.from(start, step)` applies to a
    * parameter named `start`, not to the actor block. What IS read is the OPERATOR
    * LAYER (drake.dlt EXPRESSIONS, 2026-09-21): a run of operands separated by the
    * declared infix operators, reassociated by fixity, and a lambda `\p1 p2 -> body`
    * whose body runs to the end of the text. */
  private def value (text: String) : Json = expression (lex (text), text)

  /** The fixity of every declared infix operator — Haskell's Prelude table, since drake
    * takes Haskell's semantics: a precedence, and whether a run of one precedence
    * groups left, right, or not at all (a chained non-associative operator is an error
    * at parse, as in Haskell). The arrow sits BELOW every other row, so a lambda body
    * takes everything to its right — the arrow's fixity is what makes a lambda's scope
    * unambiguous (Dev, 2026-09-21). `++`, `&&` and `||` are FLAT VARIADIC in the tree,
    * one node for the whole run; the others stay binary. */
  private final case class Fixity (precedence: Int, associativity: Char)
  private val fixity: Map[String, Fixity] = Map (
    "*"  -> Fixity (7, 'l'), "/"  -> Fixity (7, 'l'), "%"  -> Fixity (7, 'l'),
    "+"  -> Fixity (6, 'l'), "-"  -> Fixity (6, 'l'),
    "++" -> Fixity (5, 'r'),
    "==" -> Fixity (4, 'n'), "!=" -> Fixity (4, 'n'),
    "<"  -> Fixity (4, 'n'), "<=" -> Fixity (4, 'n'), ">"  -> Fixity (4, 'n'), ">=" -> Fixity (4, 'n'),
    "&&" -> Fixity (3, 'r'),
    "||" -> Fixity (2, 'r'),
    "->" -> Fixity (0, 'r'))
  private val flattened: Set[String] = Set ("++", "&&", "||")

  /** One expression over a token run. The run is split FLAT at every operator token; a
    * lambda — a token opening with `\` in operand position — takes every token after it
    * as its parameters and body, so it is always the LAST operand. The run trees only
    * when every operand is ONE token (or the lambda). An operand of several tokens is a
    * form the parser does not read — a host `if`, a `new`, a block — and treeing around
    * it would hand the generator a tree with the wrong scope, so such a run stays the
    * raw leaf it was, its spacing intact, and the loss report measures it. */
  private def expression (tokens: Vector[Token], source: String) : Json = {
    if (tokens.isEmpty) return Json.Null
    def raw = leafValue (source.substring (tokens.head.start, tokens.last.end))
    val lambdaAt = tokens.indexWhere (_.text.startsWith ("\\"))
    val lambda   =
      if (lambdaAt >= 0 && (lambdaAt == 0 || fixity.contains (tokens (lambdaAt - 1).text)))
        Some (lambdaOf (tokens.drop (lambdaAt), source))
      else None
    val head      = if (lambda.isDefined) tokens.take (lambdaAt) else tokens
    val runs      = Vector.newBuilder[Vector[Token]]
    val operators = Vector.newBuilder[String]
    var run       = Vector.newBuilder[Token]
    head.foreach { t =>
      if (fixity.contains (t.text)) { runs += run.result (); operators += t.text; run = Vector.newBuilder[Token] }
      else run += t
    }
    runs += run.result ()
    val allRuns = runs.result ()
    val ops     = operators.result ()
    val operands: Option[Vector[Json]] = lambda match {
      case Some (l) =>
        // the head ends in an operator (or is empty), so its final run is empty and the
        // lambda stands in that operand's place
        val before = allRuns.init
        if (before.exists (_.size != 1)) None else Some (before.map (r => treed (r.head.text)) :+ l)
      case None =>
        if (ops.isEmpty) return (if (allRuns.head.size == 1) treed (allRuns.head.head.text) else raw)
        if (allRuns.exists (_.size != 1)) None else Some (allRuns.map (r => treed (r.head.text)))
    }
    operands.fold (raw) (os => resolve (os, ops))
  }

  /** A lambda, `\p1 p2 -> body` (drake.dlt EXPRESSIONS, Haskell form): the `\` node,
    * parameters then body. The `\` glues to the first parameter or stands alone; a
    * parameter is one token, so a host-typed binder `(x: T)` is carried as the leaf it
    * is. The body is read as an expression in its own right. */
  private def lambdaOf (tokens: Vector[Token], source: String) : Json = {
    def text  = source.substring (tokens.head.start, tokens.last.end)
    val arrow = tokens.indexWhere (_.text == "->")
    if (arrow < 0) sys.error (s"Drake.parse: a lambda has no arrow in '$text'")
    val params = (tokens.head.text.drop (1) +: tokens.slice (1, arrow).map (_.text)).filter (_.nonEmpty)
    val body   = tokens.drop (arrow + 1)
    if (params.isEmpty || body.isEmpty) sys.error (s"Drake.parse: a lambda needs a parameter and a body in '$text'")
    Json.obj ("\\" -> Json.fromValues (params.map (Json.fromString) :+ expression (body, source)))
  }

  /** Reassociate a flat operator run by fixity — Haskell's resolution, done by
    * precedence climbing over the operands and the operators between them. A run of a
    * flattened operator becomes one node; two operators of one precedence that do not
    * associate with each other (`a == b == c`) are an error, as in Haskell. */
  private def resolve (operands: Vector[Json], operators: Vector[String]) : Json = {
    var next = 0   // the operator about to be read; operands(next) is the operand before it
    def climb (floor: Int) : Json = {
      var left = operands (next)
      var last = Option.empty[Fixity]
      while (next < operators.length && fixity (operators (next)).precedence >= floor) {
        val op = operators (next)
        val f  = fixity (op)
        if (last.exists (l => l.precedence == f.precedence && (l.associativity == 'n' || f.associativity == 'n' || l.associativity != f.associativity)))
          sys.error (s"Drake.parse: '$op' does not associate with the operator before it; parenthesize")
        next += 1
        val right = climb (if (f.associativity == 'r') f.precedence else f.precedence + 1)
        left = infixNode (op, left, right)
        last = Some (f)
      }
      left
    }
    climb (0)
  }

  private def infixNode (op: String, left: Json, right: Json) : Json = {
    val rightRun = if (flattened (op)) Expression.node (right).filter (_._1 == op).map (_._2) else None
    Json.obj (op -> Json.fromValues (left +: rightRun.getOrElse (Vector (right))))
  }

  /** Tree a single value token (drake.dlt APPLICATION SURFACE). A token ending in a
    * `( )` group APPLIES what precedes the group to what it holds: `f(a, b)` is the
    * head `f` applied to `a` and `b`, positional arguments first and then `name:value`
    * ones, each name at most once. The whole call is one token because brackets glue
    * to the word before them and whitespace inside them never ends a token, so a call
    * bounds itself — nothing after it can be claimed by it, and nothing inside it
    * needs a bracket. A group with NOTHING before it is a tuple when it holds a depth-0
    * comma and a leaf otherwise (`(x)` is a parenthesized opaque expression, not a
    * one-tuple). A token with no closing group is a path (a.f(x).g selects on a call)
    * or a leaf. */
  private def treed (token: String) : Json = {
    // A COLLECTION LITERAL (2026-09-20): a token that is one `[ ]` or `{ }` group with nothing
    // before it — `[name]`, `[x, y]`, `{a, b}` — is the sequence / set literal, the `[]` / `{}`
    // value node with its members read as values (the empty ones already parse to it).
    if (token.length >= 2 && "[{".contains (token.head) && closesWhole (token)) {
      val key    = if (token.head == '[') "[]" else "{}"
      val inside = token.substring (1, token.length - 1).trim
      val items  = if (inside.isEmpty) Seq.empty[Json] else splitDepthZero (inside, ',').map (m => value (m.trim))
      return Json.obj (key -> Json.fromValues (items))
    }
    val open = groupStart (token)
    if (open > 0) {
      val head   = token.substring (0, open)
      val inside = token.substring (open + 1, token.length - 1).trim
      val args   = if (inside.isEmpty) Seq.empty[Json] else splitDepthZero (inside, ',').map (a => argument (a.trim))
      val names  = args.flatMap (Expression.namedArgument).map (_._1)
      names.diff (names.distinct).headOption.foreach (n =>
        sys.error (s"Drake.parse: argument '$n' is named more than once in $token"))
      val firstNamed = args.indexWhere (a => Expression.namedArgument (a).isDefined)
      if (firstNamed >= 0 && args.drop (firstNamed).exists (a => Expression.namedArgument (a).isEmpty))
        sys.error (s"Drake.parse: a positional argument follows a named one in $token")
      val function = if (groupStart (head) > 0) treed (head) else path (head).getOrElse (Json.fromString (head))
      Json.obj ("()" -> Json.fromValues (function +: args))
    } else if (open == 0) {
      val inside  = token.substring (1, token.length - 1)
      val members = splitDepthZero (inside, ',')
      if (members.size >= 2) Json.obj ("(,)" -> Json.fromValues (members.map (m => value (m.trim))))
      else {
        // A PARENTHESIZED SUB-EXPRESSION (2026-09-21): the group holds one expression. When
        // the parser reads it as a tree the parentheses DISSOLVE — a tree is already
        // associated, so the pair is not information; each renderer writes back the minimal
        // pair its own target needs (`parenthesized`). A group the parser reads as a leaf
        // keeps its parentheses, because there they may be the host's.
        val inner = value (inside.trim)
        if (inner.isObject) inner else leafValue (token)
      }
    } else path (token).getOrElse (leafValue (token))
  }

  /** One argument of a call: `name:value` names it — `{"=": [name, value]}`, the tree's
    * named-argument node, whose surface marker is now the colon — and anything else is
    * positional. The name is an identifier immediately before the colon (whitespace
    * around the colon is insignificant, as everywhere); a host-opaque ascription in
    * argument position (`x: Int`) would read the same way, and the corpus has none. */
  private def argument (text: String) : Json = text match {
    case namedArgument (name, rest) => Json.obj ("=" -> Json.arr (Json.fromString (name), value (rest.trim)))
    case _                          => value (text)
  }

  private val namedArgument = """(?s)^([A-Za-z_][A-Za-z0-9_]*)\s*:\s*(.+)$""".r

  /** A `.`-path whose elements may themselves be calls: `a.f(x).g` selects `g` on the
    * call `f(x)` on `a`. Split at depth-0 dots, the LAST element that is a call closes
    * the receiver, which is treed on its own, and the elements after it are plain
    * selections. None when no element is a call — a plain path is a leaf and stays
    * the one string it was written as (`0.0` included). This is what a chain is now:
    * the nested `()` / `.` spine that the retired `.member parameters …` lines used to
    * spell one call per line. */
  private def path (text: String) : Option[Json] = {
    val elements = splitDepthZero (text, '.')
    val last     = elements.lastIndexWhere (e => groupStart (e) > 0)
    if (last < 0) None
    else Some (Json.obj ("." -> Json.fromValues (
      treed (elements.take (last + 1).mkString (".")) +: elements.drop (last + 1).map (Json.fromString))))
  }

  /** The index of the `(` that the token's final `)` closes; -1 when the token does
    * not end in a group. Scanned from the end, so `f(a)(b)` applies the call `f(a)`,
    * and 0 when the group is the whole token. String literals are skipped whole. */
  private def groupStart (token: String) : Int =
    if (!token.endsWith (")")) -1
    else {
      var depth = 0
      var i     = token.length - 1
      var found = -1
      while (found < 0 && i >= 0) {
        token (i) match {
          case '"' =>
            i -= 1
            while (i >= 0 && !(token (i) == '"' && (i == 0 || token (i - 1) != '\\'))) i -= 1
          case ')' | ']' | '}' => depth += 1
          case '(' | '[' | '{' => depth -= 1; if (depth == 0 && token (i) == '(') found = i
          case _               =>
        }
        i -= 1
      }
      found
    }

  /** Split `text` at every depth-0 occurrence of `separator`, where depth counts the
    * three bracket pairs and a string literal is skipped whole — the argument commas
    * of a call, the dots of a path. Pieces are returned raw (untrimmed). */
  private def splitDepthZero (text: String, separator: Char) : Seq[String] = {
    val pieces  = Seq.newBuilder[String]
    val current = new StringBuilder
    var depth   = 0
    var i       = 0
    while (i < text.length) {
      text (i) match {
        case '"' =>
          val start = i
          i += 1
          while (i < text.length && text (i) != '"') { if (text (i) == '\\') i += 1; i += 1 }
          current.append (text.substring (start, math.min (i + 1, text.length)))
        case c @ ('(' | '[' | '{') => depth += 1; current.append (c)
        case c @ (')' | ']' | '}') => depth -= 1; current.append (c)
        case c if c == separator && depth == 0 => pieces += current.result (); current.clear ()
        case c => current.append (c)
      }
      i += 1
    }
    (pieces += current.result ()).result ()
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
  private def takeValueType (c: Cursor) : Json = typeForm (c.takeText ())

  /** A drake type expression as a TYPE-FORM TREE (drake.dlt VALUE-TYPES; TypeForm for
    * the encoding). Read outside-in: a top-level arrow is Morphic and groups to the
    * RIGHT (`A -> B -> C` is A -> (B -> C), Haskell's); a top-level bound is its leaf;
    * `[T]` / `{T}` / `{K, V}` are the bracket nodes `[]` / `{}` (neutral, no host head); a parenthesized
    * list is Objective, a parenthesized single member the arrow slot's wrapper;
    * `F(A, B)` applies; a bare name is Atomic and stays the string it is. The one
    * form still carried as HOST TEXT is `mut {T}` — `mut` belongs to the element,
    * not the type (agreed 2026-09-13, not yet built), so its spelling is the
    * host's until then, exactly as parseTypeExpression wrote it. */
  private def typeForm (expr: String) : Json = {
    val s = expr.trim
    val arrow = splitTopArrow (s, drakeArrow)
    if (arrow.size > 1) Json.obj ("->" -> Json.arr (typeForm (arrow.head), typeForm (arrow.tail.mkString (drakeArrow))))
    else boundForm (s).getOrElse {
      // A trailing `+` is the MUTABLE collection (2026-09-20): `[T]+`, `{T}+`, `{K, V}+`, the
      // `+` riding the node key. `mut {T}` (host text) retired with it.
      if (s.startsWith ("[") && s.endsWith ("]+")) Json.obj ("[]+" -> Json.arr (typeForm (s.substring (1, s.length - 2))))
      else if (s.startsWith ("{") && s.endsWith ("}+")) Json.obj ("{}+" -> Json.fromValues (splitTypeArguments (s.substring (1, s.length - 2)).map (typeForm)))
      // The collection sugar is carried NEUTRALLY (2026-09-18): the bracket is the node
      // key — `[T]` is {"[]": [T]}, `{T}` / `{K, V}` are {"{}": [...]} at two arities —
      // and no target's name (Seq, Set, Map) enters the carrier; each target spells it.
      else if (s.startsWith ("[") && s.endsWith ("]")) Json.obj ("[]" -> Json.arr (typeForm (s.substring (1, s.length - 1))))
      else if (s.startsWith ("{") && s.endsWith ("}")) {
        val members = splitTypeArguments (s.substring (1, s.length - 1))
        Json.obj ("{}" -> Json.fromValues (members.map (typeForm)))
      }
      else if (s.startsWith ("(") && s.endsWith (")")) {
        val members = splitTypeArguments (s.substring (1, s.length - 1))
        if (members.size == 1) typeForm (members.head)
        else Json.obj ("(,)" -> Json.fromValues (members.map (typeForm)))
      }
      else {
        val idx = s.indexOf ('(')
        if (idx <= 0 || !s.endsWith (")")) Json.fromString (s)
        else application (s.substring (0, idx), splitTypeArguments (s.substring (idx + 1, s.length - 1)))
      }
    }
  }

  private def application (head: String, arguments: Seq[String]) : Json =
    Json.obj ("()" -> Json.fromValues (Json.fromString (head) +: arguments.map (typeForm)))

  /** `p <: b` / `p >: b` at the top level: a bound leaf, parameter first. */
  private def boundForm (s: String) : Option[Json] =
    Seq (" <: ", " >: ").view.map (op => (op, splitTopArrow (s, op))).collectFirst {
      case (op, Seq (p, b)) => Json.obj (op.trim -> Json.arr (typeForm (p), typeForm (b)))
    }

  /** A type reference as typeRef spells it: name plus ( ) type parameters. The
    * surface carries no package here — a bare reference means the referring type's
    * own package, and `resolved` supplies it once the domain line has been read. */
  private def parseRef (token: String) : TypeName = {
    val (name, typeParameters) = splitApplied (token)
    TypeName (name, _typeParameters = typeParameters.map (typeForm))
  }

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
    val arguments = splitTypeArguments (s.substring (1, s.length - 1)).map (typeForm)
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
    * keywords until the closing `]`, which is not a member keyword, so the list
    * bounds itself — no layout is consulted. */
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
  private val declarationKeywords: Set[String] = Set ("fix", "mut", "dyn", "loc", "now")

  /** What an ACTION body admits (a rule's `action`, an actor's start / message /
    * signal): the declaration forms plus `mon`, since an action is mostly effects. */
  private val actionKeywords: Set[String] = declarationKeywords + "mon"

  /** One member of a list-block. `dyn` may open its own sub-block (parameters and
    * a body); every other keyword is a single leaf. */
  private def parseMember (c: Cursor) : TypeElement = {
    c.takeText () match {
      // `now <member>`: the member as written, marked strict (drake.dlt EVALUATION).
      case "now" => strict (parseMember (c))
      // A factory parameter is by-name (drake.dlt EVALUATION, step 3), so an argument that
      // consumes the cursor must be bound BEFORE the call: passed inline it would run at first
      // read, out of token order. The caller sequences its effects — Haskell's stance too.
      case "mon" => val value = parseValue (c); Monadic (value)
      case "con" => val value = parseValue (c); Condition (value)
      case "var" =>
        val name      = parseElementName (c.takeText ())
        val valueType = takeValueType (c)
        Variable (name, valueType)
      case "dyn" =>
        val name      = parseElementName (c.takeText ())
        val valueType = takeValueType (c)
        // A dyn-with-body opens with `[`; a leaf dyn carries its result inline.
        if (!c.at ("[")) { val value = parseValue (c); Dynamic (name, valueType, Seq.empty, Seq.empty, value) }
        else {
          c.take ()
          val parameters = parseSection (c, "parameters", Set ("par", "now")).map (_.asInstanceOf[Parameter])
          // The `body` section, as in a factory. Its statement named `value` is the
          // result (drake.dlt DYN-WITH-BODY); a block dyn has no value of its own.
          val body       = if (!c.at ("body")) Seq.empty else { c.take (); statements (c) }
          c.expect ("]")
          Dynamic (name, valueType, parameters, body, Json.Null)
        }
      case "case" =>
        // A case-branch (drake.dlt CASE-BRANCH): up to two tokens before its `[` — a
        // lower-case binder, an upper-case branch type, either, both, or neither — told
        // apart by case alone, the rule that already bounds a reference. Then its own
        // statements and `]`; its result, when it has one, is the statement named
        // `value`, exactly as in a dyn-with-body.
        var name      = ""
        var valueType = Json.Null
        while (!c.exhausted && !c.atReserved) {
          val t = c.takeText ()
          if (t.headOption.exists (_.isUpper)) valueType = typeForm (t) else name = parseElementName (t)
        }
        c.expect ("[")
        val body = statements (c)
        c.expect ("]")
        Case (name, valueType, body, Json.Null)
      case keyword @ ("fix" | "mut" | "loc" | "par") =>
        val name      = parseElementName (c.takeText ())
        val valueType = takeValueType (c)
        val value     = parseValue (c)
        keyword match {
          case "fix" => Fixed (name, valueType, value)
          case "mut" => Mutable (name, valueType, value)
          case "loc" => Local (name, valueType, value)
          case _     => Parameter (name, valueType, value)
        }
      case other => sys.error (s"Drake.parse: unknown member keyword '$other'")
    }
  }

  /** The member rebuilt with `now` set. Only the binding kinds carry the flag: a value
    * a body evaluates (fix, mut, loc), a parameter, a method. */
  private def strict (element: TypeElement) : TypeElement = element match {
    case e: Fixed     => Fixed (e.name, e.valueType, e.value, _now = true)
    case e: Mutable   => Mutable (e.name, e.valueType, e.value, _now = true)
    case e: Local     => Local (e.name, e.valueType, e.value, _now = true)
    case e: Parameter => Parameter (e.name, e.valueType, e.value, _now = true)
    case e: Dynamic   => Dynamic (e.name, e.valueType, e.parameters, e.body, e.value, _now = true)
    case e            => sys.error (s"Drake.parse: `now` applies to a binding, a parameter or a method, not to '${e.getClass.getSimpleName}'")
  }

  /** The factory's valueType is the enclosing type — the drake surface leaves it
    * implicit (drake.dlt CONVENTIONS: `factory` takes no name). */
  private def factoryValueType (name: String, typeParameters: Seq[Json]) : Json =
    if (typeParameters.isEmpty) Json.fromString (name)
    else Json.obj ("()" -> Json.fromValues (Json.fromString (name) +: typeParameters))

  /** Parse a .drake source into its TypeDefinition — the inverse of emit().
    * The plain-type template plus the rule and actor aspects; codec is the remaining
    * increment and is rejected loudly rather than silently dropped. */
  def parse (source: String) : TypeDefinition = {
    val c = new Cursor (source, lex (source))
    // A definition whose first word is `domain` is the NAMELESS domain's anchor (draco's
    // default package, 2026-09-20): no header, no name, the empty path its whole identity.
    val nameless = c.at ("domain")
    if (!nameless) c.expect ("type")
    // The header's parameters are TYPE FORMS (drake.dlt CONVENTIONS): a bare variable is
    // an Atomic string, `S <: DomainType` a bound leaf, exactly as a value-type slot reads.
    val (name, typeParameterTexts) = if (nameless) ("", Seq.empty[String]) else splitApplied (c.takeText ())
    val typeParameters = typeParameterTexts.map (typeForm)
    // A parent is a NAME (a draco type, resolved against the domain once it is read) or a
    // TYPE FORM (a foreign parent, outside every draco domain — Dictionary's `{K, V}`), and the
    // carrier holds each as what it is (2026-09-20); order is kept.
    val derivation = Seq.newBuilder[Either[TypeName, Json]]
    if (c.at ("from")) {
      c.take ()
      // Each reference may carry package words. The case rule bounds the SEQUENCE as
      // well as each member: lower-case words are package, the first upper-case token
      // ends the reference, and the next lower-case word starts the following one —
      // so `from draco Dictionary(K, V) draco DracoType` needs no separator.
      while (!c.exhausted && !c.atReserved)
        derivation += (if (c.peek.exists (opensTypeExpression)) Right (typeForm (c.takeText ())) else Left (takeQualifiedRef (c)))
    }

    var modules          = Seq.empty[TypeName]
    var extensible       = TypeName.Null
    var elements         = Seq.empty[TypeElement]
    var factory          = Factory.Null
    var globalElements   = Seq.empty[BodyElement]
    var superDomain      = TypeName.Null
    var domainName       = TypeName.Null
    var elementTypeNames = Seq.empty[String]
    var domainSource     = TypeName.Null
    var domainTarget     = TypeName.Null
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

    def actionBody (admits: Set[String] = actionKeywords) : Action = {
      val body = parseBlock (c, admits).map (_.asInstanceOf[BodyElement])   // sequenced: by-name parameter
      Action (Seq.empty, body)
    }

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
          // Sequenced before the call: parameters precede the body on the surface, and a
          // by-name argument would read them in whichever order the instance is read.
          val parameters = parseSection (c, "parameters", Set ("par", "now")).map (_.asInstanceOf[Parameter])
          val body       = parseSection (c, "body", declarationKeywords).map (_.asInstanceOf[BodyElement])
          factory = Factory (valueType, parameters, body)
        case "domain"      => domainName = if (c.exhausted || c.atReserved) TypeName.Null else takeQualifiedRef (c)
        case "super"       => superDomain = takeQualifiedRef (c)
        case "source"      => domainSource = takeQualifiedRef (c)
        case "target"      => domainTarget = takeQualifiedRef (c)
        case "types"       => elementTypeNames = parseNameList (c)

        case "rule"        => isRule = true
        case "pattern"     => ()   // its variables / conditions head themselves
        case "variables"   => variables = parseBlock (c, Set ("var")).map (_.asInstanceOf[Variable])
        case "conditions"  => conditions = parseBlock (c, Set ("con")).map (_.asInstanceOf[Condition])
        case "action"      => action = actionBody ()

        case "actor"       => isActor = true
        case "messageType" => messageType = takeQualifiedRef (c)
        case "start"       => start = actionBody ()
        // Of the three actor blocks only `message` admits a case-branch (drake.dlt
        // CASE-BRANCH): dispatch is on what arrived, and start / signal receive nothing.
        case "message"     => message = actionBody (actionKeywords + "case")
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
      if (tn.name.isEmpty || tn.namePackage.nonEmpty) tn
      else TypeName (tn.name, domainName.namePackage, tn.typeParameters)

    TypeDefinition (
      _typeName = TypeName (name, domainName.namePackage, typeParameters),
      _dracoAspect = DracoAspect (
        _superDomain    = superDomain,
        _modules        = modules.map (resolved),
        _extensible     = extensible,
        _derivation     = derivation.result ().map { case Left (tn) => TypeName.encoder (resolved (tn)); case Right (form) => form },
        _elements       = elements,
        _factory        = factory,
        _globalElements = globalElements),
      _domainAspect = DomainAspect (domainName, elementTypeNames, resolved (domainSource), resolved (domainTarget)),
      _ruleAspect   = if (!isRule) RuleAspect.Null else RuleAspect (Pattern (variables, conditions), action),
      _actorAspect  = if (!isActor) ActorAspect.Null else ActorAspect (message, resolved (messageType), signal, start))
  }
}
