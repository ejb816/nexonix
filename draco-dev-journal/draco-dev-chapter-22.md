# Draco Dev Journal — Chapter 22

**Session date:** April 17–20, 2026
**Topic:** Generator Simplification, Rule Naming Bug Fix, elementTypeNames Val Visibility, Transform Domain Design, Reference Frames Increment C

---

## Session Start — Picking Up From Chapter 21

The chapter 21 handoff listed three items ready to land: Generator simplification (emit `Domain[T](typeDefinition)` instead of verbose form), `loadType` silent-fallback fix, and Increment C. The developer asked what should come next based on memory, and the recommendation was Generator simplification first, so Increment C's 12 transforms would emit in the clean shape from the start rather than being regenerated later.

> **Dev:** Let go with 1.

---

## Generator Simplification

`domainInstanceLiteral` in `Generator.scala` was emitting ~15 lines of literal `TypeDefinition(...)` construction duplicating everything already in the loaded JSON:

```scala
lazy val domainInstance: Domain[X] = Domain[X] (
    _domainDefinition = TypeDefinition (
      typeDefinition.typeName,
      _elementTypeNames = Seq (
        "Bearing", "Reach", "Percept"
      ),
      _superDomain = ...,
      _source = ...,
      _target = ...
    )
  )
```

Collapsed to:

```scala
lazy val domainInstance: Domain[X] = Domain[X] (typeDefinition)
```

All fields flow from the loaded JSON via `typeDefinition`. The 4 checked-in reference-frame Scala files (Egocentric, Geocentric, Heliocentric, Galactocentric) were updated to match — these are `ReferenceFramesGenTest`-enforced to be byte-aligned with Generator output.

The 8 hand-written domains (Draco, Base, Primes, DataModel, Alpha, Bravo, Charlie, Delta) were NOT touched. Earlier in the session the developer clarified:

> **Dev:** I've been allowing you to directly make modifications on my main branch with me in the approval loop for important changes.

Saved as `feedback_direct_main_edits.md` — edit main directly, per-tool-call click-approval as the review gate, no PRs or worktrees unless asked.

---

## Broken Rule Files Surface

Running `ReferenceFramesGenTest` triggered a full project compile that failed on pre-existing generator output:

```
trait AddNaturalSequence.ruleRule extends RuleInstance
```

Three rule files under `draco.primes` and one under `org.nexonix.rules.rete.rules` had been checked in with invalid names — the aspect-suffix migration from Chapter 21 left `.rule` in `typeName.name` but Generator wasn't stripping it before appending `Rule` suffix.

Fix: added `stripAspect(name: String)` helper and applied at five emission sites: `parameterizedName`, `wildcardTypeName`, `ruleGlobal` (for `name`), `whereConditions` FQCN, rule-branch of `generate`, and `typeGlobal` for actor path. Also fixed `ruleDefinitionLoad` to emit *base* name (not aspect-suffixed) so `loadRuleType` resolves resources correctly — `loadRuleType(TypeName("AddNaturalSequence", ...))` adds `.rule` aspect back when resolving `/draco/primes/AddNaturalSequence.rule.json`.

4 broken rule files hand-regenerated to match new Generator output. Per `feedback_fix_known_bugs.md`: don't assume the user fixed their copy — fix bugs in output when spotted.

---

## Workflow Shift — Claude Runs sbt

After the IDE test pass, the developer asked:

> **Dev:** I'm concerned that I'm not running the tests soon enough. Can you run important tests yourself, using sbt, when you think it would be useful? I would like to continue to use the click-approval option so I can know the issues as soon as you do.

Updated `user_preferences.md`: Claude now runs `sbt` tests (prefer `testOnly <class>` for targeted runs) after non-trivial changes. Commits and pushes still user-owned. Click-approval preserved as the real-time review gate.

---

## Cached JVM Jar Calibration

The very next `sbt "testOnly domains.ReferenceFramesGenTest"` run failed with:

```
FatalError: bad constant pool index: 0 at pos: 49428
  while compiling: <no file>
```

Mis-read as corrupt jar. Ran `rm -rf ~/.sbt/boot/scala-2.12.17/` — destructive fix. sbt re-downloaded; identical SHA `4a4dee1ebb59...`, identical bytes, same error. The file wasn't corrupt; the JDK 17 + Scala 2.12.17 + sbt parser-init combo was genuinely incompatible at the CLI path the developer's IDE runner bypassed.

> **Dev:** I'm ok with you doing if you have high confidence in the result. Use the result to determine the validity of your confidence.

The result invalidated the confidence. Calibration lesson saved (`feedback_sbt_bad_constant_pool.md`): verify file SHA against canonical before destructive cache cleanup; "bad constant pool index" often means version skew, not corruption.

For the rest of the session the developer ran tests from the IDE's Run/Debug configuration menu. Every IDE test run that followed passed.

---

## elementTypeNames Val Visibility

The developer opened a file and panicked when elementTypeNames wasn't visible in Scala:

> **Dev:** I was looking at the current domain source code that still had inline TypeDefinition instead of .loadType and when I didn't see elementTypeNames, I panicked.

After searching, no domain file still had inline `TypeDefinition(...)`. The inline-TD files found were core draco bootstrap types (Domain.scala, DomainType.scala, TypeDefinition.scala, etc.) — which *can't* load themselves because they define the load mechanism. The developer was looking at those and concluding the migration was incomplete.

Then the deeper realization:

> **Dev:** I had the (what appears to be now mistaken) impression, from earlier work and discussions, that elementTypeNames was already part of the generated code, and that having it in the domain type definition was a cross-check for whatever existed under src/*resources/ in the file system.

The Generator simplification from earlier in the session had *removed* elementTypeNames from generated Scala entirely — previously it was at least visible (if awkwardly nested) inside the `TypeDefinition(...)` literal. After simplification, only the JSON showed the list.

> **Dev:** I was hoping we were near or at the point where we could make changes by changing just the definitions, and then only the Generator source code itself, for any major code changes going forward.

The right move: make the Generator emit a standalone `lazy val elementTypeNames: Seq[String] = Seq(...)` on domain companions — Scala-visible mirror of the JSON list. JSON stays runtime-authoritative via `typeDefinition.elementTypeNames`; the val is documentation-as-code.

Scope was almost botched. Initial plan was to hand-edit all 12 (4 ref-frame + 8 hand-written) to match:

> **Dev:** Why are you doing that? It seems you addin something to source code that should be generated.

Correction: the Generator emits the val for domains; the 4 ref-frame files (generator-canonical, `ReferenceFramesGenTest`-enforced) get manually updated to match; the 8 hand-written domains are left alone — they'll catch up when someone touches them for unrelated reasons. Principle stated: only ever edit Generator source + JSON definitions going forward; hand-written source is not a workaround surface.

Change landed. IDE reported tests passed.

---

## Transform Domain Design Fork

Increment C opened with three open questions about how to represent a transform's cross-package type references. A transform at `domains.egocentric.geocentric.EgocentricGeocentric` extends `Transform[Egocentric, Geocentric]` — the two type parameters live in different packages from each other and from the transform's own package.

Three options surfaced:

1. **New `referenced: Seq[TypeName]` field on TypeDefinition.** Purely for import emission. Small data-model change (~5 touch points).
2. **Structure typeParameters as TypeName, not String.** Breaking refactor — every typeParameter usage anywhere would need updating.
3. **Implicit scanning at generation time.** Parse typeParameter strings for identifier-like tokens, cross-ref against known TypeNames. Fragile in single-td mode.

Also considered: **abuse `modules`**. Rejected — `typeModifier` emits `sealed` when `modules.nonEmpty`, a real semantic change.

Preparing to propose Option 1 as pathfinder, the developer restored a pre-API-error prompt that reframed everything:

> **Dev:** The one that distinguishes a transform domain is that the it is not a "tuple domain" but a "domain tuple". A single draco domain in the draco type system would be a "domain scalar". The term "domain transform" says that it is a domain 2-tuple (source, target) where source and target are domain scalars.

The distinction that resolved the fork:

- **Domain scalar** — a single draco domain (one TypeName, one package).
- **Domain tuple** — an ordered pair of domain scalars — which is exactly what `TypeDefinition.source` and `TypeDefinition.target` already encode.
- **Tuple domain** — a domain whose value is a tuple of anything (Primals, Holons, etc.) — the tuple elements need not be domains.

`source` and `target` have been `TypeName` fields on `TypeDefinition` all along. They carry `namePackage`. `referencedPackageImports` already scans them for cross-package imports. The data model *already* encoded the domain-tuple structure — no new field needed. Option 1 was dropped; Option 2 was taken off the critical path. Transform's derivation typeParameters can stay `Seq[String]` for the pathfinder, because being in scope is source/target's job — which the data model already does.

`isDomain` extended accordingly:

```scala
td.elementTypeNames.nonEmpty ||
(td.source.name.nonEmpty && td.target.name.nonEmpty)
```

— a type is a domain if it has named sub-elements (tuple-domain / domain-scalar form) OR if it is a domain tuple (source+target populated).

---

## Increment C Pathfinder

First transform: `domains.egocentric.geocentric.EgocentricGeocentric`, a skeleton `Transform[Egocentric, Geocentric]` with no inner primals.

JSON shape (the three-field structure that encodes domain-tuple semantics):

```json
{
  "typeName": {"name": "EgocentricGeocentric", "namePackage": ["domains","egocentric","geocentric"]},
  "source":   {"name": "Egocentric",           "namePackage": ["domains","egocentric"]},
  "target":   {"name": "Geocentric",           "namePackage": ["domains","geocentric"]},
  "derivation": [
    {"name": "Transform", "namePackage": ["draco"], "typeParameters": ["Egocentric", "Geocentric"]}
  ]
}
```

Generator-canonical Scala emitted matches what `domainGlobal` produces; the trait has `extends Extensible with Transform[Egocentric, Geocentric]`. Source (`Egocentric`) is in scope via Scala's enclosing-package rules — the transform's package is `domains.egocentric.geocentric`, so `domains.egocentric` is an enclosing package and its members are in scope automatically. Target (`Geocentric`) is imported via `referencedPackageImports` picking up `target`'s package. No `modules` involved; no `sealed` modifier; no abuse.

`ReferenceFramesGenTest` expanded to 47 tests (19 types parse + 19 match + 6 per-family compileMulti + 1 universe = 45 + the 2 new from the pathfinder = 47). IDE-reported: 47 passed.

---

## Increment C Fan-Out

> **Dev:** No, just go ahead and do all 11 without pausing for me.

The other 11 transforms to complete the 4×3 peer matrix: Ego→Helio, Ego→Galacto, Geo→Ego, Geo→Helio, Geo→Galacto, Helio→Ego, Helio→Geo, Helio→Galacto, Galacto→Ego, Galacto→Geo, Galacto→Helio.

Pure mechanical duplication — 11 JSONs + 11 Scala files + 12 `Ty` declarations and 12 family entries in `ReferenceFramesGenTest`. No new Generator changes. Batched as one directory-creation + 22 parallel file writes + one test-file edit.

`ReferenceFramesGenTest` now at 80 tests (31 parse + 31 match + 17 family compileMulti + 1 universe compileMulti). IDE-reported: 80 passed.

Task #5 (Reference-frame rebuild) complete across three increments: 5 frames + 10 leaves + 4 assemblies + 12 transform skeletons = 31 types.

---

## Cosmocentric as Domain — Deferred Design Question

With the transforms landed, Cosmocentric's status became visible: it's the designed "host of inter-frame transform rules" but currently emits as `TypeInstance` not `DomainInstance` (empty `elementTypeNames`, no source/target). To become a real domain hosting the 12 transforms, it needs to reference them somehow. The convention question:

- **elementTypeNames has been about package siblings** — every existing domain's list names types in the *same* package. Cosmocentric (`domains.cosmocentric`) has no transforms as siblings — they live at `domains.<src>.<tgt>`.
- **Option A:** stretch `elementTypeNames` to allow cross-package references (fully-qualified entries or entries carrying explicit package info).
- **Option B:** add a separate field like `hostedDomains` or `transformDomains` for super-domain hosts.

Deferred to a clean session. Fresh context, better thinking.

---

## Session Summary

### Code Changes

1. **Generator simplification** — `domainInstanceLiteral` collapsed to single-line `Domain[X](typeDefinition)`; no Scala-side duplication of `_elementTypeNames` / `_superDomain` / `_source` / `_target`. 4 ref-frame files updated.
2. **Rule naming bug fix** — `stripAspect` helper added; applied at 5 Generator emission sites; `ruleDefinitionLoad` emits base-name TypeName so `loadRuleType` re-adds `.rule` aspect correctly. 4 broken rule files hand-regenerated.
3. **elementTypeNames val emission** — Generator emits `lazy val elementTypeNames: Seq[String] = Seq(...)` for domain types, between `typeInstance` and `domainInstance`. 4 ref-frame files updated. 8 hand-written domains intentionally untouched (per "only Generator + JSON are authoring surfaces" principle).
4. **Increment C** — `isDomain` extended to recognize domain tuples via populated source + target. 12 transform-domain skeletons landed: 12 JSONs + 12 Scala files + `ReferenceFramesGenTest` at 80 tests.

### Design Decisions

- **Domain scalar / domain tuple / tuple domain** — explicit vocabulary. Domain tuple = transform = (source, target) of domain scalars. Data model already encodes this via `TypeDefinition.source` and `TypeDefinition.target`.
- **`isDomain` is structural, not derivation-sniffing** — recognized by either populated `elementTypeNames` OR populated `(source, target)`.
- **Principle: only ever edit Generator source + JSON going forward.** Hand-written source is not a workaround surface. Convergence happens by the Generator catching up, not by hand-aligning source to match Generator output.
- **Direct edits on `main` with click-approval as review gate.** No PRs, no worktrees unless asked.
- **Claude runs sbt tests proactively.** Commits and pushes remain user-owned.

### Calibration

- `sbt` "bad constant pool index" is usually JVM/Scala version skew, not file corruption. Verify SHA against canonical before destructive cache cleanup (`feedback_sbt_bad_constant_pool.md`).

### Next Session Priorities

1. **Cosmocentric as domain** — decide Option A (cross-package `elementTypeNames`) vs Option B (new `hostedDomains` field) or a third path. Blocks Cosmocentric-hosted transform rules.
2. **Inner primal value types** for transform domains — the "honesty later" items from the transform design memo (dual-inheritance primals, factory computes target primitive from source inputs).
3. **Transformation meta-type** in `draco.TypeElement` — `sealed trait Transformation extends Action`, encoder/decoder wiring.
4. **8 hand-written domains** — converge their boilerplate shape with generator-canonical form (`elementTypeNames` val, `domainInstance` shorthand). Separate conversation about when/how.
5. **`bin/draco-gen discover` / `verify`** — cross-check elementTypeNames against package siblings. Still pending from Chapter 21.
