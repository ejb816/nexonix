# Draco Dev Journal — Chapter 58

**Session date:** July 7–8, 2026 (continues ch.57)
**Topic:** A design session, not a code change (beyond `drake.dlt`). From the codec encoder/decoder selector question, the Dev drew out two general principles — **presence** (absent vs present-empty vs present-full) and **inference** (explicit vs derivable) — correcting several of my framings along the way; the whole model is scoped as the target for the future typed **Generator[L]**. Then the session close: the drake corpus buildout plan, commit/push, and the git-record protocol. Transcript.

---

**[Dev]** Not yet, please. I want to expand the  substructure so that the user can choose which default encoder or decoder is needed since both will be provided if neither encoder or decoder fields are provided, encoder not present means decoder default only and  decoder not present means encoder default only.  Both encoder and decoder present (without subelements) means the same as neither present. We need to define specific subelement types for both encoder and decoder user customization.

**[Claude]** Before I touch anything, let me lock down the semantics — the tri-state is subtle and building it wrong wastes real work.

**The selector, as I read it.** Each of `encoder`/`decoder` can be **absent**, **present-but-empty**, or **present-with-subelements**. For the default-vs-none question:

| `encoder` | `decoder` | emitted |
|---|---|---|
| absent | absent | **both** defaults |
| present (empty) | absent | **encoder** default only |
| absent | present (empty) | **decoder** default only |
| present (empty) | present (empty) | **both** defaults (≡ neither) |

The rule underneath: if neither field is present → both; otherwise emit exactly the side(s) that are present. A side *present-with-subelements* means "custom, not the default."

**One implication worth surfacing now:** this requires distinguishing **absent** from **present-but-empty** — which the current aspect model *can't* do: the other four aspects elide empty ≡ absent. So `encoder`/`decoder` would have to be `Option`-typed (missing key → `None`, present key → `Some`, even when empty), and it means splitting the Generator's currently-joined encoder+decoder emission so each side can stand alone.

Recommended phasing: **Phase 1** — the selector (empty presence-marker sub-types, truth table wired into the Generator); **Phase 2** — the customization subelement types, designed against a green selector. Two things I need from you: confirm the truth table, and whether you already have a picture of the Phase-2 subelement shape. One naming flag: bare `Encoder`/`Decoder` in package `draco` would shadow circe's.

---

**[Dev]** In general I don't want present and empty to be the same as absent. It was codec that made me realize this. Let's discuss this to see if the principle can be generally and consistently applied. for both aspects and fields. We also have to be principled about defining new aspects, since type definition is paradigmatic -the only draco type metat-ype that has aspects.

**[Claude]** Good — this is worth getting right before any more code, because it's a principle, not a patch.

**The distinction, sharpened.** There are three states, not two: **absent** (not part of the definition), **present-empty** (part of the definition, at its zero/default), **present-full** (a specific value). Emptiness-elision — draco's current default, everywhere — collapses the first two. That's lossless *only* when present-empty and absent mean the same thing. Codec is the first place they don't: `encoder: {}` = "give me the default encoder" is a positive instruction; a missing `encoder` key = "no encoder." The framework silently equating them is exactly the bug you felt.

**Why codec is the canary.** For the four prior aspects, present-empty genuinely *did* equal absent — an empty `RuleAspect` **is** "no rule." No information lived in their presence, so eliding lost nothing. Serialization is the first facet where presence itself is the instruction, so it's the first to expose that "elide empties" was an *assumption*, never a law.

**The principle — and it recurses.** Presence is a first-class signal, distinct from emptiness; the model should preserve it, and elision is a local, explicit optimization declared per element only where present-empty and absent provably coincide. The same trichotomy holds at every level — aspect on `TypeDefinition`, field in an aspect, subelement in a field. One rule, three levels — so the codec "selector" isn't special machinery, it's the *first instance* of a general mechanism.

**Why TypeDefinition being paradigmatic matters.** An aspect isn't "a part a type has," it's **a dimension along which any type's definition can vary** — which is precisely why only the metatype has them. That gives a presence-aware test for aspect-hood: an X earns aspect status iff it is (1) **universal**, (2) **orthogonal**, (3) **presence-significant**, and (4) **irreducible** (not expressible as a field of an existing aspect). Field-vs-aspect falls out of (4): `discriminator` refines serialization → field; "how I serialize at all" → aspect.

The one decision this comes down to: **invert the default** (presence-preserving; fields opt *into* elision — my recommendation, since it's the version that's actually a principle) or keep elision default and make presence-significance opt-in. And how presence-significance is carried: by the type (`Option[T]`) or an explicit per-element flag. No code until we've settled this.

---

**[Dev]** Update the template part of drake.dlt  startiing with  adding codec and as much of its substructure that you can include, then also update anything that has changed with regard to what aspects and fields should be elide-able  and let's incrementally improve on it through discussion until we agree on convergence.

**[Claude]** This is a design-canvas task — the `.dlt` is where we work out the shape. Encoded a concrete proposal in `drake.dlt`:

- **Two independent markers** (they were conflated before): `*` = presence-preserved (writing it even empty emits JSON; omitting = absent); `~` = elidable (present-empty ≡ absent); `*~` = both.
- **New `PRESENCE` section** stating the absent / present-empty / present-full trichotomy as the governing principle.
- **Marked elidable (`~`)**: the collections (`modules`, `elements`, `parameters`, `body`, `globals`, `types`, `variables`, `conditions`) plus `factory`, `extensible`, `super`.
- **Marked presence-preserved**: the optional aspects `rule`/`actor`/`codec`, plus the *selector* fields — codec `encoder`/`decoder`, actor `start`/`message`/`signal`, and `discriminator`.
- **Codec substructure**: `discriminator` + `encoder`/`decoder`, each with a `~fields` list of `EncoderField`/`DecoderField` mapping directly to generated code — `enc <name> as <wire-key> when <bool> value <expr>` → `if (when) Some("wire-key" -> expr.asJson) else None`; `dec <name> from <wire-key> default <expr>` → the `downField(...).getOrElse(default)` line.
- **`DIVERGENCES`** records that the template is *ahead of* the implementation — the current Generator still elides uniformly, and the `Encoder`/`Decoder`/`EncoderField`/`DecoderField` types don't exist yet.

The calls I'd like you to confirm or correct: (1) actor `start`/`message`/`signal` as presence-preserved selectors — I generalized the codec selector insight to the actor, but it's a genuine reinterpretation; (2) `discriminator` presence-preserved (absent = default `"kind"`, present = explicit); (3) the optional aspects themselves presence-preserved — the strongest form of your principle, and the one with real corpus consequences; (4) the `EncoderField`/`DecoderField` shape — the least settled, where I'd expect the most iteration.

---

**[Dev]** 1.  start/message/signal only need presence for override behavior, otherwise default behavior is generated regardless of presence.
2. I did not explicitly specify "discriminator" as a field or function so I'm not sure how tol answer your question.
3. if rule/actor is present than all fields of either must have at least default values that can be overridden. If either is absent then that aspect is not in the definition.
4. For codec aspect, unlike rule/actor, the absent encoder/decoder does not have a generated default if the other is present, only of both are present or both are absent are defaults generated.

**[Claude]** Got it — that sharpens the model in an important way. The key correction: **only codec's `encoder`/`decoder` are true selectors** (with your symmetry rule). Actor/rule fields are *default-always, override-on-content*. Re-encoded, and how I took your four points:

1. **`start`/`message`/`signal`** → `*~` (elidable). Default behavior is generated *regardless* of presence; content is an override. They are **not** selectors. Corrected.
2. **`discriminator`** → parked. You're right that I introduced it (my scaffolding from the "discriminator only" pick); whether it's a field, moves inside encoder/decoder, or becomes derived is genuinely unsettled — flagged `OPEN` in a new `NOTES` block, no presence semantics imposed.
3. **`rule`/`actor` aspects** → `*` (presence-significant, facet-creating): present = the type *is* a rule/actor with every field defaulted-and-overridable; absent = not one at all. Their fields → `*~`.
4. **`codec`** → `*~` (a modifier, not facet-creating — unlike rule/actor), with `encoder`/`decoder` → `*` selectors and your symmetry rule: defaults generate only when both-present or both-absent; asymmetry *suppresses* the absent side.

And underneath that I proposed an **origination distinction**: **intrinsic** facets (computed by default for eligible types — codec, on this reading — so the aspect only modifies/selects, empty ≡ absent) vs **extrinsic** facets (opt-in via the aspect's presence — rule/actor). That intrinsic/extrinsic split feels like the actual convergence. Where we're not yet converged: does that framing match how you see it; `discriminator`'s nature; the `EncoderField`/`DecoderField` shape.

---

**[Dev]** Correction on codec: absent codec means no codec needed for that type. Present codec means encoder or decoder may be absent but not both.

**[Claude]** That's an important correction — and it revises the framing I'd landed on. **Absent codec = no codec at all** (not "default codec"), and **present codec = at least one of encoder/decoder, never both absent**. So codec isn't "computed by default / intrinsic" as I'd claimed — my intrinsic/extrinsic convergence was wrong. Fixed across the template.

**Corrected model:** every facet is opt-in — no aspect is computed-by-default. A type with no `codec` aspect gets **no codec**, full stop. Codec sides are selectors with a floor: a present codec generates exactly the sides present; an omitted side is *suppressed*, not defaulted; ≥1 side is required.

**A nice thing that fell out:** the aspect-level marker now *derives* from field behavior instead of being stipulated —
- fields are **default-when-omitted** (rule/actor) → present-empty aspect = a fully-defaulted facet ≠ absent → `*`
- fields are **selectors** (codec sides; suppress-when-omitted, ≥1 required) → present-empty aspect = nothing ≡ absent → `*~`

So `*rule`/`*actor` vs `*~codec` isn't arbitrary — it's forced by whether the aspect's fields default or select. That's the real invariant, replacing the intrinsic/extrinsic story I had.

**The big implication — needs your confirmation before any code:** "absent codec = no codec" means **codec becomes *declared*, not *inferred*.** But the Generator we have does the opposite: ch.57 just built a structural gate (`ownElementNames.nonEmpty` + the `inheritedElementNames` walk) that *infers* a default codec for any eligible record with no codec aspect at all. Adopting this model would retire that gate and require a `codec` aspect on every type that currently auto-derives one. The upside is real — it kills the heuristic in favor of an honest declaration — but it reverses the direction of the last two increments, so it's a `DIVERGENCES` entry marked "confirm before implementing." Is that the direction you want, or would you rather keep inference as a default that an explicit `codec` aspect can override/suppress?

---

**[Dev]** I may be able to address your question by specifying under what conditions aspects can be inferred rather than explicitly declared. So, rule, actor and codec  never inferred, always explicit in both  in json and drake.  The draco aspect is always explicit in drake but can  be inferred  from drake into json, which can also infer the domain aspect, based on the data in TypeName.

**[Claude]** That resolves it cleanly — and it's a better answer than either pole of my question. The principle: **an aspect is inferable iff its json can be reconstructed from information already present; otherwise it must be explicit.** Encoded:

| aspect | authored where | json |
|---|---|---|
| `rule`, `actor`, `codec` | drake **and** json, explicit | never inferred — irreducible intent (logic / behavior / the decision to serialize and how) |
| `draco` | drake (the structure you write) | a projection of the drake authoring |
| `domain` | — | inferred from `TypeName` (package path → membership: `draco.base.Meters` → `Base`) |

**How it interlocks with presence:** the two principles are complementary — presence (absent ≠ present-empty where it carries meaning; `~` marks where it doesn't) and inference (explicit vs derived, per the table). And they meet cleanly: the inferable aspects (`draco`/`domain`) are always determinable, so their presence was never really a question; **presence-significance lives entirely in the explicit, opt-in aspects** (`rule`/`actor`/`codec`) — exactly where the `*` vs `*~` distinction sits. The aspect marker itself falls out of field behavior. Nothing here is stipulated; each layer forces the next.

**What this settles:** codec is declared, gate retired — confirmed direction, flagged as a separate future migration (retire `ownElementNames`/`inheritedElementNames`, add explicit `codec` to the types that auto-derive today). Not started. Still open, parked in `NOTES`: `discriminator`'s nature, and the `EncoderField`/`DecoderField` shape. Have we converged enough to turn to those, or is there more to push on in the presence/inference model?

---

**[Dev]** Whatever is still open should stay open until Generator becomes a domain type-parameterized by target language with operation rules for each target. Let's decide what needs to happen (if anything) with the open issues  for the current procedural version of Generator. I won't need custom override encoders or decoders for a while so the encoder/decoder element types can be deferred, at least. I'll take your preference on discriminator for now, unless it impacts an immediate concern.

**[Claude]** That settles the scope cleanly: the deep model is **Generator[L]'s** to implement (typed, per-target operation rules); the procedural `draco.Generator` stays put. **Decision: nothing needs to happen to the procedural `draco.Generator`.** Walking the open items:

| open item | disposition for the procedural Generator |
|---|---|
| Presence model (`*`/`~`, preserve vs elide) | **Deferred to Generator[L].** Current uniform elision stays. |
| Codec declared, gate retired | **Future migration**, scoped separately. The ch.57 gate keeps working and is correct for now. |
| `encoder`/`decoder` subelement types | **Deferred** (you confirmed — no custom overrides needed). Default both-sided codec keeps deriving. |
| `discriminator` nature | **Decided: keep it a field.** Per the inference principle it's a free tag choice, not reconstructable from anything, so it *must* be explicit — a String field with default `"kind"` is the honest form. It already is exactly that, so **zero code change.** |

So the procedural Generator + the JSON corpus are coherent and complete as they stand (195/195). `drake.dlt` now carries the target model for Generator[L], with `NOTES`/`DIVERGENCES` marking every deep item "deferred to Generator[L], not pending on the procedural Generator" — the boundary between what's built and what's designed is explicit for future-you.

*(Wrote journal chapter 58 — including the two framings of mine the Dev's corrections discarded — and a new `project_presence_inference_model` memory. No tests run; the only artifact touched this whole arc was `drake.dlt`, a hand-authored design surface with no parser — [[feedback_drake_is_syntax_discovery]].)*

---

**[Dev]** So for the next new session can we start making drake versions of all the existing and testable working json definitions in preparations for a drake parser and json generator?

**[Claude]** That's a sound plan — and a good fit for a fresh session (mechanical, per-file, corpus-building). The value is a **round-trip test corpus**: hand-authored `.drake` for each working JSON gives the future parser (`.drake` → JSON) something to reproduce, and the JSON→drake generator its inverse target — `JSON → .drake → JSON` as identity, mirroring what `DracoGenTest` does for JSON→Scala.

Inventoried the gap now so the next session starts with a concrete worklist: 62 JSON defs, 23 already have `.drake`, so the worklist is **39 types** — draco core (23: ActorType, Assembly, Binding, CLI, ContentSink, Dictionary, Domain, DomainDictionary, DomainTransform, DomainType, DracoType, Holon, Main, Primal, REPL, Rule, RuleType, SourceContent, Test, Type, TypeDictionary, TypeTransform, Value), base (10), format (1), primes (5, including the two rules).

The one thing to keep straight: concrete `.drake` files map to the **current, working JSON** — not the presence/inference model. The `*`/`~` markers in `drake.dlt` are *template notation*; the presence/inference semantics are a *parser* concern for Generator[L], not something the concrete corpus carries. Hand-authoring doubles as syntax discovery: each new `.drake` tests whether `drake.dlt` actually covers that type's shape. Saved as `project_drake_corpus_buildout` memory so a fresh session picks it up cold.

---

**[Dev]** Let's do a commit/push with the usual ancillary tasks supporting a clean start in a new session.

**[Claude]** All changes are this session's coherent work, and the suite is green — the last full `sbt test` was **195/195** (after the gate-relaxation narrowing), and nothing compilable has changed since (only `drake.dlt`, journals, and memory). So the push gate is satisfied without a redundant re-run. Ancillary tasks already done: journal chapters 55–58 written, memory updated.

*(Staged 38 files, committed, and pushed `4d98de5..3c71f8f` on `main`: CodecAspect + discriminator consumption, TypeDefinition codec derivation, YAML retirement, the drake.dlt presence/inference model, journal chapters 55–58. +525/−767, tree clean.)*

One note: I ran `git commit`/`git push` here because you asked directly — your `DRACO.md` normally reserves those for your IDE, so flag me off if you'd rather keep doing them yourself. **Next session** is primed: open it with "continue the drake corpus buildout" and the memory hands over the 39-file worklist cold.

---

**[Dev]** Can you write an ex post facto git-record for draco-git-record, if you haven't already done it?

**[Claude]** *(after reading the existing `draco-git-record/` format — each record is a shell-script reproduction of the commit)* Done — `draco-git-record/git-record-2026-07-08-1206` written, matching the existing format (the `cd` / `git add -A` / `git commit -F - <<'EOF'` / `git push` shell reproduction with the exact commit message).

Since it documents commit `3c71f8f` which is already pushed, it can't live in that commit — it's currently **untracked** and will naturally fold into the next commit, which is how these records accumulate. I left it uncommitted rather than making a separate push, since it's your audit trail — say the word if you'd like me to commit it on its own instead.

---

**[Dev]** Go ahead and commit and note that if you're using git commands directly, to create the record before you execute the commands in the future]]]

**[Claude]** Two things: commit the record, and internalize the rule (record first, then git) — applied immediately: I wrote this commit's own record *before* committing. Key catch from memory: the established workflow is that *you* create the git-record, and I only provide the command block. Since this session you had me run git directly, the rule you're giving is the correct adaptation: when I run git myself, I create the record first.

*(Wrote `git-record-2026-07-08-1228` before running git; committed and pushed `3c71f8f..c557e6e` — both record files in the repo, tree clean. Updated `reference_draco_git_record`: **mode 1** = Claude runs git directly → Claude writes the record first; **mode 2** = Claude hands Dev a paste-able block → Dev creates the record.)*

Worth flagging: memory also reminded me that your *established* workflow is mode 2 — `DRACO.md` even says "don't run git." Both modes are now captured, so next time I'll match whichever you signal — and if you'd prefer I go back to just handing you the command block, say so and I'll default to that.

---

**[Dev]** I'm ok with you continuing with the git commands and git-record entries each time before a new session.

**[Claude]** Noted and recorded as a standing authorization. Going forward, at each session-close I'll: (1) verify the push gate (full suite green), (2) **write the `git-record-YYYY-MM-DD-HHMM` file first**, (3) `git add -A` / `git commit` / `git push` on `main` — no need to ask each time. This supersedes the `DRACO.md` "don't run git" line for you, and it's noted in `reference_draco_git_record` so future sessions start with it.

Everything's clean and pushed (`c557e6e`), and the next session is primed for the drake corpus buildout. Good place to close.

## Status
No code change to the procedural `draco.Generator` — it and the JSON corpus are coherent at **195/195**. `drake.dlt` carries the full target model for the typed **Generator[L]** (presence markers, inference table, codec selector semantics, `EncoderField`/`DecoderField` sketch), every deep item marked deferred — see [[project_generator_evolution]] / [[project_presence_inference_model]]. `discriminator` stays a field (default `"kind"`), zero change. Session pushed as `3c71f8f` + `c557e6e` on `main`; git-record protocol settled (record before commands, standing authorization). Queued for the next session: the drake corpus buildout — 39 `.drake` files against the working JSON ([[project_drake_corpus_buildout]]).
