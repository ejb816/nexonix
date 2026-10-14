# Dreams Name Skeleton

Established October 7, 2026, before the next authoring increment. Three empty,
definition-backed domains name the layer surrounding the Draco core:

| Domain | Intended direction; no members implemented yet |
|---|---|
| draco.dreams.Dreams | Construction and evolution of Draco-defined systems. Since 2026-10-12: `Initialization`, the endogenous client (below). |
| draco.dreams.editor.Editor | Definition-authoring operations and editing workflows. Since 2026-10-09: Latent, Actual and the Editor actor (below). |
| draco.dreams.user.User | User-facing participation and interaction. Since 2026-10-12: the editing REQUEST vocabulary — the abstract `Request` family (2026-10-13), `Latent` (Create/Update), `Read` and `Delete` deriving it, and the abstract `Response` (below). |

Each domain has a canonical Drake/JSON/generated Scala trio under
src/mods/{resources,scala}/draco/dreams. The build's existing staging paths already
compile and load them; no new dependency or build project is needed.

The domains are ontology peers. Package nesting adds neither a super-domain edge
nor membership in Dreams or Draco. Compose them explicitly with Draco and Service
when building a DomainOntology. Dreams has empty membership; User holds the two request
types and, like Dreams, no rule, actor or codec aspects; Editor is described below. This increment adds no editing behavior, user model, authentication,
persistence, activation or changes to the ZeroMQ bridge.

The former handwritten src/main/scala/draco/dreams/Dreams.scala scaffold is replaced
by the generated Dreams in src/mods, preserving its fully qualified name without a
duplicate declaration. The old draco.dreams.Service trait and draco.dreams.orion
scaffolds remain in src/main. They have no corresponding domain trios or registration;
draco.dreams.Service is not an alias, parent or replacement for draco.service.Service.
No behavior or dependency on those legacy scaffolds is introduced here.

The next authoring design can use Editor for application operations while retaining
reusable capabilities in draco.service.Service and validation/dictionary primitives
in core. Detailed message, actor and configuration shapes still need implementation;
the [agreed Service plan](../service/README.md) remains the planning reference.

## The first authoring slice (2026-10-09)

Editor is a domain of one member (two until 2026-10-12, when Latent moved to User) and
an actor. A **Latent** is one domain in candidate form: its definition plus its
members' definitions. An **Actual** is the same definitions with the problems found,
accepted iff there are none; its populated DomainType is derived (`domain`), not
carried, so the Actual stays JSON-codable; since 2026-10-12 it derives User's
`Response`. The **Editor actor** takes an initial DomainOntology, a consumer of
Response and (since 2026-10-12) a consumer of DomainOntology; per Latent
it composes the candidate through core's `Domain(definition, members)` factory, builds
the ontology that would result, and fires core's own rules — Completeness,
SelfDeclaration, DerivationResolvable, CollectProblems — over the candidate's populated
members in a fresh stateful session. It never consults the classpath and never mutates
the ontology it was given. Since 2026-10-12 it HOLDS the accepted ontology: each Latent
is judged against `accepted.including(candidate)` — including replaces a domain of the
same name and package, so a resubmission is an update — with every composed domain and
definition in working memory and the five core rules firing (Uniqueness refuses a name
owned by more than one domain); the state is swapped only when every rule is quiet.
The grown ontology reaches the caller through the factory's `ontology` consumer, after
the Actual, on every acceptance and never on a rejection: the ontology has no codec, so
it cannot ride a message or the Actual, and a caller keeping its last delivery holds the
current state (the initialization client's first design point, settled 2026-10-12).
The editing interface is Dev's CRUD reading (2026-10-12): **User defines the requests,
Editor the responses.** Latent is the Create/Update request — the ontology decides which,
by identity — and Read and Delete are later request types. Since 2026-10-13 the requests
are a FAMILY: `Request`, abstract (no elements, no factory), is the Editor's message type;
Latent derives it; the Editor's message body dispatches by `case latent draco.dreams.user.Latent [ … ]`,
the corpus's first cross-package case dispatch, with the generator's own catch-all for a
Request that has no branch yet. `Read` is the second branch (2026-10-13): it names one
domain by TypeName (name and package, parameters free, as definitions are keyed) and is
answered through the same Response consumer — an Actual of the domain's definition and
members as the accepted ontology holds them, or an Actual with a stub definition, no
members and one Problem naming the domain when the ontology owns none. A Read never
touches the state and never delivers an ontology; the ontology has no codec, so a user
reads it one domain at a time. `Delete` is the third branch (2026-10-14): it names one
domain, the ontology that would result comes through core's `excluding` (the inverse of
`including`), and the same five rules judge the whole of it — a domain nothing depends on
is removed and the shrunk ontology delivered, a domain another derives from is refused
naming the dependent, an unowned domain is refused with one Problem; the Actual carries
the domain as the ontology held it. Draco itself cannot be deleted while anything derives
from its root. The judgment is one global on the Editor, `judge(rules, provisional)`,
shared by the Latent and Delete branches — the first `globals` section on an
actor-minting type. `Response` in User is the
abstract reply, `problems` and `accepted`, elements without a factory; Editor's Actual
derives it (`from draco dreams user Response`, the corpus's first cross-package `from`),
so a user consumes `User.Response` and never names `Editor.Actual`, and the dependency
between the two domains runs one way — Editor depends on User, stated by the derivation
edge the ontology already projects. A cycle would have been an ontology edge a Haskell
module pair cannot express. The endogenous initialization client is draco acting as its
own first User.
Latent and Actual operate on domains; the ontology is built from domains and their
relationships, and the Editor's state is draco's own innate ontology, so acceptance
never generates or activates anything. Two core changes carry this: `DerivationResolvable` resolves every named
parent against the `DomainOntology` fact in working memory, and `SelfDeclaration`
compares the whole TypeName.

DreamsTest covers the two empty anchors because the main-corpus gates do not scan
staged draco definitions: five tests per domain plus one combined composition test
with Editor, Draco and Service. EditorTest covers the Editor trio and the actor's
behaviour: acceptance with a populated domain, the three problem kinds, ordering
and non-mutation, and the JSON round trip. The direct compiled-class probe passed 555 tests / 7
suites with unchanged parse scope, surface loss and GenDrake counts. Dev's October 7
full sbt run passed 701 tests / 50 suites with zero failures/aborts, unchanged
report-only baselines and zero pending mods actors in the file-only Drake report.

## The endogenous initialization client (2026-10-12)

`draco.dreams.Initialization` is Dreams' first member: draco acting as its own first
User. It lives in Dreams because it must name both User (its requests) and Editor (the
actor it drives), which neither of those may do without re-creating the cycle the
abstract Response removed. It takes the canonical domains as Latents — supplied, never
loaded: the consuming project owns persistence; `InitializationTest` supplies them from
the resource tree through DomainBuilder — and ORDERS them from the definitions (Dev: "the
computed dependency order from an empty ontology"): `owns`, `parents` (named parents
outside the Latent), `ready` (every parent some supplied domain owns is already placed)
and `order`, a recursive dyn placing a wave at a time, the supplied order kept within a
wave, a parent nobody supplied treated as no constraint (the Editor refuses it), the
remainder appended unchanged when no wave forms so a cycle cannot hang it. Every loaded
definition is rooted at DracoType, so Draco alone is the first wave. `editorType` mints
the Editor over `DomainOntology()` with the client's own consumers, `note` and `keep`;
`run(editor)` sends the ordered Latents; `mut ontology` holds the last delivery, and
`complete`/`accepted` read the state. Nothing is hand-listed: a new corpus domain never
means editing the client. Its first run refused Format, XML, JSON and Value: `Format(F)`
self-declares bare, as its members point at it, so `SelfDeclaration` now compares name and
package with parameters free (the Uniqueness principle). The run then accepts all 18
canonical domains, and the ontology it builds defines the client itself.
