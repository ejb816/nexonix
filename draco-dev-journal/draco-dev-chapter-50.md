# Draco Dev Journal — Chapter 50

**Session date:** June 23–24, 2026
**Topic:** How actors that thread `ActorRef`s through working memory get *defined* (not hand-written). Three over-abstractions stripped away; the actor lifecycle settled along two orthogonal dimensions (statefulness = Draco author choice, multiplicity = Orion-only); the always-`def actorType` emission; comment provenance traced and Appendix A preserved before stripping; the `setupAction`/`messageAction`/`signalAction` triad reclaimed and exercised by the first stateful actor; then the closing arc — every actor acknowledges via a setup rule, ActorSystem investigated and kept out of draco core, and the `Assembly` of actors sketched. Transcript.

---

**[Dev]** We were about to talk about definitions for actors that utilized consumer actor references put into working memory of provider actors

**[Claude]** Let me ground this in the actual current state before we design. *(Sweeps the media-domain actor wiring, HEAD's commit — "Actor signalAction->setup emission", which names this precisely as the deferred piece — the actor fields in TypeDefinition, `Creator.json`, `ActorAspect.json`, and the `actorBehavior` emission in `Generator.scala`.)*

**Where the seam lives today.** The Consumer-ref-into-Provider-working-memory pattern is currently expressed as **three bare string literals** spread across two files, with nothing tying them together:

| Where | What it says | File |
|---|---|---|
| Provider factory (Scala only) | `def actorType(consumer: ActorRef[Json])` | hand-written `Creator.scala` |
| Provider seed | `session.set("consumer", consumer)` | `Creator.json` messageAction, as a `Monadic` |
| Rule RHS lookup | `ctx.getRuntime().get[ActorRef[Json]]("consumer")` | `OriginateReport.rule.json` action, as a `Fixed` |

The name `"consumer"` is the join key across all three, but it's an un-modelled coincidence of string literals. And critically: **`Creator.json`'s body references a free variable `consumer` that nothing in the JSON declares.** The hand-written `def actorType(consumer:…)` supplies it. That's exactly why these 8 factory actors show up as DIFF in `ExampleDomainsGenTest` — the Generator emits `lazy val actorType: ActorType = …` (a *val*), and has no way to know it should be a *def* taking an `ActorRef`.

**What I'd propose: reuse the factory we already have.** Every `DracoType` already carries a `dracoAspect.factory` with typed `Parameter`s, and the Generator already knows how to emit factories for ordinary types. An actor's factory simply *is* `actorType`:

```json
"dracoAspect": {
  "factory": {
    "kind": "Factory", "valueType": "ActorType",
    "parameters": [
      { "kind": "Parameter", "name": "consumer",
        "valueType": "ActorRef[draco.format.json.Json]" }
    ]
  }
}
```

From that single declaration `actorBehavior` derives all three emissions: parameters non-empty ⇒ `def actorType(consumer: ActorRef[Json]): ActorType` instead of `lazy val`; any parameter whose `valueType` is `ActorRef[_]` ⇒ auto-emit `session.set("consumer", consumer)` into the setup block; the param name *is* the slot key. This is Haskell-honest per our own rule: an actor that forwards downstream is a function `ActorRef[T] → Behavior`. Nothing new on the schema; we're spending the factory field that's already there. And a free rider: with the setup/`signalAction` split now emitting at instance scope, the right shape is session creation + the seed at setup (run once, persistent working memory), `messageAction` shrinking to `insert + fire` — the actor finally becoming a persistent-working-memory membrane instead of a per-message throwaway.

**The one real fork** is the rule side (`ctx.getRuntime().get[…]("consumer")`): (1) leave it as the authored `Fixed` lookup — closes the 8 factory-actor DIFFs, leaves the 4 rule DIFFs; or (2) model it symmetrically — the rule declares it *requires* a role `consumer: ActorRef[Json]` and the Generator emits the `getRuntime().get` prologue. My recommendation: do the actor side via the existing factory field first, get the 8 actors to MATCH, then add the symmetric rule-side role. Does the "reuse `dracoAspect.factory.parameters` rather than add an ActorAspect field" framing match how you were thinking about it — or were you picturing the role declaration living on the `actorAspect` itself?

---

**[Dev]** I'm a bit concerned about any explicit reference to type definition aspects. It is a user authoring choice at the draco level how to wire actors together. That is more automated at the orion outer layers.

**[Claude]** You're right — that retraction is warranted. Putting the `ActorRef` into `dracoAspect.factory.parameters` bakes a *topology* decision into the actor's intrinsic definition. The same `Creator` membrane could be wired to any consumer; "who it forwards to" is not a property of what `Creator` *is*. I conflated composition with type.

Redrawing the line: **intrinsic to the actor type def** — the membrane behavior (session at setup, `insert + fire` per message) and the rules it fires. **Intrinsic to the rule** — the RHS referencing a *logical channel* (`consumer ! originate(intent)`); the rule legitimately says "I emit to a channel named `consumer`." **Wiring — NOT type-def content** — the concrete `def actorType(consumer: ActorRef[Json])` factory signature, and the `session.set("consumer", ref)` seed binding a real ref to the logical slot. That binding is authored composition at the draco level today (the test literally does `Creator.actorType(consumerRef)`), and it's what Orion automates at the outer layer.

A consequence worth stating plainly: the **8 "factory actor" DIFFs in `ExampleDomainsGenTest` are correct, not Generator gaps** — hand-authored wiring/bindings, the "bindings needing capability domains" exception to define-before-handwrite. The Generator *shouldn't* learn to emit them from aspects.

So the question I'd put back to you, to find the seam precisely: the `session.set` seed and the `getRuntime().get` lookup are two halves of one logical-channel binding. **Do you see the channel-name as belonging to the rule's definition (the rule declares "output: consumer"), with only the ref-binding living in the wiring layer? Or should even the channel name stay out of the type system entirely, so the whole `consumer`-slot vocabulary is something the wiring layer imposes?**

---

**[Dev]** It is completely domain dependent logic how actors use actor refs whether in the actor itself or in the rules executed by the actor. The consumer/provider model was invoked as supporting the set of domain examples only.

**[Claude]** Understood — and that collapses the question rather than refining it. There's no framework-level channel/role/port concept to find, because there's nothing universal to model. How an actor threads an `ActorRef` — in its own `receive` body, or in the RHS of a rule it fires, or not at all — is just that domain's logic. `session.set("consumer", …)` / `getRuntime().get("consumer")` is the *examples'* chosen idiom, not a pattern the framework should recognize or name. My "does the channel-name belong to the rule or the wiring layer" fork was the same over-lifting in a new costume. Dropped. The framework ascribes no meaning to refs; it just faithfully emits whatever body the domain author wrote, wherever they wrote it — and the Generator already does exactly that (`actorActionBody` passes body elements through verbatim).

The only thing left that's genuinely framework-level (and carries *no* ref semantics) is mechanical: an actor's emitted factory is currently a bare `lazy val actorType` with no construction parameters, whereas every ordinary type's factory honors `dracoAspect.factory.parameters`. Making actor factories honor generic params the same way would let a domain author declare construction arguments — of *any* type — and write their own body against them, with the framework attaching no meaning to whether one happens to be an `ActorRef`.

Is that the right altitude — actor factories as expressive as ordinary factories, nothing more — or would you rather even that stay out, and these example actors simply remain hand-written until Orion drives them?

---

**[Dev]** Manually "defined" not written.

**[Claude]** Now I have it — the distinction is *defined* (authored as JSON TypeDefinition, generated to Scala) versus *written* (Scala source by hand). These actors get manually defined in JSON; they don't stay as hand-written `.scala`. And the enabler is making actor factories honor `dracoAspect.factory.parameters` generically — the author declares `consumer: ActorRef[Json]` as an ordinary construction parameter and authors the `session.set` body themselves; the framework attaches no meaning to it.

*(Reads the hand-written `Creator.scala`, the ordinary factory-parameter emission, and the actor import path.)* That's the crux — and it makes the change small. The current `Creator.json` already reproduces the hand-written body exactly (session + `set` + insert + fire + close, all in `receive`); the *only* reason it can't generate `Creator.scala` is that `actorBehavior` emits a bare `lazy val actorType` and ignores `dracoAspect.factory.parameters`. One gap confirmed on the import side: `pekkoImports` emits `{Behavior, Signal, TypedActorContext}` but not `ActorRef`. So the enabler is three small, semantics-free changes, made for the Aerial `Creator` as the proof:

- [Generator.scala](src/mods/scala/draco/Generator.scala) — `actorBehavior` now emits `def actorType(consumer: ActorRef[Json])` when `dracoAspect.factory.parameters` is present (falls back to the old `lazy val actorType` when absent, so the param-less actors emit byte-identically); `actorImports` adds the `ActorRef` import only when a param references it.
- [Creator.json](src/mods/resources/domains/aerial/Creator.json) — declares `consumer: ActorRef[draco.format.json.Json]` as an ordinary construction parameter. No framework semantics attached — the `session.set("consumer", consumer)` in the body is the domain's own logic, emitted verbatim as before.

Nothing here teaches the framework about consumers, channels, or wiring — it just makes an actor's factory as expressive as any other type's, so the author can *define* construction parameters instead of hand-writing the factory.

One question on intent so I replicate correctly: the current body keeps session creation + seed in `messageAction` (per-message, with `close()`). Now that HEAD emits `signalAction` as run-once setup, do you want these definitions left as-is (faithful to the existing hand-written), or moved to the persistent-session shape (session + seed at setup, `insert`+`fire` per message) as part of defining them?

---

**[Dev]** Before these example domains and domain element type, I had not considered using signalAction for anything. Now I see it functions very well for actor initialization and shutdown in all cases. What do you think?

**[Claude]** I think your instinct is right about *what* it's surfacing, and slightly optimistic about *one field* covering it.

**The good part is real.** You've found that an actor has a lifecycle bracket around its message loop, and that bracket is universal — resources to stand up before the first message and tear down after the last. Giving that its own definitional home, distinct from `messageAction`, is the correct instinct. It's exactly the `bracket acquire release use` shape: `messageAction` is `use`, and the brackets are lifecycle.

**The wrinkle is mechanical, not aesthetic.** Init and shutdown aren't one moment — they fire at two different Pekko seams: **init** runs *at behavior construction*, once, before any message (there is no "started" signal in Pekko typed); **shutdown** arrives *as the `PostStop` signal*, in `receiveSignal`. A single `signalAction` body can be emitted into one of those locations, not both. So "one field for init and shutdown" can't hold literally — they're a *dual pair*, not the same action. And note the naming tension: in Pekko, "signal" *means* `receiveSignal` — `PostStop`, `PreRestart`, `Terminated`. So `signalAction` semantically *is* the shutdown/teardown channel. HEAD currently routes it to construction (init) instead and leaves `receiveSignal` a no-op — that's a squatter on the wrong seam.

**What I'd recommend: three handlers, each mapped to its real seam.**

```text
setupAction    -> instance-scope, runs once at construction   (acquire: new session, seed refs, load)
messageAction  -> receive(ctx, msg)                           (use)
signalAction   -> receiveSignal(ctx, signal) / case PostStop  (release: session.close)
```

One thing gates whether this is worth the third field: it only earns its keep with *persistent* sessions. The current actors create + seed + `close()` the session inside `messageAction` every message — in that world there's no lifecycle to bracket. Lifecycle becomes first-class precisely when the session is stood up once at `setup`, reused across messages, and closed at `PostStop`. So my question back: are you moving these actors to a persistent per-actor session? If yes, the three-handler bracket is the clean model and I'd reclaim `signalAction` for `receiveSignal`. If sessions stay per-message, I'd leave `signalAction` as the run-once setup it is today rather than add ceremony it can't yet justify.

---

**[Dev]** There are two dimensions for your question about actor persistence 1) Is there both stateless and stateful  actor types 2) are there multiple value instances of an actor type running concurrently?

**[Claude]** Good decomposition — those two axes are exactly what my "persistent session" question was bundling together, and they're orthogonal.

**Dimension 1 — stateless vs stateful — decides whether the bracket exists at all.** In Evrete terms this is `newStatelessSession()` vs `newStatefulSession()`: stateless — no memory between messages, `messageAction` self-contained, nothing to acquire or release; stateful — the working memory *is* the actor's state, `setup` stands the session up, `messageAction` inserts+fires, `signalAction`/`PostStop` closes it. The elegant consequence: you don't need a "kind" flag on the actor type. **Statefulness is encoded by *where the session is created*** — `messageAction` (per-message) ⇒ stateless; `setup` (once) ⇒ stateful. The three-handler structure *is* the stateless/stateful distinction made visible. Today's Aerial actors create+`close()` per message — that's stateful-API used statelessly.

**Dimension 2 — multiple concurrent instances — decides the *scope* of the bracket.** `knowledge` (the compiled RETE network) is immutable and shared → stays type-scope `lazy val`, correct already. The `session` for a stateful actor is private state → must be per-instance, created once *per spawn*, never shared. The matrix:

```text
                 stateless              stateful
single instance  per-msg fire,          bracket once;
                 no bracket             session can be type-scoped
multi instance   behavior is shareable  *** per-instance setup/teardown ***
                 (no private state)     session per spawn; PostStop per spawn
```

The bottom-right cell is the demanding one, and it's the case the examples actually want (you'd spawn one `Creator` per `consumer` ref). It forces a real mechanical point: **`setup` must run per spawn, not once per type.** Pekko's guarantee for "per spawn" is `Behaviors.setup { ctx => … }` — but that returns a `Behavior[T]`, not an `ExtensibleBehavior[T]`, so it collides with `ActorType` being an `ExtensibleBehavior`. That tension is the thing to resolve if multi-instance-stateful is in scope.

So the question I need answered to commit the emission: **is multi-instance-stateful in scope?** If yes, we resolve the `ExtensibleBehavior` vs per-spawn-`setup` mechanism and the three-handler bracket is per-instance. If actors are effectively singletons, the bracket can stay simple and type-scoped, and I won't build per-spawn machinery you wouldn't use.

---

**[Dev]** Multi-instance stateful is in scope, and my preference is that any or all of the four are configuration parameters in orion, and definition author choices in draco.

**[Claude]** That lands it cleanly, and it resolves the `ExtensibleBehavior` tension I raised — without any new framework vocabulary. All four cells are expressible as **structural** authoring choices in the definition, so "author choice in Draco / config parameter in Orion" falls out naturally: Orion picks a cell as a config knob and emits the corresponding Draco definition shape.

The two axes map to two independent structural signals: **statefulness ← where the session is created** (in `messageAction` ⇒ stateless; in `setup` ⇒ stateful — placement *is* the choice, no flag); **multiplicity ← whether `actorType` is a value or a factory** (`lazy val` ⇒ one shared behavior ⇒ single instance; `def actorType(…)` ⇒ a fresh behavior per call ⇒ multi-instance).

The `def`-returns-fresh shape is exactly what makes the demanding cell safe: each `def actorType(…)` call constructs a new `Actor[T]{}` with its own instance-scope session, so "call `actorType()` once per spawn" gives per-instance state — **no `Behaviors.setup` needed, so `ActorType` stays an `ExtensibleBehavior`.** `knowledge` stays type-scope and shared; only the session is per-instance. `signalAction` reclaimed as `receiveSignal`/`PostStop` closes each instance's session.

This implies one refinement to what I landed last turn: I keyed `def`-vs-`val` on *params being present*, but multiplicity is really *factory presence*. A multi-instance actor with no construction args should still mint fresh instances. *(Fixes the trigger in `Generator.scala`.)* Aerial `Creator` as it stands is therefore **multi-instance stateless**: factory (multi) but session created+closed inside `messageAction` (stateless) — faithful to the existing hand-written code.

Two things to confirm, since both are your authoring-model calls: (1) **is factory-presence the right structural signal for multiplicity**, or did you picture multiplicity as its own declared dimension? (2) **Reclaiming `signalAction` for `receiveSignal`** — a behavior change to HEAD's just-landed emission, so I'll hold it until you say go. And one worked-example question: leave Aerial `Creator` multi-instance stateless, or promote it to multi-instance stateful so the example demonstrates the demanding cell end-to-end?

---

**[Dev]** Multiplicity as its own dimension will occur in the orion layer, not in draco.  While signalAction is invoked in PostStop cleanup, what comes before that is any post-rule-execution that would interact with working memory, and execute any pre-cleanup code the definition author would  add to signalAction. Let me know if any of this seems inconsistent with how you perceive my goals.

**[Claude]** Mostly consistent with how I read your goals — with one tension I want to surface, since you asked.

**On multiplicity → Orion: yes, and it means I mislabeled the factory.** Calling factory-presence "the multiplicity signal" put an Orion concern into Draco. In Draco the factory is *only* construction-parameterization; it says nothing about how many instances run. *(Corrects the comment so the wrong framing doesn't ossify.)*

**On `signalAction`: your description matches my model and sharpens it.** I had it as "PostStop → `session.close()`," but you're right that it's the whole shutdown routine, author-owned: at `PostStop`, the stateful actor's working memory holds everything the rules accumulated across its life, so `signalAction` first does the *post-rule-execution* read of that memory (harvest/flush the final state), then any author pre-cleanup, then the close. That's the natural fold — `messageAction` accumulates, `signalAction` reaps on stop. The framework emits that body verbatim into `receiveSignal`; it doesn't synthesize the close — the author writes it.

**The one tension I perceive** is between "multiplicity lives only in Orion" and the emission I currently produce. A `lazy val actorType` *is itself* a single-instance commitment — one shared behavior object, one session, baked into the Draco output. So a no-factory actor is silently a multiplicity decision, which is exactly the thing you want to keep out of Draco. Two consistent resolutions, both leaving the decision at Orion:

- **(a) Draco always emits the factory form** — `def actorType(…)`, nullary when there are no construction args — so every actor is mintable and Orion alone chooses cardinality. Cleanest; Draco holds zero multiplicity commitment. Cost: call sites churn from `actorType` to `actorType()`, and the existing param-less actors all change shape.
- **(b) Draco's `lazy val` is just the standalone single-instance *default*** that Orion regenerates into a factory when its multiplicity config says "many."

I lean **(a)** — it's the only form that holds *no* multiplicity opinion, which is what you're asking for; the churn is mechanical and one-time. Which way on (a)/(b)?

---

**[Dev]** (a)

**[Claude]** (a) it is — Draco holds no multiplicity opinion; every actor is a mintable factory. Now the consequence to manage: every param-less actor shifts from `lazy val actorType` to `def actorType(): ActorType`, so committed `.scala` needs regenerating and call sites pick up a `()`. *(Scopes the churn: six actor declarations — `sentient/EgoActor`, the four `Consumer`s, test-fixture `NaturalActor` — and eight call sites, all the uniform `.actorType.asInstanceOf[…]` shape; applies both classes; verifies the only stray `.actorType` left is a doc comment in `AerialActorTest.scala:14`.)* State after (a): `actorBehavior` now always emits `def actorType(…): ActorType` (nullary when no construction params); Orion chooses cardinality by how often it calls the factory. Captured the design decisions in memory. Two design items remain queued: reclaim `signalAction` → `receiveSignal` + a run-once `setup` slot, and whether to promote Aerial `Creator` to multi-instance stateful.

*(Delegated action — Dev ran `sbt test`: all 185 pass.)* 185/185 green — (a) lands cleanly with no regression, and the param-less `Consumer`s stayed MATCH. One thing worth noting: the gen-map held at **26 match / 22 differ** — Aerial `Creator` did *not* flip to MATCH despite now generating `def actorType(consumer:…)`. *(Diffs the generated vs hand-written `Creator`.)* The whitespace-normalized comparison shows the generated `Creator` is now **structurally identical** — `trait Creator`, `object`, `knowledge`, `def actorType(consumer:…)`, and the `session.set("consumer", consumer)` body all line up. It stays in the DIFF column for one reason only: the hand-written file carries a top-of-file **descriptive comment block** (`// The source face of the medium's SourceSink (Brahma)…`) that the Generator doesn't emit. No structural gap remains. So the actor-fold is effectively done; the "8 factory actors" are no longer capability gaps — they're *comment* gaps.

Three things you could point me at next: (1) **comment/MATCH cosmetic** — drop the doc-comments from the eight Creators, or teach the definition to carry a leading doc comment, or treat these as MATCH-modulo-comment in the test; (2) **reclaim `signalAction` → `receiveSignal`** + run-once `setup` slot; (3) **promote Aerial `Creator` to multi-instance stateful**. Which one?

---

**[Dev]** From what did the source code comments originate, and what is lost if the comments are elided? When the definitions are loaded in memory as domain dictionaries. could there be meta-data associated with the dictionary that contained the type definition comments?

**[Claude]** Good questions — they separate cleanly.

**Where the comments came from.** They were hand-authored directly into the `.scala` files when these actors were hand-written. Nothing in `Creator.json` produced them — there's no doc field in the definition. So they're pure "written, not defined" prose: design knowledge that lives only in the generated target language, untracked by the type system. It's also *why* regeneration drops them — the Generator can't emit what the definition never carried.

**What's lost if elided.** Real, non-recoverable knowledge — actually two different kinds mixed in one block: *type semantics / role* ("the source face of the medium's SourceSink (Brahma)…") — genuine type documentation, not derivable from structure; and *design history* ("with the per-medium relay stripped, the Creator hands directly to the Consumer") — a past decision, which is journal material, not type documentation.

**Your metadata idea — yes, and I'd put it on the definition, not beside the dictionary.** A `TypeDictionary` is `Map[TypeName, TypeDefinition]`, so the cleanest "metadata in the dictionary" is simply a `doc` field *on `TypeDefinition` itself* — then it's already in the dictionary, co-located with the type, and survives any transform that moves the type. It passes the Haskell test cleanly: Haddock doc-strings attach to declarations as first-class artifacts, so a `doc` field isn't a Scala-comment leak — it's universal documentation each target renders its own way (`//` in Scala, `-- |` in Haskell). Documentation becomes **defined, not written**; regeneration is lossless; self-description deepens.

My recommendation: add a type-level `doc: String` to `TypeDefinition` (elided when empty), emit it as the leading comment, and migrate the eight Creator blocks into their JSON — keeping the *role* prose as `doc` and letting the *history* sentence go to the journal. Want me to add the field + emission and migrate the Aerial `Creator` doc as the proof?

---

**[Dev]** I'm thinking of introducing comments in the type definition language that succeeds YAML. Since I didn't put comments in source code examples I created, I assume you introduced them in your "hand-written" source code. Does the "authoritative" comment source exist only in the source code itself, after you wrote it?

**[Claude]** Let me verify the provenance rather than assume it. *(Greps the corpus for the comment text; runs `git blame` on the Creator files.)* Confirmed, and the answer is yes on both counts:

- The comment text exists in exactly four files — the four `Creator.scala` — and **nowhere in any definition**. No JSON, no YAML carries it. There is no upstream source.
- `git blame` attributes the lines to `ejb816` on commit `b04da0d` ("Media message-domains…", 2026-06-17), but that attribution is mechanical: you commit everything through your IDE, so git records you as author regardless of who wrote the content. `b04da0d` is Claude-co-authored work, and the prose — "SourceSink (Brahma)", "source face", "rule↔ref seam" — is mine, written into the hand-written Scala during that session.

So: **the authoritative source is the source code, and it has no origin prior to my writing it there.** You didn't author these comments; there's no canonical version anywhere. They're my interpretation of what the type does, deposited directly in the generated target.

That reframes last turn's decision. Migrating them into the definition would be **canonizing my prose as your design intent** — exactly backwards from how you're describing the successor language. The right direction is the one you're stating: comments become a first-class construct *in the definition language*, authored there deliberately, generated down into source. My Scala comments aren't a source of truth to back-port — at most a draft you might read, edit, or discard. The architectural conclusion from two turns ago still holds — documentation belongs in the definition — but the seed changes: don't auto-migrate the eight Creator blocks. If you want, I can simply **strip** my comments from the example source so regeneration is clean and the definition language starts as the sole, authored home for comments. Want me to do that?

---

**[Dev]** Yes. We can recover comments after we introduce the definition language.

**[Claude]** *(Scans for every commented example file and its JSON twin.)* That's a broad spread. Before I strip, the principled boundary: comments should come out of **definition-backed** example types — the ones that get regenerated and whose comments you'll reauthor in the successor language. The hand-only files with *no JSON twin* (the `*Sink.scala`, and pure helpers) aren't regenerated and have no definition to recover a comment into, so stripping them would just destroy docs with no path back. Clear split: **keep** (hand-only, no JSON twin) the four `*Sink.scala` and `world/Geodesy.scala`; **strip** (definition-backed, regenerated) 17 files. All these comments are mine per the provenance check, so they all come out of the def-backed set.

---

**[Dev]** I didn't mean to suggest destructively strip the comments. We should be able to preserve the specific comments for when we have the definition language that permits them. Just preserve them in a journal chapter explicit appendix. We may not have a format for a chapter appendix, so we can discus that first, if necessary.

**[Claude]** Understood — and I'm glad the strip was rejected; nothing was lost, all comments are intact in source. I moved to delete when the intent was **preserve, then** strip. You're right, and I over-rotated to deletion — that's the opposite of preserve-first. *(Saves the working principle to memory — preserve-then-strip, never destructive delete — then studies the journal's structure before proposing a format.)*

Here's the format I'd propose — discussing first as you suggested, since there's no appendix precedent in the journal. This session is **Chapter 50** (49 closed yesterday). I'd attach at its end: **`## Appendix A — Example-domain source comments (preserved for the definition language)`** — a short preamble (provenance; stripped from def-backed source for clean regeneration; recorded verbatim for reauthoring once the successor language permits comments), then one entry per commented def-backed type, **keyed by its JSON twin** so each comment has an unambiguous home to return to. Each entry: the definition path, the attachment point, and the verbatim text in a fenced block.

Three things to confirm before I generate it: (1) **inline vs sidecar** — append `Appendix A` directly into `draco-dev-chapter-50.md`, or a separate referenced file? I lean inline. (2) **Scope** — 17 def-backed files only; the five hand-only files keep their comments in place, since they're not regenerated. Agreed? (3) **Sequence after preservation** — strip the comments from the 17 def-backed files (now safe, since preserved), or leave source untouched until the definition language exists?

---

**[Dev]** 1. Inline
2. Scope 17
3. Sequence after preservation - strip comments

**[Claude]** Confirmed: inline, 17 files, strip after preservation. *(Pulls every comment verbatim — including scaladoc continuation lines — from the 17 files with attachment points; writes Chapter 50 with the inline appendix; applies the strip.)* Done — preserve-then-strip complete:

- **Chapter 50** written with the design narrative + **Appendix A** holding all 17 comments verbatim, keyed by JSON twin and attachment point (type-level, member, or rule body) so each can be reauthored when the definition language gains comments.
- **17 def-backed files stripped** clean — no comments remain, code untouched (the strip only removed full-line comments).
- **5 hand-only files** (`*Sink`, `Geodesy`) kept their comments — not regenerated, nothing to recover into.

*(Delegated action — Dev ran `sbt test`: all 185 pass.)* 185/185 green — but the gen-map didn't move (still 26/22), so comments weren't the *only* diff on those types after all. *(Diffs `Creator` post-strip.)* That's the real picture — and it's clarifying. What remains on `Creator` is **not** comments. Two things: (1) **import style** — I emit `ActorRef` as a *separate* `import` line; the hand-written folds it into the brace: `{ActorRef, Behavior, Signal, TypedActorContext}`. That one extra line offsets the whole file. (2) **Only aerial `Creator.json` got the factory param** — the other three Creators generate `def actorType()` (nullary) against a hand-written `def actorType(consumer:…)`. *(Fixes the import to merge into the brace; reads the marine/terrestrial/ethereal `Creator.json`s — identical structure — and adds the same `consumer` factory block to all three.)* All four `Creator` definitions now carry the factory param; with the comments already gone, the four Creators should now be byte-clean against generation. I expect gen-map **26 → 30 match**, with the remaining ~18 all *genuine*: `Input`/`Output`/`World.Consumer`/`World.Provider` (factory params not yet defined — their downstream types differ), the 4 `OriginateReport` rules (the rule-side `getRuntime` read — a real capability, not cosmetic), `Observable`/`Cartesian` (real custom geodesy), and the domain-shell import-style cases.

*(Delegated action — Dev reran `sbt test`: all 185 pass.)* 30 match / 18 differ, exactly as predicted — the four `Creator`s flipped to MATCH; the **`Creator` actor-fold is now closed end-to-end**: definition → generation → byte-clean source, no comment noise, no hand-written residue. *(Records the closed state in journal ch.50 and memory.)* Clean stopping point. Three ways forward: (1) **finish the other four factory actors** — mechanical, closes more of the gen-map; (2) **reclaim `signalAction`→`receiveSignal` + run-once `setup`** — the foundational lifecycle piece, prerequisite for #3 done honestly; (3) **promote Aerial `Creator` to multi-instance stateful**. My lean: **#2**, the design keystone the other two lean on.

---

**[Dev]** I go with your suggestion - #2

**[Claude]** #2 it is — the lifecycle keystone. Before I touch a core schema type, let me verify the "safe by construction" claim still holds. *(Greps: the only file mentioning `signalAction` is `ActorAspect.json` itself; nothing populates it — so both `setupAction` (new) and `signalAction` are empty everywhere today, and every actor emits byte-identically.)* The plan:

- **`ActorAspect` gains a third Action field, `setupAction`** (default `Action.Null`). The triad becomes name-honest: `setupAction`→construction (acquire, run once), `messageAction`→`receive`, `signalAction`→`receiveSignal`.
- **Generator `actorBehavior`**: the run-once construction block reads `setupAction` (it reads `signalAction` today); `signalAction` routes into `receiveSignal`, scaffolded as `signal match { case PostStop => <body>; Behaviors.same; case _ => Behaviors.same }` (PostStop fully-qualified, no import churn). Empty fields ⇒ today's exact output.

*(Checks the YAML twin and the round-trip test: `YAMLRoundTripTest` reads the JSON files and round-trips through YAML in-memory — the `.yaml` twin is human-authoring parity only, updated anyway for corpus consistency. Confirms `isActor` keys on derivation, so adding `setupAction` doesn't perturb detection. Applies the coordinated change across `Generator.scala`, `ActorAspect.scala` (trait, codec, factory, `isEmpty`), `ActorAspect.json`, `ActorAspect.yaml` — `setupAction` appended last so existing positional construction stays valid.)*

The lifecycle triad is now name-honest:

| field | emits to | role |
|---|---|---|
| `setupAction` | instance scope, once at construction | acquire (session, seed, load) |
| `messageAction` | `receive` | per-message (insert + fire) |
| `signalAction` | `receiveSignal` → `case PostStop` | harvest accumulated working memory + cleanup |

Expectations for the suite: no output changes — emission byte-identical, `ExampleDomainsGenTest` stays 30/18, all 185 green. The one to watch is `DracoGenTest` → `ActorAspect`: I hand-edited `ActorAspect.scala` to mirror what the Generator now produces from the 3-field JSON; if my hand-edit and the generated form diverge in any detail, that test flags it.

*(Delegated action — Dev ran `sbt test`: all 185 pass; `ActorAspect` self-generation matched on the first run; 30/18 unchanged.)* The schema change landed byte-clean. **`signalAction`→`receiveSignal` + `setupAction` is reclaimed**, and the multi-instance-stateful cell is now expressible end-to-end. *(Records in journal and memory.)* Session tally so far: always-`def` actor emission · `Creator` fold closed across four media (gen-map 26→30) · comments preserved (Appendix A) and stripped · preserve-then-strip principle saved · lifecycle triad reclaimed. The new fields are in place but **not yet exercised by any definition**. Two ways to put them to work: (1) finish the other four factory actors — mechanical, no lifecycle fields; (2) **promote an Aerial actor to multi-instance stateful** — the first real exercise of `setupAction`/`signalAction`. One design note: `Creator` *originates-and-forwards* (little to accumulate), whereas **`Consumer` is the more natural stateful demo** — accumulate received reports in a persistent session, harvest them at `PostStop`. My lean: #2, with `Consumer` as the subject — it actually exercises what we just built rather than leaving the new fields dormant. Which way?

---

**[Dev]** #2

**[Claude]** #2 with `Consumer`. *(Reads the current Aerial `Consumer` chain — definition, rule, sink, test.)* Here's the current chain: the `ConsumeReport` rule records each report to `AerialSink` *immediately* on fire, and the session is a per-message throwaway. The test sends one intent, sleeps, terminates, then checks the sink.

The stateful promotion (accumulate-then-reap) reshapes it into the demanding cell — a genuine **semantic** change, so I want to confirm the shape before touching four files:

**`Consumer.json`** — `setupAction` (run once, per spawn): `val session = knowledge.newStatefulSession()`; `val consumed = new java.util.ArrayList[String]()`; `session.set("consumed", consumed)` — a persistent per-instance session plus an accumulation buffer, seeded into the Evrete Environment via the *same* `set`/`getRuntime().get` seam the consumer-ref uses. `messageAction`: `session.insert(...)`; `session.fire()` — accumulate, **no close**. `signalAction` (`PostStop`): drain `consumed` to `AerialSink`; `session.close()` — **the reap**.

**`ConsumeReport.rule.json`** — RHS changes from recording immediately to appending into the buffer: `ctx.getRuntime().get[java.util.List[String]]("consumed").add(report.value.noSpaces)`.

**`AerialChainTest`** — because recording now happens at stop, the test sends *two* intents (to actually show accumulation), `Await`s `whenTerminated` before asserting, and checks both were harvested. Plus regenerate the committed `Consumer.scala` to match.

The one semantic question: this makes the sink observe **only at PostStop** (a batch reaped on shutdown), versus today's per-consume recording. That's the whole point of the stateful cell — the actor holds its consumed reports as private state and emits them on stop. If you'd rather a softer version (keep per-fire recording, make `signalAction` a summary), say so. Otherwise this is the faithful demonstration and I'll implement it. Go?

---

**[Dev]** Anything that can be done with working memory in signalAction can also be done in messageMessage, depending on how those actions are defined in the type definition. I'm not sure I understand the "one semantic question".

**[Claude]** You're right, and that dissolves it — I over-framed an authoring choice as a framework question. There's no framework-level decision: once `setupAction` stands the session up, the working memory is reachable from both `messageAction` and `signalAction`, so *where* the harvest lives is purely how the bodies are written. The framework only fixes *when* each runs (per message vs once at `PostStop`); it attaches no meaning to what's inside. The only concrete consequence of putting the reap in `signalAction` is observable *timing* — the sink fills once at stop instead of per-message — which is why the test has to await termination. That's not a question for you; it's just what this example happens to demonstrate. And since the whole point of #2 is to exercise the reclaimed `signalAction`/`receiveSignal`, that's where the reap goes. Implementing now.

*(Edits `Consumer.json`, `ConsumeReport.rule.json`, and rewrites `AerialChainTest`. Confirms `AerialGenTest` is the sanctioned regeneration step — it writes `Consumer.scala` and `ConsumeReportRule.scala` from their JSON, so the definitions are edited, not the `.scala`. Checks compile safety: no `-Xfatal-warnings`, so discarding the `Boolean` from `List.add` into the void SAM is safe.)* This is the **first definition to populate `setupAction` and `signalAction`**, exercising the reclaimed lifecycle end-to-end. Workflow: regenerate via `sbt "testOnly domains.aerial.AerialGenTest"`, reload, then the full suite.

*(Delegated action — Dev ran `AerialGenTest`: regeneration succeeded.)* Let me verify the emitted Scala before you recompile — the first time the new `signalAction`/`setupAction` paths produce real code. *(Reads the regenerated files.)* Both emit exactly as designed: `Consumer.scala` has `setupAction` at instance scope (persistent session + buffer + seed), `messageAction` in `receive` (insert+fire, no close), `signalAction` as the `receiveSignal` `PostStop` case (drain buffer → sink, then close) — and `def actorType()` mints this fresh per spawn, so each instance gets its own session. `ConsumeReportRule.scala`'s RHS appends to the buffer via the Environment seam. Clean, compilable Scala.

*(Delegated action — Dev ran `sbt reload` + `sbt test`: 183/185 — `AerialActorTest` failed twice.)* `AerialChainTest` passed on the new behavior — but I missed that `AerialActorTest` also exercises this rule and Consumer, and it assumed the old per-fire recording. Both failures are that: (1) the rule now needs the `"consumed"` buffer seeded in the session, which the direct-insert test doesn't do → NPE; (2) the actor test checks the sink immediately, but recording is now deferred to PostStop. *(Updates `AerialActorTest`: the rule-direct test seeds the `"consumed"` buffer as `setupAction` does; the actor test awaits `whenTerminated` so the `PostStop` reap has run before checking the sink.)*

*(Delegated action — Dev reran `sbt test`: all 185 pass; `AerialChainTest` line now reads "the stateful Consumer accumulates originated PositionReports and reaps them at PostStop".)* 185/185 green — gen-map holds at 30/18. **#3 is done**: the Aerial `Consumer` is the first definition to drive the full lifecycle triad end-to-end (`setupAction` stands up a persistent per-instance session + buffer, `messageAction` accumulates, `signalAction` reaps to the sink at `PostStop`). The reclaimed lifecycle isn't just plumbing now — it's exercised by a real worked example. *(Records in journal and memory.)*

**Session complete — a full arc landed, all green at 185/185:** always-`def` actor emission (multiplicity is Orion's) · `Creator` fold closed (gen-map 26→30) · comments traced to me, preserved in Appendix A, stripped · preserve-then-strip saved as a working principle · lifecycle triad reclaimed · first stateful actor. Remaining, whenever you pick it up: (1) the other four factory actors (`Input`/`Output`/`World.Consumer`/`World.Provider`); (2) the comment-bearing definition language superseding YAML, with Appendix A as its first reauthoring source. Want to keep going on #1, start scoping the definition language, or wrap here?

---

**[Dev]** #1

**[Claude]** #1 — the other four factory actors. Same situation as `Creator`: each has a hand-written `def actorType(<ref>)` but no `factory` in its JSON. *(Reads the four `.scala` files and their JSONs.)* Important wrinkle: this isn't the mechanical pass I implied — the four split into three very different cases, and they share a blocker. **They're all ruleless forwarding/codec actors** (`elementTypeNames: []`, no rule), and their JSON-definedness varies:

- **`Input`** — fully defined: its `messageAction` body (decode `cursor`→`Position`, forward) matches the hand-written `receive` exactly. Just needs the `worldConsumer: ActorRef[domains.world.World]` factory param.
- **`Output`** — *partially* defined: the body references `location` but the JSON is **missing the `msg match { case location: Location => }` wrapper** the hand-written has. Generated as-is, it wouldn't compile.
- **`World.Consumer` / `World.Provider`** — **no `actorAspect` at all**. Their entire `receive` bodies (the geodesy transform; the forward) are hand-written, absent from the JSON.

**Shared blocker:** the Generator *always* emits a `knowledge` val + `import org.evrete.api.Knowledge`, but these ruleless actors neither have nor need it — so generated carries dead code the hand-written omits, blocking MATCH even for `Input`. The clean enabler is a Generator fix: a ruleless actor omits the `knowledge` val and its import. My recommendation: do the Generator ruleless-knowledge fix + close `Input` now — the clean quick win — then `Output` and the two `World` actors are a real definition task (authoring hand-written bodies into JSON), one at a time. Proceed with the enabler + `Input`, then continue through the rest? Or just enabler + `Input` and reassess?

---

**[Dev]** Every setupAction should have a rule it can fire to acknowledge that it exists and what its message type is. Message type is not related to Message[Domain[T]]. is is just the draco type that is defined for that actor to receive.

**[Claude]** Got it — that supersedes my "omit knowledge for ruleless actors" idea entirely. The principle is the opposite: **no actor is ruleless.** Every actor carries an acknowledgment rule it fires from `setupAction`, so a `knowledge` val is always justified — the forwarding actors (`Input`/`Output`/`World.*`) become proper rule-bearing actors, and the Generator never needs a ruleless special case.

And I take the message-type clarification: the rule acknowledges the actor's **`Actor[T]` receive type** — the `T` already sitting in the derivation's `typeParameters` (`Json` for `Input`, `World` for `World.Consumer`) — *not* the deferred `Message[Domain[T]]` concept. The acknowledgment rule's pattern *is* that `T`, which is precisely what makes the actor's own rule engine "know" the type it receives. A ruleless session knows no type; the ack rule on `T` is how the actor declares it.

So the shape I'd build: a generated **acknowledgment rule per actor**, pattern = the actor's `T`; `setupAction` builds the session (loading that rule) and fires it, so at construction the actor acknowledges *"I exist; I receive `T`."* Two things I need from you to make the firing concrete, since the engine needs a fact to fire on: (1) **what triggers the acknowledgment fire at setup?** A rule with pattern `T` won't fire until a `T` is inserted — either (a) `setupAction` inserts a startup token the rule matches, or (b) "acknowledge" happens on the first real message. (2) **What does the rule's action *do*** to acknowledge — log at debug, or record somewhere a test can observe? My lean is (1a) — a self-inserted token fired once at setup — because it makes "acknowledge at setup" literally true and independent of message traffic.

---

**[Dev]** Do you think it sufficient for the setupAction to insert the Null instance value of its receive type, into working memory, which would trigger the rule that sends to the runtime registrar actor

**[Claude]** Short answer: yes — it's a clean trigger, and routing to a registrar gives the acknowledgment a real purpose instead of a throwaway log. Inserting `T.Null` reuses the existing `Null` convention as the setup sentinel, and the rule's pattern *being* `T` is exactly how the actor "declares its message type." Two viability checks decide whether it actually works, though. *(Verifies: does `Json` have a `Null`? Does `World`?)*

Grounded check changes my answer: **neither receive type currently has a `Null`** — `Json` is `Format[io.circe.Json]` with none, and `World` is a bare `trait World extends DracoType` with none. So as proposed there's literally nothing to insert. The mechanism is logically sufficient, but it presupposes `T.Null` exists, and that precondition isn't met for the actors in question. It's sufficient *only if* we first make every receive type carry a `Null`. That's easy and natural for `Json` (wrap `io.circe.Json.Null`), but **`World` is the awkward one**: it's an abstract message domain, and a "null `World`" is a forced, degenerate sentinel value. There's also a secondary Evrete risk — if `T.Null` is an anonymous subclass instance, `forEach(classOf[T])` may not match it (Evrete maps facts by exact runtime class; recall the `Integer`-not-`Int` and `Tuple3` gotchas).

The robust alternative keeps everything you want except the literal "Null of T": **insert the actor's own `typeDefinition`** at setup. It always exists, it *is* a `DracoType`, and it carries the message type in its `derivation.typeParameters` — so the registrar still learns "this actor exists and receives `T`," read off the fact. One generic acknowledgment rule (pattern: `TypeDefinition`) serves every actor; no per-receive-type `Null`, no abstract-`World`-Null problem, no anonymous-subclass matching risk. The tradeoff is the point you flagged: with `T.Null` the rule's *pattern is `T`* (the message type declared structurally); with `typeDefinition` the pattern is uniform and `T` is data the registrar reads.

Two things to decide: (1) **sentinel** — `T.Null` (requires giving every receive type, including `World`, a `Null`) or `typeDefinition` (always works)? I lean `typeDefinition` for robustness and uniformity. (2) **The runtime registrar** is a new component either way — an actor receiving these acknowledgments, its `ActorRef` seeded into every actor's session (the consumer-ref seam, universalized). Is building it in scope now, or do we stub the acknowledgment until the registrar exists?

---

**[Dev]** Look into how Pekko ActorSystem factors into how actors work in draco, if it does. Let's discuss what you find out.

**[Claude]** *(Launches an investigation agent over the codebase's `ActorSystem` usage.)* Here's what the investigation turns up — and it bears directly on the registrar idea.

**ActorSystem doesn't factor into draco at all — it's a caller boundary.** Zero `ActorSystem` in `src/main`. Every instantiation (9 of them) is in tests or demos, with a uniform pattern: the **test** creates the system, wires the actor graph by hand, and terminates it. Draco itself has **no runtime** — no guardian, no registrar, no registry, no `Receptionist`, no supervision, no system ownership. So draco's actual contract is narrow: **it generates Pekko `Behavior`s.** Everything past that — spawning, wiring refs, lifecycle, who's alive — is the caller's job (tests today, Orion tomorrow). This is consistent with the boundaries you've already drawn: multiplicity → Orion, wiring → authored at draco / automated at Orion. **Runtime identity/liveness is the same shape: a runtime concern, not a draco one.**

**The constraint that decides the registrar mechanism:** `setupAction` runs *without a Pekko context*. Because `Actor[T]` is an `ExtensibleBehavior` and `setupAction` emits as instance-scope `val`s, it's evaluated when `actorType()` is *called* — before the behavior is spawned. There's no `ActorContext` yet; the context only arrives in `receive(ctx, msg)` and `receiveSignal(ctx, signal)`. Direct consequence for "insert the sentinel at setup → rule fires → send to the runtime registrar": at setup, the actor **cannot discover** the registrar — no `ctx`, so no `ctx.system.receptionist`, no `ctx.self`. The registrar `ActorRef` would have to be a **construction parameter, seeded by whoever spawns the actor** — exactly the consumer-ref seam, universalized. The idiomatic Pekko alternative — the built-in `Receptionist` — needs `ctx`, so it could only happen on the first message, not at setup.

So your proposal is coherent, but it forces a choice: (1) **acknowledge at setup** ⇒ registrar as seeded ref param on every actor, Orion threads it in; (2) **acknowledge on first receive** ⇒ `ctx`/Receptionist available, but it's no longer purely setup-driven; (3) **change the actor model** so setup carries a context — the `ExtensibleBehavior`-vs-`Behaviors.setup` tension again; it would unseat `ActorType = ExtensibleBehavior`.

My read: the registrar belongs at the **Orion/runtime layer** (it owns the `ActorSystem` and the live-actor directory), and draco's part is just to emit actors that *can* acknowledge to a registrar `ActorRef` they're handed. Given setup has no `ctx`, option 1 is the only one that keeps the acknowledgment literally at setup without disturbing the actor model — at the cost of every actor carrying a registrar param that Orion supplies. Where do you want to take it — accept that the registrar is an Orion-layer actor draco only sends to via a seeded ref, or rethink whether "acknowledge at setup" is worth the universal param versus letting `ctx.self`/Receptionist do it on first receive?

---

**[Dev]** I am reluctant to use ActorSystem at the draco core, so what do you  suggest we can do without a Pekko context to configure a group of actors based on our current type definition?

**[Claude]** The key realization from the ActorSystem map: the only thing that *needs* a Pekko context is the final mechanical step — spawning a behavior and getting back an `ActorRef`. Everything before that — *which* actors, *what* each receives, *who wires to whom* — is pure description that draco already half-holds. So the move is to make that description complete and draco-native, and leave the one context-touching step as a thin, generic edge.

**Suggestion: a draco "assembly" definition that configures the group as data.** Wiring can't be expressed as `ActorRef`s (those only exist inside a context), but it *can* be expressed by **identity** — the member's `TypeName`. So introduce a composition-level definition (call it `Assembly`, or `ActorGroup`) that holds:

- **members** — the actors in the group, by `TypeName` (`domains.aerial.Consumer`, `domains.aerial.Creator`);
- **bindings** — for each actor's construction param, which member fills it (`creator.consumer → consumer`);
- **entry** — the member that receives the group's inbound traffic.

Crucially, this stays *off* the actor types themselves — consistent with everything you've held: the actor type doesn't know its topology; the assembly does. And it's **based on the current type definitions** — each actor's factory already declares its construction params and their message types, so the assembly only adds the role→member bindings. It even **type-checks without Pekko**: `Creator`'s `consumer: ActorRef[Json]` can only bind to a member whose receive type is `Json` — a draco-level validation, no context required.

**The one Pekko-touching step becomes generic and external.** Today every domain hand-writes a guardian (`ctx.spawn(consumer); ctx.spawn(creator(consumer))`) — all the *same logic*: topo-sort members by binding dependency, spawn each, thread the resolved refs into the next factory. That collapses into **one generic spawner** that consumes any `Assembly` — the only thing that ever imports `ActorSystem`/`ctx`, living at the edge (src/mods or the test/Orion layer), never in draco core. Draco *configures*; the edge *realizes*. This also fits the constraint we found: since `setupAction` has no context, an actor can't resolve its own wiring — it must be handed refs as construction params. The assembly + edge-spawner is exactly that: wiring resolved *before* spawn, threaded in as params.

**The deeper version, if you want it later:** abstract `ActorRef[T]` behind a draco `Sink[T]`/`Channel[T]` type, so even the actor *definitions* are Pekko-free, with a Pekko adapter in `src/mods` — then "configure a group" is entirely draco types end to end, and Pekko is one swappable binding. Bigger move; `Assembly` works the same either way.

My recommendation: start with the **`Assembly` definition + one generic edge-spawner** — context-free group configuration now, collapses the duplicated guardians, and is the natural home for the registrar/acknowledgment later (the assembly already *is* the static "who's in the group and what they receive"). Want me to sketch the `Assembly` shape against the Aerial chain concretely, or pull the World transform chain through it since it's the bigger wiring?

---

**[Dev]** Show me a type definition compatible Scala implementation of Assembly as if it were generated from the type definition.

**[Claude]** Here's `Assembly` (and the small `Binding` it carries) rendered exactly as the Generator would emit a draco-core type — same shape as `ActorAspect.scala`: `trait` + `object`, `typeDefinition` via `loadType`, `dracoType`/`domainType`, elided Circe codec, defaulted `apply`, `Null`. Note there is **no Pekko import anywhere** — it's pure data, which is the whole point.

```scala
package draco

trait Assembly extends DracoType {
  val members: Seq[TypeName]
  val bindings: Seq[Binding]
  val entry: TypeName
}

object Assembly extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Assembly", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Assembly] = Type[Assembly] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  // implicit lazy val encoder/decoder: field-elided Circe codec over
  // members / bindings / entry, exactly the ActorAspect pattern

  def apply (
    _members: Seq[TypeName] = Seq.empty,
    _bindings: Seq[Binding] = Seq.empty,
    _entry: TypeName = TypeName.Null
  ) : Assembly = new Assembly {
    override lazy val members: Seq[TypeName] = _members
    override lazy val bindings: Seq[Binding] = _bindings
    override lazy val entry: TypeName = _entry
    override lazy val typeDefinition: TypeDefinition = Assembly.typeDefinition
  }

  lazy val Null: Assembly = apply()
}
```

The peer type its `bindings` reference — one wiring edge, "member `from`'s construction param `param` is filled by member `to`":

```scala
trait Binding extends DracoType {
  val from: TypeName
  val param: String
  val to: TypeName
}
// object Binding: same generated shape — loadType, elided codec,
// defaulted apply(_from, _param, _to), Null
```

**The Aerial chain as an `Assembly` value** (what a caller or a generated literal would hold) — entirely context-free:

```scala
Assembly(
  _members  = Seq(
    TypeName("Consumer", _namePackage = Seq("domains", "aerial")),
    TypeName("Creator",  _namePackage = Seq("domains", "aerial"))
  ),
  _bindings = Seq(
    Binding(
      _from  = TypeName("Creator",  _namePackage = Seq("domains", "aerial")),
      _param = "consumer",
      _to    = TypeName("Consumer", _namePackage = Seq("domains", "aerial"))
    )
  ),
  _entry = TypeName("Creator", _namePackage = Seq("domains", "aerial"))
)
```

Two things worth noting about this as a generated type: **it's authored/loaded like any other definition** — its `TypeDefinition` JSON would carry `elements = [members: Seq[TypeName], bindings: Seq[Binding], entry: TypeName]` and a `factory` with those three defaulted params, nothing the Generator can't already emit. And **it's validatable without Pekko** — given the member `TypeName`s, you can load each actor's `TypeDefinition`, read its `factory.parameters`, and check every `Binding.param` names a real construction param whose `ActorRef[T]` element type matches the `to` member's receive type. That's the whole "configure a group" step — pure draco, no context. The single Pekko-touching consumer is the generic spawner that walks `bindings` for the topo order and calls each `actorType(...)`.

Want me to push the World transform chain (`Input → World.Consumer → Provider → Output`) through this shape next — four wired members and two distinct message types (`Json` and `World`), so it'll stress the binding/type-check more than the Aerial pair?

---

**[Dev]** An Assembly of Actors - well done!

**[Claude]** Thanks — "an Assembly of Actors" is the right name for it. The pleasing part is how little it needs: members + bindings + entry, addressed by `TypeName`, validated against the factories already in the definitions — and Pekko stays a single swappable edge. *(Captures the concept in memory.)* Where the `Assembly` thread stands: sketched and endorsed, not yet committed — when you want to make it real, the path is the ordinary one (write `Assembly.json` + `Binding.json`, let the Generator emit them, then the one generic edge-spawner that walks `bindings`).

Whenever you pick back up, the open threads are: **`Assembly`** — commit the definitions + spawner; fold the four hand-written chain-test guardians into it. **The World chain through `Assembly`** — the four-member, two-message-type stress case. **The acknowledgment/registrar** — now clearly an Orion/runtime concern that the `Assembly` feeds. **The other factory actors** (#1) — `Input`/`Output`/`World.*`, still needing their bodies defined (the ruleless-actor question now resolved by "every actor acknowledges via a setup rule"). A good place to pause. Nicely-shaped session.

---

**[Dev]** Let's commit and push and prepare to continue in a new session.

**[Claude]** On it. Per the project rule I don't run `git commit`/`push` myself — I'll prepare the commit message and the exact commands for you to run (the `draco-git-record` audit trail). *(Reads the working tree: the 42 changed files are exactly this session's work, nothing stray; branch `main`, upstream `origin/main`, HEAD already pushed.)* Prepared the commit block: subject "Actor lifecycle triad + always-def factories; first stateful actor; comment preservation", with sections covering the always-`def` emission (multiplicity is Orion's), the `Creator` factory fold closed across four media (26→30), the lifecycle triad reclaim (`setupAction` added; HEAD `91e4a7c` had squatted `signalAction` on construction and left `receiveSignal` a no-op — this evicts it), the first stateful actor (Aerial `Consumer`, accumulate-then-reap), and comments preserve-then-strip — full suite 185/185 green.

Everything's in place to continue fresh: journal `draco-dev-chapter-50.md` (narrative + Appendix A), three new memory entries indexed in `MEMORY.md` (`project_actor_lifecycle_dimensions`, `project_assembly_actor_group`, `feedback_preserve_before_destructive_strip`), and the open threads recorded.

*(Delegated action — Dev ran the commit block: `[main 4cbd413]`, 42 files changed, +497/−166, pushed `91e4a7c..4cbd413` to `origin/main`.)* Pushed cleanly — the session's work is committed and live. A new session will pick up from `MEMORY.md` with the context intact. Good session — the actor-lifecycle design went from open question to realized-and-validated, and `Assembly` is teed up as the natural next move. Talk soon.

## Status

Commit `4cbd413` (42 files, +497/−166) on `origin/main`. Full suite **185/185** green throughout; gen-map `ExampleDomainsGenTest` 26 → **30 match / 18 differ**, all remaining diffs genuine. Landed: always-`def actorType` emission (multiplicity is Orion's alone); the `consumer` factory parameter defined on all four `Creator` JSONs and the `Creator` fold closed byte-clean; the lifecycle triad reclaimed (`setupAction`→construction, `messageAction`→`receive`, `signalAction`→`receiveSignal`/`PostStop`) with `ActorAspect` self-generation matching first run; the first stateful actor (Aerial `Consumer`: `setupAction` stands up a persistent per-spawn session + buffer, `messageAction` accumulates, `signalAction` reaps at PostStop); comment provenance established, Appendix A preserved, 17 def-backed files stripped (5 hand-only files keep theirs); preserve-then-strip saved as a standing principle. Queued: commit the `Assembly`/`Binding` definitions + the one generic edge-spawner and push the World chain through it; the acknowledgment rule / runtime registrar (an Orion-layer concern, seeded-ref seam); the four remaining factory actors (`Input`/`Output`/`World.Consumer`/`World.Provider`), now resolvable via "every actor acknowledges via a setup rule"; and, the larger arc, the comment-bearing definition language that supersedes YAML — at which point Appendix A is the reauthoring source.

---

## Appendix A — Example-domain source comments (preserved for the definition language)

Provenance: Claude-authored, hand-written into the example `.scala` during the media-domains sessions (commits through `b04da0d`/ch.42–47); no definition twin ever carried them. Stripped from the 17 definition-backed example types in this chapter so regeneration is clean; recorded here verbatim, keyed by the JSON definition they document and the point they attached to, for reauthoring once the successor type-definition language permits comments. Fenced as `text` (comment fragments, not whole compilation units).

### domains/aerial/Creator — Creator.json
Attachment: type-level, before `object Creator`
```text
// The source face of the medium's SourceSink (Brahma): receives a seed intent,
// whose OriginateReport rule originates a report and sends it — via the rule↔ref
// seam — to the medium's Consumer, seeded as "consumer" in the session Environment.
// With the per-medium relay stripped, the Creator hands directly to the Consumer;
// cross-medium transforms live in World, not inside a Format domain.
```

### domains/aerial/OriginateReportRule — OriginateReport.rule.json
Attachment: rule action body
```text
// Creation phase: the report is *originated algorithmically* from the intent —
// no JSON fixture. Here the origination is a unit transform (flight level, in
// hundreds of feet, → altitude in feet) carrying the callsign through. This is
// the seam where a real medium would synthesize its native representation.
```

### domains/aerial/Position — Position.json
Attachment: type-level, before the `Position` type
```text
/** The TYPED Aerial position form — a direct subtype of `Aerial` (hence `World`),
  * the strong composition tree the input adapter decodes a loose `PositionReport`
  * into. Geodetic horizontal (degrees) + altitude in feet; the discriminator is
  * subsumed by the type itself. */
```

### domains/aerial/Input — Input.json
Attachment: type-level, before `object Input`
```text
/** Aerial input adapter (codec, World-bound): decodes a loose `PositionReport` Json
  * into the typed `Position` — a direct subtype of `Aerial`, hence an indirect
  * subtype of `World` — and hands it to `World.Consumer`. The only place that knows
  * Aerial's wire schema; World itself deals only in typed values.
  *
  * Definition-backed (`Input.json`, `actorAspect.messageAction`) like the medium's
  * `Creator`/`Consumer`; the Scala body stays hand-written until the actor-emission
  * Generator fold. Not a `domainAspect` member of Aerial — actors are defined types
  * but not message-type members, matching the `Creator`/`Consumer` convention. */
```

### domains/ethereal/Creator — Creator.json
Attachment: type-level, before `object Creator`
```text
// The source face of the medium's SourceSink (Brahma): receives a seed intent,
// whose OriginateReport rule originates a report and sends it — via the rule↔ref
// seam — to the medium's Consumer, seeded as "consumer" in the session Environment.
// With the per-medium relay stripped, the Creator hands directly to the Consumer;
// cross-medium transforms live in World, not inside a Format domain.
```

### domains/ethereal/OriginateReportRule — OriginateReport.rule.json
Attachment: rule action body
```text
// Creation phase: originate an EphemerisReport algorithmically from the launch
// intent. Ethereal's native representation diverges from the other media —
// discriminator "category", id "object", altitude in kilometres, and the report
// concept is an "EPHEMERIS" (a position computed from orbital mechanics), distinct
// from position/location/fix. The origination is a clean unit transform (nautical
// miles -> kilometres).
```

### domains/marine/Creator — Creator.json
Attachment: type-level, before `object Creator`
```text
// The source face of the medium's SourceSink (Brahma): receives a seed intent,
// whose OriginateReport rule originates a report and sends it — via the rule↔ref
// seam — to the medium's Consumer, seeded as "consumer" in the session Environment.
// With the per-medium relay stripped, the Creator hands directly to the Consumer;
// cross-medium transforms live in World, not inside a Format domain.
```

### domains/marine/OriginateReportRule — OriginateReport.rule.json
Attachment: rule action body
```text
// Creation phase: originate a FixReport algorithmically from the voyage intent.
// Marine's native representation diverges from the other media — discriminator
// "kind", id "vessel", depth in fathoms not feet/metres, and the report concept
// is a "FIX" (a determination one takes), distinct from position/location. The
// origination is a clean unit transform (metres -> fathoms).
```

### domains/terrestrial/Creator — Creator.json
Attachment: type-level, before `object Creator`
```text
// The source face of the medium's SourceSink (Brahma): receives a seed intent,
// whose OriginateReport rule originates a report and sends it — via the rule↔ref
// seam — to the medium's Consumer, seeded as "consumer" in the session Environment.
// With the per-medium relay stripped, the Creator hands directly to the Consumer;
// cross-medium transforms live in World, not inside a Format domain.
```

### domains/terrestrial/OriginateReportRule — OriginateReport.rule.json
Attachment: rule action body
```text
// Creation phase: originate a LocationReport algorithmically from the march
// intent. Terrestrial's native representation diverges from Aerial's — different
// keys (type/unit/elevationMetres vs message/callsign/altitudeFeet), metres not
// feet, "LOCATION" (weakly fixed) not "POSITION" (a point in motion). The
// origination is a clean unit transform (feet -> metres) carrying the unit id
// through; this is the divergence a future Aerial=>Terrestrial transform must
// bridge.
```

### domains/terrestrial/Location — Location.json
Attachment: type-level, before the `Location` type
```text
/** The TYPED Terrestrial location form — a direct subtype of `Terrestrial` (hence
  * `World`), the strong composition tree the output adapter encodes an `Observable`
  * into before re-serialising to a loose `LocationReport`. Geodetic horizontal
  * (degrees) + elevation in metres. */
```

### domains/terrestrial/Output — Output.json
Attachment: type-level, before `object Output`
```text
/** Terrestrial output adapter (codec): encodes a typed `Location` (a World subtype)
  * back into a loose `LocationReport` Json and hands it to the medium's `Consumer`
  * — the sink face of Terrestrial's SourceSink. The only place that knows
  * Terrestrial's wire schema.
  *
  * Definition-backed (`Output.json`, `actorAspect.messageAction`) like the medium's
  * `Creator`/`Consumer`; the Scala body stays hand-written until the actor-emission
  * Generator fold. Not a `domainAspect` member of Terrestrial — actors are defined
  * types but not message-type members, matching the `Creator`/`Consumer` convention. */
```

### domains/world/Consumer — Consumer.json
Attachment: type-level, before `object Consumer`
```text
/** World.Consumer — the transform INPUT face (Vishnu-at-work). Receives a typed
  * source value (a World subtype, decoded by a subdomain input adapter) and
  * produces the target representation through the change of form that PRESERVES
  * MEANING: it projects through the `Observable` world-fact (geodetic -> ECEF ->
  * geodetic), then hands the typed result to the `Provider`.
  *
  * This is where "all transform rules live in World" is realised — so World knows
  * the media types by design. For this first slice the transform is Scala (the
  * `Geodesy`/`Observable` core proven by `AerialTerrestrialTransformTest`);
  * expressing it as JSON-backed World rules is the next precursor to TransformBuilder. */
```
Attachment: inline in `receive`, `case position: Position` branch
```text
// input adapter projected nothing yet — World owns the geodesy: feet ->
// metres, geodetic -> ECEF (the Observable world-fact) -> geodetic.
```

### domains/world/Observable — Observable.json
Attachment: type-level, before the `Observable` type
```text
/** The world-fact: the objective thing the media's reports are reports *of*, the
  * meaning-invariant a transform must preserve. A `Holon` is the perspective that
  * observes; an `Observable` is what is observed. It carries the canonical position
  * in BOTH frames simultaneously — `geocentric` (Geocentric/Axial, ECEF) and
  * `heliocentric` (Heliocentric/Ecliptic) — and will later grow identity / velocity
  * / time as the tracked thing acquires more state. Heliocentric is `Cartesian.Null`
  * until the first crossing to Ethereal. */
```
Attachment: member `def fromGeodetic`
```text
/** Geocentric/Axial input-adapter primitive: geodetic (degrees, metres) -> Observable.
  * Uses Cartesian's positional-value constructor to map the ECEF triple into x/y/z. */
```
Attachment: member `def toGeodetic`
```text
/** Geocentric/Axial output-adapter primitive: Observable -> geodetic (degrees, metres). */
```

### domains/world/Provider — Provider.json
Attachment: type-level, before `object Provider`
```text
/** World.Provider — the transform OUTPUT face: re-dispatches a transformed typed
  * value (a World subtype) to the target subdomain's output path. Thin in this
  * first vertical (one configured downstream); type-directed routing across many
  * targets arrives with the JSON-backed transform rules. */
```

### domains/world/Cartesian — Cartesian.json
Attachment: type-level, before the `Cartesian` type
```text
/** A named-field 3D Cartesian coordinate (metres). The composition tree exposes
  * `x`, `y`, `z` as fields (messages-as-named-trees), and the factory *also* accepts
  * a positional coordinate value, mapping `(_1, _2, _3) -> (x, y, z)` — so the named
  * surface and draco's positional `Coordinate` substrate compose through the
  * constructor rather than being a choice between them.
  *
  * Frame-agnostic: which frame a `Cartesian` is in (Geocentric/Axial vs
  * Heliocentric/Ecliptic) is fixed by where it sits in the world-fact, not by the
  * coordinate itself. */
```
Attachment: member `def apply (value: (Double, Double, Double))`
```text
/** Map a positional coordinate value into the named fields. */
```

### domains/world/World — World.json
Attachment: type-level, before the `World` domain
```text
/** The super-domain the four media derive — the shared semantic ground of the
  * transformation service (the Eagle's emanations / the invariant against which
  * meaning is preserved). A *message domain*: every contained type is a subtype of
  * `World`. For now that discipline is carried implicitly by `Domain[T]` + the
  * media's derivation; a future explicit `Message[Domain]` will name and enforce it.
  *
  * Not a Format domain: `World`'s messages are typed values (subdomain subtypes),
  * not `Format[Json]` shells. Its transform machinery (input/output adapter actors
  * and the meaning-preserving transform rules) is added in the next slice. */
```
