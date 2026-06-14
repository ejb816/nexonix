# Draco Dev Journal — Chapter 41

**Session date:** June 14, 2026
**Topic:** From "continue TransformBuilder" to a draco-supplied actor execution context. The session pivoted hard — TransformBuilder's missing fixture turned out to be a whole *example world* of message domains (Aerial/Terrain/Marine/Ethereal under World), which in turn forced a Format domain and the Generator's first actor-from-`actorAspect` emission. Landed: `draco.format.Format[T]` + json/yaml/xml, Generator actor emission, and a green end-to-end Aerial Consumer actor+rule generated entirely from JSON.

---

## Opening — TransformBuilder had no real subject

> **Dev:** Let's continue with TransformBuilder, where there is more design and implementation planning work

DomainBuilder (chapter 40) had a sibling penciled in: `TransformBuilder`, the populated counterpart for *transform* domains. The plan was define/validate/generate/compose over the 12 reference-frame transforms. But `validate`-to-zero needs a real subject, and the Centric transforms are empty skeletons flagged "not a work target." Asking which subject to use drew the first redirect:

> **Dev:** The Centric domains got away from me in that they didn't easily allow for message type to message type cross domain transforms. I think I need you to help me define some notional message domains, that are complex enough to be distinctly different to require cross domain transforms, and realistic enough to be comparable to real message domains without compromising externally imposed information security constraints.

The diagnosis: the Centric frames are all the *same* data model (positions) under different origins — symmetric, so a transform is a coordinate rebase. Real message domains differ in *representation*. The frames weren't wrong, they were at the wrong layer — they become the geometric substrate a message transform computes *through*, not the transform itself.

A civilian/military-neutral carrier was needed. I proposed an air/maritime track picture; the Dev went further:

> **Dev:** I had the idea as you were thinking that we ignore the civilian/military distinct and then consider the domains of earth, water, air and space.

Key realization recorded: **medium ⟂ reference frame.** Earth/water/air/space differ in *native representation* (grid vs geodetic vs orbital), which is exactly the representational gap the Centric frames lacked.

## The recapitulation principle

> **Dev:** At the JSON format level the 4 domains don't exist as defined domains. They are independently developed message sets that are used to communicate data within a physical transport medium, initially independent of the other three media. Before transform services there is no way for semantic data to cross from one transport medium to another. Please don't try to project too far ahead about these domains being created to support TransformBuilder. We need to recapitulate the phylogeny of the draco framework domains into the ontogeny of these new example domains.

This retired a premature `domains.air` framing. Draco itself began as format-level JSON experiments (`org.nexonix.format`) before TypeDefinitions/domains/rules/actors existed; the examples replay that ontogeny. First cut moved to a pre-domain `media.air` namespace (hand-written `AirMessageSet`/`AirMedium` over loose JSON) — itself later superseded.

## The execution context must be draco-supplied

> **Dev:** We need to rework the implementation of the air domain and its element types, focusing first on the data format input/output followed by the domain types and domain type elements. This needs to be an interactive dialog with terse prompt/response exchanges.

A terse Q/R sequence settled the shape:

- **Execution context is generated, not hand-written:** TypeDefinition JSON → Generator → Scala. > **Dev:** *"supply from the draco framework... the execution context for the example domains... Part of the execution context will be actors that are message data creators, providers and consumers."*
- **Messages stay loose JSON** carried by strong shells: > **Dev:** *(a) Yes - Primal[Json]*
- **Three actor types with different rule sets:** > **Dev:** *(b) Three actor types with different rule sets*
- **Everything in src/mods:** > **Dev:** *"the examples and execution context should be in src/mods. Just tests in src/test."*
- **Two algorithmic phases:** > **Dev:** *"Creation and Streaming are different execution phases handled algorithmically. Streaming is loop-back through the transforms."* → Aerial-in-isolation is creation-only; streaming waits on World transforms.
- **A Format domain was missing:** > **Dev:** *"What's missing is the draco.format.Format domain with draco.format.json.Json, draco.format.yaml.Yaml, draco.format.xml.Xml... the format domain generic draco.format.Format[T] which can be in src/main"*

## The ruleTypeNames concession (an abandoned path)

I proposed adding `ActorAspect.ruleTypeNames` to bind each actor to its rule set. The Dev pushed back:

> **Dev:** I'm not clear on the need for ruleTypeNames, since to be in a domain a rule name is just another name in elementTypeNames which is part of DomainAspect.

Reading the wiring proved the Dev right: a session's rules are an *explicit enumeration* (`Rule.ruleType.pattern.accept(knowledge)`), so an actor needs only its list of rule names — and that list *is* `domainAspect.elementTypeNames`. A parallel field would duplicate it. The actor type doubles as a mini-domain owning its rules. The one real consequence folded into the emission work:

> **Dev:** Thank you for the clarification. Reusing elementTypeNames was my intent and I did not realize the code emission condition you mentioned would not work for types that were both actor and rule. Proceed.

`generate()` checks `isDomain` before `isActor`, so a type carrying *both* aspects would route to domain emission. The fix: a `hasActorBehavior` branch *before* `isDomain`.

## Landed

- **Format domain:** `draco.format.Format[T] extends Primal[T]` in src/main; `draco.format.json.Json extends Format[io.circe.Json]` (real) + yaml/xml stubs in src/mods. Mirrors `Language` (domain) and `Primal` (parametric).
- **Generator actor emission:** new `hasActorBehavior(td)` branch emits `actorType: ActorType = new Actor[T]` with receive/receiveSignal from `actorAspect.messageAction/signalAction`, and `private knowledge` built by walking the actor's `elementTypeNames`. Safe-by-construction: `hasActorBehavior` is false for every existing TD, so all prior emission is byte-identical and DracoGenTest is untouched.
- **build.sbt:** root now root-compiles `src/mods/scala/domains` and reads `src/mods/resources`.

## Name remap

> **Dev:** Before we continue I want to remap the domain names: air => Aerial, earth => Terrain, sea => Marine, space => Ethereal, with the supervening domain being World.

Clean swap — only the soon-deleted `media/air` carried the old name; Format and the Generator are name-agnostic.

## Aerial, green

Built in compile-checkpointed sub-steps because the Generator can't be run here (the Dev runs sbt):
- **6a** — Aerial domain + `PositionReport` shell (`extends Aerial with Json`), hand-templated from `Language`/`Distance` (zero new-emission risk). `AerialShellTest` green.
- **6b** — `Consumer` actor + `ConsumeReport` rule, **Generator-produced** via a gen-runner test the Dev executed. Both flagged risks evaporated: the `Consumer` vs `java.util.function.Consumer` name collision resolved by import precedence, and Evrete matched the anonymous `PositionReport` subclass against `classOf[PositionReport]`. **4/4 tests green** — a generated actor, its `knowledge` assembled from `elementTypeNames`, firing a generated rule through a thin-membrane receive, all from JSON.

## Where it stands / next

- **6c (next):** Creator → Provider chain with cross-actor sends. Blocked on a missing pattern — there is no existing example of a rule RHS reaching an `ActorRef` via the Evrete Environment. That is framework design, not JSON authoring.
- Then FlightIntent + Creation-phase algorithmic origination; Streaming + World (super-domain, strong cross-domain types, transform rules) is Phase 2 — and the eventual home of TransformBuilder, whose fixture this whole example became.
