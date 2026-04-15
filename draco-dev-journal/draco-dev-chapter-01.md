# Draco Dev Journal — Chapter 1

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

> **Dev:** That's what we need to find out next. I have determined there are five distinct interactive patterns that will need to be supported at the orion level of defining system of systems features/functions. ORION - Open Resilient Inter-Operability Nexus
>
> - PSION - Pattern Semantic Inter-Operability Nexus
> - ANION - Actor Network Inter-Operability Nexus
> - CATION - Communications Actor Transformation Inter-Operability Nexus
> - IONIC - Inter-Operability Nexus Integrating Communications
> - UNION - Unit Network Inter-Operability Nexus

**Draco:** *(Creates project_orion.md)* *(Edits MEMORY.md)* The chemistry is intentional — ions, charge, flow:

- **PSION** — Pattern/semantic matching across domains (the rule/RETE layer of interoperability)
- **ANION** — Actor network topology (how actors discover and connect — the receiving/consuming side)
- **CATION** — Transformation actors at domain boundaries (the sending/producing side — source->target)
- **IONIC** — Integration of communication channels (the medium — serialization, protocols, the draco.json layer)
- **UNION** — Aggregation/composition of units across the network (the combining operation)

Each pattern generates a different actor topology and interaction shape from the same domain definitions.

> **Dev:** PSION is the catch-all. It just indicates that there are domains and that actors operate based on the rules in the scope of the actors domains.

**Draco:** So PSION is the baseline — domains exist, actors follow their domain rules. No special interaction pattern, just the fundamental architecture working. The other four are specific interaction patterns layered on top.

---

## Data Domains vs Message Domains

> **Dev:** All five involve a specialization of domain which is message domain. In the context of message domains, domains that aren't message domains are called data domains or data models.

- **Data domains** — define what things ARE (shared semantic model)
- **Message domains** — define how things FLOW (external message system models)
- Transforms go through data domains: Alpha → DataModel → Bravo

> **Dev:** Not actor messages, but external message systems that are being processed in message and data domains for interoperability between independently developed message systems.

**Draco:** This is system-of-systems interoperability. Independently developed external message systems — each with their own formats, protocols, schemas — that need to communicate through Draco's domain architecture. The flow is: External Message System A -> Message Domain A -> Data Domain -> Message Domain B -> External Message System B. The five patterns describe different topologies for this integration.

> **Dev:** Does adding something that generates "type MessageDomain = Domain[T]" into the Domain source file, work?

> **Dev:** Actually, I think it belongs in draco.dreams.orion since I don't need it until I need all the other ION patterns.

> **Dev:** Maybe we should create a domain level specialization in draco, without defining MessageDomain specifically?

> **Dev:** Where did you get LinkSixteen??!! I am trying to develop in open source environment. That reference is perilously close to CUI (Controlled Unclassified Information) at my workplace.

**Draco:** My apologies. That was a bad example — I inferred from "independently developed message systems" and "interoperability" and reached for a domain I shouldn't have. I'll stick to abstract examples like `MessageDomainA` / `MessageDomainB` or use the naming you provide.

> **Dev:** Maybe what I need instead is "trait Message" followed by "trait MessageDomain[T] extends Extensible[MessageDomain] with Domain[T]" in its own file with its own definition.

**Draco:** That's the right approach — proper types in the Draco type system, not aliases. `Message` and `MessageDomain` each get their own JSON definition and generated source:

```
Message.json → trait Message extends Extensible[Message]
MessageDomain.json → trait MessageDomain[T] extends Extensible[MessageDomain[T]] with DomainType
```

These would live in `draco` package alongside Domain, DomainType, etc. — they're core framework types, not a separate domain.

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

## Knowledge and Session Lifecycle

> **Dev:** There is something that's important for actors that is not yet in the type definition structure. That is how the knowledge instances are created and applied to load the rules in a particular actor.

**Draco:** The actor is a thin membrane, but it still needs to be set up — a Knowledge instance configured with the right rules for its domain. The rules are discoverable — they're types in the domain's package whose derivation includes `RuleType`. The domain containment (package) tells you what rules belong. Domain inheritance (superDomain) tells you what additional rules are in scope from parent domains. But the creation and application — `newKnowledge`, registering rule patterns, creating a session — that's not expressed in any TypeDefinition currently.

> **Dev:** That's what we have to discuss, with you helping me maintain consistency. This is an unresolved design issue, and this is the time to address it. So evrete has KnowledgeService, Knowledge, StatelessSession, and StatefulSession. All of that has to be accessed in actor configuration, initialization and operation.

**Draco:** *(Launches agent: Research Evrete API usage)* The Evrete lifecycle mapped to actor participation:

```
KnowledgeService (singleton)
  → Knowledge (per domain, rules loaded)
    → StatefulSession (per actor instance, working memory)
      → insert(message)  ← actor does this
      → fire()           ← actor does this
      → close()          ← actor does this on shutdown
```

Knowledge creation (per domain), then load domain rules (from domain scope), create session, and operate. The session is stateful because the actor accumulates facts across messages — working memory persists between `fire()` calls.

> **Dev:** StatelessSession is preferred if all of the domain input data is available in a single actor message. It's when data arrives in fragments over time that StatefulSessions are required.

**Draco:** So the session type is determined by the data arrival pattern:

- **StatelessSession** — complete data in one message. Insert all facts, fire, get results, done. No state between messages.
- **StatefulSession** — data arrives incrementally. Working memory accumulates across messages. Each insert + fire adds to existing state. Needs lifecycle management.

This connects back to the five Orion patterns. Some patterns naturally produce complete messages (StatelessSession), others produce incremental data (StatefulSession).

> **Dev:** When we have MessageDomain, then temporally distributed fragment arrivals are likely.

**Draco:** So the session type follows from the domain kind:

- **Data domain** -> StatelessSession (complete data, rules fire once)
- **Message domain** -> StatefulSession (fragments arrive over time, working memory accumulates)

No need for an explicit flag — the Generator can derive it from whether the domain is a data domain or a message domain. Since MessageDomain doesn't exist yet (deferred to Orion), everything is currently StatelessSession by default. StatefulSession arrives when MessageDomain arrives.

> **Dev:** What would it mean for an actor to support multiple sessions, and what is the relationship between sessions and a specific instance of working memory?

**Draco:** In Evrete, a Session IS a working memory instance. One session = one independent set of facts with the same rules applied.

| Scenario | Sessions | Type |
|----------|----------|------|
| Data domain, complete input | One per message, discarded after fire | Stateless |
| Message domain, single stream | One persistent session | Stateful |
| Message domain, multiple streams | One per logical conversation | Stateful |

---

## Evrete Environment for ActorRefs

**Draco:** *(Researches Evrete documentation — discovers the Environment mechanism: `knowledge.set("key", value)` / `ctx.getRuntime.get("key")`)*

The Environment interface is purpose-built for external references in rule actions. Both Knowledge and RuleSession support arbitrary key-value storage.

> **Dev:** I completely missed considering the use knowledge.set in step 3. It seems to bypass the fact mechanism inherent for data in working memory.

> **Dev:** Given I was willing to use global elements generated into the companion object, I think your Environment suggestion is more consistent, at least with regard to ifriti.

Domain data through `session.insert()`. External references through `knowledge.set()`. Two doors, each purpose-built.

> **Dev:** The next thing after using evrete (ifriti) as an external dependency, is to create a draco definition based RETE domain that supports the minimal set of evrete functions needed for the draco framework, that can be generated in any language.

---

## DataModel Research

> **Dev:** We need to go into research mode. I have a primitive incomplete example of a data model with four message models, (ad hoc, because MessageDomain doesn't exist yet). You can look at what is there now, DataModel, and Alpha, Bravo, Charlie, Delta as proto message models.

**Draco:** *(Launches agent: Research DataModel and message models)* Here's what's there: **DataModel** — base data domain in `domains.dataModel`, extends `DomainInstance`. Empty. **Alpha, Bravo, Charlie, Delta** — all extend `DataModel`, each in their own package. Also empty stubs. No rules, no sessions, no transforms. The interesting contrast is **Natural** — it has a populated JSON, a `Primal[Int]` value, a factory, and `NaturalActor`.

> **Dev:** Natural is intended to be a simple input feed stream of natural numbers to the Primes domain. The only rules needed are the ones that specify the nature of the sequence.

**Draco:** So Natural is a source domain for Primes — it feeds numbers in, Primes rules process them. The Natural -> Primes relationship is what `source`/`target` would express once fully wired.

> **Dev:** You nailed it in your previous response.

**Draco:** OK so: DataModel is the common semantic model (the shared data domain). Alpha, Bravo, Charlie, Delta are four independent message systems, each with their own representation of the same data. Transforms go through DataModel: Alpha -> DataModel -> Bravo.

---

## Alpha → DataModel → Bravo Pipeline

> **Dev:** Using the word fragments is a bit misleading. More like, the data from three messages in Alpha is needed to create a single message in Bravo.

> **Dev:** Assembly rules need to be in DataModel. The only thing Alpha can do is convert the JSON input message into the corresponding Alpha message type plus Alpha message data type and send it to DataModel which is the superDomain of Alpha.

> **Dev:** Can we extend the current example to show this working the way that was just described without formally adding the MessageDomain data type?

**Draco:** Yes. Alpha types (`AlphaOne` with number, `AlphaTwo` with text), Bravo types (`BravoResult` with both), DataModel actor owns the StatefulSession. Assembly rule fires when both `PartOne` and `PartTwo` are in working memory, producing `BravoResult` and sending it to the Bravo actor.

> **Dev:** Using evrete documentation ("evrete" can be pronounced "ifrit" like the fire djinn - "ifriti" for plural), and based on the current version of draco rule generator, suggest a way of configuring and initializing a domain actor with domain actor references needed by the domain's rules.

**Draco:** *(Launches agent: Research Evrete docs and patterns)* The key finding is Evrete's **Environment** mechanism:

```scala
// Set on Knowledge (inherited by all sessions)
knowledge.set("bravoActorRef", bravoActorRef)

// Access from within a rule action
ctx.getRuntime.get[ActorRef[Bravo]]("bravoActorRef")
```

The lifecycle: Knowledge creation per domain, load domain rules, set ActorRefs as Environment properties, create session. The ActorRef property names follow a convention — `targetDomainName + "ActorRef"` — so the Generator can derive them from the domain's target relationships. No closure capture, no facts-as-refs needed.

Architecture validated:
- Alpha actor: format conversion (StatelessSession)
- DataModel actor: correlation/assembly (StatefulSession)
- Bravo actor: receives assembled result (StatelessSession)

> **Dev:** Just add Charlie and Delta symmetrically to Alpha and Bravo so they can be intermixed pairwise as DataModel subdomains.

**Draco:** *(Creates symmetric types for all four domains)* Each domain gets symmetric message types. DataModel defines the common abstractions (`PartOne`, `PartTwo`, `Assembled`). Alpha/Bravo/Charlie/Delta each have `XOne extends X with PartOne`, `XTwo extends X with PartTwo`, `XResult extends X with Assembled`.

> **Dev:** I think I made a big mistake going symmetric on all four message domains this early. Most real cases will be necessarily asymmetric, and also the domains should change as incrementally as possible.

**Draco:** You're right. Let me revert to the minimal asymmetric case.

> **Dev:** The smallest change for me would be ignore my symmetry request and just maintain Charlie and Delta as "empty" domains and do your original example with Alpha, DataModel and Bravo: Alpha -> DataModel, DataModel -> Bravo.

**Draco:** *(Creates Alpha.scala, Bravo.scala, Charlie.scala, Delta.scala)* Charlie and Delta reverted to empty stubs. The asymmetric example is now:

- **Alpha** — source: `AlphaOne` (number) and `AlphaTwo` (text), sent to DataModel
- **DataModel** — common abstractions: `PartOne`, `PartTwo`, `Assembled`. Assembly rules here.
- **Bravo** — target: `BravoResult` (number + text), receives assembled data

> **Dev:** I want JSON definitions for all the code you're writing.

**Draco:** *(Creates DataModel.json, PartOne.json, PartTwo.json, Assembled.json, Alpha.json, AlphaOne.json, AlphaTwo.json, Bravo.json, BravoResult.json, Charlie.json, Delta.json)*

| File | What it defines |
|------|----------------|
| `domains/dataModel/DataModel.json` | Domain, extends DomainInstance |
| `domains/dataModel/PartOne.json` | Abstract type, has number: Int |
| `domains/dataModel/PartTwo.json` | Abstract type, has text: String |
| `domains/dataModel/Assembled.json` | Abstract type, has number + text |
| `domains/alpha/Alpha.json` | Domain, extends DataModel |
| `domains/alpha/AlphaOne.json` | extends Alpha with PartOne |
| `domains/alpha/AlphaTwo.json` | extends Alpha with PartTwo |
| `domains/bravo/Bravo.json` | Domain, extends DataModel |
| `domains/bravo/BravoResult.json` | extends Bravo with Assembled |
| `domains/charlie/Charlie.json` | Domain, extends DataModel (empty) |
| `domains/delta/Delta.json` | Domain, extends DataModel (empty) |

*(Creates DataModel types: PartOne, PartTwo, Assembled. Alpha types: AlphaOne, AlphaTwo. Bravo type: BravoResult. Assembly rule: AssembleResult. Actors: DataModelActor, BravoActor. Test: AlphaBravoTest.)*

> **Dev:** *(Runs test)*
> ```
> BravoActor received: number=42, text=hello
> Assembly verified: number=42, text=hello
> Process finished with exit code 0
> ```

---

## DracoType Definition Cleanup

> **Dev:** If DracoType definition doesn't need it explicitly to generate the source correctly, then remove it from the definition.

**Draco:** *(Edits DracoType.json)* Done. The Generator will need to add `extends Extensible[Self]` automatically when it generates trait declarations — that's part of the Phase 5 `typeExtends` update. For now, the JSON definitions are clean: derivation only lists what comes after `with`.

> **Dev:** Is there a setting for change approval that is scoped to a particular prompt response rather than the rest of the session?

**Draco:** No — permissions are session-wide via `settings.json`. You can press `Shift+Tab` to cycle through approval modes: default (prompts for each), acceptEdits (auto-accepts file edits), bypassPermissions (skips all).

> **Dev:** OK, got it.

**Draco:** *(Designs Alpha -> DataModel -> Bravo implementation plan)*

> **Dev:** I have to stop this plan because "AlphaOne extends Alpha with PartOne" indicates that I am not sure if Extensible[Alpha] or Extensible[AlphaOne] needs to be in current source code that is currently generateable from type definition.

---

## Extensible Linearization Problem

**Draco:** That's a real issue. If `DracoType extends Extensible[DracoType]`, and `Alpha extends DataModel extends ... extends DracoType`, then Alpha already inherits `Extensible[DracoType]`. If AlphaOne tries to `extends Extensible[AlphaOne] with Alpha`, Scala will reject it — you can't extend `Extensible` with two different type parameters in the same linearization chain.

> **Dev:** Your saying "extends" in the table, but does that mean extends Extensible[<self>]?

> **Dev:** We may not have discussed this, but explicitly putting Extensible in the derivation should only happen if the type parameter is not the type being defined.

> **Dev:** You have confirmed my reservations about using Extensible[T].

**Draco:** Option 1: Drop the type parameter. `trait Extensible` (no `[T]`). It still occupies the `extends` position.

> **Dev:** What do you think of this variant of option 1? If the name of the first derivation element is "Extensible" then Generator replaces it with the first type parameter. If otherwise, then the Generator uses Extensible as a placeholder type for extends and adds the derivation as "with $name".

**Draco:** Elegant. Extensible serves two roles: as a Scala trait (non-parameterized marker) and as a Generator directive (when in derivation[0] with a typeParameter, substitute into extends position).

> **Dev:** Yes, I want to ensure that we can still use Specifically[T] as we originally discussed.

**Draco:** Yes, it works. `Specifically[T]` extends non-parameterized `Extensible`, carries its own type parameter, and the linearization problem doesn't apply because types that extend `Specifically[Self]` are endpoints — they define the specialization. Subtypes extend the specialization directly, not re-specifying `Specifically`. The chain is: `Extensible` <- `Specifically[T]` <- concrete specialization — only one parameterized level.

```scala
// Updated:  trait Specifically[T] extends Extensible
```

> **Dev:** yes

**Draco:** *(Edits Extensible.scala, Extensible.json, Specifically.scala, Specifically.json, DracoType.scala, Generator.scala)* Starting with Part 1 — Extensible simplification. All six changes made: Extensible non-parameterized, Specifically extends bare Extensible, DracoType extends bare Extensible, Generator `typeExtends` implements the new convention.

---

## Pipeline Test Passes

*(Creates assembly rule, actors, test.)*

> **Dev:**
> ```
> DataModelActor.scala:28:17
> ambiguous reference to overloaded definition,
> both method insert in trait RuleSession of type (x$1: Object*): org.evrete.api.StatefulSession
> and  method insert in trait SessionOps of type (x$1: Object): org.evrete.api.FactHandle
> match argument types (domains.dataModel.DataModel)
>         session.insert(msg)
> ```

**Draco:** *(Edits DataModelActor.scala)* The `insert` method is overloaded — `insert(Object)` returns a `FactHandle`, `insert(Object*)` returns `StatefulSession`. Scala can't pick between them for a single argument. Fix by explicitly calling the varargs version: `session.insert(Seq(msg): _*)`. This matches the pattern used in `PrimesRulesTest`.

> **Dev:** *(Runs test)*
> ```
> BravoActor received: number=42, text=hello
> Assembly verified: number=42, text=hello
> Process finished with exit code 0
> ```

**Draco:** The pipeline works. Alpha -> DataModel -> Bravo, with the assembly rule firing when both PartOne and PartTwo are in working memory, producing BravoResult and sending it to the Bravo actor via the Evrete Environment. Actors as thin membranes. All logic in rules. The architecture validates.

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
