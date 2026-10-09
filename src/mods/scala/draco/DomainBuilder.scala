package draco

/** DomainBuilder — the resource-backed convenience for standing up a populated
  * domain: load a domain's own definition and every member it names from the
  * definition resources, hand them to core's `Domain` factory, and return the
  * concrete `DomainType`. Since 2026-10-09 core's `TypeDictionary` and `Domain`
  * factories accept supplied member definitions, so population itself is a core
  * capability; what remains here is the *loading* — which members, from where —
  * and the procedural validation and generation helpers built on it.
  *
  * == The ontology ==
  * `ontology` composes already-defined domains into a `DomainOntology`, the
  * cross-domain structure that was called `DomainDictionary` until 2026-10-09.
  * "Dictionary" now names only a domain's own `TypeDictionary`; the ontology is
  * built from domains and their intra- and inter-relationships, of which today's
  * map holds only membership. The Editor actor (`draco.dreams.editor`) takes an
  * accepted ontology as its reference set and validates a candidate domain
  * against the ontology that would result, never against the classpath.
  *
  * == Promotion path ==
  * This lives in `src/mods/scala/draco/` under `package draco`, mirroring
  * `src/main/scala/draco/`. The shared package name means sbt and the IDE flag
  * any duplicate FQN, so the stand-in cannot silently diverge from — or collide
  * with — what it is destined to become. Built entirely from draco's public API —
  * no new third-party deps.
  *
  * @see [[TypeDictionary]] for the population these loaders feed.
  */
object DomainBuilder {

  /** Load a member's full `TypeDefinition`. `TypeName.resourcePath` points at
    * the member's JSON and a plain `loadType` resolves every aspect uniformly.
    * A member named but not yet authored comes back as an empty TD (a stub). */
  private def loadMember(name: String, namePackage: Seq[String]): TypeDefinition =
    DracoGenerator.loadType(TypeName(name, _namePackage = namePackage))

  /** True when a loaded TD came back empty — named in the dictionary but with no
    * JSON on disk. Such members still generate, as a skeleton. */
  def isStub(td: TypeDefinition): Boolean =
    DracoAspect.isEmpty(td.dracoAspect) &&
      DomainAspect.isEmpty(td.domainAspect) &&
      RuleAspect.isEmpty(td.ruleAspect) &&
      ActorAspect.isEmpty(td.actorAspect)

  /** Define a domain from its resources: load its own definition, then load
    * every member named in `domainAspect.elementTypeNames`, and return the
    * concrete `Domain` core builds from them — its `typeDictionary` populated
    * with the loaded definitions. A member with no resource is a stub entry. */
  def define(name: String, namePackage: Seq[String]): DomainType = {
    val domainDef = DracoGenerator.loadType(TypeName(name, _namePackage = namePackage))
    val members: Seq[TypeDefinition] =
      domainDef.domainAspect.elementTypeNames.map(m => loadMember(m, namePackage))
    Domain[Any](domainDef, members)
  }

  /** Compose the `DomainOntology` from one or more already-defined domains.
    * Delegates to `DomainOntology.apply`; because the supplied domains carry
    * populated `typeDictionary`s (from [[define]]), the ontology defines every
    * member they hold. */
  def ontology(domains: DomainType*): DomainOntology =
    DomainOntology(domains)

  /** Validate a built domain against the structural invariants the endogenous
    * draco domains are expected to uphold. Returns a list of human-readable
    * problems — empty means well-formed. This is the rigorous counterpart to
    * `generate`'s skeleton tolerance: `generate` *accommodates* an in-progress
    * user domain, while `validate` *reports* every hole, so first-party domains
    * (and, eventually, domain-expert-authored ones) can be held to zero.
    *
    * Checks (battery 1–2):
    *  1. Self-declaration — the domain's `domainAspect.typeName` equals its own
    *     `typeName`, type parameters included (it actually claims to be the
    *     domain it is loaded as).
    *  2. Completeness — every declared member resolves to real content, not the
    *     empty (stub) `TypeDefinition` a missing JSON would yield.
    *  3. Derivation resolvability — every draco-internal ancestor named in a
    *     member's `dracoAspect.derivation` itself resolves to a definition, so no
    *     member claims an inheritance chain that dangles. External supertypes
    *     (non-`draco` packages, e.g. Pekko/Evrete) are out of scope and skipped.
    *     This procedural check resolves through the definition resources, which
    *     is what a resource-loaded domain is accountable to; the rule
    *     `draco.DerivationResolvable` resolves against the `DomainOntology` in
    *     working memory instead, so a candidate domain is never judged by the
    *     classpath.
    */
  def validate(domain: DomainType): Seq[String] = {
    val td = domain.typeDefinition

    val selfDeclaration: Seq[String] =
      if (td.domainAspect.typeName == td.typeName) Nil
      else Seq(
        s"domain ${td.typeName.namePath} does not self-declare: " +
          s"domainAspect.typeName is ${td.domainAspect.typeName.namePath}")

    val members = domain.typeDictionary.elementTypes

    val completeness: Seq[String] = members.collect {
      case m if isStub(m) =>
        s"member ${m.typeName.name} is declared but unauthored (no JSON on disk)"
    }

    val derivation: Seq[String] = members.flatMap { m =>
      DracoAspect.parents(m.dracoAspect)
        .filter(_.namePackage.headOption.contains("draco"))
        .collect {
          case anc if isStub(DracoGenerator.loadType(anc)) =>
            s"member ${m.typeName.name} derives from ${anc.namePath}, " +
              s"which does not resolve to a definition"
        }
    }

    selfDeclaration ++ completeness ++ derivation
  }

  /** Generate Scala for an entire domain — the domain object itself plus every
    * member of its dictionary — keyed by `TypeName`. Skeleton-tolerant: a member
    * with no complete definition (a stub) still emits whatever `DracoGenerator.generate`
    * produces for a thin TD; should generation of one member fail, a clearly
    * marked placeholder skeleton is emitted in its place so a single bad member
    * never sinks the whole batch. */
  def generate(name: String, namePackage: Seq[String]): Map[TypeName, String] = {
    val domain = define(name, namePackage)
    val all: Seq[TypeDefinition] = domain.typeDefinition +: domain.typeDictionary.elementTypes
    all.map(td => td.typeName -> safeGenerate(td)).toMap
  }

  private def safeGenerate(td: TypeDefinition): String =
    try DracoGenerator.generate(td)
    catch { case t: Throwable => placeholderSkeleton(td, t) }

  /** Last-resort skeleton when `DracoGenerator.generate` throws on an incomplete member.
    * Strips any aspect suffix so the emitted identifier is a legal Scala name. */
  private def placeholderSkeleton(td: TypeDefinition, cause: Throwable): String = {
    val pkg = td.typeName.namePackage.mkString(".")
    val ident = td.typeName.name.takeWhile(_ != '.')
    s"""package $pkg
       |
       |// stub skeleton — could not generate ${td.typeName.name}: ${cause.getMessage}
       |trait $ident
       |object $ident
       |""".stripMargin
  }
}
