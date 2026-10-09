# DRACO.md

Shared operating rules for Claude Code and Codex in this repository. `CLAUDE.md` and
`AGENTS.md` are symlinks to this file. Read `DEVELOPMENT_HANDOFF.md` when resuming work
or transferring development between agents; the bidirectional protocol is in section 7.

**The architecture baseline was audited on 2026-08-15; later verified updates are dated below.** The previous
version of this file described an architecture that had not existed for months — the
`*Instance` triad, `typeInstance` vals, `TypeElement extends Primal[String]`, Generator
owning type loading. It steered sessions wrong for as long as it stood. If you find a
statement here contradicted by the code, **the code is right and this file is a bug** —
fix it in the same commit as the work that exposed it.

---

## 1. Operating rules

These are the rules that do not change with the architecture. Read them first.

**Do not run sbt, git commit, or git push.** Dev compiles, commits, and pushes from the
IDE. Hand over a command block instead. `git status`, `git log`, `git show` and other
read-only git are fine.

**A new GitHub issue requires Dev's explicit approval, every time.** When work is scoped
out, a decision is deferred, or a bug is noticed in passing, *surface it in conversation*
— batched and prioritised — and file only if Dev says so. Commenting on an existing issue
does not need approval. The backlog grows faster than it drains, and an unprompted issue
buries the few that matter.

**Definitions move as a trio.** A type is three artifacts that must agree:
`src/main/resources/<pkg>/X.drake` (the Source — drake is the definition language),
`src/main/resources/<pkg>/X.json` (the normative form the drake parses to, a bootstrap carrier
and the only form loaded at runtime; re-canonicalized from the drake with `DrakeCLI parse` after
every parser step), `src/main/scala/<pkg>/X.scala` (generated). Change one, change all three, or a
gate fails. There is no partial edit.

**Every val in ANY object that extends `App` must be `lazy val` — the generated companions
AND the hand-written engine.** Scala 2 `App` uses `DelayedInit`, so eager `val` initializers are
deferred to `main()` and read as null across objects. This includes `typeDefinition`, the
kind-vals, `Null`, encoders/decoders, and any private val a lazy val references. `DracoGenerator`
itself extends `App`: a private lookup table added there as an eager `val` on 2026-09-11 failed
eleven tests on its first run. The one axiom exempt from it is `DracoType.typeDefinition`, which
does not extend `App`.

**Read the report-only numbers, not just pass/fail.** Several tests measure rather than
assert (drake surface losses, the example-domain generate map, PON discrepancies). Two
real defects in one August session were caught only by reading a headline that moved —
new corpus data quietly adding to a known tail. See GitHub #62. Until that lands, a green
suite does not mean nothing regressed.

**The baselines, originating at `87a2bb9` (2026-08-31).** The last verified full-suite baseline is
**701 tests / 50 suites**, verified by Dev's run on 2026-10-07. The 2026-10-09 authoring increment
(§3, *Domain ontology and the first authoring slice*) passed a compiled-class PROBE of the whole tree at
**713 tests / 51 suites** (the scalatest Runner reports 52, counting its discovery wrapper) with every
report-only figure below unchanged; Dev's full `sbt test` is pending and sets the next baseline. Progression: 575 at `6f5a8bb`, then five per-type tests each for `gendrake.Emit`,
`generator.EmissionReceived` (`SurfaceReceived` until 2026-09-10) and `gendrake.Emitter`, plus the two gates of `GenDrakeTest`,
the first suite that fires a generator transform as rules — which also moves the two type
COUNTS below — 93 draco types in scope, 103 measured — and none of the loss figures; then one
structural test in `DrakeParseTest` for the `++` operator (2026-09-10); then `draketarget.DomainLine`
(five per-type tests) and its own two-test suite `DomainLineTest` (2026-09-11), which moves the
type counts to 94 in scope, 104 measured; then `gendrake.DomainLineOf`, the first member of the
transform domain (2026-09-11), to 95 and 105; then one more `DrakeParseTest` test for the
call syntax's at-most-once rule (2026-09-16); then one structural `DrakeParseTest` test for the
four type forms (2026-09-17); then one for the header's type parameters (2026-09-17); then the
`draco.drake` Presence family — three types, their per-type tests and a `DracoGenTest` group test
(2026-09-17), which moves the type counts to 98 in scope, 108 measured and GenDrake to 96 of 96; then
`fold` on the family and its own two-test suite `PresenceTest` (2026-09-17); then one `DrakeParseTest`
test for `now` (2026-09-17); then a `PresenceTest` test that a Present never evaluates `fold`'s default
(2026-09-18); then one that a factory argument is evaluated on first read and never if unused
(2026-09-18); then one that `ifThenElse` never evaluates the branch not taken (2026-09-20); then one `DrakeParseTest` test for the operator layer and the lambda
(2026-09-21); then one for the parenthesized sub-expression and the minimal pair (2026-09-21); then one for `bracket`
(2026-09-22) — **627 tests / 44 suites**. The Text increment adds five corpus tests and
four tests in `draco.drake.TextTest`: **636 tests / 45 suites passed** in Dev's full
sbt run; all report-only figures matched the then-current baselines. A direct compiled-class probe also passed 506 tests / 5 suites (TextTest, DracoGenTest,
DrakeGenTest, DrakeParseTest, GenDrakeTest); it measured 99 draco types in scope, 109 measured,
one surface loss, and GenDrake 97 of 97. These
are the headlines those tests print. They go to the console
logger, not to the per-suite files, so they have to be caught off stdout — every row below
except the last `DrakeGenTest` one, which prints only to its per-suite file:

```bash
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

| test | headline | baseline |
|---|---|---|
| `ExampleDomainsGenTest` | example-domain gen map | 28 match, 20 differ, 0 error, 0 missing (of 48) |
| `DrakeParseTest` | drake surface losses | 1 field across 113 types — type form 1 (ActorAspect), expression form 0 |
| `DrakeParseTest` | Drake.parse scope | 103 draco + 10 mods in, 0 held back |
| `DrakeGenTest` | mods actors pending `.drake` | 0 — **file-only**, in `target/test-output/DrakeGenTest.log` |
| `PonCorpusTest` | PON corpus | 80 numbers, 550 expressions, 42 discrepancies |
| `PonCorpusTest` | canonical check | 80 numbers, 7 differ from generated canonical |
| `ScenarioDrakeTest` | scenario in today's drake | 23 files, 0 rejected, 0 drifted, 23 clean |
| `SubtypeFactVisibilityTest` | rete subtype visibility | CO-DECLARATION DROPS THE FACT (categorical, not a count) |

A number here disagreeing with a run means one of two things, and both are worth stopping
for: the run regressed, or this table went stale. **The commit that legitimately moves a
number updates this table in the same commit** — the same discipline the CHANGELOG entry
and the git-record already follow, and for the same reason: a figure nobody is obliged to
maintain is one nobody can trust. The alpha.6 notes still quote 16 surface-loss fields;
that was true when written and became 15 at `324556c`, which is exactly the drift this
table exists to make visible.

**Verified domain-aspect increment (2026-09-22):** two new definition-backed types,
`draketarget.DomainAspectText` and `gendrake.DomainAspectOf`, add ten corpus tests;
the new six-test `DomainAspectTest` brings the full count to **652 tests / 46 suites passed**.
GenDrake emitted 99/99 definitions. The new section test reproduced all 101 complete
domain sections, including the parameterized domain omitted by the retained DomainLine
compatibility test, with zero exclusions. All report-only figures matched the then-current table.

**Verified Draco-aspect increment (2026-09-28):** two new types add ten corpus tests
and DracoAspectTest adds seven focused tests. Dev's full run passed **669 tests / 47
suites**, with no failures or aborted suites and all report-only baselines matching.
An isolated compiled-class probe had passed **538
tests / 7 suites** (DracoAspectTest, DomainAspectTest, DracoGenTest, DrakeGenTest,
DrakeParseTest, GenDrakeTest, SourceContractTest). GenDrake emitted 101/101 definitions;
both aspect probes covered 103 sections with none skipped. The Draco probe uses frozen
pre-migration sections for ActorAspect and BodyElement, whose authored sources remain
ahead of JSON. Both Drake and Scala projections were byte-identical before/after over
all 103 current JSON definitions. The full run also confirmed the table above and zero
mods actors pending Drake authoring in the file-only report.

Two of these have since become ASSERTIONS and are deliberately not listed: the scenario
now projects, wires and runs under `ScenarioGenTest`, which fails rather than reports. So
does `GenDrakeTest`'s `GenDrake runs` line — in the capture grep for visibility, not here.
Moving a measurement into an assertion is the goal; the table should shrink over time.

**Where things are recorded** — distinct artifacts and jobs, do not conflate them:

| artifact | holds | who writes it |
|---|---|---|
| auto-memory (`MEMORY.md` + notes) | tool-local retrieval aids for conventions and feedback; shared decisions must also be in project documents | the active agent, as work happens |
| GitHub Issues | work to do; decisions deferred | only with Dev's approval |
| `draco-git-record/` | audit trail — one file per commit, containing the commit message | this session, before the commit |
| `CHANGELOG.md` | the *fact* of each change, for a reader outside the tree | this session, **in the same commit as the record** |
| `draco-dev-journal/` | sourced historical narrative, preserving dialogue and corrections | Claude Code or Codex; Cowork may also contribute when Dev assigns it |
| `DEVELOPMENT_HANDOFF.md` | current checkpoint, evidence, open decisions and documentation coverage for the next agent | the outgoing agent; the incoming agent verifies it against the tree |

**Journal authorization (Dev, 2026-09-23).** Claude Code and Codex both have explicit
permission to maintain the journal, DRACO.md, README.md and project-embedded continuity
records during development and handoffs. This supersedes the Cowork-only restriction;
no separate Cowork handoff is required. Preserve the journal's source and format rules,
and distinguish verified facts, Dev's decisions and model proposals. This permission
does not authorize sbt, commits, pushes, new GitHub issues or unrelated changes.

**The CHANGELOG entry is written with the git-record, not at release time.** Both go in
the commit they describe. The record carries the reasoning; the CHANGELOG carries one or
two sentences of observable fact under `[Unreleased]`. Attaching it to a step that already
happens is the point — this file fell two and a half months behind when it depended on
remembering.

**Git-record filenames use `git-record-YYYY-MM-DD-HHMM` in local project time.**
Use the record heading to describe its purpose. Dev restored this convention on
2026-10-05; committed descriptive-suffix records remain historical exceptions.

**Commit messages go through a file, never a heredoc.** A long `git commit -F - <<'EOF'`
breaks on paste and the remainder runs as shell commands. Write the message to a file and
use `git commit -F <file>`. When a commit is meant to be path-scoped, put the pathspec on
the *commit* (`git commit -F msg -- <paths>`) — the IDE auto-adds new files, so a scoped
`git add` does not scope the commit.

**Model-authored prose is not Dev's intent.** Issues, memory notes, and review documents
in this repo are largely model-authored and Dev-tolerated. Cite them as prior reasoning to
re-examine, never as Dev's authority or specification.

---

## 2. The gates, and what a failure means

Three suites pin the corpus. Knowing which one failed tells you what is actually wrong.

| gate | pins | a failure means |
|---|---|---|
| `DracoGenTest` | `DracoGenerator.generate(X.json)` ≡ hand-written `X.scala`, whitespace-normalized, for every definition | the JSON, the Generator, or the Scala moved without the others |
| `DrakeGenTest` | `Drake.emit(X.json)` ≡ hand-written `X.drake` | the emitter or the surface moved |
| `DrakeParseTest` | `emit(parse(source))` ≡ source, and `parse(emit(td))` ≡ td | the parser and emitter disagree, or a value form is not carried |

`DracoGenTest` compares **text and never compiles**. It is nonetheless a compile check in
effect, transitively: generated output is pinned to the hand-written files, and sbt
compiles those. Do not "fix" this by adding compilation — the guarantee holds. The real
gap is `ExampleDomainsGenTest`, which reports 28 match / 20 differ over the example domains
and compiles none of it.

`comparisonOnlyExcluded` is `Map.empty`: no hand-written customisation remains under
`src/main/scala/draco/`. Keep it that way. If generated output is wrong, fix the JSON or
`DracoGenerator`, not the Scala.

---

## 3. Orientation — what is actually true

Compact by intent. Architecture detail belongs in `README.md` once that file is corrected
(§5); this section exists so a session is not misled in the meantime.

**Type system.** `DracoType` is a one-member trait (`val typeDefinition`).
`TypeDefinition extends Aspects { val typeName }`, and `Aspects` is **five** slots:

- `dracoAspect` — superDomain, modules, extensible, derivation, elements, factory, globalElements
  (`derivation` is `[Json]` since 2026-09-20: a TypeName object for a draco parent, a type-form node
  for a FOREIGN one — Dictionary's `{"{}": ["K", "V"]}`; read names through `DracoAspect.parents`)
- `domainAspect` — typeName (self-loop for a domain, container pointer otherwise), elementTypeNames,
  source, target (both present = a **transform domain**; role is presence, as everywhere else)
- `ruleAspect` — pattern, action
- `actorAspect` — messageType, start, message, signal
- `codecAspect` — discriminator

**Role is presence, not name.** A type is a rule because it carries a `ruleAspect`, an
actor because it carries an `actorAspect`. Generated objects take the bare authored name —
no `Rule` or `Actor` suffix, and no `.rule`/`.actor` filename suffix.

**`TypeElement`** is a sealed family of **twelve** kinds — Fixed, Mutable, Dynamic, Local,
Parameter, Monadic, Condition, Action, Pattern, Variable, Factory, Case — all extending
`Primal[Json]`. So `value` is a JSON node: either a host-opaque source string or a
single-key `{op: [operands]}` expression tree. **`valueType` is a JSON node on the same terms
since 2026-09-16** — a string is the type's authored text (all of the corpus today), an object
is a type-form tree (the parser trees the four forms since 2026-09-17); every consumer reads it through
`TypeForm.text` (`e.valueType.text`), and the engine spells it for the target once, in
`targetTypes`. Every element also carries `now: Boolean` (default false, elided), the strictness
override of drake.dlt EVALUATION. JSON uses a `"kind"` discriminator;
`Codec.sub` narrows the parent codec.

**`TypeName`** is `name` + `namePackage` + `typeParameters`, with derived `namePath` and
`resourcePath`. It compares **structurally** (GitHub #37). Each type parameter is a JSON node on the
value-type terms since 2026-09-17 — a string is the authored text, a tree a type form, and the parser
builds the tree for every header parameter and reference argument (`S <: DomainType` is a bound leaf,
`(S, T)` an Objective) — read as text through
`TypeForm.text` and spelled for Scala at each render site, because identity is not rewritten in place. Type parameters are part of the
identity: a position holds either a variable or a concrete type, a type is abstract iff any
position contains a variable at any depth, and only a fully concrete name can be the
derivation for an atomic term. So `Dictionary(K, V)` ≠ `Dictionary(TypeName, TypeDefinition)`.

**Companion convention.** Every generated companion is:

```text
object X extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName("X", _namePackage = Seq(...)))
  lazy val dracoType: Type[X] = Type[X](typeDefinition)
  lazy val domainType: Domain[Container] = Domain[Container](typeDefinition)
  // plus derived codec, apply, Null; ruleType / actorType where the aspect is present
}
```

The kind-val is named for the kind — `dracoType`, `domainType`, `ruleType`, `actorType`.
There is no `typeInstance` and no `*Instance` trait.

**Loading.** `TypeLoader` owns it, not `DracoGenerator`:

```text
TypeLoader.loadType → tryLoad → draco.generator.carrier.DefinitionPath.default.source(resourcePath) → readDefinition
```

The loader path speaks `Presence`, not the host's option, since 2026-09-18: `source`, `readDefinition`,
`loadFromResource` and `tryLoad` declare `draco.drake.Presence(T)`, convert once at the host boundary with
the `presence` symbol, and chain with `fold`; `Dictionary.get` keeps `Option` because it overrides `Map.get`.
`rooted` appends the universal root to any definition carrying no NAMED parent — an absent
derivation is the common case, a solely foreign one (`Dictionary`, whose parent is a type form) the
other — so no definition in the corpus spells `DracoType`. An EMPTY package is not "foreign": it is
the nameless domain, draco's default package, whose whole identity is the empty path (Dev, 2026-09-20).
It has no companion, no dictionary entry and no name; `src/main/resources/.drake` (the one line `domain`)
and `.json` are its anchor for the parser and emitter, and nothing permanent may refer to it.

`DefinitionPath` — in `draco.generator.carrier`, the generator's source side, where host
realization is the content rather than a leak — holds `roots: Seq[URI]` explicitly and
resolves unique-or-error: more than one root carrying a name is a hard error, because order
cannot survive projection to another target language. `hostRoots` derives the default from `java.class.path`; it is one
realization, not the definition, and a path can be constructed without a classloader. A
missing definition yields a typeName-only stub, which is legitimate.

**`DracoGenerator` dispatch** (`src/mods/scala/draco/DracoGenerator.scala`, the hand-written
engine — renamed from `Generator` on 2026-09-08 so the definition-backed `draco.Generator(T)`
can own the name) normalizes at the entry
with `TypeLoader.rooted` (absent derivation means derives-`DracoType`) and `targetTypes`
(every value type takes the target's spelling here — a type-form tree through `targetType`, a
neutral `{K,V}` string through the `Map[K,V]` rewrite — the *only* place a Scala type spelling
is produced; a TypeName's parameters are spelled at each render site instead, because identity
is not rewritten in place). It then dispatches six ways: two-or-more role aspects → composed;
rule; actor; domain; object-only; plain type.

**Evaluation is lazy by default, `now` overrides** (drake.dlt EVALUATION, 2026-09-17): a `fix` or `loc`
inside a method, action or factory body renders `lazy val` (step 1); a method parameter is by-name,
`_x: => T`, bound once by a `lazy val x = _x` prelude (step 2, 2026-09-18); a factory parameter is by-name
too, and the factory body is its prelude — a bodiless factory binds `override lazy val x = _x`, a bodied
one reads `_x` inside its lazy members (step 3, 2026-09-18); the actor-minting `factory ActorType`
follows the same convention, `def actorType(_consumer: => M => Unit)` with `lazy val consumer =
_consumer` as the minted actor's first member (step 4, 2026-09-18; the parameter was the host's `ActorRef[M]`
until the consumer form landed the same day — see drake.dlt VALUE-TYPES). `now` before a member keeps it strict and is a reserved word; it is
required where the target binds the signature — a host method met (TypeName's `equals`, Dictionary's
`Map` operations, CLI's `main`) or a method used as a function value (TypeLoader's two).

**DRAKE** is draco's own definition surface — `X.drake` beside every `X.json`.
`Drake.emit` and `Drake.parse` are mutual inverses in `src/mods/scala/draco/Drake.scala`;
`DrakeCLI` offers `emit | parse | check`. Whitespace is insignificant. Three bracket rules:
name lists always bracketed, keyword blocks never, openers bracket themselves. References
are bare in the referring type's own package, qualified elsewhere, and written as a type
expression when the referent is outside every draco domain (`Dictionary` derives `{K, V}`).
Value types are
`[T]` Seq, `{T}` Set, `{K,V}` Map, their mutable versions `[T]+`, `{T}+`, `{K,V}+` (2026-09-20; `mut {T}` retired),
`F(A,B)`, `A -> B`, tuples. The full spec is
`src/main/resources/draco/drake.dlt`, which is current and authoritative.

**Caveat, and it matters if you author drake:** `Drake.parse` trees every VALUE TYPE as one of
the four type forms (2026-09-17; no host-text type form remains since `[T]+` landed on 2026-09-20), and
builds expression trees for calls — `f(a, b)`, positional then `name:value`, one glued token split
at depth-0 parentheses, commas and dots (2026-09-16; the `parameters`/`par` call form, its `[ ]`
argument brackets and `.member` chain lines are RETIRED) — tuples, collection literals `[x, y]` /
`{a, b}` (2026-09-20), the INFIX OPERATOR LAYER — `* / %`, `+ -`, `++`, the six comparisons, `&&`,
`||` and `->`, read flat and reassociated by Haskell's Prelude fixities with the arrow loosest of all
(2026-09-21; `++` alone since 2026-09-10) — and a LAMBDA `\p1 p2 -> body`, the `\` node, whose body
takes everything right of its arrow (2026-09-21). A run trees only when every operand is ONE token
or the lambda; a run with a several-token operand — a host `if`, a `new`, a block — stays whole as a
host-opaque string in *drake* form, which `DracoGenerator.expression` would pass verbatim into
Scala, so the parser never hands the engine a tree with the wrong scope. A parenthesized group
holding an expression the parser reads DISSOLVES into its tree, and each renderer writes back the
minimal pair by its own target's fixity (2026-09-21) — so an authored redundant pair does not survive
re-canonicalization. `=>` is never a drake token: it is the Scala target's spelling of the lambda node. The JSON corpus is re-canonicalized from the drake after each parser step (`DrakeCLI parse
X.drake > X.json` with the compiled parser; 2026-09-16 for calls, 2026-09-17 for type forms, 2026-09-21 for operators and lambdas), with
two deliberate exceptions: BodyElement and ActorAspect (authored-ahead aspects); format/json/Value
came off the list on 2026-09-21 when its conditionals became `guard(c).ifThenElse(t, e)`. Parse is a measurement tool, not
yet an authoring path (GitHub #61).

**Tiers.** `src/main/scala` is definition-backed. `src/mods/scala` compiles into the same
package tree and holds the hand-written engine: `DracoGenerator`, `GeneratorCLI`, `Drake`,
`DrakeCLI`, `Expression`, `DomainBuilder`, `Assembly*`, `SourceContract`. mods → main is
allowed; main → mods is not. Whether mods is now *the* engine tier rather than a
speculative layer is an open question for Dev.

**Domain-aspect generation (2026-09-22).** `draketarget.DomainAspectText` owns complete
domain-section layout; `gendrake.DomainAspectOf` maps from TypeDefinition, reading the
super-domain from DracoAspect and the other fields from DomainAspect. `Drake.emit`
uses this generated transform, which reaches the running Emit/EmissionReceived flow.
The transform receives type-parameter spelling as an explicit callback, supplied by
the existing Drake type-form renderer; it never calls back into the full emitter.
`DomainLine` and `DomainLineOf` remain compatibility helpers, not full aspect renderers.

**Draco-aspect generation (2026-09-28).** `draketarget.DracoAspectText` owns the type
section's layout; `gendrake.DracoAspectOf` maps the header, derivation, modules,
extensibility, elements, factory and globals. It preserves own-package reference
elision, reconstructable-root elision and implicit versus explicit factory results.
`Drake.emit` delegates to it through `dracoAspectText`. Four explicit callbacks retain
parameter/type-form/value-type-slot spelling and element-body rendering in Drake;
the generated main types do not depend on mods classes. Super-domain output remains
in the domain section and owned by DracoAspect. This is Drake output only: the
Scala generator and GenScala have not been migrated. Rule/actor rendering and codec
rejection retain their existing behavior.

**Domains.** `draco` (root), `draco.base`, `draco.primes`, `draco.format` (+ `json`,
`xml`), `draco.rete`, `draco.drake` (the runtime: `Presence(T)` / `Present` / `Absent` with `fold` and `ifThenElse` as dispatch, 2026-09-17 and 2026-09-20; a Boolean reaches `ifThenElse` through the `guard` symbol), `draco.draketarget`, `draco.generator` (+ `carrier`),
`draco.genscala`, `draco.gendrake`, `draco.scalatarget`. Domains are
peers in the `DomainOntology`, not hierarchical. **The cross-domain structure is the DOMAIN
ONTOLOGY since 2026-10-09** (Dev: an operational ontology built from domains and their intra- and
inter-relationships; `DomainDictionary` was the expedient name); "dictionary" now means only a
domain's own `TypeDictionary`. Say ontology for the whole, dictionary for a domain's members. Example domains live in
`src/mods/scala/domains/` (the World / media chain).

**Service staging (2026-09-30).** `draco.service.Service` is a self-declaring peer
domain under `src/mods/{resources,scala}/draco/service`. Its initial trio was generated with
no members. It is the intended neutral home for service capabilities and
configuration, ahead of target-language ZeroMQ wrappers, not a running service.
Do not add it to Draco's member list: use explicit DomainOntology composition.
The main-corpus gates do not scan staged draco definitions; ServiceTest provides
five explicit projection, loading, validation and dictionary/generation checks.
The focused ServiceTest + DomainBuilderTest probe passed 15 tests / 2 suites;
Dev's full suite passed 674 tests / 48 suites on September 30. Existing corpus report
counts stay unchanged because this definition is outside those scans.

**Service plan (Dev decisions, recorded 2026-10-05; not implemented).**
ServiceConfiguration contains Assembly. The primordial service supports external
creation/modification of domain/type definitions and domain-ontology composition;
the first increment is authoring, validation and composition. Generation and activation
are later explicit operations. An endogenous initialization client uses existing
actors/messages and canonical JSON definitions, independently of the ZeroMQ track.
The consuming project owns persistence. Parsing remains rule-owned and dispatch
uses the active dictionary. See src/mods/resources/draco/service/README.md for the
shared plan and src/mods/ZEROMQ_CODEC_CONTRACT.md for the next integration proposal.
The empty Service anchor is committed in 604aff2; proposed envelope/socket/codec
choices are not settled. Journal coverage reaches chapter 92; the first authoring slice landed
2026-10-09 (below).

**ZeroMQ bridge staging (2026-10-05).** Service now contains TextOutput, the neutral
Text -> Boolean send capability (queue acceptance only). Root pins JeroMQ 0.6.0.
The handwritten mods bridge uses existing Draco actors/rules, bounded queues and
one socket-owning thread. JSON parsing, exact ontology resolution and output wrapping
remain rule actions. The executable loopback PAIR/single-frame fixture forwards the
payload to a JSON-input actor and returns typed-output JSON to the same peer; those
protocol choices are provisional. No authoring, initialization client, new codec syntax
or persistence is implemented. Direct compiled-class probe: 548 tests / 7 suites,
existing measured baselines unchanged. Dev's full run passed 685 tests / 49 suites
on October 5, with zero failures/aborts and unchanged report-only baselines, including
zero pending mods actors in the file-only Drake report. See the contract for limitations.

**Dreams name skeleton (2026-10-07).** draco.dreams.Dreams,
draco.dreams.editor.Editor and draco.dreams.user.User are empty, self-declaring
peer domains with trios under src/mods/{resources,scala}/draco/dreams. The old main
Dreams trait is replaced by generated staging source under the same fully qualified
name. Package nesting implies neither domain membership nor super-domain inheritance.
The legacy draco.dreams.Service and Orion scaffolds remain separate, unregistered
Scala traits; they are not new domain definitions. DreamsTest supplies 16 explicit
gates. The direct probe passed 555 tests / 7 suites; Dev's October 7 full run passed
701 tests / 50 suites with zero failures/aborts and unchanged report-only baselines.
See the Dreams README for scope.

**Domain ontology and the first authoring slice (2026-10-09; probe-verified, Dev's run pending).**
`DomainDictionary` is renamed `DomainOntology` — the DOMAIN ONTOLOGY, Dev's operational ontology built
from domains and their intra- and inter-relationships; today it still holds only the map and gains
`defines(typeName)`, true when any domain key or any member matches by name and package (parameters
ignored: a definition is keyed bare). `TypeDictionary` and `Domain` factories take an optional
`members [TypeDefinition]` and populate from it, name-matched within the domain's package, placeholders
for the rest — the core capability `DomainBuilder.define` used to supply from resources (it now loads and
delegates; `DomainBuilder.dictionary` is `ontology`). `DerivationResolvable` joins `var o DomainOntology`
and flags any NAMED parent `o.defines` does not know — no package filter (a foreign parent is a type form,
never a name) and no classpath lookup, so the validation sessions insert the ontology as a fact.
`SelfDeclaration` compares the whole TypeName. `draco.service.ServiceConfiguration` contains an
`Assembly`. `draco.dreams.editor` gains `Latent` (one domain in candidate form: `definition` +
`members`), `Actual` (the same + `problems`; `accepted` iff none; `domain` is a DYN deriving the
populated DomainType, because a `DomainType` factory parameter would emit an uncompilable codec) and
the `Editor` actor (`actorType(ontology, actual: Actual -> Unit)`, message `Latent`): per message it
composes the candidate through `Domain(definition, members)`, the ontology that WOULD result, and fires
the four core rules in a fresh stateful session over the candidate's populated members. It never
consults the classpath and never mutates the ontology given. Latent and Actual operate on DOMAINS;
composing accepted Actuals and their relationships into the ontology is the NEXT increment, as are the
endogenous initialization client, a membership-reciprocity rule (a supplied member the domain does not
name is silently dropped today), duplicate-identity rejection, generation and activation. EditorTest
(13) and two ServiceTest tests are new; DreamsTest keeps the two empty anchors.

**Retired — do not reintroduce, and treat any doc mentioning these as stale:**
the `parameters`/`par` CALL form (`f parameters par a`), `par = name` named arguments, `.member
parameters` chain lines and `[ ]` argument brackets (a call is `f(a, b)` since 2026-09-16), the
dyn `=` result marker, the Haskell-form `if … then … else` surface as a plan (`ifThenElse` is a
dyn on `Presence`, not a keyword), `eager` (the strictness word is `now`),
the `Actor(T)` derivation on an actor (a ROLE's parameter has no business being an edge in the
data inheritance tree — actor-ness is aspect presence, and the message type lives in
`actorAspect.messageType`), `TypeInstance`, `DomainInstance`, `RuleInstance`, `ActorInstance`, `typeInstance`,
`Extensible`, `DomainDefinition`/`RuleDefinition`/`ActorDefinition`, `TypeDefinition.load`,
`loadRuleType`/`loadActorType`, the `.rule`/`.actor` filename suffixes, YAML and the
`from-yaml`/`to-yaml` CLI subcommands, `DomainDictionary` (renamed `DomainOntology` on 2026-10-09; the word dictionary now names only a
domain's `TypeDictionary`), the `draco.language` domain, the reference-frame
`*centric` domains, `Alpha`/`Bravo`/`Charlie`/`Delta`, `PrimeOrdinal`, named
`Cartesian`/`Polar`/`Spherical` coordinates.

---

## 4. Build and test

```bash
sbt test                                            # full suite — the gate before any push
sbt "testOnly draco.DrakeParseTest"                 # one class
sbt "testOnly draco.DracoGenTest -- -z \"TypeName\""  # one test
```

Recommend the **full suite** before a push, not a scoped run — scoped-green is not
suite-green. Report the gate scope with every count.

`bin/draco-gen` (generate/compile/inspect/discover/verify) and `bin/draco-sc` are built by
`sbt assembly` and can be stale relative to the tree.

**Regenerating without sbt.** The compiled classes of the last `sbt test` run the CLIs directly
and byte-exactly: `java -cp "$(cat target/streams/runtime/dependencyClasspath/_global/streams/export)"
draco.DrakeCLI parse X.drake` (or `draco.GeneratorCLI generate X.json`, `generate-multi …`). Two
rules: overlay the CURRENT resources on a copy of `target/scala-2.13/classes` first — main AND
`src/test/resources` — because an actor's projection walks its domain chain through the loader,
and a stale or missing definition yields a stub, an actor with rules missing; and rebuild that
overlay after every run that recompiles the engine, or a regeneration silently changes nothing.
An engine change cannot be regenerated by the compiled engine: those commits take two runs (the
first fails the gate, the regeneration follows, the second is green).

---

## 5. Documentation status

Current documentation was reconciled through journal chapter 92 on 2026-10-07.
Dates below distinguish that reconciliation from earlier rewrites; always check
claims against the code rather than treating a historical audit as permanent validation:

- **`README.md`** — **rewritten and verified 2026-08-15, synced through journal chapter 92 on
  2026-10-07.** The canonical architecture doc, written in draco's own vocabulary rather than any
  target's, with a *Language-specific residues* table recording every place a host term still leaks.
- **`GETTING_STARTED_TARGET_*.md`** — **rewritten 2026-08-17**, one guide per target:
  `SCALA` (realized), `HASKELL` and `TYPESCRIPT` (stubs holding structure and open
  questions). One shared skeleton; only the toolchain, projection command, running, and
  command set are target-specific.
- **`AGENTS.md` / `CLAUDE.md`** — tracked symlinks to DRACO.md, verified 2026-09-23.
  Both agents read the same operating rules; edit DRACO.md, never the symlinks.
- **`DEVELOPMENT_HANDOFF.md`** — shared current-state handoff and documentation coverage;
  refresh it at a handoff or completed development increment, and verify it on resumption.
- **`CHANGELOG.md`** — **current.** Release blocks through alpha.6 (2026-08-17); `[Unreleased]`
  carries one entry per commit since, written with each commit's record, and was consolidated
  into one section each (Added / Changed / Fixed / Build) with a lead paragraph on 2026-09-09.
- **`drake.dlt`** — current and authoritative for the surface.
- **`HOLARCHY.md` / `ORION.md`** — aspirational; vocabulary largely absent from code.

**Related documentation.** `README.md` (architecture, for a reader outside the tree),
`CHANGELOG.md` (the fact of each change), `draco-dev-journal/` (the session transcripts; the
`## Status` section of each chapter is its historical closing state), `DEVELOPMENT_HANDOFF.md`
(current resumption checkpoint), `src/main/resources/draco/drake.dlt`
(the surface specification).

**Local setup.** `CLAUDE.md` and `AGENTS.md` are tracked symlinks to this file;
`.claude` is an untracked local symlink to `.draco`. Inspect before any repair. With Dev's
approval, create only missing links using `ln -s DRACO.md CLAUDE.md`,
`ln -s DRACO.md AGENTS.md` or `ln -s .draco .claude` as appropriate; do not force-replace
an existing file, directory or different link. Never write through a symlink: many tools
replace it with a regular file. Tool-specific material (Claude Code
auto-memory, `.claude`/`.draco` settings) lives outside this file.

---

## 6. Gotchas worth carrying

- **Expression vocabulary has more than two consumers.** Alongside Drake and DracoGenerator,
  the SourceContract prototype and its tests carry operator cases. Retiring the `if` node
  passed scoped probes but failed two SourceContractTest cases in the first full run
  (chapter 81). Search every consumer and retain the full-suite gate.
- **Evrete** compiles conditions as Java at runtime: fully qualified class names required.
  Working memory is boxed — rule variables use `classOf[Integer]`, not `classOf[Int]`.
  A single-fact insert needs `Seq(fact): _*`. Tuple facts need a `forEach` declaration.
- **Two rules in one knowledge must not declare fact types related by inheritance.** Evrete
  resolves a fact's type by walking its supertypes; when more than one declared type fits, it
  resolves NEITHER and **skips the insert** — the fact never reaches working memory, both rules
  stay quiet, and the only trace is a `java.util.logging` warning ("due to ambiguity" / "insert
  operation skipped"). Nothing throws. A single rule declared anywhere up the chain works fine —
  `SubtypeFactVisibilityTest` measures both halves. The corpus is safe only by shape: each medium's
  session declares SIBLINGS (`PositionReport` + `FlightIntent`), and draco's validation session a
  disjoint taxonomy (`Problem` / `TypeDefinition` / `DomainType`). GitHub #63.
- **Actors** are thin membranes: `session.insert(msg); session.fire(); Behaviors.same`.
  `Rule.knowledgeService` is a singleton; Knowledge is per domain, Session per actor.
- **circe 0.14.1 has no `java.net.URI` codec** — the codec gate excludes parameters whose
  type has no derivable instance, sourced from `externalTypeImports`.
- **Values must be single-line.** No value in the corpus contains a newline, and drake's
  surface is line-based; a multi-line value breaks the round-trip.
- **macOS filesystem is case-insensitive** — same-package names differing only in case
  collide, and resources clobber silently.
- Use ` ```text ` not ` ```scala ` for pseudo-Scala; IntelliJ injects a parser into the latter.
- **`now` is required where the target binds a signature.** A `dyn` that meets a host method
  (`TypeName.equals`, `Dictionary`'s `Map` operations, `CLI.main`) or that is used as a function
  value (`TypeLoader.readDefinition`, `rooted`) cannot take by-name parameters in Scala; mark
  the parameter `now par …`. Find these by what a type DERIVES (a foreign parent, an explicit
  `main`) and by method-as-argument uses, not by method names.
- **Evrete's `execute` takes Java's `Consumer`; a Scala function value is refused there, a lambda literal
  converts.** A rule's `pattern` and `action` are functions (`(Knowledge -> Unit)`, `(RhsContext -> Unit)`,
  2026-09-20), applied as `pattern(knowledge)`; the engine's rule template writes `execute (action(_))` at
  the one call Evrete owns. Never hand a function value to a Java functional-interface parameter.
- **A factory argument is by-name, so a hand-written caller sequences its own effects.** Since lazy
  step 3 (2026-09-18) `X(expr)` evaluates `expr` on first read of the instance, not at the call. An
  argument that consumes state — a token cursor, an iterator, a `var` mutated afterwards — must be bound
  to a `val` before the call. `Drake.parse` had six such sites and failed 133 tests on the first run.
- **When a slot's type changes, grep its named-argument construction as well as its reads.** Changing
  `DracoAspect.derivation` from `[TypeName]` to `[Json]` (2026-09-21), every `.derivation` reader was
  found and moved; every `_derivation = Seq(TypeName(...))` construction was not — `Generated.scala`
  (hand-written, no definition) and twenty-one test sites stopped the first run at compile.
- **`loc` inside an actor action is not a binding.** `actorActionBody` renders `fix` as `lazy val` and
  `mut` as `var`; a `loc` falls through to the bare-expression case and its name is simply lost —
  the generated `receive` then references an undefined name. Use `fix` in `start`/`message`/`signal`
  (the first Editor draft, 2026-10-09, rendered four dangling expressions this way).
- **Codec eligibility is not transitive.** `hasCodec` refuses only function-like and host-external
  parameter types; a factory parameter whose type is a DRACO type without a codec (`DomainType`, which
  holds a `TypeDictionary`) emits a codec that cannot compile. Model a result with codec-able
  definitions and derive the rest as a `dyn` — `Actual` carries definitions and problems and derives
  its populated `domain` (2026-10-09).
- **sbt does not copy hidden resources.** `src/main/resources/.json` and `.drake`, the nameless
  domain's anchor, never reach `target/classes` (HiddenFileFilter), so `loadType(TypeName.Null)` is a
  stub under sbt and `MinimalTypeTest` relies on that. A compiled-class probe that overlays resources
  with `cp -R src/main/resources/.` copies them and fails that one test; delete the dotfiles from the
  overlay.
- **A re-canonicalization sweep de-trees what the parser cannot tree.** `DrakeCLI parse` writes
  back what it reads; an infix condition authored as a tree (`{"==": [{"*": ["i1","i2"]}, "i3"]}`)
  came back as the string `"i1 * i2 == i3"` until the parser treed infix operators (2026-09-21).
  Three conditions (`PrimesFromNaturalSequence`, `RemoveCompositeNumbers`, `SelfDeclaration`)
  lost their trees this way on 2026-09-16; the generated Scala is identical, so no gate saw it.
  The operator layer restored all three on 2026-09-21. What the parser still cannot tree — a host
  `if`, a block — a sweep still de-trees: read the diff of every sweep.

## 7. Claude Code / Codex handoffs

**Shared documentation skill.** When Dev requests journal/documentation maintenance,
read [.draco/skills/update-draco-docs/SKILL.md](.draco/skills/update-draco-docs/SKILL.md).
Both agents use this single version-controlled procedure and its link checker, even
when their tool does not automatically discover that directory. Do not create a
second agent-specific copy or require a .claude link to read it. DRACO.md takes
precedence in a conflict. Only the skill file and checker are allowlisted under
.draco; local settings, worktrees and other material remain ignored.

This protocol works in both directions and is independent of the selected model.
Changing the agent or model does not change project permissions or accepted decisions.
The repository is the shared continuity record; private chat histories and tool-local
memory are optional evidence, not prerequisites or hidden sources of authority.
Since 2026-09-27 Dev alternates development sessions between Claude Code and Codex as a
standing practice; the cadence is set by experience, so every session begins by receiving
a handoff and every completed increment ends by preparing one.

### Receiving a handoff

1. Read these rules and `DEVELOPMENT_HANDOFF.md`, then inspect the actual branch, HEAD,
   working-tree changes and relevant files. Do not reset, overwrite or assume ownership
   of another agent's uncommitted work. A clean tree and matching commit are observations,
   not permanent properties of a handoff.
2. Read the latest journal Status and only the chapters, git records or transcripts
   needed for the current task. Earlier sessions already covered by the journal need
   not be re-imported. If evidence is unavailable, record the gap rather than invent it.
3. Distinguish implemented, generated, compiled, tested, committed and pushed states.
   Record who ran tests, their code checkpoint, scope and report-only numbers. A result
   from before a relevant edit does not verify that edit; local remote-tracking state
   is not fresh proof of a remote push.
4. Summarize the recovered checkpoint and any discrepancies. Continue only the work
   Dev has authorized; proposed next steps and deferred decisions are not approvals.

### Preparing a handoff

1. Update `DEVELOPMENT_HANDOFF.md` with the agent/date, branch and observed HEAD, changes
   still in the tree, completed work, verification evidence, blockers, decisions and the
   next authorized action. Identify a destination if Dev has chosen one; otherwise leave
   the checkpoint available to either agent. Writing it does not launch another session.
2. Keep DRACO.md, README.md, CHANGELOG.md and the git record aligned with changes actually
   made. Shared decisions and durable lessons belong in these project files, not only in
   auto-memory. Do not overwrite another tool's private memory as a substitute for handoff.
3. Track journal coverage separately from code/documentation currency. Reconcile the last
   chapter's closing commit with git history, including changes from either agent. Name
   uncovered ranges and available sources. Do not silently skip older uncovered work to
   reach a newer Codex or Claude Code increment.
4. When writing journal chapters, follow the Introduction's Journal Conventions: source
   Dev's words verbatim and model replies near-verbatim, label Claude and Codex distinctly,
   preserve corrections, and summarize delegated command results with their provenance.
   Git records and diffs support factual summaries, not reconstructed dialogue. Missing
   transcripts must be disclosed; voice noise and duplicate delivery are not new decisions.
5. Update the chapter index when adding chapters. Advance `draco-docs-synced-through`
   markers only after reviewing the corresponding chapters against DRACO.md and README.md.
   Keep coverage gaps explicit; never advance a marker merely because code is newer.
6. Give Dev a concise handoff summary, the remaining verification or decisions, and any
   requested commands to run. Do not commit, push, run sbt, create an issue or contact
   another agent merely because a handoff is ready.

### Transcript integrity at handoff

Compaction summaries are working context, never quotation evidence. Before journal
updates, recover original message records and preserve a bounded project-local extract
with source identity, sequence, timestamps, text hashes and explicit exclusions. Report
user/reply counts and paired exchanges separately; do not count event mirrors twice.
Distinguish missing originals, editorial omissions and the currently unanswered turn.
Report observed compactions without attributing gaps to them absent evidence. Verify
older quotations when extending an already-journaled task, but preserve historical Status.
Prefer a new development task at a verified increment boundary after the handoff is
current, not solely because compaction occurred. No task is created automatically.

<!-- draco-docs-synced-through: chapter 92 -->
