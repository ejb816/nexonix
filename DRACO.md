# DRACO.md

Operating rules for Claude Code (claude.ai/code) in this repository. `CLAUDE.md` is a
symlink to this file, so it is auto-loaded every session.

**Every factual claim below was verified against the tree on 2026-08-15.** The previous
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
`src/main/resources/<pkg>/X.json` (normative), `src/main/resources/<pkg>/X.drake`
(surface), `src/main/scala/<pkg>/X.scala` (generated). Change one, change all three, or a
gate fails. There is no partial edit.

**Every val in an `extends App` companion must be `lazy val`.** Scala 2 `App` uses
`DelayedInit`, so eager `val` initializers are deferred to `main()` and read as null
across objects. This includes `typeDefinition`, the kind-vals, `Null`, encoders/decoders,
and any private val a lazy val references. The one axiom exempt from it is
`DracoType.typeDefinition`, which does not extend `App`.

**Read the report-only numbers, not just pass/fail.** Several tests measure rather than
assert (drake surface losses, the example-domain generate map, PON discrepancies). Two
real defects in one August session were caught only by reading a headline that moved —
new corpus data quietly adding to a known tail. See GitHub #62. Until that lands, a green
suite does not mean nothing regressed.

**Where things are recorded** — four artifacts, four jobs, do not conflate them:

| artifact | holds | who writes it |
|---|---|---|
| auto-memory (`MEMORY.md` + notes) | durable knowledge: conventions, feedback, how the system works | this session, as work happens |
| GitHub Issues | work to do; decisions deferred | only with Dev's approval |
| `draco-git-record/` | audit trail — one file per commit, containing the commit message | this session, before the commit |
| `CHANGELOG.md` | the *fact* of each change, for a reader outside the tree | this session, **in the same commit as the record** |
| `draco-dev-journal/` | historical narrative | **Cowork, not this session** — do not write or suggest chapters |

**The CHANGELOG entry is written with the git-record, not at release time.** Both go in
the commit they describe. The record carries the reasoning; the CHANGELOG carries one or
two sentences of observable fact under `[Unreleased]`. Attaching it to a step that already
happens is the point — this file fell two and a half months behind when it depended on
remembering.

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
| `DracoGenTest` | `Generator.generate(X.json)` ≡ hand-written `X.scala`, whitespace-normalized, for every definition | the JSON, the Generator, or the Scala moved without the others |
| `DrakeGenTest` | `Drake.emit(X.json)` ≡ hand-written `X.drake` | the emitter or the surface moved |
| `DrakeParseTest` | `emit(parse(source))` ≡ source, and `parse(emit(td))` ≡ td | the parser and emitter disagree, or a value form is not carried |

`DracoGenTest` compares **text and never compiles**. It is nonetheless a compile check in
effect, transitively: generated output is pinned to the hand-written files, and sbt
compiles those. Do not "fix" this by adding compilation — the guarantee holds. The real
gap is `ExampleDomainsGenTest`, which reports 28 match / 20 differ over the example domains
and compiles none of it.

`comparisonOnlyExcluded` is `Map.empty`: no hand-written customisation remains under
`src/main/scala/draco/`. Keep it that way. If generated output is wrong, fix the JSON or
the Generator, not the Scala.

---

## 3. Orientation — what is actually true

Compact by intent. Architecture detail belongs in `README.md` once that file is corrected
(§5); this section exists so a session is not misled in the meantime.

**Type system.** `DracoType` is a one-member trait (`val typeDefinition`).
`TypeDefinition extends Aspects { val typeName }`, and `Aspects` is **five** slots:

- `dracoAspect` — superDomain, modules, extensible, derivation, elements, factory, globalElements, source, target
- `domainAspect` — typeName (self-loop for a domain, container pointer otherwise), elementTypeNames
- `ruleAspect` — pattern, action
- `actorAspect` — messageType, start, message, signal
- `codecAspect` — discriminator

**Role is presence, not name.** A type is a rule because it carries a `ruleAspect`, an
actor because it carries an `actorAspect`. Generated objects take the bare authored name —
no `Rule` or `Actor` suffix, and no `.rule`/`.actor` filename suffix.

**`TypeElement`** is a sealed family of **eleven** kinds — Fixed, Mutable, Dynamic, Local,
Parameter, Monadic, Condition, Action, Pattern, Variable, Factory — all extending
`Primal[Json]`. So `value` is a JSON node: either a host-opaque source string or a
single-key `{op: [operands]}` expression tree. JSON uses a `"kind"` discriminator;
`Codec.sub` narrows the parent codec.

**`TypeName`** is `name` + `namePackage` + `typeParameters`, with derived `namePath` and
`resourcePath`. It compares **structurally** (GitHub #37). Type parameters are part of the
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

**Loading.** `TypeLoader` owns it, not `Generator`:

```text
TypeLoader.loadType → tryLoad → DefinitionPath.default.source(resourcePath) → readDefinition
```

`rooted` appends the universal root to any definition carrying no draco-domain parent — an
absent derivation is the common case, a solely foreign one (`Dictionary`) the other — so no
definition in the corpus spells `DracoType`.

`DefinitionPath` holds `roots: Seq[URI]` explicitly and resolves unique-or-error — more
than one root carrying a name is a hard error, because order cannot survive projection to
another target language. `hostRoots` derives the default from `java.class.path`; it is one
realization, not the definition, and a path can be constructed without a classloader. A
missing definition yields a typeName-only stub, which is legitimate.

**Generator dispatch** (`src/mods/scala/draco/Generator.scala`) normalizes at the entry
with `TypeLoader.rooted` (absent derivation means derives-`DracoType`) and `targetTypes`
(the neutral `{K,V}` → Scala `Map[K,V]` rewrite — the *only* place the Scala spelling of a
map is produced). It then dispatches six ways: two-or-more role aspects → composed;
rule; actor; domain; object-only; plain type.

**DRAKE** is draco's own definition surface — `X.drake` beside every `X.json`.
`Drake.emit` and `Drake.parse` are mutual inverses in `src/mods/scala/draco/Drake.scala`;
`DrakeCLI` offers `emit | parse | check`. Whitespace is insignificant. Three bracket rules:
name lists always bracketed, keyword blocks never, openers bracket themselves. References
are bare in the referring type's own package, qualified elsewhere, and written as a type
expression when the referent is outside every draco domain (`Dictionary` derives `{K, V}`).
Value types are
`[T]` Seq, `{T}` Set, `{K,V}` Map, `mut {T}`, `F(A,B)`, `A -> B`, tuples. The full spec is
`src/main/resources/draco/drake.dlt`, which is current and authoritative.

**Caveat, and it matters if you author drake:** `Drake.parse` builds expression trees only
for the application surface. Every other value — lambdas, `if/then/else`, `->`, operators
— returns as a host-opaque string in *drake* form, which `Generator.expression` would pass
verbatim into Scala. Parse is a measurement tool, not yet an authoring path (GitHub #61).

**Tiers.** `src/main/scala` is definition-backed. `src/mods/scala` compiles into the same
package tree and holds the hand-written engine: `Generator`, `GeneratorCLI`, `Drake`,
`DrakeCLI`, `Expression`, `DomainBuilder`, `Assembly*`, `SourceContract`. mods → main is
allowed; main → mods is not. Whether mods is now *the* engine tier rather than a
speculative layer is an open question for Dev.

**Domains.** `draco` (root), `draco.base`, `draco.primes`, `draco.format` (+ `json`,
`xml`), `draco.rete`, `draco.drake`, `draco.generator`, `draco.scalatarget`. Domains are
peers in the `DomainDictionary`, not hierarchical. Example domains live in
`src/mods/scala/domains/` (the World / media chain).

**Retired — do not reintroduce, and treat any doc mentioning these as stale:**
the `Actor(T)` derivation on an actor (a ROLE's parameter has no business being an edge in the
data inheritance tree — actor-ness is aspect presence, and the message type lives in
`actorAspect.messageType`), `TypeInstance`, `DomainInstance`, `RuleInstance`, `ActorInstance`, `typeInstance`,
`Extensible`, `DomainDefinition`/`RuleDefinition`/`ActorDefinition`, `TypeDefinition.load`,
`loadRuleType`/`loadActorType`, the `.rule`/`.actor` filename suffixes, YAML and the
`from-yaml`/`to-yaml` CLI subcommands, the `draco.language` domain, the reference-frame
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

---

## 5. Documentation status

Only this file has been verified. As of 2026-08-15 the others are stale and should not be
trusted without checking the code:

- **`README.md`** — **rewritten and verified 2026-08-15.** The canonical architecture doc,
  written in draco's own vocabulary rather than any target's, with a
  *Language-specific residues* table recording every place a host term still leaks.
- **`GETTING_STARTED_TARGET_*.md`** — **rewritten 2026-08-17**, one guide per target:
  `SCALA` (realized), `HASKELL` and `TYPESCRIPT` (stubs holding structure and open
  questions). One shared skeleton; only the toolchain, projection command, running, and
  command set are target-specific.
- **`AGENTS.md`** — a *diverged older copy* of this file, not a symlink. Should be one.
- **`CHANGELOG.md`** — stops at alpha.5 (2026-06-03); ~25 git-records since.
- **`drake.dlt`** — current and authoritative for the surface.
- **`HOLARCHY.md` / `ORION.md`** — aspirational; vocabulary largely absent from code.

---

## 6. Gotchas worth carrying

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
