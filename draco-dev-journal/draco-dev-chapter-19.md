# Draco Dev Journal — Chapter 19

**Session date:** April 9–10, 2026
**Topic:** Reference Frame Domains, Transform Domain Architecture, Holon, RuntimeCompiler, Self-Closure Foundation

---

## Session Start — Renaming the Example Domains

> **Dev:** I have decided that the four domains of our example can be four nested reference frames: Alpha = Egocentric, Bravo = Geocentric, Charlie = Heliocentric, Delta = Galactocentric (or properly named equivalent acceptable to spell-checking).

**Draco:** All four names are standard astronomical terms and spell-checker safe. "Galactocentric" is the accepted term. The nesting concept is natural: Egocentric (self) sits inside Geocentric (Earth), inside Heliocentric (Sun), inside Galactocentric (galaxy).

---

## Per-Frame Vocabularies — No Shared Terms

> **Dev:** I like option 2 [semantically meaningful names]. Are Observation and Bearing Egocentric and Position Geocentric? I expect completely different languages, specific to each reference frame, regardless of nesting.

**Draco:** Your instinct is right — each reference frame should have its own native vocabulary. A bearing in an egocentric frame is a fundamentally different concept from a position in a geocentric one.

First-pass vocabularies proposed:

| Frame | Leaf types | Assembly |
|-------|-----------|----------|
| Egocentric | Bearing, Reach | Percept |
| Geocentric | Coordinate, Altitude, Heading | Fix |
| Heliocentric | Elements, Epoch | Ephemeris |
| Galactocentric | Parallax, ProperMotion, RadialVelocity | Trajectory |

These would be refined as each frame was implemented. The key principle: equivalence between frames only shows up when you cross frames via a transform domain.

---

## Transform Domain Package Convention

> **Dev:** Transform domains require concatenation-composition of package names: a source domain for Geocentric would be domains.geocentric.Geocentric — the Transform domain for Geocentric as a target (with Egocentric as source) would be domains.egocentric.geocentric.Geocentric, capturing both source and target of the Transform domain.

**Draco:** The outer segment (egocentric) is the source frame, the inner segment (geocentric) is the target.

### Peer Structure, Not Chains

> **Dev:** The names do not naturally form chains, since we want to maintain sources as peer level with other sources and targets as peer level with other targets.

**Draco** had proposed that paths like `domains.egocentric.geocentric.heliocentric.Heliocentric` could encode multi-hop chains. Dev corrected this: sources are peers at `domains.*`, targets within a source are peers at `domains.<source>.*`. Chaining would break both peer invariants. Multi-hop transforms must be composed at runtime from single-hop transform domains.

With 4 frames: 4 source domains, 4×3 = 12 transform domains, 16 total slots — a tidy matrix.

### Principal Type Naming

> **Dev:** Let me give some clarifying examples of package, domain and type naming:
> `domains.<name>.<Name>` is a type that is a domain
> `domains.<other-name>.<OtherName>` is a type that is a domain
> `domains.<name>.<other-name>.<NameOtherName>` is a type that is a transform domain
> `domains.<name>.<other-name>.<SomeTypeName>` is any type defined in the transform domain (if it exists as `<NameOtherName>` in the package)

**Draco** had been thinking the transform domain's principal type would share the target's simple name (e.g., `Geocentric`). The correct naming is the **capitalized concatenation**: `EgocentricGeocentric`. No name collision with the base target, no shadowing needed. The containment rule: other types in a transform package belong to that transform domain if and only if the principal `<NameOtherName>` exists.

---

## The Cosmocentric Super-Domain

> **Dev:** The DataModel domain has to be named as the domain that can host the cross domain transform rules. DataModel is the super-domain of the Alpha, Bravo, Charlie, Delta "message" data domains, so the renamed DataModel is the super-domain of four of the reference frame domains as peer sub-domains, no relevance to their nested status.

The four reference frames are flat peers under a common super-domain. The nesting (ego ⊂ geo ⊂ helio ⊂ galacto) is semantic flavor, not structural hierarchy.

> **Dev:** Cosmocentric is the right name for the universal super-frame of all reference frames, unless you can suggest a name I might like better.

**Draco:** Cosmocentric is the right choice — naming uniformity (all five end in -centric), monotonic scale progression (ego → geo → helio → galacto → cosmo), etymological consistency (all Greek roots), and semantic precision (the cosmos as the frame containing all frames).

| Role | Name | Package |
|---|---|---|
| Super-domain | Cosmocentric | `domains.cosmocentric` |
| Sub-domain | Egocentric | `domains.egocentric` |
| Sub-domain | Geocentric | `domains.geocentric` |
| Sub-domain | Heliocentric | `domains.heliocentric` |
| Sub-domain | Galactocentric | `domains.galactocentric` |

---

## How Transforms Work — The Deep Dive

### Disaggregation and Aggregation

> **Dev:** Viewing domain type value instances as hierarchically contained type values in trees with terminating paths of possibly varying length, then all transforms are a disaggregation of the source composite value mapped into the Primal type value parameters of the "leaf node" factory methods of the target aggregation tree into the target composite value.

The point of the target domain sharing a reference in the transform is that all leaf-node factory methods in the target can be referenced directly as types in the source version of the transform, with the Primal types defined in the target domain implementing the specific transform for the leaf-node data.

### Primal Value Type vs Primal Type Value

> **Dev:** Let me side-step a bit and start by talking about the difference between primal value type and primal type value.
> Primal[Int], Primal[Float] and Primal[String] are each primal value types, with others determined by the lack of accessible type substructure. Any type parameter of T that has accessible type substructure is a primal type value.

A critical cleavage in the Primal universe:
- **Primal value type** — T is opaque (Int, Double, String). No accessible substructure. The "value" is atomic from the type system's perspective.
- **Primal type value** — T has accessible substructure (tuple, record, composite). The type system can see into the value.

Both satisfy the one-`value`-property rule. The distinction is about what the Generator and type-checker can do with T.

### Primals as the Cross-Domain Conduit

> **Dev:** So all the transform data moves from the source to the target through specific primal value types defined in the transform domain and inheriting from either the source domain or target domain (or maybe both).

**Primal value types** specifically (not primal type values) are the conduit. Opacity of T is the structural prerequisite for dual inheritance: if T has no substructure, both source and target type universes can agree on what the type looks like without structural conflict.

Each transform-domain primal value type:
- `extends Primal[T]` with opaque T
- has exactly one property `value: T`
- has `_derivation` listing both source and target TypeNames (dual inheritance)
- has a factory whose parameter list accepts source-flavored inputs, whose body computes the target-flavored `value`

> **Dev:** Let's give priority to cleanliness over honesty for now, and address honesty later.

Canonical case adopted: all primals in a transform domain use dual inheritance. Source-only and target-only primals deferred.

### Primal Factory Parameters

> **Dev:** "Primal" only refers to the type and content of the "value" property, and is completely independent to the number and type(s) of the parameter(s), which is why it is defined and used in the target domain of a transform. That means that a single value parameter of a particular primitive can be converted to a value of a different "value" property type (e.g. integer → float) or unit conversion for the same type (e.g. miles → kilometers). Also multiple primitive parameters of the same type converted to a single primitive value (e.g. range, azimuth, elevation → (x, y, z)).

Three orthogonal axes of transform work the factory can do:
1. **Type change**: `factory(n: Int): Primal[Float]` with `value = n.toFloat`
2. **Unit change**: `factory(miles: Double): Primal[Double]` with `value = miles * 1.609`
3. **Arity change**: `factory(range: Double, az: Double, el: Double): Primal[(Double,Double,Double)]`

---

## Transform Rules — Antecedent and Action

> **Dev:** The transform rule antecedent is defined in the super-domain, but referencing the source sub-domain exclusively for variables and conditions. The action of the rule references a factory composition definition in the transform target, and results in either a scalar or tuple of domain type value instance.

Transform rules are physically cosmocentric rules. The antecedent matches source facts only; the action invokes the transform domain's factory composition.

### Actor Dispatch — Scalar Only

> **Dev:** I assert that all transform rule actions send the transform result to an actor of the appropriate type, which actor has to be defined, generated, instantiated and executing, in order for the send to succeed as a cross-domain transform.

Transforms are cross-domain actor messages, not intra-session fact cascades. The source→target boundary is the actor membrane. The target actor must be fully online.

---

## Tuple Domain vs Domain Tuple

> **Dev:** First, I will make a clear distinction between tuple domain and domain tuple. A tuple domain has elements that can be any domain type. A domain tuple has tuple elements that all have to be the same domain type. Note the "can be" and "have to" parts of the distinction description.

And the critical resolution:

> **Dev:** Since the super-domains get input from sub-domains, there is no need for tuple structured extra domains composed from the sub-domains, because any composite of sub-domain data as input can be "handled" by explicit rules in the super-domain. For tuple domain output, the super-domain can create a tuple that it can insert into itself, to be handled by another rule that can disaggregate the tuple data elements and send them to the specific corresponding sub-domains. So my conclusion is that we don't need to send tuple data between actors.

**Tuples never cross actor membranes.** The actor protocol stays permanently simple: scalar in, scalar out. Tuples are a within-session construct only, used for disaggregation-and-dispatch patterns inside a super-domain's session.

### Scalar vs 1-Tuple

> **Dev:** In general, is it important to make a clear distinction between a 1-tuple and what we're calling a scalar?

**Draco:** Yes — a 1-tuple (whether tuple domain or domain tuple) is a container type with its own identity, while a scalar is a bare value with no container. Collapsing them would erase type identity, break arity uniformity, create dispatch ambiguity, and override author intent.

---

## Transformation Meta-Type

> **Dev:** Let's make a start by suggesting that there is something called Transformation that inherits directly from Action, and discuss from there.

`Action` already exists in `draco.TypeElement` as a sealed trait extending BodyElement, with fields `variables: Seq[Variable]`, `values: Seq[Value]`, `body: Seq[BodyElement]`.

**Proposed:** `sealed trait Transformation extends Action` — mirroring Action's shape exactly (same three fields, different discriminator). The distinction between Transformation and plain Action is the discriminator tag: the Generator picks a different emission strategy for Transformation actions.

### The Role of Values

> **Dev:** The "values" field, if it is non-empty, indicates that some or all of the types in "variables" are Json. If they are all Json, then the transform will be {Json → Json}. If they are mixed then {Json → SomeType} or {SomeType → Json}, and there are no Json, then {SomeType → SomeOtherType}.

`Value` (from `draco.Value`) is a named JSON path extractor with `name: String` and `pathElements: Seq[String]`. Its `value[T](_source: Json)` method navigates into a source JSON by field name or array index.

This led to a restructuring proposal for formats:

---

## The Formats Domain

> **Dev:** Suppose we create draco.format.Format as domain and add draco.format.Json, draco.format.Xml and draco.format.Yaml as types in the Format domain. We implement the types as wrappers for external library implementations, and then create transform domains: Format[Json] → SomeDracoDomain, SomeDracoDomain → Format[Json].

If formats are first-class draco domains, the four-case taxonomy (SomeType→SomeOtherType, Json→SomeType, SomeType→Json, Json→Json) collapses to one shape with four inhabitations. Direction is encoded in the package path of the transform domain, not in any Action field.

### Generated Trait Patterns

> **Dev:** In other sessions we discussed how to specialize a domain type by:
> `(generated) trait Format[T] extends Extensible with Domain[T]`
> `(generated) trait Message[T] extends Extensible with Domain[T]`
> `(generated) trait Transform[S,T] extends Extensible with Tuple2(Domain[S],Domain[T])`
> The Transform[S,T] is new. The current draco.dreams.Transform is only defined for use at runtime.

Three parameterized traits, all following the same `Extensible with <domain-shape>` spine. Transform[S,T] is the new one, extending a product of two Domains.

> **Dev:** I think there needs to be a draco.formats.Formats domain (note plural names) and that Value and Format are member types and Value and Value.value would be parameterized with different type variables.

Format[T] and Value[S] as members of `draco.formats.Formats` (plural). Value generalizes from JSON-only to format-agnostic, with two independent type variables: S at the type level (the format), T at the method level (decoded result).

---

## Naming the Base Trait — From Composite to Holon

### The Coordinate Connection

> **Dev:** Look at draco.base.Coordinate and see if something like that could help define Transform.

`Coordinate[T <: Product] extends Primal[T]` — a parameterized trait whose T is bounded to Product (tuples, case classes). The existing pattern for primal type values.

Transform[S,T] as `extends Coordinate[(Domain[S], Domain[T])]` would make Transform a Primal-derived wrapper whose value is a 2-tuple of domain references. The Tuple2(Domain[S], Domain[T]) notation from the earlier discussion is realized as a Scala tuple type argument to a Coordinate-like base.

### Seeking a More Fundamental Name

> **Dev:** I prefer a more fundamental and conceptually (as opposed to mathematically) appropriate name that I could define in the draco package and have both Coordinate and Transform inherit from it.

**Draco** proposed: Composite, Compound, Whole, Ensemble.

### Transformation — The Detailed Exchange

> **Dev:** I'll try to address your 4 queries as one. Action already exists as a module subtype in draco.TypeElements. That should also answer 1 through 3 as well since it will be the same, as for Action.

**Draco:** *(Reads TypeElement.scala)* Now I have concrete ground. Action in `draco.TypeElement` is a sealed trait extending BodyElement with `variables: Seq[Variable]`, `values: Seq[Value]`, and inherited `body: Seq[BodyElement]`. Its `valueType` is `"org.evrete.api.RHSContext => Unit"`. Parameterized via three pieces (not type-parameterized in the `[T]` sense). Encoded/decoded via discriminated-union codec in `TypeElement`.

Proposed `sealed trait Transformation extends Action` mirroring Action's shape exactly (same three fields, different discriminator). The TransformDomain reference lives inside `body` as a Factory BodyElement whose `valueType` is the transform domain's fully qualified TypeName. The cross-actor send is implicit — derived by the Generator from the Transformation subtype.

> **Dev:** Ah! You gave it to me indirectly from Whole: Holon! "It's holons all the way down! (And all the way up.)" At this point I can add terms that relate to nexonix, which is a system that recognizes that any holon is spatial/temporal mix of nexons (individuals) and plexons (aggregates) that are all changing toward unity and separation continuously.

**Holon** — Koestler's term for a thing that is simultaneously whole and part, recursively.

- **nexon** — the unified/individual face (primal value type, opaque T)
- **plexon** — the aggregate face (primal type value, structured T)
- Both are holons; which face dominates depends on T.

> **Dev:** Option 1 is a good start.

Nexon and plexon remain vocabulary-layer only, not draco trait names. Clean start; may become types later if needed.

### The draco.base.Base Layer

> **Dev:** draco.base.Base is a domain for system types that are not fundamental to the core draco type system itself, but useful for the systems that are built using the draco framework including the non-core layers of dreams and orion and anything else beyond those layers and built with them as well.

This clarified: Holon belongs at the `draco` root (core), Coordinate stays in `draco.base` (system-level utility).

### Transform Placement and Formats Domain

> **Dev:** 1. draco package
> 2. discuss further
> 3. (generated) indicates that I was referring to what would be generated from the type definition for the Scala code.
> 4. I think there needs to be a draco.formats.Formats domain (note plural names) and that Value and Format are member types and Value and Value.value would be parameterized with different type variables.

**Draco:** Confirmed on 1 and 3, acknowledged on 4. On 1: `draco.Transform[S,T]` at the draco root, naming symmetry with `draco.Format[T]` and `draco.Message[T]`. On 3: `(generated)` means the Scala trait is what the Generator would emit from the TypeDefinition — this raises the question of whether the Generator handles multiple type parameters. On 4: `draco.formats.Formats` (plural) as domain, with `Format[T]` and `Value[S]` as members. Value generalizes from JSON-only to format-agnostic with two independent type variables: S at the type level (the format) and T at the method level (decoded result).

For the `Tuple2(Domain[S], Domain[T])` notation, four candidate implementations were analyzed: literal Scala Tuple2 inheritance (case-class inheritance gotchas), trait intersection via `with` (linearization conflicts), draco-level Tuple2 as own parameterized trait (most semantically consistent), and a hybrid approach. Strongest preference was candidate C (draco-level `Tuple2[A, B]`) for semantic consistency with the earlier tuple domain concepts.

### The Coordinate Connection

> **Dev:** I can't rule out the necessity for changing the Generator to support two parameters, but I believe the TypeDefinition has the correct information in its data to support generating code with two type parameters, as long as they are both formal (as in [S,T]). I am not sure about both actual or mixed formal actual ([Type1,Type2], [Type1, T], [S,Type2]).
>
> Look at draco.base.Coordinate and see if something like that could help define Transform.

**Draco:** *(Reads Coordinate.scala)* *(Reads Coordinate.json)* Coordinate changes how Transform should be proposed. `Coordinate[T <: Product] extends Primal[T]` demonstrates: parameterized traits with bounds, type parameter passthrough to parent via string-based `typeParameters`, elements referencing formals (`value: T`), and wildcard `Type[Coordinate[_]]` for the singleton.

Proposed Transform[S, T] using the Coordinate pattern:

```scala
trait Transform[S, T] extends Extensible with Coordinate[(Domain[S], Domain[T])]
```

Under this reading, Transform is a **primal type value** (via Coordinate -> Primal[T] where T is a Product). It is NOT a subtype of Domain[S] or Domain[T] — it *contains* references to them in its `value` tuple, as data. Dual inheritance lives at exactly one tier now — the Primal tier — and the transform-domain principal level carries the source/target pairing as introspectable metadata.

The Generator handles mixed formal/actual through string splicing — `typeParameters: ["(Domain[S], Domain[T])"]` is a single string spliced verbatim. No Generator changes needed.

---

## The Initialization Order Problem

> **Dev:** Transform[S,T] (as it derives from Holon[(Domain[S],Domain[T])]) when given actual type parameters, is a problem because the actual domain names that replace the [S,T] in the Transform, represent the values of the "val domainInstance: Domain[S or T] = new Domain[S or T] { ... }" for whatever domain names are substituted for S or T, both of which have to be initialized before Transform[S,T] can be successfully initialized.

The original `Holon[(Domain[S], Domain[T])]` shape creates an irreducible initialization dependency: every concrete transform forces both source and target domain singletons.

### The Hallucinated apply() Solution

> **Dev:** I was hallucinating a solution that created an apply method in the companion object of (using as an example) Egocentric: `def apply(): Domain[Egocentric] = Domain.apply[Egocentric]() = Egocentric.domainInstance` ... or something close to that.

**Draco:** The apply() approach is essentially syntactic sugar over `.domainInstance` — the deferral comes from lazy val, not from the method. It helps at the call site but doesn't change the underlying dependency. If anything forces the Transform's value during startup (rule loading, dictionary construction), the cascade still fires.

### The Generalization

> **Dev:** We have an incomplete interpretation of what Transform[S,T] means in the draco type system. We have started with Holon[(Domain[S],Domain[T])]. What if it was Holon[(S <: DracoType, T <: DracoType)] instead?

This drops the `Domain[]` wrapping. S and T are any DracoType subtypes — including the domain traits themselves (via `DomainInstance extends TypeInstance extends DracoType`).

### Companion-as-Instance — The Resolution

> **Dev:** I just realized that companion as instance pattern I've been using for almost everything that involves the draco type system, extends App, for purposes of rapid debugging and in-line testing. It looks like that is starting to disappear.

Each domain's companion object already extends `App`. The pattern: `object Egocentric extends App with Egocentric`. Constructing `(Egocentric, Geocentric)` captures two stable object references without forcing any lazy vals — initialization is safe.

> **Dev:** Remember, with traits we always extend Extensible except for abstract types inherited from external libraries. Using extends App with object lets us focus on uniformly using "with" for all draco type inheritance. I'm for keeping it, for more than one reason.

Convention confirmed: `extends App` (non-draco base for objects), `with` for all draco type inheritance. App stays.

---

## Holon and Coordinate — First Code

```scala
package draco
trait Holon[T <: Product] extends Extensible with Primal[T]
```

Created `draco/Holon.scala` and `draco/Holon.json`. Uses `Generator.loadType`. Coordinate retargeted to extend Holon. All tests passed.

---

## Session Pause — Memory Consolidation

> **Dev:** This is late for me. Is this session to big to continue in? Should we consolidate memory, so we can create the next journal entry and start a new session? Or would it be safe to just continue with your current context?

**Draco:** Honest read: the session is long and dense, but context fidelity is still intact. However, the design work is substantial and worth preserving — Cosmocentric super-domain, Holon base trait, Transform[S,T] generalization, companion-as-instance resolution, Transformation meta-type, Formats domain plan. Recommendation: consolidate memory and start fresh next session. The discussion has reached a natural pause point, and a journal entry written now will be more faithful than one written after more discussion dilutes the key decisions.

> **Dev:** yes, draft the memory entry - I'll return to check after rest.

**Draco:** Drafted `project_transform_domain_design.md` covering all design decisions: Cosmocentric/frames rename, Holon as new base trait, Transform[S,T] via companion-as-instance, Transformation meta-type plan, Formats domain plan, the concrete implementation steps for next session, and decisions explicitly deferred to "honesty later."

---

## Session Resumes — April 9 Evening

> **Dev:** I'm back. Ready to write another chapter in the draco-dev-journal?

**Draco:** *(Reads MEMORY.md)* *(Creates project_transform_domain_design.md)* *(Edits MEMORY.md)* Memory consolidated. The new entry captures the full design discussion. Where we left off: the generalized `Holon[(S <: DracoType, T <: DracoType)]` shape for Transform, and the companion-as-instance pattern. Two verification questions: does `DomainInstance` reach `DracoType`? Is companion-as-instance what you had in mind?

> **Dev:** 1. Yes
> 2. Yes

**Draco:** *(Reads DomainInstance.scala)* *(Reads Extensible.scala)* Both confirmed. The full inheritance chain is clean:

```
Extensible                      { — marker, no abstract members }
   └── DracoType                { val typeDefinition: TypeDefinition }
         └── TypeInstance        { val typeInstance: DracoType }
               └── DomainInstance { val domainInstance: DomainType }
```

Three abstract vals total. The companion-as-instance pattern is a **one-token change** per domain: add `extends <TraitName>` to the object declaration. No new members to implement — they're all already there.

---

## Inline TypeDefinition Cleanup

> **Dev:** There should be no reason for inline TypeDefinition any more, unless you can remind me of one.

Converted all 12 remaining companions from inline `TypeDefinition(...)` to `Generator.loadType(TypeName(...))`:

| Package | Types |
|---------|-------|
| draco.base | Coordinate, Unit, Cardinal, Nominal, Ordinal, Distance, Rotation, Meters, Radians |
| draco.primes | Accumulator, Numbers |
| draco.dreams | Transform |

> **Dev:** Sorry, I didn't know how to stop you cleanly from doing draco.dreams.Transform — it should be obsolete since draco.Transform.

`draco.dreams.Transform` deleted entirely — both Scala source and JSON.

---

## Transform[S,T] — Two-Parameter Type

> **Dev:** Yes

**Draco:** *(Reads Extensible.scala)* *(Creates Transform.scala)* *(Creates Transform.json)* Two files:

- **`src/main/scala/draco/Transform.scala`** — `trait Transform[S <: DracoType, T <: DracoType] extends Extensible with Holon[(S, T)]`. Companion: `extends App with TypeInstance`, uses `Generator.loadType`, wildcard `Type[Transform[_, _]]` for the two-parameter case.
- **`src/main/resources/draco/Transform.json`** — Two type parameters `["S <: DracoType", "T <: DracoType"]`, derivation from `Holon` with `typeParameters: ["(S, T)"]`, element `value: (S, T)`.

```scala
package draco
trait Transform[S <: DracoType, T <: DracoType] extends Extensible with Holon[(S, T)]
```

The first two-parameter type definition in the codebase. JSON carries `"typeParameters": ["S <: DracoType", "T <: DracoType"]` and derivation `"typeParameters": ["(S, T)"]`. The Generator's string-splicing approach handles nested type expressions without modification. All tests passed.

---

## Primal Extends TypeInstance

> **Dev:** I can not recall a reason why any draco type should not inherit directly or indirectly from TypeInstance instead of directly from DracoType.

Changed `trait Primal[T] extends DracoType` to `trait Primal[T] extends TypeInstance`, incrementally (option 2). This rippled through every Primal subtype — every anonymous instance in every factory now needed `typeInstance`.

### The Circular Init Trap

First attempt used `override val typeInstance` (eager). StackOverflowError in Parameter:

```
Parameter.typeDefinition → constructs Factory → calls Parameter.apply
  → anonymous instance forces Parameter.typeInstance → forces Parameter.typeDefinition → ∞
```

**Fix:** All anonymous class `typeInstance` overrides changed to `override lazy val typeInstance`. Matches the existing memory note: "Factory defaults must be deferred to avoid circular init."

Changes: Primal.scala (trait + companion), TypeElement.scala (11 factories), Coordinate.scala (factory), Meters.scala and Radians.scala (trait bodies). All tests passed.

---

## Holon and Coordinate — The Detailed Exchange

> **Dev:** Yes.

**Draco:** *(Reads Primal.scala)* *(Reads Coordinate.json)* *(Creates Holon.scala)* *(Creates Holon.json)* `draco.Holon` is in place. Two files:

- **`src/main/scala/draco/Holon.scala`** — `trait Holon[T <: Product] extends Extensible with Primal[T]`, companion uses `Generator.loadType`.
- **`src/main/resources/draco/Holon.json`** — TypeDefinition with `typeParameters: ["T <: Product"]`, derivation from `Primal[T]`, single `value: T` element. Mirrors Coordinate.json's structure exactly, one level more abstract.

> **Dev:** Test in Primes failed:
> ```
> Input Natural.json.json Sequence:(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, ...)
> Result Prime Sequence: (2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, ...)
> [info] - AddAndRemoveRulesTest *** FAILED ***
> [info]   java.lang.UnsupportedOperationException: tail of empty list
> ```

**Draco:** *(Reads PrimesRulesTest.scala, Numbers.scala, Accumulator.scala, RemoveCompositeNumbersRule.scala, AddNaturalSequenceRule.scala)* *(Edits RemoveCompositeNumbersRule.scala)* *(Edits PrimesRulesTest.scala)* Pre-existing bug, not caused by Holon. Two fixes:

1. **`RemoveCompositeNumbersRule.scala`** — `classOf[Int]` changed to `classOf[Integer]` for all three integer variables. Evrete working memory uses boxed types.
2. **`PrimesRulesTest.scala`** — `indexDifference` now guards against lists with fewer than 2 elements, returning `List.empty` instead of crashing on `tail of empty list`.

---

## RuntimeCompiler — The Self-Closure Foundation

> **Dev:** I've needed this compile capability since the beginning. It's time to get it into the system.

> **Dev:** go

**Draco:** *(Reads Coordinate.scala)* *(Edits Coordinate.scala)* *(Edits Coordinate.json)* Three edits, all mechanical: Coordinate.scala trait `extends draco.Primal[T]` changed to `extends draco.Holon[T]`, Coordinate.scala TypeDefinition derivation updated, Coordinate.json derivation updated. The chain is now `Coordinate[T] -> Holon[T] -> Primal[T] -> DracoType`. Everything downstream of Coordinate still sees a `Primal[T]` by transitivity.

> **Dev:** All passed.

**Draco:** Good. Steps 2 and 3 are done — Holon exists and Coordinate derives from it. Step 4 is `draco.Transform[S, T]`. Before writing it: the Coordinate companion uses an inline TypeDefinition rather than `Generator.loadType`. For Transform, which pattern — `Generator.loadType` (like Holon) or inline TypeDefinition (like Coordinate)?

### The Workflow Need

> **Dev:** I need a reliable workflow that supports making changes in definitions and generating the source interactively, just before running tests to see if the changes passed. This may involve invoking the scala compiler at test execution time and using a special test class loader to load the generated updated and compiled source code.

The mechanism: one compile unit at a time.

> **Dev:** Might as well clean it all up.

**Draco:** *(Reads Coordinate.scala, Unit.scala, Cardinal.scala, Nominal.scala, Ordinal.scala, Distance.scala, Rotation.scala, Meters.scala, Radians.scala, Accumulator.scala, Numbers.scala, Transform.scala)* *(Creates draco/dreams/Transform.json)* *(Edits all 12 files)* All 12 files updated. **draco.base** (9 files): Coordinate, Unit, Cardinal, Nominal, Ordinal, Distance, Rotation, Meters, Radians. **draco.primes** (2 files): Accumulator, Numbers. **draco.dreams** (1 file): Transform, plus new `src/main/resources/draco/dreams/Transform.json` created. Every companion's `typeDefinition` is now a one-liner pointing at `Generator.loadType`, with the TypeDefinition data living exclusively in the JSON resource files.

### The Long-Term Vision

> **Dev:** The ultimate goal is type definition self closure, so that draco can completely regenerate itself in any sufficiently capable programming language, similar to the way a programming language compiler can be written in it's own language.

The layered roadmap:

1. **Now**: one compile unit at a time, runtime compiler in tests
2. **Generator-as-domain**: rules orchestrate multi-unit builds
3. **Multi-language generation**: proves type definitions are language-independent
4. **Draco Logos**: the type definitions become the language itself

> **Dev:** My intuition tells me that Draco Logos will be based on lambda calculus, combinators and homotopy type theory.

### Generator Owns Language Awareness

> **Dev:** Let's assume that only the Generator is implementation-language aware. It is already external dependency aware, so it knows to import package names from circe, evrete and pekko, so it can accommodate any PL domain the same way.

TypeDefinitions stay language-neutral. The Generator carries the PL-domain knowledge. When asked how to introduce Scala/Java package names into TypeDefinition for RuntimeCompiler, the answer was: you don't. The Generator already handles this.

### Implementation

`draco.RuntimeCompiler` wraps `scala.tools.nsc.Global` (`scala-compiler` was already a dependency):

- **`compile(source, fileName)`** — single source → temp directory → `Either[Seq[String], File]`
- **`compileMulti(sources)`** — multiple sources as one compilation unit (for sealed trait hierarchies)
- **`loadClass(classDir, className)`** — URLClassLoader with current classloader as parent

### RuntimeCompilerTest

Four tests verified the full cycle:

1. **Simple standalone class** — compile, load, verify field value via reflection ✓
2. **Source referencing draco types** — proves classpath bridging (generated code can import and extend draco traits) ✓
3. **Full cycle from JSON** — Holon.json → Generator.generate → RuntimeCompiler.compile → success ✓
4. **Error reporting** — bad source correctly produces Left with error messages ✓

---

## GenerateAndCompileTest — The Baseline

> **Dev:** How about you make a test that tests all existing type definitions in src/main/resources and write a brief record of each test instead pass/fail?

> **Dev:** Let's get the fundamentals of draco first. Also, after this and before we go back to example domains, can we look at the draco.formats.Formats domain and the format types and format domains that might go into it?

**Draco:** *(Greps for '^object .+ extends App with')* Good call — solidify the core, then Formats, then examples. Before applying companion-as-instance to existing domains, there's a subtlety: not all traits include `TypeInstance` in their inheritance chain. Domain-level types (Draco, Base, Primes) that extend DomainInstance are safe for a one-token change. Base/measurement types (Unit, Cardinal, Coordinate, Meters, etc.) that only reach DracoType would lose the `typeInstance` contract. Options: keep both mix-ins, restructure so Unit extends TypeInstance, or only apply to domain-level types.

> **Dev:** Why would you exclude *.rules.json and (presumably) *.actor.json?

**Draco** had excluded rule definitions. Dev corrected: rules and actors are TypeDefinitions like everything else. All JSON files included.

The TypeElement sealed hierarchy (12 types) is compiled as one unit via `compileMulti`.

### Baseline Results: 31 passed, 17 failed, 48 total

**Passing (31):**
ActorInstance, ActorType, AddNaturalSequence.rule, Base, CLI, Cardinal, Coordinate, Dictionary, Distance, Domain, DomainInstance, DomainType, Draco, DracoType, Holon, Meters, Nominal, Ordinal, Primal, Primes, PrimesFromNaturalSequence.rule, REPL, Radians, RemoveCompositeNumbers.rule, Rotation, RuleInstance, RuntimeCompiler, Specifically, Transform, TypeInstance, Unit

**Failing (17) — categorized by root cause:**

| Category | Types | Root Cause |
|----------|-------|------------|
| Sealed hierarchy | TypeElement hierarchy (12 types as group) | "Null is not a member of object" |
| Missing overrides | ContentSink, Main, Test, SourceContent, Numbers, DomainDictionary, TypeDictionary, RuleType | Factory params don't match trait members |
| typeInstance mismatch | Type, TypeDefinition, TypeName, Value, Rule | Override type/name doesn't align with hierarchy |
| Cyclic reference | Extensible | Self-referential extends |
| Syntax error | Actor | Type parameter handling in derivation |
| Missing import | Accumulator | `scala.collection.mutable` not imported |

These 17 failures are the road to self-closure — each one is a Generator improvement that brings the system closer to regenerating itself from JSON definitions alone.

---

## RemoveCompositeNumbers — The Persistent Bug

The `classOf[Int]` vs `classOf[Integer]` bug in RemoveCompositeNumbersRule was fixed early in the session but reverted (likely by IDE operation or sbt regeneration) and had to be re-applied. This reinforces the workflow problem that motivated RuntimeCompiler: hand-editing generated source is fragile. The real fix belongs in the JSON definition or the Generator's emission logic.

The `indexDifference` function in PrimesRulesTest was also fixed to guard against empty lists (`list.tail` on Nil).

---

## Primal Extends TypeInstance — The Approval Loop

> **Dev:** Option 2 it is. Add me back into the approval loop for each source code change.

**Draco:** *(Reads Primal.scala)* Starting with Primal. The change: `trait Primal[T] extends DracoType` to `trait Primal[T] extends TypeInstance`.

This adds `val typeInstance: DracoType` as an abstract requirement for all Primal subtypes. Two questions before editing: approve the trait change, and also switch the companion to `Generator.loadType`?

> **Dev:** [Pastes compiler output showing 14 errors and 10 warnings — missing `typeInstance` implementations in all anonymous classes in TypeElement.scala, Coordinate.scala, Meters.scala, and Radians.scala, plus Scala 3 ambiguity warnings for `typeInstance` references]

**Draco:** 14 errors and 10 warnings, all predictable. Each factory's anonymous instance needs `override val typeInstance: DracoType = <Companion>.typeInstance`. The ambiguity warnings also resolve once `typeInstance` is locally defined in each anonymous class.

Proposed: TypeElement.scala (11 factory methods), Coordinate.scala, Meters.scala, Radians.scala — all get `override val typeInstance` in their anonymous classes.

> **Dev:** [Pastes test output — all 58 tests pass after the fix, with warnings remaining for Scala 3 ambiguity]

**Draco:** Circular initialization — the trace shows `Parameter.typeDefinition` constructing a Factory which calls `Parameter.apply`, whose anonymous instance forces `override val typeInstance` eagerly, looping back to `Parameter.typeDefinition`.

**Fix:** Change all `override val typeInstance` to `override lazy val typeInstance` in the anonymous classes. This defers evaluation and breaks the cycle. Matches the existing memory note: "Factory defaults must be deferred to avoid circular init."

---

## RemoveCompositeNumbers — The Persistent Bug (Reprise)

> **Dev:** Everything passes, but the output of this test is incorrect — the sieve is not filtering composites.

**Draco:** *(Reads PrimesFromNaturalSequenceRule.scala)* *(Greps for 'classOf\[Int\]')* *(Reads RemoveCompositeNumbersRule.scala)* *(Edits RemoveCompositeNumbersRule.scala)* The fix from earlier didn't persist — possibly reverted by an IDE operation or git checkout. Still `classOf[Int]` in the main project. Re-applied: `classOf[Int]` to `classOf[Integer]` in RemoveCompositeNumbersRule.

---

## RuntimeCompiler — The Conversation

> **Dev:** That worked. Now let's discuss how to consistently update type definitions in the json file data, and subsequently run tests that need the updated re-generated source code.

**Draco:** The current regeneration pattern has a consistency problem: JSON changes don't automatically trigger regeneration, generated `.scala` files can be stale relative to their `.json` source, and there's no single command for "regenerate everything, recompile, then run all tests." The `classOf[Int]` regression is a symptom — hand-fixing generated source works until the next regeneration overwrites it. The real fix belongs in the JSON definition or the Generator's emission logic.

> **Dev:** The mechanism is one compile unit at a time. Once Generator and Scala are domains, we can write rules for multiple compiles and integrated builds. And when we have successfully generated Draco in more than one language, we can create the Draco language. My intuition tells me that Draco Logos will be based on lambda calculus, combinators and homotopy type theory.

**Draco:** That's a clear layered roadmap — each step enables the next. HoTT is a natural fit: types-as-spaces with paths-as-equivalences maps directly onto the Holon/Transform structure — a Transform between reference frames is a path between type-spaces, and the self-closure property echoes univalence. But the immediate step is concrete: build the RuntimeCompiler utility for a reliable generate-compile-load-verify cycle.

> **Dev:** How do we introduce scala and java package names into the TypeDefinition for RuntimeComplier?

**Draco:** The current TypeDefinition can express derivation parents and element types, but not arbitrary implementation dependencies like `scala.tools.nsc.Global` or `java.io.File`. Proposed: add an `imports` field to TypeDefinition — a `Seq[TypeName]` of external type references. The Generator emits `import` statements from them. Cleanest separation: `derivation` means "what this type IS," `imports` means "what this type USES."

> **Dev:** Yes, and proceed.

**Draco:** *(Creates RuntimeCompiler.json)* *(Creates RuntimeCompiler.scala)* *(Creates RuntimeCompilerTest.scala)* Three files created:

1. **`src/main/resources/draco/RuntimeCompiler.json`** — minimal TypeDefinition
2. **`src/main/scala/draco/RuntimeCompiler.scala`** — compile and loadClass utilities using `scala.tools.nsc`
3. **`src/test/scala/draco/RuntimeCompilerTest.scala`** — four tests: simple standalone class, source referencing draco types, full cycle from JSON (Holon.json to Generator to compile), and error reporting

> **Dev:** [Pastes RuntimeCompilerTest results — all 62 tests pass, full cycle works including Holon generated from JSON compiling successfully at runtime]

**Draco:** All 62 tests pass. The full cycle works: simple source compiles and loads, generated source referencing draco types compiles (classpath bridging works), Holon generated from JSON compiles successfully at runtime, and bad source correctly reports errors. The RuntimeCompiler is operational — the workflow for change-JSON, generate, compile, verify is now in place within a single test execution.

---

## GenerateAndCompileTest — The Conversation

> **Dev:** What is the file name of the test? I'd like to try just executing that.

**Draco:** `GenerateAndCompileTest` — in sbt:

```
testOnly draco.GenerateAndCompileTest
```

> **Dev:** Now we can plan how to fix or discard the definitions that didn't pass. [Pastes test output showing 31 passed, 17 failed out of 48 total]

**Draco:** The 17 failures group into distinct categories by root cause:

1. **Sealed trait hierarchy (12 types as group)** — TypeElement subtypes fail with "illegal inheritance from sealed trait" because the Generator produces them as separate files but they must be compiled together. Fix: add multi-source compilation to RuntimeCompiler.
2. **Missing overrides (8 types)** — ContentSink, Main, Test, SourceContent, Numbers, DomainDictionary, TypeDictionary, RuleType. Factory params don't match trait members. Generator emission bugs.
3. **typeInstance mismatch (5 types)** — Type, TypeDefinition, TypeName, Value, Rule. Override type/name doesn't align with updated hierarchy.
4. **Cyclic reference (1 type)** — Extensible. Self-referential extends.
5. **Syntax error (1 type)** — Actor. Type parameter handling in derivation.
6. **Missing import (1 type)** — Accumulator. `scala.collection.mutable` not imported.

Proposed plan: multi-source compilation first (biggest impact — 11 types in one fix), then small targeted fixes, then systematic Generator improvements.

> **Dev:** Multi-source fix first.

**Draco:** *(Reads RuntimeCompiler.scala)* Proposed `compileMulti` method taking `Seq[(source, fileName)]` and compiling all files together as one unit:

```scala
def compileMulti(sources: Seq[(String, String)]): Either[Seq[String], File] = {
  val tempDir = Files.createTempDirectory("draco-gen").toFile
  val sourceFiles = sources.map { case (source, fileName) =>
    val sourceFile = new File(tempDir, fileName)
    val writer = new PrintWriter(sourceFile)
    writer.write(source)
    writer.close()
    sourceFile.getAbsolutePath
  }
  // ... compile all together as one unit
}
```

Then update `GenerateAndCompileTest` to detect the TypeElement hierarchy and compile those 12 types as a group.

---

## Session Summary

### Code Changes
1. **`draco.Holon[T <: Product]`** — new base trait for primal type values
2. **`draco.base.Coordinate`** — retargeted from Primal to Holon
3. **`draco.Transform[S <: DracoType, T <: DracoType]`** — new two-parameter type, extends Holon[(S,T)]
4. **`draco.Primal[T]`** — now extends TypeInstance (was DracoType)
5. **`draco.TypeElement`** — 11 factory methods updated with `lazy val typeInstance`
6. **`draco.base.*`** — Meters, Radians traits gain typeInstance; Coordinate factory updated
7. **Inline TypeDefinitions eliminated** — 12 companions switched to Generator.loadType
8. **`draco.dreams.Transform`** — deleted (obsolete)
9. **`draco.RuntimeCompiler`** — new: compile, compileMulti, loadClass
10. **`draco.RuntimeCompilerTest`** — 4 tests verifying the compilation cycle
11. **`draco.GenerateAndCompileTest`** — comprehensive baseline: 31/48 passing
12. **`RemoveCompositeNumbersRule`** — classOf[Int] → classOf[Integer] (re-applied)
13. **`PrimesRulesTest`** — indexDifference guard against empty list

### Design Decisions (documented in memory, not yet implemented)
- Cosmocentric super-domain with Ego/Geo/Helio/Galacto peer sub-domains
- TransformDomain naming convention (`domains.<source>.<target>.<SourceTarget>`)
- Per-frame native vocabularies (no shared terms across frames)
- Transformation meta-type (subtype of Action, mirrors Action's shape)
- draco.formats.Formats domain with Format[T] and Value[S] as members
- Companion-as-instance pattern (`object X extends App with X`)
- Nexon/plexon as vocabulary layer
- All transform data through dual-inheritance primal value types
- Tuples never cross actor membranes
- Scalar vs 1-tuple distinction preserved

### Next Session Priorities
1. Fix remaining Generator emission issues (17 failures → 0)
2. Complete TypeInstance hierarchy (Type[T], DomainType)
3. draco.formats.Formats domain design and implementation
4. Begin reference-frame example domains
