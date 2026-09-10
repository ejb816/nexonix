# Draco Dev Journal — Chapter 76

**Session date:** August 31 – September 9, 2026 (session in progress)
**Topic:** From "generate the source from the scenario JSON" — already done, the session discovers, at `87a2bb9` — to the drake→JSON gate closing the trio's unwitnessed leg, then the design dialogue that determines drake's match-case: Dev's corrections ("the Pattern does not apply to the match pattern", "a type and case branch to specific sibling subtypes") converging on `Case` as a twelfth BodyElement kind, the binder settled optional by the engine's own 44 dispatch sites (8 of 9 branches bind nothing), and Dev's forward uses named ("I may want to use case in the drake/generator domains…", `Base` as an upper ontology) → the Fable 5.1 model switch and OS update, the command-echo troubleshooting (both commands ran in the wrong directory; `error` added to the capture grep so a swallowed failure can't vanish again) → increment 1 exposing a principle violation Dev catches cold ("Seeing anything Scala or Java dependent in drake is significantly regressive…") and the Source/Carrier/Target grounding dialogue transcribed verbatim — Source and Target as roles, `ScalaTarget from draco Source` exposed as crossed, Dev's "Drake is the Source" refinement demoting JSON to bootstrap carrier, the operational rule *never author in a carrier what Drake cannot parse back*, and `draco.generator.carrier` coined and accepted — landing as `8239d08` → the `Case` kind in two engine builds (`606c1a6`, `12bed84`), `TypeName.equals` and `RootInterface` the first two case-branches, the smuggled Scala `match` gone from the tree → Dev's foundational assumption ("Let's assume there is only one definition language for draco and that is drake…") and the generator-domain structure it implies, the forest scenario judged "not complete or correct enough to serve as an example", the naming tightened twice (`draco.genscala.GenScala`; "Let's use draco.DracoGenerator for draco.Generator in src/mods" — the September 8 engine rename, `e3c96f8`) → the structure increment (`1cb3578`), target-side types GenDrake-first (`Surface`, `Emission`, `6f5a8bb`), the receiver put in the super-domain at Dev's word, two engine gaps flushed by first real use (the `targetBody` arm; a wildcard import silently outranking a package's own member), and **`GenDrake` runs** — 91 of 91 definitions crossing a transform domain through working memory and coming out as their own `.drake`, byte for byte (`4c977e5`, 592 tests / 42 suites) → tonight's still-open transforms dialogue: composition of target factory calls, and Dev's five-point refinement of `TypeDefinition -> LanguageTarget` (substitution strings in the target domain, neutral rules, the per-target binary, the endogenous-invariance requirement) with Claude's three self-identified misalignments queued as the next increment. Transcript.

---

**[Dev]** I want to start this session by having you continue by generating the source code from the json data emitted from the drake definitions in scenario.forest domains and types. Discuss this task with me before you do it.

**[Claude]** I'll look at the current state of the forest scenario before we discuss. *(Surveyed the scenario tree, the git log, the test suites and the working tree.)*

## This one already landed

The task as stated is done — commit `87a2bb9` ("The scenario becomes a corpus, and the forest runs"), yesterday's session. Working tree is clean.

All 23 scenario definitions are a full trio: `.drake` beside `.json` under `src/test/resources/scenario`, and generated Scala committed under `src/test/scala/scenario`. The three forest artifacts are there —

| type | aspects | projected |
|---|---|---|
| `scenario.forest.Forest` | domain | `Forest.scala` |
| `scenario.forest.AshBirchAlarm` | domain + rule | `AshBirchAlarm.scala` |
| `scenario.forest.BirchAlarmReceived` | domain + rule | `BirchAlarmReceived.scala` |

— and `ScenarioGenTest` asserts (not reports) that `Generator.generate` from each `.json` reproduces the committed `.scala`, that the actor accepts every rule its domain chain owns, that transform members are leaf conversions, and that the forest actually fires (0.8 µM crosses at ×0.62, 0.3 µM does not).

So either you've lost the thread across the session boundary, or you mean a next increment. Two things I'd flag before you choose:

## The gap I did find

**Nothing ties the committed scenario `.json` back to its `.drake`.** `ScenarioGenTest` walks `.json` only; `ScenarioDrakeTest` walks `.drake` only and checks `emit(parse(x)) ≡ x` — drake-internal. The `.drake → .json` parse was run once by hand for `87a2bb9` and never gated. Edit a scenario `.json` without touching its `.drake` and the whole suite stays green. `src/main` doesn't have this hole: `DrakeGenTest` pins `Drake.emit(X.json) ≡ X.drake` in the emit direction.

That's a ~15-line gate on the test that already walks the tree, and it's the literal completion of "generate the source from the JSON emitted from the drake" — right now the *emitted from* half is unwitnessed.

## The next real increment, if you want one instead

`ash/RootInterface.drake` smuggles its dispatch through as a single-line Scala `match`. `ScenarioDrakeTest`'s own header calls this out — it parses, and parsing it proves nothing, because the text projects to no target but Scala. Dispatch has no drake spelling. Birch also has no actor at all, so no message crosses an actor boundary anywhere in the scenario; gate 4 fires rules directly into one `Knowledge`.

**My recommendation: the `.drake → .json` gate first.** It's small, it closes a hole in work you just landed rather than opening a new front, and it's the thing that keeps the scenario corpus honest while the dispatch question gets worked. Say the word and I'll do it — or correct me if the forest work you had in mind is something else entirely.

---

**[Dev]** Proceed with your recommendation.

**[Claude]** Confirmed the shape. Writing the gate into `ScenarioGenTest` as a new gate 1, ahead of the projection gate. *(Read the emitter, `DrakeGenTest`, the scenario trio and the prior record; wrote the gate; updated DRACO.md's baseline row; wrote the CHANGELOG entry and `git-record-2026-08-31-1502` together.)*

Done. The gate is in, ahead of the projection gate.

## What changed

**`ScenarioGenTest.scala`** — a new gate 1, `parse(X.drake) == X.json` for each of the 23 scenario files. The existing four renumbered to 2–5, so the suite now reads in the order the trio is made: drake → JSON → Scala → running.

Three decisions worth naming:

- **Parse direction, not emit.** `src/main` pins `emit(X.json) ≡ X.drake` via `DrakeGenTest` — right there, because that corpus is JSON-first. The scenario is the other way round: `87a2bb9` made the JSON *by parsing the drake*. Gating the direction the artifacts were actually produced in is the one that means something.
- **JSON values, not text.** The files are canonical-printer output, so a text diff would pass today and would give a free line diff. Still wrong: the printer is a formatting choice, and pinning it makes one changed separator read as 23 regressions. Circe object equality is order-insensitive; both sides get logged through the corpus printer on failure so the expectation pastes straight into the file.
- **Raw file JSON, not decoded-then-re-encoded.** Incidentally closes a second hole — `definitions` builds its list with `.toOption`, so a `.json` that stops decoding silently *vanishes* from the projection gate instead of failing it. Every scenario `.json` has a `.drake`, so all 23 now go through a comparison a decode failure can't survive.

**DRACO.md** — baseline count 531 → 532 tests / 41 suites, same commit, per the rule the previous commit wrote down. None of the eight headline figures moves; the new gate asserts rather than reports.

**CHANGELOG** and **`git-record-2026-08-31-1502`** — written together, as required.

## The honest caveat

I can't run sbt, so **this gate has never been run.** The count arithmetic is safe (one `test` added to an existing suite). Whether the 23 files actually agree is a question only the run answers — and if they don't, the gate has done its job on first firing rather than being broken.

```bash
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|CO-DECLARATION|^\[info\] Tests:"
```

Expect 532 tests / 41 suites with every headline in the table unchanged. If it says 531 or 533, `DRACO.md`'s baseline row is what to correct.

If the new gate is **red**: paste just the `ScenarioGenTest` failure and the two logged blocks for the named file, not the whole run — the message names the file and the log prints both sides. If **green**:

```bash
sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-31-1502 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

---

**[Dev]** This is an unexpectedly succinct result:

*(Followed by the pasted nine-line capture: every headline exactly at baseline — gen map 28 / 20 / 0 / 0 of 48, PON 80 / 550 / 42, canonical 7, `drake surface losses: 15 fields across 91 types` (12 + 3), parse scope 81 + 10 with 0 held back, scenario 23 clean, `the forest runs: 1 of 2`, the CO-DECLARATION verdict — and `Tests: succeeded 532, failed 0`.)*

**[Claude]** Green on first firing — all 23 `.json` agree with the `.drake` they were made from, and the count landed exactly at 532.

Every headline matches the table:

| headline | table | run |
|---|---|---|
| example-domain gen map | 28 / 20 / 0 / 0 of 48 | ✓ |
| drake surface losses | 15 (12 + 3) | ✓ |
| Drake.parse scope | 81 + 10, 0 held back | ✓ |
| PON corpus | 80, 550, 42 | ✓ |
| canonical check | 80, 7 differ | ✓ |
| scenario in today's drake | 23, 0, 0, 23 clean | ✓ |
| forest runs | 1 of 2 | ✓ |
| rete subtype visibility | categorical | ✓ |

The succinctness is the grep doing its job, and the new gate is *supposed* to be invisible to it. It asserts, so it writes its count to `log` (the per-suite file) rather than `console` — no headline, nothing to catch off stdout. Its line is in `target/test-output/ScenarioGenTest.log`: `parsed 23 drake files, 0 differ from the .json committed beside them`. The one documented file-only row, `DrakeGenTest`'s pending-actors 0, is absent from stdout for the same reason.

The commit block is in my previous message — nothing has been committed or pushed from here. Let me note the gate structure in memory while you commit. *(Memory updated; the commit landed as `97d1f89`, confirmed later in the session.)*

**Two threads left open in this area**, both from the earlier discussion and neither touched:

1. `ash/RootInterface.drake` still carries its dispatch as a single-line Scala `match`. It parses, and parsing it proves nothing, because that text projects to no target but Scala. Dispatch has no drake spelling (#58 for the `match` form, #61 for paths and infix operators).
2. Birch has no actor, so no message crosses an actor membrane anywhere in the scenario — gate 5 fires rules directly into one `Knowledge`. The actor path is defined but unexercised.

Say the word if you want either next.

---

**[Dev]** Would now be a good time to determine the syntax of the drake equivalent of match case?

**[Claude]** Good question to ask before diving in. Let me get the evidence rather than speculate — the spec, the operator set, and every actual use site. *(Censused every `match` in both corpora; read `drake.dlt`'s expression sketch; read #58 and #61; read the Generator's `signal` emission.)*

Yes — but the question splits three ways, and only one third is ready. Here's what's actually in the corpus.

## "match case" is three different constructs

There are exactly three `match` sites across both corpora, and no two are the same thing:

| site | what it really is | needs |
|---|---|---|
| `RootInterface.drake` | dispatch on which variant of a sum arrived | **#58** — not an expression at all |
| `TypeName.drake` | the Scala equality protocol leaking | probably **no syntax** |
| `DefinitionPath.drake` | structural patterns with binding | a pattern sub-language — **blocked** |

**#58 already answers the first one**, and answers it better than "add a `case` expression" would: dispatch is ordered `Pattern` cases in the actor aspect, reusing `Pattern` verbatim (`variables` = typed binders, `conditions` = guards). Its argument is that an actor action is procedural, so ordering and first-match-wins come for free — no salience, no negation, neither of which the model has. It's labeled `next-feature`.

**The second one probably shouldn't get syntax at all.** The tell is `par other Any` — that's Scala's `equals(Any)` protocol, not something draco means. Haskell derives `Eq`; #37 already made `TypeName` identity structural. That's a language-specific residue for README's table, not a grammar feature. Worth confirming with you, since it's a deletion rather than an addition.

**The third one is the real value-level case**, and it's the one to leave alone.

## Why the general one waits on #61

Patterns aren't expressions, but they nest inside the expression grammar — and that grammar doesn't exist yet. `expr := app (opsym app)*` is a sketch in `drake.dlt`, not an implementation.

The sharper reason: `if` and `\` are **already specified with normative tree forms and still don't parse**. That's most of the `expression form (string vs tree): 12` loss you saw in today's run. Adding `case` to the spec now would make a fourth specified-but-unparsed construct — it would move that number the wrong way while looking like progress.

## Recommendation

**Determine the syntax, scope it to #58, and do `signal` first.** Three things make it the right size: the Generator still emits a frozen one-case `signal match { case PostStop => … }`, and generalizing a hardcoded 1-case match into an authored n-case one is **byte-identical for the current corpus**; Pekko signals are a genuine sum (`PostStop`, `Terminated`, `ChildFailed`), so a real second case has somewhere to come from rather than being invented; and it consumes `Action.variables`, which exists, serializes, and nothing reads.

There *is* genuine syntax work in it that doesn't touch #61 — the thing you flagged in the issue yourself: the same `Pattern` spelling would carry two semantics, an unordered RETE conjunction in a rule LHS versus an ordered test against one scrutinee in an actor case. That asymmetry needs a stated home in drake.dlt, and settling how the surface shows it is exactly the design question worth doing now.

Want me to work up the `signal` surface syntax against drake.dlt, or would you rather settle the LHS/scrutinee asymmetry first, since it constrains the spelling?

---

**[Dev]** The actor aspect has 3 separate executable blocks: start, message, signal. The only one where the match case equivalent needs to exist is message, and it is optional for that. Each of the three executable blocks is an Action, same as in Rule, but the Pattern does not apply to the match pattern, that has to be syntactically separate from the Rule Pattern. Also, for domains, transforms and transform domains, the full match case semantics is not necessary. All we need initially is a type and case branch to specific sibling subtypes, in order to separate the message type from the types that are inserted in working memory.

**[Claude]** Both corrections land — and the second dissolves the question I closed with, since separate constructs have no shared spelling to disambiguate. Let me read the actual shapes before proposing anything. *(Read `ActorAspect`, the `TypeElement` kind family, the scenario's actor and rule surfaces, and the reserved-word set in `Drake.scala`.)*

Corrections taken. Three things in my last message were wrong: `signal`-first is not the plan, `Pattern` is not the vehicle, and the LHS/scrutinee asymmetry I offered to settle doesn't exist once the constructs are separate. #58's body argues all three the other way — model-authored, and this is exactly the case where it gets re-examined rather than followed.

## The pieces already exist

Reading the metamodel rather than the issue:

```text
Action from BodyElement    variables [Variable] + body [BodyElement]
Variable from BodyElement  name + valueType
```

`Action` *is* a `BodyElement`, so an action already nests inside an action's body. And a `Variable` is already exactly "a type and a binding name" — the thing you said is all we need. So the construct isn't new machinery; it's a body element carrying one binder and a body.

## Proposal: `case`, a twelfth BodyElement kind

`RootInterface.drake` becomes:

```text
actor
  messageType AshSap
  start
    fix session org.evrete.api.StatefulSession knowledge.newStatefulSession parameters
  message
    case a AlarmSignal [
      mon session.insert parameters par Seq(a.infochemical): _* ]
    case d DroughtCue [
      mon session.insert parameters par Seq(d.infochemical): _* ]
    mon session.fire parameters
```

```json
{ "kind": "Case", "name": "a", "valueType": "AlarmSignal",
  "body": [ { "kind": "Monadic", "value": { "()": [ … ] } } ] }
```

`name`, `valueType` and `body` are all existing field names — no new vocabulary, and `Codec.sub` narrows it like the other eleven.

**It brackets itself**, which is forced rather than stylistic: `mon session.fire parameters` has to run after the dispatch, not inside the last branch, and whitespace can't say so. That's drake's third bracket rule doing its job — same as `dyn equals Boolean [ … ]`.

**A new kind rather than an `Action` with one variable**, because a `Case` has a scrutinee type and an `Action` has no notion of one. Reusing Action-with-one-variable would make the JSON unable to distinguish a branch from a plain nested block — role by container-and-arity instead of role by presence, and it would defeat exactly the negative-presence lint that's supposed to catch mis-modeling.

**Two things stay unauthored** because they're reconstructable: `case _ => ()` (a message matching no branch is a no-op) and the scrutinee name (`msg` is the message parameter, already fixed by `messageType`). On the Haskell test this reads directly as a `case … of` alternative — constructor plus binder, nothing left over.

The `Seq(…): _*` is still host-opaque. That's the pre-existing Evrete single-insert residue, not something the construct adds — I'd leave it visible rather than hide it.

## The blocker: `case` is not a free token

`Drake.scala` holds one **global, flat** reserved set — "a value runs until the next reserved keyword" is what whitespace-insignificance rests on. Reserving `case` terminates the value span in two pinned `src/main` files that currently contain the token inside host-opaque text: `TypeName.drake` (`other match { case tn: TypeName => … }`) and `DefinitionPath.drake` (`sources(rp) match { case Seq() => … }`). Both are gated per-file by `DrakeGenTest` and `DrakeParseTest`. So the token can't be reserved until they're converted.

**My recommendation: keep `case` and convert those two, rather than picking a token that dodges them.** Both conversions are improvements independent of this work: `DefinitionPath.source` is Scala extractor patterns, which will never project anywhere, and rewrites to the `if` operator drake already declares in Haskell form; `TypeName.equals` takes `par other Any` — Scala's equality protocol leaking — and I think it deletes rather than converts, but that's your call, not mine to assume.

The alternative is naming it `alt` or `cas`, which leaves both unprojectable values in the corpus permanently and buys a worse keyword forever to avoid a one-time conversion.

Two questions before I'd build any of it: is the binder always required, or optional when the branch body doesn't use it (I'd require it — one less inference, and the real site uses it); and do you want a gate asserting each `case` type derives `messageType`, which is checkable from the graph and is the direct analogue of the scenario's transform-consistency gate?

---

**[Dev]** None of the draco endogenous domains have an inheritance structure that needs the new case body element, so there will be actors that don't use even the limited match case semantics we are defining in drake. How does that affect your questions about the binder?

**[Claude]** It collapses the question rather than complicating it. Let me get the actual count first. *(Censused every actor aspect in both corpora.)*

**1 of 14 actors** would use it — `RootInterface`. The rest: `JSON` ×9 across the media chain, `World`, `DracoType` for Draco's self-validation actor, and three present-empty heads with no message type at all.

That number answers the binder question, and not by way of the binder.

**It makes "required" right for a reason that isn't evidence.** With one use site there's nothing to calibrate optionality against. Any rule I wrote about when a binder may be omitted would be confirmable only by round-tripping the one file that motivated it — and round-trip preserves mis-modeling by construction, which is the whole reason the second-projection test exists. So the choice has to be made on reversibility instead: required→optional is additive later and breaks nothing, optional→required invalidates authored files. Today the migration cost of guessing wrong in the safe direction is one file.

**The presence question was never inside the case.** It's whether `message` holds any `Case` at all, and the existing model already answers that with no new mechanism: a body of plain statements is an actor that doesn't dispatch. The 13 pay nothing — no flag, no empty block, nothing to author. That's where "optional for `message`" lives, and it's already free.

**And at the one site the binder isn't optional anyway** — the body reaches into the narrowed value (`a.infochemical`). Scala needs a name for that and so does Haskell. An optionality rule would be a rule about a branch shape that doesn't occur.

Two knock-ons:

The gate question shrinks the same way. A dedicated check that each `case` type derives `messageType` is machinery for n=1; it belongs folded into the scenario's existing consistency gate, or deferred until a second user appears.

More importantly, it changes the economics of the token. Reserving `case` costs two pinned `src/main` conversions — paid for a construct **no `src/main` type will ever use**. So those conversions have to stand on their own. I think both are worth doing regardless. But if you don't agree they're worth doing on their own merits, that's the signal to spend a worse keyword rather than to spend the corpus — and I'd want to hear that before touching either file.

---

**[Dev]** I may want to use case in the drake/generator domains when I am generating source code that has implemented data types. Also the draco.base.Base domain will evolve into an upper level ontology that is endogenous to draco, so there's that.

**[Claude]** That moves the ground under two of my three answers. Let me check what the engine already does today. *(Censused subtype dispatch in the hand-written engine.)*

That reverses my binder answer — and the calibration corpus I said didn't exist is the one you just pointed at. There are already **44 sibling-subtype dispatch sites** in the hand-written engine: 43 in `Generator`, one in `Drake`. They're blocked from being definition-backed by exactly this missing construct.

The canonical one is `Drake.keyword`, and it settles the question by counting: **8 of its 9 branches bind nothing.** `case _: Fixed => "fix"` never reaches into the narrowed value. Only the catch-all binds.

So: **binder optional, absent by default.** It costs no new mechanism — `TypeElement.name` already defaults to `""` since `b5ba152`, so an unbound branch is absence, not a flag. Requiring it would put a dead name in eight of nine branches at the site draco most wants to project.

```text
case Fixed [ = "fix" ]                      unbound — the generator shape
case a AlarmSignal [ mon session.insert … ] bound — the actor shape
```

Two things fall out that are worth having:

**A value-returning branch needs no new semantics.** `case Fixed [ = "fix" ]` uses the `= <expr>` result line that `dyn`-with-body already has. The actor shape is the same construct with statements and no result. One kind, both shapes.

**The token decision hardens.** I offered `alt`/`cas` as an escape from converting two pinned files. That's off the table — saddling draco's own projection domains with a worse keyword across 44 future sites to dodge two conversions is plainly the wrong trade. Reserve `case`; convert `DefinitionPath.source` to the `if` operator and settle `TypeName.equals`.

The gate question flips too, but less far: a lint that each `case` type derives the scrutinee's type earns its keep once `drake`/`generator` are authoring these, so it's worth building **when the second user lands** rather than now — and as a general lint over any `case`, not bolted onto the scenario gates.

Two shapes still sit outside the narrow form, and I'd leave both there: **dispatch on an expression's operator key** — `{op: [operands]}` branches on a *string*, not a subtype, so some of the 43 `Generator` sites are literal-pattern territory and the 43 is an upper bound; and **`DefinitionPath.source`'s `Seq()`/`Seq(url)`** — structural arity, which still goes to `if`, not to `case`.

`Base` as an upper ontology is the strongest argument of the three, since sibling-subtype dispatch is precisely the shape an ontology generates — but it's also the one I can't check against anything today, so I'm treating it as direction rather than as a constraint on the spelling.

If this reads right, the next step is to write the construct into `drake.dlt` — spec only, no parser and no corpus change, so nothing breaks and the token stays free until you want it. Want me to draft that entry?

---

**[Dev]** I like what you proposed for the case body element so I want to proceed with realizing it. If making it required in all actors is useful, then I can always do the single "case ashSap AshSap [... ]" to bind a variable name in case I didn't care about the sub-types, or come up with some "syntactic sugar" for drake, so I wouldn't have to do a single case explicitly.

**[Claude]** Let me check one thing that bears directly on "required in all actors" — where the scrutinee's name comes from today. *(Read the Generator's actor emission; wrote the spec into `drake.dlt` and tested it against the real endogenous sites.)*

`msg`, `ctx` and `signal` are all hardcoded in the Generator — they appear in no definition anywhere.

That decides it: **keep `case` optional.** Three reasons, and your own instinct is the second one:

1. Optional→required is the additive direction. Required→optional invalidates authored files. Same reversibility argument that just paid off on the binder.
2. The sugar for "I don't want to write a single case" *is* optionality — no desugaring step, no expansion rule. Mandating the construct and then adding sugar to elide it is strictly more machinery than not mandating it.
3. The one real gain from a mandatory wrapper is naming the scrutinee from the definition instead of inheriting the Generator's `msg`. But `msg`, `ctx` and `signal` are all equally magic, so that fixes one of three and leaves an asymmetry. It's a genuine residue and worth its own pass — not something to smuggle in as a side effect here.

Your `case ashSap AshSap [ … ]` stays fully available and meaningful — it's how you name the scrutinee when you want it. It's just never forced.

Now the spec entry. Testing the spec against the real endogenous sites found a hole in what I just wrote. `Drake.keyword`'s ninth branch is `case e => sys.error(…)` — a catch-all that **produces a value**. My "catch-all is never authored" rule is right for Unit branches and wrong for value-returning ones.

Spec is in. Four edits to `drake.dlt`, all documentation — nothing reads that file at build time, no corpus changed, the token still isn't reserved, so the tree stays green: a `CASE-BRANCH` section beside `DYN-WITH-BODY`; `BRACKETS` — *two* openers became **three**, since a case has no leaf form and always brackets; `*~case` under `*~message` in the template, marked optional; and the spec-only status plus the token prerequisite in `DIVERGENCES`, so a future session finds it.

**The defect was worth the check.** Fixing it produced the default branch, and the four forms turn out to disambiguate on capitalization alone — reusing the rule drake already has for bounding references, so nothing new is consulted:

```text
case a AlarmSignal [ … ]   binder + type          case e [ … ]   default, bound
case AlarmSignal [ … ]     type, unbound          case [ … ]     default, unbound
```

Whether a default is *needed* then falls out of the statements-or-result split rather than being its own rule: effect branches never need one, result branches always do unless exhaustive over the scrutinee's subtypes.

**That has a consequence for the plan.** `TypeName.equals` no longer needs the deletion decision I said was yours — it converts:

```text
dyn equals Boolean [
  parameters
    par other Any
  case tn TypeName [ = name == tn.name && namePackage == tn.namePackage && typeParameters == tn.typeParameters ]
  case [ = false ]
]
```

So freeing the token shrinks to `DefinitionPath.source` alone, and `TypeName` stops carrying a host-opaque value at all — which moves the surface-loss count the right way instead of the wrong one.

The catch: that's `case` in a **dyn body**, not an actor `message`, and I wrote the spec as `message`-only. Widening it is where the 43 `Generator` dispatch sites live and is what you named when you said the drake/generator domains would use this — so I'd widen it. But it changes what "initially" covers, so I'd rather you say yes than assume it.

Remaining increments, smallest first: **1** — free the token (`DefinitionPath.source` to the `if` operator, `TypeName.equals` to a case-branch); **2** — the `Case` kind (trio + `BodyElement.modules`, reserve the token, `Drake.emit`, `Generator`); **3** — `Drake.parse` and convert `RootInterface`.

Nothing is committed. Say the word on widening and I'll start on 1 — or if you'd rather bank the spec first, I'll write the record and CHANGELOG for it as its own commit.

---

**[Dev]** I'm switching to Fable 5.1 in this prompt. The MacOs has been updated on this device since that last time worked in this session. Let me know what the focus should be going forward in this session, since it's been days on my time frame since the latest prompt was responded to in this session.

**[Claude]** Let me check where the tree actually stands before saying anything — days have passed and things may have moved. *(Checked git status and log; wrote `git-record-2026-09-06-2215` and the CHANGELOG entry for the uncommitted spec.)*

Ready to bank. Where things stand and what I'd focus on:

## State

- `97d1f89` — the drake→JSON gate — landed. Suite was 532 / 41 at that point.
- **Uncommitted:** the `case` construct spec in `drake.dlt`, plus the record and CHANGELOG entry I just wrote for it. Spec only — no kind, no reserved word, no emit/parse. Nothing reads the file at build time, so the tree is green as-is.

## Focus for this session

**Realize the `case` body element.** The design converged before the gap; what's left is building it, smallest increment first:

1. **Bank the spec** — the block below. Also your first suite run since the OS update, so it doubles as the toolchain check.
2. **Free the token** — `DefinitionPath.source` to the `if` operator; `TypeName.equals` to a case-branch with a default. Two pinned files, held by `DrakeGenTest`/`DrakeParseTest`.
3. **The `Case` kind** — trio, `BodyElement.modules`, reserve `case`, `Drake.emit`, `Generator`.
4. **`Drake.parse`**, and `RootInterface` converts — the site that started this.

## The one decision

Increment 2 puts a `case` in a **`dyn` body**, and the spec as written admits it only in an actor's `message`. **I recommend widening it** — it's where the 44 engine dispatch sites live, and it's the use you named for the drake/generator domains. A yes is all I need; the spec line changes in increment 2's commit, not this one.

## Command block

Paste only the capture lines. Expected: 532 / 41, every headline as DRACO.md's table has it — anything off is the environment, not this commit.

```bash
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|CO-DECLARATION|^\[info\] Tests:"
```

If green:

```bash
sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-06-2215 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

---

**[Dev]** I can't see that anything useful executed. The two commands just echoed in terminal.

**[Claude]** The grep in that pipeline swallows everything that isn't a headline — including "command not found" — so if the toolchain broke in the OS update, you'd see exactly nothing. The `tee` captured whatever actually happened, though. Let me look at that and probe the toolchain, without running sbt. *(Read `/tmp/sbt-test.log`, probed `sbt`/`java`, checked git state — and found the cause; widened the capture grep in DRACO.md.)*

Nothing broke and nothing was committed — the toolchain is fine. Both commands ran in the wrong directory.

Both blocks again, anchored to the repo this time. Expected: 532 / 41, every headline as the table has it — and now any `[error]` line shows up instead of vanishing.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-06-2215 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

The commit now also carries the capture-line fix in DRACO.md — a swallowed error is exactly what you just hit, and the line in the operating rules shouldn't be able to do that.

---

**[Dev]** First command only:

*(Followed by the pasted capture: 532 tests, 0 failed, every headline at baseline — plus one new line, `[info] - Report compilation errors`, caught by the widened grep.)*

**[Claude]** Green, and the toolchain survived the OS update — 532 / 0 failed, every headline exactly as the table has it.

One line is new and harmless: `[info] - Report compilation errors` is a test *name* caught by the wider grep, not an error. That's the price of letting real errors through, and it's cheap.

Commit block:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-06-2215 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

Once that's in, the only thing between us and increment 1 is your yes on admitting `case` in a `dyn` body.

*(Delegated action — Dev ran the block and pasted the result: `[main 94b35cc] drake: the case body element, specified and not yet built` — 4 files changed, 136 insertions, 5 deletions — pushed `97d1f89..94b35cc`.)* Banked — `94b35cc`, pushed.

The spec is now a fact in the tree. Increment 1 — freeing the token — is ready to start, and it's the one that needs your call: `TypeName.equals` converts to a case-branch inside a **`dyn` body**, which the spec currently admits only in an actor's `message`.

I recommend widening it. Yes, or a different answer?

---

**[Dev]** Yes, widen it and proceed with increment 1.

**[Claude]** Widening noted. Before touching files, one re-scoping I have to state plainly: **`TypeName.equals` can't convert in increment 1.** A case-branch needs the `Case` kind to exist — decoder, emitter, Generator, parser — or the JSON can't even load. So it rides with increment 2 as that kind's first `src/main` use, and increment 1 is `DefinitionPath.source` plus the spec line. Let me read the trio and the `if` exemplar. *(Worked out how to run `DrakeCLI` and `GeneratorCLI` without sbt — `java -cp` over the tree's compiled classes plus sbt's exported dependency classpath — converted `DefinitionPath.source`, regenerated its drake and Scala through the CLIs, ran `check` over the whole `src/main` corpus; updated DRACO.md's baseline; wrote two memory notes, the CHANGELOG entry and record `2026-09-06-2230`.)*

Increment 1 is done and ready for your run.

## What changed

| file | change |
|---|---|
| `DefinitionPath.json` | `source`: the Scala `match` → a `loc` binding the candidates + a nested `if` tree |
| `DefinitionPath.drake` / `.scala` | **emitted and generated, not hand-typed** — `DrakeCLI` and `GeneratorCLI` run with `java -cp` over the compiled classes and sbt's exported classpath; `check` over the whole `src/main` corpus reports 0 surface / 0 JSON drift |
| `drake.dlt` | `case` admitted in a dyn-with-body; `DIVERGENCES` corrected — `TypeName.equals` converts, it isn't dropped |
| DRACO.md | surface-losses baseline **15 → 16** (expression form 12 → 13), with a note on why a rising count can be the corpus improving |
| CHANGELOG, record `2026-09-06-2230` | written |

The projection is a two-statement method, `sources` still called exactly once — that mattered, since it probes every root by opening a stream.

## The number to watch

`DrakeCLI parse` on the new file shows the `loc` coming back as a tree and the `if` as a string — the parser reads the application surface and not yet `if` (#61). So one previously-opaque value became a normative tree the parser can't reproduce, and the loss report counts that against the parser. Expected line:

`drake surface losses: 16 fields across 91 types [expression form (string vs tree): 13] [empty-collection spelling: 3]`

If it reads anything else, stop — the table and the record both predicted 16/13, and a different number means the prediction was wrong, not the table.

Two memory notes saved: the `java -cp` route for byte-exact CLI output without sbt, and the `case` track's decisions and build order.

---

**[Dev]** Seeing anything Scala or Java dependent in drake is significantly regressive with regard to the principle of programming language neutrality for the draco framework definition language. Also, DefinitionPath should be in the Source (vs Target)  part of the Generator domain,, not in draco.Draco, supporting programming language neutrality of the definition language.

**[Claude]** Both points land, and the first one indicts my own framing: the record and CHANGELOG call this "leaving the host-opaque tail," but the tree I wrote is `None` / `Some` / `.isEmpty` / `.size` / `.head` / `sys.error` — Scala vocabulary as atoms inside a drake form. That is not less host-bound than the `match` was. It is host-bound in tree shape. Let me ground the second point in the tree before proposing anything. *(Read the generator domain, the `Source`/`SourceContent`/`ContentSink`/`TypeLoader` cluster, every reference to `DefinitionPath`, and README's residues table.)*

Both points hold, and the second one explains the first. Let me put what I found next to it.

## What my increment actually is

The record says `DefinitionPath.source` "left the host-opaque tail." It didn't. The atoms in that tree are Scala vocabulary in a drake form. The `match` was Scala in a string; this is Scala in a tree. The token is freed either way, but the framing was wrong and **the uncommitted state should not go in as written.**

The deeper reason it reads as regressive is that it sits in **root `draco`**, where the definition language lives and where host vocabulary has no business. That's your second point.

## What the tree says about the second point

- **`draco.generator` is one element** — `generator (TypeDefinition -> String)` — with no source/target split. The split doesn't exist yet in the corpus.
- **`draco.Source` already exists and means the opposite thing.** It's an empty marker from the original domain unification, and its only use is `ScalaTarget from draco Source` — the *output* side. So the word "Source" is currently taken by the side you're calling Target. Flagging that once before it hardens into a package name.
- **`DefinitionPath` is one of a cluster.** Root `draco` also holds `SourceContent` (file reading), `ContentSink` (file writing), and `TypeLoader` (definition reading) — all host realization, all listed as members of the definition-language domain. You named one; the principle covers the others.
- **`TypeLoader` is the hard one.** It's definition-backed, it names `DefinitionPath`, and every generated companion in every domain calls it. Moving it drags a dependency on the generator domain into the whole corpus. That's a real question, but a bigger one than this increment.

## Recommendation

**Increment 1 becomes the move, with the conversion riding along.** `DefinitionPath` goes to a source side of the generator domain — declared the way `draco.format.json` already is (a domain type in a nested package deriving the parent's). The `loc` + `if` conversion goes with it unchanged, host atoms and all, because on that side host realization is the *content*, not a leak — `hostRoots` is literally the Java classpath and always was. `TypeLoader` stays in root for now and names it qualified, and the record says plainly that `TypeLoader`, `SourceContent`, `ContentSink` are the same question, deferred.

**One question before I build it:** the domain type for that source side would naturally be `Source` — colliding by name with the existing `draco.Source` marker. Is that marker stale (it predates the target concept and has one deriver), so it retires and `ScalaTarget` gets re-pointed in a later step — or should the new part take a different name?

---

**[Dev]** The use of Source and Target needs to be more conceptually grounded in the draco framework.  Drake is a Domain and is a Source. ScalaTarget is a Domain, is a Target, and is a type parameter of Generator, Generator(ScalaTarget) is a domain that consists of morphisms (in the form of rules) that converts type definitions intp Scala source code. This is not adequately grounded consistently (yet) in the Draco framework.

**[Claude]** This reframes the question from "where does a file go" to "what are Source and Target *in draco*" — and the corpus, as it stands, has the roles crossed: `ScalaTarget from draco Source`. Let me read exactly what exists before I propose the grounding. *(Checked for a `draco.Target`; reread the transform-domain precedent.)*

Understood, and it changes the order of work. Restating the grounding in draco's own terms so you can correct it before I build on it:

1. **Source and Target are roles.** A domain *is a Source* or *is a Target* by derivation — `X from draco Source` — the same way `Holon` and `Primal` are roles today. A Source is where definitions come from; a Target is what they project into.
2. **`TypeDefinition` is the neutral middle** both roles speak. Language neutrality of the definition language is exactly the claim that nothing in root `draco` is a Source or a Target.
3. **`Generator(L)` is parameterized by a Target**, and `Generator(ScalaTarget)` is a domain whose members are rules, each a morphism from a definition to Scala source.

## The corpus against it

| | today | under the grounding |
|---|---|---|
| `draco.Source` | empty marker in root, from the first unification | **right** — it's the role; it stays |
| `draco.Target` | does not exist | missing |
| `ScalaTarget` | `from draco Source` | **crossed** — `from draco Target` |
| `Drake` | derives no role; carries `generator = Drake.emit` | `from draco Source`; the emit face is `Generator(Drake)`, not Drake itself |
| `Generator(L)` | one element, a function | a domain of rules — the six ways the hand-written engine already dispatches *are* those rules, unwritten |
| `DefinitionPath`, `TypeLoader`, `SourceContent` | members of root `draco` | the Source side — how definitions get *into* `TypeDefinition` form |
| `ContentSink` | root `draco` | the Target side |

So the name collision I asked about was the wrong question. `draco.Source` isn't stale — `ScalaTarget` deriving it is the bug.

## One thing the grounding reconciles

Draco now has two spellings for "this domain converts X to Y": the transform domain's `source` / `target` keywords (`Ash_Birch`, landed two weeks ago) and the type parameter (`Generator(L)`). I think they're both right, for different shapes. `Ash_Birch` is *pairwise* — one source, one target. `Generator(L)` is **target-parametric and source-universal**: any Source that yields a `TypeDefinition` feeds it. That's precisely why it carries a type parameter and no `source` keyword, and it's why `DefinitionPath` belongs on the generator's source side rather than inside `Drake` — loading definitions from roots is source-universal too, not a Drake concern.

## Recommendation

**Ground the roles first; move `DefinitionPath` second.** Increment A is small and unambiguously right: `draco.Target` — a new trio, an empty role marker like `Source`; `ScalaTarget from draco Target` and `Drake from draco Source` — two derivation edges; README's domain table gets the grounding in a sentence. Nothing else moves. `Drake`'s `generator = Drake.emit` global stays untouched for now — whether Drake is *also* a Target (round-trip emission as `Generator(Drake)`) is a real question, but not one increment A has to answer.

**The uncommitted `DefinitionPath` conversion stays parked and must not be committed as-is.** It rides with the move in increment B, into the source side of the generator domain, where its host atoms are the content rather than a leak — and the record gets rewritten to say that.

**The one thing I need from you:** the source side of the generator domain needs a name, and `Source` is now taken by the role. On the `draco.format.json` precedent it's a sub-domain — a lowercase package under `generator` with a domain type deriving `Generator`. What's it called?

---

**[Dev]** Drake is the Source, in that it can be parsed to create the normative data format, which is currently json. Once Drake is complete and stable, any format that can normatively  equivalent to json (xml, yaml?, protobuf?) can be alternatively generated by parsing from Drake. I see no requirement for selecting a particular format to itself be normative to the framework, just that the format be consistently applied through maintaining the Drake <-> Format round-trip validation.  Source itself, whatever form it takes (domain or type hierarchy) is what has to determine the definitive and the normative structure of the framework, in a programming language independent way. Please feel free to disagree and/or clarify.

**[Claude]** The refinement is sharper than my table, and it has a direct consequence for the parked work that I want to test rather than assert. Let me run the experiment first, then respond to the whole thing. *(Redid the `DefinitionPath` conversion drake-first through the offline CLIs; restored the baseline row; rewrote the record and CHANGELOG entry.)*

Working tree is honest again: the trio is drake-first, the baseline row is back to the committed 15 / 12, and the record and CHANGELOG describe what's actually there. The record's header says *do not run the commit block yet*.

Now the substance. I agree with the grounding, and it caught a mistake of mine. Three clarifications, one of which is a mild disagreement.

## What your grounding exposed

Under "Drake is the Source," the rule that falls out is: **never author in a carrier what Drake cannot parse back.** My first conversion did exactly that — a `loc` plus an `if` tree, written straight into JSON, which Drake reads back as a string (#61). The surface-loss count rose 15 → 16 and my record called that "the corpus improving." It was the opposite: JSON getting *more* normative, not less. The 15 existing loss fields are the standing violations of that rule; I nearly added a sixteenth.

So I redid it the other way round — authored the drake, derived the JSON by parsing it, generated the Scala from that:

```text
loc found [URL] sources parameters par resourcePath
mon require parameters
  par found.size <= 1
  par s"draco: $resourcePath is defined at ${found.size} roots, …" + found.mkString(", ")
= found.headOption
```

Every value is on the application surface Drake parses today. `emit(parse(drake))` reproduces the drake with zero drift; `check` over all of `src/main` is 0 / 0; the projection calls `sources` once and has the same three outcomes. The loss count doesn't move. **That order — surface first, carrier derived — is what "Drake is the Source" means operationally**, and it's the order every future conversion should follow.

## Three clarifications

**Normative is not bootstrap.** I agree no format needs to be normative *to the framework*. But some carrier has to be readable before anything can parse, and today that's JSON — `TypeLoader` reads it, and the metamodel's own definitions are loaded through it before the first drake file is touched. Its honest name is *bootstrap carrier*, not normative. DRACO.md's `X.json (normative)` line becomes wrong under your grounding and will need to change. And the migration point has a number: **the surface-loss baseline is literally the distance between Drake-as-Source in principle and Drake-as-Source in fact.** At 0, the loader can read Drake, or any carrier, and JSON stops being special.

**Round-trip validates carriers, not content** — this is your own standing principle, and it's the mild disagreement. Drake ↔ Format round-trip is exactly the right test for "is XML a faithful carrier?" It says nothing about whether the content is *right*; a mis-modeling round-trips perfectly. What validates content is projection to something that has to compile and run — the scenario's five gates. Two different jobs. I agree round-trip is all a *format* needs; the second job still stands beside it.

**"Whatever form Source takes — domain or type hierarchy" — it's both, and half of it exists.** `draco.drake.Drake` is the domain, with no members and one global. The `TypeElement` / `BodyElement` kind family is already the type hierarchy the grammar's keyword blocks map onto, one keyword per kind. What is *not* in the framework is that mapping and the rules around it: `Drake.keyword` is Scala, and brackets and presence are prose in `drake.dlt`. For Source to determine the normative structure in a language-independent way, those have to become definitions — members of the Drake domain. That's the gap the grounding names, and it's the same gap the 44 engine dispatch sites and the `case` element are already pointing at.

## Where that leaves the increments

- **Increment A** (roles): `draco.Target` as a marker; `ScalaTarget from draco Target`; `Drake from draco Source`. Unchanged, still first.
- **Increment B** (the move): `DefinitionPath` to the generator domain's source side, carrying this drake-first conversion. **Still needs the name** — `Source` is the role now, so the sub-domain under `generator` can't be that.

Whether the conversion commits on its own first, in root, or waits to ride with B is your call; the record is written for the first and says so.

---

**[Dev]** Proceed with increment A first, then name the source side.

**[Claude]** Increment A. Drake-first for every artifact: author the surface, derive JSON by parsing, generate the Scala. Reading the pieces I'll touch. *(Authored `Target.drake`, edited `ScalaTarget.drake` and `Drake.drake`, added `Target` to `Draco`'s member list — each JSON derived by parsing its drake through the offline CLI, each Scala generated from that; updated README, CHANGELOG, DRACO.md's baselines; wrote record `2026-09-07-1030` and the `feedback_drake_first_authoring` memory note.)*

Increment A is in the working tree, drake-first throughout, and the check over `src/main` is clean (the only surface drift is the two files deliberately ahead of the emitter).

| file | change |
|---|---|
| `draco/Target` | new trio — an empty role marker, the twin of `Source`; byte-identical shape |
| `ScalaTarget.drake` | `from draco Target` (was `Source` — the crossed role) |
| `Drake.drake` | `from draco Source` (derived nothing) |
| `Draco` trio | `Target` joins the root domain's member list |
| README | roles in the domain table, plus the pairwise-vs-source-universal reconciliation |
| DRACO.md | 537 tests; 82 draco types in scope; 92 measured |

Every JSON was derived by parsing its drake; every Scala by generating from that. Each diff is exactly one line.

**The commit is path-scoped**, because the tree also holds increment B's files — the `DefinitionPath` trio, `drake.dlt`, and the earlier record — which must not go in. The IDE auto-adds new files, so the pathspec is on the commit itself, and the record's block already has it: `git commit -F /tmp/msg -- <the sixteen A-only paths> && git push`.

Expected: 537 / 41, `15 fields across 92 types` (still 12 + 3), `82 draco + 10 mods`, all else unchanged. Paste the capture lines.

## Naming the source side

**Recommendation: `draco.generator.carrier`, domain type `Carrier`.**

What that side *is*, concretely, is everything about the format a definition travels in between Drake and the loader: where carriers are found (`DefinitionPath`), how their bytes are read (`SourceContent`), how they decode (`TypeLoader`). Your grounding named the concept without naming it — "any format that can be normatively equivalent … maintaining the Drake ↔ Format round-trip." *Carrier* is that format, seen from the generator's side. It's distinct from `Source` (the role Drake holds), from `draco.format` (payload formats a message travels in), and from any one format.

Two I rejected: `source` — the word is now the role, and a package named for a role its contents don't hold would be exactly the confusion `ScalaTarget from Source` was. `loader` — it names the mechanism, collides with `TypeLoader` inside it, and says nothing about *why* those three types belong together.

One structural point that makes B small: this sub-domain is *contained* under `generator`, not derived from `Generator(L)`. It's target-independent, so it has no `L` to instantiate — and containment is not inheritance, which is your own correction from the forest scenario. So `Carrier`'s domain aspect self-loops and its `types` list holds `DefinitionPath`; no derivation edge, no parameter.

The name is a coinage of mine, so treat it as one — accept, or give me the word you'd use.

---

**[Dev]** Carrier is fine, proceed with increment B.

**[Claude]** Before touching anything: B changes `Draco`'s trio, `CHANGELOG.md` and `DRACO.md`, which are all in A's path-scoped commit. So whether A has landed decides how I sequence this. *(Checked `git log` — HEAD still `94b35cc`, A never committed; built `Carrier` drake-first, moved the `DefinitionPath` trio, requalified `TypeLoader`'s reference, updated `Draco`, both docs and the baselines; deleted the two superseded records and wrote one record, `2026-09-07-1108`, covering A and B together.)*

Increment B is in. A and B are one commit now, one record; the two earlier records are gone.

## What landed in the tree

| | change |
|---|---|
| **`draco.generator.carrier.Carrier`** | new sub-domain, drake-first; contained under `generator`, no derivation edge, `types [ DefinitionPath ]` |
| **`DefinitionPath`** | moved into it — three files deleted at root, three created under `generator/carrier`; carries the drake-first `require`/`headOption` conversion |
| **`TypeLoader`** trio | names it by its new package, in the drake, the JSON, and the projection |
| **`Draco`** trio | `DefinitionPath` out of the root member list; `Target` in |
| **`Target`** trio, `ScalaTarget`, `Drake` | increment A, unchanged |
| `drake.dlt` | `case` admitted in a dyn body |
| README, DRACO.md | the roles, the carrier side, the loader chain, the project-structure listing; baselines 542 / 83 / 93 |

Every JSON was derived by parsing its drake; every Scala generated from that. The round-trip check over all of `src/main` is 0 JSON drift, with the only surface drift the two files deliberately ahead of the emitter. No code outside the moved trio names `DefinitionPath` except the qualified reference in `TypeLoader`.

**Why one commit:** B edits `Draco`, `CHANGELOG.md` and `DRACO.md`, all in A's pathspec. Landing A alone would have committed a `Draco` that no longer lists `DefinitionPath` while `DefinitionPath` still sat in root — a lying intermediate tree. So **A's earlier block is void; do not run it.** The record for `2026-09-07-1108` supersedes both earlier ones and uses a plain `git add -A`.

## Run

Expected: 542 / 41, `15 fields across 93 types` (still 12 + 3), `83 draco + 10 mods`, every loss figure unchanged. Paste the capture lines.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-07-1108 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

One thing the run can catch that the CLI can't: the compiled classes I generated against didn't know `Carrier` or `Target` existed until I dropped their JSON in, so if any qualification came out differently than a real compile would produce it, `DracoGenTest` names the file and it's a one-line fix. Next after this is the `Case` kind itself, with `TypeName.equals` as its first use.

---

**[Dev]** Did you account for the fact that I had (inadvertently) elided your second suggested command (ending with "git push") in your previous response?

**[Claude]** Yes. The first thing I did when you said "proceed with increment B" was check `git log`, and HEAD was still `94b35cc` — A had never been committed. That's exactly why A and B became one commit: B had to edit `Draco`, `CHANGELOG.md` and `DRACO.md`, all in A's pathspec, so landing A afterwards on its own was no longer possible without a lying intermediate tree.

Nothing from A was lost. The working tree holds all of it — `Target`, the two derivation edges, `Target` in the root domain — alongside B, and the `1108` record describes both and supersedes the two earlier ones, which I deleted.

One consequence worth naming: A's test run was never pasted either, so the block in my last message is the **first** verification of either increment. The expectation stands at 542 / 41 with the two type counts at 83 and 93. If that run is green, the single commit block that follows it lands everything.

---

**[Dev]** Both command results, this time:

*(Followed by both pasted outputs: 542 tests / 0 failed, `15 fields across 93 types` (12 + 3), `83 draco + 10 mods`, every other headline at baseline; committed as `[main 8239d08] Source and Target are roles, and DefinitionPath moves to the generator's carrier side` — 26 files changed, 312 insertions, 45 deletions, git recording the `DefinitionPath` move as a rename — and pushed `94b35cc..8239d08`.)*

**[Claude]** Green, and every predicted number landed exactly — 542 tests, 93 measured, 83 in scope, both loss buckets unchanged, every other headline as the table has it. Committed and pushed as `8239d08`; git even saw the move as a rename.

Where the track stands after `8239d08`:

| increment | status |
|---|---|
| spec — `case` in `drake.dlt` | `94b35cc` |
| Source / Target roles; `draco.Target` | `8239d08` |
| `draco.generator.carrier`; `DefinitionPath` moved, drake-first | `8239d08` |
| **the `Case` kind** — trio, `BodyElement.modules`, reserve the token, `Drake.emit`, Generator; `TypeName.equals` as first use | next |
| `Drake.parse` for `case`; `ash/RootInterface` converts | after that |

The next one is the first that touches the engine rather than the corpus: a twelfth `TypeElement` kind means a decoder case, an emitter keyword, a Generator projection for both shapes (statements and result), and reserving `case` — which is safe now, since `DefinitionPath` no longer carries the token and `TypeName.equals` converts in the same step. The compiled classes I've been generating against won't know the new kind until you've compiled once, so that increment will need a build partway through rather than only at the end. I'll plan it around that when you say go.

---

**[Dev]** Proceed with the Case kind increment.

**[Claude]** This one is engine work, so I'm staging it in two builds rather than hand-authoring JSON and Scala I can't verify:

- **3a** — the `Case` kind (trio + `BodyElement.modules`), `Drake.emit` and Generator support, and one spec clarification the build will need. Nothing uses `case` yet; the token stays free. Verified by compile plus new per-type tests.
- **3b** — with fresh classes: reserve the token, add parse, and convert `TypeName.equals` drake-first, byte-exact.

Reading what I'll build on. *(A long pass over the family file, the emitter's opener/bracket machinery, the parser's statement handling, `actionBody`, the per-type test loops and the CLIs; authored the `Case` trio drake-first, regenerated `TypeElement.scala` via `generate-multi`, wrote the emit, parse and projection arms, updated `drake.dlt`, DRACO.md and CHANGELOG, wrote record `2026-09-07-1124`; verified the corpus round-trip unchanged on the old classes.)*

Increment 3a is in the tree. This is the first build in the track that touches the engine, so the run's job is different from the last three: **it's a compile check**, and the corpus is unchanged.

## What's in

| layer | change |
|---|---|
| **kind** | `draco/Case from BodyElement` — `name` (optional binder), `valueType` (optional type; absent = default branch), `body`, `value`. Drake-first, round-trips. |
| **family** | `BodyElement.modules` gets `Case`; `TypeElement.scala` regenerated by `generate-multi` in the test's own order — encoder arm, decoder arm, companion all came out of `modules`. Diff is pure addition. |
| **emit** | `case` is a third opener; brackets itself; header carries binder and type only when present. |
| **parse** | `case` admitted in a dyn body and in `message` only (`start`/`signal` receive nothing to dispatch on). Up to two tokens before `[`, upper-case is the type, lower-case the binder. |
| **project** | a run of consecutive branches is one `match`; scrutinee is the dyn's single parameter or `msg`; effect runs get `case _ => ()` when no default, result runs get none; a case in a rule action or factory body is a named error, not a silent fall-through. |
| **spec / docs** | scrutinee rule and consecutive-cases rule in `drake.dlt`; twelve kinds in DRACO.md; baselines 545 / 84 / 94. |

**The token is not reserved yet**, on purpose: `TypeName.equals` still carries `case` inside opaque text, and reserving it now would break that file's round-trip. 3b reserves it in the same commit that converts `TypeName` — the last such site — so the corpus never sits with the word reserved and still opaque somewhere. That's also why nothing exercises the new emit/parse/project code in this build.

## Run

Expected: 545 / 41 — three per-type tests for `Case`, `15 fields across 94 types` still 12 + 3, `84 draco + 10 mods`. **A compile error is the likely failure mode here and the grep passes `[error]` lines through — paste the whole error if there is one**, not just the capture lines.

*(Delegated action — Dev pasted both results: 545 tests / 0 failed, every headline exactly as predicted; committed as `[main 606c1a6] Case: a twelfth TypeElement kind, carried by the engine, authored by nothing yet` — 15 files changed, 387 insertions, 43 deletions — and pushed `8239d08..606c1a6`.)*

Compiled first time, 545 exactly, `606c1a6`. Now 3b, and the compile changed one thing about how I can work: the fresh classes know `Case` for emit and generate, but their parser doesn't reserve `case` yet, so I can't derive the JSON by parsing this time. So for this one increment the JSON is authored by hand to the spec and the drake and Scala are *emitted and generated from it* — then the build's `DrakeParseTest` is what proves parse reproduces it.

One thing I have to settle before reserving the token: every place the word appears as a token in any drake file, not just the two I know about. *(A `grep -w case` over every corpus found precisely the two known files; converted both, reserved the token, and found and fixed a missing `Case` arm in `Generator.targetBody` — a sealed-match gap that compiled as a warning and would have crashed on first use.)*

Increment 3b is in the tree. Two things about this one are different from every previous handover, and the record says both.

## What's in

| | change |
|---|---|
| **`case` reserved** | `Drake.scala` — the last opaque carrier of the word converts in the same commit |
| **`TypeName.equals`** | two result branches, `case tn TypeName [ = … ]` and a default `case [ = false ]`; the first `match` the engine ever writes from a definition |
| **`RootInterface`** | two effect branches on `AlarmSignal` / `DroughtCue`, then the fire — the site that started this; the target now supplies the `case _ => ()` |
| **`Generator.targetBody`** | the missing `Case` arm — a sealed-match gap that compiled as a warning and crashed on the first definition to carry one |

Both drakes are `Drake.emit` output and read exactly as the spec wrote them. The `grep -w case` over every corpus found precisely these two files, so the tree never sits with the token reserved and still opaque somewhere. That's also why the old "increment 4" folded in: reserving breaks *any* opaque `case`, and `RootInterface` was the other one.

## What's predicted, not generated

The fresh classes emit and project a `Case` but don't reserve the token yet, and the `targetBody` crash means the generator can't run on either file until it's recompiled. So: the two `.json` are hand-authored to the spec; the two `.drake` are emitted from them, byte-exact; the two `.scala` are **hand-spliced from the projection rules I wrote in 3a**.

The gates that check the predictions: `DracoGenTest` per-type for `TypeName`; the scenario's gate 2 for `RootInterface`; and the parser's `case` arm gets its first real exercise in `DrakeParseTest` and the scenario's gate 1. Those four are the point of the run.

Expected: 545 / 41, every headline unchanged — surface losses stay 15 across 94, since `equals` was an opaque string before and its branch values are strings now.

*(Delegated action — Dev pasted both results: 545 tests / 0 failed, every headline unchanged; committed as `[main 12bed84] case is reserved, and TypeName.equals and RootInterface are the first two case-branches` — 10 files changed, 177 insertions, 7 deletions — and pushed `606c1a6..12bed84`.)*

Both predictions held exactly, and the parser's `case` arm passed its first real exercise in two suites. `12bed84`.

The `case` track is complete. Four commits from spec to first use, none of them re-touched: `94b35cc` (the construct, specified), `8239d08` (roles and the carrier side), `606c1a6` (the kind, carried by the engine), `12bed84` (the token reserved; the first two branches). What the last run actually proved: the two hand-spliced projections matched the generator's output to the byte, and the parser read a case-branch for the first time. The single-line Scala `match` that `ScenarioDrakeTest`'s own header called the thing parsing could not vouch for is gone from the tree.

**Two things I'd carry forward**, one a lesson and one a suggestion:

- **An increment that authors nothing cannot exercise the engine.** 3a compiled clean and was green, and `targetBody` had no `Case` arm. Expect one such gap on the first real use of any new kind, and plan the following increment so the gap has somewhere to show.
- **A sealed-match gap should be a compile error, not a warning.** That crash would have been caught at 3a's build with a `-Wconf` promoting non-exhaustive matches to errors. It's a build-config change, so I haven't made it; it's yours to say.

**What's open behind the track**, from the records: `Generator(L)` still one function element — the six-way dispatch in the hand-written engine *is* the rules of `Generator(ScalaTarget)`, unwritten, and the 44 dispatch sites can now be spelled in drake; whether Drake is also a Target; `SourceContent`, `TypeLoader`, `ContentSink` as the same question `DefinitionPath` was; DRACO.md's `X.json (normative)` line, which under your grounding becomes *bootstrap carrier*; and `Base` as an endogenous upper ontology.

None of these are started. Say which, and I'll ground it in the tree before proposing an increment.

---

**[Dev]** Let's assume there is only one definition language  for draco and that is drake. Let's also assume that the definition data format is a configuration parameter for a given executable instance of the draco framework. What is is the (domain and type) / (containment and inheritance) structure implied for source code generation? Since we are implicitly supporting transforms and transform domains in addition to domains and types, and since source code generation from decoded type definitions is just another form of cross domain data transformation, we should define the domain and type structure accordingly. Given what is currently being implemented in the draco framework, the accordant structure is:

1. DracoGenerator - the super domain of all the source_target transform domains where source is always Draco and target is ScalaTarget, HaskellTarget, LeanTarget etc, plus the source language DrakeTarget for the definition data -> drake emitter transforms. The reason for adding DrakeTarget, is so that lessons learned from targeting other languages can be used to improve the definition language.
2. DracoGenScala - the generator subdomain created with "type DracoGenScala from Generator[ScalaTarget]" to specify the atomic type name for the generator domain.
3. ScalaTarget - the target subdomain for the Scala language
4. DracoGenDrake - The generator subdomain for drake emission.

The above is possibly missing some important structure, which you will have an opportunity to reveal in the response, as well a point out the issues with the structure, as mentioned.

**[Claude]** A design answer, so let me first check two things in the tree that bear on it: whether the `Source_Target` naming convention from the forest is written anywhere normative, and which targets actually exist as stubs.

The proposal is the forest scenario, one for one — and that's its strength, because the forest already found what such a structure needs and the proposal is missing exactly those parts. It also settles something I'd left open: Drake stays a Source and `DrakeTarget` is the Target. Two domains, one language, two roles. Good.

## The mapping, and what it exposes

```text
forest                                   generation (proposed)            status
────────────────────────────────────     ──────────────────────────────   ──────────
Forest            super-domain           DracoGenerator                   new
Ash               source data domain     Draco                            exists
Birch             target data domain     ScalaTarget                      exists, EMPTY
Ash_Birch         transform domain       DracoGenScala                    new
  Potency, Marker   leaf conversions       (the six-way dispatch, as rules)  unwritten
RootInterface     source-side ACTOR      —                                missing
BirchAlarmReceived  target-side RECEIVER —                                missing
birch.Micromolar  what a conversion      —                                missing
                  produces (a TARGET type)
```

The right-hand gaps are the missing structure.

**The target domains have no types.** `Ash_Birch.Potency` derives a Birch type — a conversion needs a codomain *value*. `ScalaTarget` has zero members; the only "value" anywhere is `String`. A morphism from a definition to Scala has to produce something that lives in `ScalaTarget` — at minimum a source-text type, more likely a compilation unit with its parts. Until the target side has types, `DracoGenScala` can have members that derive nothing, which is the scenario's gate 3 failing by construction.

**The receiving end.** The forest needed `BirchAlarmReceived` and it was not decoration: a fact inserted by a rule's action gets no working-memory node unless something consumes it. Generated source is such a fact. The consumer is the target-side rule that takes the produced source and writes it — which is where `ContentSink` belongs, and why I put it on the target side yesterday. Without it, generation produces facts that go nowhere, silently, exactly as the forest did before that rule existed.

**The actor.** Rules fire in a knowledge; something has to build it and insert definitions. One actor per transform domain, `messageType TypeDefinition`, knowledge from its domain chain — `Draco`'s own self-validation actor is the precedent and the shape is identical.

**The other direction.** The proposal names emission only. Drake → Draco (parse) and Format → Draco (the carrier decode) are transforms too, and they're where your round-trip validation lives — a round trip has a leg each way, and one of them is currently outside the framework as `Drake.parse` in Scala. Parse belongs as a member morphism of `Drake`. And "the definition data format is a configuration parameter of an executable instance" names exactly what `draco.generator.carrier` is *for*: the seam where an instance is told which `draco.format` sub-domain decodes its definitions and from which roots.

## Issues with the structure as stated

1. **"Source" means two things in one sentence.** `Drake from draco Source` is the role; `source draco Draco` is the transform pointer. Your message uses both ("source is always Draco" / "the source language DrakeTarget"). They're not in conflict, but nothing states the distinction, and it will bite the first reader who meets `DracoGenDrake` — a transform whose *target* is the language that *is a Source*. `drake.dlt` should say it once.

2. **Three relations where two say the same thing.** `DracoGenScala from Generator(ScalaTarget)` + `target ScalaTarget` + `source Draco`: the type argument *is* the target. Under explicit-iff-not-reconstructable, `target` elides when a domain derives `Generator(X)`. And `source Draco` is common to every member of the family, so it belongs once, on `DracoGenerator` — which would require `source`/`target` to resolve through the `super` chain. **Nothing does that today**; the scenario's gate 3 reads a domain's own aspect directly. That resolution is missing structure of its own.

3. **Two naming conventions.** The forest's is `Source_Target` in package `source.target`; yours is `DracoGen<X>`. The forest's is stated nowhere normative — only in the scenario, memory, and #63, all model-authored — so it's yours to overrule. I'd take `DracoGen<X>`: the machinery is presence-based, so a name needn't carry direction. But then say the forest convention was scenario-local.

4. **`draco.generator` has one domain type, and it's `Generator(L)`.** A package's domain self-loops; `DracoGenerator` would displace `Generator(L)` as the package's domain and demote it to a member type. That's fine, but it's a decision, not an implication.

5. **`Generator(L).generator`, `ScalaTarget.generator`, `Drake.generator`** — three function elements that are the whole current apparatus. Under morphisms-as-rules they either become the composed result of firing the domain's rules, or retire. Decision.

6. Small: drake spells type application `Generator(ScalaTarget)`, not `[ ]`; and Lean has no stub in the tree where Haskell and TypeScript do — the list is open anyway.

## Recommendation

**Build it the way the forest was built — structure as drake first, gates second, executable last — and make the first executable transform `DracoGenDrake`, not Scala.** Drake emission already has a gate on *both* legs: `DrakeGenTest` pins emit, `DrakeParseTest` pins the round trip. So the first rule-driven, actor-fired, receiver-sunk transform can be checked byte-for-byte against the hand-written emitter it's replacing, on 94 definitions, with no new oracle to write. Scala second: its rules are the 43 dispatch sites, and by then the shape — types on the target side, the receiver, the actor, `source` resolved once through `super` — will have been proven on the smaller case.

Decisions that are yours before increment one: the `source`-twice vocabulary (state it, or rename a role), and whether `DracoGenerator` becomes the package's domain type.

---

**[Dev]** The forest scenario is not complete or correct enough to serve as an example for the generator domain and subdomains. In the forest example, any transform domain name of the form *.source.target.Source_Target, both *.source.Source and *.target.Target could be just domains without there being any transform domain. With generator subdomains there are only draco.scalatarget.ScalaTarget as a domain and draco.scalatarget.Draco_ScalaTarget as the transform domain. Since there will never be a scalatarget.draco.ScalaTarget_Draco transform domain, we can instead name a generator subdomain as draco.dracogenscala.DracoGenScala, keeping the  name prefixes "draco" and "Draco" as a nod to naming it as a transform domain.

**[Claude]** Understood, and the reason the forest doesn't transfer is sharper than "incomplete": its placement rule *degenerates* here. The forest nests a transform under its source package (`ash.birch.Ash_Birch`), which works because Ash and Birch are peers. For generation the source is the root itself, so "under the source" means directly under `draco` — where everything already lives — and a directional `Source_Target` name buys nothing when the reverse transform can never exist. Following the convention literally would also put two domain types in one package (`draco.scalatarget.ScalaTarget` and `draco.scalatarget.Draco_ScalaTarget`), and a package has one self-looping domain. Your rename avoids exactly that.

So the structure now reads:

| package | domain type | what it is |
|---|---|---|
| `draco` | `Draco` | the source data domain — definitions live here |
| `draco.scalatarget` | `ScalaTarget` | a target domain, `from Target` |
| `draco.dracogenscala` | `DracoGenScala` | the transform `Draco → ScalaTarget`; `from Generator(ScalaTarget)` |
| `draco.dracogendrake` | `DracoGenDrake` | the transform `Draco → DrakeTarget` |
| `draco.draketarget` | `DrakeTarget` | the emission side of the definition language, `from Target` |
| `draco.drake` | `Drake` | the definition language as a Source; parse belongs here |

Each transform domain is a peer package, and the `draco`/`Draco` prefix carries the fixed source. That settles the naming question, and it settles it against the forest — which I'd now treat as scenario-local naming, not a convention.

What it doesn't change: the four gaps are structural, not nominal, and they stand — target domains need types for morphisms to produce; a target-side receiver, or generated facts get no memory node; an actor per transform to fire the rules; and the inverse direction (parse, carrier decode) as transforms in the framework rather than Scala outside it.

Two things still yours: **`DracoGenerator`'s home** — by the same rule it's `draco.dracogenerator.DracoGenerator`, a peer package, with `Generator(L)` staying in `draco.generator` as the parameterized type each `DracoGen<X>` derives; correct it if `DracoGenerator` should displace `Generator(L)` as `draco.generator`'s domain instead. And **keywords or type argument** — `from Generator(ScalaTarget)` and `target ScalaTarget` say the same thing; I'd keep both for now, with a lint that they agree as the right fix when there's a second member.

If that reading holds, the first increment is the structure as data: the six drakes above, drake-first, with empty member lists — nothing executes, every gate stays green, and the shape exists before any rule is written in it. Say go and I'll draft them.

---

**[Dev]** I think we can tighten up the naming by eliminating some of the "draco" and "Draco" name prefixes:

* draco.genscala.GenScala instead of dracogenscala.DracoGenScala
* draco.gendrake.GenDrake instead of draco.dracogendrake.DracoGenDrake
* draco.generator.Generator instead of draco.dracogeneratorDracoGenerator
* draco.Generator for "type Generator(T)" as a draco type with parameter for target, making "type GenScala  from Generator(ScalaTarget) domain draco genscala"

Let me know if this naming has consistency issues.

**[Claude]** One real inconsistency, and it's a hard one: **`draco.Generator` already exists — it's the hand-written engine.** Let me size it before answering. *(Sized the engine object and its call sites.)*

The naming is consistent with itself everywhere except one place, and that one is a compile error rather than a style point.

**`draco.Generator` is taken — by the hand-written engine.** `object Generator` in `src/mods/scala/draco/Generator.scala` is `package draco`, and mods compiles into the same package tree. A definition-backed `type Generator(T)` in root `draco` projects to `trait Generator[T]` + `object Generator` in that same package: two `Generator`s, one package.

Everything else checks out:

- **`draco.Generator(T)` beside `draco.generator.Generator`** — same simple name, different packages, and drake is fine with it: cross-package references are always qualified, so nothing is ambiguous on the surface. On the Scala side, `genscala`'s hierarchy imports are `draco._` only — `generator` is a sibling, not an ancestor — so a bare `Generator` in generated code resolves to the root type, which is the one wanted.
- **Filesystem** — `Generator.json` / `Generator.scala` against the `generator/` directory are different names, no case collision.
- **`gen<x>` + `<x>target`** per language, and three packages for drake (`drake` the Source, `draketarget` the Target, `gendrake` the transform) — regular, if heavy.
- **`carrier` stays under `draco.generator`** — under the super-domain, which is where the input seam belongs.
- `type GenScala from Generator(ScalaTarget)` / `domain draco genscala GenScala` reads exactly right; `GenScala` is atomic.

**Resolution: rename the engine, not the definition.** The definition should own the good name; the engine is the thing `GenScala`'s rules exist to replace, and it should carry a name that says so. I'd call it **`Projector`** — README's own vocabulary is "a target projects a definition", it collides with nothing, and when the rules land "the Projector retires" is a sentence that means something. It's mechanical: 32 files, about 125 call sites, no behaviour change, suite stays at 545. I'd leave `GeneratorCLI` and `bin/draco-gen` as they are — they're documented commands, not types.

The rename has to land *before* the structure increment, because the first drake that authors `draco.Generator(T)` triggers the collision. So the order becomes: rename (one mechanical commit) → the six domains as data with empty member lists → the gaps (target types, receiver, actor, parse as a member).

Say go on the rename and I'll do it drake-first where a trio is touched — only `ScalaTarget`'s — and by hand in mods and tests.

---

**[Dev]** Let's use draco.DracoGenerator for draco.Generator in src/mods.

**[Claude]** `DracoGenerator` it is. Before the mechanical pass, three greps that decide what the regex must *not* touch: qualified references to the typed domain, imports of the engine by name, and the doc mentions I'll fix by hand. *(Renamed the file and object, repointed 32 files, regenerated `ScalaTarget`'s trio, updated DRACO.md and README, wrote record `2026-09-08-0001` with the settled naming as a table.)*

The rename is complete and verified: no leftover reference anywhere in `src` or `bin`, `ScalaTarget`'s trio names the engine identically on all three sides, and the corpus round-trip is unchanged.

| | |
|---|---|
| engine | `object draco.Generator` → **`draco.DracoGenerator`**, file renamed to match |
| repointed | 32 files — every call site, four brace imports, the `ScalaTarget` trio |
| unchanged by name | `GeneratorCLI`, `bin/draco-gen` — documented commands, not types |
| docs | DRACO.md (the dispatch section now says why the engine was renamed), README |
| baselines | none can move |

The record also writes down the settled naming as a table, so the next session finds it in the tree rather than in this conversation.

Expected: 545 / 41, every headline exactly as the table has it. The only plausible failure is a missed reference, which is a compile error — the grep passes `[error]` through.

*(Delegated action — Dev pasted both results: 545 tests / 0 failed, every headline at baseline; committed as `[main e3c96f8] The engine is DracoGenerator, and draco.Generator is free for the definition` — 36 files changed, 187 insertions, 128 deletions, the engine file a 99% rename — and pushed `12bed84..e3c96f8`.)*

Green, `e3c96f8`, and git saw the engine file as a 99% rename.

`draco.Generator` is free. The next increment is the structure as data — the domains as drake with empty member lists, nothing executing, every gate green — so the shape exists before any rule is written into it. Concretely:

| package | drake | change |
|---|---|---|
| `draco` | `type Generator(T)` | new — the transform from Draco to target `T`; a plain root type, no elements yet |
| `draco.generator` | `type Generator` / `domain draco generator Generator` | **loses `(L)` and its `generator` element**; becomes the unparameterized super-domain; `carrier` stays under it |
| `draco.genscala` | `type GenScala from draco Generator(ScalaTarget)` + `source` / `target` / `super` | new |
| `draco.gendrake` | `type GenDrake from draco Generator(DrakeTarget)` / same shape | new |
| `draco.draketarget` | `type DrakeTarget from draco Target` | new |
| `draco.scalatarget` / `draco.drake` | unchanged except their `generator` globals | see below |

Two things in that table are decisions I've made provisionally and you can overrule: **`source` and `target` stay explicit** on `GenScala` and `GenDrake` alongside `from Generator(X)` — the presence rule reads both off the aspect, and changing it for a family of two is premature; and **the three `generator` function elements** — I'd move the two globals onto `GenScala` and `GenDrake` respectively, as the transforms' one member each until rules replace them, and drop the element from the super-domain. They're the engine's faces, and the transform domains are where those faces belong.

Say go — or correct either provisional choice first.

---

**[Dev]** Go, proceed with the structure increment as proposed.

**[Claude]** Starting the structure increment. Four reads first, all independent: the current `generator/Generator` projection and a parameterized root type as the pattern for `Generator(T)`; how the engine imports a *cross-package* type argument in a derivation (`Generator(ScalaTarget)` is the first concrete one in the corpus); any code that uses the two `generator` globals I'm moving; and the doc sentences that say `Generator(L)` is "source-universal", which your structure has since overruled. *(Built all six trios drake-first off the fresh classes; repointed `CLI`; updated README, DRACO.md and CHANGELOG; wrote record `2026-09-08-1507`.)*

The structure increment is in the tree: 30 paths, all drake-first off the fresh classes, corpus round-trip unchanged, no engine change needed.

## What exists now

```text
draco.Generator(T)               the transform Draco → T           root type, new
draco.generator.Generator        super-domain of every transform   (L) and its function gone; carrier under it
draco.genscala.GenScala          from Generator(ScalaTarget)       new — holds generator = DracoGenerator.generate
draco.gendrake.GenDrake          from Generator(DrakeTarget)       new — holds generator = Drake.emit
draco.scalatarget.ScalaTarget    a Target                          generator global removed
draco.draketarget.DrakeTarget    a Target                          new
draco.drake.Drake                a Source                          generator global removed
```

`CLI` points at the two new homes. Every member list is empty — nothing executes through the structure.

**The cross-package type argument resolved without touching the engine.** The projection already imports a transform domain's `source`, `target` and `super` packages, so `import draco.scalatarget._` arrived on its own — and because `draco.generator._` arrives too, `derivationRef` qualified the parent unprompted: `trait GenScala extends draco.Generator[ScalaTarget]`. That's the explicit `target` keyword paying for itself on day one, since type parameters are flat text and the keyword carries what the parameter can't.

**Named in the record so it isn't rediscovered:** any file importing both `draco._` and `draco.generator._` — today `Carrier` and `DefinitionPath` — would find a bare `Generator` ambiguous. Neither uses the name.

## Run

Expected: 565 / 41 — five per-type tests for each of four new definitions; `15 fields across 98 types` still 12 + 3; `88 draco + 10 mods`. What only the build can check is the two `Generator`s coexisting and the self-validation actor over four more peer domains. A compile error names the file; paste it.

*(Delegated action — Dev pasted the run: compile FAILED — `DracoGenerator.scala:10:7: Generator is already defined as trait Generator`, one error; nothing committed.)*

A `trait Generator` in the engine file, not the object — my rename caught the object and every call site, but not that trait. Let me see what it is and who uses it. *(Read the engine file's head; fixed all five sites; amended the record.)*

The leftover is fixed. The rename at `e3c96f8` caught `object Generator` and every `Generator.` call site — and missed the engine's *self-description* in the same file: an empty `trait Generator`, the `Type[Generator]` built on it, an `apply` returning it, and a hand-rolled `typeDefinition` naming itself `"Generator"`. None of it is referenced anywhere else. All five are `DracoGenerator` now, so the engine describes itself by its own name and the definition-backed `draco.Generator(T)` has the root to itself. The record for this commit says what was missed and why: a sweep that greps for what it expects to find misses what it didn't expect.

Same expectation as before — 565 / 41, `15 fields across 98 types` still 12 + 3, `88 draco + 10 mods`. If it fails again it's a compile error naming a file; paste it.

*(Delegated action — Dev pasted both results: 565 tests / 0 failed, every headline as predicted; committed as `[main 1cb3578] Source code generation is a transform, and the generator domains exist as structure` — 34 files changed, 404 insertions, 82 deletions — and pushed `e3c96f8..1cb3578`.)*

Green at 565 exactly, and `1cb3578` is in.

The generator domains exist. Since the session picked up on the 6th, the tree has moved through eight commits, none re-touched: `97d1f89`, `94b35cc`, `8239d08`, `606c1a6`, `12bed84`, `e3c96f8`, `1cb3578`. Three principles came out of it that now live in the tree rather than in the conversation: Drake is the Source and no carrier is normative; never author in a carrier what Drake can't parse back; and host vocabulary belongs on the generator's sides, not in the definition-language domain.

**What the structure still lacks to run** — each its own increment, and the forest already showed the order matters:

1. **Types on the target side.** `ScalaTarget` and `DrakeTarget` have no members; a morphism has nothing to produce but `String`.
2. **A target-side receiver**, or generated facts get no working-memory node.
3. **An actor per transform** to build the knowledge and insert definitions.
4. **Parse as a member of `Drake`**, so both legs of the round trip are in the framework.

`GenDrake` first when the rules come — both its legs are already gated, so a rule-driven emitter can be checked byte-for-byte against the hand-written one it replaces.

The open questions behind that are yours: what a target-side type *is* (source text, or a structured compilation unit), and whether `Base` as an upper ontology comes before or after the generator runs. Say which thread, and I'll ground it in the tree before proposing an increment.

---

**[Dev]** Proceed with the target-side types increment, GenDrake first.

**[Claude]** Target-side types, `GenDrake` first. The forest is the pattern here — a target domain's value types are primal wrappers, and a transform's member is a leaf conversion deriving one of them with a factory taking a source value. Reading both shapes so the new trios mirror them exactly. *(Authored `Surface` and `Emission` drake-first; found and documented the string-literal projection limit; updated the baselines, CHANGELOG and record `2026-09-08-1658`.)*

Increment in. `DrakeTarget` has its first value type and `GenDrake` its first conversion, both in the shape the forest proved:

```text
draco.draketarget.Surface   from Primal(String)          the drake surface of a definition   (Compound's shape)
draco.gendrake.Emission     from draketarget Surface     par definition TypeDefinition        (Potency's shape)
                                                          fix value String draco.Drake.emit(_definition)
```

`Emission` derives a type in the transform's target and takes a parameter typed in its source — the scenario's gate 3 convention, which `GenDrake` now satisfies — and deliberately does *not* derive the transform domain: it is a `Surface` and lives in `GenDrake`.

**One finding, surfaced rather than filed.** I first wrote the body on the application surface and it projected as a string literal: `value: String = "draco.Drake.emit(_definition)"`. `DracoGenerator` quotes *any* expression tree whose slot is `String`-typed — a rule made for a type arrow carried in a `valueType` field, which can't tell that from a computation whose *result* is a string. No definition had put one there before. So the body is the opaque call, which is also what `Drake.parse` produces for that glued token: the JSON is parse's own output and the loss count doesn't move. The limit stands until the projection can tell a value tree from a type tree in a `String` slot — the distinction #51 already draws.

Expected: 575 / 41; `15 fields across 100 types` still 12 + 3; `90 draco + 10 mods`.

*(Delegated action — Dev pasted both results: 575 tests / 0 failed, every headline as predicted; committed as `[main 6f5a8bb] DrakeTarget has a Surface, and GenDrake has its first conversion` — 15 files changed, 247 insertions, 10 deletions — and pushed `1cb3578..6f5a8bb`.)*

Green at 575, `6f5a8bb`.

The generator track now has a target-side value and a conversion that produces it. What remains before `GenDrake` *runs* is the executable half the forest needed, and it's one coherent increment rather than two, because neither piece means anything without the other:

- **A rule in `GenDrake`** — pattern: a `TypeDefinition` in working memory; action: insert `Emission(definition)`. The morphism as the engine sees it, wrapping the conversion that already exists.
- **A receiver on the `DrakeTarget` side** — a rule matching `Surface` that hands it to a sink. Without it the inserted `Emission` gets no memory node, silently, which is the exact failure the forest found and fixed with `BirchAlarmReceived`.
- **An actor in `GenDrake`** — `messageType TypeDefinition`, knowledge from its domain chain, body: insert and fire. `RootInterface` is the shape, now with the `case` construct available if dispatch is ever needed.
- **A gate that runs it** — feed a definition through the actor's knowledge and assert the received `Surface` equals `Drake.emit` of it, on the whole `src/main` corpus. That's the point of doing drake first: the hand-written emitter is the oracle, so the rule-driven transform is checked byte-for-byte with no new oracle to write.

Where the receiver lives is the one decision I'd want from you. In the forest it's a rule in the *super*-domain (`Forest` owns `BirchAlarmReceived`), which is why both sides' actors see it. The analogue here is `draco.generator.Generator` owning the receiving rule for every target — a `Surface` arriving is the same event whether it came from `GenDrake` or, later, `GenScala`'s equivalent — versus each transform owning its own. I'd put it in the super-domain, matching the forest and giving `Generator` its first member and its reason to exist as a domain. Your call, then go.

---

**[Dev]** Put the receiver in the super-domain, proceed.

**[Claude]** The executable half. Four pieces — a rule in `GenDrake` that inserts an `Emission`, the receiver in `generator.Generator`, an actor in `GenDrake`, and a gate that runs the chain over the whole `src/main` corpus against `Drake.emit` as the oracle. Reading the `src/main` precedents so each mirrors a proven shape: the collector rule and a rule over `TypeDefinition` facts from Draco's self-validation, and the two actor projections. *(Authored `Emit`, `SurfaceReceived` and `Emitter` drake-first; wrote `GenDrakeTest`; updated the baselines, CHANGELOG and record `2026-09-08-1751`.)*

`GenDrake` is wired to run. Everything is in the tree and the corpus round-trip is unchanged.

## The three members

```text
gendrake.Emit               rule    a TypeDefinition in working memory → insert Emission(definition)
generator.SurfaceReceived   rule    a Surface arrives → received.add(surface)        (the super-domain, your call)
gendrake.Emitter            actor   messageType TypeDefinition; Knowledge = Emit + SurfaceReceived
```

Each came out as its precedent's exact shape: `Emit` as `AshBirchAlarm`, the receiver as `BirchAlarmReceived` in tree form, the actor as `Draco`'s own — construction parameter set on the session, insert and fire, close on stop — but in the DEFAULT activation mode, because this is a chain. `actorKnowledge` walked the super chain unasked and the `Emitter` accepts both rules. No engine change.

## The gate, and what it checks against

`GenDrakeTest` fires the two rules by hand over every `src/main` definition and asserts the received surfaces *are* the corpus — the set of normalized `.drake` files equals the set of normalized `Surface` values. The oracle is deliberately the files, not `Drake.emit`: comparing to the emitter would be circular, since `Emission` calls it, whereas the files are what `DrakeGenTest` pins the emitter *to*. The two files authored ahead of the emitter are left out, so 88 of 90.

Sets rather than a per-definition match, because a `Surface` has no identity — it's the text and nothing else. That's honest about what the target type is today, and it's the next gap: a receiver can collect but can't *sink* until the product knows which definition it's the surface of.

## Run

Expected: 592 / 42, and a new headline in the capture: `GenDrake runs: 88 of 88 definitions emitted, 0 missing, 0 not in the corpus`. Type counts move to 93 in scope, 103 measured; no loss figure moves.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

*(Delegated action — Dev pasted the run: compile FAILED — `SurfaceReceived.scala:12:31: trait Generator takes type parameters`, twice on one line, two errors; nothing committed.)*

The hazard I named in the record fired, and worse than I described: it isn't ambiguity. In package `draco.generator`, `import draco._` is a wildcard import, and the package's *own* member `Generator` from another file ranks *below* it in Scala's precedence — so the root `Generator[T]` silently wins and `Domain[Generator]` means the parameterized one. `generator/Generator.scala` itself compiles only because its object is in the same file. `derivationRef` already solves exactly this for parents; the domain reference needs the same treatment. Reading both. *(Made the container reference go through the same shadowing probe; scanned the corpus for other such leaves; spliced the predicted line; amended the record.)*

Fixed. The container reference now goes through the same shadowing probe `derivationRef` uses for a parent, so a leaf whose container's name is held by another package in scope qualifies it — `Domain[draco.generator.Generator]` — and a corpus scan found `SurfaceReceived` to be the only such leaf today. Its projection is spliced to that predicted line, since the engine can't be recompiled from here; `DracoGenTest` checks it. The record now says what fired: not ambiguity, but a wildcard import silently outranking a member of the file's own package.

Same expectation: 592 / 42, `GenDrake runs: 88 of 88 definitions emitted, 0 missing, 0 not in the corpus`, type counts 93 and 103, no loss figure moved. If a gate fails, paste its block.

*(Delegated action — Dev pasted both results: `GenDrake runs: 91 of 91 definitions emitted, 0 missing, 0 not in the corpus`, 592 tests / 0 failed, `15 fields across 103 types`, `93 draco + 10 mods`, every other headline at baseline; committed as `[main 4c977e5] GenDrake runs: a definition crosses a transform domain and comes out as its surface` — 20 files changed, 615 insertions, 15 deletions — and pushed `6f5a8bb..4c977e5`.)*

Green, `4c977e5`, and the chain fired on the whole corpus: 91 of 91 definitions crossed `GenDrake` through working memory and came out as their own `.drake`, zero missing, zero extra.

One number in my prediction was wrong and I'll own it rather than let it sit: the record's header says "88 of 88." That was 90 definitions minus the two authored ahead — counted *before* this increment added three definitions of its own. 93 minus 2 is 91. The suite count, the type counts and the loss figures were all exact; the record's comment is historical and the table doesn't carry that headline, so nothing in the tree is stale.

The generator track, from grounding to a running transform, since Sunday:

| commit | what |
|---|---|
| `8239d08` | Source and Target as roles; the carrier side |
| `606c1a6` · `12bed84` | the `Case` kind; reserved; first uses |
| `e3c96f8` | the engine renamed `DracoGenerator` |
| `1cb3578` | generation as a transform: `Generator(T)`, super-domain, `GenScala`, `GenDrake`, `DrakeTarget` |
| `6f5a8bb` | `Surface`, and `Emission` as the first conversion |
| `4c977e5` | **`GenDrake` runs** — rule, receiver in the super-domain, actor, gate over the corpus |

A definition now enters working memory, a rule in a transform domain turns it into a product on the target side, a rule in the super-domain receives it, and the result is the corpus's own file, byte for byte. Nothing in the engine changed to make that happen; the rules wrap the conversion, which wraps the emitter.

Two engine gaps surfaced along the way, both the kind that compile clean and fail on first real use — the `targetBody` arm and the shadowed container reference — and both are now closed by the general rule rather than a special case.

**What the structure still lacks**, in the order I'd take them:

1. **Identity on the product.** A `Surface` is text and nothing else, so the receiver can collect but can't sink — it doesn't know which definition it's the surface *of*. This is what blocks `CLI` going through the actor and `GenDrake.generator` retiring.
2. **A sink** — `ContentSink` on the target side, once the product carries where it goes.
3. **Parse as a member of `Drake`**, so both legs of the round trip live in the framework.
4. **`ScalaTarget`'s types**, then `GenScala`'s rules — the 43 dispatch sites, on the shape `GenDrake` has now proved.

Identity first, because it's small and everything after it needs it. Say go, or redirect.

---

**[Dev]** Is it clear that the mechanics of all transforms involves composition of target factory calls with the "primal" valued target factories having all parameters from source values that have no dependencies from the target domain?

**[Claude]** Yes, in principle, and the forest instantiates it exactly. But the generator forces four things the forest never had to face, and one of them is where "clear" stops. Let me lay it out against what's in the tree.

## Where it holds cleanly

The forest is the mechanics verbatim:

```text
leaves      Potency  from birch.Micromolar   par potency  ash.Micromolar   → value = _potency.value * 0.62
            Marker   from birch.Compound     par compound ash.Compound     → value = _compound.value
composite   BirchJasmonate(Potency(j.potency), Marker(j.compound))         — in the RULE's action
```

The leaves are primal-valued target factories whose parameters are source values only. The composite is a target factory applied to target values. Nothing above a leaf ever sees the source; nothing at a leaf ever sees the target. That layering is what makes the transform a homomorphism — leaves map, composition is preserved — and it's why "a transform domain declares only leaf conversions" was right: the composites are already target factories, so declaring them again would be declaring the target twice.

`Emission` satisfies the *typing*: `Surface from Primal(String)`, one parameter, a source value. But it's degenerate — one leaf swallows the whole `TypeDefinition` and produces the whole `Surface`, and every bit of composition happens inside `Drake.emit`, in the engine, invisibly. It's the mechanics with the structure hidden.

## Where the generator forces more than the forest showed

1. **Leaves take composite source values, and context.** `Potency` takes one primal. A real leaf for generation — say, the line for a reference — needs the reference *and* the referring type's package to decide bare from qualified, and `derivationRef` today consults the whole dictionary. All source-side, so your principle holds — but "all parameters from source values" has to be read as including the enclosing definition and the dictionary, not just the leaf's own primal.

2. **Variable arity is the real gap.** `BirchJasmonate(Potency(…), Marker(…))` is a fixed expression. A definition has *N* elements, *M* derivations, and the composition is a fold over them, not an application. Two ways to say a fold, and only one is native to what draco has: a lambda in the action (`\e -> …` — specified, not parsed, #61), or **decomposition into facts** — each source element becomes a fact, a leaf rule per kind converts it to a target fragment fact, and an assembling rule collects the fragments. That second way is the RETE way; it's where `case` dispatch over `TypeElement`'s twelve kinds is precisely the mechanism; and it only works if fragments carry *identity* — which definition, which position. Composition by rules over facts is still composition of target factory calls at the bottom. But it's not one expression, and drake can't yet write the fold that would make it one.

3. **The leaf formula should be neutral, and today it's the engine.** `_potency.value * 0.62` is a formula drake can carry. `draco.Drake.emit(_definition)` is a host call standing in for a formula. The mechanics say the leaf body is the conversion; here the conversion is a black box until the fragments and their formulas are expressible in drake.

4. **It's symmetric, and that's the strongest confirmation.** Parse — `Drake → Draco` — is the same mechanics reversed: text primals in, leaf conversions to `TypeName`s and values, target factories (`TypeDefinition(…)`, the aspects) composing them. Same for a carrier decode. That's *why* Drake ↔ carrier round-trip is the natural gate: it checks a homomorphism against its inverse.

## What it changes about the plan

Nothing in the order, and it sharpens the reason. Identity on the product was next because a receiver can't sink without it; it's *also* the precondition for composition by facts, since a fragment has to know its definition and its place. So identity first still — but designed for both jobs, which means it's not just a name on `Surface`; it's the position of a fragment within a definition's surface. That's worth a moment of design before it's authored, and I'd rather ground it in what `Drake.emit`'s structure actually is than guess.

One line for the record when this lands: the mechanics above should be stated once, in `README` or `drake.dlt`, so a transform domain author knows what a leaf is allowed to take. Nothing says it today.

---

**[Dev]** We need to refine our shared understanding of then nature of transforms TypeDefinition -> LanguageTarget.

1. The transform itself is composition by string concatenation plus  string variable substitution
2. The substitution strings only exist in the LanguageTarget domain as type definitions with factory functions that map the input parameters into the substitution parameters declared in the substitution string
3. The type definitions in the LanguageTarget domain capture all of the syntactic structure needed to implement all aspects present in the source type definition. Each of the three optional aspects must have  LanguageTarget specific syntactic forms for the variable substitution strings that support the dependencies needed for each aspect.
4. All the rules that invoke  the code generation are target language neutral  (both pattern and action). This means that there has to be a draco framework binary that exists for the given target language, so that it can be the runtime dependency for anything build with the framework.
5. The capability for the draco framework to port itself to another target language is a much longer term priority. What's important short term is that none of the draco endogenous type definitions have to change change except for type definitions that belong to the target language domain, and it companion definition to target transform domain (where the transform factory implementations exist)

**[Claude]** Before I answer, one thing in the tree bears directly on points 1 and 2 — `SourceContract` in mods carries `ScalaTemplates` and `DrakeTemplates` and calls itself a prototype staging toward a definition. Let me see how far it already goes. *(Read `SourceContract` and its template objects.)*

The prototype already carries points 1 and 2 at the expression layer — an invariant traversal plus per-target substitution slots — and it recorded one honest boundary: `lambda` doesn't reduce to a substitution string because "bare param when single, else parenthesise" is logic, not a template. Your point 2 dissolves that boundary, and I'll come back to it. First the model as I now understand it, in layers, so it's checkable:

```text
neutral rules      pattern: a TypeDefinition (and what else is in working memory)      super-domain
                   action:  apply the transform, insert the product                    — no target named
transform domain   factories: SOURCE values → the inputs a target template declares    gen<x>
target domain      types: one per syntactic form; a substitution string + a factory     <x>target
                   that maps its inputs onto the string's variables
composition        concatenation — a form's factory takes other forms' strings
runtime            a draco binary in the target language, the dependency of everything generated
invariant          adding a target touches <x>target and gen<x>, and nothing else in draco
```

The forest is the case where every template is trivial (a number) and the fold is absent. `Emission` is the case where the *whole* template is the engine. `SourceContract` is the case done properly for one layer — and its lambda finding is exactly where your point 2 puts the logic: the template stays a pure substitution string, and the *factory* that maps inputs onto its variables is where "single or many" gets decided. Structural logic lives in factory bodies, never in templates. That reconciles the prototype with the refinement, and it's the rule I'd write down.

**Where what I built contradicts points 4 and 5 — three places, all mine:**

1. **`SurfaceReceived` is in the super-domain and names `draketarget.Surface`.** Not neutral. The super-domain needs a target-agnostic product type — the rendered text of a definition, whatever the target — that each target's product derives, and the receiver matches *that*.

2. **`Emit` lives in `gendrake` and constructs `Emission`.** Under point 4 the invoking rule is neutral and belongs in the super-domain, which means it can't name a transform's type. It needs a binding to *the* transform. The RETE-native way, using nothing but rule mechanics: the transform is itself a **fact**. The actor inserts its transform at start; the neutral rule's pattern is `var definition TypeDefinition` joined with `var generator draco.Generator`; the action inserts `generator.generate(definition)`. No session strings, no target named, and the fact types are unrelated so the co-declaration hazard doesn't apply.

3. **`Generator(T)` has no element**, because `1cb3578` removed the function from the old parameterized domain and parked it as a free global on each `Gen<X>`. Under point 2 that was the wrong direction: `generate` belongs on `Generator(T)` as the abstract element, and `GenDrake` *overrides* it. That's what makes item 2's `generator.generate(definition)` typeable.

**On points 3 and 5:** the three optional aspects become three template families per target — for Scala: the Evrete builder chain and action consumer, the Pekko behaviour, the circe codec — plus the *dependency* templates each pulls in, which is what `typeImports` and the six-way dispatch in `DracoGenerator` do procedurally today. Point 5 is a checkable invariant, and I'd make it a gate: no definition outside `<x>target` and `gen<x>` references either package. Point 4's binary already exists for Scala — the draco jar is what generated code depends on — with the distinction that neutral rules run in whatever draco *hosts* them, while a target's binary is the runtime of what it *generates*.

**What this changes in the plan:** composition happens in factories by concatenation, not in facts by position. So fragments never need to know their place, and identity on the product is only for the sink — a product that carries its `TypeName` knows its path. Variable arity is a fold in a transform factory's body, host-opaque for now, as `Drake.emit` is. That's a smaller identity increment than I was designing.

So the next increment is the three realignments — a neutral product type in the super-domain, `generate` restored on `Generator(T)` with `GenDrake` overriding it, and the invoking rule moved to the super-domain matching the transform as a fact — with `GenDrakeTest` unchanged as the gate that it still runs. Identity-for-sink after. One name to settle when I build it: the neutral product type, since "Surface" is drake's and "Source" is the role.

---

*(Session in progress at time of transcription — chapter to be extended when the session closes.)*

## Status

This chapter transcribes the August 31 – September 9, 2026 session, which is **still open at the time of transcription** — the last recorded exchange is September 9, 23:56 UTC, tonight, and it ends not with a close-out but with Claude's answer to Dev's five-point refinement of transforms, a next increment proposed (the three realignments) and one name left to settle (the neutral product type). The chapter will be extended when the session closes. **Opening state (carried from chapter 75's close):** `9616748`, 531 tests / 41 suites green, the forest scenario a full trio corpus in the test tree that runs (0.8 µM crosses at ×0.62, 0.3 µM filtered), the DRACO.md baselines table freshly written with its maintenance rule and verified nine-line capture command, #51's two open tracks standing (`valueType` expressions and the `TypeParameter` flat increment — decided, not built), #63 filed on the CO-DECLARATION verdict with its body correction (dispatch converts hierarchy into siblings, not routing), the four remaining host-opaque scenario values queued behind #58 and #61, and the full-result-paste protocol committed to memory at the previous chapter's open end. The session opened with Dev asking for work that had already landed — Claude established this from the tree rather than proceeding, and surfaced the real gap instead. **Commits landed so far, ten, none re-touched:** **`97d1f89`** (532/41) — the scenario's `.json` gated against its `.drake` (parse direction, JSON values not text, raw file JSON closing the silent `.toOption` decode hole), green on first firing. **`94b35cc` "drake: the case body element, specified and not yet built"** (532/41) — the `case` construct in `drake.dlt` after the design dialogue transcribed above: Dev's corrections (three executable actor blocks, only `message` needs it and optionally; the match pattern syntactically separate from the Rule `Pattern`; initially just "a type and case branch to specific sibling subtypes"), `Case` as a twelfth `BodyElement` kind reusing `name`/`valueType`/`body`, always self-bracketing (the third opener), binder settled **optional** by the engine's own evidence — 44 sibling-subtype dispatch sites, 8 of `Drake.keyword`'s 9 branches binding nothing — and the construct optional in actors by the same reversibility argument, with the four header forms disambiguating on capitalization alone; Dev's forward uses (the drake/generator domains, `Base` as an endogenous upper ontology) the deciding weight. Also the session's environment beat: Dev switched to Fable 5.1 mid-session after a macOS update; the first command block echoed uselessly (wrong directory, the grep swallowing everything including errors), fixed by anchoring both blocks to the repo and adding `error` to the capture grep — now in DRACO.md's operating rules. **`8239d08` "Source and Target are roles, and DefinitionPath moves to the generator's carrier side"** (542/41, 26 files) — the session's conceptual center, all four Dev prompts transcribed verbatim: the first `DefinitionPath` conversion (a `loc` + `if` tree of Scala atoms, authored straight into JSON, surface losses 15 → 16) caught by Dev as "significantly regressive" against language neutrality; the grounding dialogue — Source and Target as **roles** by derivation, `ScalaTarget from draco Source` exposed as crossed, `draco.Target` created, `Drake from draco Source`; Dev's "Drake is the Source" refinement demoting JSON to *bootstrap carrier* (no format normative to the framework; the surface-loss baseline literally the distance between Drake-as-Source in principle and in fact) and yielding the operational rule **never author in a carrier what Drake cannot parse back** — the conversion redone drake-first (`require`/`headOption`, losses back to 15); increments A and B merged into one commit after Dev inadvertently elided A's commit block (the "lying intermediate tree" reasoning, and Claude's accounting when Dev asked); **`draco.generator.carrier`** coined by Claude, accepted by Dev, `DefinitionPath` moved into it (git saw a rename) with `Carrier` *contained* under `generator`, not derived — containment is not inheritance. The `java -cp` offline-CLI route (byte-exact emit/generate/check without sbt) entered the workflow here and carried every subsequent increment. **`606c1a6` "Case: a twelfth TypeElement kind, carried by the engine, authored by nothing yet"** (545/41) and **`12bed84` "case is reserved, and TypeName.equals and RootInterface are the first two case-branches"** (545/41) — the two-build engine increment: kind, emit, parse (dyn body and `message` only), projection (consecutive branches one `match`, effect runs get `case _ => ()`); then the token reserved in the same commit that converted the last two opaque carriers — `TypeName.equals` (two result branches, the protocol residue gone) and `RootInterface` (the smuggled Scala `match` that started the whole track, gone from the tree). First real use flushed `Generator.targetBody`'s missing `Case` arm — a sealed-match gap that compiled as a warning — yielding the carried lesson *an increment that authors nothing cannot exercise the engine* and the queued suggestion of `-Wconf` promoting non-exhaustive matches to errors (build config, Dev's to say). **`e3c96f8` "The engine is DracoGenerator, and draco.Generator is free for the definition"** (545/41, 36 files, a 99% rename) — after Dev's foundational assumption prompt (drake the only definition language, the data format a configuration parameter, source generation "just another form of cross domain data transformation" with the DracoGenerator/DracoGenScala/ScalaTarget/DracoGenDrake structure), Claude's forest-mapping response (target domains need types, the receiver, the actor, the inverse direction; "Source" meaning two things; three relations where two suffice), Dev's judgment that **the forest scenario is not complete or correct enough to serve as an example** (its placement rule degenerates when the source is the root and the reverse transform can never exist), and Dev's naming tightening to `draco.genscala.GenScala` / `draco.gendrake.GenDrake` / `draco.generator.Generator` / `draco.Generator(T)` — which collided with the hand-written engine `object draco.Generator`; Claude recommended renaming the engine (proposing `Projector`), Dev chose **`DracoGenerator`**, and the first run flushed the engine's missed *self-description* (the empty `trait Generator` and its hand-rolled `typeDefinition` in the same file — "a sweep that greps for what it expects to find misses what it didn't expect"). **`1cb3578` "Source code generation is a transform, and the generator domains exist as structure"** (565/41, 34 files) — `Generator(T)` in root, `generator.Generator` the unparameterized super-domain (`(L)` and its function element gone), `GenScala from Generator(ScalaTarget)`, `GenDrake from Generator(DrakeTarget)`, `DrakeTarget from Target`, the two `generator` globals moved onto the transform domains, every member list empty; `derivationRef` qualified `extends draco.Generator[ScalaTarget]` unprompted. **`6f5a8bb` "DrakeTarget has a Surface, and GenDrake has its first conversion"** (575/41) — `Surface from Primal(String)` (Compound's shape) and `Emission` (Potency's shape: derives the target, parameter typed in the source, deliberately not deriving the transform domain); one engine limit surfaced rather than filed — the projection quotes any expression tree in a `String`-typed slot, so `Drake.emit(_definition)` stays an opaque call until value trees and type trees are distinguishable there (#51's distinction). **`4c977e5` "GenDrake runs: a definition crosses a transform domain and comes out as its surface"** (592/42, 20 files) — `Emit` (rule), `SurfaceReceived` (the receiver in the super-domain, at Dev's word), `Emitter` (actor), and `GenDrakeTest` firing the chain over the whole `src/main` corpus with the committed `.drake` files as the oracle (not the emitter — that would be circular): after one compile failure exposing that a wildcard `import draco._` silently **outranks** a package's own member from another file (the container reference now goes through `derivationRef`'s shadowing probe), the gate ran **91 of 91 definitions emitted, 0 missing, 0 not in the corpus** — Claude owning that its predicted "88 of 88" had been counted before the increment's own three definitions. The DRACO.md baselines table was updated at each moving commit per its own rule, ending at 592 tests / 42 suites, 93 draco types in scope, 103 measured, losses steady at 15 (12 + 3) throughout — the one deliberate 16 reverted with the drake-first redo. **Test counts across the arc:** 531 (`9616748`) → 532 (`97d1f89`, `94b35cc`) → 542 (`8239d08`) → 545 (`606c1a6`, `12bed84`, `e3c96f8`) → 565 (`1cb3578`) → 575 (`6f5a8bb`) → 592/42 (`4c977e5`). **The open end — tonight's exchanges, September 9:** Dev's transform-mechanics question (composition of target factory calls, primal-valued factories with source-only parameters) answered "yes in principle" with four generator-specific pressures (context-bearing leaves, variable arity as the real gap, the leaf formula still being the engine, the symmetry with parse); then Dev's five-point refinement — composition by string concatenation plus variable substitution, substitution strings as target-domain type definitions with mapping factories, per-aspect syntactic forms, **target-language-neutral rules** implying a per-target framework binary, self-port long-term but endogenous invariance short-term — against which Claude identified three of its own freshly-landed pieces as misaligned (`SurfaceReceived` naming a target type from the super-domain; `Emit` living in `gendrake`; `generate` missing from `Generator(T)`) and proposed the realignment increment: a neutral product type in the super-domain, `generate` restored on `Generator(T)` with `GenDrake` overriding, the invoking rule neutral in the super-domain matching the transform *as a fact* (`generator.generate(definition)` — no target named, fact types unrelated so #63's hazard doesn't apply), `GenDrakeTest` unchanged as the gate. **Queued at the open end:** the three realignments and the neutral product type's name (the immediate next increment, not yet authorized); identity-for-sink (now smaller — a `TypeName` on the product, for the sink only); `ContentSink` to the target side; parse as a member of `Drake`; `ScalaTarget`'s types and `GenScala`'s rules (the 43 dispatch sites, on `GenDrake`'s proven shape); the point-5 invariant as a gate (no definition outside `<x>target`/`gen<x>` references either package); the transform mechanics stated once in README or `drake.dlt`; the `-Wconf` sealed-match promotion (Dev's call); `SourceContent`/`TypeLoader` as the remaining carrier-side moves (`TypeLoader` the hard one); DRACO.md's `X.json (normative)` line becoming *bootstrap carrier*; the `msg`/`ctx`/`signal` hardcoded-scrutinee residue; the `case`-derives-scrutinee lint when a second user lands; `Base` as an upper ontology; and from earlier chapters, still standing — #51's `valueType` expressions and `TypeParameter` increment, #61 (paths, infix operators, `if`, lambdas — now also what keeps `Emission`'s body and the fold host-opaque), the example-tier regeneration and gating, `AGENTS.md` as a diverged copy, the closedness-declaration question. **Because the session is open, everything above is the state as of the last recorded exchange, not a close: no session-ending ritual has been run, and this chapter's final exchange may not be the session's.**
