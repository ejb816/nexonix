# Draco Dev Journal — Chapter 70

**Session date:** July 28 – August 4, 2026 (session possibly ongoing)
**Topic:** The recovery session after Dev deleted the abandoned `draco.rete` session (chapter 69) — rollback verified clean at `59f793a`, the three over-claiming records (memory, MEMORY.md, #49's comment) retracted, and the WHAT/HOW line the deleted attempt crossed named precisely: it moved Evrete *configuration* into main, the opposite of the 2026-06-14 north star → the "hygiene" exchange — a coined term standing in for an argument; Dev: keep the term, record the exchange for the journal → the 27 redundant sole-`[DracoType]` derivations stripped (228 deletions, zero insertions, inert by construction — both projections now agree) → the Draco-actor symmetry dialogue: `Actor[DracoType]` doesn't belong in the derivation, `messageType` moves onto the actorAspect — Dev authors the drake edit himself, the "messed up typo" (`Acpetion`) discovered only because `authoredAhead` files are unguarded, the derivation drop staged behind the drake emitter learning `messageType` → type expressions as the context-independent form: #51 filed with "type expressions close to value expressions" as its spine, `Action.json`'s own `{"->": …}` tree the corpus's unprompted precedent → the Action-definition outlier arc — Dev re-authors `Action.drake` to propose better, Claude's counter-proposal twice corrected by the test (a `par` in a factory body isn't a thing; the arrow stays, `Pattern` is the sibling that settles it), the outlier reduced to one fact: a binder name in `name` → wrap types: a domain per non-native dependency (#51's vocabulary half, cross-linked to #49), `draco.rete.Rete` first pass as contentless markers, the atomic-terms constraint on `TypeName.typeParameters` killing the recursion fork, `from` kept over a new keyword, `Dictionary` as the worked exemplar of systemic dependency isolation, the two-layer stratification (draco wrap types under dependency-domain wrap types — `draco.format` proves it by failing the language-neutrality test), Base as upper-ontology placeholder and the constitutive/incidental cut (60 `Seq` sites = one missing constitutive type) → the operator/name boundary: `{(K, V)}` vs `{K, V}`, the hash-discipline correction, the elided-capability hunt resolving to "names are a second, unpoliced channel for type structure" → `typeParameters [String]` → `[TypeParameter]` decided on #51 → the two open items closed on request (derivation drop + `Action.json` out of `authoredAhead`), 224/224, the git-record rewritten in place for three commits — the session's last recorded exchange, still open. Transcript.

---

**[Dev]** I need you to figure out where we are. I just deleted the latest session after the work went in a confusing and unproductive direction. I did a rollback of all the changes made in the project during that session, so were back at the point of the previous session end, after the last commit/push.

**[Claude]** I'll get oriented — repo state first, then the backlog. *(Ran git status/log, the issue backlog, the memory timestamps, #49 and its comments, the last commit's stat, the leftover rete directories; read `project_draco_rete.md`.)* Working tree is clean and exactly at `origin/main` (59f793a) — the rollback was complete. Memory was written at 17:15 and 21:29 — well after the last commit (12:44). So some memory claims may describe rolled-back work.

## Where we are

**Repo: clean, at `origin/main` = `59f793a` (2026-07-27 12:44).** No stash, no untracked files, nothing uncommitted. The rollback was complete — you're exactly at the previous session's end.

**What that commit contains** (`draco-as-actor + aspect composer runtime (#48) + uniform root meaning (#38 by inversion)`): Draco validates its own dictionary via a definition-backed *actor* — `CollectProblems.scala`/`.json`, `DracoActorTest.scala`, Draco.json gaining the actor aspect; the aspect composer runtime (domain⊕actor), uniform root meaning via `TypeLoader.rooted`; journal chapter 68 + git-record-2026-07-27-1240.

**What the deleted session did, and what's left of it:** it built the `draco.rete` increment-1 (#49). That code is gone — only two empty directories survive the rollback (`src/main/scala/draco/rete`, `src/main/resources/draco/rete`), and `Draco.json:41` still carries the raw engine binding `knowledge.newStatefulSession(org.evrete.api.ActivationMode.CONTINUOUS)`. So the quarantine is *not* in.

## Records that now over-claim

The rollback didn't reach memory or GitHub, so three model-authored records assert work that doesn't exist:

1. `project_draco_rete.md` — "Increment 1 — LANDED (2026-07-27, suite 324/324)"
2. The MEMORY.md index line for it — same claim
3. Issue #49's comment — "Increment 1 landed … suite 324/324"

Reading past the claims, the deleted session did produce two findings that are *analysis*, not code, and survive the rollback intact: the naive staged/exhaustive inference rule misclassifies the forcing case; refinement is **staged = production feeding further production**, and an edge into a pure sink (CollectProblems) doesn't make a set staged — inference stays blocked until "assert a fact" is a native action operation. And the "latent DEFAULT-activation trap" was already retired before that session — a census showed all three RulesTests already use `CONTINUOUS`; migrating them is vocabulary completion, zero behavior change.

Also new from that session: issue #50 (root-meaning follow-ups — 27 redundant `[DracoType]` derivations, rule trait emission) and the feedback memory `feedback_model_authored_records_are_not_user_intent.md`, which is precisely the lens to apply to #49's comment: prior reasoning to re-examine, not a spec.

## Recommendation

Correct the three records first — rewrite `project_draco_rete.md` and the #49 comment to say increment 1 was attempted and rolled back, keeping the two findings as design notes — so nothing downstream builds on a false "landed." That's a 10-minute pass touching no project code.

Before I re-plan #49 itself, though: what went sideways? I can see the shape of the work but not where it lost the thread, and that determines whether the rete increment is worth a second, smaller attempt or whether the pickup point should be something else entirely (#50's hygiene strip is a much narrower, well-gated increment).

---

**[Dev]** The deleted session had introduced src/main/resources/draco/rete/Rete.{json,drake} which actually defined a new domain, without really explaining why it did that. I let it go to far in that direction without really ungerstanding the reason.

**[Claude]** Let me trace where the "new domain" idea actually came from, rather than take #49's framing at face value. *(Surveyed the existing capability domains, the 2026-06-14 memory note, `ScalaTarget.json`.)* Your instinct was right, and the record shows why. Here's the trace.

## The idea wasn't invented in that session

It comes from a note dated 2026-06-14, recorded as a correction from you: the three heavy external couplings (Evrete, Pekko, Circe) should sit behind capability domains, and `draco.rete` + `draco.actor` are "the two MISSING peers." So there *was* a prior rationale — the session just never put it in front of you before acting on it. That's the explanation gap you felt.

## But what it built inverts that rationale

The principle as recorded: a definition **in `src/main` must never name an implementation type** — it names a capability type; the binding to the real library is non-definitive and lives in `src/mods`. The precedent cited is explicit: `draco.format.Format[T]` in main (abstract), `draco.format.json.Json` in mods (binds circe). "The definition/binding split IS the main/mods split."

The session put `draco.rete.Rete` in **`src/main`**, with dyn bodies containing `knowledge.newStatefulSession(org.evrete.api.ActivationMode.CONTINUOUS)` and a parameter typed `org.evrete.api.Knowledge`. That's Evrete named inside a main-tree definition — the exact thing the principle forbids. It didn't quarantine the coupling; it relocated one line of it out of Draco.json into a new domain and gave it a draco-sounding name. Couplings in `src/main` went *up*.

Three more things don't hold up:

- **The precedent it claimed is not a precedent.** `ScalaTarget.json` — the domain it said it mirrored — names no external library type at all; its one global element is `Generator.generate`, draco's own. "One-type capability domain mirroring scalatarget" was a shape analogy, not a justification.
- **It contradicted its own warning.** #49's body says don't add definition-language surface until a real system needs two disciplines (YAGNI). The increment then added `staged` with zero clients — the comment concedes it is "speculative surface, which this issue itself warns against."
- **The trigger was one line.** `Draco.json:41`, a single `Fixed` element. And singling it out was arbitrary: the surrounding actor `start`/`message` body is already host glue throughout — `org.evrete.api.StatefulSession`, `session.set`, `session.insert`, `session.fire`. Quarantining the enum while leaving its four neighbours untouched doesn't buy a portability property.

The honest version of this work is the whole Evrete surface — abstract Session/Knowledge/RhsContext in main, Evrete binding in mods, per the 2026-06-14 note. That's a large, real piece of work with a clear payoff, not a one-line move.

## Recommendation

Don't rebuild `draco.rete`. Rewrite #49 to record what actually happened — attempted, rolled back, and specifically *why the shape was wrong* (binding placed in main, precedent misapplied, own YAGNI rule broken) — keeping the two surviving findings. Drop `priority-next` from it and leave the Draco.json line as the honest host glue it is. Then take **#50's hygiene strip** as the next pickup: strip the 27 redundant sole-`[DracoType]` derivations, each gated by DracoGenTest byte-identity — narrow, mechanical, no new structure.

I'll also fix `project_draco_rete.md` and its MEMORY.md line to match. Say go and I'll do the record corrections; the empty leftover dirs go too:

```bash
rmdir src/main/scala/draco/rete src/main/resources/draco/rete
```

---

**[Dev]** Go ahead with the corrections.

**[Claude]** *(Removed the two empty dirs; prepended a retraction to #49's comment preserving its text, posted the review comment, dropped `priority-next`; rewrote `project_draco_rete.md`, updated MEMORY.md, created `feedback_surface_structural_moves_first.md`.)* Both empty dirs are gone. Now the issue comment — I'll preserve its text and prepend a retraction. Now the review comment and the label change. Now the memory records.

---

**[Dev]** Describe what is meant by hygiene here. That's another conceptual convenience term that was determined by the model in an earlier session.

**[Claude]** Let me check the actual operation rather than repeat the issue's prose. *(Checked `TypeLoader.rooted`, re-counted the corpus, read the inference section of `drake.dlt`.)* Fair challenge — I used it without unpacking it, same failure mode as before. Here is what's actually underneath.

## The concrete operation

`TypeLoader.scala:15` — `rooted` — does this: if a loaded definition's `derivation` is empty (and it isn't `draco.DracoType` itself), it inserts `DracoType` as the derivation. That's the #38 inversion.

So two spellings now produce the identical `TypeDefinition`:

- `"derivation": [ { "name": "DracoType", "namePackage": ["draco"] } ]`
- no `derivation` key at all

I re-counted the corpus independently: **27** files carry the first spelling, matching #50's census. The "strip" is deleting that block from those 27 files. It cannot change generated Scala, because `rooted` puts the entry back before the Generator ever sees the definition — which is why DracoGenTest byte-identity is the gate.

## What the word is doing

"Hygiene" is not a draco term. It's borrowed from general programming talk ("code hygiene"), and it does work that isn't argued for: it classifies redundancy as *dirt* and its removal as self-evidently good. That framing skips the question rather than answering it. It's the same move as calling `draco.rete` a "capability domain" — a familiar-sounding label doing the job of a justification.

Stated without the metaphor, the actual question is:

> After #38, the corpus admits two spellings for one meaning. Should a draco definition state a parent that load-time elaboration would supply anyway?

That's a real question about the definition language, not a chore.

## What bears on it

`drake.dlt:170` records an INFERENCE principle: something is inferable when its JSON "can be reconstructed from information already present; otherwise it must be explicit." A sole `[DracoType]` derivation is exactly reconstructable, so that principle points at stripping.

But I should flag the weakness rather than lean on it: drake.dlt states inference at the level of **aspects** (rule/actor/codec/draco), not individual fields. Applying it to the `derivation` field is an extension by analogy — the same shape of reasoning that made the ScalaTarget "precedent" hollow. It's suggestive, not decisive.

And there's a genuine argument the other way, which #50 doesn't mention. Draco's premise is self-description, and the JSON is the single source of truth. Strip the derivation and the file no longer states its own parent — the parent lives in an elaboration rule inside the loader. A reader with the file alone, or a second-language port, needs `rooted` reimplemented before the corpus means what it says. That's a trade of *locality of meaning* for *one spelling*, not a free cleanup.

The counterweight: every Scala class implicitly extends `AnyRef` and nobody writes it. #38 was closed "by inversion" precisely to give absence that meaning — and leaving 27 files spelling it out undercuts the inversion it just established.

## Recommendation

Rewrite #50 item 1 to drop "hygiene" and state it as the question above — a decision about the definition language, with the self-description cost named. On the substance my lean is to strip, because #38 deliberately made absence meaningful and a corpus that keeps saying it out loud in 27 places contradicts what was just decided. But that's a lean on a real trade-off, and it's your call — tell me if you read the self-description cost as the heavier side.

---

**[Dev]** I don't need to drop the term, I just need this exchange recorded when it's time to update draco dev journal.

**[Claude]** Understood — term stays, exchange gets transcribed. Journaling is Cowork's job, so I won't write or suggest a chapter; I'll just make sure this doesn't get lost before then.

It's already durable in two places: this session's transcript (what Cowork transcribes from) and the #49 review comment. The gap worth naming for whoever does the transcription is that the *deleted* session has no transcript at all — so the chapter covering this will have the post-mortem of the `draco.rete` attempt but no record of the attempt being made. Chapter 68 is the last one, ending at `59f793a`.

*(Added the thread to `feedback_surface_structural_moves_first.md` rather than starting a duplicate file.)* Recorded. The memory file now carries the exchange with your position attached — the term stays, the pattern of a coined term substituting for the argument is what's noted — plus the caveat that the building session left no transcript. Nothing else changed: repo still clean at `59f793a`, #50 untouched (still says "hygiene strip"), and no chapter written.

---

**[Dev]** Since we switched to TypeLoader, taking the unnecessary explicit DracoType derivation seems appropriate. Is that something we can take care of immediately?

**[Claude]** Yes. Let me check the load path first — the gate only holds if the test elaborates through `rooted`. *(Checked the test's load site, the Generator's entry points, the drake emitter, then surveyed the edit shape; wrote and dry-ran a surgical strip script, then applied it.)* The test bypasses elaboration — that's the real blocker… Correction: the gate does hold. `Generator.generate` applies `TypeLoader.rooted` to its input at entry (`Generator.scala:1882`, and the `Seq` overload at :1962), so the test's non-elaborating load site is harmless — elaboration happens inside the Generator regardless of which loader produced the definition. Both projections are safe — the drake emitter already suppresses a sole-`DracoType` derivation as inferable (`Generator.scala:171`). 14 files have pre-existing formatting that a re-dump would churn, so I did a surgical line-level removal instead and verified semantically.

Done — 27 files, **228 deletions, zero insertions**. Pure removal, all 76 JSONs still parse, no sole-`DracoType` derivations remain.

What made it safe by construction:

- `Generator.generate` roots its input at entry, so `rooted` restores the entry before emission regardless of which loader produced the definition — Scala output should be byte-identical.
- The drake emitter already treats a sole `DracoType` as inferable and emits nothing for it, so no `.drake` file needed touching. That was already true — `Holon.drake` carried no derivation line while `Holon.json` spelled it out. The two projections now agree, which is the substantive gain beyond one-spelling: JSON no longer contradicts drake about whether the root is written.

Two shapes were involved: 21 files where `derivation` sat alongside other keys (removed the entry), and 6 where it was `dracoAspect`'s only key (dropped the whole aspect — proven safe since 7 files, the rules plus `CollectProblems`, already omit `dracoAspect` entirely).

Your gate:

```bash
sbt "testOnly draco.DracoGenTest draco.DrakeGenTest"
```

If that's green, the strip is confirmed inert. Then the full suite before push, expecting 321/321. Two things I did *not* do, since neither is mechanical: `DracoGenTest.loadTypeDefinition` still decodes without elaborating — harmless, because `generate` roots internally, but it means the test's `td` isn't what production loads, and the same is true of `CLI.scala:16`. Worth a decision separately rather than folded in here. And #50 still reads as written; I'll update it once the gate confirms, so the record follows the result rather than predicting it.

*(Delegated action — Dev ran both gates: `sbt testOnly …` and `sbt test` — all tests passed.)* Both gates green — the strip is confirmed inert. Recording the result now. *(Commented #50 item 1 done; updated `project_presence_inference_model.md` and MEMORY.md; wrote `draco-git-record/git-record-2026-07-29-1132`.)* Records are updated and the audit-trail file is in place so `git add -A` will sweep it into the commit:

- **#50 item 1** — marked done with the byte-identity explanation, the projections-disagreed finding, and the framing correction. Issue stays open for item 2.
- **`project_presence_inference_model.md`** — removed the now-false "corpus may stay textually mixed / 27 files redundant-harmless" line and added the landing section. MEMORY.md index updated to match.
- **`draco-git-record/git-record-2026-07-29-1132`** — written, with the commit block. Paste the file's contents into your terminal, or commit from the IDE using its message body.

Two notes on what I recorded as deliberately unfinished. `DracoGenTest.loadTypeDefinition` and `CLI.scala:16` decode without elaborating — inert, since `generate` roots internally, but I'd rather that be a decision than a silent inconsistency. And #50 item 2 (rule trait emission) is untouched. Nothing is marked `priority-next` right now.

---

**[Dev]** I 'm  not sure that Draco should extend Actor[DracoType] because that should be actor aspect in addition to the domain aspect, which indicates that Draco is as much a domain as it is an actor. Also, I don't see why actor has to have a start parameter since whatever value instances it needs can be constructed directly in the start body in it's definition.

**[Claude]** Both are about the committed `draco-as-actor` work. Let me look at what's actually there before responding. *(Read `Draco.json`, the two actor predicates in the Generator, `Draco.scala`, and censused the `Actor[T]` derivations across the corpus.)* Both hold up. Here's what the code actually says.

## `Actor[DracoType]` in the derivation

There are already **two** actor predicates in the Generator, and they disagree:

- `isActor` (`Generator.scala:1721`) matches derivation names `ActorType`/`ExtensibleBehavior`. Draco's derivation is `Actor`, so **this returns false for Draco**.
- `hasActorBehavior` (`:1498`) is `!ActorAspect.isEmpty(td.actorAspect)` — pure aspect presence. This is what `roleAspectCount` uses and what gates `actorBlock`/`actorImports` in the composer.

So Draco's actor-ness is *already* carried entirely by the actorAspect. The derivation isn't what makes it an actor. It's doing two other things:

1. Emitting `trait Draco extends Actor[DracoType]` — exactly the asymmetry you're naming. The actor role climbs into the type hierarchy; the domain role contributes only body members (`elementTypeNames`, `domainType`). That contradicts the composer's own stated premise, "no single winner, every present aspect contributes its block."
2. Carrying the **message type** — `actorMessageType` (`:1502`) reads the type argument off the `Actor[T]` derivation. This is the one load-bearing job, and the actorAspect has no field for it.

So the derivation does one legitimate thing and one illegitimate one. Move the message type onto the actorAspect and Draco's derivation goes empty → rooted → `trait Draco extends DracoType`, with both roles contributing symmetrically.

Scope check: 12 more actor definitions in `src/mods` carry `Actor[T]` derivations. Two of them — `domains/world/Consumer.json` and `Provider.json` — have the derivation but **no actorAspect at all**, so under an aspect-only rule they'd stop being actors. Those need a look before the rule tightens. `Actor.json` deriving `ActorType` is fine — that's the framework type legitimately in the hierarchy.

## The `problems` parameter

You're right that the actor doesn't need it, but constructing it in `start` alone would break something, so it's worth being precise about why it's there.

`problems` is a `java.util.ArrayList[Problem]` the **test** creates, hands in, and reads back after the run (`DracoActorTest.scala:44`); `session.set("problems", problems)` binds it into the Evrete Environment so rule RHSs append to it. It isn't serving the actor's needs — it's a back-channel for the caller to observe findings. Construct it inside `start` and the actor still works; the findings just become unreachable.

Which exposes the real problem: a shared mutable `java.util.List` crossing the actor boundary is the thing actors exist to prevent. The idiomatic replacement is a reply — the message carries a reply-to `ActorRef`, or the actor emits findings onward — which is the Environment→`ActorRef` seam the Aerial example already established. That removes the parameter *and* the shared mutable state. So I'd agree with dropping it, with the replacement being "the actor replies," not "construct it in start."

## Recommendation

Take the derivation fix first, on its own: add an explicit message-type field to `actorAspect`, drop `Actor[T]` from Draco's derivation, and decide what the two aspect-less `world` definitions are. It's narrow, gated by DracoGenTest plus the mods gen tests, and mostly removes a contradiction the code already leans against. The `problems` change needs the reply-protocol decision first and touches rule RHSs plus the test, so it should follow rather than ride along.

One sub-choice worth your call: explicit `messageType` field versus inferring it. My lean is explicit — nothing else in the definition names that type, so by the drake.dlt rule ("explicit iff not reconstructable") it can't be inferred. Same principle that settled the strip.

Say go and I'll file both as issues and start on the first; correct me if the message type belongs somewhere other than the actorAspect.

---

**[Dev]** I've added messageType to ActorAspect.drake, so we can avoid filing the two issues, if you can carry the drake change forward to json and Scala

**[Claude]** Let me see exactly what you wrote, then match it in the other two projections. *(Read `ActorAspect.drake`, `.json`, `.scala`, and the `DomainAspect` TypeName-valued-element idiom; edited `ActorAspect.json`.)* The idiom is settled — `TypeName.Null` default, `.name.nonEmpty` elision. One thing your drake leaves open: `messageType` is an element but not a factory parameter, and `apply` builds the instance from parameters only, so the field would stay abstract and the type wouldn't construct. I'm adding the matching parameter; say if you meant it non-constructible.

---

**[Dev]** The messageType is not a factory parameter, it is a type parameter that replaces the "T" in ExtensibleBehavior[T] when the actor aspect source code is generated.

**[Claude]** Let me check whether the corpus already has elements that aren't factory parameters — that would tell me the mechanism rather than me inferring one. *(Censused element/parameter shapes, `actorMessageType`'s call site, the codec coverage.)* Understood on the role — that's the T substitution at `Generator.scala:1617`, `actorBehavior(td, actorMessageType(td))`, replacing the read off the derivation. That's the design I'll wire.

But on "not a factory parameter" the mechanism disagrees, and it's worth being concrete because I checked the two precedents:

- **Elements with no factory at all** (`Aspects`, `RuleType`, `Primal`, `TypeElement`, …) stay abstract — nothing constructs them.
- **Elements with a factory but no parameter** are *derived* values computed in the factory body. `TypeName.namePath`/`resourcePath` are the clean case, and critically they appear in **neither the encoder nor the decoder** — the codec covers exactly the factory parameters.

`messageType` is neither shape: it's authored per-actor in `Draco.json`, so it has to survive JSON → decoded instance for the Generator to read `td.actorAspect.messageType` at all. Today the only route from JSON into the instance is a factory parameter. Without one, the field decodes to nothing and there's no `T` to substitute.

The closest precedent is exactly analogous and settles it for me: **`DomainAspect.typeName`** is an authored `TypeName` on an aspect, consumed at generation time to decide domain-ness — and it is a plain factory parameter. `messageType` on `ActorAspect` is the same kind of thing. "Parameter" there describes how the authored value reaches the decoded aspect, not a claim that it's runtime instance data; its *use* is the type-argument substitution you describe.

So my recommendation is to keep what's now in place — element plus parameter — on that precedent, and next wire `actorMessageType` to read `td.actorAspect.messageType`, then drop `Actor[DracoType]` from Draco's derivation.

The alternative is real but bigger: give the Generator a notion of a decode-but-don't-construct element, which needs a codec change and has no precedent in the corpus. I wouldn't spend that unless you want the aspect's `apply` signature kept clean on principle.

That also resolves the drake question you left open. If drake's `parameters` block mirrors the JSON factory it wants `par messageType TypeName TypeName.Null`; if `parameters` is meant as the authoring surface only, drake and JSON diverge here — your call, and I'll make either edit.

---

**[Dev]** You're correct about the factor parameter still being needed, that factory call occurs as Generator is deciding what to generate, and needs the messageType TypeName to generate the actual type parameter for the call signature of the message and signal actions.

**[Claude]** Right — the factory call happens inside generation, to build the `receive`/`receiveSignal` signatures. Let me wire it, taking the inert step first. *(Edited `Generator.scala` and `Draco.json`.)* Now `Draco.json`'s actor aspect. One choice to flag: I'm authoring the bare name rather than the qualified one, to keep this step inert.

Wired, as a deliberately inert step so the gate proves the plumbing before anything semantic moves.

**`Generator.actorMessageType`** now prefers `td.actorAspect.messageType.namePath` and falls back to the `Actor[T]` derivation when the aspect is silent. The fallback is what keeps the 12 mods actor definitions working — they still carry `Actor[T]` derivations and no `messageType`, so nothing there changes. I documented the fallback as transitional in the comment rather than leaving it looking like a permanent dual source.

**`Draco.json`** gains `actorAspect.messageType`. Both sources now say `DracoType`, so generated output should be byte-identical and the suite should stay green — that's the whole point of doing it this way.

One deliberate choice: I authored it as `{"name": "DracoType"}` with **no `namePackage`**, so `namePath` yields the bare `DracoType` and reproduces today's bytes exactly. The honest model is `("DracoType", ["draco"])`, which would emit `draco.DracoType` — valid inside `package draco` but a real diff across the `receive`/`receiveSignal` signatures, and I'd be hand-guessing the emission in several places without being able to compile. Qualification matters properly when the mods actors migrate, since theirs are genuinely cross-package (`draco.format.json.JSON`, `domains.world.World`). Flagging it as deferred, not settled.

```bash
sbt "testOnly draco.DracoGenTest draco.DrakeGenTest"
```

Green means the aspect is being read and the substitution works. Then the semantic step is one edit: drop `Actor[DracoType]` from `Draco.json`'s derivation, leaving `trait Draco extends DracoType` with actor-ness carried by the aspect alone and both roles contributing symmetrically — which is what you were after. I've held that back so it gets its own gate.

Two things still open from earlier and untouched: the `par messageType TypeName TypeName.Null` line in your drake's `parameters` block (your authoring surface, your call), and the `problems` reply-protocol question.

---

**[Dev]** Before I ran the test, I updated ActorAspect.drake  factory parameter for messageType. See if that change is consistent with the current successful run of DracoGenTest *(followed by the pasted run: DrakeGenTest output — 211 tests run, 211 succeeded, all passed.)*

**[Claude]** Two things, and the second matters: **that run never looked at `ActorAspect.drake`.** *(Diffed the drake edit, read `DrakeGenTest`'s exclusion lists, fixed the file.)*

`DrakeGenTest` skips it — `ActorAspect.json` is listed in `authoredAhead` (`DrakeGenTest.scala:31`, *"present-empty heads + start/message/signal rename (Phase 2b)"*). So the 211 passing tests say nothing about that file, and the edit as written has two typos that nothing would have caught. Fixed both: `messageTy` → `messageType`, and `start`'s type `Acpetion` → `Action` (that one looks like a stray keystroke on an untouched line).

**What the green run actually confirms.** `DracoGenTest.excluded` contains neither `ActorAspect.json` nor `Draco.json`, and `comparisonOnlyExcluded` is `Map.empty` — so both were compared against their on-disk Scala and matched. That means my hand-emission of `ActorAspect.scala` is exactly what the Generator produces, and `Draco.scala` is unchanged, so the `messageType` plumbing is confirmed inert as intended. The parameter shape you settled is correct in the JSON and Scala projections.

**What it doesn't confirm.** Nothing in the drake projection for this type. Beyond the typos, your authored order is `messageType, start, message, signal` while the JSON is alphabetical (`message, messageType, signal, start`), so the file still wouldn't exact-match the emitter. That divergence is pre-existing — it's the `start/message/signal` reordering already cited as the reason for the exclusion — so the typos were the only *new* inconsistency, and the file legitimately stays excluded until Phase 2b.

Worth knowing: the drake corpus has a real gap here. Three files sit in `authoredAhead` (`Action`, `BodyElement`, `ActorAspect`), and edits to those are unguarded in both directions — a typo like `Acpetion` survives indefinitely. Everything else in `src/main/resources/draco` is exact-matched.

The semantic step is still queued and unchanged: drop `Actor[DracoType]` from `Draco.json`'s derivation so actor-ness rests on the aspect alone. Want me to take it now?

---

**[Dev]** Wow, that was a messed up typo.  I wish I had noticed it. Let's get rid of generating extends Actor[DracoType}, if we don't need todo anything else first, and then address and try to eliminate the test exclusions.

**[Claude]** No need to dwell on it — it's fixed. Taking the derivation drop now; let me first predict the exact emission change so I can update both projections. *(Checked the `new Actor[` emission site, `Draco.drake`, the drake emitter's actor block.)* One prerequisite, and it's exactly the failure mode we just fixed.

The drake emitter doesn't emit `messageType`. `Generator.scala:567` builds the actor block as `"actor" +: (block("start", …) ++ block("message", …) ++ block("signal", …))` — start, message, signal only. That's why the 211 passed with `Draco.json` already carrying `messageType`: the emitter ignores it.

So today the message type reaches the drake surface *only* through line 1 of `Draco.drake`, `type Draco from Actor(DracoType)`. Drop the derivation and that line becomes bare `type Draco`, the actor block still says nothing about `T`, and the drake projection silently loses the message type. `Draco.json` is not in `authoredAhead`, so I'd also have to edit `Draco.drake` to keep DrakeGenTest green — meaning the loss would be committed as green.

Right order is therefore: teach the drake emitter to emit `messageType` in the actor block and add the line to `Draco.drake` (additive, gated, should stay green), *then* drop the derivation and update line 1 — at which point `T` lives in the actor block and nothing is lost.

The one thing I won't decide is the drake surface form, since that's your authoring surface. My recommendation is a head-plus-payload line mirroring `domain draco Draco`:

```text
actor
  messageType DracoType
  start
    ...
```

Give me that form (or the one you want) and I'll do the emitter change, then the derivation drop as a second gated step.

Meanwhile, the exclusion landscape you asked about, so the shape is clear before we start:

- **`DracoGenTest.excluded`** — 13 entries, all the `TypeElement` sealed-trait family, and each is covered by the group test that generates them as one file via `generate(Seq)`. Those aren't drift holes; they're a different comparison path. Retiring them means per-type tests would have to understand family emission.
- **`DrakeGenTest.authoredAhead`** — 3 entries (`Action`, `BodyElement`, `ActorAspect`), and these *are* real holes: unguarded in both directions, which is how `Acpetion` survived. Two cite the presence model not yet being in JSON, one cites the Phase-2b rename plus element ordering.

So "eliminate the exclusions" is mostly those 3, and at least two of them are blocked on the presence model rather than on cleanup — worth confirming before we treat it as a tidy-up.

---

**[Dev]** The context independent type ""names" should actually be type expressions that allow for algebraic data types. TypeName itself contains most of the components for an ADT. By "context independent", I mean a type name that that does rely on the format context of json or the language context of drake.

**[Claude]** That reframes something I flagged earlier. Let me ground it in what the corpus actually contains. *(Censused every `valueType` and `typeParameters` string in `src/*/resources`.)* You're describing the type-level counterpart of a move already landed at the value level, and the corpus backs it hard.

**112 distinct `valueType` strings, all Scala type expressions in text form:** arrows — `TypeDefinition => String`, and `Map[String, Seq[String] => Unit]` (an arrow *nested inside* an application); applications — `Seq[String]`, `mutable.Set[Int]`, `ActorRef[draco.format.json.JSON]`; qualified paths — `org.evrete.api.StatefulSession`, `java.nio.file.Path`.

**29 distinct `typeParameters` strings, already covering four constructors:** variables (`T`, `F`, `L`, `K`, `V`); bounded variables (`T <: Product`, `S <: DomainType`, `T <: DracoType`); products (`(Double, Double)`, `(Gaze, Distance)`, `(S, T)`, `(Percept, Effect)`); applications (`Seq[Waypoint]`).

So the corpus is *already* writing type expressions; it just writes them in Scala syntax and stores them as opaque strings. Every one of those operators — `=>`, `<:`, `[…]`, `(,)`, `.` — is host-language syntax sitting inside what's supposed to be a neutral definition.

**On "TypeName has most of the components":** it does, and the gap is one field. `name` is the constructor, `namePackage` is qualification *already structural*, `typeParameters` is the argument list — but typed `Seq[String]` instead of `Seq[TypeName]`. Make that recursive and `TypeName` *is* a type expression tree: `Seq[String]` becomes `Seq` applied to `String`; `TypeDefinition => String` becomes `=>` applied to two arguments; `Map[String, Seq[String] => Unit]` nests without special-casing. Note the tell — `org.evrete.api.StatefulSession` as a flat string is a *regression* against `namePackage`, which already models exactly that.

**The precedent is yours already.** `TypeElement.value` became `Json` carrying `{op: [operands]}` trees, with `isString` = host-opaque and `isObject` = native, fixity living only at the drake surface. Type expressions are the same normative-tree move one level up, and the drake vocabulary is sitting there too — `[T]`/`{T}`/`(A,B)` are renderings of `Seq`/`Set`/product, not syntax the model should own.

**On "context independent" — `TypeName` currently carries both contexts as derived members.** `namePath` produces `draco.DracoType` (dotted *language* context) and `resourcePath` produces `/draco/DracoType.json` (JSON *format* context). Both are computed in the factory body and, as I confirmed earlier this session, appear in neither the encoder nor the decoder. So they're already recognized as derived — the point your framing sharpens is that they're derived *into one specific context*, which is precisely what belongs in a renderer instead.

That also dissolves a question I raised two steps ago. I flagged bare `{"name": "DracoType"}` versus `("DracoType", ["draco"])` for `messageType` and deferred it. Under type expressions there's no choice to make: qualification is structural, and whether the package prints is the target renderer's business.

**One caveat on ADTs.** Sums are the one constructor the corpus does *not* express. The `TypeElement`/`BodyElement` sealed family is a sum, but it's carried by `derivation` plus a codec discriminator rather than by a type expression. I'd check but not assume a link to your other ask: those 13 `DracoGenTest` exclusions exist because the family emits as one Scala *file* (Scala 2 requires sealed variants co-located), which is a target-language layout rule — so first-class sums might not retire them on their own.

**Recommendation.** File this as a design issue now and don't start it — it touches `TypeName`, `TypeElement.valueType`, the codecs, both projections, and every JSON in the corpus, and it deserves a deliberate start rather than riding in behind the actor work. Then finish the derivation drop with the simplest provisional drake form (`messageType DracoType`), explicitly marked as a rendering we expect to revisit.

And I'd revise what I told you about the exclusions: I called two of the three "blocked on the presence model" and framed the rest as cleanup. With this in view, `ActorAspect`'s exclusion is plausibly waiting on type expressions too. Treating that cleanup as tidy-up would have been wrong — it's downstream of two unbuilt models.

Want me to write the issue?

---

**[Dev]** Yes, but first tell me what needs to be done before I start it, because I don't want to defer it beyond necessity.

**[Claude]** Let me confirm two things that bear directly on the answer. *(Checked the commit state and the `resourcePath` consumers.)* Short answer: two decisions and one small increment. Nothing here justifies a long deferral.

**Do first, because it deletes work you'd otherwise migrate.** The strip is committed (`eafc649`); the five actor files are uncommitted but green. Commit them, then finish the derivation drop — it *removes* `Actor[DracoType]`, a type-expression-bearing field. Migrating it into the new model and then deleting it is wasted motion. Three edits: drake emitter emits `messageType`, `Draco.drake` gains the line, then derivation out plus `Draco.drake` line 1 and the `Draco.scala` trait line. Short.

**Decide first — the fork, and `resourcePath` forces it.** Is this `TypeName` made recursive, or a new expression type with `TypeName` as its leaf? `TypeLoader.scala:14` is `tryLoad(typeName) = loadFromResource(typeName.resourcePath)` — the load path resolves a definition *by* the name's resource path. A named type has a resource; an arrow or a product does not. Make `TypeName` recursive and `resourcePath` becomes partial on its own domain, silently, right under the loader.

My recommendation: `TypeName` stays the atom that names a loadable type, and a type expression is a distinct tree whose *leaves* are `TypeName`s, with `typeParameters: Seq[TypeExpression]`. You still get everything you described — the components are all there, `namePackage` still carries qualification structurally — and the load path stays total. It also means `namePath`/`resourcePath` stay well-defined where they're used, and become explicitly the two context renderings you named rather than model members that only sometimes mean something.

**Decide first — #37, structural equality.** This is the one I'd genuinely not skip. `AssemblyValidator` already compares types by `namePath` *strings* as a Set key because `TypeName` equality is reference-based. Type expressions make equality pervasive — "is this `Seq[String]` the same expression as that one" becomes a question you ask constantly, on trees. Fixing equality on the flat type is far cheaper than on a recursive one, and doing it after means every tree comparison inherits the stringly workaround.

**Not prerequisites — don't wait on any of these.** The presence model is an independent axis. The test exclusions are downstream, not upstream — I had that backwards two turns ago and it's worth being explicit that clearing them is not a gate. The `problems` reply protocol is unrelated. The 12 mods actors on `Actor[T]` derivations are covered by the fallback and migrate naturally *with* this work rather than before it. And sums/ADT completeness is inside the increment, not ahead of it.

**One method note for the issue, not a prerequisite.** Build the renderer before migrating any JSON — trees render back to today's exact strings, byte-identity proves the tree carries everything, then convert the corpus family by family. That's the pattern that made both of today's increments verifiable, and without it 112 distinct strings go red simultaneously with nothing to tell you which are wrong.

So: commit, finish the derivation drop, settle the fork and #37. Want the issue written with that prerequisite section in it, and my recommendation on the fork flagged as a recommendation rather than a decision?

---

**[Dev]** I would like to see type expressions in json and drake, close to or the same as value expressions in json and drake. Is this part of the deferred issue?

**[Claude]** Not yet — the issue isn't written, so nothing is in it. But that's a scope change worth catching before I write it, so let me check how well types actually fit the value convention. *(Censused the expression ops in use; drafted and filed the issue.)* It isn't in the issue yet, but it should be its spine rather than a footnote — and it's already happening in one place. From `Action.json`:

```json
{ "kind": "Fixed", "name": "valueType", "valueType": "String",
  "value": { "->": [ { ".": ["org","evrete","api","RhsContext"] }, "Unit" ] } }
```

That's `org.evrete.api.RhsContext => Unit` — **a type expression written in the value-expression form**, because the author needed to produce that type and preferred a tree to an opaque string. The element is even *named* `valueType`: draco describing the very field we'd be converting, using the form you're asking for.

The ops already carry it. Live in the corpus today: `"."` qualification (19 uses), `"->"` arrow, `"()"` application, plus `"=="`, `"||"`, `"if"`, `"="`. Types would add bounds (`<:`) and sums; values keep named-args and conditionals. Application, qualification and tuple are genuinely shared — so "close to or the same as" is well-founded, not aspirational.

**But it exposes a conflict with what I recommended last turn, and I'd rather flag that than paper over it.** I said keep `TypeName` as the atom and give expressions `TypeName` leaves. Under full unification, qualification is `{".": [...]}` — which is a *second* representation of what `TypeName.namePackage` already models structurally. So either `TypeName` survives only for loadable named types (where `resourcePath` is load-bearing) with a conversion at the load boundary, or type expressions use `TypeName` leaves and deliberately diverge from the value convention on qualification. That's the crux, and it's a decision, not a detail. Minor companion: the trees say `->` while the strings say `=>`.

Filed as **#51**. The issue records the census (112 `valueType` strings, 29 `typeParameters`), your context-independence framing with `namePath`/`resourcePath` named as the two context renderings, the shared-operator argument including the `isEmpty` case where the arrow appears on both the type and value side of one element, and the three prerequisites — finish the actor increment, settle the qualification question, do #37 — plus the explicit "don't wait on these" list so it can't drift.

The one thing I did **not** resolve is the qualification conflict, and I want to be plain that it's a live contradiction rather than a choice awaiting write-up: my recommendation last turn (keep `TypeName` as the leaf) and your requirement this turn (same form as value expressions, where qualification is `{".": [...]}`) can't both hold. `TypeLoader.tryLoad` resolving definitions by `resourcePath` is the constraint that decides it, and it's recorded as the issue's central decision.

Nothing is labelled `priority-next`. Current state: `eafc649` committed, five actor files uncommitted and green, and the derivation drop still waiting on the drake surface form for `messageType` — which #51 now suggests should be provisional and cheap rather than designed, since it's a rendering that work will revisit.

---

**[Dev]** Action definition is a flawed outlier. I've updated Action.drake to see if I could propose a better, more consistent and comprehensible definition.

**[Claude]** Let me ground this in what the Action actually becomes when a rule is generated. *(Diffed `Action.drake`, read a generated rule, checked the emission site.)* Your read is right, and the generated code shows exactly how. `Generator.scala:1448` emits:

```text
private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
```

That line is **hardcoded**. `Action.json`'s `name` and `valueType` don't drive it — nothing reads them. They're decorative self-description, and they describe the wrong things: they say the element is *named* `ctx` with type `RhsContext => Unit`, when `ctx` isn't the element's name at all, it's a parameter binder. That conflation is the outlier.

**Your `par` instinct is correct and grammatically sound** — `Parameter` is a `BodyElement` in the sealed hierarchy, so `par` belongs in a factory body. It replaces a name-override with the thing the parameter list exists for.

**But the type landed on the wrong side.** Read it straight off that generated line: `ctx` is an `RhsContext`; `Consumer[RhsContext]` is the type of the *action*, not of `ctx`. Your line says `ctx : Consumer(RhsContext)`, which is the action's type attached to its own parameter. The honest decomposition of `Consumer[RhsContext] = (ctx: RhsContext) => Unit` is two facts on two different slots:

```text
    body [
      par ctx org.evrete.api.RhsContext
      fix variables [Variable] _variables
      fix body [BodyElement] _body ]
```

— with the action's own type, if stated, belonging in `valueType`. And there the *old* value was arguably right in kind: `org.evrete.api.RhsContext -> Unit` is the context-independent arrow, and `Consumer[RhsContext]` is its Scala rendering. So under #51 the arrow is what the definition should carry and `Consumer[…]` is what the Scala target emits from it. The flaw was never the arrow; it was `name`.

That also names the real prize: once the description is accurate, line 1448 can *read* it instead of hardcoding it. Right now Action's self-description is inert, which is the deeper sense in which it's an outlier — a self-describing type whose self-description nothing consumes.

**A bonus you may not have intended.** Dropping the `rule`/`actor` present-empty heads removes the stated reason for `Action.json`'s exclusion — `DrakeGenTest.authoredAhead` cites *"present-empty rule/actor heads (presence model not yet in JSON)"*, and `Action.json` has only `typeName`, `dracoAspect`, `domainAspect`. So drake and JSON now agree on that point and the file may be able to leave the exclusion list. `BodyElement.json` carries the identical reason, so the same edit likely frees it too — two of the three exclusions you wanted gone, without waiting on the presence model.

**Recommendation:** move `par ctx` to `org.evrete.api.RhsContext`, keep the arrow as `valueType`, and make the matching edit in `Action.json` — the JSON still has the old four-element body, and since the file is excluded nothing will flag the divergence. Then try lifting `Action.json` and `BodyElement.json` out of `authoredAhead` and see whether the emitter reproduces them; that's a cheap test of how much of the exclusion problem was this outlier rather than the presence model.

Want me to make those edits, or would you rather author the drake and have me follow with the JSON?

---

**[Dev]** I want to see how you think it should be.

**[Claude]** Let me check one thing first — how a function-valued element is already modelled elsewhere. *(Censused the 21 function-valued Dynamics; authored `Action.drake` and the matching JSON edits.)* Confirmed, and it sharpens the diagnosis — my advice last turn was half wrong. Every function-valued element in the corpus (21 Dynamics) uses one convention:

```text
CLI.inspect        params=[(path, String)]        valueType=Unit
TypeLoader.rooted  params=[(td, TypeDefinition)]  valueType=TypeDefinition
```

**`parameters` carries the parameters; `valueType` carries the *result*. The arrow is never written — it's reconstructable.** So I was wrong to tell you to keep `RhsContext -> Unit`. Action isn't an outlier only for misusing `name`; it's an outlier for spelling out an arrow that the whole corpus leaves implicit. By the inference rule you already apply — explicit iff not reconstructable — that arrow shouldn't be there at all.

That's the definition I'd argue for. Three changes, one per defect:

| Was | Now | Why |
|---|---|---|
| `fix name String "ctx"` | *(deleted)* | The element isn't named `ctx`. This was the actual flaw — a parameter binder written into the element's own name slot. |
| `fix valueType String org.evrete.api.RhsContext -> Unit` | `fix valueType String Unit` | `valueType` is the **result**, matching all 21 Dynamics. The arrow is reconstructable from parameter + result. |
| — | `par ctx org.evrete.api.RhsContext` | Your placement instinct, with the type on the right side. `ctx` is an `RhsContext`; `Consumer[RhsContext]` is the *action's* type, which under this convention doesn't get written down. |

Read together it now says what the thing is: an element taking one parameter `ctx: RhsContext`, returning `Unit`, carrying `variables` and `body`. Nothing about `Consumer` or arrows — those are the Scala target's rendering, which is where they belong.

**What I couldn't verify, and deliberately didn't guess at.** A `Parameter` sitting in a *factory body* is new — `factoryBody` derives its overrides from the factory's parameters, so it likely doesn't yet know to emit `override lazy val parameters: Seq[Parameter] = Seq(Parameter("ctx", …))`. I left `TypeElement.scala` untouched rather than hand-writing what I think it should become; the test diff is more reliable than my guess.

`Action.json` is in `DracoGenTest.excluded`, but only from the *per-type* comparison — it's a `TypeElement` family member, and the family group test generates `TypeElement.scala` from the whole family, so this change does flow into a real gate:

```bash
sbt "testOnly draco.DracoGenTest"
```

Expect it to fail, and the diff to tell us exactly how much Generator work the `par` implies. If it turns out to be more than a small change, the fallback that still fixes the outlier is dropping `name` and setting `valueType` to `Unit` without the `par` — accurate as far as it goes, with the binder left as the target's choice.

One consequence worth noting: with the present-empty heads gone, `Action.json`'s stated reason for sitting in `DrakeGenTest.authoredAhead` no longer holds. Worth trying to lift it out in the same pass — that would be the first of the three exclusions actually retired rather than deferred.

*(Delegated action — Dev pasted the full `DracoGenTest` run: 127 tests run, 126 succeeded, **1 FAILED** — the `TypeElement` family group test, with the `Local` diff at `DracoGenTest.scala:192`.)* The test gave us the truth, and it corrects me on the main point. *(Checked the sibling elements' JSON, reverted `Action.json`, re-authored `Action.drake`, fixed `TypeElement.scala` back.)* Definitive. The family convention is unambiguous, and it corrects my main claim:

| element | `name` | `valueType` |
|---|---|---|
| Pattern | `""` | `"org.evrete.api.Knowledge => Unit"` |
| Condition | `""` | `"Boolean"` |
| Monadic | `""` | `"Unit"` |
| **Action** | **`"ctx"`** | `RhsContext -> Unit` |

**I was wrong that `valueType` should become `Unit`.** I generalized from the 21 Dynamics, but Dynamics carry a parameter list, so their arrow is reconstructable. `Pattern` — Action's actual sibling, the other rule-side function element — has no parameter list and writes the full arrow `Knowledge => Unit`. Action's arrow is correct and must stay. Stripping it to `Unit` would have made Action inconsistent with the very element it should match.

So the outlier is one thing only: `name = "ctx"` where every sibling has `""`. And the `par ctx` line has no Generator support — it emitted a dangling `override lazy val ctx: org.evrete.api.RhsContext =` with no right-hand side. My `"value": "Unit"` was also unquoted where the family uses `"\"Unit\""`.

Reverting to the minimal correct change: the definition is now one line different from before, and Action matches its siblings exactly.

What the failure actually taught us, beyond my two mistakes:

**`par` in a factory body isn't a thing.** `factoryBody` treats every body element as an override, reading name + valueType + value — so a `Parameter` (which has no `value`) emitted a dangling override. Your instinct that `ctx` belongs in a parameter slot rather than in `name` was right about the modelling; there's simply no slot for it today, and `Pattern` shows the family's answer is to not model the binder at all. Line 1448 hardcoding `ctx` is consistent with that.

**The arrow is not the outlier.** `Pattern` writes `Knowledge => Unit` for the same reason Action writes `RhsContext => Unit` — neither has a parameter list, so the arrow *is* the type. I mistook the Dynamic convention for a universal one; it only applies where a parameter list exists to reconstruct from. That was the substantive error in what I proposed, and the test caught it before it went in.

**Action is still ahead of its siblings in one respect, correctly.** Its `valueType` is a native tree `{"->": [{".": [org, evrete, api, RhsContext]}, "Unit"]}` while Pattern, Condition and Monadic use flat quoted strings. That's not a flaw — it's the only element in the family already written the way #51 wants all of them, which is why it was the evidence for that issue. Worth leaving as is.

So the outlier reduces to what you spotted: a binder name sitting in `name`. Everything else about the definition was already right, and the present-empty head removal still stands on its own — `Action.json` may now be liftable out of `DrakeGenTest.authoredAhead`, which is worth trying once this is green.

---

**[Dev]** What do you think of creating a domain for each of the non-native dependencies of the draco framework? A domain for each target language,  rete (rules) and actor.  That would allow DracoType wrappers for all the third party dependencies data types. Part of the language neutral definitions, is how the third party data types are directly referenced.

**[Claude]** Let me size it before answering — how many distinct third-party types actually appear in definitions. *(Censused qualified third-party types across the corpus.)* Yes — and this is the recorded north star, not a new direction. The 2026-06-14 note says it almost verbatim: a definition in `src/main` must never name an implementation type; it names a *capability* type — `draco.rete.Session`, `draco.actor.Ref`, `draco.format.json.Json` — with the binding to the real library living in `src/mods`. The pattern is already proven for one dependency: `draco.format.Format[T]` is abstract in main, the circe binding sits in mods.

**And it is not what the deleted session did.** Worth saying plainly, because the resemblance is superficial and shouldn't make you hesitate. That session moved Evrete *configuration* into main — session construction, `ActivationMode` — which is HOW, and belongs in mods. Yours moves *type names* into main and leaves the binding in mods, which is WHAT. Same axis, opposite directions.

**It's the missing half of #51, and that's the strongest argument for it.** Type expressions give you structure but not neutrality: `{".": ["org","evrete","api","RhsContext"]}` is still Evrete sitting in a main definition, merely in tree form. Wrapped types give the *leaves* a neutral vocabulary. Neither half achieves language neutrality alone — which is exactly your point that how third-party types are referenced is part of the neutrality problem, not separate from it. I'd fold this into #51's design rather than treat it as a successor.

**The scope is smaller than it sounds.** Across the entire corpus there are only **11 distinct qualified third-party types**: Evrete 2 (`StatefulSession`, `Knowledge` — plus `RhsContext`, which my scan missed because it lives inside Action's arrow *tree* rather than a string, so really 3), Pekko 1 (`ActorRef`), circe 2, `java.*` 4, `scala.io` 1, `mutable.Set`. A `draco.rete` of three types and a `draco.actor` of one or two is a genuinely small build for a large payoff.

**One refinement.** I'd not put a domain per target language in `src/main`. The north star's shape is capability domains in main (rete, actor, format) with each target language supplying its *own* binding layer in mods — otherwise main carries N×M. `ScalaTarget`/`ScalaSource` already exist but are about emission, a different axis from type vocabulary; I'd keep them out of this.

**The real cost isn't in the 11 — it's the prelude, and it's invisible in that census.** `Seq`, `Option`, `Map`, `LazyList`, `Array`, and the scalars `String`/`Int`/`Double`/`Boolean`/`Unit` are unqualified, so they don't read as third-party, but `Seq` is Scala and Haskell offers `[]`/`Maybe`/`Map`. `Seq[String]` appears 13 times, `Seq[Int]` 11, `Unit` 99, `String` 64. Wrap the 11 and leave those, and you get partial neutrality with the largest category untouched; wrap them too and you're designing a draco prelude, which `draco.base` doesn't currently provide. That decision — not the dependency domains — is what makes this a small job or a large one.

My recommendation: commit to it as the vocabulary half of #51, build `draco.rete` and `draco.actor` at the 11-type scope where the payoff is immediate and the risk is low, and settle the prelude question as an explicit in-or-out call up front rather than discovering it midway.

Want me to record this on #51, or open it as its own issue cross-linked to both #51 and #49?

---

**[Dev]** Keep it with #51 and add a link between that and #49 if that makes sense.

**[Claude]** *(Wrote the comments; posted to both issues.)* Recorded on both, linked in each direction.

**#51** now carries the wrapped-types proposal as the issue's vocabulary half, with the argument for why it belongs there rather than after: an expression tree is only context-independent if its leaves are, so "what does a `TypeName` name" and "what wraps `org.evrete.api.RhsContext`" are one question, and #51's design should assume wrapped leaves from the start. It includes the 11-type census, the refinement against per-target-language domains in main, and the prelude question flagged as the thing that decides whether this is small or large.

**#49** gets the reciprocal link. The cross-link does make sense, but with a caveat I made explicit in both places rather than letting the shared name imply more than it should: the two issues would create `draco.rete` from opposite sides — #49 from evaluation discipline and session configuration, #51 from data-type vocabulary. They're compatible and probably land together, but the WHAT/HOW line that the rolled-back attempt crossed runs right between them, so I restated it on both so a future session can't reconstruct the confusion.

Working tree still has the actor increment plus the Action fix uncommitted, and `DracoGenTest` is unverified since the `name` correction — that's the one outstanding thing before any of this becomes actionable.

---

**[Dev]** I think we can create an initial pass at the draco.rete.Rete domain, to address the Action fix, which executes but still can be improved with type wrappers for evrete. The test passed and I don't want to commit it before I attempt to improve it. The initial Rete domain can just wrap the types that are currently directly referenced by draco definitions, and maybe just a subset of those.

**[Claude]** Good — additive first, so the domain exists and generates before anything repoints at it. Let me get the exact templates for a bare type and a one-type domain. *(Read the `Source`/`Nominal`/`Base` templates; created the twelve files.)* Templates are unambiguous. Creating the domain plus the three Evrete types that definitions actually name — generating the members uniformly rather than hand-typing nine files. First pass is in — purely additive, nothing repointed at it yet.

```bash
sbt "testOnly draco.DracoGenTest draco.DrakeGenTest"
```

**What's there:** `draco.rete.Rete` as a one-type-per-file domain (mirroring `draco.base.Base`), with the three Evrete types that draco definitions actually name — `Knowledge`, `RhsContext`, `StatefulSession`. Twelve files: JSON, drake and Scala for each, hand-emitted from the templates. The test will arbitrate whether my emission matches the Generator; if it's off it'll be off uniformly across all four, so one diff fixes all of them.

**Shape, deliberately.** These are opaque markers — `trait RhsContext extends DracoType`, no elements, no factory. They're *references* to foreign types, not reimplementations of the Evrete API. That keeps this on the right side of the line that sank the earlier attempt: type names in `src/main`, no Evrete configuration or construction anywhere near it.

**What it does not yet do**, and I'd keep these as separate steps rather than folding them in:

- Nothing references them. `Action.valueType` still says `org.evrete.api.RhsContext -> Unit`. Repointing it is the next step and is where the real question lands — the Generator needs a binding from `draco.rete.RhsContext` to the host type, and `externalTypeImports` is the embryo of that, per the north star.
- Worth knowing before that step: `Action.valueType` is descriptive only — nothing consumes it, since `Generator.scala:1448` hardcodes the emission. So repointing Action alone changes an emitted string literal and proves nothing about the binding. The load-bearing sites are `RuleType`'s `Consumer[Knowledge]`/`Consumer[RhsContext]` elements and `Draco.json`'s `StatefulSession` — those actually drive emission, and they're the honest test of whether the wrapper works.

One thing to watch: `draco.rete.Knowledge` and `org.evrete.api.Knowledge` now share a simple name. Nothing imports both today, but the binding step will put them in the same file, so the Generator's import handling will need to disambiguate.

*(Delegated action — Dev pasted the full run of both gen tests: 223 tests run, 223 succeeded, all passed — the four new rete types matching in both projections.)* All 223 green, and the hand-emitted Scala and drake for all four new types matched the Generator exactly. The Action `name = ""` fix is confirmed too.

Now the actual point of the exercise — putting the wrappers to use. *(Checked `Pattern.drake`; applied the repoint edits.)* Two things before the edit. `Pattern.drake` shows the emitter's canonical style is **unbracketed** (`elements` / `factory` / `body` with no `[ ]`), while your `Action.drake` uses brackets — so lifting `Action.json` out of `authoredAhead` needs more than the present-empty heads I mentioned; the whole block style differs. I was too optimistic about that being cheap.

And Pattern carries the identical host reference (`org.evrete.api.Knowledge => Unit`), so repointing Action alone would re-create the very inconsistency we just removed. Doing both: repointed. `Action` and `Pattern` now name `draco.rete.RhsContext` and `draco.rete.Knowledge` instead of Evrete paths.

**Be clear about what this buys, though** — I'd rather you know the limit than think the wrapper is proven. Both of these sites are *descriptive*: nothing consumes `Action.valueType` or `Pattern.valueType`. So this changes an emitted string literal and makes the definitions say the neutral thing, but it exercises no binding. The wrapper types are still referenced only as text.

The grep shows the two sites that would actually test it, and I left both alone deliberately: `Draco.json:43` — `"valueType": "org.evrete.api.StatefulSession"`, which drives real emission in the actor `start` body — and `primes/Primes.json:14` — `"valueType": "org.evrete.api.Knowledge"`, likewise. Repointing those needs the Generator to map `draco.rete.StatefulSession` back to the host type and emit the right import — the `externalTypeImports` binding the north star calls "the per-L binding selection in embryo." That's the increment that proves the idea, and it's a Generator change rather than a definition edit, so it wants its own step and its own gate.

Also worth noting what the wrappers are *not* yet: `draco.rete.Knowledge` has no relationship to `org.evrete.api.Knowledge` anywhere in the tree. Nothing declares the binding — main has a name, mods has the library, and no edge connects them. That edge is the actual content of the capability-domain idea, and it doesn't exist yet.

So current state: three uncommitted increments stacked up — the actor `messageType` work, the Action/Pattern outlier fix, and this rete first pass. All independent, all green as of the last run. Once this passes I'd suggest committing them as separate commits rather than one, since they're separable and the rete one is the speculative one.

---

**[Dev]** An important function of the type wrappers is to constrain the references to type parameters within TypeName to "atomic terms" rather than allow unconstrained type expressions. I haven't explicitly  mentioned this constraint yet, because I just realized it's value in preserving the simplicity of how TypeName can contain the necessary formal and actual parameter binding, without having to parse expressions.

**[Claude]** That's a load-bearing constraint, and it changes #51's central open question. Let me ground how far it already holds. *(Censused all 66 `typeParameters` occurrences.)* That constraint does more than simplify — it **resolves the central open decision on #51**. And the corpus is already very close to satisfying it.

| kind | occurrences | distinct | where |
|---|---|---|---|
| atomic (plain + qualified) | 52 | 19 | everywhere |
| bounded | 6 | 5 | **formals only** |
| tuple | 7 | 4 | `(S, T)` in main; rest in mods `sentient` |
| application | 1 | 1 | mods `Path` → `Primal[Seq[Waypoint]]` |

**In `src/main` the constraint is violated in exactly one distinct way** — `Holon(S, T)` on `DomainTransform` and `TypeTransform`. Everything else compound lives in the mods sentient example. So enforcing "actuals are atoms" costs one named type in main and a handful in mods. That's about as cheap as a structural constraint ever gets.

**The bounded rows show the model is even simpler than you said.** All 6 bounds sit on *formals* — `Holon` declares `T <: Product`, `DomainTransform` declares `S <: DomainType`. Not one appears as an actual. So `TypeName.typeParameters` needs to carry, at most, a name plus an optional bound that is itself an atomic reference. Formals are a pair of atoms; actuals are a single atom. No recursion, nothing to parse — exactly the "necessary formal and actual parameter binding" you're after, and it keeps `resourcePath`/`namePath` total, since every `TypeName` still names a loadable type.

That kills the fork I'd flagged as #51's crux. There's no longer a choice between a recursive `TypeName` and a separate expression type with `TypeName` leaves: `TypeName` stays the atom, full stop.

**It also reframes what the wrappers are for.** I'd described them as supplying neutral *names*; your point is that supplying an atomic name is the mechanism by which composition gets pushed out of `TypeName` and into definitions. A wrapper is how a would-be expression becomes a term.

**One question replaces the one it resolved, and it's worth deciding deliberately: does the atomic constraint extend to `valueType`?** That's where the expression surface actually lives — 34 distinct applications and 7 arrows, including `Map[String, Seq[String] => Unit]`. Two coherent answers:

- **Atoms in `TypeName`, expressions in `valueType`.** Principled rather than expedient: `typeParameters` is about type *identity and binding* — nominal, atomic; `valueType` is about what an element *holds* — structural. #51's "same form as value expressions" then applies to `valueType` only. My recommendation.
- **Atoms everywhere.** Requires naming ~40 compounds, and `Map[String, Seq[String] => Unit]` is unpleasant to name. This is the prelude explosion arriving by a different road.

It narrows but doesn't erase the qualification conflict I flagged: `TypeName` keeps `namePackage` structurally, `.`-trees stay inside `valueType` expressions. Still two representations — but now each has a clear jurisdiction rather than both competing for the same slot, which is a real improvement over where that stood.

Recommendation: record this on #51 as superseding its stated central decision, with the `valueType` question as the replacement. Want me to write that up?

---

**[Dev]** Yes.

**[Claude]** *(Wrote and posted the #51 comment; edited the issue body.)* Now marking the superseded section in the body so it can't be read as current. Recorded on #51, and the issue body's "central decision" section is now marked superseded rather than left to be read as current — the original reasoning is retained beneath the banner, since the `resourcePath` constraint that drove it is still what makes the atomic form work.

The write-up captures the constraint, the census showing `src/main` violates it in exactly one distinct way (`Holon(S, T)`), and the refinement the data surfaced: bounds occur only on formals, never on actuals, so a formal is a pair of atoms and an actual is a single atom — `TypeName` needs no nesting in either role. It also carries the `valueType` question as the explicit replacement for the fork, with the atoms-in-`TypeName`/expressions-in-`valueType` split recommended and the reason it's principled rather than convenient.

Where things stand on the working tree: four uncommitted increments, all green as of the last run except the Action/Pattern repoint, which hasn't been tested yet. They're independent and I'd commit them separately: the actor `messageType` wiring, the Action outlier fix, the `draco.rete` first pass, and the Action/Pattern repoint onto the wrappers. The rete pair is the speculative one and the one most worth isolating in its own commit given the history.

---

**[Dev]** Before more testing, let's continue with type wrappers, specifically about how to reduce or maybe eliminate the need for the namePackage to be a part of a draco type expression. It seems to  me that we can limit the explicit use and reference of namePackage sequence to the inside of type wrapper definitions (or wrapper type? another non-commuting order semantic duality?). Drake examples:

* External: type WorkingMemory from [ org evrete api] StatefulSession domain draco rete Rete
* Inter-domain:
*            type  Volume from  [ draco base ] Coordinate (Meters,Meters,Meters)
*            domain draco space Space
*
*            type Surface from [ draco base ] Coordinate (Meters,Meters)
*            domain draco space Space
*
* Intra-domain:
*            type ParametricSurface from Volume (Surface,Surface,Surface)

The above is intended as a notional example of how to atomize type expressions using type wrappers

**[Claude]** That reframes what I built, so let me measure the corpus against it before responding. *(Censused derivation references by package position.)* Measured against the corpus, and the containment you're proposing is largely an invariant that already holds.

**`typeParameters` occupy exactly two positions, and compounds are already confined to one of them.** 23 sit on the declaring `typeName` (formals — `T`, `K`/`V`, `S <: DomainType`); 43 sit inside derivation entries (actuals). Every compound — the tuples, `Seq[Waypoint]` — is in the derivation actuals; not one formal is compound. So "expressions live only at the definition head" isn't a change to the corpus, it's a rule the corpus already obeys and that you'd now be enforcing.

**namePackage: 55% of it is noise today.** Of 102 derivation references, **56 are same-package** and would go bare — `draco.BodyElement` 11×, `domains.sentient.Sentient` 10×, `draco.base.Unit` 3×. The 46 cross-package ones are led by `draco.Actor` 12×, `draco.Primal` 11×, `draco.format.json.JSON` 8×. And those cross-package sites are largely the wrapper sites themselves: `sentient/Direction` deriving `[draco] Primal (Double, Double)` *is* your inter-domain example. Brackets and compounds co-occur at the same position, which is what makes this one rule rather than two.

**Two defects it would eliminate**, both found by the census: `Dictionary.json` derives `Map` with **no namePackage at all** — a host type in a derivation, invisible to any package-based audit. And `test/.../Natural.json` derives `Primal[Int]` with the application **baked into the name string** rather than `typeParameters` — the same information in a second encoding. Atomization makes that unrepresentable.

**Your notation exposes that what I built is wrong in two ways.** The rete types mirror Evrete's names (`draco.rete.StatefulSession`); `WorkingMemory` carries draco vocabulary and confines the foreign name to the derivation, which is better and avoids the `draco.rete.Knowledge` / `org.evrete.api.Knowledge` collision. More seriously, my wrappers have **no derivation at all** — they're bare rooted types that wrap nothing. Your `from [ org evrete api ] StatefulSession` supplies exactly the missing edge I flagged: the derivation *is* the binding.

**Which surfaces the one thing I'd push back on: `from` may be conflating two different relations.** Inter- and intra-domain wrapping is genuine subtyping — `Volume` *is* a `Coordinate[(Meters,Meters,Meters)]`. Derivation is right there. But `WorkingMemory` is not a subtype of Evrete's `StatefulSession` in any draco-meaningful sense, and if `from` emits Scala inheritance you've put an Evrete interface into `src/main`'s hierarchy — moving the dependency from a leaf position into the inheritance graph, which is worse than where it sits now. It also hardcodes one language's answer: the same wrapper needs a *different* external target per target language, and under the north star that binding belongs in mods. A distinct slot for the external case — `binds` rather than `from` — keeps main's hierarchy clean and gives the per-language binding somewhere to live.

On the duality: **"wrapper type" is the more accurate order.** It *is* a type — a first-class member of a domain — whose role is wrapping. "Type wrapper" reads as a mechanism applied to types. The asymmetry is real rather than merely stylistic, because the wrapper is always a draco type while the wrapped thing may not be one at all. Whether that rises to a non-commuting duality of the kind elsewhere in the model I wouldn't claim on this evidence.

Recommendation: adopt the containment rule as stated for intra- and inter-domain references, take the same-domain-implicit reduction, and split the external case into its own relation rather than `from`. Then rename the three rete types to draco vocabulary and give them that binding. Want me to record this on #51 and rework the rete wrappers accordingly?

---

**[Dev]** I don't see the necessity of creating a new keyword in drake to replace "from" in the case that the defined draco type inherits from a type outside of the draco type system.

**[Claude]** You're right, and my suggestion doesn't survive your own inference rule: whether a parent is outside the draco type system is already reconstructable from its package — `[ org evrete api ]` is not a draco domain — so a second keyword would make explicit what the reference already tells you. One relation, one word.

The concern I attached to it doesn't need the keyword either. Putting the foreign type in the hierarchy is a consequence of inheriting, not of what it's called, and the north star asks for couplings to be *localized*, not driven to zero — the wrapper is exactly that single named site. So `from` is right.

What survives is narrower and isn't a syntax question: `WorkingMemory from [ org evrete api ] StatefulSession` in `src/main` states the JVM answer, and a Haskell target needs a different parent for that same wrapper. Either the derivation's external target becomes target-selectable, or the wrapper definitions are themselves the per-language layer and a port replaces them. That's `Generator[L]` territory — worth noting on #51 when the time comes, not worth solving now.

*(Checked how an external parent actually renders — `derivationRef`.)* One concrete blocker for the wrapper shape, worth knowing before we write it. `Generator.scala:700` qualifies a derivation reference only in the self-shadow case — same simple name, different package. Everything else emits **bare**. So `WorkingMemory from [ org evrete api ] StatefulSession` would emit `extends StatefulSession` with no import, since `externalImports` scans element/factory/global valueTypes and not derivations. It wouldn't compile.

The minimal fix is in `derivationRef`: emit the fully-qualified parent when the entry's root package is neither `draco` nor the declaring type's own root. That leaves `org`/`java`/`io`/`scala` qualified inline and every current case untouched. Fully-qualified inline also sidesteps the `draco.rete.Knowledge` / `org.evrete.api.Knowledge` collision, which an import-based fix would walk straight into. It's inert today: no definition currently derives from a non-draco package. The only near-miss is `Dictionary` deriving `Map` with an empty package, which the rule skips.

So the rete rework would be: rename the three types to draco vocabulary with the foreign name confined to the derivation — `WorkingMemory from [ org evrete api ] StatefulSession`, and the same for `Knowledge` and `RhsContext` — plus that `derivationRef` change, then regenerate and test.

Two things I'd want your call on, since they're vocabulary rather than mechanism: `StatefulSession` → `WorkingMemory` is yours. For the other two I'd guess `Knowledge` → something like `RuleBase` and `RhsContext` → `Activation` or `Firing`, but these are draco's rules vocabulary and you may well have better words — or may want to keep `Knowledge`, since it isn't obviously Evrete jargon the way `StatefulSession` is. And whether `Action.valueType` and `Pattern.valueType`, which I repointed earlier, should follow the renames.

---

**[Dev]** The example of Dictionary.drake is a significant one, as it demonstrates a relationship between wrap types and the containing domains that is the systemic method for isolating fundamental dependencies of draco from draco framework users. The Idea is to use the isolation to  create alternative target language versions of the core dependencies (actors, rete and format (json, xml, etc), and also set up standard drake definitions that map to any language based on the use of common terms and operators is drake syntax.

**[Claude]** `Dictionary` is a good choice of exemplar — let me check whether its isolation actually holds today. *(Read `Dictionary.drake`; censused host-type usage.)* `Dictionary` earns that status — the isolation measurably holds.

**`Map` appears 6 times in the whole corpus; 5 are inside `Dictionary.json`.** The single escape is `CLI.json`'s `Map[String, Seq[String] => Unit]`. And its two dependents — `TypeDictionary[TypeName, TypeDefinition]` and `DomainDictionary[DomainType, TypeDictionary]` — reference `Dictionary`, never `Map`. So draco's map dependency really is confined to one definition, with users who never see through it.

The drake also shows the pattern is already *written*, not hypothetical:

```text
type Dictionary(K, V) from Map(K, V) DracoType
```

One type, two parents — the host implementation and the draco root, simultaneously. That's the wrapper shape we've been circling, and it predates the discussion.

Worth noting what the missing package on `Map` actually is: Scala's `Predef` default scope. It reads as an omission but it's a target-language fact — Haskell would need `Data.Map`. So even that gap is an argument for the per-language mapping you're describing rather than sloppiness.

**The honest gap is deployment, not method.** Isolation holds for `Map` and for nothing else:

| host type | sites outside a wrapper |
|---|---|
| `Seq` | **60** |
| `mutable.Set` | 8 (all `Accumulator`) |
| `LazyList` | 3 (all `Primes`) |
| `Option` | 2 (`TypeLoader`) |
| `Array` | 1 (`CLI`) |

So `Dictionary` is a proof of the pattern rather than evidence it's applied, and `Seq` at 60 sites is the cost driver. This is the prelude question from #51 — but now with a worked example showing exactly what "done" looks like for one type, which makes it a decidable question rather than an open-ended one.

**And the exemplar surfaces the mechanism gap in the second half of your idea.** `Dictionary` isolates the *type*, but its element bodies are Scala calls — `kvMap.removed(key)`, `kvMap.updated(key, value)`, `kvMap.get(key)`. The drake's type-level syntax is already portable (application, tuple, `V1 >: V` bound); the bodies are not. So a wrapper splits cleanly into **portable structure** and **per-language binding**, and today both live in the same file in `src/main`. Alternative-language versions of the core dependencies need those separated — the structure staying definitional, the bodies becoming what each target supplies. That's the concrete form of "each target language supplies its own bindings," and `Dictionary` is where you can see the seam.

Recommendation: record `Dictionary` on #51 as the worked exemplar, with the isolation measurement, the structure/body seam, and the `Seq`-at-60 figure — it converts the prelude question from a judgement call into a costed one. Then the rete rework can follow the `Dictionary` shape directly (`from [ org evrete api ] StatefulSession DracoType`) rather than the shape I guessed at.

Still open from before, and I'd rather not guess: the draco vocabulary for the other two rete types, and whether `Action`/`Pattern`'s repointed `valueType`s follow the renames.

---

**[Dev]** The draco domain wrap types are specific to target language neutrality. The external dependency domain wrap types will use draco domain wrappers as a basis for the language neutrality in the dependency domains (rete, actor, format). For example,  if we make a draco wrap type for the Scala Map type, that draco wrap type can be used in any rete domain wrap types to redefine how rete is used in draco.

**[Claude]** That's a stratification I hadn't separated — and it's testable against the one dependency domain that already exists. *(Censused `draco.format`'s element valueTypes.)* That layering is right, and the existing `draco.format` domain proves it by failing the test.

**`draco.format` — the north star's own cited success — is not language-neutral.** `format/Value.json` carries 4 element valueTypes, 2 of them host-typed (`Seq[String]`, `String`); the json sub-domain's `Value.json` carries 8, with 4 host-typed. So the abstract `Value[F]` successfully hides circe and does not hide Scala. That's exactly your point: a dependency-domain wrapper built without draco wrappers underneath inherits the host language through its own element types.

Worth being precise about what that means for the record: `Format[T]` in main with the circe binding in mods achieves **library**-neutrality. The 2026-06-14 note treats that as the realized pattern for the whole problem, but **language**-neutrality is a second axis it doesn't reach. Your stratification separates them; the note conflates them.

**And it exposes that my rete first pass is neutral only because it's empty.** All four new types have zero valueTypes — they're contentless markers, so they can't fail the test. The moment `WorkingMemory` gets fact collections or `RhsContext` gets accessors, they hit the same wall `Value` did. The neutrality is deferred, not achieved.

**Which corrects how I've been framing the prelude.** I called it the cost driver and an in-or-out judgement call, as though wrapping `Seq` were an expense to weigh against benefit. Under your layering it's the basis: layer 2 cannot be language-neutral without layer 1, so the 60 unwrapped `Seq` sites aren't an argument for deferring — they're the measurement of how much of draco is currently Scala-bound. Same number, opposite conclusion.

The staging that falls out:

- Empty markers plus the external derivation are safe to finish now — no element types, so no dependency on layer 1. That completes the increment in flight.
- Real content in rete/actor waits on layer 1, because adding it earlier just re-creates `Value`'s problem in a new domain.
- Layer 1 starts with `Seq` — 60 of the ~74 host-typed sites — then `mutable.Set` (8), `Option`, `LazyList`, `Array`, and the scalars.

One open question I'd want settled before layer 1 rather than during: where it lives. `Dictionary` sits at the draco root wrapping `Map`, while `draco.base` holds `Cardinal`/`Ordinal`/`Nominal`, which are adjacent in spirit but are quantity and naming concepts rather than container types. So layer 1 currently has one member in one place and near-neighbours in another, and a `Seq` wrapper has no obvious home yet.

Recommendation: record the stratification and the `draco.format` measurement on #51 — it corrects a belief that's currently load-bearing in the memory record, not just an open question — then finish rete as markers-with-derivation and treat layer 1 as the next real increment.

That still needs your two calls from earlier to proceed on rete: the draco names for `Knowledge` and `RhsContext`, and whether `Action`/`Pattern`'s `valueType`s follow the renames.

---

**[Dev]** The daco.base.Base domain is a place holder intended to evolve into an upper level ontology for any domain that can be developed using draco framework. Dictionary is fundamental to the ontological structure of draco itself and the  domain ontologies developed using the framework.

**[Claude]** That corrects two things I got wrong: I read Base's current members as its definition rather than as placeholder content, and I filed `Dictionary` as a host-type wrapper when its position at the root follows from being constitutive. Let me test what your principle sorts. *(Ran host-type usage against draco's own metamodel versus everything else.)* Your principle sorts the corpus cleanly:

| host type | draco metamodel | other main | examples/mods/test |
|---|---|---|---|
| `Seq` | **46** | 17 | 0 |
| `Map` | 5 (all in `Dictionary`) | 1 | 0 |
| `Iterator` | 1 (in `Dictionary`) | 0 | 0 |
| `Option` | 1 | 2 (`TypeLoader`) | 0 |
| `Array` | 0 | 1 (`CLI` main) | 0 |
| `LazyList` | 0 | 3 (`Primes`) | 0 |
| `mutable.Set` | 0 | 8 (`Accumulator`) | 0 |

**Sequence is constitutive in exactly the way dictionary is.** `TypeDefinition`, `TypeElement`, `TypeName` and the aspects cannot be expressed without ordered collections — 46 metamodel sites. That's the same kind of fact as `Map` living inside `Dictionary`, and it says a sequence wrapper belongs beside `Dictionary` at the draco root rather than in `Base`.

**And the bottom three rows are not draco's ontology at all.** `LazyList` and `mutable.Set` appear only in the Primes example, `Array[String]` only in `CLI`'s JVM entry point. Zero metamodel presence. They're implementation choices of a particular domain and a particular host, so language neutrality of the *framework* doesn't require wrapping them.

That deflates what I've been calling the prelude explosion, and I had it wrong twice over. Last message I listed `Seq`, `mutable.Set`, `Option`, `LazyList`, `Array` and the scalars as a queue to work through, as if they were equals. They aren't: 60 `Seq` sites are not 60 problems, they're **one missing constitutive type used 60 times** — precisely parallel to `Map`/`Dictionary`. The rest mostly fall outside the question. Scoped by ontological role, layer 1 is two or three types, not a prelude.

The division that seems to follow from what you've said: **constitutive structure at the draco root** — `Dictionary`, and a sequence peer — because draco's own metamodel is built from them; **scalar and quantity concepts in `Base`'s upper ontology**, which is where `Cardinal`, `Ordinal` and `Nominal` already sit and would be the natural home for name-ness and magnitude as ontological rather than host notions.

Two genuinely open, and I'd flag rather than guess:

- **`Option`** is the borderline case — 1 metamodel use plus 2 in `TypeLoader`, where it expresses "this definition may not exist." That's the load boundary, and presence/absence is arguably constitutive given the presence model already in play on #51.
- **`String`** at 64 uses is where the two levels meet. `TypeElement.name` and `valueType` are strings, but under the upper ontology `name` is a concept and `String` is its host realization — which is what `Nominal` already gestures at. Whether the metamodel's names become `Nominal` is a much larger call than the collection types.

Recommendation: record the constitutive/incidental distinction on #51 with this table, and name the layer-1 set explicitly — `Dictionary` done, sequence next, optionality open — so the scope is fixed by ontological role rather than by counting occurrences. Shall I write that up?

---

**[Dev]** Write it up, but reserve what the most primitive draco wrap types might be. As I mentioned, while Dictionary may remain abstract with type parameters, it may use a more primitive draco wrap type of Map, which would allow the drake definition to remain unchanged because the source generation of Map itself would be determined by the use of the same terms and operator symbols used in syntactic string substitution forms supplied by specific target languages, such as Java, TypeScript, Haskell and other languages supporting dependent types.

**[Claude]** *(Checked drake's container-operator substitution; wrote and posted the #51 comment.)* Confirmed, and it sharpens the reservation: drake **already** does this substitution for some containers. `[Variable]` in drake → `Seq[Variable]` in JSON; `{Int}` → `mutable.Set[Int]`; `Map(String, [String] => Unit)` → `Map[String, Seq[String] => Unit]`. Sequence and set are neutral at the drake surface today, while `Map`, `Option`, `Iterator`, `LazyList` still appear by name. Writing it up with that boundary as the reason the primitive set stays open.

Written up on #51, with the primitive set explicitly reserved rather than named. The comment carries the two-layer stratification, the `draco.format` measurement showing library-neutrality and language-neutrality are separate axes (and that the north star's cited success only reaches the first), the constitutive/incidental table, and — since it was my framing that needed retracting — an explicit note that "prelude explosion" was wrong: 60 `Seq` sites are one missing constitutive type used 60 times, not sixty problems.

The reservation is recorded with the reason rather than as a bare "TBD". The operator/name boundary is what makes it undecidable right now: `[T]`, `{T}`, `(A,B)` are operator-carried and already neutral in drake, while `Map`, `Option`, `Iterator`, `LazyList` appear by name and aren't. Whether each of those becomes a wrap type, gains operator sugar, or is resolved by a name-keyed substitution table is precisely what fixes the primitive set — so naming it now would prejudge it.

Two things that fell out of checking your point and are worth having on the record:

- **drake is currently the more language-neutral projection.** `[Variable]` is neutral; the JSON's `Seq[Variable]` is the Scala rendering. That inverts the usual assumption that JSON is normative and drake is a surface — for collections, the normative form has the host baked in and the surface doesn't.
- **The substitution already carries more than naming.** `{Int}` renders to `mutable.Set[Int]` — mutability is a target decision that appears nowhere in the drake. So the substitution forms are doing semantic work, not just symbol mapping, which matters for what a Haskell or dependent-typed table would need to specify.

Nothing on the working tree changed this turn. Still uncommitted and unverified since the Action/Pattern repoint: the actor `messageType` wiring, the Action outlier fix, the rete markers, and that repoint.

---

**[Dev]** Let's focus on the operator/name boundary. We should be able to upgrade the json definition format to use operator idioms and drake keywords instead of language specific terms.

**[Claude]** That's a concrete increment. The key question is what substitution already exists — the drake emitter must already convert `Seq[X]` → `[X]`, so let me find that table. *(Found `drakeValueType`, checked `mut {`, read `drake.dlt`'s tables; posted a correction to #51.)* Correction first: my note on #51 that `{T}` bakes in mutability "absent from the drake" was wrong — drake carries it explicitly as `mut {Int}`, and `drake.dlt` documents the mapping. Corrected on the issue.

**The strongest argument for what you're proposing is already inside `drake.dlt` — it applies your rule to expressions but not to types.**

For EXPRESSIONS the spec says the `->` operator is *"HASKELL FORM"* and *"ScalaTarget renders ` => `"*. So the normative JSON already carries a neutral operator and the target supplies its spelling. That's exactly the model you want.

For VALUE-TYPES the same spec heads the table `drake type-expression -> JSON valueType string`, mapping `[<T>]` → `Seq[<T>]`. The direction is inverted: JSON holds the *Scala* spelling and drake holds the neutral one. So the JSON is already neutral for value expressions and host-spelled for type expressions — an internal inconsistency, not a design choice, which makes this an alignment rather than a new principle.

**The substitution table is small and already written**, as `drakeValueType` at `Generator.scala:341`:

| neutral | Scala |
|---|---|
| `[T]` | `Seq[T]` |
| `{T}` | `Set[T]` |
| `mut {T}` | `mutable.Set[T]` |
| `F(A, B)` | `F[A, B]` |
| `(A, B)`, plain names | unchanged |

Note the fourth row: even the name-carried containers get their *application syntax* neutralized — `[...]` is Scala's type-application bracket, drake uses parens. So the operator layer is fully covered; only the *names* — `Map`, `Option`, `Iterator`, `LazyList`, and the scalars — remain host, which is the part you've reserved.

**The work, and it's the inverse-renderer pattern that's held all session:** write `scalaValueType` as the mirror of `drakeValueType`, convert the corpus's valueType strings to the left column, make `drakeValueType` a pass-through, and fix the four predicates that key on the Scala spelling (`startsWith("Seq[")` at lines 906, 922, 945, 1033). Both projections must come out byte-identical, which makes the whole thing inert-by-construction and testable in one gate. Scope: 107 valueType strings carry an application, 63 with `Seq[`, 8 with `mutable.Set[`. Plus a one-line correction to `drake.dlt`'s VALUE-TYPES heading, since it currently documents the direction being reversed.

One thing I'd rather not do silently: this touches ~107 strings across the corpus on top of four uncommitted increments, one of which (the Action/Pattern repoint) hasn't been through a test run. I'd commit that stack first — separately, as discussed — so a corpus-wide transformation lands against a clean, verified base rather than compounding.

Want me to proceed on that basis?

---

**[Dev]** Would {(K, V)} work as neutral form for Map[K, V]?

**[Claude]** Mathematically apt — a map is a functional set of pairs — but it collides with a meaning already in use.

`{(Long, String)}` exists in the corpus right now: `primes/Accumulator.drake:6` has `fix intervalTextSet mut {(Long, String)}`, rendering `mutable.Set[(Long, String)]`. That's a genuine set of pairs, not a map. Give `{(K, V)}` the map meaning and those two distinct types share one form, with nothing in the notation to separate them.

There's also a semantic loss underneath the collision. A set of pairs permits duplicate keys with differing values; a map doesn't. `{(K, V)}` can't state the functional constraint — and that constraint is exactly what `Dictionary`'s own operations rest on (`dyn get Option(V)` taking `par key K` is only well-defined because keys are unique). Rendering `Map` to a form that can't express it means the renderer has to remember what the notation dropped.

Composed alternatives run into the same wall, because `{X}` already binds whatever it encloses as the *element* type. `{K -> V}` reads as a set of functions from K to V, since `->` is drake's arrow. `[K -> V]` is a sequence of them. Any brace-plus-existing-operator form inherits that reading.

So a map notation would have to be a genuinely distinct operator — nothing currently free reads well — or the neutral form stays a **name** resolved per target, which is the route you reserved. The name route sidesteps the whole collision class, since names don't compete with operator syntax.

Worth noting where that leads, because it's the reserved question in concrete form: draco already owns the ontological word for a keyed collection — `Dictionary`. But `Dictionary` is currently *defined* as `from Map(K, V) DracoType`, so making `Map` render as `Dictionary` is circular. Breaking that circle is precisely the "Dictionary stays abstract while resting on a more primitive draco wrap type" question — the primitive would need to be something other than `Dictionary` itself, and naming it is the call you deliberately left open.

My read: don't spend an operator on maps. Sequence and set earn theirs by being structural and unambiguous; a map's defining property is a constraint that bracket notation can't carry, which is a signal it wants to be a named type rather than a symbol.

---

**[Dev]** Isn't a hash operation implicit in Set?

**[Claude]** Yes — and that corrects my argument. I said `{(K, V)}` "can't express the functional constraint," but a set is not constraint-free: it already presupposes an equality/hash discipline. Both forms carry uniqueness; they differ only in *what it's keyed on*.

- `{(K, V)}` — uniqueness over the whole pair. `(k, v1)` and `(k, v2)` are distinct members, both permitted. That's a **relation**.
- A map — uniqueness over the first component. That's a **function**.

So the distinction isn't presence-versus-absence of a constraint, it's the projection the constraint applies to. `{(K, V)}` doesn't say which — that's the residue of my point that survives, and it's narrower than what I claimed.

Your observation generalizes further, and I think it's the more interesting consequence: **`{T}` already elides a required capability on `T`.** Scala's `Set` needs `==`/`hashCode`; Haskell's `Data.Set` needs `Ord a`; a dependent-typed target would want the decidable-equality witness named outright. Drake's `{T}` states none of it. So the set operator is *already* under-specified against the target family you're aiming at — which means the map question isn't introducing a new kind of gap, it's exposing one that's there.

Given that, the collision I raised does still bite, but it has a clean resolution the syntax already affords — the tuple parens are load-bearing:

| form | meaning |
|---|---|
| `{T}` | set of `T` — uniqueness over `T` |
| `{(K, V)}` | set of pairs — uniqueness over the pair (a relation) |
| `{K, V}` | map — uniqueness over `K` (a function) |

Two operands directly inside braces versus one tuple operand. It's unambiguous, it composes with the existing convention that parens make a tuple, and `{A, B}` is currently unused — the only brace forms in the corpus are `mut {Int}` and `{(Long, String)}`. It also keeps `Accumulator`'s `mut {(Long, String)}` meaning exactly what it means today, which a bare `{(K, V)}`-as-map would have silently changed.

Whether one character of difference is too subtle to carry that weight is a judgement about the surface, and that's yours. But on your actual question: the hash discipline doesn't make map and set the same notation — it makes them two members of one family, which argues for distinguishing them *within* the brace form rather than giving maps a name.

The elided-capability point seems worth recording on #51 regardless of which way the map form goes. It bears directly on the dependent-typed targets, and it's the kind of thing that's invisible until a Haskell binding won't compile.

---

**[Dev]** I am having trouble finding (precisely) the text that "elided-capability point" references

**[Claude]** There is no such text — that's my fault for phrasing it as if it referenced something. The point is about what `drake.dlt` **doesn't** say, so you were hunting for a passage that doesn't exist.

Concretely, this is the whole VALUE-TYPES section (`drake.dlt:36-42`):

```text
VALUE-TYPES  (drake type-expression -> JSON valueType string)
  [<T>]            Seq[<T>]
  {<T>}            Set[<T>]
  mut {<T>}        mutable.Set[<T>]
  <F>(<A> ...)     <F>[<A> ...]        type application
  (<A>, <B>)       (<A>, <B>)          tuple, unchanged
  <Name>           <Name>              plain type, unchanged
```

`{<T>}` maps to `Set[<T>]` and says nothing about `<T>`. Scala's `Set` needs `==`/`hashCode`, Haskell's `Data.Set` needs `Ord a` — neither requirement is stated or derivable from that line. That silence was my "elided capability."

What makes it a real gap rather than a nitpick is that drake already **has** the mechanism, two sections earlier (`drake.dlt:25-26`): context bounds exist and are in use — `format/Value.json` and `format/json/Value.json` both declare `value[T: Decoder]`. So drake can say "this type parameter must supply a capability," and does, for decoding. The set form simply doesn't use it.

*(Checked whether the corpus's actual set element types would even satisfy such a bound.)* The data cuts my point down, so let me be accurate about its size.

**Every set in the corpus is concrete.** All eight occurrences are `Accumulator`'s — `mutable.Set[Int]` and `mutable.Set[(Long, String)]`. Not one set is parameterized over a type variable. At concrete element types both Scala and Haskell resolve the equality/ordering instance without anything being declared, so the missing capability statement costs nothing today. There is no latent breakage — I implied there was, and that was overstated.

It becomes real only when something declares a *generic* set — which is exactly what a layer-1 `Set(T)` wrap type would be. So it's a note for that work, not a present defect, and I'd file it that way rather than as a gap in the current spec.

**The more useful thing the check turned up** is where the capability actually lives when it is used. `format/Value.json` writes it as:

```json
"name": "value[T: Decoder]"
```

The bound rides *inside the element's name string*. Drake renders it `dyn value(T: Decoder) T`, so the parens/brackets differ, but in both projections it's text inside a name rather than a structured slot.

That's the same species as the two defects the earlier census found — `Natural.json` deriving `Primal[Int]` with the application baked into the derivation's name, and `Dictionary` deriving `Map` with no package at all. Three separate cases of type structure smuggled into name strings, where a field should carry it. That connects directly to the atomization work on #51: constraining `typeParameters` to atomic terms doesn't achieve much if bounds and applications can still hide inside `name`.

So the thing worth recording isn't the set-capability gap — it's that names are currently a second, unpoliced channel for type structure. That one has instances today.

---

**[Dev]** Would you suggest  changing TypeName:  "fix typeParameters [String]" to "fix typeParameters [TypeExpression]"?

**[Claude]** No — that would undo the constraint you just established. `TypeExpression` re-admits arbitrary nesting in the one place the atomic rule exists to keep it out, and it puts `TypeName` straight back on the fork that constraint closed (recursion, and `resourcePath` going partial).

*(Inventoried what shapes a parameter actually has to carry.)* Here's the complete inventory — three bound kinds, and only one of them currently lives in the field:

| form | count | where it lives today |
|---|---|---|
| upper `T <: Product` | 6 | `typeParameters` ✓ |
| context `T: Decoder` | 2 | **inside element names** (`value[T: Decoder]`) |
| lower `V1 >: V` | 1 | **inside an element name** (`updated[V1 >: V]`) |
| bare `Int` | 1 | **inside a derivation name** (`Primal[Int]`) |

So four of the ten are hiding in name strings, and `[String]` is why: it can't distinguish `T` from `T <: Product` from `T: Decoder` structurally, so anything that doesn't fit gets written as text somewhere else.

That says `[String]` should change — just not to `TypeExpression`. What the field needs is a small **record**, every component of which is atomic: parameter name, bound kind (`<:` / `>:` / `:`), and bound — the bound itself being a type reference, i.e. a `TypeName`. Nothing nests.

So my suggestion is `fix typeParameters [TypeParameter]`, with `TypeParameter` a new atomic element type. That buys two things `[String]` can't:

- **Atomicity becomes structural.** A nested type expression isn't forbidden-by-convention, it's unrepresentable. That's the difference between the constraint being a rule someone has to enforce and a property of the model.
- **The bound kinds get a home**, so names stop being a second channel. All four of those hidden cases move into the field where they belong.

I'd also reject `[TypeName]`, which is the other obvious candidate: `TypeName` carries its own `typeParameters`, so nesting stays representable and atomicity is back to convention — and it has no bound slot, so `T <: Product` and `T: Decoder` would still have nowhere to go. It reintroduces the recursion while solving neither problem.

On timing, though: `TypeName` is the most load-bearing type in the system, this is squarely #51, and #51's own prerequisites aren't done — the actor increment isn't finished and #37 structural equality is explicitly flagged as do-not-skip, which matters more once parameters are records rather than strings. There are also four uncommitted increments in the tree, one unverified. Right change; I'd want the tree clean and #37 settled before touching it.

Want me to record the `TypeParameter` shape on #51 with this table, so the argument is captured while the work waits on its prerequisites?

---

**[Dev]** Yes, I was looking for a way of changing typeParameters to something other  than [String]. TypeParameter seems exactly right.

**[Claude]** *(Wrote and posted the #51 comment.)* Recorded on #51 as a decision, with the census table showing why `[String]` fails — four of ten occurrences currently hide type structure inside name strings — and the reasoning for rejecting both `TypeExpression` and `TypeName`.

One thing I put in as an open item rather than assuming, because the obvious modelling contains a trap: **don't type the atom or the bounds as `TypeName`.** `TypeParameter { reference: TypeName, … }` reopens nesting through the back door — `TypeName` has `typeParameters`, which hold `TypeParameter`, which holds a `TypeName` — and atomicity drops back to convention, which is the exact failure the change exists to fix. Flattening `name` + `namePackage` inline makes nesting unrepresentable instead, at the cost of duplicating the qualified-reference shape in two types. That trade is the remaining design step.

Also noted that this closes the `name`-as-overflow-channel problem, which is what makes it worth more than a type-tightening: the atomic constraint on `typeParameters` doesn't buy much while `value[T: Decoder]`, `updated[V1 >: V]` and `Primal[Int]` can still smuggle structure through names.

Prerequisites are unchanged but bind harder — #37 especially, since comparing parameters stops being string comparison once they're records.

The tree is still where it was: four uncommitted increments, and the Action/Pattern repoint hasn't been through a test run.

```bash
sbt "testOnly draco.DracoGenTest draco.DrakeGenTest"
```

*(Delegated action — Dev ran the gate: all tests passed.)* *(Checked git status and history — a merge had come in from `origin/main`; verified whether the actor increment survived into HEAD; compared the two identically-messaged strip commits; wrote `git-record-2026-08-04-1559`.)* A merge came in from `origin/main`, and the actor increment is no longer in the working tree. Let me confirm whether it survived into HEAD or was lost. The actor increment is safe in HEAD — `Draco.json` and `ActorAspect.json` both carry `messageType`, and the Generator has the aspect-first lookup.

Two things the history shows, and one is a real record problem. `b93e3a1` carries the message *"strip 27 redundant sole-[DracoType] derivations (#50 item 1)"* but its content is the strip **plus** the actor increment — `ActorAspect.scala` (+7) and `Generator.scala` (+21). So the strip got committed twice (once alone in `eafc649`, once again alongside the actor work), and `9941de0` merged them. The tree is correct — that's why tests pass — but the `messageType` wiring, the `ActorAspect` element, and the aspect-first `actorMessageType` lookup are all recorded under a message that mentions none of them.

Since it's already merged and pushed, I wouldn't rewrite history for it. The cheap fix is to carry the correction in the next audit record rather than leave it only in the git graph. Record written. Both increments are green, and they split cleanly — no file overlap, and the repoint depends on the rete types existing, so the order matters:

```bash
cat /Users/edwardburke/IdeaProjects/nexonix/draco-git-record/git-record-2026-08-04-1559
```

Paste its contents into your terminal, or use the two message bodies from the IDE. The record file stages itself with the first commit so the audit trail sweeps in.

**Commit 1** — the `draco.rete` first pass, twelve additive files, with the four known-incomplete items recorded rather than left implicit: no binding edge exists yet, the names mirror Evrete instead of carrying draco vocabulary, `derivationRef` would emit an external parent bare with no import, and the markers are neutral only because they're contentless.

**Commit 2** — the Action outlier fix and the Action/Pattern repoint. I wrote both rejected attempts into the message (`par ctx` in the factory body, and `valueType → Unit`) since the reasoning that killed them — Pattern being the sibling that settles it, not the Dynamics — is worth more than the final diff. It also states plainly that the repoint exercises no binding, so nobody later reads it as the wrapper being proven.

The record correction for `b93e3a1` is in commit 2's message: the actor `messageType` work is real and in the tree, but it's filed under the strip's message, and that's now written down somewhere other than the git graph.

Two things stay open and are noted in the record: dropping `Actor[DracoType]` from `Draco.json`'s derivation, still blocked on the drake emitter not emitting `actorAspect.messageType`; and `Action.json` remaining in `authoredAhead`, since the bracket style differs from the emitter's — the present-empty heads were only half the reason.

---

**[Dev]** Can we close the two things  that you indicate stay open, and then update the the commit, instead of committing on the new git record first?

**[Claude]** *(Ran the bracket-style census, read the actor-block emitter; edited `Generator.scala`, `Draco.json`, `Draco.drake`, `Draco.scala`, re-authored `Action.drake` in emitter form, edited `DrakeGenTest.scala`.)* The bracket census contradicts what I told you earlier — several bracketed files pass DrakeGenTest. Now I can see the rule — it's delimiting, not style. `TypeLoader`'s `globals [` brackets because its `dyn` leaves have their own nested `parameters` sub-blocks; `Pattern`'s sections hold only simple leaves so they need no delimiter. Action's leaves are all simple `fix`/`par`, so it should look exactly like Pattern. My "emitter style is unbracketed" was too broad — it's conditional on nesting.

Both are closable. Making the changes. Clean — pure deletion. Both closed. Please run:

```bash
sbt "testOnly draco.DracoGenTest draco.DrakeGenTest"
```

**Derivation drop.** `Draco.json` loses `Actor[DracoType]` (11 deletions, no churn), so actor-ness now rests on the aspect alone and both roles contribute symmetrically. `Draco.scala`'s trait becomes `extends DracoType`. The factory body is unaffected — `new Actor[DracoType]` comes from `actorMessageType`, which reads the aspect.

The prerequisite is in first: `Generator.drake` now emits `messageType` in the actor block (`Generator.scala:566`), so `Draco.drake` reads `actor` / `messageType DracoType` / `start` … and `T` survives the loss of the derivation instead of silently vanishing. I used a head-plus-payload line mirroring `domain draco Draco`, and placed it first to match the order in `ActorAspect.drake` and `Draco.json` — still your surface call, easy to change.

**`Action.json` out of `authoredAhead`**, with `Action.drake` re-authored in emitter form.

I owe you a correction on why that was blocked. I said the emitter's canonical style is unbracketed — that's wrong. `TypeLoader.drake` and `Dictionary.drake` are bracketed and pass. The real rule is delimiting: a section brackets when its leaves carry their own nested sub-blocks (`dyn` with `parameters`), because otherwise the section's extent is ambiguous. Sections of plain `fix`/`par` leaves need no delimiter. Action's leaves are all simple, so it takes Pattern's exact shape — which is what I've written, along with dropping `factory Action` to bare `factory`, since the valueType is inferable.

Two caveats on that file. It's a reconstruction of emitter output rather than generated, so the test is the arbiter — if it fails, the diff says exactly what's off. And `ActorAspect.json` is now the only remaining exclusion besides `BodyElement.json`; it stays, since its drake is bracketed while its leaves are simple, and it carries the present-empty heads too.

I've held off updating the git record until this passes — no point describing commits whose content might still shift.

*(Delegated action — Dev pasted the full terminal output: 224 tests run, 224 succeeded, all passed — including the new `draco/Action.json: Generator.drake matches draco/Action.drake`, and `Draco.json` matching its drake.)* All 224 pass — including the new `draco/Action.json: Generator.drake matches draco/Action.drake`, so the reconstruction was exact and Action is genuinely out of the exclusion list, and `Draco.json` matches its drake with the bare head plus `messageType DracoType`.

*(Rewrote `git-record-2026-08-04-1559` in place.)* Record rewritten in place — same file, now covering three commits instead of two, so nothing stale gets committed.

**Commit 1** — `draco.rete` first pass, unchanged from before, with the four known-incomplete items still recorded (no binding edge, Evrete-mirrored names, `derivationRef` emitting bare, markers neutral only by being contentless).

**Commit 2** — Action outlier fix, the repoint, and Action leaving `authoredAhead`. `DrakeGenTest.scala` joins this commit since the exclusion removal and the drake re-authoring are one change. It records the correction about block style: the rule is delimiting, not stylistic — a section brackets when its leaves carry nested sub-blocks, which is why `TypeLoader` and `Dictionary` are bracketed and pass.

**Commit 3** — the derivation drop, with the drake `messageType` emission as its stated prerequisite. The message spells out why the derivation was never load-bearing for actor-ness: `isActor` matches `ActorType`/`ExtensibleBehavior`, so it was already false for `Draco`, and `hasActorBehavior` was doing the work. The `b93e3a1` mislabelling correction moved here, since that commit's content was the actor increment this one completes.

Both remaining exclusions are noted as still standing: `BodyElement.json` and `ActorAspect.json`.

Worth flagging for whenever the rete binding gets applied — the three items now interlock. `derivationRef` emitting parents bare is what blocks `from [ org evrete api ] StatefulSession`; that same fix is what the renames to draco vocabulary depend on; and the load-bearing sites (`Draco.json:43`, `primes/Primes.json:14`) are what would actually prove the wrapper works. None of that is in these three commits.

*(Session possibly ongoing at time of transcription — chapter to be extended if more follows.)*

## Status
This chapter transcribes the recovery session that ran July 28 – August 4, 2026 — and the session may still be open: the last recorded exchange (August 4, 20:09 UTC) ends with the three-commit git-record rewritten in place and the rete-binding interlock flagged for a future increment, but no commit confirmation, no session-transition ritual, and no closing exchange appear in the data, so this chapter should be extended if more follows. It sits two sessions after chapter 68: between them lies the **abandoned session** (chapter 69) that Dev deleted and rolled back — that session left no transcript of its own, and this session's opening is its post-mortem. **Opening state:** Dev's rollback verified complete — repo clean at `origin/main` = `59f793a` (2026-07-27 12:44, the chapter-68 close), only two empty `rete` directories surviving (removed). Three model-authored records over-claimed rolled-back work ("Increment 1 — LANDED, suite 324/324" in `project_draco_rete.md`, its MEMORY.md line, and #49's comment) and were corrected — retraction prepended, `priority-next` dropped from #49 — with the deeper diagnosis recorded: the deleted session's `draco.rete.Rete` put Evrete *configuration* (session construction, `ActivationMode`) into `src/main`, inverting the 2026-06-14 north star (capability *names* in main, bindings in mods — "the definition/binding split IS the main/mods split"), on a hollow ScalaTarget "precedent" and against #49's own YAGNI warning; a new feedback memory (`feedback_surface_structural_moves_first.md`) records the pattern, later joined by the **"hygiene" exchange** — a coined term doing the job of a justification — which Dev explicitly directed be recorded for this journal (term kept, pattern noted). **Test counts across the arc:** strip gates "All tests passed" (unnumbered, testOnly + full suite) → 211/211 (the run that never looked at `ActorAspect.drake`) → **127 run / 126 pass / 1 fail** (the `par`-in-factory-body attempt, deliberately run to let the test arbitrate — it corrected Claude twice) → 223/223 (rete first pass + Action `name=""` fix) → **224/224** (last recorded gate: derivation drop + Action out of `authoredAhead`). **What landed (per the last gate; commits staged but unconfirmed):** (1) **#50 item 1** — the 27 redundant sole-`[DracoType]` derivations stripped, 228 deletions / zero insertions, inert by construction because `Generator.generate` roots its input at entry and the drake emitter already elides the sole root — the two projections now agree; committed as `eafc649`, then (a record defect found later) committed *again* inside `b93e3a1`, whose message names only the strip while its content also carries increment (2) — correction carried in the audit record, history not rewritten. (2) **The actor `messageType` increment** — `ActorAspect` gains a `messageType: TypeName` element + factory parameter (Dev authored the drake himself; two typos — `messageTy`, `Acpetion` — survived a green 211-test run because `ActorAspect.drake` sits unguarded in `DrakeGenTest.authoredAhead`, the "messed up typo" discovery); `Generator.actorMessageType` reads the aspect first with the `Actor[T]`-derivation fallback covering the 12 mods actors; authored bare-name (`{"name": "DracoType"}`) to stay byte-inert, qualification deferred. (3) **The Action outlier fix** — reduced by test feedback to exactly one fact: a binder name (`"ctx"`) sitting in `name` where every sibling has `""`; the arrow `RhsContext => Unit` stays (`Pattern`, the true sibling, settles it — the Dynamics' parameters+result convention only applies where a parameter list exists), and `par` in a factory body has no Generator support. (4) **The `draco.rete` first pass** — twelve additive files (`Rete` + `Knowledge`/`RhsContext`/`StatefulSession` as contentless markers extending DracoType), hand-emitted and Generator-verified, plus the `Action`/`Pattern` `valueType` repoint onto the wrappers — explicitly recorded as *descriptive only*: no binding edge exists between `draco.rete.X` and `org.evrete.api.X`, and the markers are "neutral only because they're contentless." (5) **The two open items closed at Dev's request:** `Actor[DracoType]` dropped from `Draco.json`'s derivation (`trait Draco extends DracoType`, actor-ness on the aspect alone, both roles symmetric — staged behind the drake emitter learning to emit `messageType` in the actor block at `Generator.scala:566` so the drake projection couldn't silently lose `T`), and `Action.json` lifted out of `authoredAhead` with `Action.drake` re-authored in emitter form (the bracket rule corrected: delimiting, not style — a section brackets only when its leaves carry nested sub-blocks). **The design corpus recorded on #51 (filed this session):** type expressions in JSON/drake close-to-or-same-as value expressions as the issue's spine (`Action.json`'s `{"->": …}` tree the corpus's own unprompted precedent; 112 `valueType` / 29 `typeParameters` census); wrapped types as the vocabulary half, cross-linked to #49 with the WHAT/HOW line restated on both (11 distinct third-party types); the **atomic-terms constraint** on `TypeName.typeParameters` killing the recursion fork (`TypeName` stays the atom; formals = pair of atoms, actuals = single atom; bounds occur only on formals; `src/main` violates it in exactly one way, `Holon(S, T)`), superseding the issue body's central decision; `from` kept over a new `binds` keyword (externality reconstructable from the package) with the `derivationRef` bare-emission blocker named; `Dictionary` as the worked exemplar of systemic dependency isolation (`Map` 5-of-6 confined; the portable-structure/per-language-binding seam); the **two-layer stratification** — draco domain wrap types (target-language neutrality) underneath dependency-domain wrap types (rete/actor/format), with `draco.format` measured as library-neutral but *not* language-neutral; **Base as upper-ontology placeholder** and the constitutive/incidental cut (Seq constitutive — 46 metamodel sites, one missing type used 60 times, not sixty problems; `LazyList`/`mutable.Set`/`Array` incidental), the **primitive wrap-type set deliberately reserved** because the operator/name boundary decides it (`drakeValueType` table at `Generator.scala:341`; drake currently the *more* language-neutral projection — an inversion of the JSON-normative assumption; the JSON-side upgrade scoped: `scalaValueType` inverse renderer, ~107 strings, four `startsWith("Seq[")` predicates, awaiting a clean tree); the map-form question (`{(K, V)}` collides with `Accumulator`'s genuine set-of-pairs; the hash-discipline correction — relation vs function, uniqueness projection; the proposed `{T}`/`{(K,V)}`/`{K,V}` brace family — undecided, Dev's surface call); the "names are a second, unpoliced channel for type structure" finding (bounds, applications, and packages smuggled into name strings — three live instances); and **`typeParameters [String]` → `[TypeParameter]` decided** (rejecting `TypeExpression` and `[TypeName]` as re-admitting nesting; the flatten-vs-`TypeName`-reference trade the remaining design step; blocked on #37 and a clean tree). **Issues:** #48 closed (prior); #49 open, `priority-next` dropped, retraction + #51 cross-link; #50 open (item 1 done, item 2 rule trait emission pending); #51 filed and carrying the session's design corpus. **Open at the transcription boundary:** the three staged commits in `git-record-2026-08-04-1559` (rete first pass / Action fix + repoint + exclusion removal / derivation drop, the `b93e3a1` mislabelling correction in commit 3's message) awaiting Dev's commit; remaining exclusions `BodyElement.json` + `ActorAspect.json`; the rete-binding interlock (`derivationRef` fix ⟶ draco-vocabulary renames — `WorkingMemory` for `StatefulSession` is Dev's, names for `Knowledge`/`RhsContext` still his call ⟶ the load-bearing sites `Draco.json:43` and `primes/Primes.json:14` as the honest proof); the `problems` reply-protocol question; the mods actors' migration off `Actor[T]` derivations; and the ch.68 carry-forwards not touched this session — the presence model (Phase 2b heads), the Source→Drake→Generator(Drake) pivot, #47 (shared `isStub`), **#37 (TypeName structural equality — now binding harder as #51's do-not-skip prerequisite)**, the command protocol, and #46/#42/#43 residue.
