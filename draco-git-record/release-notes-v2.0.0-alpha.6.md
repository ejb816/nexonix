## v2.0.0-alpha.6 — DRAKE, and definitions that stop naming their target

The largest release so far, and the first in two and a half months. Draco acquired its own
definition language, its element values became a normative tree form rather than target
source text, type loading moved onto an explicit and language-neutral path concept, and
the framework began validating its own foundation through the rule engine it ships.

Covers dev-journal chapters 41–73 and 37 commits.

### Highlights

**DRAKE — a definition surface of draco's own**

Every definition now has a human-authoring surface beside it: `X.drake` next to `X.json`,
specified in `src/main/resources/draco/drake.dlt`. Whitespace is insignificant. Three
local bracket rules decide structure — name lists always bracketed, keyword blocks never,
and a construct that opens a sub-block brackets itself — so nothing consults layout and
nothing needs look-ahead. References are written bare when they resolve in the referring
type's own package and qualified elsewhere.

`Drake.emit` and `Drake.parse` are mutual inverses, gated in both directions over 91
definitions across two corpora. `draco.DrakeCLI` offers `emit | parse | check`. The
residual round-trip loss is *measured rather than assumed* — 16 fields, each with an
owning issue.

**Values became expression trees**

`TypeElement.value` changed from a string to a JSON node. A value is now either
host-opaque source text or a normative `{op: [operands]}` tree — `.` for paths, `()` for
application, `->` for the arrow, `\` for abstraction, `if`, `(,)` for tuples. Rule
conditions and action bodies are trees, which is what makes them readable by a target
other than Scala.

**Loading became a path, and the path is draco's**

Loading left the Generator. `TypeLoader` owns the single route from a name to a
definition, and `DefinitionPath` holds its **roots explicitly** and resolves
**unique-or-error** — a name found at more than one root is an error to report, not a
search-order accident. Order carries no meaning, because order is precisely what cannot
survive projection into a language whose module system shadows by different rules. The
default derives roots from the host environment, but a path can be constructed from given
roots with no host mechanism at all.

**The framework validates itself**

`SelfDeclaration`, `DerivationResolvable` and `Completeness` rules with a
`CollectProblems` sink, fired by the `Draco` domain acting as a definition-backed *actor*
over its own dictionary. Draco checks its own foundation using the rule engine it ships.

**Role is presence, not name**

The `.rule` and `.actor` filename suffixes are gone, and so are the `Rule` and `Actor`
suffixes on projected names. A type is a rule because it carries a rule aspect. Related:
an absent derivation now *means* derives-`DracoType`, which let 27 redundant explicit
derivations be stripped from the corpus with byte-identical output.

**The first constructor to leave the host's vocabulary**

A map is stated `{K, V}` in both the definition and the surface; the Scala spelling
`Map[K, V]` is produced by the target alone, in exactly one function. Nothing upstream of
projection names it. This is the pattern the remaining type constructors will follow, and
`README.md` now carries a *Language-specific residues* table naming the eight places a
host term still leaks and what neutral would look like in each.

### Also in this release

- `CodecAspect` as a fifth aspect; `Local` as an eleventh element kind
- `draco.format` with `json` and `xml` sub-domains
- Aspect composition — a type with two or more role aspects emits every present block
- `Assembly` and `Binding` — actor groups as pure data, with a validator and one generic
  spawner
- Capability domains: `draco.rete`, `draco.drake`, `draco.generator`, `draco.scalatarget`
- The World media example chain and its first meaning-preserving cross-domain transform
- Structural `TypeName` identity, with type parameters distinguishing abstract from
  concrete
- YAML retired as a definition language

### Documentation

All three top-level documents were rewritten this month after a review found them
describing an architecture that no longer existed. `README.md` is now written in draco's
own vocabulary rather than the Scala target's; `DRACO.md` is operating rules plus a
verified orientation; and getting-started is split per target, with Scala realized and
Haskell and TypeScript stubs holding the open questions.

### Known limitations

- **Scala is the only realized target.** Haskell and TypeScript are structure and
  questions, not projections.
- **`Drake.parse` is not yet an authoring path.** It builds expression trees only for the
  application surface; other value forms return as opaque text, so parsing is a
  measurement tool. Tracked as #61.
- **The surface CLI has no shell wrapper.** `DrakeCLI` is reachable through the assembled
  jar only.
- **The metamodel is not frozen.** `TypeName` and `derivation` are expected to change
  shape (#51) before beta.
