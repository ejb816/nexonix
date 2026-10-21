package draco

import io.circe.{Json, JsonObject}

/** An operation's explicit name environment. No resource or classpath discovery.
  * Bindings map authored primitive/host names to their qualified identities.
  * The supplied ontology always takes precedence over a binding of the same name.
  */
final class NameResolution(val ontology: DomainOntology, val bindings: Map[String, TypeName]) {
  private def identity(n: TypeName): String = (n.namePackage :+ n.name).mkString(".")
  private val names = ontology.kvMap.keys.toSeq.flatMap(d =>
    d.typeDefinition.typeName +: d.typeDictionary.elementTypes.map(_.typeName))
    .filter(_.name.nonEmpty).groupBy(identity).values.map(_.head).toSeq.sortBy(identity)
  require(bindings.keys.forall(_.matches("[A-Za-z_][A-Za-z0-9_.]*")), "Invalid binding name")
  require(bindings.values.forall(_.name.nonEmpty), "An explicit binding must name a type")

  private def lookup(text: String, local: Map[String, String]): String = local.getOrElse(text, {
    val candidates = names.filter(n => if (text.contains(".")) identity(n) == text else n.name == text)
    require(candidates.size <= 1, s"Ambiguous type '$text': ${candidates.map(identity).mkString(", ")}")
    candidates.headOption.map(identity).getOrElse {
      bindings.get(text).map(identity).orElse {
        val qualified = bindings.values.map(identity).filter(_ == text).toSet
        qualified.headOption
      }.getOrElse(throw new IllegalArgumentException(s"Unknown type '$text' in the supplied ontology and bindings"))
    }
  })

  /** Resolve one name, independently of any source definition. */
  def resolveName(text: String): TypeName = {
    val parts = lookup(text, Map.empty).split('.').toSeq
    TypeName(parts.last, parts.dropRight(1))
  }

  private def short(full: String, local: Map[String, String]): String = {
    val simple = full.split('.').last
    def matches(candidate: String): Boolean =
      try lookup(candidate, local) == full catch { case _: IllegalArgumentException => false }
    if (matches(simple)) simple
    else bindings.toSeq.sortBy(_._1).collectFirst {
      case (alias, target) if identity(target) == full && matches(alias) => alias
    }.getOrElse(full)
  }

  /** Resolve from either carrier. compactJson is the ontology-scoped JSON spelling;
    * resolved identities remain available internally and are never inferred from paths. */
  def read(json: Json): TypeDefinition =
    TypeDefinition.decoder.decodeJson(rewrite(json, compact = false)).fold(throw _, x => x)
  def resolve(td: TypeDefinition): TypeDefinition = read(TypeDefinition.encoder(td))
  def compactJson(td: TypeDefinition): Json =
    rewrite(TypeDefinition.encoder(resolve(td)), compact = true)
  def compact(td: TypeDefinition): TypeDefinition =
    TypeDefinition.decoder.decodeJson(compactJson(td)).fold(throw _, x => x)

  private def rewrite(document: Json, compact: Boolean): Json = {
    val owner = document.hcursor.downField("typeName")
    val ownerName = owner.get[String]("name").getOrElse("")
    val ownerPackage = owner.get[Seq[String]]("namePackage").getOrElse(Seq.empty)
    val ownerPath = (ownerPackage :+ ownerName).mkString(".")
    def formals(values: Vector[Json]): Map[String, String] = values.flatMap { p =>
      val name = p.asString.orElse(p.hcursor.get[String]("name").toOption)
        .orElse(TypeForm.node(p).filter(n => Set("<:", ">:")(n._1)).flatMap(_._2.headOption.flatMap(_.asString)))
      name.filter(_.matches("[A-Za-z_][A-Za-z0-9_]*")).map(n => n -> n)
    }.toMap
    val header = owner.get[Vector[Json]]("typeParameters").getOrElse(Vector.empty)
    val rootLocal = formals(header) ++ Map(ownerName -> ownerPath, ownerPath -> ownerPath)
    def name(text: String, local: Map[String, String]): String = {
      val full = lookup(text, local)
      if (compact) short(full, local) else full
    }
    def form(j: Json, local: Map[String, String]): Json = {
      if (j.isNull || j.asString.contains("")) j
      else j.asString match {
        case Some(text) => Json.fromString(name(text, local))
        case None => TypeForm.node(j) match {
          case Some((op, args)) if Set("()", "(,)", "[]", "[]+", "{}", "{}+", "->", "<:", ">:")(op) =>
            Json.obj(op -> Json.fromValues(args.map(form(_, local))))
          case _ => throw new IllegalArgumentException(s"Not a structural type form: ${j.noSpaces}")
        }
      }
    }
    def reference(j: Json, local: Map[String, String], anchor: Boolean = false): Json = {
      val c = j.hcursor
      val n = c.get[String]("name").getOrElse("")
      if (n.isEmpty) j
      else {
        val pkg = c.get[Seq[String]]("namePackage").getOrElse(Seq.empty)
        val full = lookup((pkg :+ n).mkString("."), local)
        val rendered = if (compact && !anchor) short(full, local) else full
        val parts = rendered.split('.').toSeq
        val fields = Vector("name" -> Json.fromString(parts.last)) ++
          (if (parts.size > 1) Vector("namePackage" -> Json.fromValues(parts.dropRight(1).map(Json.fromString))) else Vector.empty) ++
          c.get[Vector[Json]]("typeParameters").toOption.filter(_.nonEmpty)
            .map(ps => "typeParameters" -> Json.fromValues(ps.map(form(_, local)))).toVector
        Json.obj(fields: _*)
      }
    }
    // Only explicit symbolic expression leaves are resolved. Quoted literals and
    // host-opaque spans are not searched/replaced as if they were type expressions.
    def expression(j: Json, local: Map[String, String]): Json = j.asString match {
      case Some(text) if text.matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*") =>
        val parts = text.split('.').toVector
        val prefix = (parts.size to 1 by -1).map(i => parts.take(i).mkString(".")).find { candidate =>
          local.contains(candidate) || names.exists(n => identity(n) == candidate || n.name == candidate) ||
            bindings.contains(candidate) || bindings.values.exists(n => identity(n) == candidate)
        }
        prefix.map { p =>
          Json.fromString(name(p, local) + text.drop(p.length))
        }.getOrElse {
          val typeIndex = parts.indexWhere(_.headOption.exists(_.isUpper))
          if (typeIndex < 0) j
          else {
            val p = parts.take(typeIndex + 1).mkString(".")
            Json.fromString(name(p, local) + text.drop(p.length))
          }
        }
      case Some(_) => j
      case None if TypeForm.node(j).exists(_._1 == "\\") =>
        val args = TypeForm.node(j).get._2
        val binders = args.dropRight(1).flatMap(_.asString).map(n => n -> n).toMap
        Json.obj("\\" -> Json.fromValues(args.dropRight(1) :+ expression(args.last, local ++ binders)))
      case None => j.arrayOrObject(j, xs => Json.fromValues(xs.map(expression(_, local))),
        obj => Json.fromJsonObject(JsonObject.fromIterable(obj.toIterable.map { case (k, v) => k -> expression(v, local) })))
    }
    def walk(j: Json, local: Map[String, String], context: String = "", valueLocal: Map[String, String] = Map.empty): Json = j.arrayOrObject(j,
      xs => Json.fromValues(xs.map(walk(_, local, context, valueLocal))), obj => {
        val method = obj("kind").flatMap(_.asString).contains("Dynamic")
        val scope = if (method) local ++ formals(obj("typeParameters").flatMap(_.asArray).getOrElse(Vector.empty)) else local
        val isElement = obj("kind").isDefined
        val values = if (isElement) {
          val own = obj("name").flatMap(_.asString).toSeq
          val params = obj("parameters").flatMap(_.asArray).toSeq.flatten.flatMap(_.hcursor.get[String]("name").toOption)
          (own ++ params).map(n => n -> n).toMap
        } else Map.empty[String, String]
        Json.fromJsonObject(JsonObject.fromIterable(obj.toIterable.map {
          case ("typeName", v) if context.isEmpty =>
            // The declaration establishes identity; its parameter bounds resolve in local scope.
            "typeName" -> v.mapObject(o => o("typeParameters").fold(o)(ps => o.add("typeParameters", Json.fromValues(ps.asArray.get.map(form(_, scope))))))
          case ("typeName", v) => "typeName" -> reference(v, scope, anchor = context == "domainAspect")
          case (key @ ("superDomain" | "extensible" | "source" | "target" | "messageType"), v) =>
            key -> reference(v, scope)
          case ("modules", v) => "modules" -> Json.fromValues(v.asArray.get.map(reference(_, scope)))
          case ("derivation", v) => "derivation" -> Json.fromValues(v.asArray.get.map { p =>
            if (p.hcursor.downField("name").succeeded) reference(p, scope) else form(p, scope)
          })
          case (key @ ("valueType" | "upperBound" | "lowerBound"), v) => key -> form(v, scope)
          case ("value", v) => "value" -> expression(v, scope ++ valueLocal ++ values)
          case ("typeParameters", v) if method => "typeParameters" -> walk(v, scope, "formals")
          case ("typeParameters", v) => "typeParameters" -> Json.fromValues(v.asArray.get.map(form(_, scope)))
          case (key, v) => key -> walk(v, scope, key, valueLocal ++ values)
        }))
      })
    walk(document, rootLocal)
  }

  /** Target projection may shorten qualified identities and add selective imports.
    * Only Scala code tokens are changed; literals, comments and existing imports remain intact. */
  def projectScala(source: String): String = {
    val definitions = "\\b(?:trait|class|object|val|var|def) ([A-Za-z_][A-Za-z0-9_]*)".r
      .findAllMatchIn(source).map(_.group(1)).toSet
    val formals = "(?:trait|class|def)\\s+[A-Za-z_][A-Za-z0-9_]*\\[([^\\]]*)\\]".r
      .findAllMatchIn(source).flatMap(_.group(1).split(",").map(_.trim.takeWhile(c => c.isLetterOrDigit || c == '_'))).toSet
    val imported = "(?m)^import .*".r.findAllIn(source).mkString("\n")
    val candidates = (names ++ bindings.values).groupBy(_.name).toSeq.collect {
      case (simple, ns) if ns.map(identity).distinct.size == 1 && !definitions(simple) && !formals(simple) &&
        !("\\b" + java.util.regex.Pattern.quote(simple) + "\\b").r.findFirstIn(imported).isDefined =>
        identity(ns.head) -> simple
    }.filter(_._1.contains(".")).toMap
    val used = scala.collection.mutable.Set.empty[String]
    // Strings (including triples), character literals and comments precede the token alternative.
    val tokens = ("(?s)"+ "\"\"\".*?\"\"\"|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|//[^\\n]*|/\\*.*?\\*/|[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*").r
    val protectedTokens = ("(?m)^(?:import|package) [^\n]*|" + tokens.regex).r
    val lines = protectedTokens.replaceAllIn(source, m => {
      val text = m.matched
      val prefix = candidates.keys.toSeq.sortBy(-_.length).find(p => text == p || text.startsWith(p + "."))
      java.util.regex.Matcher.quoteReplacement(prefix.map { p => used += p; candidates(p) + text.drop(p.length) }.getOrElse(text))
    })
    if (used.isEmpty) source else {
      val imports = used.toSeq.sorted.map(p => s"import $p").mkString("\n")
      val end = lines.indexOf('\n')
      if (lines.startsWith("package ") && end >= 0) lines.take(end + 1) + "\n" + imports + "\n" + lines.drop(end + 1)
      else imports + "\n\n" + lines
    }
  }
}
