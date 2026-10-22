package draco

import io.circe.{Json, JsonObject}

/** An operation's explicit name environment. No resource or classpath discovery.
  * Bindings map authored primitive/host names to their qualified identities.
  * The supplied ontology always takes precedence over a binding of the same name.
  */
final class NameResolution(val ontology: DomainOntology, val bindings: Map[String, TypeName], lenient: Boolean = false) {
  private def identity(n: TypeName): String = (n.namePackage :+ n.name).mkString(".")
  private val names = ontology.kvMap.keys.toSeq.flatMap(d =>
    d.typeDefinition.typeName +: d.typeDictionary.elementTypes.map(_.typeName))
    .filter(_.name.nonEmpty).groupBy(identity).values.map(_.head).toSeq.sortBy(identity)
  require(bindings.keys.forall(_.matches("[A-Za-z_][A-Za-z0-9_.]*")), "Invalid binding name")
  require(bindings.values.forall(_.name.nonEmpty), "An explicit binding must name a type")

  /** True when `full` is an identity the ontology holds, as opposed to a binding's. */
  def ontological(full: String): Boolean = names.exists(n => identity(n) == full)

  private def lookup(text: String, local: Map[String, String]): String = local.getOrElse(text, {
    val candidates = names.filter(n => if (text.contains(".")) identity(n) == text else n.name == text)
    require(candidates.size <= 1, s"Ambiguous type '$text': ${candidates.map(identity).mkString(", ")}")
    candidates.headOption.map(identity).getOrElse {
      bindings.get(text).map(identity).orElse {
        val qualified = bindings.values.map(identity).filter(_ == text).toSet
        qualified.headOption
      }.getOrElse(
        // A RECORDED scope (built from a definition's own `uses`) knows only what the record
        // names; every other leaf is the author's business and passes as written.
        if (lenient) text else throw new IllegalArgumentException(s"Unknown type '$text' in the supplied ontology and bindings"))
    }
  })

  /** Resolve one name, independently of any source definition. */
  def resolveName(text: String): TypeName = {
    val parts = lookup(text, Map.empty).split('.').toSeq
    TypeName(parts.last, parts.dropRight(1))
  }

  /** The COMPACT spelling of an identity (2026-10-21, narrowed with the corpus sweep): only an identity
    * the ontology holds is shortened, and only when its simple name is unique across the ontology AND
    * the binding vocabulary — a reader holding just the file's record must find it unambiguous, and a
    * target whose own table realizes the simple name (Decoder, Encoder) must not capture it. The
    * draco.rete wrap types are the exception: their bare name IS their realization in the host table
    * (Knowledge, RhsContext), so the surface keeps them bare as it always has. A host identity is
    * never shortened here: the vocabulary is the target's. */
  private def short(full: String, local: Map[String, String]): String = {
    val simple = full.split('.').last
    def matches(candidate: String): Boolean =
      try lookup(candidate, local) == full catch { case _: IllegalArgumentException => false }
    val wrap = full.startsWith("draco.rete.")
    val hostShadow = !wrap && bindings.exists { case (alias, target) => alias == simple || target.name == simple }
    if (ontological(full) && !hostShadow && matches(simple)) simple else full
  }

  /** Resolve from either carrier. compactJson is the ontology-scoped JSON spelling;
    * resolved identities remain available internally and are never inferred from paths. */
  def read(json: Json): TypeDefinition = {
    // The record is VALIDATED against the ontology on every scoped read (Dev, 2026-10-21): an
    // entry the ontology does not define is an error, as an unknown name is.
    json.hcursor.downField("uses").as[Option[Seq[TypeName]]].toOption.flatten.getOrElse(Seq.empty).foreach { tn =>
      require(ontological(identity(tn)), s"uses names '${identity(tn)}', which the supplied ontology does not define")
    }
    // A RESOLVED definition carries no record: every name is in full, so the record is the
    // compact form's artifact and compact recomputes it.
    TypeDefinition.decoder.decodeJson(rewrite(json.mapObject(_.remove("uses")), (_, full, _) => full)).fold(throw _, x => x)
  }
  def resolve(td: TypeDefinition): TypeDefinition = read(TypeDefinition.encoder(td))
  /** The compact form WRITES the resolution record (Dev, 2026-10-21): `uses` lists every
    * identity of the ontology that a body names by its short form, other than the definition's
    * own package, sorted; a definition that spells everything in full carries none. */
  def compactJson(td: TypeDefinition): Json = {
    val resolved = resolve(td)
    val own = resolved.typeName.namePackage
    val shortened = scala.collection.mutable.Set.empty[String]
    // A host leaf keeps the author's spelling: the vocabulary is the target's, and the compact
    // form never writes a binding's identity into a definition.
    val authoredHost = scala.collection.mutable.Map.empty[String, String]
    rewrite(TypeDefinition.encoder(td), (authored, full, _) => { if (!ontological(full)) authoredHost.getOrElseUpdate(full, authored); full })
    val compact = rewrite(TypeDefinition.encoder(resolved), (authored, full, local) => {
      if (!ontological(full)) authoredHost.getOrElse(full, authored)
      else {
        val s = short(full, local)
        if (s != full && full.take(full.lastIndexOf('.')).split('.').toSeq != own) shortened += full
        s
      }
    })
    val record = shortened.toSeq.sorted.map { full => val parts = full.split('.').toSeq; TypeName(parts.last, parts.dropRight(1)) }
    compact.mapObject { o =>
      val without = o.remove("uses")
      if (record.isEmpty) without
      else JsonObject.fromIterable(without.toIterable.toSeq.flatMap { case (k, v) =>
        // `uses` sits after `typeName`, where the carrier's field order puts it.
        if (k == "typeName") Seq(k -> v, "uses" -> Json.fromValues(record.map(TypeName.encoder(_)))) else Seq(k -> v) })
    }
  }
  def compact(td: TypeDefinition): TypeDefinition =
    TypeDefinition.decoder.decodeJson(compactJson(td)).fold(throw _, x => x)

  /** MINIMAL qualification, for a target whose own engine reads conventional spellings
    * (2026-10-21): every leaf is resolved, but a leaf keeps its authored spelling when it is
    * locally bound (the definition's own name, a formal), already in full, or when
    * `readable(authored, full)` says the target reads it as written — a bare name of the
    * target's own unqualified scope, or one the target realizes itself. The leaves that had
    * to be qualified are returned with the definition, so the target can shorten exactly
    * those back by import. Applied to the COMPACT form, this is independent of how the
    * author spelled a resolvable name. */
  def minimal(td: TypeDefinition, readable: (String, String) => Boolean): (TypeDefinition, Set[String]) = {
    val qualified = scala.collection.mutable.Set.empty[String]
    val json = rewrite(TypeDefinition.encoder(td), (authored, full, local) =>
      if (local.contains(authored) || authored == full || readable(authored, full)) authored
      else { qualified += full; full })
    (TypeDefinition.decoder.decodeJson(json).fold(throw _, x => x), qualified.toSet)
  }

  private def rewrite(document: Json, render: (String, String, Map[String, String]) => String): Json = {
    val owner = document.hcursor.downField("typeName")
    val ownerName = owner.get[String]("name").getOrElse("")
    val ownerPackage = owner.get[Seq[String]]("namePackage").getOrElse(Seq.empty)
    val ownerPath = (ownerPackage :+ ownerName).mkString(".")
    def formals(values: Vector[Json]): Map[String, String] = values.flatMap { p =>
      val name = p.asString.orElse(p.hcursor.get[String]("name").toOption)
        .orElse(TypeForm.node(p).filter(n => Set("<:", ">:")(n._1)).flatMap(_._2.headOption.flatMap(_.asString)))
      name.filter(_.matches("[A-Za-z_][A-Za-z0-9_]*")).map(n => n -> n)
    }.toMap
    // The definition's FORMALS are DracoAspect.typeParameters records (2026-10-21); the header's
    // name carries none. Their names bind locally over the whole definition.
    val header = document.hcursor.downField("dracoAspect").downField("typeParameters").as[Vector[Json]].getOrElse(Vector.empty)
    val rootLocal = formals(header) ++ Map(ownerName -> ownerPath, ownerPath -> ownerPath)
    def name(text: String, local: Map[String, String]): String = render(text, lookup(text, local), local)
    def form(j: Json, local: Map[String, String], r: (String, String, Map[String, String]) => String = render): Json = {
      if (j.isNull || j.asString.contains("")) j
      else j.asString match {
        // A string leaf that is not a name is host-opaque type text (the ActorAspect residue
        // the surface-loss headline counts); it is preserved, as host-opaque expression spans are.
        case Some(text) if !text.matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*") => j
        case Some(text) => Json.fromString(r(text, lookup(text, local), local))
        case None => TypeForm.node(j) match {
          case Some((op, args)) if Set("()", "(,)", "[]", "[]+", "{}", "{}+", "->", "<:", ">:")(op) =>
            Json.obj(op -> Json.fromValues(args.map(form(_, local, r))))
          case _ => throw new IllegalArgumentException(s"Not a structural type form: ${j.noSpaces}")
        }
      }
    }
    // An anchor's type parameters follow the anchor's own convention (2026-10-21): bare in the
    // definition's own package, in full elsewhere — never the record's short form.
    val anchorParameter: (String, String, Map[String, String]) => String = (authored, full, local) =>
      if (local.contains(authored)) authored
      else { val cut = full.lastIndexOf('.'); if (cut >= 0 && full.take(cut).split('.').toSeq == ownerPackage) full.drop(cut + 1) else full }
    def reference(j: Json, local: Map[String, String], anchor: Boolean = false): Json = { val _ = anchor
      val c = j.hcursor
      val n = c.get[String]("name").getOrElse("")
      if (n.isEmpty) j
      else {
        val pkg = c.get[Seq[String]]("namePackage").getOrElse(Seq.empty)
        val full = lookup((pkg :+ n).mkString("."), local)
        // A NOMINAL reference — a derivation record, a domain pointer, a message type — is an
        // anchor and is always spelled in full, whatever the policy (Dev, 2026-10-21): the
        // unscoped readers (companions, the engine's parent chains, the ontology's edges)
        // compare it by name path. Only bodies — type forms and expression leaves — vary.
        val rendered = full
        val parts = rendered.split('.').toSeq
        val fields = Vector("name" -> Json.fromString(parts.last)) ++
          (if (parts.size > 1) Vector("namePackage" -> Json.fromValues(parts.dropRight(1).map(Json.fromString))) else Vector.empty) ++
          c.get[Vector[Json]]("typeParameters").toOption.filter(_.nonEmpty)
            .map(ps => "typeParameters" -> Json.fromValues(ps.map(form(_, local, anchorParameter)))).toVector
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
          case ("typeParameters", v) if method || context == "dracoAspect" => "typeParameters" -> walk(v, scope, "formals")
          case ("typeParameters", v) => "typeParameters" -> Json.fromValues(v.asArray.get.map(form(_, scope)))
          case (key, v) => key -> walk(v, scope, key, valueLocal ++ values)
        }))
      })
    walk(document, rootLocal)
  }

  /** Target projection may shorten qualified identities and add selective imports.
    * Only Scala code tokens are changed; literals, comments and existing imports remain intact.
    * An identity the file already imports by name, or whose package the file is in or imports
    * wholesale, is shortened without a new import (2026-10-21); a simple name the file imports
    * from ELSEWHERE, defines or binds as a formal is a collision and stays qualified. */
  def projectScala(source: String): String = projectScala(source, None)

  /** With `only` given, shortening is restricted to those identities — the ones a minimal
    * qualification introduced, possibly none — so an identity the definition spelled in full
    * stays in full (2026-10-21). Without it, every unambiguous identity is a candidate. */
  def projectScala(source: String, only: Set[String]): String = projectScala(source, Some(only))

  private def projectScala(source: String, only: Option[Set[String]]): String = {
    val definitions = "\\b(?:trait|class|object|val|var|def) ([A-Za-z_][A-Za-z0-9_]*)".r
      .findAllMatchIn(source).map(_.group(1)).toSet
    val formals = "(?:trait|class|def)\\s+[A-Za-z_][A-Za-z0-9_]*\\[([^\\]]*)\\]".r
      .findAllMatchIn(source).flatMap(_.group(1).split(",").map(_.trim.takeWhile(c => c.isLetterOrDigit || c == '_'))).toSet
    val ownPackage = "(?m)^package ([A-Za-z0-9_.]+)".r.findFirstMatchIn(source).map(_.group(1))
    val importBodies = "(?m)^import (.*)$".r.findAllMatchIn(source).map(_.group(1).trim).toSeq
    // Exact imports (`import a.b.C`, `import a.b.{C, D}`), renames (`C => E`) and wildcards (`a.b._`).
    val exactImports: Set[(String, String)] = importBodies.flatMap { body =>
      val cut = body.lastIndexOf('.'); val pkg = body.take(cut); val tail = body.drop(cut + 1)
      if (tail == "_") Nil
      else if (tail.startsWith("{")) tail.stripPrefix("{").stripSuffix("}").split(',').map(_.trim).filter(_.nonEmpty).toSeq.map { m =>
        val parts = m.split("=>").map(_.trim); if (parts.size == 2) ("", parts(1)) else (pkg + "." + parts(0), parts(0)) }
      else Seq((body, tail))
    }.toSet
    val importedSimple = exactImports.map(_._2)
    val exactIdentities = exactImports.map(_._1).filter(_.nonEmpty)
    val wildcards = importBodies.filter(_.endsWith("._")).map(_.stripSuffix("._")).toSet
    def covered(full: String): Boolean = {
      val pkg = full.take(full.lastIndexOf('.'))
      exactIdentities(full) || wildcards(pkg) || ownPackage.contains(pkg)
    }
    val candidates = (names ++ bindings.values).groupBy(_.name).toSeq.collect {
      case (simple, ns) if ns.map(identity).distinct.size == 1 && !formals(simple) &&
        (!definitions(simple) || ownPackage.contains(identity(ns.head).take(identity(ns.head).lastIndexOf('.')))) =>
        identity(ns.head) -> simple
    }.filter(_._1.contains(".")).filter { case (full, simple) => exactIdentities(full) || !importedSimple(simple) }
      .filter { case (full, _) => only.forall(_(full)) }.toMap
    val used = scala.collection.mutable.Set.empty[String]
    // Strings (including triples), character literals and comments precede the token alternative.
    val tokens = ("(?s)"+ "\"\"\".*?\"\"\"|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|//[^\\n]*|/\\*.*?\\*/|[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*").r
    val protectedTokens = ("(?m)^(?:import|package) [^\n]*|" + tokens.regex).r
    val lines = protectedTokens.replaceAllIn(source, m => {
      val text = m.matched
      val prefix = candidates.keys.toSeq.sortBy(-_.length).find(p => text == p || text.startsWith(p + "."))
      java.util.regex.Matcher.quoteReplacement(prefix.map { p => if (!covered(p)) used += p; candidates(p) + text.drop(p.length) }.getOrElse(text))
    })
    if (used.isEmpty) source else {
      val imports = used.toSeq.sorted.map(p => s"import $p").mkString("\n")
      val end = lines.indexOf('\n')
      if (lines.startsWith("package ") && end >= 0) lines.take(end + 1) + "\n" + imports + "\n" + lines.drop(end + 1)
      else imports + "\n\n" + lines
    }
  }
}

object NameResolution {
  /** The scope a definition carries in itself (2026-10-21): its `uses` record as the whole
    * ontology — one placeholder domain per package, holding the names — no bindings, and lenient,
    * so every leaf the record does not name passes as the author wrote it. An unscoped reader
    * resolves the file from this alone; the ontology is needed only to author and validate it. */
  def recorded(td: TypeDefinition): NameResolution = {
    val domains: Seq[DomainType] = td.uses.groupBy(_.namePackage).toSeq.sortBy(_._1.mkString(".")).map { case (pkg, names) =>
      val anchor = TypeName("", pkg)
      Domain[Any](TypeDefinition(anchor, _domainAspect = DomainAspect(anchor, names.map(_.name))))
    }
    new NameResolution(DomainOntology(domains), Map.empty, lenient = true)
  }
}
