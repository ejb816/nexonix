# Development Handoff

This is the shared resumption checkpoint for Claude Code and Codex, not an additional
architecture specification or a transcript. Follow DRACO.md section 7 and verify this
snapshot against the current tree before acting.

## Checkpoint

- Updated: 2026-10-15, by Claude Code, closing the session for a handoff. Destination:
  **Codex** (Dev named the OpenAI platform as the next task-session, continuing Generate).
  Everything the next agent needs is in tracked project files — DRACO.md, this file, the
  records, chapters 93 and 94 with their audits; no Claude-private memory is required or
  relied upon.
- Observed branch/HEAD: main at 78bff9d, pushed (`main...origin/main`) — the combined
  Create/Update and Editor-rules commit, one after f4336a4 (Delete), 799e9ae (Read),
  5057a5c (the Request family), 6869e47 (the combined delivery, CRUD and initialization
  commit), fcc80ba (chapter 93) and efa9085 (composition). Session start was fcc80ba.
- That commit (5057a5c) carried the Request-family increment: the new `Request` trio and
  the Latent, User and Editor trios under `src/mods/{resources,scala}/draco/dreams/
  {user,editor}`; the engine fixes in `src/mods/scala/draco/Drake.scala` (case-head
  dotted type) and `DracoGenerator.scala` (import scan into case bodies);
  `DrakeParseTest` (+1), `EditorTest`, `DreamsTest` (+2), `InitializationTest`; DRACO.md,
  README, CHANGELOG, the Dreams README, this file and record `git-record-2026-10-13-1311`,
  plus the confirmation of the SelfDeclaration alignment. No main-corpus TYPE added; the
  gates are byte-identical over the main corpus.
- That commit (799e9ae) carried the Read increment: the new `Read` trio and the User and
  Editor trios under `src/mods/{resources,scala}/draco/dreams/{user,editor}`; `EditorTest`
  (+1) and `DreamsTest` (+2); DRACO.md, README, CHANGELOG, the Dreams README, this file
  and record `git-record-2026-10-13-2158`, plus the 744 / 54 verification note for
  5057a5c. No main-corpus TYPE added, no engine change.
- That commit (f4336a4) carried the Delete increment: the core `DomainOntology` trio
  (`excluding`); the new `Delete` trio and the User and Editor trios; `DomainOntologyTest`
  (+1), `EditorTest` (+1), `DreamsTest` (+2); the documents and record
  `git-record-2026-10-14-1136`, plus the 747 / 54 verification note for 799e9ae.
- In the tree after it, documentation only: this verification note (DRACO.md §1 and §3,
  CHANGELOG, README, this file, record `git-record-2026-10-15-1132`).
- That commit (78bff9d) carried the Create/Update increment (2026-10-14) and the
  Editor-rules increment (2026-10-15) — under
  `src/mods/{resources,scala}/draco/dreams` the new `Create`, `Update`, `Accepted`,
  `CreateHeld`, `UpdateUnheld`, `ReadUnheld` and `DeleteUnheld` trios, the Latent (abstract),
  Request (three dyns), Read, Delete, User, Editor (thin actor) and Initialization (typed by
  Create) trios; in core the `DomainOntology` trio (`holds`, `holding`); the engine
  (`DracoGenerator`: transitive codec eligibility); `EditorTest` (+12), `DreamsTest` (+4),
  `DomainOntologyTest` (+1), `InitializationTest` (constructions); DRACO.md §1, §3 (two
  paragraphs) and §6 (two entries), README, CHANGELOG (three entries), the Dreams and
  Service READMEs, this file and record `git-record-2026-10-14-1217`. One core type gained
  two dyns; no TYPE added; the gates are byte-identical, so no headline moves.
- That commit carried: under `src/mods/{resources,scala}/draco/dreams/` the Editor and
  Actual trios (editor), the User and new Response trios and the Latent trio moved from
  editor to user (a `git mv` rename), the Dreams trio and the new Initialization trio; in
  core the `SelfDeclaration` trio (namePath comparison) and `DomainBuilder` (mods);
  `EditorTest`, `DreamsTest`, `SelfDeclarationRulesTest` and the new `InitializationTest`;
  DRACO.md §1, §3 (three paragraphs) and §6 (three gotchas), README, CHANGELOG (four
  entries), the Dreams, Service and mods READMEs, this file and record
  `git-record-2026-10-12-1413`. No main-corpus TYPE was added, so no report-only headline
  moves.
- Gate: Dev's full `sbt test` on 2026-10-15 passed **768 tests / 54 suites, 0 failed, 0
  aborted**, every report-only headline unchanged, then the commit and push went through
  (78bff9d). 768 = 751 + 17 as predicted, which corroborates the Delete figure (f4336a4,
  committed after its gate but with the output not pasted). Earlier read runs: 747 / 54
  (799e9ae, 2026-10-14), 744 / 54 (5057a5c) and 741 / 54 (6869e47) on 2026-10-13.
- Destination: either agent. Dev alternates sessions.
- Permission: Dev authorized both agents to maintain the journal and shared documentation
  on September 23. sbt, commit, push and new-issue restrictions remain in force.

## Decisions this session (Dev, 2026-10-09)

- `DomainDictionary` is the **domain ontology**, `DomainOntology`: an operational ontology
  built from domains and their intra- and inter-relationships. "Dictionary" was an
  expedient on the way to a formal ontology and now names only a domain's `TypeDictionary`.
- The authoring types are **Latent** and **Actual**, and they operate on **domains**, not
  ontologies: a Latent is one domain in candidate form, an Actual the accepted (or
  rejected) domain. Ontologies are built from domains and their relationships.
- Scope boundary accepted: the first slice takes a Latent to an Actual one domain at a
  time, reusing core's rules; the Editor builds its own knowledge from core's rule
  patterns (no reply protocol on the Draco actor).
- **The ontology is draco's innate, endogenous ontology** (Dev, 2026-10-12): the framework's
  own domains plus the dreams definitions as they can be accepted — self-referential, to be
  maintained with care. The Editor is accepted as its holder on condition that acceptance
  never compromises the framework: acceptance alters nothing running (generation and
  activation stay explicit), and `Uniqueness` — over name and package, parameters free —
  refuses a second owner of any name. Update-by-identity: a Latent whose domain name and
  package match an accepted domain replaces it. Delivering the grown ontology to a caller
  waits for the initialization client.
- **Derivation is the ontology's first explicit relationship**, with provenance on the
  edge (`declared`), declared edges first; inferred derivation (classification by shared
  elements) is a later rule. Scope of the second increment: the relationship alone —
  `Derivation`, projected `derivations`, `dependencies`, `including`, the rule reading
  edges — Editor untouched. Composing accepted Actuals into the ontology is still ahead.
- **The grown ontology reaches the caller through a consumer on the Editor's factory**
  (Dev, 2026-10-12 afternoon, the initialization client's first design point): the ontology
  has no codec, so it cannot ride a message or the Actual; the Editor's factory takes
  `ontology: DomainOntology -> Unit` beside `actual`, invoked after the Actual on every
  acceptance and never on a rejection. A caller keeping its last delivery holds the current
  state. Chapter 91's candidate-batch idea remains superseded, not pending.
- **User defines the requests, Editor the responses** (Dev, 2026-10-12 afternoon, restating
  the original CRUD view of Editor and User; accepted as the client/server interface of
  editing). The "client" is a User: the endogenous initialization client is draco as its own
  first User, not a new domain and not a Service type. Create and Update are the `Latent`
  (the ontology decides which, by identity); Read and Delete are LATER request types; the
  `Actual` is the response. `Latent` therefore lives in `draco.dreams.user`.
- **An abstract `Response` in User, derived by Editor's `Actual`** (Dev, same afternoon,
  after the mutual-dependency point was raised at Dev's request): a user consumes
  `User.Response` (`problems`, `accepted`), never `Editor.Actual`, so Editor depends on User
  and never the reverse — a cycle the ontology could hold but a Haskell module pair cannot.
- **The computed dependency order from an empty ontology** (Dev, same afternoon, the
  client's bootstrap order): one Latent per canonical domain, ordered from the definitions
  themselves, Draco first, nothing hand-listed. The client's home — `draco.dreams.Dreams`,
  the constructing layer — is the model's placement (it must name both User and Editor,
  which neither may do), stated, not asked; Dev may move it.
- **SelfDeclaration over name and package, parameters free** — NOT a decision Dev took in
  dialogue. The endogenous run refused `Format(F)` (bare self-declaration, as its members
  point) and XML, JSON and Value behind it; the model applied Dev's 2026-10-12 principle
  (parameters compose references, not definitions; a self-declaration points at the
  definition) and aligned the rule, reversing the 2026-10-09 whole-TypeName comparison.
  Dev CONFIRMED the alignment on 2026-10-13 ("I confirm the SelfDeclaration alignment"); the
  alternative (a parameterized self-declaration on Format) was rejected because the
  members' container pointers are bare.
- **The Request family** (Dev, 2026-10-13, "I accept the Request family in User,
  proceed" — Read's first design point): User declares abstract `Request`; Latent and
  Read derive it; the Editor's message type becomes `Request` and its message body
  dispatches by `case`. Delete joins the family later.
- **Read names one domain and is answered with an Actual** (Dev, 2026-10-13 evening, "I
  accept Read naming one domain answered with an Actual, proceed" — Read's second point):
  by TypeName (name + package, parameters free, as definitions are keyed); the answer is
  the domain's definition and members as the accepted ontology holds them, or a refused
  Actual naming the domain with one Problem when the ontology owns none; through the same
  Response consumer; a Read never changes the state and never delivers an ontology. Not
  chosen: a new response type for reads, reading a member by name, one answer carrying
  the whole ontology (no codec).
- **Delete through `excluding`** (Dev, 2026-10-14, "I accept Delete through excluding,
  proceed" — Delete's first point): one domain by TypeName; the ontology that would
  result through `excluding`, the inverse of `including`; judged by the same five rules
  over the whole of it, so a domain nothing depends on is removed, one another derives
  from is refused naming the dependent, an unowned one is refused with one Problem; the
  Actual carries the domain as held; the shrunk ontology is delivered on acceptance.
  Draco itself cannot be deleted while anything derives from its root. The CRUD
  vocabulary is complete. Model's choice, stated not asked: the judgment factored into
  one global `judge` on the Editor, called by the Latent and Delete branches.
- **Create and Update deriving Latent** (Dev, 2026-10-14, "I accept Create and Update
  deriving Latent, proceed", after asking why User had Read and Delete but not Create and
  Update — the answer was history, and a hidden difference: a Create of an existing name
  was a silent update, an Update of a misspelled name a silent creation). Latent is the
  abstract candidate shape; Create is refused when the identity is already owned, Update
  when it is not; both otherwise judged as before. Model's choice: a second global
  `holder` for the lookup all four branches share; the client typed by Create (Latent has
  no codec).
- **Editor-domain rules with the actor thinned** (Dev, 2026-10-15, "I accept Editor-domain
  rules with the actor thinned, proceed", after observing that the dreams domains carried
  no rule aspects, only an actor firing core's rules): the intent checks are four Editor
  rules over the request and an `Accepted` fact; the core five keep validating the
  provisional; the actor inserts, fires and reads the Actual back. Model's choices, stated
  not asked: `proposal`/`subject`/`contents` as bodiless dyns on User's Request that each
  verb implements (a request knows what it asks for); `holds`/`holding` in core; the
  engine's codec eligibility made transitive so `Accepted(ontology)` compiles (closing the
  §6 gap rather than dodging it).

## Completed Development

- **Ontology rename.** `DomainOntology` replaces `DomainDictionary` across the trio, the
  Draco member list, `DomainBuilder` (`dictionary` → `ontology`), the ZeroMQ bridge and
  five tests; `defines(typeName)` added. Map shape unchanged.
- **Core composition.** `TypeDictionary(domainDefinition, members = [])` and
  `Domain(domainDefinition, members = [])` populate from supplied definitions;
  `DomainBuilder.define` loads from resources and delegates.
- **Core validation.** `DerivationResolvable` joins the `DomainOntology` fact and flags
  any named parent it does not define — no package filter, no loader call.
  `SelfDeclaration` compares the whole TypeName. The validation suites and the Draco
  actor test insert the ontology as a fact.
- **Service.** `ServiceConfiguration` contains an `Assembly` (the accepted shape).
- **Editor.** `Latent`, `Actual` and the `Editor` actor in `draco.dreams.editor`, as
  generated trios under `src/mods/{resources,scala}/draco/dreams/editor`. Actual's
  populated `domain` is a dyn, not a carried field (codec eligibility is not transitive).
- **Delivery (2026-10-12 afternoon).** The Editor's factory is
  `actorType(initial, actual: Actual -> Unit, ontology: DomainOntology -> Unit)`; after the
  Actual, `guard(problems.isEmpty).ifThenElse(ontology(accepted), ())` delivers the new state
  on acceptance only. `()` is the corpus's first unit literal; it round-trips as a leaf.
  EditorTest's helper collects both consumers and their interleaving; two tests pin the
  contract.
- **CRUD reading (2026-10-12 afternoon).** `Latent` trio moved to `draco.dreams.user`
  (`git mv`); new `draco.dreams.user.Response` (`fix problems [draco.Problem]`, `dyn accepted
  Boolean problems.isEmpty`, no factory — abstract, the Aspects precedent); `Actual` is
  `type Actual from draco dreams user Response` keeping definition, members and the derived
  domain, its factory supplying `problems`, its codec carrying the inherited slot; `Editor`
  has `messageType draco dreams user Latent`, `types [Actual]`, consumer
  `(draco.dreams.user.Response -> Unit)`; `User` has `types [Latent, Response]`. A qualified
  reference in `from` and `messageType` is SPACE-separated package words. EditorTest: Latent
  trio checks gone, a dependency-direction test added (16). DreamsTest rewritten: Dreams
  anchor folded to one test, User/Latent/Response trio checks, request-domain and
  abstract-Response tests, the composition test pinning Latent at user and Editor → User (12).
- **Initialization client (2026-10-12 afternoon).** `draco.dreams.Initialization`, Dreams'
  first member (Dreams trio updated): `fix domains [Latent]`, `fix initial DomainOntology`
  (`DomainOntology()`), `fix responses [Response]+`, `mut ontology`, consumers `note`/`keep`,
  dyns `owns`, `parents`, `ready`, `order` (recursive, wave by wave), `ordered`,
  `editorType` (mints the Editor with the client's consumers), `run(editor)`, `complete`,
  `accepted`; codec over `domains` only. `SelfDeclaration` trio and `DomainBuilder.validate`
  compare `namePath`. `InitializationTest` (6): trio ×2, computed order over the 18 canonical
  domains supplied in reverse, cycle/dangling case, the endogenous run (all 18 accepted, the
  client in the ontology it built), JSON round trip. `SelfDeclarationRulesTest`'s
  parameter test inverted (checks Format(F) itself); EditorTest's SelfDeclaration case
  refuses a self-declaration naming another definition and accepts the bare one;
  DreamsTest's Dreams tests expect the one member.
- **Request family (2026-10-13).** `draco.dreams.user.Request` (no elements, no factory);
  `type Latent from Request`; User `types [Latent, Request, Response]`; Editor
  `messageType draco dreams user Request`, message body `case latent draco.dreams.user.Latent [ … ]`
  holding the whole validation with `latent` for `msg`; the generator writes
  `case latent: Latent =>` and `case _ => ()`. Engine: `Drake.parse` case head — a token
  whose last segment is upper-case is the type (a dotted type had become the binder's
  NAME); `DracoGenerator.externalImports` looks one level into case bodies (the `[Problem]+`
  inside the branch had lost its `mutable` import). Tests: DrakeParseTest +1 (dotted case
  head keeps its binder; unbound and default heads unchanged; round trip), DreamsTest +2
  (Request trio; User holds three), EditorTest (messageType Request, the one branch,
  Latent derives Request), InitializationTest (`Actor[Request]` cast).
- **Read (2026-10-13 evening).** `type Read from Request` with `fix domain draco.TypeName`
  (factory `par domain`); User `types [Latent, Read, Request, Response]`; Editor's second
  branch `case read draco.dreams.user.Read [ fix owner Presence(DomainType)
  presence(accepted.keys.find(\d -> namePath == read.domain.namePath)); mon
  actual(owner.fold(Actual(TypeDefinition(read.domain), [], [Problem(read.domain, s"domain
  … is not in the accepted ontology")]), \d -> Actual(d.typeDefinition,
  d.typeDictionary.elementTypes))) ]`. Tests: EditorTest +1 (Read before and after
  acceptance, a domain of the initial ontology with every member, an unknown domain, and
  only the one acceptance delivering an ontology; `deliveries` now takes `Seq[Request]`),
  DreamsTest +2 (Read trio; User holds four; Read derives Request and names one domain).
- **Delete (2026-10-14).** Core `DomainOntology` gains `dyn excluding DomainOntology [ par
  typeName TypeName ]` (filter by namePath; trio regenerated, DracoGenTest byte-identical).
  `type Delete from Request` with `fix domain draco.TypeName`; User `types [Delete, Latent,
  Read, Request, Response]`. Editor: a `globals` section with `dyn judge [draco.Problem]`
  (`par rules Knowledge`, `par provisional DomainOntology`; definitions, problems buffer,
  session, set/insert ×3/fire/close, `fix value problems.toSeq`) — the first globals on an
  actor-minting type, rendered as a companion `def` visible inside the minted actor; the
  Latent branch now `fix problems [Problem] judge(rules, provisional)`; the third branch
  `case delete draco.dreams.user.Delete [ owner; provisional = accepted.excluding(delete.domain);
  problems = owner.fold([one Problem], \d -> judge(rules, provisional)); swap; actual(held
  domain or stub); deliver on acceptance ]`. The emitter orders `factory` before `globals`
  (authored drake adopted). Tests: DomainOntologyTest +1 (excluding), EditorTest +1 (the
  seven-step Delete sequence; three branches and the one global pinned), DreamsTest +2
  (Delete trio; User holds five).
- **Create and Update (2026-10-14).** `Latent.drake` loses its factory (elements only,
  `from Request`); `type Create from Latent` and `type Update from Latent`, each with the
  two elements and a factory; User `types [Create, Delete, Latent, Read, Request, Response,
  Update]`. Editor: a second global `dyn holder Presence(DomainType) [ par ontology, par
  typeName ]` (the `presence(keys.find(namePath ==))` lookup); the Latent branch became
  `case create Create [ owner = holder(accepted, identity); provisional = including(...);
  problems = owner.fold(judge(rules, provisional), \d -> [Problem "already in the accepted
  ontology"]) ... ]` and `case update Update [ ... owner.fold([Problem "not in the accepted
  ontology"], \d -> judge(rules, provisional)) ... ]`; Read and Delete use `holder` too.
  Initialization: `domains`, `order`, `ordered`, `run` typed `[draco.dreams.user.Create]`.
  Tests: every former `Latent(...)` construction is a `Create(...)` (perl rename), the
  three update-sequence steps and the delivered-update step are `Update(...)`; EditorTest
  +1 (the intents test; four branches and two globals pinned; Latent abstract), DreamsTest
  +4 (Create and Update trios; User holds seven; derivations pinned); InitializationTest
  builds Creates and decodes as Create.
- **Editor-domain rules (2026-10-15).** Core `DomainOntology`: `dyn holds Boolean [par
  typeName]` and `dyn holding Presence(DomainType) [par typeName]`. User `Request`: three
  bodiless dyns `proposal`, `subject`, `contents` (each `par accepted DomainOntology`);
  Create/Update/Read/Delete implement them (Create/Update: `including(Domain(definition,
  members))`, own definition and members; Delete: `excluding(domain)`; Read: `accepted`;
  Read/Delete subject and contents through `holding(domain).fold(stub, held)`). Editor:
  `Accepted` (`fix ontology DomainOntology`, factory — codec-less by the transitive rule);
  rules `CreateHeld` (`var c draco.dreams.user.Create`, `var a Accepted`, `con
  a.ontology.holds(c.definition.typeName)`, Problem "already in the accepted ontology"),
  `UpdateUnheld`, `ReadUnheld`, `DeleteUnheld` (the `!` conditions, "not in the accepted
  ontology"); `types [Accepted, Actual, CreateHeld, DeleteUnheld, ReadUnheld,
  UpdateUnheld]`; the actor's start fires nine patterns, its message is the membrane
  (`now fix provisional/subject/contents/changing`, definitions, problems buffer, session,
  five inserts, fire, close, swap, actual, deliver). No globals, no cases. Engine:
  `DracoGenerator.namesCodecless` + `parentChain` (transitive codec eligibility). Tests:
  EditorTest +11 (trios ×5, the rules in a bare session, the structural test pins six
  members, nine patterns, no cases, no globals, a Read's proposal is the same ontology),
  DomainOntologyTest +1 (holds/holding), DreamsTest pins Request's dyns and the six members.
- **Tests.** `EditorTest` (13: six trio checks, one structural, six behavioural: acceptance
  with a populated domain, Completeness/DerivationResolvable/SelfDeclaration problems,
  ordering and non-mutation, JSON round trip). ServiceTest +2, SelfDeclarationRulesTest +1,
  DerivationResolvableRulesTest rewritten (+1, including a parent no resource holds).
  DreamsTest keeps Dreams and User only. DomainBuilderTest checks `defines`.
- **Docs.** DRACO.md (orientation paragraph, §1 probe sentence, three §6 gotchas, retired
  list), README (ontology vocabulary, work in progress, status), drake.dlt history line,
  ZEROMQ contract, Service/Dreams/mods READMEs, CHANGELOG (Added + Changed), git record.

## Verification

Editor-rules increment (2026-10-15, one commit with the Create/Update increment below):
Dev's full run passed **768 tests / 54 suites, 0 failed, 0 aborted**, every report-only
headline unchanged — the current verified baseline, recorded in DRACO.md §1; committed and
pushed as 78bff9d. Before it, Claude did not run sbt. A compiled-class probe recompiled the engine
(`DracoGenerator`), the core `DomainOntology` and all seventeen dreams companions into
`target/scala-2.13/probe-classes` and DomainOntologyTest, EditorTest, DreamsTest and
InitializationTest into `probe-test-classes` over the current overlay: **DomainOntologyTest
8, EditorTest 30, DreamsTest 22, InitializationTest 6 — 66 / 0 failed**. Gates and the
ontology-using suites against the new engine: **DracoGenTest, DrakeGenTest,
SourceContractTest, UniquenessRulesTest, DerivationResolvableRulesTest, ServiceTest,
DomainBuilderTest — 319 / 0 failed**: transitive codec eligibility changes nothing in the
main corpus. `DrakeCLI check`: user, editor and dreams 0 drift, main corpus the two
authored-ahead files only. Two probe iterations caught: the engine's stub test takes a
TypeName; a lambda inside a splat argument passing through as host text (bound first); a
named parent is not a codec guarantee (DomainOntology derives Dictionary — the loaded parent
chain is the family context now); and three test expectations of mine. Expected sbt
arithmetic from 751 / 54 (inferred, see Gate): +1 +4 (Create/Update) +1 +11 (rules) =
**768 / 54**, no headline moves. Probe dirs removed. Dev's full run is the gate.

Create/Update increment (2026-10-14; gated with the increment above, 768 / 54 at 78bff9d):
before Dev's run, Claude did not run sbt. A compiled-class
probe over overlays REBUILT from Dev's post-Delete classes (the first attempt used overlays
from before that run: `excluding` missing at compile, then stale test classes ran — refresh
the overlay after EVERY Dev run) recompiled the twelve staged companions into
`target/scala-2.13/probe-classes` and EditorTest, DreamsTest and InitializationTest into
`probe-test-classes`: **EditorTest 19, DreamsTest 22, InitializationTest 6 — 47 / 0
failed**; ServiceTest 9 / 0 unchanged. `DrakeCLI check` on user, editor and dreams: 0
drift. Expected sbt arithmetic from 751 / 54 (inferred, see Gate): +1 +4 = **756 / 54**,
no headline moves (no main-corpus change). Probe dirs removed. Dev's full run is the gate.

Delete increment (2026-10-14): committed by Dev as f4336a4 after the gate; the run's
output was not pasted (751 / 54 expected). Before it, Claude did not run sbt. A compiled-class probe
recompiled `DomainOntology` and the ten staged companions into
`target/scala-2.13/probe-classes` and DomainOntologyTest, EditorTest, DreamsTest and
InitializationTest into `probe-test-classes` over the current overlay: **DomainOntologyTest
7, EditorTest 18, DreamsTest 18, InitializationTest 6 — 49 / 0 failed**. DracoGenTest,
DrakeGenTest, UniquenessRulesTest, DerivationResolvableRulesTest, ServiceTest and
DomainBuilderTest from the compiled tests against the probe: **316 / 0 failed** — the
regenerated `DomainOntology.scala` is byte-identical with its gate. `DrakeCLI check`: user
and editor 0 drift, main corpus the two authored-ahead files only. Expected sbt arithmetic
from Dev's 747 / 54: +1 +1 +2 = **751 / 54**, no headline moves. Probe dirs removed. Dev's
full run is the gate.

Read increment (2026-10-13 evening): Dev's full run on 2026-10-14 passed **747 tests / 54
suites, 0 failed, 0 aborted**, every report-only headline unchanged — the current verified
baseline, recorded in DRACO.md §1; committed and pushed as 799e9ae. Before it, Claude did
not run sbt. A compiled-class
probe recompiled the nine staged companions into `target/scala-2.13/probe-classes` and
EditorTest, DreamsTest and InitializationTest into `probe-test-classes`, over the overlay
rebuilt from Dev's 741 classes with the engine fixes compiled in: **EditorTest 17,
DreamsTest 16, InitializationTest 6 — 39 / 0 failed**; ServiceTest 9 / 0 unchanged.
`DrakeCLI check` on user and editor: 0 drift. Expected sbt arithmetic from Dev's 744 / 54:
+1 +2 = **747 / 54**, no headline moves (no main-corpus TYPE added, no engine change).
Probe dirs removed. Dev's full run is the gate.

Request-family increment (2026-10-13): Dev's full run passed **744 tests / 54 suites, 0
failed, 0 aborted**, every report-only headline unchanged — the current verified baseline,
recorded in DRACO.md §1; committed and pushed as 5057a5c. Before it, Claude did not run
sbt. A compiled-class probe over overlays REBUILT from Dev's 741 / 54 classes — the first attempt used overlays
copied before that run and failed two SelfDeclaration cases with the pre-alignment rule:
stale classes, not code; refresh the overlay after every Dev run — recompiled the engine
(`Drake`, `DracoGenerator`) and the eight staged companions into
`target/scala-2.13/probe-classes` and the four changed suites into `probe-test-classes`:
**DrakeParseTest 239, EditorTest 16, DreamsTest 14, InitializationTest 6 — 275 / 0 failed**
(surface losses 1 across 115, parse scope 105 + 10 unchanged). Gates against the new
engine from the compiled tests: **DracoGenTest, DrakeGenTest, SourceContractTest,
ScenarioDrakeTest, ServiceTest, GenDrakeTest — 307 / 0 failed**, scenario 23 clean,
GenDrake 103 of 103: the engine fixes are byte-identical over the main corpus. `DrakeCLI
check` on user, editor, the ash scenario and the main corpus: no drift beyond the two
authored-ahead files. Expected sbt arithmetic from Dev's 741 / 54: +1 +2 = **744 / 54**,
no headline moves. Probe dirs removed. Dev's full run is the gate.

Initialization increment + SelfDeclaration alignment (2026-10-12 afternoon): Dev's full run
on 2026-10-13 passed **741 tests / 54 suites, 0 failed, 0 aborted**, every report-only
headline unchanged — the current verified baseline, recorded in DRACO.md §1. Before it,
Claude did not run sbt. A compiled-class probe recompiled `SelfDeclaration`,
`DomainBuilder` and the seven staged companions (Dreams, Initialization, User, Latent,
Response, Actual, Editor) into `target/scala-2.13/probe-{classes,test-classes}` over an
overlay of current resources on Dev's efa9085 classes (hidden dotfiles and the old
editor/Latent excluded) and ran the four changed suites — **InitializationTest 6,
DreamsTest 12, EditorTest 16, SelfDeclarationRulesTest 3: 37 / 0 failed** — and seven
unchanged suites against the new rule (Foundation, DracoActor, DomainBuilder, Service,
DomainOntology, Uniqueness, DerivationResolvable: **34 / 0**). `DrakeCLI check`: dreams and
user 0/0, main corpus the two authored-ahead files only. Expected sbt arithmetic from Dev's
733 / 53: +1 +1 +6 = **741 / 54**, no headline moves (no main-corpus TYPE added). Probe
dirs removed. Dev's full run is the gate. Before the Format finding, the first probe of
this increment had the endogenous run refusing 4 of 18 domains — the finding, not a
test defect.

Delivery + CRUD increments (2026-10-12 afternoon): Dev's full run passed **735 tests / 53
suites, 0 failed, 0 aborted**, every report-only headline unchanged — the current verified
baseline, recorded in DRACO.md §1. Before it, Claude did not run sbt.
A compiled-class probe recompiled the five regenerated companions (user: User, Latent,
Response; editor: Actual, Editor) and both suites into `target/scala-2.13/probe-{classes,
test-classes}` over an overlay of the current main and mods resources on Dev's efa9085
classes (hidden dotfiles and the old editor/Latent excluded) and ran them with the
scalatest Runner: **EditorTest 16 / 0 failed** (was 15), **DreamsTest 12 / 0 failed** (was
11), **ServiceTest 9 / 0** unchanged. `DrakeCLI check` on both directories: surface drift
0, json drift 0. Two probe iterations caught defects in the new DreamsTest assertions only
(a loaded definition is rooted at DracoType; every dreams domain therefore depends on
Draco). Expected sbt arithmetic from 733 / 53: +1 +1 = **735 / 53**, no headline moves.
Probe dirs removed. Dev's full run is the gate.

Composition increment: Dev's full run on 2026-10-12 passed **733 tests / 53 suites, 0 failed,
0 aborted**, headlines at 105 draco + 10 mods, one loss across 115 types, 105 aspect
sections, GenDrake 103/103, everything else unchanged; committed/pushed efa9085. That is
the current verified baseline; DRACO.md §1 records it. The probe before it — main, mods
and tests recompiled into `target/scala-2.13/probe-*`, hidden resources excluded — had
passed 733 / 0 failed / 53 suites as sbt counts (54 with the Runner's wrapper): 723 + 5 corpus tests for `Uniqueness` + 2 `UniquenessRulesTest`
+ 2 net in EditorTest + 1 in DomainOntologyTest. The added corpus type moves four
headlines by one — parse scope 105 draco + 10 mods, one loss across 115 types, 105
aspect sections, GenDrake 103/103; the DRACO.md table carries the two tabled rows.
Everything else unchanged. Probe dirs removed.

Derivation increment: Dev's full run on 2026-10-10 passed **723 tests / 52 suites, 0 failed,
0 aborted**, the four moved headlines matching the probe, and committed/pushed d181e64.
That is the current verified baseline; DRACO.md §1 records it. The probe before it —
main, mods and tests recompiled into `target/scala-2.13/probe-*`, hidden resources
excluded — passed **723 tests / 0 failed / 52 suites** as sbt counts (53 with the
Runner's wrapper): 713 + 5 corpus tests for `Derivation` + 5 in `DomainOntologyTest`.
The one added corpus type moves four headlines by one — parse scope 104 draco + 10 mods,
one surface loss across 114 types, 104 sections in both aspect probes, GenDrake 102/102;
the DRACO.md table carries the two tabled rows. Every other figure unchanged. Two
earlier probe iterations caught defects in the new tests only. Probe dirs removed.

First authoring slice (verified): Claude did not run sbt. A compiled-class probe recompiled the WHOLE tree — main, mods
and tests — into `target/scala-2.13/probe-{classes,test-classes}` (the `Domain` factory's
new parameter makes every compiled companion binary-stale, so a partial overlay was not
an option), overlaid current resources (hidden dotfiles excluded, as sbt excludes them),
and ran every suite with the scalatest Runner:

- **713 tests, 0 failed, 0 aborted**; 52 suites as the Runner counts (51 as sbt will:
  50 + EditorTest). Expected sbt arithmetic from Dev's 701: −5 +13 +2 +1 +1 = 713.
- Report-only headlines all match DRACO.md: gen map 28/20/0/0 of 48; surface losses 1
  across 113; parse scope 103 draco + 10 mods; PON 80/550/42; canonical 7; scenario 23
  clean; DomainAspect 103/1/0; DracoAspect 103/2/0; GenDrake 101/101; CO-DECLARATION
  DROPS THE FACT; mods actors pending Drake authoring 0 (file-only).
- Earlier probe iterations caught: the ambiguous single-fact `insert` (fixed with
  `Seq(fact): _*`); unsupplied members never reaching working memory (the Editor now
  inserts the candidate's populated `elementTypes`); the obsolete `draco`-package filter.
- The probe directories were removed afterwards. Probe logs: scratchpad only, transient.

Dev then ran the full gate on 2026-10-09: **713 tests / 51 suites, 0 failed, 0 aborted**,
every report-only headline matching, and committed/pushed 462cde6. That is the current
verified baseline; DRACO.md §1 records it.

## Documentation Coverage

- Journal coverage reaches chapter 94 (this Claude session, 2026-10-12 to 10-15): 16
  assertive Dev prompts verbatim, 19 final replies near-verbatim, 5 delegated-action notes;
  the bounded extract `sources/claude-2026-10-12-to-15.jsonl` (39 records with source
  line, UTC, SHA-256) and its audit record the frozen prefix (3381 lines). The reply
  answering Dev's wrap-up request — this documentation pass — is outside the boundary;
  chapter 94's Status says so. One evidence gap is disclosed in the chapter: the gate run
  for f4336a4 (Delete) was never pasted; its 751 / 54 is corroborated by the next run. Both
  `draco-docs-synced-through` markers advance to 94 after reviewing DRACO.md and README
  against the chapter — they were written during the session and carry its decisions.
- Chapter 93 (2026-10-07 to 10-12) and Codex's chapters 90–92 with their audits are
  untouched by this pass.
- Codex's chapters 90–92 and their audits are untouched by this session.

## Resumption

1. The Create/Update and Editor-rules increments are committed and pushed as 78bff9d at
   the verified 768 / 54 baseline, which also corroborates the Delete figure. Only this
   verification note is pending: a documentation-only commit with
   `draco-git-record/git-record-2026-10-15-1132`, no sbt run needed.

2. Next authorized work, in Dev's accepted order: generation and activation as explicit
   operations — open with one design point before building (what a generation request
   names, what it produces, and that acceptance never triggered it). Later: inferred
   derivation (classification by shared elements). Candidate rule surfaced, not filed:
   membership reciprocity (a supplied member the domain does not name is silently
   dropped).

### First action for the next agent

Read DRACO.md, this file and the 2026-10-15-1132 record; inspect `git status` and `git log`.
Expect HEAD at or after 78bff9d (baseline 768 / 54) with a clean tree once Dev has committed
the documentation note. Read chapter 94's Status and DRACO.md §3's eleven 2026-10-09/15
paragraphs for the decisions; chapter 94 and its audit cover this Claude session completely
up to Dev's wrap-up request, so nothing from it needs re-importing. The next authorized work
is GENERATION as an explicit operation. Its first design point is RAISED and NOT YET ACCEPTED
(Claude's recommendation, 2026-10-15): a fifth User request, `Generate`, naming a held domain
and a transform domain by TypeName; answered through the same Response consumer with a new
Editor response, `Generated`, deriving Response and carrying one `generator.Emission` per
definition of the domain, produced by inserting the domain's definition and members into a
fresh session built from the transform domain's rules exactly as `GenDrakeTest` runs it; a
domain or transform the ontology does not hold is a Problem by a fifth Editor rule in the
shape of the four; nothing is written anywhere and nothing is activated (the consuming project
owns persistence); the state is untouched, so no ontology is delivered; GenDrake is the first
transform a Generate can name (GenScala is not yet migrated). Open that with Dev — accept or
correct — before building; then activation as an explicit operation, with its own first point.
Later: inferred derivation. Surfaced, not filed: membership reciprocity.
