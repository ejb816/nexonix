package draco

/** DomainBuilder — a `src/mods` stand-in for an under-development draco core
  * capability: comprehensively define a *domain dictionary* from JSON alone,
  * stand up a concrete, fully-populated instance of it, and generate code
  * (skeleton-tolerant) for the whole dictionary.
  *
  * == Why this is a stand-in ==
  * A domain is already comprehensively describable in JSON today — a domain's
  * `.json` carries `domainAspect.elementTypeNames`, and each member has its own
  * `.json` in the same resource package. The *data* side is complete.
  *
  * The *concrete-instance* side is not. `TypeDictionary.apply(domainDefinition)`
  * builds its members as `elementTypeNames.map(n => TypeDefinition(TypeName(n)))`
  * — the empty `TypeDefinition` constructor (all aspects `Null`). It never calls
  * `Generator.loadType`, so a concrete `TypeDictionary` holds member *names* but
  * not member *content*, and you cannot generate code from it. DomainBuilder
  * supplies the populated counterpart by loading each member's full definition.
  *
  * == Promotion path ==
  * This lives in `src/mods/scala/draco/` under `package draco`, mirroring
  * `src/main/scala/draco/`. The shared package name means sbt and the IDE flag
  * any duplicate FQN, so the stand-in cannot silently diverge from — or collide
  * with — what it is destined to become. When core's dictionary instantiation is
  * made comprehensive (or this lands in the dreams layer), the file moves from
  * the mods `draco` tree to the main `draco` tree and any conflict resolves in
  * place. Built entirely from draco's public API — no new third-party deps.
  *
  * @see [[TypeDictionary]] for the hollow counterpart this completes.
  */
object DomainBuilder {

  /** Load a member's full `TypeDefinition`. The aspect suffix (`.rule` / `.actor`)
    * is baked into the `elementTypeName`, so `TypeName.resourcePath` already points
    * at the correct JSON and a plain `loadType` resolves every aspect uniformly.
    * A member named but not yet authored comes back as an empty TD (a stub). */
  private def loadMember(name: String, namePackage: Seq[String]): TypeDefinition =
    Generator.loadType(TypeName(name, _namePackage = namePackage))

  /** True when a loaded TD came back empty — named in the dictionary but with no
    * JSON on disk. Such members still generate, as a skeleton. */
  def isStub(td: TypeDefinition): Boolean =
    DracoAspect.isEmpty(td.dracoAspect) &&
      DomainAspect.isEmpty(td.domainAspect) &&
      RuleAspect.isEmpty(td.ruleAspect) &&
      ActorAspect.isEmpty(td.actorAspect)

  /** Comprehensively define a domain from JSON: load its own definition, then load
    * every member named in `domainAspect.elementTypeNames`, and return a concrete
    * `Domain` whose `typeDictionary` is *populated* with the loaded definitions —
    * the non-hollow counterpart to `TypeDictionary.apply`. */
  def define(name: String, namePackage: Seq[String]): DomainType = {
    val domainDef = Generator.loadType(TypeName(name, _namePackage = namePackage))
    val members: Seq[TypeDefinition] =
      domainDef.domainAspect.elementTypeNames.map(m => loadMember(m, namePackage))

    val populated = new TypeDictionary {
      override lazy val elementTypes: Seq[TypeDefinition] = members
      override lazy val kvMap: Map[TypeName, TypeDefinition] =
        members.map(td => (td.typeName, td)).toMap
      override lazy val typeDefinition: TypeDefinition = TypeDictionary.typeDefinition
    }

    new Domain[Any] {
      override lazy val typeDefinition: TypeDefinition = domainDef
      override lazy val typeDictionary: TypeDictionary = populated
    }
  }

  /** Assemble the cross-domain `DomainDictionary` from one or more already-defined
    * domains. Delegates to `DomainDictionary.apply`; because the supplied domains
    * carry populated `typeDictionary`s (from [[define]]), the resulting registry is
    * fully populated too. */
  def dictionary(domains: DomainType*): DomainDictionary =
    DomainDictionary(domains)

  /** Validate a built domain against the structural invariants the endogenous
    * draco domains are expected to uphold. Returns a list of human-readable
    * problems — empty means well-formed. This is the rigorous counterpart to
    * `generate`'s skeleton tolerance: `generate` *accommodates* an in-progress
    * user domain, while `validate` *reports* every hole, so first-party domains
    * (and, eventually, domain-expert-authored ones) can be held to zero.
    *
    * Checks (battery 1–2):
    *  1. Self-declaration — the domain's `domainAspect.typeName` matches its own
    *     `typeName` (it actually claims to be the domain it is loaded as).
    *  2. Completeness — every declared member resolves to real content, not the
    *     empty (stub) `TypeDefinition` a missing JSON would yield.
    *  3. Derivation resolvability — every draco-internal ancestor named in a
    *     member's `dracoAspect.derivation` itself resolves to a definition, so no
    *     member claims an inheritance chain that dangles. External supertypes
    *     (non-`draco` packages, e.g. Pekko/Evrete) are out of scope and skipped.
    */
  def validate(domain: DomainType): Seq[String] = {
    val td = domain.typeDefinition

    val selfDeclaration: Seq[String] =
      if (td.domainAspect.typeName.name == td.typeName.name &&
          td.domainAspect.typeName.namePackage == td.typeName.namePackage) Nil
      else Seq(
        s"domain ${td.typeName.namePath} does not self-declare: " +
          s"domainAspect.typeName is ${td.domainAspect.typeName.namePath}")

    val members = domain.typeDictionary.elementTypes

    val completeness: Seq[String] = members.collect {
      case m if isStub(m) =>
        s"member ${m.typeName.name} is declared but unauthored (no JSON on disk)"
    }

    val derivation: Seq[String] = members.flatMap { m =>
      m.dracoAspect.derivation
        .filter(_.namePackage.headOption.contains("draco"))
        .collect {
          case anc if isStub(Generator.loadType(anc)) =>
            s"member ${m.typeName.name} derives from ${anc.namePath}, " +
              s"which does not resolve to a definition"
        }
    }

    selfDeclaration ++ completeness ++ derivation
  }

  /** Generate Scala for an entire domain — the domain object itself plus every
    * member of its dictionary — keyed by `TypeName`. Skeleton-tolerant: a member
    * with no complete definition (a stub) still emits whatever `Generator.generate`
    * produces for a thin TD; should generation of one member fail, a clearly
    * marked placeholder skeleton is emitted in its place so a single bad member
    * never sinks the whole batch. */
  def generate(name: String, namePackage: Seq[String]): Map[TypeName, String] = {
    val domain = define(name, namePackage)
    val all: Seq[TypeDefinition] = domain.typeDefinition +: domain.typeDictionary.elementTypes
    all.map(td => td.typeName -> safeGenerate(td)).toMap
  }

  private def safeGenerate(td: TypeDefinition): String =
    try Generator.generate(td)
    catch { case t: Throwable => placeholderSkeleton(td, t) }

  /** Last-resort skeleton when `Generator.generate` throws on an incomplete member.
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
