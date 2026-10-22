package draco

import draco.generator.carrier.DefinitionPath

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
      td.ruleAspect.fold(true, _ => false) &&
      td.actorAspect.fold(true, _ => false)

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

  /** Load one definition from an explicit path: the file the name points at, rooted
    * as `TypeLoader.loadType` roots it; a name with no file is a stub. Only the path
    * differs from the companions' unscoped loading, which always reads the default. */
  private def loadFrom(path: DefinitionPath, typeName: TypeName): TypeDefinition =
    path.source(typeName.resourcePath).fold(draco.drake.Absent[TypeDefinition](), TypeLoader.readDefinition)
      .fold(TypeDefinition(typeName), TypeLoader.rooted)

  /** The declared domains' own definitions, read as authored. A declaration is an
    * error when nothing at the roots defines it, or when what it names does not
    * self-declare as a domain: the set is explicit, so neither is tolerated. */
  private def declared(declarations: Seq[TypeName], path: DefinitionPath): Seq[TypeDefinition] =
    declarations.map { d =>
      val td = loadFrom(path, d)
      require(!isStub(td), s"draco: declared domain ${d.namePath} has no definition at the supplied roots")
      require(td.domainAspect.typeName.namePath == td.typeName.namePath,
        s"draco: ${d.namePath} is not a domain: it declares ${td.domainAspect.typeName.namePath}")
      td
    }

  /** STAGE ONE of two-stage loading (Dev accepted 2026-10-21): the identity-only
    * ontology of an explicitly declared set of domains. A domain definition's own name,
    * its self-declaration and its `types` list are qualified by construction — a listed
    * member's identity is the domain's package plus its bare name — so identity needs no
    * resolution scope. `Domain(definition)` with no members gives core's name-only
    * placeholders for every listed member, and an ontology of those placeholders is, as a
    * resolution scope, identical to the populated one: `NameResolution` reads identities
    * only. Nothing is discovered: which domains exist is the caller's declaration. */
  def identities(declarations: Seq[TypeName], path: DefinitionPath = DefinitionPath.default): DomainOntology = {
    // Factory arguments are by-name: every effect is bound to a value before the call (§6).
    val domains: Seq[DomainType] = declared(declarations, path).map(td => Domain[Any](td))
    DomainOntology(domains)
  }

  /** STAGE TWO: load the declared domains with their members and resolve every body —
    * the domain definition's and each member's — through `NameResolution` over the
    * identity-only ontology and the explicit binding vocabulary, then compose the
    * populated ontology. A body may name a type held by any declared domain by its
    * short name; a name no declared domain holds and no binding supplies fails, as it
    * does in every scoped operation. The result does not depend on the declarations'
    * order. The generated companions keep loading unscoped through `TypeLoader`; the
    * bootstrap files are untouched by this operation. */
  def load(declarations: Seq[TypeName], bindings: Map[String, TypeName],
           path: DefinitionPath = DefinitionPath.default): DomainOntology = {
    val definitions = declared(declarations, path)
    val placeholders: Seq[DomainType] = definitions.map(td => Domain[Any](td))
    val scope = new NameResolution(DomainOntology(placeholders), bindings)
    // Resolution is strict here, so an unknown name fails in `load`, not on the first read of a
    // dictionary: factory arguments are by-name and would defer it (§6).
    val domains: Seq[DomainType] = definitions.map { td =>
      val definition = scope.resolve(td)
      val members = td.domainAspect.elementTypeNames
        .map(m => loadFrom(path, TypeName(m, td.typeName.namePackage))).map(scope.resolve)
      Domain[Any](definition, members)
    }
    DomainOntology(domains)
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

    // Name and package, parameters free (2026-10-12): a self-declaration points at the
    // definition, and definitions are keyed bare — Format(F) self-declares as Format.
    val selfDeclaration: Seq[String] =
      if (td.domainAspect.typeName.namePath == td.typeName.namePath) Nil
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
