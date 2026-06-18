# Draco Dev Journal — Chapter 45

**Session date:** June 16–17, 2026
**Topic:** Phase 2 opens. A question about generated sources surfaced the real architecture of the transformation service — the `Transformer` is not a per-medium actor but the **`World`** domain — which forced a strip of the just-renamed media `Transformer`, then a sequence of clarifications (SourceSink as an integration-layer stand-in; messages as composition trees; the general-vs-message-domain distinction) that culminated in laying the `World` foundation: the four media now derive `World` as subdomains, with message-domain-ness carried implicitly by `Domain[T]`.

---

## The question that moved the architecture

> **Dev:** So when will generated scala sources appear in src/mods/scala?

The honest answer — *some already do* (`AerialGenTest` regenerates the Aerial `Consumer`/`ConsumeReport` each run); the rest waits on folding three discovered emission shapes into the Generator — but the question's real payload arrived next:

> **Dev:** It turns out that Format domains don't reference Transformer actors, all the rules that execute transforms as actions are in the World domain which is not a Format domain. The World domain has two actors, a consumer for transform input and a provider for transform output. The World consumer actor receives the World message type, and the World provider actor sends the transformed data to the proper subdomain consumer actor.

This relocated the `Transformer` entirely. It is not a per-medium actor — it is the `World` domain. Each Format (media) domain is a `SourceSink` (`Creator` + `Consumer`); `World` holds the transform rules and the input/output membrane. The per-medium `Transformer` we'd renamed the turn before was Phase-1 scaffolding mislocated inside the Format domains.

> **Dev:** 1. Strip it. 2. The actual subdomain actor of World that receives format data and converts it to typed data, which is an indirect subtype of World and a direct subtype of the subdomain.

So the strip executed across all four media: `Transformer` and its `RelayReport` identity rule removed, the `Creator` rewired to send directly to the `Consumer` (env role `transformer → consumer`), chain tests collapsed to a single `Creator → Consumer` origination each. And #2 named the key fact: the `Format ⇒ typed` decode happens in the *subdomain*, producing a value that is a *direct* subtype of the subdomain and an *indirect* subtype of `World`.

## SourceSink is an integration layer

> **Dev:** The SourceSink is a test stand-in for an abstract meta-adapter (alternatively, integration layer) that converts between manifold external disjoint message formats and message domains and a single format that can distinguish between them in both input and output directions. The subdomain actors that ingest the uniform format and send typed data to be transformed are the input side of the transformation service and the subdomain actors that ingest the transformed typed data and send the uniform format data back to SourceSink are the output side.

That gave a clean two-layer stack, with the **uniform format** as the seam:

- **SourceSink** (a test stand-in) absorbs *format* diversity: manifold external formats ⇄ one uniform format that still distinguishes the subdomains. In the example the four media's loose JSON *already is* that uniform format — the discriminator (`message:"POSITION"` vs `type:"LOCATION"` vs `kind:"FIX"` vs `category:"EPHEMERIS"`) is exactly "a single format that can distinguish between them."
- **World** (the transformation service) absorbs the *semantic* work: per-subdomain input adapter (`Format ⇒ typed`), transform rules (`typed ⇒ typed`, meaning preserved), per-subdomain output adapter (`typed ⇒ Format`) back to SourceSink.

The current `Creator`/`Consumer` are the SourceSink stand-in (inject / receive uniform JSON); the adapters and transform rules are what `World` will add.

## Messages are composition trees; message domains enforce subtyping

> **Dev:** Messages, whether type or format, are represented by a composition tree of primal and composite named types and format data associated with the names referencing compositional values. Those names are themselves named as fields, elements, properties disjunctively… The message type name represents the entire set of possible values for any specific instance. A general domain does not require the contained types be subtypes of the domain type. A message domain does require the contained types be subtypes of the domain type.

This is the data-model floor, and it maps onto draco's existing self-description: a message is a `TypeDefinition` (a composition tree of `Primal` leaves and composite elements); *field = element = property* is one concept (`TypeElement`) under three vocabularies; the type name denotes its instance set. "Type or format" is the *same tree* — the format form binds loose data at the names, the typed form binds typed values; so the input/output adapters are *tree-preserving* (loose↔typed, same names) while the cross-medium transform is *tree-changing but meaning-preserving*.

And the load-bearing distinction — **general domain** (a dictionary, no subtype requirement: `Draco`, `Base`, `Primes`) vs **message domain** (contained types must subtype the domain: `World`, each medium) — is the `MessageDomain` concept Orion had deferred, now concrete. It *explains* the earlier subtype-chain claim: a typed message subtypes its medium, and the medium (a message domain) subtypes `World` (a message domain), so the value is an indirect `World` subtype because both links are message domains.

> **Dev:** I think eventually (more sooner than later) we can make Message[Domain] explicit, but for now let's create it implicitly with Domain[T].

So no new type yet: message-domain-ness is carried purely as a *discipline* — every contained type derives the domain — over the existing `Domain[T]`. The explicit `Message[Domain]` later only names and enforces what the derivation already does.

## The foundation

> **Dev:** Proceed with the foundation.

Laid the smallest safe slice — the hierarchy only:

- `domains.world.World` — a bare `Domain[World]`, *not* a Format domain.
- The four media derive `World`: Scala `trait Aerial extends World` (Scala-first, per the inheritance rule) plus `dracoAspect.derivation = [World]` in each domain JSON. This follows the reference frames' `Cosmocentric ← Egocentric` super-domain precedent (peers related by derivation, not package nesting — nesting is reserved for transforms).
- `WorldHierarchyTest` asserts each medium's shell is, transitively, a `World`, while still its own medium and a `Json` shell.

> **Dev:** [`testOnly domains.world.* domains.aerial.* …`]
> *each medium derives World, and its message shells are indirect World subtypes*
> **Tests: succeeded 10, failed 0**

Additive — the Format-side tests stayed green. The subtype spine `shell ⊂ medium ⊂ World` now holds, carried by `Domain[T]` alone.

## Where this leaves things

`World` exists as the super-domain; the message-domain discipline is in place implicitly. What remains for the transformation service — and awaits the typed-form spec — is the substance: the **typed message forms** (composition trees, `Domain[sub<:World]`), the per-subdomain **input** (`Format ⇒ typed`) and **output** (`typed ⇒ Format`) adapters, and the meaning-preserving **transform rules**. The natural opening is a single `Aerial ⇒ Terrestrial` vertical: inject an Aerial `POSITION`, watch it come back out as a Terrestrial `LOCATION` with the world-fact intact — the moment "preserves meaning" becomes a passing assertion.
