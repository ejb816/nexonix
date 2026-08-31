# Draco Dev Journal — Chapter 75

**Session date:** August 28–31, 2026 (session in progress)
**Topic:** The forest scenario made real, gate by gate: the generate-and-compile gate built in the session's first turn — and the session-transition ritual run prematurely with it, Dev's confusion surfaced honestly ("Why so early in this new session?") and the record rolled back by Dev's own hand — then the three findings worked through: the cross-package import ambiguity fixed as a pure win, the codec-guard fork put to Dev and answered by a design correction transcribed verbatim ("The purpose of derived unit types is to identify unit conversion formula expressions, not to structurally morph a type"), `super` left visible in a headline rather than patched → the transform-domain question ("is transform-ness a derivation or an aspect?") answered by the `05b14bd` precedent and Dev's directive "Move source and target to domainAspect and add the drake keywords" — the metamodel move landed with two new globally reserved words and `83d87ac` pushed → the beta-priorities question, the four-point proposal (freeze the metamodel #51, gate the example tier, make `Drake.parse` an authoring path #61, one falsifying second projection), and Dev's redirect making the scenario the priority ("We should stabilize that before we try to get closer to beta working on something else") → `super` projecting, Gate 4 verified by breaking it, Gate 5 executing the forest for real (0.8 µM crosses at ×0.62, 0.3 µM filtered), two bugs found in the gate's own classloader handling → Dev's double catch transcribed verbatim ("You are not generating any source code because you are not parsing the drake into json. Also, you are writing Scala value expressions instead of using drake expression syntax") — the drake-expression rewrite immediately flushing two defects the Scala-writing habit had hidden, the scenario becoming a full trio corpus in the test tree at `87a2bb9`, 531 tests → the report-only archaeology (15 vs the alpha.6 notes' 16), the baselines table written into DRACO.md at Dev's word, and the closing protocol exchanges on full-result pastes versus the nine-line capture, ending with a commitment written to memory rather than promised. Transcript.

---

**[Dev]** Continuing draco. Last session closed at 0af56fd (527 green). Read first: project_transform_scenario_design, reference_evrete_scala_interop, project_type_expressions_track.

The forest scenario (src/test/resources/scenario/ — Forest root, Ash/Birch message domains, Ash_Birch transform domain, 22 definitions) parses and round-trips but has never been generated or compiled. That's the next gate, and it's one test file with no corpus changes: Drake.parse already yields a TypeDefinition, so hand it straight to Generator.generate and then RuntimeCompiler. Four constructs in it have no precedent in either corpus and have not been through the Generator — super (zero definitions carried one before), the transform domain's deliberate package/type-name break, a cross-package derivation into a target domain, and an underscore in a type name.

Deferred, none blocking: #51's valueType expressions and TypeParameter (flat, nesting unrepresentable — decided, not built); #63's dispatch grammar, whose acceptance corpus is world/Consumer + world/Provider, the two definitions that couldn't migrate off Actor(T); and how closedness gets declared now that modules is containment.

Conventions: Dev runs sbt, commits and pushes; write the git-record and CHANGELOG entry before staging; Cowork writes the journal.

**[Claude]** I'll start by reading the memory notes you named and getting oriented in the scenario corpus. *(Read the three notes and the scenario tree; traced `RuntimeCompiler`, the Generator's `superDomain`/`derivationRef`/knowledge paths, `TypeLoader` and `DefinitionPath`; wrote `src/test/scala/scenario/ScenarioGenTest.scala`; probed it offline against the tree's already-compiled classes plus the alpha.5 jar's dependencies; wrote `git-record-2026-08-27-2350` and a CHANGELOG entry.)*

The gate is in. I ran it offline first — compiled the new test with the tree's already-built classes plus the alpha.5 jar's dependency classes, then drove it through ScalaTest's runner — so these numbers are measured, not predicted. sbt still needs to run for real.

## `scenario.ScenarioGenTest` — report-only

```text
GATE 1  generate    22 of 22 emitted, nothing thrown
GATE 2  compile     12 errors, all 22 sources compiled as one unit
```

**Two of the four constructs are simply fine.** `Ash_Birch` — package tail disagreeing with the name, plus the corpus's first underscore in a type name — emits valid Scala with no special handling anywhere.

**Cross-package derivation is ambiguous at the import level** (2 errors + 2 cascade):

```text
trait Potency extends Micromolar     // in scenario.ash.birch
reference to Micromolar is ambiguous; it is imported twice in the same scope
by import scenario.birch._ and import scenario.ash._
```

`derivationRef` qualifies a parent only when it shares the *declaring* type's simple name. This is a different collision: `packageHierarchyImports` wildcards the own-package chain, `referencedPackageImports` the derivation's, and a transform domain sits under one species while deriving from the other. The two `type mismatch` errors are downstream of it.

**A `Primal(T)` wrapper carries no codec, so nothing containing one can** (8 errors). `Micromolar`/`Compound` declare no elements of their own, the codec gate is guarded on `ownElementNames.nonEmpty`, and `AshJasmonate`'s derived codec has nothing to call.

**`super` has no projection, and no error reports it.** The only trace in `Ash.scala` is an unused `import scenario.forest._`. The cost lands downstream: `actorKnowledge` collects rules from the actor's own domain, so `RootInterface` gets `newKnowledge("RootInterface")` with nothing accepted into it — `Forest` owns the transform rules. It compiles, runs, matches nothing, and stays green.

**One recommendation:** fix the Primal-wrapper codec gap first. It's the only one of the three that isn't scenario-specific — a newtype over a primitive is the most ordinary shape a domain has, and draco's own corpus misses it only because its leaves bottom out in `String`/`Int` directly.

One thing worth knowing about the test: the Generator reads the definition *graph*, not the definition. So the test materializes each parsed `TypeDefinition` as JSON at its own `resourcePath` under the classpath root, and removes it in a `finally` — without that, every `chainHits`/codec/knowledge lookup stubs out and the measurement is worthless. Nothing under `src/` is touched.

Record and CHANGELOG are written. The gate:

```bash
sbt test && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-27-2350 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

Expect 528 tests and the new headline `forest through the Generator: 22 definitions, 0 failed to emit, 12 compile error(s)`. Detail in `target/test-output/ScenarioGenTest.log`; emitted sources dumped to `target/test-output/scenario-generated/`, since `compileMulti` returns `info.msg` with no file attribution.

---

**[Dev]** I'm confused. I just added the suggested prompt from the previous session without carefully reading it, to this new session, and you have already made a CHANGELOG, git record after creating just one new file, with a commit/push command. Why so early in this new session?

**[Claude]** You're right, and nothing was committed or pushed — it's three uncommitted files, trivially undone.

The mistake was mine. Your prompt's last line — "write the git-record and CHANGELOG entry before staging" — is a statement of convention, telling me *when* those get written relative to a commit. I read it as a task for this turn. Combined with reading the previous session's record file for its format, I slid into the session-transition ritual after the first deliverable instead of at a stopping point.

It's also premature on its own terms: the record I wrote describes a session that found three defects and fixed none of them. That's a fine gate commit in isolation, but it isn't where this session should stop.

**Recommendation:** delete `draco-git-record/git-record-2026-08-27-2350`, revert the CHANGELOG edit, keep `ScenarioGenTest.scala`, and get on with the findings — starting with the Primal-wrapper codec gap, which is the one that isn't scenario-specific. I'll write one record at the end covering whatever the session actually does.

Say the word and I'll roll those two back. Or if you'd rather land the gate on its own first and start the fixes fresh, they're ready as-is.

---

**[Dev]** I did the delete and rollback so you can continue from here, as you would have.

**[Claude]** Dev handled the rollback. Now the codec gap. Before changing the Generator, let me measure what the guard currently suppresses — it was added deliberately. *(Verified the working tree; measured the guard's effect in a scratch copy — dropping it closes 8 of the 12 errors and moves exactly four pinned corpus files; implemented the import-ambiguity fix and verified it offline over the full pinned corpus; removed the probe directory.)*

The ambiguity fix is a pure win — 137/137 on the pinned corpus, no movement anywhere, four fewer errors. Landed and verified offline: 44 suites, **527 tests, 0 failures** with the fix in — the pinned corpus doesn't move at all, and the scenario drops from 12 errors to 8. The two `type mismatch` errors were indeed cascades of the two ambiguities; both went with them.

```text
trait Potency extends scenario.birch.Micromolar     // was: extends Micromolar
```

`derivationRef` now asks whether a bare name would actually *reach* the parent, rather than only whether it collides with the declaring type's own name. "Does package P hold a type named N" is a load probe — a definition is a resource, so the question was already answerable.

Caveat on the verification: I ran it offline against the tree's existing compiled classes plus the alpha.5 jar's dependencies, compiling only `Generator.scala` to a scratch directory. That's real measurement, not prediction, but it isn't sbt — the full suite still needs to run before anything is pushed.

## The codec gap — a fork that's yours, not mine

I measured it rather than fixing it. Dropping `ownElementNames.nonEmpty` from `codecDeclaration` closes all 8 remaining scenario errors, and every gate stays green except four pinned corpus files that would need regenerating:

```text
draco.Type              typeDefinition: TypeDefinition
draco.base.Meters       value: Double
draco.base.Radians      value: Double
draco.format.json.Value name: String, pathElements: Seq[String]
```

**The guard cannot be narrowed — only removed or replaced.** `draco.base.Meters` is structurally identical to `scenario.ash.Micromolar`: a `Primal(T)` wrapper adding nothing. No predicate gives one a codec and withholds it from the other. (The nine `TypeElement` subtypes that look like they'd be affected aren't — they take the `Codec.sub` branch before reaching this guard.)

Which puts the real question underneath: draco already holds that **codec is declared, not inferred** — there's a `codecAspect` — yet codec *presence* is inferred from a shape heuristic, and the heuristic is what's wrong. Two ways out:

- **Relax the heuristic now.** Small, measured, closes the gap; costs four inert codecs on types nobody serializes.
- **Make presence declared** — a field on `codecAspect`. Right end state, and it lets `Micromolar` say yes while `Meters` says nothing. It's a metamodel change plus a corpus-wide sweep, and it sits next to #51.

**Recommendation: relax now, and treat the declaration as the real answer for later.** The four spurious codecs are cheap, and I'd rather not have the scenario blocked behind a metamodel change. But adding codecs to four corpus types changes what draco projects, so I've stopped here rather than assume.

`super` is untouched and still the one no error reports — the empty `Knowledge` in `RootInterface`. That one is genuinely a design question (does a super-domain confer rule visibility?), tied to #63.

---

**[Dev]** The purpose of derived unit types is to identify unit conversion formula expressions, not to structurally morph a type.

**[Claude]** That correction lands on my framing, not just the detail. Let me say back what I take it to mean, so you can cheaply tell me if I've got it wrong.

`Meters`, `Radians`, `Ash_Birch.Potency` aren't wrappers that happen to sit on a parent — they're unit types, and the derivation edge is there so a conversion formula has two ends to name (`ash.Micromolar → birch.Micromolar` is `× 0.62`; that factor is the whole content). The edge adds no structure and takes none away. So "four codecs nobody asked for" was the wrong cost to weigh: they aren't inert wrappers, they're first-class types whose job I'd mischaracterized, and the guard that keeps them codec-less rests on the same mischaracterization.

That also settles the shape question I was about to put to you. A unit type has its parent's structure exactly, so its JSON should be its parent's JSON — and the corpus already says what that is: `TypeElement extends Primal[Json]`, and every one of its subtypes encodes `value` as a named field. `{"value": 3.4}` is not a morph, it's the faithful projection. Transparency would have been the morph.

*(Checked the reading against the corpus — `Meters from Distance(Double)` with a one-parameter factory is the same shape as `Potency`, and only leaf units carry a factory at all; dropped the guard, regenerated the four corpus files, added a third gate, ran everything offline.)*

Both fixes are in, and the scenario now projects end to end.

```text
forest through the Generator: 22 definitions, 0 failed to emit, all of it compiled,
                              1 super-domain rule(s) an actor cannot reach
```

Offline: 44 suites, 527 tests, 0 failures — the existing suite doesn't move. The four regenerated corpus files compile on their own too.

| | |
|---|---|
| `derivationRef` | qualifies when a bare name wouldn't *reach* the parent, not just when it collides with the declaring type's own name |
| codec guard | `ownElementNames.nonEmpty` dropped; a unit type derives its parent's projection |
| `Type`, `Meters`, `Radians`, `format/json/Value` | regenerated, now byte-identical to Generator output |

I added a third gate rather than let the green headline stand. GATE 2 going clean is precisely what would have buried `super` — an actor with an empty `Knowledge` compiles, runs, and matches nothing. So the gate now counts what an actor can actually reach:

```text
ash/RootInterface.drake   own domain scenario.ash.Ash        reachable=0  []
                          super      scenario.forest.Forest  UNREACHABLE=1 [AshBirchAlarm]
```

That number is in the console headline, so it moves when the projection does.

## `super`, and where I've stopped

`actorKnowledge` collects rules from the actor's own domain only. Making it walk the `superDomain` chain is a small change and would put `AshBirchAlarm` into `RootInterface`'s knowledge — which also gets the co-residency the fact-ambiguity finding needs, since every species' actor would then share Forest's rule set in one knowledge.

But `super` has never had a projected meaning at all, and choosing one is your call rather than mine — that's why I stopped here instead of writing the third fix. **Recommendation: yes, `super` confers rule visibility, and `actorKnowledge` walks the chain.** Say go and I'll do it.

No git-record or CHANGELOG this time — I don't know whether `super` makes this a stopping point or the middle of one. Tell me which and I'll write them then.

---

**[Dev]** Look at src/test/resources/scenario/ash/birch/Ash_Birch.drake and src/test/resources/draco/Transform.drake and discuss if we can move toward a de jure formulation of transform domain in drake

**[Claude]** `Ash_Birch.drake` has changed since I last read it — it now derives `Transform(Ash, Birch)`. Let me gather what's already in the metamodel before discussing. *(Read both files and the aspect definitions; put Dev's new formulation through parse, emit, and generate.)*

`source` and `target` already exist on `dracoAspect` — but nothing in any corpus populates them, and drake has no keyword for them. Three things the current pair does, measured:

```text
Transform.drake      from (S,T)  →  TypeName(name="", namePackage=[], typeParameters=[S,T])
                     generates:  trait Transform[S, T] extends [S, T] with DracoType
Ash_Birch.drake      from Transform(Ash, Birch)  →  resolves to scenario.ash.birch.Transform
                     (bare = own package; needs `from draco Transform(...)`)
draco.Transform      TypeLoader.tryLoad → false (there is no Transform.json)
```

The first is the interesting one. Compare the map operator, which drake already carries as a foreign derivation:

```text
Dictionary   from {K, V}   →   name="Map",  namePackage=[]   →   extends Map[K, V]
Transform    from (S, T)   →   name="",     namePackage=[]   →   extends [S, T]
```

`{K,V}` has a name behind the operator; `(S,T)` doesn't. It round-trips only because emit reconstructs the operator from an empty name plus two parameters — parse and emit agree on nothing at all, which is why the projection comes out as `[S, T]`. So `from (S,T)` isn't yet a legal foreign derivation; it's a hole the round-trip gate can't see.

The knock-on from the second is quieter and worth noting for its own sake: because `scenario.ash.birch.Transform` doesn't resolve, `chainHits` can't reach `DracoType`, and `object Ash_Birch` silently lost `with DracoType` and its `override`. A mis-resolved reference degrades emission without erroring.

## The design question

I'd put it this way: **is transform-ness a derivation or an aspect?** Because draco just answered that question for actors, in the other direction.

`05b14bd` removed `Actor(T)` on the reasoning that a role's parameter has no business being an edge in the data-inheritance tree — actor-ness is aspect presence, and the message type lives in `actorAspect.messageType`. `Transform(Ash, Birch)` is the same move with two parameters instead of one.

And here the structural reading doesn't merely offend a principle, it's false. `Transform(S,T) from (S,T)` asserts that `Ash_Birch` *is* a pair. It isn't: a transform domain holds no `Ash` value and no `Birch` value, it holds conversions. That's why it can't project — Scala's `Tuple2` is final, and `Ash_Birch` has no `_1`/`_2` to offer. The Haskell test gives the same answer: you would not write `data AshBirch = AshBirch Ash Birch`, because there is nothing to put in the fields. The pair is *metadata about* the domain, not its structure. `DomainTransform` dodges this by routing through `Holon((S, T))` — and that is the single compound type-parameter actual #51 records as `src/main`'s only violation of atomic `typeParameters`.

## What I'd recommend

**`source` and `target` as aspect fields, given a drake surface inside the `domain` block.** The slots already exist on `DracoAspect`, nothing in any corpus populates them, and `Generator.isDomain` already carries a `(source && target)` clause it calls a transitional fallback for transform-domain JSONs. The construct has been sitting there waiting for a surface.

```text
type Ash_Birch
domain scenario ash birch Ash_Birch
  super scenario forest Forest
  source scenario ash Ash
  target scenario birch Birch
  types [
    Potency
    Marker
  ]
```

Two keywords, symmetric with `super` and `types`, both nominal `TypeName` references per the 2026-08-25 decision. Role is presence, applied to domains exactly as it's applied to rules and actors: a domain with a direction *is* a transform domain, and nothing needs to appear in the extends clause — which is right, because nothing is inherited.

Three things I'd flag as coming with it rather than settled by it:

- **Which aspect.** They're on `dracoAspect` today, but direction is a domain-level role, so `domainAspect` is where the five-aspect split would put them. That's a metamodel move, not a surface one.
- **The leaf level needs nothing.** `Potency from birch Micromolar` + `factory par ash.Micromolar` already identifies the conversion by its two ends — that's your unit-type formulation, and Gate 2 now proves it projects. Only the domain level is missing a de jure form.
- **It buys a lint.** With direction declared, "every member of a transform domain derives something in `target` and takes a parameter typed in `source`" becomes checkable — the negative presence-predicate the projection-validation note keeps asking for. And `DomainTransform`/`TypeTransform` would likely become redundant, taking `Holon((S, T))` and #51's lone compound actual with them.

The one thing I'd keep from the current experiment either way: `from (S,T)` exposed a real gap in the foreign-derivation surface. Whether or not transform goes to an aspect, the tuple operator either needs a name behind it like `{K,V}` has, or should be rejected at parse rather than accepted into an unprojectable shape.

---

**[Dev]** Move source and target to domainAspect and add the drake keywords

**[Claude]** Accepted. Let me survey every reference before moving anything. *(Surveyed every reference to the two fields; moved them from `DracoAspect` to `DomainAspect` — definitions first, then the Generator's three references and the drake surface; checked how value slots terminate before reserving the keywords, and checked every `.drake` in all three corpora for bare `source`/`target` at bracket depth zero; regenerated the three affected companions with the existing compiled Generator to break the bootstrap circularity; rebuilt the whole tree when the first probe turned out to be reusing stale classes; redid two JSON edits as text surgery when the round-trip expanded compact expression trees; updated the scenario, `drake.dlt`, and DRACO.md's aspect table; wrote `git-record-2026-08-30-1515` and the CHANGELOG entry. `Transform.drake` was gone — Dev had removed it.)*

Done. `source` and `target` now live on `domainAspect`, and drake says a transform domain out loud:

```text
type Ash_Birch
domain scenario ash birch Ash_Birch
  super scenario forest Forest
  source scenario ash Ash
  target scenario birch Birch
  types [ Potency Marker ]
```

Parsed: `source=scenario.ash.Ash  target=scenario.birch.Birch`, derivation empty, `DomainAspect.isEmpty=false`. `Ash` — an ordinary domain — comes back with both ends empty, so there are no false positives. Nothing appears in the emitted extends clause, which is the point.

**528 tests / 45 suites, 0 failures**, with every main, mods and test source recompiled from scratch — the earlier probe was reusing stale classes that still called `DracoAspect.apply$default$8`, so I rebuilt the whole tree to make the run honest. `EXAMPLE-DOMAIN GEN MAP` holds at 28/20, and the scenario still parses 22 clean, so the new keywords round-trip.

What moved, beyond the two aspect definitions: `TypeLoader.rooted` no longer copies the two fields; `Generator.isDomain` gained `isTransformDomain` and its `(source && target)` clause stopped being a "transitional fallback" in the comment, since it now means something; `referencedPackages` reads them from the domain aspect, which is why `Ash_Birch` now imports `scenario.birch`. `drake.dlt` and `DRACO.md`'s aspect table are updated in the same change.

One thing worth flagging on the surface itself: `source` and `target` are now globally reserved words, so a value slot stops at either. Three definitions already use `source` as a member *name* — `SourceContent`, `DefinitionPath`, `CLI` — and those are safe because names are read positionally after `fix`/`dyn`/`loc`. But no host-opaque value anywhere may contain a bare `source` or `target` token at bracket depth zero. I checked every `.drake` in all three corpora before reserving them; none does. It's the same constraint `message`, `start`, `body` and `action` already impose, just on two more common words.

Record and CHANGELOG are written. The gate:

```bash
sbt test && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-30-1515 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

Still open, and deliberately visible rather than patched: `super` has no projection, so `RootInterface` builds a `Knowledge` that accepts nothing while `Forest` owns the rule it needs. That's the `1 super-domain rule(s) an actor cannot reach` in the headline.

---

**[Dev]** The tail end of the results from the composite command you gave me. Would more of the result data be useful, or is verifying it passed, enough?

*(Followed by the pasted tail: `Total number of tests run: 528`, `Suites: completed 41`, all passed in 19 s; committed as `[main 83d87ac] The forest projects, and a transform domain says so in drake` — 21 files changed, 586 insertions, 84 deletions, `ScenarioGenTest.scala` and the record among the new files — and pushed `0af56fd..83d87ac`.)*

**[Claude]** Enough to know the gate passed and the push landed — 528/528 matches what I measured, and 21 files is exactly the working set plus the record.

But pass/fail isn't the whole gate here, by DRACO.md's own rule: three of those 528 assert nothing, they measure. The numbers I'd actually want back scrolled past above that tail:

```text
scenario in today's drake: 22 files, 0 rejected, 0 parsed-but-drifted, 22 clean
forest through the Generator: 22 definitions, 0 failed to emit, all of it compiled,
                              1 super-domain rule(s) an actor cannot reach
EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48)
```

Those are the ones that can move while the suite stays green — the two-defects-in-one-August-session case behind #62. For this commit I'd already measured all three offline, so nothing was riding on it; but as a habit the headlines are the part worth pasting, not the tail.

They're console-only (`test.report` doesn't go to the per-suite files), so they have to come off stdout:

```bash
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "scenario in today|through the Generator|GEN MAP|CO-DECLARATION|PON|Tests:"
```

One discrepancy worth naming rather than glossing: sbt reports 41 suites, my offline runner reported 45. The test count is identical at 528, so nothing ran twice and nothing was skipped — it's a discovery-counting difference between sbt's framework adapter and ScalaTest's own `Runner` with four `-w` wildcards. Treat sbt's number as the real one; mine was the probe.

So: for a normal green run, the tail is fine. When something in the Generator, the emitter, or the corpus moves, send the headline lines — that's where a regression would show up without failing anything.

---

**[Dev]** What are the priorities for getting to beta release?

**[Claude]** Let me ground this in the actual backlog rather than my impressions. *(Read the open issue list, the alpha.6 release notes, and every "before beta" mention in the tree.)*

There's no written definition of beta anywhere in the tree — one passing mention in the alpha.6 notes ("`TypeName` and `derivation` are expected to change shape (#51) before beta") and nothing else. Priorities are unorderable without one, so let me propose it first:

> **Beta = the definition format has stopped moving, and every tier that ships is gated.**

Not "feature complete" — draco's users author *definitions*, so what has to stabilise is the metamodel and the surface, not the roadmap. Ordered against that:

**1. Freeze the metamodel — #51.** Already `priority-next`, and the only thing anything in the tree ties to beta. It's also the one that gets more expensive every week: this session moved `source`/`target` between aspects and it cost an afternoon, because nobody outside the repo has a definition file yet. After beta that's a migration. What's left is `valueType` expressions and the `TypeParameter` atom — both decided, neither built. Add the hole I hit yesterday: `(S,T)` parses to a *nameless* `TypeName` and emits `extends [S, T]`, where `{K,V}` correctly carries `Map` behind the operator. That's #51's shape exactly, and it round-trips clean, so no gate sees it.

**2. Gate the example tier.** `src/main` is pinned type-for-type by `DracoGenTest`. The example domains — the tier that models what an outside author writes on top of the jar — are at 28 match / 20 differ, and `ExampleDomainsGenTest` compiles none of it. I looked at the diffs: they're not Generator defects, they're hand-written files that stopped being regenerated several Generator changes ago (`Aerial` is missing `with DracoType` and `elementTypeNames`). So it's regenerate-the-20, then make that test a real gate. `ScenarioGenTest` is now the template — generate, compile as one unit, assert. This is #62's substance for the case that matters most.

**3. Make `Drake.parse` an authoring path — #61.** Today drake round-trips but only the application surface becomes a tree; every other value comes back as host-opaque text that only Scala can read. A definition language you cannot author in is a serialisation format with good manners. This is the largest gap between what draco claims and what it does, and #63's dispatch grammar and #58/#59's match cases are all instances of it — the scenario's `RootInterface` is a Scala `match` smuggled through as a single-line string.

**4. One falsifying second projection — not the full port.** #20/#21 are roadmap-sized, but the *claim* under test is neutrality, and one realization cannot test it. Enough Haskell to project the `draco` root domain would either falsify the claim or retire it. Your own note is the argument: a round-trip preserves mis-modelling, only a second opinionated projection exposes it.

**What I'd cut from beta:** the tooling scripts (#33–#36), Orion/Dreams/Ego (#12/#13/#25), Maven Central (#22 — that's release engineering, needed at release, not before it), and `super`. `super` is a real gap and I left it visible in a headline, but it's one construct in one scenario; it doesn't block a format freeze.

**If you take one thing: #51.** Everything else on this list survives being late. A metamodel that moves after people have definition files is the one mistake that can't be absorbed — and #51 is the issue that has been frozen, unfrozen, and partially landed three times already, which is itself the signal.

I haven't opened or edited anything — say the word if you want any of this put on the issues as comments.

---

**[Dev]** The point of scenario.forest example is to establish consistent transform domains, transform types, and specifying the correct super-types for both, enough to generate and execute source code. We should stabilize that before we try to get closer to beta working on something else.

**[Claude]** That reorders things, and the redirect makes sense — a language that can't execute one worked example isn't ready to have its format frozen. Let me take you at the strongest reading: **generate and execute**, not generate and compile.

`super` projecting is the blocker for that, so let me do it. *(Made `actorKnowledge` walk the super-domain chain; rewrote Gate 3 to measure the emitted source rather than the definitions; ran everything offline.)*

`super` projects now. 528/528, pinned corpus unmoved.

```text
GATE 3 — rules an actor's Knowledge OWNS vs rules it ACCEPTS:
  ash/RootInterface.drake   own domain scenario.ash.Ash        owns=0 []
  ash/RootInterface.drake   super      scenario.forest.Forest  owns=1 [AshBirchAlarm]
                            ACCEPTED   in emitted source      1 of 1
```

```text
private lazy val knowledge: Knowledge = {
  val k = Rule.knowledgeService.newKnowledge("RootInterface")
  scenario.forest.AshBirchAlarm.ruleType.pattern.accept(k)
  k
}
```

`actorKnowledge` walks the super-domain chain (cycle-guarded, since a super edge is authored and an authored edge can be wrong) and spells each rule package-relative. I also rewrote Gate 3 to count what the *emitted source* accepts rather than what the definitions own — the old version described the gap, so it would have kept reporting it after the fix. The headline is now `0 rule(s) an actor owns but does not accept`, and it moves if that ever stops being true.

## On "correct super-types for both"

Worth saying explicitly, because I think it's already right and shouldn't be "fixed":

| | inherits | contained in |
|---|---|---|
| `AshSap` | `Ash` | `Ash` |
| `Potency` | `scenario.birch.Micromolar` | `Ash_Birch` |

Every other member of every other domain mirrors its containment with an inheritance edge — which your design note flags as the reason the corpus can't tell the two apart. `Potency` is the first place they genuinely diverge: it *is* a birch Micromolar, and it *lives in* Ash_Birch. So the right move is to leave `Potency` deriving only the target primal, and let the domain pointer carry membership. `trait Ash_Birch` ends up an empty marker with no subtypes, and that's correct rather than a gap.

But that's me asserting consistency, not establishing it. Now that direction is declared, it's checkable — every member of a transform domain must derive a type belonging to `target` and take a factory parameter typed in `source`. That's a Gate 4, and it turns the convention into something the suite enforces.

## To actually execute — one thing I need from you

Then Gate 5: load the compiled classes and fire the rule for real — insert an `AlarmSignal`, expect a `BirchJasmonate` with potency × 0.62.

One obstacle is a scenario decision, not a code one. Evrete gives an RHS-inserted fact no working-memory node unless some rule consumes it, so `ctx.insert(primed)` on a `BirchJasmonate` will fail with nothing declaring it in an LHS. The scenario currently has no consumer. Biologically the missing piece looks like the receiving end — birch's own root interface taking delivery — which would also make the example symmetric rather than one-directional.

**Recommendation: add a consuming rule to `Forest` (say `BirchAlarmReceived`, matching `BirchJasmonate`), since Forest already owns the transform rules and that keeps them co-resident in one knowledge.** Confirm that, or name the shape you'd rather have, and I'll do Gate 4 and Gate 5 together.

---

**[Dev]** Your recommendation seems aligned with my current understanding. Please proceed.

**[Claude]** *(Authored `BirchAlarmReceived`; built Gate 4 and confirmed it has teeth by deliberately breaking it; built Gate 5 and ran the forest for real; the gate passed alone but failed in the full suite — diagnosed and fixed two bugs in its own classloader handling; three consecutive full-suite runs green; wrote `git-record-2026-08-31-1055` and the CHANGELOG entry.)*

The scenario executes.

```text
GATE 1  generate      23 of 23
GATE 2  compile       all of it, as one unit
GATE 3  means it      rules an actor OWNS vs rules its emitted source ACCEPTS — 1 of 1
GATE 4  consistent    each transform member is one leaf conversion — 2 of 2
GATE 5  runs          fired: 1 BirchJasmonate reached the birch side
                        potency=0.496 uM  compound=jasmonic-acid
```

Two alarms went in, one came out — 0.8 µM crosses, 0.3 µM is filtered. The threshold lives in `AshBirchAlarm`'s condition and the 0.62 factor lives nowhere but `Potency`'s factory body, so both numbers being right is what says the guard and the formula survived projection. Everything executed is generated and none of it is on the compile classpath. The actor membrane is *not* exercised — `receive` is a host-opaque `match`, so running it would prove nothing about any other target.

**`super` projects.** `actorKnowledge` walks the super-domain chain now, so the root's transform rule reaches the species' actor — and that's also what puts those rules in one knowledge, which the engine's fact resolution requires. I rewrote Gate 3 at the same time: it had been counting what the *definitions* own, so it described the gap rather than detecting it and would have gone on reporting `UNREACHABLE=1` after the fix. It counts the emitted source now.

**Gate 4 was verified by breaking it** — pointing `Potency` at the source side gives `derives nothing in target scenario.birch.Birch`, naming the member, independently of the compile error it also causes. It deliberately does *not* require a member to derive its transform domain: `Potency` **is** a birch `Micromolar` and **lives in** `Ash_Birch`. That's the first place in any corpus where containment and inheritance come apart instead of coinciding, which is worth stating rather than filling in.

`BirchAlarmReceived` joined `Forest` as the receiving end. Required, not decoration — Evrete gives an RHS-inserted fact no working-memory node without a consumer.

**Two bugs in my own gate, both worth knowing about.** It passed alone and failed in the full suite: `Rule.knowledgeService` is a singleton that fixes its classloader before the test can reach it, so Evrete's runtime Java compilation of the condition failed only when another suite had touched it first. It builds a private `KnowledgeService` now. And I'd restored the context classloader only on success, which would have left a temp-dir loader installed for whatever ran next on that thread. Three consecutive full-suite runs green after both fixes.

Record and CHANGELOG written.

```bash
sbt test && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-31-1055 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

528 tests. Every clause of the headline is now load-bearing, so send that line back rather than the tail if anything looks off.

---

**[Dev]** You are not generating any source code because you are not parsing the drake into json. Also, you are writing Scala value expressions instead of using drake expression syntax that should be parsed into json format expression tree.

**[Claude]** Both criticisms land. Let me check what I've actually got before answering. *(Reread the scenario tree and the rule/actor renderers; rewrote `BirchAlarmReceived`'s action on drake's application surface and put it through parse and emit; fixed the crash and the missing case that surfaced; censused the remaining host-opaque values and converted the convertible ones; reran everything.)*

You're right on both, and the second one paid immediately.

## Drake expressions instead of Scala

I'd written `BirchAlarmReceived`'s action as raw Scala. Rewritten on the application surface it parses to a real tree:

```text
    loc received java.util.List(scenario.birch.BirchJasmonate) ctx.getRuntime parameters
      .get parameters par "received"
    mon received.add parameters par jasmonate
```
```json
{"()":[{".":[{"()":["ctx.getRuntime"]},"get"]},"\"received\""]}
```

Doing that hit **two real defects that the corpus's Scala-writing habit had been hiding**:

- `Drake.emit` **crashed** — `unfoldChain` did `empty.head` on a chain whose base receiver is a *niladic* call (`ctx.getRuntime()`), because it assumed the receiver-application's function is a `.`-path. Fixed, plus `valueLines` now writes an application base with its own `parameters`.
- `Generator.actionBody` had **no `Local` case**. A `loc` in a rule action fell through to the catch-all, emitting the initializer as a bare statement and dropping the binding — every later reference left dangling. `factoryBody` and `actorActionBody` both had the case; this site was missed when `Local` was added.

Neither was reachable while values were Scala strings. Then I censused the rest and converted what's convertible:

```text
7 host-opaque → 4 host-opaque, 3 → 6 drake-native trees
  converted:  knowledge.newStatefulSession()   session.fire()   ctx.insert(primed)
  remaining:  msg match { … }                  needs #58 (match cases in the actor aspect)
              _compound.value                  a path — the lexer glues it, #61
              _potency.value * 0.62            infix operator, #61
              jasmonate.potency.value > 0.5    infix operator, #61
```

Those four aren't stubbornness — drake has no surface for them yet. 528/528, still round-trips, still runs.

## Not generating source code — you're right, and it's worse than a detail

`src/test/resources/scenario` is 23 `.drake` and nothing else. No `.json`, no `.scala`. My gate parses drake in memory, writes JSON to a temp classpath root purely so `TypeLoader` can resolve names, deletes it, compiles to a temp dir, deletes that. **Nothing durable is generated, and the scenario is one-third of a trio.** Compare `src/main/resources/draco`: 54 drake, 53 json, and matching Scala under `src/main/scala`.

So the pipeline should be the real one:

```text
X.drake  --Drake.parse-->  X.json  --Generator.generate-->  X.scala
```

with the JSON committed at `src/test/resources/scenario/**` beside each `.drake`, and the Scala committed at `src/test/scala/scenario/**` — which sbt already compiles, so no build change. Then `ScenarioGenTest` becomes `DracoGenTest`'s shape (generate from JSON, compare to the file on disk), compilation is transitive the way DRACO.md describes, and Gate 5 drops all its reflection and classloader machinery — it just calls `AshBirchAlarm.ruleType`.

**One thing I want your call on before I do it:** that commits ~46 generated files into the test tree. I think that's exactly right — it's what makes the scenario a corpus rather than a fixture, and it's the only way the generated Scala is ever compiled by the build rather than by a test. But it's a new generated-source tier, so I'd rather hear you say it than assume it.

---

**[Dev]** Yes, the generated json belongs in the test tree. As long as we keep the module/package naming consistent with how type names are used, we use src/test for new definitions that can later exist in main.

**[Claude]** *(Generated the full trio in two passes — every `.json` written before generation so cross-references resolve, each path taken from the type's own `TypeName`; rewrote `ScenarioGenTest` to `DracoGenTest`'s shape and confirmed the new pinning gate bites; deleted the now-stale `git-record-2026-08-31-1055` and replaced it with `git-record-2026-08-31-1344` covering everything since `83d87ac`; repaired a CHANGELOG block that `replace` had landed in an old release section, and consolidated the `[Unreleased]` scenario entries.)*

Both corrections acted on.

## Nothing was being generated — now it is

`src/test/resources/scenario` was 23 `.drake` and nothing else. It's a full trio now: `X.drake` beside `X.json`, projection committed under `src/test/scala/scenario`, every path taken from the type's own `TypeName` so the three agree by construction. Generation runs the real route — parse drake to normative JSON, then generate each `.scala` from the JSON **as loaded through `TypeLoader`**, not from the in-memory parse.

The gate got *smaller*, which is the argument for doing it. sbt compiles the projected tree, so compilation needs no gate of its own — the transitive argument `DracoGenTest` already rests on. And because the scenario types are on the classpath like any others, the execution gate calls them directly: the reflection, the child classloader and the private `KnowledgeService` all deleted. Four gates that assert rather than report, two of them verified by breaking them.

## Drake expressions instead of Scala — and what that found

```text
    loc received java.util.List(scenario.birch.BirchJasmonate) ctx.getRuntime parameters
      .get parameters par "received"
    mon received.add parameters par jasmonate
```

Writing that instead of Scala broke two things immediately:

- **`Drake.emit` crashed.** `unfoldChain` did `empty.head` on a chain whose base receiver is a *niladic* call — it assumed the receiver-application's function is a `.`-path, and a glued token has no path elements.
- **`Generator.actionBody` had no `Local` case.** A `loc` in a rule action fell through to the catch-all: initializer emitted as a bare statement, binding dropped, later references dangling. `factoryBody` and `actorActionBody` both had it; this site was missed when `Local` became the eleventh element kind.

Neither was reachable while values were Scala strings. That's the rule making its own case better than I could: host-opaque text isn't a shortcut, it's where the bugs hide.

Then I censused the rest — **7 host-opaque values → 4**. The three applications converted. The four that remain can't be said yet: `msg match {…}` wants #58, and `_compound.value`, `_potency.value * 0.62`, `jasmonate.potency.value > 0.5` are paths and infix operators, which is #61.

```text
Tests: succeeded 531 — the gate went from 1 report to 4 asserts, plus 23 sources joined the build
the forest runs: 1 of 2 ash alarms crossed to birch
scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean
```

Two housekeeping notes: I deleted the uncommitted `git-record-2026-08-31-1055` — it described the reflection machinery that no longer exists — and replaced it with one record covering everything since `83d87ac`. And I consolidated the CHANGELOG's `[Unreleased]` scenario entries, since one of them described a state that lasted a single commit.

```bash
sbt test && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-31-1344 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

*(Delegated action — Dev ran the block and pasted the full output: **531 tests run, 41 suites, all passed** in 15 s, every report-only headline visible — gen map 28/20, `drake surface losses: 15 fields across 91 types`, parse scope 81 + 10 / 0 held back, PON 80/550/42, canonical 7, `scenario in today's drake: 23 files … 23 clean`, `the forest runs: 1 of 2 ash alarms crossed to birch`, the CO-DECLARATION verdict with its two Evrete warnings; committed as `[main 87a2bb9] The scenario becomes a corpus, and the forest runs` — 55 files changed, 2006 insertions, 214 deletions, the scenario's `.json` and `.scala` trio among the created files — and pushed `83d87ac..87a2bb9`.)* Landed as `87a2bb9`, 531/531.

I checked the one figure in that output that looked like it had moved. `drake surface losses: 15 fields across 91 types` — the alpha.6 notes record **16**. It isn't this work:

- The 2026-08-25 derivation-edge commit predicted, in its own record, that the `[reference package: n]` bucket would disappear entirely and the other two would be unchanged. 16 − 1 = 15, and the buckets printed are exactly the two it named: `[expression form: 12] [empty-collection spelling: 3]`.
- My `Drake.emit` fix can't have touched it either. A chain on a niladic base *crashed* the emitter before, so no value in either main corpus has that shape — it would have been an error, not a count.

So 15 has been the number since `324556c`, and the "16 fields" in the alpha.6 notes and CHANGELOG is a pre-#51 figure that's correct for what it describes. I've left those alone.

The rest of the report-only numbers are where they were: gen map 28/20, PON 42 discrepancies and 7 canonical diffs, `CO-DECLARATION DROPS THE FACT` still firing its two Evrete warnings, scenario 23 clean.

One offer, no action taken: that check took three greps across the record history because the current values of the report-only numbers aren't written down anywhere — DRACO.md names *which* tests measure rather than assert, but not what they currently read. A short baseline table there would make "did that move?" a one-line lookup instead of an archaeology exercise, and it's the cheap half of #62. Say the word and I'll add it.

---

**[Dev]** Go ahead and add the short baseline table.

**[Claude]** Two checks on that: the capture line against Dev's actual output, and whether every row is reachable from it. *(Tested the grep against the exact headlines from Dev's pasted run — nine lines out, no noise, no misses — and found the one row that isn't on stdout at all; added the table to DRACO.md; wrote `git-record-2026-08-31-1358`, with the CHANGELOG omission recorded as deliberate.)*

Added to DRACO.md, right under the rule it makes followable.

| test | headline | baseline |
|---|---|---|
| `ExampleDomainsGenTest` | example-domain gen map | 28 match, 20 differ, 0 error, 0 missing (of 48) |
| `DrakeParseTest` | drake surface losses | 15 fields across 91 types — expression form 12, spelling 3 |
| `DrakeParseTest` | Drake.parse scope | 81 draco + 10 mods in, 0 held back |
| `DrakeGenTest` | mods actors pending `.drake` | 0 — file-only |
| `PonCorpusTest` | PON corpus | 80 numbers, 550 expressions, 42 discrepancies |
| `PonCorpusTest` | canonical check | 80 numbers, 7 differ from generated canonical |
| `ScenarioDrakeTest` | scenario in today's drake | 23 files, 0 rejected, 0 drifted, 23 clean |
| `SubtypeFactVisibilityTest` | rete subtype visibility | CO-DECLARATION DROPS THE FACT (categorical) |

Three things I did beyond the plain table:

**A capture line, verified against your actual output.** These go to the console logger, not the per-suite files, so they vanish with the scrollback. I tested the grep against the exact headlines from your run — nine lines out, no noise, no misses. That also caught one row that *isn't* reachable that way: `DrakeGenTest`'s pending-`.drake` census prints only to its log file, so the table says so rather than sending someone hunting.

**A maintenance rule, because a figure nobody must maintain is one nobody can trust.** A disagreement between table and run means either the run regressed or the table went stale — both worth stopping for — so the commit that legitimately moves a number updates the table in the same commit, the way the CHANGELOG entry and record already ride along.

**The alpha.6 "16" left standing.** It was true when written and became 15 at `324556c`. That drift is exactly what the table exists to make visible; papering over it would remove the worked example.

The scenario's gates are deliberately *not* in the table — they used to be report-only and now assert. Moving a measurement into an assertion is the goal, so the table should shrink over time. This is the cheap half of #62, not a substitute for it.

Record written, no CHANGELOG entry — this changes no type, field, command, behaviour or gate, and the record says the omission is deliberate.

```bash
sbt test && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-31-1358 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

Expect 531/41 unchanged — which is the point, since this commit writes those numbers down.

---

**[Dev]** Even though it's probably expensive in terms of model token usage on my Claude account, I think giving the entire result from executing your composite command (that ends with a git push), is worth it.

*(Followed by the full pasted output: **531 tests run, 41 suites, all passed** in 11 s, every suite listed by name, `SubtypeFactVisibilityTest`'s two deliberate Evrete warnings visible in place; committed as `[main 9616748] Write down what the report-only tests currently read` — 2 files changed, 79 insertions — and pushed `87a2bb9..9616748`.)*

**[Claude]** Landed as `9616748`. And the full output earns its keep this once: I could check every row of the table against a real run rather than against my own transcription.

All eight match, at the commit the table names:

```text
EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48)      ✓
drake surface losses: 15 fields across 91 types — 12 expression form, 3 spelling  ✓
Drake.parse scope: 81 draco + 10 mods types in, 0 held back                  ✓
PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies                    ✓
CANONICAL CHECK: 80 numbers, 7 differ from generated canonical               ✓
scenario in today's drake: 23 files, 0 rejected, 0 drifted, 23 clean         ✓
rete subtype visibility: CO-DECLARATION DROPS THE FACT                       ✓
531 tests / 41 suites                                                        ✓
```

And the one row I flagged as file-only — `DrakeGenTest`'s pending-`.drake` census — is indeed absent from stdout, printing only `- mods actors still lacking a .drake (report only)` with no number. So the caveat in the table is correct rather than defensive.

## On whether the full output is worth it

Directly: **this time yes, next time probably not.** The marginal information the full log carries over the nine grep lines is the suite and test names — which matter when something *fails*, and are noise when nothing does. Everything I actually checked above was in those nine lines.

So my honest read: full output when a test fails, when a gate changes shape (as this one did — the table needed proving), or when you want me to look for something you can't name precisely. The capture line otherwise. That isn't me economising on your account; it's that a 600-line paste and a 9-line paste tell me the same thing on a green run, and the 9-line one tells it faster.

The two Evrete `WARNING` lines in there are expected, incidentally — `SubtypeFactVisibilityTest` provokes them deliberately, and their presence is the finding.

Three commits landed this session: the transform domain going de jure, the scenario becoming a corpus that runs, and the baselines written down. Clean stopping point if you want one — say the word and I'll pull together the session-transition material.

---

**[Dev]** Can you tell me ahead of time, as you issue your command, that a full result prompt would be useful?

**[Claude]** Yes — and I should have been doing it. At the moment I write the command block I already know which it is, because I know what I changed and what I couldn't check offline. *(Wrote the rule into the `feedback_full_suite_before_push` memory note.)*

Yes. From now on the command block will say which, in one line. Three cases, and I can tell them apart when I write it:

**Full output** — a gate changed shape: a new suite, a new assertion, or a headline whose text or number I'm asserting for the first time. Also when I couldn't verify offline, or when I expect a specific number to move and want the context around it. The test is whether I'm about to *claim* something the run is the only proof of. The baseline table was exactly that — I needed to check it against a real run, not against my own transcription of one.

**Capture line** — a green-expected run after changes I verified offline, docs-only changes, re-runs. On a green run the 600-line paste and the 9-line paste tell me the same thing.

**On failure** — the failing suite's block plus the final `Tests:` tail, not the whole log. That one is self-signalling anyway: the command stops at `sbt test &&`, so you'll know before I do. What's missing at that point is the context around the failure, which the capture line doesn't carry.

Looking back at this session honestly: of the three gates I handed you, one genuinely warranted full output (the baseline table) and two didn't — the scenario-corpus commit I'd already verified end to end offline, and the docs-only one. I asked for nothing and got the full log twice, which was your call to make and it cost you.

I've written this into the `feedback_full_suite_before_push` memory note rather than just promising it, so it survives the session — the note already covers "full suite, not scoped, before push", and this is the same question one step later: what comes back.

---

*(Session in progress at time of transcription — chapter to be extended when the session closes.)*

## Status

This chapter transcribes the August 28–31, 2026 session, which is **still open at the time of transcription** — the last recorded exchange is August 31, 18:07 UTC, the day this chapter was written, and it ends not with a close-out but with a protocol commitment and Claude's standing offer of "the session-transition material" unanswered. The chapter will be extended when the session closes. **Opening state (carried from chapter 74's close):** `0af56fd`, 527 tests / 40 suites green, the forest scenario (22 definitions) parsing 22 clean but never generated or compiled, #51's two open tracks standing (`valueType` expressions — 34 applications, 7 arrows, still Scala text — and the `TypeParameter` flat increment, decided not built), #63 filed on the CO-DECLARATION verdict with its body correction (dispatch converts hierarchy into siblings, not routing) queued, `super` never projected, the standing conventions in force. **The session opened with an honest stumble, kept honest here:** Dev pasted the previous session's suggested prompt "without carefully reading it", Claude read the git-record/CHANGELOG convention line as a task and ran the session-transition ritual after one deliverable; Dev asked why, Claude owned the misreading and recommended the rollback, and Dev performed the delete and rollback himself ("so you can continue from here, as you would have"). **Commits landed so far:** **`83d87ac` "The forest projects, and a transform domain says so in drake"** (528/41) — the `ScenarioGenTest` gate; the cross-package import-ambiguity fix (`derivationRef` asks whether a bare name would *reach* the parent — a load probe); the codec guard dropped after Dev's design correction ("The purpose of derived unit types is to identify unit conversion formula expressions, not to structurally morph a type") reframed the four affected corpus types as unit types whose JSON is their parent's JSON — `Type`, `Meters`, `Radians`, `format/json/Value` regenerated; and the metamodel move at Dev's directive "Move source and target to domainAspect and add the drake keywords" — the two fields moved from `dracoAspect` (where nothing populated them) to `domainAspect`, `source`/`target` now drake keywords and globally reserved words, `Generator.isDomain`'s `(source && target)` clause no longer a "transitional fallback", transform-ness now aspect presence exactly as `05b14bd` made actor-ness (Dev's own `from Transform(Ash, Birch)` experiment measured first and shown to be structurally false — a transform domain is not a pair — and `from (S,T)` exposed as a hole: a *nameless* `TypeName` emitting `extends [S, T]`, round-tripping clean so no gate sees it). **`87a2bb9` "The scenario becomes a corpus, and the forest runs"** (531/41, 55 files) — after Dev's redirect ("The point of scenario.forest example is to establish consistent transform domains, transform types, and specifying the correct super-types for both, enough to generate and execute source code. We should stabilize that before we try to get closer to beta working on something else"): `super` projects (`actorKnowledge` walks the super-domain chain, cycle-guarded), Gate 3 rewritten to measure the emitted source rather than describe the gap, Gate 4 (each transform member one leaf conversion) verified by breaking it, Gate 5 executing the forest — 0.8 µM ash jasmonate crosses at × 0.62 arriving as 0.496 µM birch, 0.3 µM filtered by the condition, `BirchAlarmReceived` added to `Forest` as the required consumer; then **Dev's double catch** ("You are not generating any source code because you are not parsing the drake into json. Also, you are writing Scala value expressions instead of using drake expression syntax") converted the scenario from an in-memory fixture into a full trio corpus — 23 `.drake` + 23 `.json` + 23 `.scala` committed in the test tree per Dev's ruling ("we use src/test for new definitions that can later exist in main"), the gate shrinking from 1 report to 4 asserts, and the drake-expression rewrite immediately flushing two defects the Scala-writing habit had hidden (`Drake.emit`'s `unfoldChain` crashing on a niladic-call base; `Generator.actionBody` missing the `Local` case, silently dropping bindings) — host-opaque values 7 → 4, the remainder needing #58 (the `match`) and #61 (paths, infix operators). Two bugs were also found and fixed in the interim Gate 5's own machinery (the shared `KnowledgeService` classloader; the context loader restored only on success) before that machinery was deleted entirely. **`9616748` "Write down what the report-only tests currently read"** (2 files, 79 insertions) — the DRACO.md baselines table, at Dev's word, recording all eight report-only headlines as of `87a2bb9` with a verified nine-line capture command, a maintenance rule (the commit that legitimately moves a number updates the table in the same commit), the file-only `DrakeGenTest` row flagged, and the alpha.6 "16 surface-loss fields" deliberately left standing as the worked example of the drift the table exists to expose (the archaeology showed 15 has been the number since `324556c`). **Beta discussed and re-ordered:** no written definition of beta existed in the tree; Claude proposed one (the definition format has stopped moving, and every tier that ships is gated) with four priorities — freeze the metamodel (#51), gate the example tier (regenerate the 20 stale example domains, make `ExampleDomainsGenTest` assert — #62's substance), make `Drake.parse` an authoring path (#61), one falsifying Haskell projection — and cuts (#33–#36, #12/#13/#25, #22, `super`); Dev's redirect made stabilizing the forest scenario the priority instead, and the session did exactly that. Nothing was posted to any issue. **Test counts across the arc:** 527 (`0af56fd`) → 528/41 (`83d87ac`) → 531/41 (`87a2bb9`, the scenario's 4 asserts and 23 sources in the build) → 531/41 unchanged (`9616748`, by design). **Protocol established at the open end (written to the `feedback_full_suite_before_push` memory note, not just promised):** each command block will say up front whether it warrants the full result paste, the nine-line capture, or (on failure) the failing suite's block plus the tail — with the honest audit that of the session's three gates only the baseline table genuinely warranted full output. **Queued at the open end:** the offered session-transition material (not yet requested); the four remaining host-opaque scenario values (#58 for the actor `match`, #61 for paths and infix operators); the `(S,T)` nameless-TypeName hole (reject at parse or name the tuple head — adjacent to the `(,)` product-head proposal); #63's dispatch grammar proper; #51's `valueType` expressions and the `TypeParameter` flat increment; codec presence as a declared `codecAspect` field (the "real answer for later" behind the relaxed heuristic); the example-tier regeneration and gating (#62's expensive half); the closedness-declaration and tag-discrimination questions; Cedar/Dogwood and `Birch_Cedar` for the chaining case; and from earlier chapters, still standing — `AGENTS.md` as a diverged copy, the `TargetScala` rename explanation, the group-1 extraction after #61, the five `.namePath` workarounds. **Because the session is open, everything above is the state as of the last recorded exchange, not a close: no session-ending commit block has been issued, and this chapter's final exchange may not be the session's.**
