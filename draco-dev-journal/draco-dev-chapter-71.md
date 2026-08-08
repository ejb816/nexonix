# Draco Dev Journal — Chapter 71

**Session date:** August 5–6, 2026
**Topic:** The drake parser session — #44 picked up at last: `parseDrake` written as recursive descent directly inverting the emitter, design-validated by a Python mirror against the corpus before Dev ever compiled (45/45 plain types, then the full 72) → the 123-failure first run traced to a single cause: the project's own DelayedInit rule broken by its own enforcer (three keyword sets as plain `val`s in an `App` companion) → the round-trip's justifying find — the `domain` line silently dropped `typeParameters`, and `Format(F)`'s `F` is load-bearing (`Domain[Format[_]]` is generated from it); Claude deleted it as stray data before checking the consumer, the error recorded → four follow-ups filed (#52 brackets, #53 increment 2, #54 reference packages, #55 legacy `Seq()`) → #52 discussed and implemented same-session: the bracket was on the wrong node — moved from the container to the opener, the indentation tie-break deleted, drake whitespace-insignificant in fact rather than aspiration; #56 filed for the `from` sequence → the `Drake`/`Expression`/`DrakeCLI` split out of `Generator.scala`, taken on a measured coupling of zero references outward and exactly three inward — the three being expression-tree accessors that belong to neither projection → the self-hosting detour Dev opens: `draco-gen` is not natively defined, `DrakeCLI` is the third definition-less CLI, and the one construct blocking drake from expressing its own toolchain is `match` (43 sites, measured by flavour) → Dev's two placements: match-case belongs in the actor aspect's `start`/`message` (a `RuleAspect` already *is* one case; `signal`'s hardcoded `PostStop` match the cheapest increment) and, for pure functions, `Pattern` cases in a `dyn` body — plus `Generator(L <: Source)`, not `Generator(Source)`, because the argument must be a domain but the bound needn't → #57/#58/#59 filed, #52 closed, `priority-next` moved to #53 → the one honest commit: the rename's shadow collision caught by the gate exactly as the gate was for, 478/478, `70d94a8` pushed, the next-session prompt handed over. Transcript.

---

**[Dev]** From the previous session:

All three increments landed in order and are pushed. But the journal commit repeated the pattern:
`6abaa43` contains only the two journal files, yet carries "JSON corpus made uniformly implicit: strip 27 redundant sole-[DracoType] derivations (#50 item 1)" — the third commit with that message. Unlike `b93e3a1`, this one is unpushed (`ahead 1`), so it's safe to fix:

```bash
git commit --amend -m "draco-dev-journal: chapter 68 update + intro sync"

```

Setting the pickup point and correcting memory, which currently states that `draco.rete` was rolled back:
Everything's landed. State of play:
Commits — all three increments pushed in order (`17bcbb5` rete, `e876509` Action, `4c6e67c` Draco actor-ness). The journal commit `6abaa43` is the one loose end: it carries the strip message for the third time, but it's unpushed, so the amend above fixes it cleanly. That's twice now that message has been reused — worth watching for whatever in the IDE flow is pre-filling it.
Next session's pickup — #44 now holds `priority-next` and is the only issue with it. The parser is genuinely unblocked: codec emission is moot (zero definitions carry a `codecAspect`), the 39 missing mods `.drake` files don't matter because the round-trip gate generates drake from JSON, and the two remaining exclusions don't touch it either.
Memory corrected — `project_draco_rete.md` said the domain was rolled back and doesn't exist, which would have actively misled the next session. It now leads with the fact that `draco.rete` exists as wrap types, and keeps the rolled-back config attempt below it with the WHAT/HOW distinction spelled out, since the shared name is exactly what makes those two easy to confuse.
#51 recorded and frozen — new memory capturing what's settled (atomic terms, `TypeParameter`, the two-layer stratification, constitutive-vs-incidental), what's reserved (the primitive wrap types, and why — the operator/name boundary), and the priority reasoning for freezing it. The next concrete increment is written down so it's resumable without re-deriving.
One thing I'd flag for the parser work: the round-trip will be the first thing to run the emitter over all 131 definitions rather than the ~90 currently exercised, and `BodyElement`/`ActorAspect` have never been emitted at all. Expect it to surface emitter gaps early — that's the increment doing its job, not a setback.

**[Claude]** *(Checked git status and the priority-next label.)* State check:

- The amend landed — `51392cf` now reads "draco-dev-journal: chapter 68 update + intro sync". It's the one unpushed commit (`ahead 1`), ready to push from the IDE whenever you like.
- Pickup point is #44 — drake→JSON parser, close the JSON→drake→JSON round-trip — still the only `priority-next`.

Say the word and I'll start on #44. The first increment I'd propose is small: stand up the round-trip test harness over the existing emitter output before writing any parser, so the emitter gaps (`BodyElement`, `ActorAspect` — never emitted) surface as a concrete worklist rather than as parser failures later.

---

**[Dev]** Please start on #44

**[Claude]** *(Read #44, `DrakeGenTest.scala`, the drake emitter in `src/mods/scala/draco/Generator.scala`, `drake.dlt`, and the corpus — surveying which files carry rule/actor aspects, the expression-tree `value`s, the `parameters` shapes, `Action.drake`/`Dictionary.drake`/`TypeLoader.drake`/`Main.drake`/`ContentSink.drake`, and `TypeDefinition`/`TypeElement` internals; then wrote the parser into `Generator.scala`, created `DrakeParseTest.scala`, and built a Python mirror of the lexer/parser rules to validate against the JSON corpus before compile.)* Now I'll add the parser, right after the emission section it inverts.

Let me validate the design against the real corpus before handing it to you to compile — a Python mirror of the same lexer/parser rules, checked against the JSON.

Two real findings. Fixing the first — a package path word can collide with a keyword (`extensible org apache pekko actor typed …`):

45 of 45 plain types round-trip. Now updating the test's documentation to name the third loss the mirror surfaced:

Increment 1 is written. Both files are ready for you to compile and run.

**What landed**

- `Generator.scala:579` — a `Drake parsing` section directly after the emitter it inverts, ending in `Generator.parseDrake(source): TypeDefinition`. Recursive descent over a token stream, bounded by drake's reserved keywords rather than by line ends. Rule/actor aspects and value-position applications are rejected loudly, the same way `drake` rejects the codec aspect.
- `DrakeParseTest.scala` — two gates plus two report-only tests.

**The gates**

1. `drake(parseDrake(text)) == text` — exact, per plain-type `.drake`.
2. `parseDrake(drake(td)) == td` — per plain-type JSON, over everything the surface actually carries.

I validated the design against the real corpus before handing it over (a Python mirror of the same lexical and grammatical rules, checked against the JSON, since you own the compiles): **45 of 45 plain types round-trip.**

**Three findings, all real, none a parser bug**

The drake surface is not yet information-complete, in three separable ways. Gate 2 normalizes each away and the third test counts them, so the tail is a number:

- *Expression form.* `kvMap.iterator` is a JSON string; `draco.rete.RhsContext -> Unit` is a `->` tree. Identical on the surface, so no parser can tell them apart. Closes as the corpus converts to trees.
- *Empty-collection spelling.* `drakeDefault` collapses `Seq.empty` and legacy `Seq()` onto `[]`. Three fields, in `DomainDictionary.json` and `TypeName.json` — converting those to the tree form takes this class to zero, but it changes the generated Scala from `Seq()` to `Seq.empty`, so it needs its own increment.
- *Reference packages.* `drakeTypeRef` spells `from`/`modules` references bare, so `namePackage` can't come back. `domain`, `super` and `extensible` do carry theirs.

**One thing that needs your decision, not mine**

The bracket convention doesn't close the grammar. `CLI.drake` has

```text
    dyn drake Unit
      parameters
        par path String
      mon println(...)
    fix commands Map(String, [String] => Unit) Map(...)
```

Nothing in the keyword/bracket grammar says whether `fix commands` is that dyn's statement or the next member of `globals` — the `[ ]` sit on `globals`, so they don't separate the two. I resolved it with **indentation**, confined to one function (`drakeStatements`) and flagged there, because that's the tie-break the emitter's output actually supports today. But it means drake is not yet whitespace-insignificant as `drake.dlt` claims. The grammar-level fix is brackets on a dyn's own statement list — a surface change across the corpus, so I've left it alone.

A second, smaller one I did fix: a package word can *be* a keyword — `extensible org apache pekko actor typed ExtensibleBehavior(T)`. Qualified references are now bounded by case (lower-case words are the package, the first upper-case word is the reference) rather than by the reserved set.

```bash
sbt "testOnly draco.DrakeParseTest"
```

Once that's green I'll file the follow-ups as issues — the bracket decision, the three legacy spellings, and increment 2 (rule/actor aspects and value-position applications, which is where the `parameters`/`par` application form actually occurs).

*(Delegated action — Dev ran `sbt "testOnly draco.DrakeParseTest"`: 144 tests run, 21 succeeded, **123 FAILED** — every failure the same `NullPointerException`, `drakeReserved()` returning null at `Generator.scala:668`; the scope report itself printed correctly: 72 plain types in, 8 role-aspect held back.)* *(Fixed the keyword `val`s; extended the mirror to the full resource tree; found `format/Value.json`'s domainAspect `typeParameters`, deleted it, checked what consumes it, reverted, and fixed the emitter's `domain` line instead; implemented the value-position application for `primes/Primes.drake`; re-verified the corpus with the mirror; `format/Value.drake` gains `(F)`.)* The three keyword sets are plain `val`s in an `App` object — the DelayedInit trap. That's my error; the project rule is every val in an `extends App` companion must be `lazy`.

Now extending the mirror to the full tree — the walk covers subdirectories, so 72 plain types are in scope, not the 45 I checked:

Found it. `format/Value.json` carries `typeParameters: ["F"]` on its **domainAspect** — the type's own parameter leaked onto the domain reference. The emitter's `domain` line drops type parameters, so the drake surface never showed it and `DrakeGenTest` couldn't see it. It's the only one in the corpus:

Now the value-position application, which `primes/Primes.drake` actually uses:

Mirroring both changes to verify against the corpus:

That was wrong — `Format` is a parameterized domain and the generated Scala reads `Domain[Format[_]]` from exactly that field. Reverting:

Only one case, and the fix is unambiguous — the `domain` line should spell its reference the way `from`/`modules`/`extensible` already do:

Both fixed and verified against all 70 in-scope types. Ready for another run:

```bash
sbt test
```

**What changed since the failed run**

1. **The real bug** — `drakeMemberKeywords`, `drakeStatementKeywords` and `drakeReserved` were plain `val`s in an `App` companion, so `DelayedInit` left them null until `main()`. Now `lazy val`. That single mistake accounted for all 123 failures.

2. Running the mirror over the **full** tree (the walk covers subdirectories — 72 plain types in scope, not the 45 I'd checked) turned up two more, both now handled:

   - **`primes/Primes.drake`** uses a value-position application: `fix knowledge … Rule.knowledgeService.newKnowledge parameters par "Primes"`. I'd deferred that form; it's in scope after all, so it's implemented — `drakeApply`'s inverse. Arguments are read as positional; the named form (`par <name> <value>`) is genuinely indistinguishable from a two-word positional expression, so it waits for the increment where it occurs, and nothing is guessed — a named argument reaching this path fails the round-trip loudly rather than being silently flattened.

   - **A fourth surface loss, and this one was load-bearing.** `format/Value.json` carries `typeParameters: ["F"]` on its *domainAspect*, and the `domain` line emitted only `namePackage :+ name` — so the parameters vanished. I first read that field as stray data and deleted it; it isn't. `Format` is a parameterized domain and the generated Scala reads `Domain[Format[_]]` from precisely that field, so dropping it would have broken compilation. Reverted, and fixed on the emitter side instead: the `domain` line now spells its reference with `drakeTypeRef`, the way `from`/`modules`/`extensible` already do. `format/Value.drake` gains `(F)` to match. I applied the same change to `super` for consistency — no corpus case today, so it's byte-neutral, but it's the identical latent loss.

That last one is the round-trip doing the job it was built for: a loss the one-directional emitter test structurally could not see, because it only ever compared what the emitter chose to spell.

`sbt test` rather than the scoped run — the emitter changed, so `DrakeGenTest` and `DracoGenTest` are both in the blast radius.

*(Delegated action — Dev ran `sbt test`: all tests passed.)* *(Read the loss report in `target/test-output/DrakeParseTest.log`, fixed the report test's value comparison, filed four issues, commented #44, wrote `project_drake_parser.md`, updated MEMORY.md.)* Full suite green. One thing in the report test is misleading though — let me look:

Fixed the report's categorization — a `value` must be compared whole, since the entire point is that the same expression is a string on one side and a tree on the other; descending scattered one difference across the tree's leaves (that's the bogus "OTHER — unaccounted: 47").

Now filing the follow-ups, as I said I would once it was green:

Increment 1 of #44 is done and the full suite is green.

**What's in**

`Generator.parseDrake` in `Generator.scala:579`, gated by `DrakeParseTest.scala` over all 72 plain types — surface round-trip exact, JSON round-trip over everything the surface carries. Eight definitions carrying a rule or actor aspect are held back and named by the report test.

**The finding that justified the whole exercise**

The `domain` line emitted `namePackage :+ name`, silently dropping type parameters. `format/Value.json`'s domain reference is `Format(F)`, and the Scala projection emits `Domain[Format[_]]` from exactly those parameters — so the drake projection could not reconstruct a definition that generates the same Scala. `DrakeGenTest` structurally could not see this: it only ever compares what the emitter chose to spell. Fixed by spelling the reference with `drakeTypeRef`, the way `from`/`modules`/`extensible` already do.

Worth recording that I got it wrong first: I read that field as stray data and deleted it before checking what consumes it. It's load-bearing.

**Follow-ups filed**

- **#52** — the bracket convention doesn't close the grammar. Nothing says whether a `fix` after a Unit dyn is that dyn's statement or the next member; the brackets sit on the container. Indentation is the tie-break for now, confined to one function. This is the language decision I'm leaving to you.
- **#53** — increment 2: rule and actor aspects, named arguments, call chains.
- **#54** — `from`/`modules` references lose their package (65 fields). May already be answered by `TypeLoader.rooted`, in which case the fix is to stop storing the package rather than start spelling it.
- **#55** — three legacy `Seq()` values; converting them changes the generated Scala, so it needs its own increment.

**One more run when convenient**

I corrected the report test's categorization after your green run — a `value` has to be compared whole, since the point is that the same expression is a string on one side and a tree on the other; descending into it scattered one difference across the tree's leaves and produced a bogus "OTHER — unaccounted: 47". The gates were unaffected, but the measured tail was misleading:

```bash
sbt "testOnly draco.DrakeParseTest"
```

`priority-next` is still on #44. If you want it moved to #53 for the next session, say the word — but #52 arguably comes first, since increment 2 brings more dyn-with-body cases into exactly the ambiguous position.

*(Delegated action — Dev ran the scoped test: 144 run, 144 succeeded, all passed.)*

---

**[Dev]** Proceed with #52 by discussing the approach to closing it.

**[Claude]** *(Read the corrected loss report; censused the corpus for every dyn-with-body and what follows it.)* Report's clean now: 72 fields total = 65 reference-package + 4 expression-form + 3 empty-collection, nothing unaccounted. Let me measure the corpus impact before recommending anything on #52:

First, a correction to what I told you when I filed #52 — I implied the ambiguity was broad. It isn't. Two things narrow it sharply:

- A `=` result line terminates a dyn, so only **Unit** dyns can bite.
- `dyn` is not a statement keyword, so a following sibling `dyn` is never ambiguous — which is what most of the corpus actually does.

Across main and mods, exactly **one** site bites: `CLI.drake`'s `dyn drake Unit` followed by `fix commands`. Everything else is already unambiguous.

**The diagnosis**

drake.dlt puts the bracket on the *container* — `elements [ … ]` — which marks where the container ends. But the overlap it needs to resolve is between the **opener's last sub-block** and the container's next member. The bracket is on the wrong node. That's why the corpus can be fully bracketed per the current rule and still be ambiguous.

Note also that the container rule is *conditional*: you bracket `elements` only if some member happens to open a block. Authoring that requires look-ahead.

**What I'd do: move the bracket from the container to the opener**

Three rules, each local:

- name lists (`modules`, `types`) — always bracketed; their members carry no keyword, so nothing else can bound them
- keyword-member blocks (`elements`, `parameters`, `body`, `globals`, `variables`, `conditions`) — never bracketed; a reserved keyword bounds every member
- block-opening members (a dyn-with-body) — bracket their own body

```text
  globals
    dyn version Unit [
      mon println("Draco 2.0.0-alpha.1")
    ]
    fix commands Map(String, [String] => Unit) Map(...)
```

`parameters` inside a dyn needs no brackets either: its members are all `par`, and a statement list can never contain `par`, so it self-terminates.

This closes the grammar completely — the indentation tie-break in `drakeStatements` gets deleted and drake becomes whitespace-insignificant as claimed, not as aspired to. The emitter also loses its `nesting = members.exists(opensBlock)` detection.

**The cost, honestly**

11 container bracket pairs come out, 22 opener pairs go in, across about 10 files — so net **+11 pairs**, and `CLI.drake` goes from 1 pair to 7. More visual brackets, in exchange for an unconditional rule instead of a look-ahead one. Both existing gates catch every mistake mechanically, so the sweep itself is low-risk.

**What I'm rejecting**

Simply accepting indentation and correcting drake.dlt is zero churn and the parser already does it. I don't recommend it: whitespace-insignificance is load-bearing for the two-layer surface grammar drake.dlt sketches (`expr := app (opsym app)*`), and a carve-out for statement attachment is the kind of exception that spreads. #53 also widens the hole — rule and actor action bodies are `fix`/`mon` lists that can contain dyns, so the one biting site becomes several.

Accept, or tell me where you'd rather land — in particular if the bracket noise on dyns reads as too high a price for one site today.

---

**[Dev]** We need to see how full parsing works, so I'm inclined to accept your current proposal. There is still an open question about a sequence  after "from <TypeName>" that is part of "from" (that would gene rate a "with" sequence in Scala) but that can be a follow up issue.

**[Claude]** *(Censused multi-element derivations, filed #56; edited the emitter and parser in `Generator.scala`, wrote and applied `rebracket.py` to sweep the corpus, updated the Python mirror and re-verified, rewrote drake.dlt's BRACKETS/DIVERGENCES/NOTATION sections.)* Accepted — implementing the bracket move. And you're right that the `from` sequence is a separate question; let me capture it first while it's precise.

Now the emitter — brackets move from the container to the opener:

Now the parser — the indentation tie-break comes out entirely:

Now the corpus sweep — a purely mechanical bracket move, verified by re-parsing:

Now updating the mirror to the new grammar and re-verifying the whole corpus:

70 of 70 green under the new grammar. Now drake.dlt, which states the old rule:

#52 is implemented end to end. Ready for a run:

```bash
sbt test
```

**What changed**

- **Emitter** — `drakeSection` never brackets; `drakeElement` wraps a dyn-with-body in `[ ]`. The `nesting = members.exists(opensBlock)` look-ahead is gone.
- **Parser** — the indentation tie-break is deleted, and with it the column tracking: `DrakeToken` no longer records one and the lexer no longer tracks lines. A bare block is now bounded by the member keywords it *admits* (`parameters` takes only `par`, so a `loc` after it is the enclosing dyn's first statement), and a dyn body is bounded by its own brackets. Leaf-vs-block dyn is now just "is the next token `[`".
- **Corpus** — 8 files swept mechanically: 11 container bracket pairs out, 22 opener pairs in. Indentation untouched, so the result is exactly what the revised emitter produces. No mods `.drake` has a dyn-with-body, so none needed touching.
- **drake.dlt** — the BRACKETS section now states the three local rules, with the `dyn drake` / `fix commands` case written out as the worked example of why the container is the wrong node. The DIVERGENCES row that carried this as a target is now an implemented row, and NOTATION no longer claims indentation is structural — it isn't, in fact rather than in aspiration.

I re-verified the round-trip against the rewritten corpus before handing it over: 70 of 70 still parse to their JSON under the new grammar.

`ContentSink` is the case worth eyeballing, since it's the nested one:

```text
  factory
    parameters
      par sinkRoot URI
      par logicalPath String
    body
      loc sinkPath java.nio.file.Path java.nio.file.Paths.get(...)
      dyn write Unit [
        parameters
          par content String
        mon java.nio.file.Files.createDirectories(sinkPath.getParent)
        mon java.nio.file.Files.write(sinkPath, content.getBytes(...))
      ]
domain draco Draco
```

**Also filed #56** for the `from` sequence. 22 of 129 definitions carry two derivations — `Dictionary` (`Map`, `DracoType`), the three `base` unit types, and 18 in mods. The sequence works today because it's terminated by the next reserved keyword, which only holds because section keywords are lower-case and references upper-case — a convention drake.dlt doesn't state. I linked it to #54, since if a `from` reference ever gains a package, those lower-case package words stop being distinguishable from the sequence's own structure; the two want settling together.

---

**[Dev]** Are we ready to relocate the drake emitter and parser to src/mods/scala/draco/{Drake,DrakeCLI}.scala?

**[Claude]** Good question to ask before doing it. Let me check the actual coupling rather than guess. *(Read the existing `draco.drake.Drake` definition; measured cross-boundary references between the drake sections and the rest of `Generator.scala`.)* The regex caught too many locals. Let me check the specific cross-references:

Let me answer from the actual coupling rather than impression.

**The measurement**

The two drake sections are lines 167–941 — 775 of 2438 lines, ~32% of the file. Cross-boundary references, in full:

- **drake → the rest of `Generator`: none.** The mentions of `expression()` in that range are all doc comments. `splitTypeArguments` and `splitTopArrow` look shared but aren't — every caller is drake-side.
- **the rest of `Generator` → drake: three.** `isApplication`, `appOperands` and `namedArg` are defined in the drake emission section but called at `Generator.scala:140-145`, inside the *Scala* expression renderer.

Those three aren't drake's. They're expression-tree accessors over `TypeElement.value` — "does this node apply `()`", "what are its operands", "is this a named argument". Both projections need them; they got filed under drake because drake was written second. So the boundary isn't entangled, it's **mislabelled in exactly one place**, which is a good sign for the move.

**On the naming — no objection**

`draco.Drake` alongside the existing `draco.drake.Drake` is precisely the pattern already in place: `draco.Generator` (imperative, mods) beside `draco.generator.Generator[L]` (typed, main). `draco.drake.Drake.generator` already delegates to `Generator.drake`, so that seam just repoints. This isn't the `loadType` self-shadow — that one was a tool owning a capability that belonged to the model; this is a tool named after the domain it serves, which is the convention.

**My recommendation: yes, move now — before #53, not after**

I'd have argued the other way if the coupling were messier. Given three references, the case for moving first is stronger:

- #53 adds rule/actor parsing, named arguments and chain folding — call it another 200 lines. Better landing in a fresh ~800-line `Drake.scala` than pushing `Generator.scala` toward 2700.
- Every one of #53's additions is drake-internal, so the boundary won't move under it.
- The move is mechanical and both round-trip gates catch any slip.

One prerequisite, and it's the interesting part: the three shared accessors need a home that isn't either projection. I'd put them in a small `draco.Expression` in mods, used by both `Generator` and `Drake`. Leaving them in `Generator` and having `Drake` reach back for them would invert the dependency for no reason — drake would depend on the Scala projection to read the model's own trees.

**On `DrakeCLI` — I think you're right, and for a reason worth stating**

I did two corpus-wide operations in Python this session: the parser mirror (throwaway, pre-compile verification) and `rebracket.py`, which **modified 8 files in the repo**. The first is defensible as scratchpad; the second is corpus tooling written in the wrong language, and there was no Scala home to put it in. `DrakeCLI` is that home — bulk `.drake` ↔ `.json` operations, sweeps, and re-emission, alongside `bin/draco-gen`'s existing subcommands.

If that matches what you had in mind, say go and I'll do it in three steps: extract `draco.Expression`, move the emitter and parser to `Drake.scala`, then `DrakeCLI.scala`. If `DrakeCLI` was meant to be something else, tell me what and I'll fit it.

---

**[Dev]** Perfect match. Proceed.

**[Claude]** *(Confirmed the boundaries and callers; created `Expression.scala`; extracted the two drake sections into `Drake.scala`, renaming throughout; fixed the moved comments; repointed `draco.drake.Drake.generator` through the JSON first; created `DrakeCLI.scala`.)* Boundaries confirmed. Step 1 — the shared accessors:

Step 2 — extracting the drake sections into `Drake.scala`, dropping the redundant prefix now that they have their own namespace:

The moved comments reference "the projection above", which no longer exists — and one still describes the pre-#52 bracket rule. Fixing those:

Now repointing the callers — the `Drake.drake` definition first, since JSON is the source of truth:

Step 3 — `DrakeCLI`, the home the bracket sweep should have had:

Done. Ready for a run:

```bash
sbt test
```

**What moved**

| | lines | holds |
|---|---|---|
| `Drake.scala` | 777 | the drake projection — `emit`, `parse`, and their machinery |
| `Expression.scala` | 34 | `isApplication` / `operands` / `namedArgument` — tree shape, neither projection's |
| `DrakeCLI.scala` | 146 | `emit` / `parse` / `check` over a resource tree |
| `Generator.scala` | 1663 | was 2438; keeps the Scala projection |

**Names**

With their own namespace the `drake` prefix was pure noise, so it's gone: `Generator.drake` → `Drake.emit`, `Generator.parseDrake` → `Drake.parse`, `drakeExpression` → `Drake.expression`, `drakeSection` → `sectionLines`, and so on throughout. Two renames went slightly further than mechanical — `drakeValueType` → `typeExpression` and `drakeValueTypeSlot` → `typeExpressionSlot`, because `valueType` was already the name of the *field* they operate on and the collision read badly.

`draco.drake.Drake.generator` now delegates to `draco.Drake.emit`, **fully qualified** — inside `package draco.drake` the local `Drake` shadows the package-root one. That change went through the JSON first, as it should, so `Drake.json` / `Drake.drake` / the generated `Drake.scala` all carry it and `DracoGenTest` gates the regeneration.

`Expression` sits next to the existing `ExpressionRenderer` in `SourceContract.scala` — shape questions in one, spelling in the other. Worth knowing they're neighbours when the `Source` prototype advances.

**On `DrakeCLI`**

`emit --write` is the point. The corpus is emitter-canonical, so a surface change is never a text transformation over `.drake` files — it's a change to `Drake.emit` followed by re-emission. #52 got applied as an ad-hoc Python rewrite for want of exactly this, and I've said so in the file header so the next one doesn't repeat it. It defaults to reporting and exits 5 on drift; writing takes the explicit flag.

`check` round-trips both directions and treats an aspect `Drake` doesn't yet cover as a *skip*, not a failure — so it stays useful through #53 rather than going red the moment it meets a rule.

No `bin/draco-drake` wrapper yet; it's reachable as `java -cp <assembly-jar> draco.DrakeCLI ...`. Say the word if you want the wrapper to match `bin/draco-gen`.

---

**[Dev]** How is draco-gen defined natively within draco framework?

**[Claude]** *(Read `bin/draco-gen`, `CLI.scala` and its drake, and `GeneratorCLI.scala`.)* Short answer: **it isn't.** `bin/draco-gen` is a bash wrapper that execs `draco.GeneratorCLI` — 292 lines of hand-written Scala in `src/mods` with no `.json` behind it. Nothing about it comes from a definition.

There *is* a native CLI, and it's real, but it's a different thing.

**What is native: `draco.CLI`**

`CLI.json` → `CLI.scala`, fully generated. Its `globals` are the commands:

```text
type CLI(L)
  globals
    dyn load TypeDefinition [ … ]          -- helper, not a command
    dyn version Unit [ … ]
    dyn inspect Unit [ … ]
    dyn generate Unit [ … ]
    dyn drake Unit [ … ]
    fix commands Map(String, [String] => Unit) Map("version" -> (_ => version), …)
    dyn main Unit [ … ]
domain draco Draco
```

That's the "commands as data" shape — dispatch is a declared `Map` from name to handler, not a `match`. It runs, and `generate`/`drake` route through the two projections.

**The gap**

| | `draco.CLI` (native) | `draco.GeneratorCLI` (hand-written) |
|---|---|---|
| commands | version, inspect, generate, drake | generate, generate-multi, compile, compile-multi, inspect, discover, verify |
| results | all `Unit` | six documented exit codes |
| arguments | `as.head`, by hand in each command | positional, variadic, and `--force` |
| safety | — | `discover` refuses dirty/untracked JSON |

So the native CLI is roughly the read-only print subset. Everything operational — compiling, writing files, refusing unsafe work, saying *how* it failed — lives on the hand-written side, because the definition model has no vocabulary for it:

1. **Argument shape.** `generate-multi <p>...` and `discover <json> [--force]` need declared arity and flags per command. Today every native command takes `Seq[String]` and destructures it in its own body.
2. **Result as meaning.** Exit codes are a result vocabulary; every native command returns `Unit`.
3. **Preconditions.** "refuses dirty JSON without `--force`" is a guard with no expression in the model.

Also worth knowing: the `L` in `CLI[L]` is unused in the body — a placeholder for the `Generator[L]` track. Its sibling `REPL.drake` is `type REPL(L)` and a domain line, nothing else.

**The debt I just added**

`DrakeCLI` followed `GeneratorCLI`'s precedent, which means it's the third hand-written CLI with no definition. That cuts against defining before hand-writing, and I should name it rather than let it pass.

**One concrete thing that would close part of it**

`CLI.json` currently authors both the `dyn`s *and* the `commands` map that indexes them — but the map is **derivable from the dyns**. A dyn already carries name, parameters, and result type, which is everything the adapter needs: zero parameters → `_ => version`, one → `as => inspect(as.head)`. Emitting `commands` from the declared globals instead of hand-authoring it alongside would remove the one place the definition repeats itself, and it needs no new vocabulary — only a rule for which dyns are commands (`main` and `load` aren't).

That's a small, self-contained increment. The arity/exit-code/precondition vocabulary is the larger question behind it, and that one is a design conversation rather than a change. Want me to file either?

---

**[Dev]** Is drake syntax sufficient to allow Drake and Generator (and the CLI for each) to be generated from definitions?

**[Claude]** *(Measured `match`/`while`/`var`/builder counts across the four files and the declared operator set.)* Not as they're written, no. But the gap is narrower and more specific than it looks, and it's worth separating two readings of "sufficient".

**Reading one: can drake *carry* them?** Yes, trivially — and uselessly. Every body can go in as a host-opaque string, exactly as `TypeLoader.rooted` already does with its 400-character one-liner. You'd get definitions that regenerate the identical Scala and a self-hosting claim that means nothing: it'd be Scala in a JSON envelope, dead for a Haskell target, and it would move ~2400 lines from measured-native to measured-opaque. Given that drake.dlt tracks `isString` as the host-opaque tail *precisely so it can be measured*, that's the metric moving hard the wrong way. Worth naming as the trap, because it's the easy path.

**Reading two: can drake express them natively?** Measured across the four files:

| | Drake | Generator | GeneratorCLI | DrakeCLI | Expression |
|---|---|---|---|---|---|
| `match` | 17 | 18 | 6 | 2 | 0 |
| — literal cases | 31 | 26 | 7 | 3 | 0 |
| — type-test cases | 10 | 37 | 1 | 1 | 0 |
| — destructuring cases | 5 | 13 | 10 | 7 | 0 |
| `while` | 12 | 0 | 0 | 0 | 0 |
| `var` | 14 | 0 | 0 | 2 | 0 |
| builders | 9 | 0 | 0 | 0 | 0 |

**The one blocking construct is pattern matching.** 43 sites, in every file, no encoding in the model at all. And within it, the three flavours are not equally hard:

- **Literal cases (67)** aren't really blocked. `CLI.commands` already demonstrates the encoding — dispatch as a declared `Map` from key to handler. Keyword dispatch in both engines is that shape.
- **Type-test cases (49)** are probably not blocked either. `TypeElement` already carries `kind` as a discriminator string, so `case _: Fixed => "fix"` is a `Map[String, String]` in disguise wherever the case bodies are uniform — which is most of `Generator`'s 37.
- **Destructuring (35)** has no encoding and no obvious one. `case Some((op, operands) :: Nil)` and `case "emit" :: root :: rest` are the same problem, and the second is why the CLI argv question from before collapses into this one.

**What surprised me in the measurement:** `Generator.scala` — all 1663 lines — has **zero** `while`, `var` and builders. It's already written functionally; its entire gap is `match`. All the imperative code is in `Drake`, and it's concentrated in the lexer and cursor I wrote a few turns ago. So iteration isn't a language gap either: `foldLeft` is already a native drake tree — `format/json/Value` proves it, carrying a real fold with a nested lambda — so a fold-based lexer is expressible today. That's a rewrite of ~200 lines, not new syntax.

The CLIs need everything above *plus* the arity/exit-code/precondition vocabulary from your last question, so they're strictly downstream.

**Bootstrap** is a real but separate concern: `Generator` generating `Generator` needs a checked-in bootstrap output to break the cycle. Mechanical, not syntactic.

**The smallest real step, if you want one**

`Expression.scala` is 34 lines, three pure functions, zero matches. It is **expressible natively today** — except for one thing: `&&` is not a declared operator. The set is `.`, `->`, `()`, `\`, `if`, `(,)`, `=`, `*`, `==`, `!=`, `||`. `||` is there; `&&` isn't, which reads like an oversight rather than a decision — it currently appears in ten-plus corpus files only inside opaque strings.

Add `&&`, and `Expression` becomes the first genuinely self-hosted, target-neutral piece of the toolchain. Small, verifiable, and it makes the claim honestly rather than by envelope.

---

**[Dev]** The most important, but not most urgent, concern for me is how to embed match case in the rule pattern. It has to be in the pattern or embedded in the actor before the rules are fired, so it shouldn't have to be in a rule action.

**[Claude]** That reframing is right, and I think it's stronger than "how do we encode `match`" — it says dispatch belongs on the discriminating side of the engine, and an action that re-discriminates has the engine inside out. Let me ground it in what the model actually carries. *(Read the primes rule definitions, `conditionFunctions`, and the pattern model; grepped for salience/negation.)*

Checking the model against that idea — it turns out you're not proposing a new construct. **A `RuleAspect` already *is* one `case`.**

```text
rule
  pattern
    variables
      var accumulator Accumulator     -- the typed binder:  case accumulator: Accumulator
      var i1 Int
      var i2 Int
      var i3 Int
    conditions
      con i1 * i2 == i3               -- the guard:          if i1 * i2 == i3
  action
    …                                 -- the consequent
```

That's `case (accumulator: Accumulator, i1: Int, i2: Int, i3: Int) if i1 * i2 == i3 => …`, written out. So the reframing that follows is: **a `match` is a rule set** — several rules over one scrutinee's fact types — and a rule set over shared Knowledge is what a rule domain already is. Nothing needs inventing to hold the cases.

**Three things are genuinely missing, in increasing difficulty.**

**1. The `con` line can't carry its bindings.** `Condition.parameters` is populated in the corpus and load-bearing — it names which pattern variables the condition constrains, and drives the `$`-prefixed references in the emitted `.where(...)`. `RemoveCompositeNumbers.json` carries `i1`, `i2`, `i3` with their types. But `Drake.emit` renders a `con` as its value alone and drops them. So the drake surface cannot currently express the pattern's binding vocabulary at all. That's small, concrete, and lands inside #53 — worth doing there regardless of where the larger design goes.

**2. Decomposition — your "embedded in the actor".** A `Variable` binds a whole fact *by type*. Conditions navigate (`accumulator.primeSet`) but never bind sub-parts. So `case Some((op, operands) :: Nil)` requires `op` and `operands` to already be facts — which is exactly your point, and exactly how the primes domain works. The gap isn't the pattern; it's that **nothing declares the decomposition**. The actor's `message` action does it in host code today. Making that a model element is what turns "before the rules fire" from a convention into something checkable.

**3. Ordering, and the catch-all.** This is the hard one, and it's semantic rather than syntactic. `case "" => …; case other => …` defines the second by the *failure* of the first. RETE is unordered and non-exclusive: every matching rule activates. The model has neither salience nor negation — I grepped, zero hits for both. So a fall-through case has no expression today.

There's a real prize buried in that, though. Once dispatch lives in the pattern, **disjointness becomes checkable at definition time.** Scala checks exhaustiveness only for sealed types and never checks overlap — it silently prefers the earlier case, which is how shadowed branches hide. A rule set whose conditions must be provably disjoint turns that into a detectable error. That's a reason to want this independent of self-hosting, and it argues for requiring disjointness over adding an ordered `otherwise`.

**The cost to be clear-eyed about:** point 3 forces every discrimination to name its scrutinee type, because a session's working memory is global where a `match` is local. Forty-three local matches can't share one session without cross-talk. That's arguably the point — it makes the discriminated thing nameable — but it's a real change in how the code is shaped, not a translation.

Want me to file this as a design issue? I'd capture the three gaps, the "a match is a rule set" framing, and the disjointness-over-ordering argument, linked to #53 — with the `con` bindings noted as the one piece that's already actionable.

---

**[Dev]** My current thinking is that the syntactic equivalent of match case belongs explicitly (and optionally) in the start and message actions of the actor aspect.

**[Claude]** *(Checked whether `Action.variables` is consumed by the Generator.)* Two things in the model are already waiting for this, and one of them changes my view on scope.

**`Action.variables` is a vacant slot.** It exists on `Action`, it's serialized, and nothing consumes it — the only place actions get variables is `actionBody(td.ruleAspect.action, td.ruleAspect.pattern.variables)`, which passes the *pattern's*. So the binder list is already declared where you want to put cases.

**`signal` already contains a match — hardcoded.** `actorBehavior` emits:

```scala
signal match {
  case org.apache.pekko.actor.typed.PostStop => …
}
```

one case, frozen, with a comment conceding "PostStop session cleanup is a later refinement."

**So I'd argue for `signal` too, and for doing it first.** It's the only place in the actor aspect where a match already exists; generalizing a hardcoded one-case match into an authored n-case one is a smaller step than introducing the construct fresh, and it's byte-identical for the current corpus, where every actor has exactly the one `PostStop` case. `start` and `message` then follow the same shape with no new semantics. Pekko signals are a sum type — `PostStop`, `Terminated`, `ChildFailed` — so this is also the place a real second case shows up first.

**Why your placement beats what I proposed last turn.** Both hard problems dissolve:

- *Ordering.* An actor action is procedural, so cases are ordered and first-match-wins by construction. No salience, no negation, no RETE conflict resolution. The one genuinely semantic gap I raised simply doesn't arise here.
- *Scope.* The match is local to the actor rather than global over working memory, so it doesn't force a distinct fact type per discrimination — which was the real cost of the rule-set framing, and I'd underweighted it.

**`Pattern` reuses verbatim**, which is the structural argument for the whole thing: one type, two consumers — unordered RETE conjunction in a rule LHS, ordered test against a scrutinee in an actor case. Worth writing into drake.dlt explicitly rather than leaving implicit, because it's the same syntax carrying two semantics, and that's the kind of thing that reads fine now and bites in a year. The concrete asymmetry: a rule pattern has **no scrutinee** — it quantifies over working memory — while an actor case has exactly one, the message or the construction arguments. That difference needs a stated home even if it's positional.

**One consequence to be clear about.** This doesn't reach the 43 matches in `Drake` and `Generator`. Those live in leaf functions — `expression` dispatching on operator symbol, `keyword` on element kind — not in actors. So this is an answer for *runtime message dispatch*, and a good one; it isn't an answer for self-hosting. If the engines are ever to be definition-backed, that still needs match in an ordinary `dyn` body, which is a separate question with the ordering problem back in play.

That's not an objection — the two problems just aren't the same one, and solving dispatch-in-actors on its own merits is worth doing.

Want me to file it? I'd capture: cases in `start`/`message`/`signal` reusing `Pattern`, `Action.variables` as the existing slot, `signal`-first as the cheapest increment because the match is already there hardcoded, the two-semantics caveat, and the explicit note that leaf-function match is out of scope.

---

**[Dev]** Why can't the pattern match in Generator be implemented in an actor defined  in the Generator(Source) domain? Given that Source contains all the target language neutral Generator functions and predicates. I'm still not sure if a language neutral Generator should be Generator(Source) or Generator(Drake), since the type argument generally has to be a domain.

**[Claude]** Let me look at what `Source`, `Generator`, and the two targets actually are right now, since the domain question turns on it. *(Read the `Source`/`ScalaTarget`/`Drake`/`Generator` definitions and the transform types' bounds.)*

Answering the second question first, because it settles the first.

**`Source` isn't an alternative to `Drake` — it's the bound.**

Your premise needs one correction: the type *argument* has to be a domain; the *bound* doesn't. The corpus already does exactly this:

```text
type DomainTransform(S <: DomainType, T <: DomainType)
type TypeTransform(S <: DracoType, T <: DracoType)
```

`DomainType` and `DracoType` are plain types in the Draco domain — not domains. The arguments substituted for `S`/`T` are. So the shape you want is:

```text
type Generator(L <: Source)      -- with Generator(Drake) and Generator(ScalaTarget) as the instances
```

`Generator(Source)` would be asking for a generator for *the supertype*, which isn't a projection at all.

**The corpus is already half-way there, asymmetrically:**

```text
type ScalaTarget from Source      -- declares it
type Drake                        -- does not
type Generator(L)                 -- L unbounded
```

`ScalaTarget` declares `from Source`; `Drake` doesn't, though it plays the identical role. And `Generator(L)` carries no bound, so nothing currently says `L` must be a source projection. Two small fixes — `Drake from Source`, and `L <: Source` — make the intent explicit and are independently worth doing.

Worth knowing: `Source` today is `type Source` and a domain line. Empty. The neutral engine you're describing — the language-invariant traversal plus the template slots — currently lives in `SourceContract.scala` in mods, explicitly labelled a prototype staging toward a `draco.Source` definition. So "Source contains all the neutral functions and predicates" is the target state, not the present one.

**Now: can the Generator's match be an actor in that domain?**

Yes — but only under one reading, and I don't think it's the reading you want.

The reading that works: since rule execution is decoupled from RETE (RETE as performance, not necessity), an actor-shaped *definition* needn't be actor-shaped *execution*. The target collapses it to a plain `match`. The actor is the specification. That's coherent, and it's the same argument that lets a rule be a rule without a session.

Three frictions, though:

1. **Actors receive; they don't return.** `expression(value): String` is a pure total function. An actor dispatch needs a reply channel that carries nothing meaningful — you'd be modelling the return value as a message to nowhere. The actor aspect models receive → effect; these matches are input → value. That mismatch is the sharpest one.
2. **Recursion.** `expression` descends into operands, thousands of nodes per generation. As real message passing that's a tree fold with continuations; only the collapsed-to-`match` reading makes it sane.
3. **Granularity.** An actor is the unit of identity and concurrency. A dispatcher on operator symbol has neither.

**What fits better, and it's the same machinery:** put the cases in a `dyn` body rather than an actor — `Pattern` reused exactly as in your actor proposal, ordered, first-match, but returning a value. No reply channel, no identity, no concurrency you didn't ask for.

That's the leaf-function match I said was out of scope for the actor placement last turn. This is the argument for adding it there instead — and it keeps the actor placement clean for what it's genuinely good at, message dispatch, rather than stretching it to carry pure functions. The actor version isn't wrong; it just bundles identity and concurrency with dispatch when you only want dispatch.

So: `Generator(L <: Source)`, the neutral engine as `Source`'s own elements, and its dispatch as `Pattern` cases in `dyn` bodies — with actors reserved for the message-driven side.

Want me to file the two corpus fixes (`Drake from Source`, `L <: Source`) as one small issue, and the `Pattern`-in-a-`dyn` design as another alongside the actor one?

---

**[Dev]** Yes, and then return to what the next priority issue is.

**[Claude]** *(Filed #57, #58, #59; closed #52; moved `priority-next` from #44 to #53.)* Filed, and the backlog is re-pointed.

**New issues**

- **#57** — `Drake from Source`, `Generator(L <: Source)`. Small, and it records *why*: the argument must be a domain, the bound needn't, with `DomainTransform(S <: DomainType)` as the precedent.
- **#58** — ordered `Pattern` cases in the actor aspect, `signal` first because the match is already there hardcoded.
- **#59** — `Pattern` cases in a `dyn` body, for pure functions, with the measured match counts and why the actor placement doesn't fit them.

**#52 is closed.** Reopen it if the commit doesn't land — the change is verified but still sitting in the working tree.

**Next priority: #53** — `parseDrake` increment 2. `priority-next` moved there from #44, which stays open as the umbrella.

The reason it's #53 rather than #57 or #58 is a dependency I found while examining the rule model for your actor question, and I've recorded it on the issue:

**`Drake.emit` drops `Condition.parameters`.** A `Condition` carries the list of pattern variables it constrains — `RemoveCompositeNumbers.json` holds `i1`, `i2`, `i3` with their types — and it's load-bearing: `conditionFunctions` emits `def w0(i1: Int, i2: Int, i3: Int): Boolean` from it, and `whereConditions` emits the matching `.where("…w0($i1, $i2, $i3)")`. But the emitter renders a condition as its value alone:

```text
con i1 * i2 == i3
```

The bindings never reach the surface. So the rule aspect can't round-trip — and more to the point, **#58 can't be authored until this is fixed**, because that design reuses `Pattern`, and `Pattern`'s binding vocabulary has to be expressible in drake before cases can be written there. The chain is #53 → #58 → #59.

#57 is a ten-minute change and a reasonable ride-along whenever you're touching the generator, but nothing waits on it.

One practical note: the working tree has three new files and seventeen modified, all green under `sbt test` — the `Drake`/`Expression`/`DrakeCLI` split plus the #52 sweep. Worth committing before starting #53, since #53 will touch `Drake.scala` again.

---

**[Dev]** Give me a standard git record to copy/paste from, so I can commit/push

**[Claude]** *(Read the most recent git-record for the house format, checked the tree state, wrote `git-record-2026-08-06-0642`.)* Before the block — one thing to flag: the relocation into `Drake`/`Expression`/`DrakeCLI` has **not been compiled**. Your last green run was after #52, before the split. So the gate belongs at the top.

Record written to `draco-git-record/git-record-2026-08-06-0642`, so `git add -A` sweeps it into the commit.

**Gate first** — the split is uncompiled:

```bash
sbt test
```

Then, only if green:

```bash
sh draco-git-record/git-record-2026-08-06-0642
```

The file contains the `cd`, `git add -A`, the `git commit -F -` heredoc, and `git push`.

**Why one commit and not three**

Three landings are in this tree — parser increment 1, the bracket move, the split — and they should have been three commits. They aren't, because the third **moved the code the first two changed**. `Drake.parse` doesn't exist in `Generator.scala` any more, so there's no way to stage landing 1 without re-deriving a tree that was never tested. Reconstructing it would mean inventing intermediate states and committing them untested, which is worse than one honest commit. The message separates the three landings; the record carries the detail.

The takeaway for next time is just to commit at each green gate rather than accumulating — you had two green runs in this session that were natural commit points.

**What's in the record beyond the summary**

Both of my errors, since the house style records corrections: deleting `format/Value.json`'s domain `typeParameters` before checking that `Domain[Format[_]]` is generated from them, and authoring the keyword sets as plain `val`s in an `App` companion (the single cause of all 123 failures). Also the coupling measurement that justified the split — zero references outward, exactly three inward — and the note that `DrakeCLI` is the third definition-less CLI.

*(Delegated action — Dev ran `sbt test`: compilation FAILED — `Drake.scala:699:28: recursive value statements needs type`, at `val statements = statements(c)`.)* *(Located and fixed all three shadow sites, checked the file for further val-shadows-def.)* A rename collision I introduced: the local `val statements` now shadows the method `statements`. Let me fix it and check for any others the compiler hasn't reached yet.

Fixed. Three sites, one of which was fatal:

| line | was | now |
|---|---|---|
| 699 | `val statements = statements(c)` | `val body = statements(c)` |
| 656 | builder `statements` inside `def statements` | `collected` |
| 295 | `val statements` in the emitter | `statementLines` |

Only 699 was an error — a local val whose initializer calls the method it shadows, which Scala reads as a recursive value. The other two shadow legally but confusingly, and since the rename created all three I've cleared them together. `body` at 699 also matches the `Dynamic(name, valueType, parameters, body, result)` field it feeds.

Checked the rest of the file for the same class of problem: no remaining val-shadows-def, and the only duplicate object-level name is `parseValue`, which is the intentional two-arity overload — both annotated with explicit return types, so it resolves.

This is exactly the kind of thing the mechanical rename was always going to produce and the compiler was always going to catch, which is why the gate was worth putting first.

```bash
sbt test
```

No change to the record — the fix belongs to the same landing.

*(Delegated action — Dev ran `sbt test`: 478 tests run, 478 succeeded, 37 suites, all passed.)* *(Updated the record's gate note to state the outcome; rewrote `project_drake_parser.md` to lead with the relocation; updated MEMORY.md.)* 478/478. Let me record the actual gate result in the audit file rather than leaving it as an instruction, and fix a memory note that this session made stale.

Green across the board — 478/478, 37 suites. Ready to commit and push:

```bash
sh draco-git-record/git-record-2026-08-06-0642
```

That does the `cd`, `git add -A` (which sweeps the record itself into the commit), the `git commit -F -` heredoc, and `git push`.

**Two things I did before handing it over**

The record's gate note now states the outcome rather than the instruction — 478/478 at 06:46, and that the run caught the shadow the rename introduced. An audit trail that says "this has not been compiled" is only true for the minutes before it was.

And I corrected a memory note this session made stale: `project_drake_parser.md` still said the parser was `Generator.parseDrake` in `Generator.scala`, which stopped being true an hour ago. It now leads with the relocation and the current API, so the next session doesn't go looking for a symbol that moved. That's the failure mode worth guarding against — memory that confidently names something that isn't there.

**Where this leaves you**

#53 holds `priority-next`: parser increment 2, rule and actor aspects. Its first concrete task is the `con`-bindings gap — `Drake.emit` drops `Condition.parameters`, so a rule pattern can't yet say which variables its guard constrains. That one also unblocks #58, since your actor-case design reuses `Pattern` and needs its binding vocabulary spellable in drake first.

*(Delegated action — Dev ran `sh draco-git-record/git-record-2026-08-06-0642`: commit `70d94a8` "drake parser increment 1, brackets on the opener, and the Drake/Expression/DrakeCLI split (#44, #52)" — 21 files changed, 1397 insertions, 493 deletions, four new source files (`Drake.scala`, `DrakeCLI.scala`, `Expression.scala`, `DrakeParseTest.scala`) plus the record — pushed `4c6e67c..70d94a8` to main.)*

---

**[Dev]** Moving to a new session.... give me a concise prompt to copy/paste for starting the new session.

**[Claude]** Pushed clean — `70d94a8`, 21 files, and the record is in the commit.

```text
Start on #53 (priority-next): parseDrake increment 2 — rule and actor aspects.

First concrete task, already diagnosed last session: Drake.emit drops
Condition.parameters, so a `con` line cannot say which pattern variables its guard
constrains. RemoveCompositeNumbers.json carries i1/i2/i3 with their types, and both
Generator.conditionFunctions and whereConditions consume them. Fix the surface first,
then extend Drake.parse to the rule aspect, then the actor aspect. Named arguments and
call chains are also in scope.

Note the drake projection MOVED on 2026-08-06: it is draco.Drake in
src/mods/scala/draco/Drake.scala (Drake.emit / Drake.parse), no longer in Generator.
Last commit 70d94a8, full suite 478/478.

#58 (match cases in the actor aspect) depends on this landing, because it reuses
Pattern and needs its binding vocabulary spellable in drake first.
```

The session-transition steps are otherwise done: record committed and pushed, issues filed and re-labelled, memory updated to point at the new location. The journal is Cowork's to write.

## Status

This chapter transcribes the drake-parser session that ran August 5–6, 2026 — and unlike chapter 70's, this session closes *committed and pushed*: the final recorded exchange executes `sh draco-git-record/git-record-2026-08-06-0642`, producing commit **`70d94a8` "drake parser increment 1, brackets on the opener, and the Drake/Expression/DrakeCLI split (#44, #52)"** — 21 files, +1397/−493, four new source files — pushed `4c6e67c..70d94a8`, with the next-session prompt handed over. **Opening state (carried from chapter 70's close):** the recovery session's three increments pushed in order (`17bcbb5` rete first pass, `e876509` Action outlier fix + repoint + exclusion removal, `4c6e67c` derivation drop), the thrice-reused journal-commit message amended to `51392cf` "draco-dev-journal: chapter 68 update + intro sync", memory corrected (`project_draco_rete.md` leading with the wrap types existing, #51 recorded and frozen), and **#44 the sole `priority-next`** — the parser genuinely unblocked. **What landed — #44 increment 1:** `Drake.parse` (born `Generator.parseDrake` at `Generator.scala:579`, relocated same-session), recursive descent over a token stream bounded by drake's reserved keywords, rejecting rule/actor aspects and (initially) value-position applications loudly; `DrakeParseTest.scala` with two gates — `emit(parse(text)) == text` exact per plain-type `.drake`, `parse(emit(td)) == td` per plain-type JSON — plus two report-only tests. **Round-trip counts:** Python-mirror pre-verification 45/45 plain types, extended to the full tree's 72 (8 role-aspect definitions held back and named); 70/70 re-verified after the #52 rewrite; the corrected loss report accounts for every field — 72 = 65 reference-package + 4 expression-form + 3 empty-collection, zero unaccounted. **Test counts across the arc:** first compiled run **144 run / 21 pass / 123 FAIL** — all 123 one cause, the three keyword sets authored as plain `val`s in an `App` companion (the project's own DelayedInit rule, broken by its enforcer and recorded as such) → `sbt test` all green (unnumbered) → 144/144 DrakeParseTest → compile FAILURE (`recursive value statements needs type` — the split's rename shadow, three sites cleared) → **478/478, 37 suites**, the number the commit carries. **The finding that justified the exercise:** the `domain` line emitted `namePackage :+ name`, silently dropping type parameters — `format/Value.json`'s `Format(F)` is load-bearing (`Domain[Format[_]]` is generated from exactly that field), and `DrakeGenTest` structurally could not see the loss because it only compares what the emitter chooses to spell; Claude first deleted the field as stray data before checking its consumer — both errors written into the git record per house style. Also fixed: package-word-as-keyword (qualified references now bounded by case), the value-position application (`primes/Primes.drake`'s `newKnowledge parameters par "Primes"` — positional only; named arguments fail loudly rather than flatten). **#52 implemented and closed same-session:** the bracket was on the wrong node — the container rule was conditional and required look-ahead while the actual overlap is opener-tail vs next member (exactly one biting site in the corpus, `CLI.drake`); brackets moved from container to opener under three unconditional local rules, the parser's indentation tie-break and column tracking deleted, 8 files swept (11 container pairs out, 22 opener pairs in — net +11), drake.dlt rewritten so whitespace-insignificance holds in fact rather than aspiration. **The split:** `Drake.scala` 777 lines (the drake projection, `Drake.emit`/`Drake.parse`, drake-prefix renames throughout), `Expression.scala` 34 (the three tree-shape accessors both projections need — the coupling measurement: zero drake→Generator references, exactly three the other way, all mislabelled expression accessors), `DrakeCLI.scala` 146 (`emit`/`parse`/`check` over a resource tree; `emit --write` so the next surface change is re-emission, not an ad-hoc Python rewrite like `rebracket.py`), `Generator.scala` down to 1663 from 2438; the `draco.drake.Drake.generator` repoint went through the JSON first. **The self-hosting measurement (Dev's detour):** `draco-gen` is *not* natively defined — `GeneratorCLI` is 292 hand-written lines with no JSON, `draco.CLI` is the generated read-only subset, and `DrakeCLI` makes a third definition-less CLI (named as debt); drake can *carry* the toolchain only as a host-opaque envelope (the trap — the `isString` metric moving hard the wrong way); the one blocking construct is **`match`** — 43 sites (literal cases 67, type-test 49, destructuring 35 — only the last with no encoding), `Generator.scala` otherwise already functional (zero `while`/`var`/builders), `foldLeft` already a native tree, and `Expression` natively expressible today but for `&&` missing from the declared operator set. **The match-placement design (Dev's, refined across three exchanges):** a `RuleAspect` already *is* one `case` and a `match` is a rule set, but RETE ordering/catch-all has no expression (disjointness-over-`otherwise` argued); Dev places match-case in the actor aspect's `start`/`message` — and `signal` argued in first, since `actorBehavior` already emits a hardcoded one-case `PostStop` match; ordering and scope both dissolve there, `Pattern` reuses verbatim with a two-semantics caveat (no scrutinee in a rule pattern; exactly one in an actor case), `Action.variables` the vacant slot; pure-function dispatch goes to `Pattern` cases in a `dyn` body instead (actors receive, they don't return); and `Generator(L <: Source)` not `Generator(Source)` — the argument must be a domain, the bound needn't (`DomainTransform(S <: DomainType)` the precedent), with `Source` today an empty definition and the neutral engine still prototyped in `SourceContract.scala`. **Issues:** filed **#52** (closed — implemented), **#53** (parseDrake increment 2 — now holds `priority-next`; first task the `Condition.parameters` emission gap; chain #53 → #58 → #59), **#54** (`from`/`modules` reference packages, 65 fields), **#55** (three legacy `Seq()` spellings — changes generated Scala, own increment), **#56** (the `from` sequence / Scala `with` — Dev's follow-up; 22 of 129 definitions carry two derivations; linked to #54, the case-convention terminator unstated in drake.dlt), **#57** (`Drake from Source`, `L <: Source`), **#58** (ordered `Pattern` cases in the actor aspect, `signal` first), **#59** (`Pattern` cases in a `dyn` body); #44 stays open as the umbrella. **Process notes recorded:** one commit instead of three because the split moved the code the first two landings changed — commit at each green gate next time; memory updated *before* handoff (`project_drake_parser.md` leads with the relocation so the next session doesn't hunt a moved symbol). **Open at the transcription boundary:** #53 increment 2 (the handoff prompt written); the actor tail (#42) and the aspects the parser still rejects; the `commands`-derivable-from-dyns increment and the arity/exit-code/precondition vocabulary (unfiled — design conversation); `&&` as the smallest self-hosting step (unfiled); no `bin/draco-drake` wrapper; and the chapter-70 carry-forwards untouched this session — #37 (structural equality, still #51's do-not-skip prerequisite), #49 (rete binding interlock), #50 item 2 (rule trait emission), #51 (frozen with the `TypeParameter` decision), the presence model (Phase 2b heads, `BodyElement.json`/`ActorAspect.json` still excluded), the `problems` reply-protocol question, the mods actors' migration off `Actor[T]` derivations, and the #46/#47 residue.
