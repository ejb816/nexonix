# Draco Dev Journal — Chapter 13

**Session date:** March 22, 2026
**Topic:** Extensible[T], Specifically[T], Domain Architecture, Actors as Membranes, Alpha→DataModel→Bravo Pipeline

---

## Session Start — Generator Domain

> **Dev:** Let's consider making a Domain, "draco.generator.Generator" (leaving draco.Generator intact and functioning as a draco type), with its domain definition src/main/resources/draco/generator/Generator.json, its constituent type definitions, rule definitions, and actor definitions — can you see where I'm going with this?

**Draco:** Yes — you're closing the loop. The Generator becomes a self-describing domain whose rules *are* the code generation rules. The framework generates itself.

The Generator domain would have types (Source, Target, Template, Language), rules (TypeRule, DomainRule, ActorRule, RuleRule, CodecRule, MultiRule), and actors (GeneratorActor — the execution surface Dreams talks to). `Generator[L]` parameterized by target language, with the current `draco.Generator` staying as bootstrap.

---

## Scala Domain

> **Dev:** Before we go deep into Generator domain, let's also consider a parallel Scala domain that can be populated by types that are scala language dependent in definition but scala language independent in reference.

**Draco:** A `draco.scala.Scala` domain would own types like Package, Trait, Class, Object, Val, Var, Def, Extend, With, Apply, Implicit, Sealed, Case — types with Scala-specific definitions but referenced language-independently by the Generator domain. The Generator rules say "emit a trait declaration" — they don't say "emit `trait Foo`". The Scala domain knows the syntax.

The layering: Generator domain references language types ← Scala domain provides definitions. Current Generator methods like `externalTypeImports`, `packageHierarchyImports`, `typeImports`, `ruleImports` belong in the Scala domain.

---

## Capability Domains

> **Dev:** There is the additional problem of evrete, pekko and circe, which are core external dependencies. I would like to come up with draco type domains for each of those three... I don't want to start doing that yet, but I don't want to do things that preclude it.

**Draco:** Four language-independent capability domains:

| Domain | Abstracts over | Currently bound to |
|--------|---------------|--------------------|
| `draco.rete` | Forward-chaining rule engine | Evrete |
| `draco.json` | Codec, serialization | Circe |
| `draco.actor` | Typed actors, behaviors | Pekko |
| `draco.scala` | Language syntax, declarations | Scala 2.13 |

Generator rules should reference these capability abstractions, not the libraries directly. The constraint for today: don't bake library-specific assumptions into the Generator domain's types or rules.

> **Dev:** Yes, but please, let's not get ahead of ourselves too far, too quickly.

---

## Updating Existing Domains First

> **Dev:** Before we continue with Generator let's update and upgrade the existing domains that were manually created: draco.Draco, draco.base.Base and draco.primes.Primes

**Draco:** *(Reads current state of Draco.json, Base.json, Primes.json, Generator.scala, Primes.scala, Draco.scala, and DomainDefinition.scala to understand what needs updating)*

Identified three issues: file convention (what goes where), stale Draco elementTypeNames, and incomplete Primes elementTypeNames.

---

## File Naming — The "domain" Aspect Was Never an Aspect

> **Dev:** I'm a little fuzzy on the json definition file names in resources, given we eliminated the subpackage names "rule" and "actor".

**Draco:** *(Reviews TypeName.scala — resourcePath, aspectExtension, canonicalOrder)*

The aspect naming convention maps file extensions to definition types: `X.json` → TypeDefinition, `X.domain.json` → DomainDefinition, `X.rule.json` → RuleDefinition, etc.

> **Dev:** What actually distinguishes a DomainType from a DracoType is the last package name element followed by the type name. Example *.foo.Bar is a DracoType, *.foo.Foo is a DomainType that includes its own DracoType in its definition. All domains are types but not all types are domains. I think the three aspect names that we identified over-complicated the issue, when just considering "rule" and "actor" would have been enough.

**Draco:** That's a much cleaner insight. Domain-ness is structural, not declared. Only "rule" and "actor" are genuine aspects. Derivation in the type already carries that information — if RuleType is in the derivation, it's a rule type. Aspects become redundant.

---

## Extensible[T] — The Structural Root

> **Dev:** I think there is a subtle but fundamental inconsistency in the draco type system that has been eluding me because of how we were able to get things to work, in spite of it.

> **Dev:** I think that "extends" in a generated trait should be reserved for non-trait derivations, and DracoType should always come after the first "with" after extends, unless there is something that already inherits that way from DracoType that can go there.

> **Dev:** Is there something like "extends Nothing with DracoType" that might work?

**Draco:** Not `Nothing`. But `AnyRef` does exactly that — `trait Numbers extends AnyRef with Primes`. It's what Scala does implicitly.

> **Dev:** Maybe DracoExtensible[T]?

> **Dev:** To attempt to keep it as simple as possible, how about generating it as a parameterized type named Extensible in the Draco domain?

**Draco:** `Extensible[T]` — every Draco type extends it in the `extends` position, carrying its own identity as the type parameter:

```scala
trait Extensible[T]
trait DracoType extends Extensible[DracoType]
trait Numbers extends Extensible[Numbers] with Primes
trait Actor[T] extends Extensible[Actor[T]] with ActorType
```

No special treatment. Just a Draco type that happens to be the structural root.

---

## Aspects Fold Into Derivation

> **Dev:** Before we continue, does this have a positive effect on the "domain rule actor" aspect issue?

**Draco:** Yes. The derivation *is* the aspect. You don't need `aspects: Seq[String]` in TypeName when the trait mixins already carry that information. The Generator can look at derivation to determine what code to generate. Everything could just be `.json` — one schema. The `aspects` field and all its derived fields simplify away.

---

## TypeDefinition as Sole Definition Type

> **Dev:** If the "with-elements" form a "product type composition", would TypeDefinition need to be a "sum type composition"?

**Draco:** The `with` mixins form a **product type composition** (AND). TypeElement is the **sum** (OR) — a sealed hierarchy where each element is one of several kinds. The definition that produces a product trait is composed of sum-typed elements.

Rule fields (Variable, Condition, Action, Pattern) are ALREADY TypeElement variants. Domain fields (elementTypeNames, superDomain, source, target) can be expressed as TypeElements. This would retire DomainDefinition, RuleDefinition, and ActorDefinition as separate types.

> **Dev:** I hope so because that seems really nice.

---

## Three Distinct Hierarchies

> **Dev:** There are implicit and strict package name rules about domains and types that make step one complicated.

The discussion revealed three hierarchies that must not be conflated:

1. **Type inheritance** (compile-time) — `derivation` in TypeDefinition
2. **Domain inheritance** (runtime) — `superDomain` — domain-level IS-A, not containment
3. **Domain containment** (structural) — package names, fully implicit

> **Dev:** The superDomain is not being interpreted correctly by the Generator. My original intent for superDomain was to specify the domain that was the supertype of the domain that was not necessarily the container of it. Domain containment is completely specified by package names with the simple type name.

---

## Actors and Typed Messages

> **Dev:** It's all about actors and typed messages.

**Draco:** The domain chain via `superDomain` tells you what's reachable. The `source`/`target` tells you what flows where. Rules vs actors is determined by whether you're staying within a domain or crossing a boundary.

> **Dev:** To be clear, you only need to send to another actor if doing a transform between different domains. That's why domains have source and target.

**Draco:** Within a single domain → rules handle everything (RETE engine). Actors enter at domain boundaries: source ≠ target → transform → actor sends result.

> **Dev:** By Jove! I think she's got it!

---

## superDomain on All TypeDefinitions

> **Dev:** I have realized I actually need it to be in all instances of type definitions, not just domain type definitions. I need an optionally available inheritance hierarchy both within domains and their contained types and between superdomains and their subdomains.

> **Dev:** Source and target — Hmm. The only two kinds of domains that can distinguish between actor computation and rule computation are update domains and transform domains. Does that imply that source and target need to be in just domains rather than any type?

**Draco:** Yes. `superDomain` → universal (every type needs domain navigation for message routing). `source`/`target` → domain types only (only domains decide computation mode).

---

## Consolidation and Pause

> **Dev:** Please consolidate what we've learned in this session, so I can pause here, exit, and continue later.

**Draco:** *(Writes comprehensive project_type_system_redesign.md, updates project_generator_evolution.md, updates MEMORY.md with current state and imminent tasks)*

---

## Session Resume — Implementation Begins

> **Dev:** Ok, ready to proceed where we left off.

**Draco:** Phase 1 — Extensible[T] and DracoType. Phase 2 — TypeName simplification. Phase 3 — superDomain on TypeDefinition. Phase 4 — TypeElement hierarchy. Phase 5 — Generator updates. Phase 6 — Update existing domains.

> **Dev:** Make it so.

---

## Phase 1: Extensible[T] Created

**Draco:** *(Creates Extensible.json — minimal definition with typeParameters: ["T"], no derivation, no elements. Creates Extensible.scala — `trait Extensible[T]` + companion loading from classpath. Updates DracoType.json — adds Extensible[DracoType] to derivation. Updates DracoType.scala — `trait DracoType extends Extensible[DracoType]`)*

> **Dev:** *(Runs tests — Actor.scala fails: `not found: type ExtensibleBehavior`)*

**Draco:** The Pekko import was missing `ExtensibleBehavior`. Added it to `externalTypeImports` map and made `externalImports` scan derivation type names (pre-existing bug exposed by the generated test).

---

## New Generate Test Pattern

> **Dev:** *(TypeDefinition.scala type mismatch in generated/draco — shadowing issue with rewritePackage)*

Discussion of the fundamental shadowing problem with `generated.draco.TypeDefinition` vs `draco.TypeDefinition`.

> **Dev:** I agree with not compiling it as a file. Instead, generate src/generated/scala — the ".generated" will keep it from compiling, otherwise the content should be exactly the same as the source in src/main/scala, no rewritePackage needed. If you can get the text string result of an in-memory diff, append it in a multiline comment at the end of the generated source.

> **Dev:** My not-a-nitpick: could you rewire what you just did by adding Generated.scala alongside of Main.scala and Test.scala, and have Generator use that instead of creating a new special method?

**Draco:** *(Creates Generated.scala following Main/Test pattern — `src/generated/` source/sink roots. Adds `lazy val generated: Generated = Generated.roots` to Generator. Updates test to use `Generator.generated.sinkRoot`. Removes old `src/test/scala/generated/` directory.)*

> **Dev:** *(59/59 tests passed)*

---

## Phase 2: TypeName Simplification

**Draco:** *(Removes `aspects`, `qualifiedName`, `aspectExtension`, `canonicalOrder`, `validAspects`, `aspectSuffix`, `fullQualifiedName` from TypeName.scala. Simplifies `namePath` and `resourcePath`. Updates TypeName.json, Generator.scala, TestTypeModule.scala, Natural.actor.json. Deletes orphaned TypeName.generated.scala.)*

> **Dev:** Done. All passed.

---

## Phase 3: superDomain on TypeDefinition

**Draco:** *(Adds `superDomain: TypeName` to TypeDefinition trait, factory, encoder (with elision), decoder. Updates TypeDefinition.json. All callers use named parameters — non-breaking.)*

---

## Phase 4: TypeElement Discussion — Morphic and Friends

> **Dev:** I'm thinking that we need an abstraction for the actor variant that represents the match case semantics in a language independent way.

> **Dev:** When I think of how to do something in a language independent way, when I'm generating Scala, I consider how the abstraction would work with Haskell.

In Haskell, the equivalent is just a function doing case analysis on a sum type — pattern matching on ADT constructors. The abstraction isn't "Dispatch" or "Match" — it's case analysis.

> **Dev:** Wouldn't actors be monads in Haskell?

> **Dev:** Let's try calling this Morphic. Then we could describe Monadic as EndoMorphic.

Discussion established:
- **Morphic** — a morphism: source → target (general transformation)
- Pattern and Action are the source and target *parts* of a Morphic
- **Condition** — a Morphic where target is Boolean
- **Factory** — a Morphic mapping constituent values to integrated instance
- **Monadic stays** — EndoMorphic doesn't add anything for people who know monads, meaningless for those who don't

> **Dev:** I suggest starting with option 3 will save us more time and effort in the long run.

Phase 4 deferred — Morphic, Case, BodyElement flattening will happen during Generator domain work.

---

## Codec Asymmetry Discussion

> **Dev:** I'm thinking of adding custom encoders and decoders to type definitions in case encoder/decoder pair can't be symmetric.

Discussion of external team's legacy binary ↔ JSON without symmetric codecs.

> **Dev:** On the other hand, I will probably try to use their JSON schema from their binary → JSON to make my JSON → typed data encoder, and then make the symmetric decoder, and make them do a JSON → JSON conversion before their JSON → binary.

**Draco:** That's the architecture working as designed — symmetric within Draco, transform domain at the boundary. Custom encoder/decoder option retained for framework users who can't do boundary transforms.

---

## Orion — Five ION Patterns

> **Dev:** I have determined there are five distinct interactive patterns that will need to be supported at the orion level.

- **PSION** — Pattern Semantic Inter-Operability Nexus (baseline catch-all)
- **ANION** — Actor Network Inter-Operability Nexus
- **CATION** — Communications Actor Transformation Inter-Operability Nexus
- **IONIC** — Inter-Operability Nexus Integrating Communications
- **UNION** — Unit Network Inter-Operability Nexus

All five involve message domains. In context of message domains, non-message domains are called data domains or data models.

---

## Data Domains vs Message Domains

> **Dev:** All five involve a specialization of domain which is message domain. In the context of message domains, domains that aren't message domains are called data domains or data models.

- **Data domains** — define what things ARE (shared semantic model)
- **Message domains** — define how things FLOW (external message system models)
- Transforms go through data domains: Alpha → DataModel → Bravo

> **Dev:** Does adding something that generates "type MessageDomain = Domain[T]" into the Domain source file, work?

> **Dev:** Actually, I think it belongs in draco.dreams.orion since I don't need it until I need all the other ION patterns.

> **Dev:** Maybe we should create a domain level specialization in draco, without defining MessageDomain specifically?

---

## Specifically[T] — "That's brilliant."

> **Dev:** Oh, now you done it! What about "trait Specifically[T] extends Extensible[Specifically[T]] with <Any trait I want to put here>"?

**Draco:** That's brilliant. `Specifically[T]` is to specialization what `Extensible[T]` is to extension. Two minimal parameterized traits that partition the entire type system into "what things are" and "what things specialize."

> **Dev:** None of the AI models I work with (that I can recall) has ever responded "That's brilliant." to any of my prompts. And I know you're not biased toward sycophantic responses because you didn't use an exclamation point. I feel like I won an award.

**Draco:** You earned it.

*(Creates Specifically.json and Specifically.scala)*

> **Dev:** So now, thanks to Specifically, I can defer structural definitions until I need them.

---

## Actors as Thin Membranes

> **Dev:** So, the only thing Draco actors have to do is insert incoming typed data into working memory, and let rules take care of everything else that needs to happen in that actor, including sending typed data to other actors. Which means the ActorRefs have to be in working memory as well, so they can be referenced by the rules.

> **Dev:** The important part of the actor structure is not the message type but the domain actor references it has to give to its rules for sending messages.

> **Dev:** It looks like there is no need for the actor to explicitly hold the ActorRefs, that all actor references can be held as global elements in the rule type.

---

## Evrete Environment for ActorRefs

**Draco:** *(Researches Evrete documentation — discovers the Environment mechanism: `knowledge.set("key", value)` / `ctx.getRuntime.get("key")`)*

The Environment interface is purpose-built for external references in rule actions. Both Knowledge and RuleSession support arbitrary key-value storage.

> **Dev:** I completely missed considering the use knowledge.set in step 3. It seems to bypass the fact mechanism inherent for data in working memory.

> **Dev:** Given I was willing to use global elements generated into the companion object, I think your Environment suggestion is more consistent, at least with regard to ifriti.

Domain data through `session.insert()`. External references through `knowledge.set()`. Two doors, each purpose-built.

> **Dev:** The next thing after using evrete (ifriti) as an external dependency, is to create a draco definition based RETE domain that supports the minimal set of evrete functions needed for the draco framework, that can be generated in any language.

---

## Alpha → DataModel → Bravo Pipeline

> **Dev:** Using the word fragments is a bit misleading. More like, the data from three messages in Alpha is needed to create a single message in Bravo.

> **Dev:** Assembly rules need to be in DataModel. The only thing Alpha can do is convert the JSON input message into the corresponding Alpha message type plus Alpha message data type and send it to DataModel which is the superDomain of Alpha.

Architecture validated:
- Alpha actor: format conversion (StatelessSession)
- DataModel actor: correlation/assembly (StatefulSession)
- Bravo actor: receives assembled result (StatelessSession)

*(Creates DataModel types: PartOne, PartTwo, Assembled. Alpha types: AlphaOne, AlphaTwo. Bravo type: BravoResult. JSON definitions for all. Assembly rule: AssembleResult. Actors: DataModelActor, BravoActor. Test: AlphaBravoTest.)*

> **Dev:** *(Runs test)*
> ```
> BravoActor received: number=42, text=hello
> Assembly verified: number=42, text=hello
> Process finished with exit code 0
> ```

---

## Extensible Linearization Problem

> **Dev:** Your saying "extends" in the table, but does that mean extends Extensible[<self>]?

> **Dev:** We may not have discussed this, but explicitly putting Extensible in the derivation should only happen if the type parameter is not the type being defined.

This surfaced a fundamental problem: once `DracoType extends Extensible[DracoType]`, all descendants inherit `Extensible[DracoType]` — Scala's linearization prevents subtypes from re-specifying the type parameter.

> **Dev:** You have confirmed my reservations about using Extensible[T].

**Draco:** Option 1: Drop the type parameter. `trait Extensible` (no `[T]`). It still occupies the `extends` position.

> **Dev:** What do you think of this variant of option 1? If the name of the first derivation element is "Extensible" then Generator replaces it with the first type parameter. If otherwise, then the Generator uses Extensible as a placeholder type for extends and adds the derivation as "with $name".

**Draco:** Elegant. Extensible serves two roles: as a Scala trait (non-parameterized marker) and as a Generator directive (when in derivation[0] with a typeParameter, substitute into extends position).

> **Dev:** Can we save the Specifically[T]?

**Draco:** Yes — `Specifically[T]` extends non-parameterized `Extensible`, keeps its own type parameter. Linearization safe because specializations aren't further subtyped with different `Specifically` parameters.

*(Implements: Extensible simplified to non-parameterized. Specifically updated. DracoType updated. Generator typeExtends updated. All tests pass.)*

---

## Pipeline Test Passes

*(Creates assembly rule, actors, test. Fixes `session.insert` overload ambiguity with `Seq(msg): _*` pattern.)*

> **Dev:** *(Test output)*
> ```
> BravoActor received: number=42, text=hello
> Assembly verified: number=42, text=hello
> Process finished with exit code 0
> ```

---

## Memory Consolidation

> **Dev:** In your view, what is next?

**Draco:** Memory consolidation first, then TypeDefinition unification.

*(Updates all six memory files: MEMORY.md, project_type_system_redesign.md, project_orion.md, project_codec_asymmetry.md, project_generator_evolution.md, user_preferences.md)*

> **Dev:** Please stage the files you created that aren't staged yet.

*(Stages 18 new files — source, JSON definitions, test, and actors)*

---

## Session End

Major accomplishments this session:
1. **Extensible** — from parameterized `[T]` concept through linearization discovery to non-parameterized implementation
2. **Specifically[T]** — specialization mechanism with deferred structure
3. **TypeName simplified** — aspects removed entirely
4. **superDomain** — added to TypeDefinition
5. **Generator typeExtends** — Extensible convention implemented
6. **Generate test pattern** — `.scala.generated` with diff, Generated.scala root
7. **Domain architecture** — actors as membranes, rules handle all logic, Evrete Environment for ActorRefs
8. **Orion** — five ION patterns, data vs message domains
9. **Alpha → DataModel → Bravo** — end-to-end pipeline validated
10. **Capability domains** — draco.rete/json/actor/scala identified, do not preclude
