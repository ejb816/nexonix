# Draco Dev Journal — Chapter 9

**Session date:** March 23, 2026 (continued)
**Topic:** DomainDefinition Creation and Dissolution, Domain[T,U] Experiment, Actor.apply Fix, ActorDefinition Codecs, TypeName Redesigns, TypeDefinition Full Unification, Generator Fixes

---

## Session Start — Replace DomainName with DomainDefinition

> **Dev:** Implement the following plan:
> # Replace DomainName with DomainDefinition
> *(A structured 7-phase plan to replace the lightweight `DomainName` (typeName + elementTypeNames) with a richer `DomainDefinition` carrying superdomain, source, and sink fields — making domain definitions consistent with `RuleDefinition`)*

**Draco:** *(Reads all affected files in parallel: DomainName.scala, DomainType.scala, Domain.scala, TypeDictionary.scala, ServiceDomain.scala, RuleDefinition.scala, Generator.scala, Draco.scala, Base.scala, Primes.scala, all transform test domains, TestTypeModule.scala, DomainNameTest.scala)*

All seven phases executed:

### Phase 1: DomainDefinition.scala Created

New sealed trait with 5 fields following the RuleDefinition pattern:

```scala
sealed trait DomainDefinition extends TypeInstance {
  val typeName: TypeName
  val elementTypeNames: Seq[String]
  val superdomain: TypeName
  val source: TypeName
  val sink: TypeName
}
```

Companion with `apply()` (defaults: `Seq.empty`, `TypeName.Null`, `TypeName.Null`, `TypeName.Null`), `Null`, encoder with elision (TypeName fields elided when `name.isEmpty`), decoder with `Option[T].getOrElse` defaults.

### Phase 2: Core Framework Types Updated (4 files)

- **DomainType.scala** — `domainName: DomainName` → `domainDefinition: DomainDefinition`
- **Domain.scala** — `_domainName: DomainName` → `_domainDefinition: DomainDefinition`
- **TypeDictionary.scala** — factory param and apply method updated, Null uses `DomainDefinition.Null`
- **ServiceDomain.scala** — `.domainName.typeName` → `.domainDefinition.typeName`

### Phase 3: Generator.scala Updated

- Added `case "TypeName"` to `elisionCheck` → `s"$instanceVar.${p.name}.name.nonEmpty"`
- `domainInstanceLiteral(objName, dn: DomainName)` → `(objName, dd: DomainDefinition)` with optional superdomain/source/sink fields
- `domainGlobal` and `generate(td, dn)` signatures updated

### Phase 4+5: All Domain Companions and Test Files Updated

Draco, Base, Primes, Alpha, Bravo, Charlie, Delta, DataModel — all `DomainName(...)` → `DomainDefinition(...)`, `_domainName` → `_domainDefinition`. TestTypeModule — import renamed, 7 variable names changed. DomainNameTest → DomainDefinitionTest.

### Phase 6: DomainName.scala and DomainNameTest.scala Deleted

### Phase 7: Documentation Updated

CLAUDE.md key files table, Generator signatures. README.md domain description and code example. CHANGELOG.md entry. Auto-memory updated.

Final check: `grep -r DomainName src/` returned nothing (only CHANGELOG.md historical reference).

---

## Actor.apply Fix

> **Dev:**
> ```
> /Users/edwardburke/IdeaProjects/nexonix/src/main/scala/draco/Actor.scala:26:108
> unbound wildcard type
>     override def receiveSignal(ctx: TypedActorContext[T], msg: Signal): Behavior[T] = Behavior.isUnhandled[_]
> ```

**Draco:** *(Reads Actor.scala)* *(Edits Actor.scala)*

The wildcard `[_]` can't be used here — Scala needs the concrete type parameter `T` since you're returning `Behavior[T]`.

Two issues: `Behavior.isUnhandled` isn't a factory method (it's a check), and the wildcard `[_]` isn't valid in that position. The correct Pekko API is `Behaviors.unhandled[T]`, which returns a `Behavior[T]` that marks messages as unhandled.

---

## The Domain[T,U] Experiment

> **Dev:** I've changed Domain.scala from Domain[T] to Domain[T,U], but my attempt is incorrect. The U type parameter is added in the trait declaration after a "with" and I'm getting this error:
> ```
> class type required but U found
> trait Domain[T,U <: Class[U]] extends DomainType with U
> ```

**Draco:** The problem is that Scala doesn't allow mixing in a type parameter directly — `with U` requires a concrete trait/class at compile time, not a type variable. Three alternatives:

1. **Self-type constraint** — `trait Domain[T, U] extends DomainType { self: U => }` — enforces at instantiation
2. **Drop the `U` parameter** — keep `Domain[T]` and mix in the domain trait at the use site: `new Domain[Primes] with Primes { ... }`
3. **Upper bound without mixin** — `trait Domain[T, U <: DomainInstance] extends DomainType` — phantom/marker type only

> **Dev:** Your option 2 makes the most sense for creating a type instance, I can add the superdomain type when I provide the actual type for the type parameter.

**Draco:** *(Reverts Domain.scala back to `Domain[T]`, fixes Primes.scala back to `Domain[Primes]`, updates Generator's generated parameter names to match the user's renames — `superDomain`, `target`)*

---

## Actor.apply Unhandled Fix

> **Dev:**
> ```
> unbound wildcard type
>     override def receiveSignal(...): Behavior[T] = Behavior.isUnhandled[_]
> ```

**Draco:** Two issues: `Behavior.isUnhandled` is a check not a factory, and the wildcard `[_]` isn't valid in that position. The correct Pekko API is `Behaviors.unhandled[T]`.

*(Changes both `receive` and `receiveSignal` to return `Behaviors.unhandled[T]`)*

---

## ActorDefinition Codecs

> **Dev:** Can you "manually" add the Codec declarations for ActorDefinition, and also for ActorDefinition.Null, consistent with DomainDefinition?

**Draco:** *(Reads ActorDefinition.scala and DomainDefinition.scala for the pattern)*

Added to ActorDefinition:

- `apply()` with defaults (`TypeName.Null` for domains, `Action.Null` for actions)
- `Null` instance via `ActorDefinition(TypeName.Null)`
- Encoder with elision: `sourceDomain`/`targetDomain` elided when `name.isEmpty`, `messageAction`/`signalAction` elided when `body.isEmpty`
- Decoder with `Option[T].getOrElse(default)` for all optional fields
- Fixed bug: `_elements` listed `messageAction` and `signalAction` as `"TypeName"` — corrected to `"Action"`
- Changed `override val` to `lazy val` for `typeDefinition` and `typeInstance` (DelayedInit rule)

---

## TypeName Redesign — Role-Specific Paths

Dev had redesigned TypeName to replace the generic `parent`/`fullName`/`resourcePath` with role-specific triples: `domainName`/`domainNamePath`/`domainResourcePath`, `actorName`/`actorNamePath`/`actorResourcePath`, `ruleName`/`ruleNamePath`/`ruleResourcePath`.

> **Dev:** Can you fix Generator based on the changes I just made to TypeName and TypeDefinition?

**Draco:** *(Reads all three files. Identifies broken references: `fullName` gone, `parent` gone. Adds `typeNameLiteral(tn)` helper to Generator. Fixes `.newRule()` to use `domainNamePath`. Fixes `domainInstanceLiteral` to use `typeNameLiteral` instead of raw `.parent` references. Fixes `TypeDefinition.load` to use `domainResourcePath`.)*

### Discussion: TypeName Design Intent

> **Dev:** Let's discuss the changes I made to TypeDefinition and TypeName so I can decide how to proceed.

**Draco:** *(Flags five observations: actor/rule paths both use "actors" subdirectory, paths use `domainName` instead of respective name fields, encoder bug writes `domainName` for all three, `TypeDefinition.load` references nonexistent `resourcePath`, and the core design question of where role context should live)*

> **Dev:** The domainName property references the domain type that contains the type definition. If name and domain name are the same, then that means the type definition will generate a domain type. With actorName and ruleName, either one or both may have an empty string value, and either one being non-empty string means that the json data descriptor in the ruleResourcePath will be used to generate the rule type, and the json data descriptor in the actorResourcePath will be used to generate the actor type.

**Draco:** That clarifies the design. With that understanding, the path derivations should use: actor paths → `actorName` + `"actors"` subdir, rule paths → `ruleName` + `"rules"` subdir. Three bugs identified against this intent:

1. Rule paths use `"actors"` instead of `"rules"`
2. Actor and rule paths use `domainName` instead of `actorName`/`ruleName`
3. Encoder writes `domainName` for both `actorName` and `ruleName` fields

> **Dev:** yes *(to: "Want me to fix all three?")*

**Draco:** *(Fixes all three in TypeName.scala, plus `TypeDefinition.load` using `domainResourcePath`)*

---

## Fixing typeName.parent References

> **Dev:** Now fix any declared code that references "typeName.parent"

**Draco:** *(Greps — only one reference found)*

`TypeDictionary.scala` line 30: `TypeName(name, _domainDefinition.typeName.parent)` — `parent` no longer exists. Element types now inherit the domain's `namePackage` directly:

```scala
// Before:
TypeDefinition (TypeName (name, _domainDefinition.typeName.parent))
// After:
TypeDefinition (TypeName (name, _namePackage = _domainDefinition.typeName.namePackage))
```

---

## SLF4J Logging Noise — Plan

> **Dev:** I would like to eliminate the following messages from the runtime output:
> ```
> SLF4J(W): Class path contains multiple SLF4J providers.
> SLF4J(W): Found provider [ch.qos.logback.classic.spi.LogbackServiceProvider@401fde0a]
> SLF4J(W): Found provider [org.slf4j.jul.JULServiceProvider@46168708]
> ...
> Slf4jLogger started
> SLF4J(W): A number (1) of logging calls during the initialization phase...
> ```

**Draco:** Two root causes identified: dual SLF4J providers (`logback-classic` + `slf4j-jdk14` in build.sbt) and Pekko startup INFO with no logback config to suppress it.

Plan proposed:
1. Remove `slf4j-jdk14` from `build.sbt` (redundant with logback-classic)
2. Add `src/test/resources/logback-test.xml` with Pekko at WARN

*(Plan pending user review — to be implemented in the next session)*

---

## SLF4J Logging Noise — Initial Report

> **Dev:**
> ```
> SLF4J(W): Class path contains multiple SLF4J providers.
> SLF4J(W): Found provider [ch.qos.logback.classic.spi.LogbackServiceProvider@401fde0a]
> SLF4J(W): Found provider [org.slf4j.jul.JULServiceProvider@46168708]
> SLF4J(W): See https://www.slf4j.org/codes.html#multiple_bindings for an explanation.
> 23:16:41.545 [naturalActor-pekko.actor.default-dispatcher-3] INFO org.apache.pekko.event.slf4j.Slf4jLogger -- Slf4jLogger started
> ```

**Draco:** *(Launches agent to explore logging config and deps. Enters plan mode.)*

These are two distinct issues: SLF4J dual-provider warnings and Pekko logger startup noise. The root causes are clear — plan pending implementation.

*(Plan deferred to next session — implemented in Chapter 10)*

---

## Session Summary

Major accomplishments this session:

1. **DomainDefinition created** — Replaced `DomainName` with richer `DomainDefinition` (typeName, elementTypeNames, superdomain, source, sink), consistent with `RuleDefinition`. Updated all core types, Generator, domain companions, and test files. Deleted `DomainName.scala`.
2. **ActorDefinition codecs** — Added `apply()`, `Null`, encoder/decoder to `ActorDefinition` following the `DomainDefinition` pattern. Fixed element types listed as `"TypeName"` instead of `"Action"`.
3. **Domain[T,U] experiment** — Attempted parameterized mixin (`with U`), hit Scala's static mixin limitation, decided to keep `Domain[T]` and mix in superdomain at use site
4. **Actor.apply fix** — `Behavior.isUnhandled[_]` → `Behaviors.unhandled[T]`
5. **TypeName redesign discussion** — Dev redesigned TypeName with role-specific triples (domainName/actorName/ruleName + 6 derived paths). Draco fixed three bugs: rule paths used `"actors"` instead of `"rules"`, actor/rule paths used `domainName` instead of respective name fields, encoder wrote `domainName` for all three.
6. **Generator updated for TypeName changes** — Added `typeNameLiteral` helper, fixed `.newRule()` to use `domainNamePath`, fixed `domainInstanceLiteral` to use `namePackage` instead of removed `parent`, fixed `TypeDefinition.load` to use `domainResourcePath`
7. **typeName.parent references fixed** — `TypeDictionary` updated to use `_namePackage` instead of removed `parent` field
8. **SLF4J logging plan** — Identified dual-provider and Pekko startup noise, plan pending implementation
