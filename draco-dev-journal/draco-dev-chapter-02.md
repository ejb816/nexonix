# Draco Dev Journal — Chapter 2

**Session date:** March 23, 2026 (continued)
**Topic:** lazy val Normalization, DelayedInit Discovery, Rule Companion Fix, Generator Pre-Requisite Planning

---

## Session Start — Continuation

This session continued from a previous conversation that ran out of context. A detailed summary was provided covering extensive prior work on companion object consistency updates, import generation discussions, Generator updates, TypeElement fixes, and lazy val normalization.

---

## The typeInstance lazy val Sweep

> **Dev:** It looks like 'val typeInstance' needs to be 'lazy val typeInstance' everywhere we updated. However, there is an additional issue in RuleInstance that goes beyond that problem.

**Draco:** *(Reads all companion objects across the codebase to audit the current state of `typeInstance` declarations)*

Two tasks identified:

1. Change `val typeInstance` to `lazy val typeInstance` everywhere we updated
2. Investigate the RuleInstance issue

For task 1, the user had already changed Pattern, Action, Condition, Variable, Factory, Rule, and RuleInstance to `lazy val typeInstance`. 28 more locations still had `val typeInstance`.

For task 2, the issue was in `Rule.scala` line 39:

```scala
object Rule extends App with RuleInstance {
  // ...
  override val ruleInstance: RuleType = ???
}
```

`Rule` companion extended `RuleInstance`, which requires providing `val ruleInstance: RuleType`. But `Rule` is the generic container type (analogous to `Type[T]`), not a concrete rule — it shouldn't implement `RuleInstance`. When any rule companion calls `Rule[T](...)`, it triggers `Rule` object init, which hits `???` and crashes.

**Fix:** Changed `Rule` companion from `extends App with RuleInstance` to `extends App with TypeInstance`, removed the `ruleInstance = ???` line.

Applied `lazy val typeInstance` across all 28 remaining companion objects:
- **TypeElement.scala**: BodyElement, TypeElement, Fixed, Mutable, Dynamic, Parameter, Monadic (7)
- **Core draco**: TypeDefinition, TypeInstance, TypeName, Type, Primal, Value, RuleDefinition, RuleType, DomainName, DomainType, DomainInstance, DomainDictionary, TypeDictionary, Domain, ActorBehavior, RuleActorBehavior, Service, ServiceDomain, ContentSink, SourceContent, Main, Test, Generator, Draco
- **draco.base**: Unit, Cardinal, Ordinal, Nominal, Distance, Rotation, Meters, Radians, Coordinate
- **draco.primes**: Accumulator, Numbers, PrimeOrdinal, Primes
- **draco.dreams**: Transform
- **draco.primes.rules**: AddNaturalSequence, PrimesFromNaturalSequence, RemoveCompositeNumbers

Generator templates also updated to emit `lazy val typeInstance` in both `generate(td)` and `generate(rd)`.

Only two `val typeInstance` remained, both correct:
- `DracoType.scala:8` — the bootstrap axiom (doesn't extend TypeInstance)
- `TypeInstance.scala:4` — the abstract trait field declaration

> **Dev:** Running AddNaturalSequence ran without any compile or execution errors anywhere.

---

## The DelayedInit Discovery

> **Dev:** *(Pastes full stack trace from running AddNaturalSequence)*
> ```
> Exception in thread "main" java.lang.NullPointerException:
> Cannot invoke "draco.TypeDefinition.typeName()" because the return value of "draco.Type.typeDefinition()" is null
>     at draco.primes.rules.AddNaturalSequence$.delayedEndpoint$draco$primes$rules$AddNaturalSequence$1(AddNaturalSequence.scala:44)
>     at draco.primes.rules.AddNaturalSequence$delayedInit$body.apply(AddNaturalSequence.scala:10)
>     at scala.App.$anonfun$main$1(App.scala:98)
> ```

**Draco:** The root cause is `extends App`. In Scala 2, `App` uses `DelayedInit`, which delays ALL `val` initializers in the object body until `main()` is called. When AddNaturalSequence runs its `main()` and accesses `RuleInstance.typeInstance`, the `lazy val` correctly defers and evaluates — but it calls `Type[RuleInstance](typeDefinition)` where `typeDefinition` is a plain `val` in RuleInstance's delayed body. Since RuleInstance's `main()` hasn't been called, `typeDefinition` is still null.

The fix: `typeDefinition` must also be `lazy val` in all `extends App` companions, so it evaluates on first access rather than being trapped in the delayed init body. Same issue applies to `domainInstance` and `knowledgeService`.

The key insight: `lazy val` bypasses `DelayedInit` because it's compiled as a method with a bitmap check — it evaluates on first access regardless of whether the App body has run.

Applied `lazy val typeDefinition` across all 47 companion objects. Also changed:
- `domainInstance` → `lazy val` in Draco, Base, Primes
- `ruleInstance` → `lazy val` in AddNaturalSequence, PrimesFromNaturalSequence, RemoveCompositeNumbers
- `knowledgeService` → `lazy val` in Rule
- `override val typeDefinition` → `override lazy val typeDefinition` in Draco
- Generator templates updated to emit `lazy val` for `typeDefinition`, `typeInstance`, and `ruleInstance`

Private vals left as `val`: `ruleDefinition`, `action`, `pattern` in rule companions — these are only used within the same object's delayed body, where they execute sequentially.

> **Dev:** Running AddNaturalSequence ran without any compile or execution errors anywhere.

**Draco:** The `DelayedInit` issue is fully resolved. Every non-private field in an `extends App` companion is now `lazy val`, so cross-object access always evaluates correctly regardless of initialization order.

---

## The Corrected lazy val Rule

The corrected rule is clean and simple: in an `extends App` companion, if another object might touch it, make it `lazy val`. Private fields used only within the same body are fine as `val`.

Updated memory with the full rule:

- `typeDefinition`, `typeInstance` → `lazy val` (bypasses DelayedInit)
- `domainInstance`, `ruleInstance`, `knowledgeService` → `lazy val` (accessed cross-object)
- `Null`/`Default` → `lazy val`
- `codec` vals → `private lazy val`
- Private vals used only within same body (action, pattern, ruleDefinition) → fine as `val`
- DracoType.typeInstance is the sole exception: `val` (axiom, doesn't extend App)

---

## Pre-Requisites for Module Generation

> **Dev:** Can you think of anything we should do before we add generating subtype modules to the Generator?

**Draco:** Two things stand out:

**1. The Generator's `apply` body is incomplete.** The `companionObject` method calls `factoryBody(factory.body)`, but `factory.body` is always `Seq.empty` in every TypeDefinition. So the generated `apply` method produces `new TypeName` with an empty body — no override assignments.

The Generator needs to:
- Auto-generate `override val` assignments by mapping factory parameters to element fields (`_name` → `override val name = _name`)
- Always append `override val typeDefinition: TypeDefinition = typeInstance.typeDefinition`

**2. Test the Generator output before adding complexity.** We've never verified that the current `generate(td)` produces correct output. We should test it against a simple type and compare.

---

## Generator as Singleton

> **Dev:** First tell me if there is a natural use case for having multiple instances of the same source code generator.

**Draco:** No. A Generator is a singleton utility — it transforms TypeDefinitions and RuleDefinitions into source code strings. There's no state that would vary between instances. The current `Generator.apply(typeDictionary: TypeDictionary)` factory creates `new Generator {}` which is an empty trait instance — it does nothing. All the real work is in the companion's `def generate(td)` and `def generate(rd)` methods, which are already static.

So `Generator` should just be its companion object. The trait and factory are vestigial.

> **Dev:** The factory use is but the trait is where the generator method signatures belong, as well as fields defining the existence of and interactions with the programming languages that are supported by the Generator.

**Draco:** Right. The trait defines the Generator's contract — method signatures for `generate(td)` and `generate(rd)`, plus fields for describing supported target languages. The companion object is the Scala-specific implementation of that contract. So the factory and `apply` method should go, but the trait stays and gains the abstract declarations.

---

## Language-Neutral Naming

> **Dev:** Any name used in the (as it is named in Scala) "trait" should avoid being specific to any single programming language. So, as a principled example, the name should be equally appropriate for generating either Scala, Haskell, F# or TypeScript.

**Draco:** That's a strong design constraint. Looking at the current Generator, names like `typeModifier`, `typeExtends`, `typeBody`, `companionObject`, `factoryParameters`, `factoryBody` are all Scala-centric.

The trait's vocabulary should describe what's being generated structurally — type declarations, type constructors, subtype discriminators — not how any particular language spells them.

> **Dev:** Are there names for those you say are Scala specific that would adequately represent the functions being performed without being Scala specific or object-oriented specific?

**Draco:** Walking through each:

- `typeExtends` → **derivation** is already Draco's own term
- `typeBody` → **elements** — also Draco's term
- `typeModifier` → **modules** — the qualifier follows from whether modules is non-empty
- `factoryParameters` / `factoryBody` → **Factory** is already a Draco concept, language-neutral
- `nullInstance` / `defaultInstance` → `nullValue` / `defaultValue` (instance is OO-specific)
- `methodParameters` / `methodBody` → just **parameters** / **body**

The hard one is `companionObject`. It houses the type's self-description, value constructor, and distinguished values. There isn't a universal name across paradigms for "the module associated with a type."

The pattern: the names that work best are the ones that already come from Draco's own domain model rather than from any target language. Derivation, elements, factory, modules — these are Draco concepts that map to different syntax in each target language.

> **Dev:** How about companionObject => typeGlobals?

**Draco:** That captures the concept well — everything in the companion is type-level rather than instance-level, i.e. global to the type.

The one issue: `typeGlobals` already exists as a field on TypeDefinition. That field represents *additional* module-level declarations beyond the standard ones (typeDefinition, typeInstance, factory, null, default). So the field is a subset of what the method would generate, creating a naming ambiguity.

> **Dev:** We could change the current typeGlobals to globalElements and then let companionObject be typeGlobal (singular).

**Draco:** That's clean. `globalElements` is a `Seq[BodyElement]` — plural, a collection of elements. `typeGlobal` is the singular structural concept — the type's global scope that contains everything: self-description, constructor, distinguished values, and the global elements. The naming is consistent with Draco's vocabulary and the singular/plural distinction makes the relationship clear.

---

## The Three-Part Pre-Requisite Plan

> **Dev:** Make those two renames and fix the factory body generation.

**Draco:** *(Enters plan mode, launches exploration and planning agents)*

Designed a three-part plan:

1. **Rename `typeGlobals` → `globalElements`** — 25 references across 9 source files plus 5 JSON resource files and CHANGELOG.md
2. **Rename `companionObject` → `typeGlobal`** — plus `typeGlobalsDeclaration` → `globalElementsDeclaration` and `hasGlobals` → `hasGlobalElements` within Generator.scala
3. **Fix factory body generation** — replace `factoryBody(factory.body)` (always empty) with `factoryBody(factory.parameters)` that generates proper `override val` assignments from factory parameters plus `override val typeDefinition`

*(Plan was presented but session ended before implementation began)*
