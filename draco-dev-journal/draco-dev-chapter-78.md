# Draco Dev Journal — Chapter 78

**Session date:** September 16–18, 2026
**Topic:** The build session that followed chapter 77's discussion, run one accepted increment at a time — the call syntax `f(a, b)` landing and the `parameters`/`par` call form retiring (`a8afcd8`), the JSON corpus re-canonicalized from the drake (`8acd9c7`), `valueType` and then `typeParameters` becoming JSON nodes and the parser treeing the four type forms for value types, header parameters and reference arguments (`d59fe20`, `f4113d2`, `49fc734`, `4de5b8a`, `20835f0`) after Dev corrected the Morphic arrow to Haskell's right grouping, the `Presence` family as `draco.drake`'s first members and the first parameterized sealed family the generator emits (`f779578`) with `fold` as dispatch (`4cdefb3`), the discussion — conducted one point per reply — in which Dev proposed `ifThenElse` as a second dyn on Presence and then made laziness the language default ("What if lazy evaluation was the drake default"), settled as call-by-need with `now` as the override, and the first two of its three steps landing as two-run commits (`550a14e`, `c60a9ed`) that forced `now` onto every method meeting a host signature or used as a function value. Transcript.

---

**[Dev]** Continue the draco work from a5d2f5b (2026-09-14, main, 605 tests / 43 suites green).

Read first: memory project_drake_semantics_2026-09-13 — every design decision from the
2026-09-13 discussion, with item 1 (the result convention) landed and the rest not built.
Then project_generator_permanence and project_drake_parser for the transform-domain and
parser state. Two feedback notes govern how we work: feedback_lazy_discussion (one point
per reply when we are discussing) and user_directional_relations (restate any
non-commuting relation with an example and confirm before building on it).

Where we are: the generator is a transform domain of transform types. GenDrake runs over
the whole corpus (93 of 93), draketarget.DomainLine is the first target-side substitution
string with gendrake.DomainLineOf its mapping member, ++ and join are the first parsed
operator and declared symbol, and a dyn's result is now its body element named value
with the body head restored and the = marker gone.

Next increment, agreed: the call syntax — f(a, b) with positional arguments first, then
name:value, defaults from the declaration — treed by splitting the glued token, retiring
the parameters/par form in call position with its brackets and chain lines, and converting
the corpus files that use it. After that: value types and type parameters as trees of the
four type forms; then ifThenElse, Presence and fold declared in draco.drake; then
draketarget.DomainAspect. The target-side types are named for the five aspects.

Conventions unchanged: I run sbt, commits and pushes; you author the trio (json, drake,
scala) drake-first, write the record and CHANGELOG entry with each commit, and hand me the
command block with the expected numbers. Surface structural moves before building them.

**[Claude]** *(25 tool runs behind this reply.)*

I'll read the three memory notes and the two feedback notes first, then look at the current parser and corpus state before surfacing the call-syntax move.

I have what I need. Writing the parser and emitter now, as one scripted patch with exact anchors so nothing drifts.

Everything I need is in hand; the remaining work is all writes. Converting the 13 drake files and rewriting the two structural tests now, in one pass.

Both remaining spec mentions are the intended historical ones, and the corpus sweep is clean. Now the record, the CHANGELOG entry, and the memory updates, all in one pass.

All writes are in. One last verification read: the escaped quotes in the rewritten test came through a Python heredoc, so I want to see the file text itself, and the CHANGELOG entry's placement.

The call syntax is built and recorded. Nothing further is needed from the tree; what remains is your compile and suite run, which I do not do.

**One structural consequence to accept or correct before you run.** The parser now trees every parenthesised call, with no way to tell a drake-native call from a host-opaque one. The JSON corpus still holds host-opaque strings for calls, and each of those comes back from parse as a tree.

| host-opaque single-token call strings | 105 (59 in src/main, 46 in mods) |
|---|---|
| report-only surface-loss headline, predicted | ~139 fields (was 15) |
| gates affected | none: gate 2 compares rendered text, and the Scala rendering is byte-identical |

My recommendation is to take that jump in this commit and re-canonicalize the JSON from the drake as the next increment, mechanically, with the compiled tool (`DrakeCLI parse X.drake > X.json` per definition, Scala pinned by the generation gate). I did not fold it in because it needs the compiled tool and is its own concept. The DRACO.md table says "predicted" until you paste the actual figure.

**Flagged once, spelling.** I emit `name:value` with no space around the colon, verbatim from your prompt. The parser accepts whitespace either way. Say so and the emitter puts a space after the colon.

**What changed, 19 files plus the record:**
- [Drake.scala](src/mods/scala/draco/Drake.scala): every value emits on one line. The parser splits the one glued token at its final depth-0 parentheses, arguments at depth-0 commas, the head at depth-0 dots with the last call element closing the receiver. Inside parentheses the reserved words are ordinary text, which the emulation caught on `LazyList.from(start, step)` and a lambda binder named `domain`. A name given twice, or a positional after a named argument, is a parse error. The `=` token leaves the reserved set.
- [DracoGenerator.scala](src/mods/scala/draco/DracoGenerator.scala): a call in a String-typed slot renders unquoted, as `++` already did. The corpus holds exactly one tree in a String slot and it is that `++`.
- [drake.dlt](src/main/resources/draco/drake.dlt): APPLICATION SURFACE rewritten, BRACKETS has two openers, DIVERGENCES updated.
- Thirteen `.drake` files converted (eight in src/main, two mods actors, three scenario files). Their JSON and Scala are unchanged.
- [DrakeParseTest.scala](src/test/scala/draco/DrakeParseTest.scala): the two structural tests moved to the call form, and one new test for the at-most-once rule.
- DRACO.md and README.md caveats and baselines, CHANGELOG entry, and the record at [git-record-2026-09-16-1410](draco-git-record/git-record-2026-09-16-1410). Memory notes updated.

**Run the full suite first.** Expected 606 tests across 43 suites, all green, with these headlines: GEN MAP 28 match / 20 differ / 0 / 0, parse scope 95 draco + 10 mods with 0 held back, scenario 23 files 0 rejected 0 drifted 23 clean, GenDrake runs 93 of 93, PON unchanged, surface losses around 139. Paste the `surface losses` line back and I will replace "predicted" in the table.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

Then, if green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-16-1410 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-16-1410 && git commit -F /tmp/msg -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-16-1410 && git push
```

I could not compile, so the one real risk is a Scala error in the new parser block. If the compile fails, paste the first error and I will fix it before you run the suite.

*(Delegated action — Dev ran the handed-over commands: `sbt test` 604 passed, 2 failed (draco.gendrake.GenDrakeTest, draco.DrakeGenTest; GenDrake runs: 93 of 93 definitions emitted, 1 missing, 1 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 114 fields across 105 types [expression form (string vs tree): 111] [empty-collection spelling: 3]; Drake.parse scope: 95 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back.)*

---

**[Dev]** Accept, re-canonicalize the JSON from drake next.

**[Claude]** *(15 tool runs behind this reply.)*

Accepted. Starting the re-canonicalization: the JSON corpus is rewritten from each `.drake` through the compiled `DrakeCLI parse`, run with plain java over the classes your last sbt run produced, so no sbt from me. First the tree state and the tooling I need.

The order matches the test's list; the run failed only because zsh passed the file list as one word. Rerunning with the paths split.

One check on the record file itself: it went through an unquoted heredoc, so I want to see that the backslashes in the Value note and the command block came out as intended.

The re-canonicalization is done and verified; nothing else is needed from the tree before your run.

**What happened.** Every definition with a `.drake` was parsed by the compiled `DrakeCLI` over your last build's classes, and its JSON replaced where the result differed.

| definitions with a `.drake` | 103 |
|---|---|
| JSON rewritten | 40 |
| already identical | 62 |
| parse failures | 0 |

**Three definitions were deliberately left on HEAD.** BodyElement and ActorAspect carry authored-ahead aspects their JSON lacks. Value in `draco.format.json` carries lambdas and `if/then/else` in Haskell form, which the parser still returns as opaque text under GitHub #61, so its parse would have put drake-form lambdas into Scala. I restored it after the sweep. The 38 mods definitions with no `.drake` have nothing to canonicalize from, and the eight remaining opaque calls live there.

**One Scala change, three lines.** Two factory defaults in [DomainDictionary.scala:11](src/main/scala/draco/DomainDictionary.scala:11) and [TypeName.scala:37](src/main/scala/draco/TypeName.scala:37) move from `Seq()` to `Seq.empty`, because the drake spells an empty default `[]` and that is the tree it parses to. That is the empty-collection spelling loss closing, with the Scala following the JSON.

**Verified four ways before handover.** Every rewritten file differs from HEAD only in value form, three also dropping a non-canonical trailing key the decoder ignores. The engine regenerates each rewritten src/main definition's committed Scala. The fourteen-member TypeElement family regenerates as a group. The CLI's own check reports zero JSON drift over both roots.

Run the full suite. Expected 606 tests across 43 suites, all green, with no test changes. The surface-loss headline should fall from 114 to 2, the two being Value's form-carrying values. The DRACO.md row says "expected" until you paste that line.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-16-1555 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-16-1555 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-16-1555 && git push
```

Next in the agreed order, once this lands: value types and type parameters as trees of the four type forms. That one has a non-commuting relation in it, the left-associative Morphic arrow, which I will restate with an example before building.

---

**[Dev]** Accept, type-form trees next.

**[Claude]** *(1 tool run behind this reply.)*

Accepted. Before I build anything here, one relation needs restating and the scope needs sizing, so I'll check the tree state and measure where `valueType` and `typeParameters` are consumed.

Both commits are in and the tree is clean. Two things stand between "accept" and the first line of code, so I'll stop here rather than build on a guess.

**The relation to confirm, once.** The 2026-09-13 note records the Morphic form as binary and left-associative, a deliberate departure from Haskell. Left-associative means this:

```text
A -> B -> C   reads as   (A -> B) -> C
```

That is a function which takes a function from A to B and returns C. Haskell groups the other way, `A -> (B -> C)`: a function of A that returns a function of B. The corpus today has no chained arrow at all, so nothing existing is affected either way, but the parse and emit rule has to pick one. Is `(A -> B) -> C` the grouping you mean?

**Scope, one recommendation.** The engine reads `valueType` or `typeParameters` at 102 sites, and `typeParameters` sits inside TypeName, which is the structural identity of every type. Doing both in one step is the kind of leap that loses the thread. I recommend the first increment be `valueType` alone: it becomes a JSON tree the way `value` did, with a string still allowed as host-opaque text, the drake parser trees the four forms into it, the engine renders a tree back to the target's spelling, and the corpus re-canonicalizes from the drake as it did today. `typeParameters` follows as its own step once that holds. Accept or correct, together with the grouping above, and I build.

---

**[Dev]** I should go with Haskell's precedence. I originally said "right associative" but the example I gave was "left associative" before I realized which way Haskell did it. I was thinking of Haskell when I said right associative, but I showed the wrong example.

**[Claude]** *(1 tool run behind this reply.)*

Recorded: right-associative, Haskell's grouping, `A -> B -> C` reads `A -> (B -> C)`. That corrects the 2026-09-13 note, and it also corrects a rule of thumb in my notes about you, so I'm fixing both before anything else.

Both notes are corrected: the semantics note now records the Haskell grouping, and the note about direction no longer treats the example as the tie-breaker, since this time the word was right and the example was the slip.

I take the scope from my last message as accepted unless you say otherwise: `valueType` first, `typeParameters` as its own step after.

**One recommendation on the shape, then I build.** The family already declares `value` as a `Json` and `valueType` as a `String`. I recommend `valueType` becomes a `Json` on the same terms as `value`: a string is host-opaque text, an object is a type-form tree using the expression-tree convention already in the spec, so `A -> B` is `{"->": [A, B]}` grouping to the right, a tuple `(A, B)` is `{"(,)": [...]}`, an application `F(A, B)` is `{"()": [F, A, B]}`, a bare name is a string leaf, and the bounds are `{"<:": [parameter, bound]}` and `{">:": [parameter, bound]}` with the parameter first. The collection sugar `[T]`, `{T}`, `{K, V}` parses to applications of `Seq`, `Set`, `Map`. Reusing that convention means the accessors in `Expression` and the token-splitting the call syntax just built carry over to types unchanged.

The first commit under that shape changes the model and the engine seam only. The family's `valueType` becomes `Json`, the corpus stays all strings so no definition and no generated line moves, and the engine gains one function that renders a value type to the target's spelling, with every consumer that reads the type as text going through it. The second commit teaches the drake parser and emitter the four forms and re-canonicalizes the corpus from the drake, exactly as today's sweep did.

Accept or correct.

---

**[Dev]** Accept, valueType as Json first.

**[Claude]** *(9 tool runs behind this reply.)*

Accepted. Building the model change first. I need the family's current declarations, how the generated family handles the `Json`-typed `value` today, and an exact count of every consumer that reads the type as text. Requesting all of that at once.

The DRACO.md match failed again, and the script stopped there, so the spec, CHANGELOG, record and memory writes have not happened yet. I need the exact wording in DRACO.md; then the rest reruns.

All writes landed. One last look at the record's two command lines, since they went through an f-string and the escapes matter when you paste them.

The valueType-as-JSON increment is written and recorded. Nothing further is needed from the tree; the next step is your compile and run.

**What changed.** A `valueType` is now a JSON node on the same terms as a `value`: a string is the type's authored text, which is everything the corpus holds today, and an object will be a type-form tree once the parser trees the four forms. The corpus stays strings, so no definition and no generated line moved except the family's own declarations and one emptiness test.

- **Model, drake-first.** TypeElement, Fixed, Mutable, Dynamic, Parameter, Local, Factory, Variable and Case declare `valueType Json`. Their JSON was regenerated from the drake and the family's Scala from that JSON, both with the compiled tools. The Scala diff is exactly the `value` pattern: `Option[Json]` decoding, `Json.Null` defaults, null-or-empty elision.
- **DracoAspect.** Its `isEmpty` reads `valueType.isNull`, since a JSON node has no `isEmpty`, and the engine's Factory elision rule and the generated encoder line follow.
- **The accessor.** [TypeForm.scala](src/mods/scala/draco/TypeForm.scala) is new beside Expression: `text` returns a string verbatim, absent as empty, and fails loudly on a tree, naming the next increment. Every consumer reads `.valueType.text`.
- **The seam.** In [DracoGenerator.scala](src/mods/scala/draco/DracoGenerator.scala) the existing `targetTypes` pass gains `targetType`, the one place the target spelling is produced. It keeps absence as null rather than turning it into an empty string.
- **Constructions.** The parser, Generated.scala, and the two test modules wrap their string types in `Json.fromString`; AssemblyValidator, the inspect script and ScenarioGenTest read through `.text`.

**What I could not verify.** This is the first increment where the engine's own code changed in a way the compiled tools cannot exercise, so the mechanical `.text` pass across 90 engine reads and the test constructions are unverified until scalac sees them. If the compile fails, paste the first error and I will fix the site.

Run the suite. Expected 606 tests across 43 suites, all green, with every headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-16-2118 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-16-2118 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-16-2118 && git push
```

Next, once this lands: the parser and emitter learn the four forms, with `[T]`, `{T}` and `{K, V}` as applications of Seq, Set and Map and the arrow grouping to the right, and the corpus re-canonicalizes from the drake as it did today. Then `typeParameters`, as its own step.

*(Delegated action — Dev ran the handed-over commands: compile errors: `TypeElement.scala:234` type mismatch; `AssemblyValidator.scala:66` value text is not a member of io.circe.Json; `AssemblyValidator.scala:68` value text is not a member of io.circe.Json; `DracoGenerator.scala:25` type mismatch;.)*

*(Delegated action — Dev ran the handed-over commands: compile errors: `TypeDefinitionTest.scala:27` type mismatch;.)*

---

**[Dev]** Accept, the parser trees the four forms next.

*(Pasted with it: `sbt test` green, 606 passed; GenDrake runs: 93 of 93 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 2 fields across 105 types [expression form (string vs tree): 2]; Drake.parse scope: 95 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `d59fe20`.)*

**[Claude]** *(5 tool runs behind this reply.)*

Green at 606, committed as d59fe20, and the loss row can now lose "expected". Building the four forms next. Before writing the parser, I want every distinct value type in the corpus, since the tree rendering has to reproduce each one's Scala text byte for byte and its drake spelling exactly.

The type-form increment is written and recorded; nothing further is needed from the tree before your compile. The corpus JSON is deliberately untouched in this commit, so the re-canonicalization follows with the compiled parser, as the call syntax's did.

**What changed, nine files.**
- **Parser.** In [Drake.scala](src/mods/scala/draco/Drake.scala) a value-type slot now parses to a tree, read outside-in: a top-level arrow grouping to the right, then a bound, then `[T]`, `{T}`, `{K, V}` as the Seq, Set and Map applications, a parenthesized list as a tuple, `F(A, B)` as an application, else an Atomic string. `mut {T}` stays host text until `mut` moves to the element.
- **Three renderers, one per side.** `TypeForm.neutral` is the tree's text in drake's own notation; the emitter respells string leaves as before and parenthesizes a top-level arrow in the slot; the engine's `targetType`, now public, spells a tree for Scala with a Morphic left operand parenthesized.
- **Two consumers that needed the shape.** The assembly validator reads `ActorRef[M]` structurally through `TypeForm.applied`, which handles a tree and a host string alike. The codec gate's inherited parent parameters, which are loaded rather than generated, now take the target spelling.
- **Tests.** The JSON gate compares a value type by its one surface spelling, so the string and tree eras agree. The loss report gains a "type form" category. One structural test asserts the three bracket forms, the right grouping, and both renderings.

**Verified before handover.** An emulation of the parse and both renderings over all 137 distinct value types in the corpus reproduces every Scala text and every drake spelling; 43 become trees, 94 stay leaves. The compile itself I could not run.

Run the suite. Expected 607 tests across 43 suites, all green. The loss headline should rise to about 123, the type-form category counting every compound value type until the re-canonicalization lands; the DRACO.md row says "expected" until you paste the line.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-17-1120 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1120 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1120 && git push
```

Then, once the classes are compiled, I re-canonicalize the JSON from the drake with them, verify the Scala against the committed files as before, and hand you that commit.

*(Delegated action — Dev ran the handed-over commands: `sbt test` 606 passed, 1 failed (scenario.ScenarioGenTest; GenDrake runs: 93 of 93 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 367 fields across 105 types [type form (string vs tree): 118] [OTHER — unaccounted: 247] [expression form (string vs tree): 2]; Drake.parse scope: 95 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back.)*

---

**[Dev]** Accept, typeParameters as type-form trees next.

*(Pasted with it: `sbt test` green, 607 passed; GenDrake runs: 93 of 93 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 8 fields across 105 types [expression form (string vs tree): 2] [type form (string vs tree): 6]; Drake.parse scope: 95 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `f4113d2`.)*

**[Claude]** *(3 tool runs behind this reply.)*

Green at 607 and pushed as f4113d2. The type-form residue measured 6 rather than my predicted 3, so before starting on `typeParameters` I want to name those six and put the measured figure in the table. Then the consumer census for `typeParameters`, which sits inside TypeName's identity.

The `typeParameters` step is written and recorded, and the three-field residue from the last commit is closed in the same pass. Nothing further is needed from the tree before your compile.

**What changed, fifteen files.**
- **TypeName's trio, drake-first.** `typeParameters` is `[Json]` on the value-type terms. The JSON and Scala regenerated with the compiled tools; the Scala diff is the three `Seq[String]` to `Seq[Json]` lines and the decoder. Structural identity compares the nodes as it compared the strings.
- **The corpus stays strings.** The census showed the header bounds `S <: DomainType` and `T <: Product` live in type-name parameters, not only in element names as I had assumed; they stay as authored text until the parser trees the header.
- **Engine.** TypeName is identity, so `targetTypes` does not rewrite its parameters in place. Each render site spells a parameter through the same target rendering a value type takes, at five sites.
- **Drake projection.** A string parameter spells verbatim, as the header always has; a tree spells as any type form. The parser wraps the header's and a reference's parameter strings.
- **Residue closed.** Domain, Rule and Type re-canonicalized from the drake with f4113d2's compiled classes, each regenerating its committed Scala byte for byte.

**Not verified.** The compile: the five engine sites and the parser's two wrap points are the risk. No new test, since no surface moved.

Run the suite. Expected 607 tests across 43 suites, all green, with the loss headline at 5 fields: type form 3, which are the standing exclusions ActorAspect and Value, and expression form 2.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-17-1226 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1226 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1226 && git push
```

Next: the parser trees the header's parameters and a reference's arguments, such as `Dictionary(K, V)` and `Primal((S, T))`, and the corpus re-canonicalizes from the drake. That step touches how a reference to a parameterized type is spelled and resolved, so I will surface its shape before building.

*(Delegated action — Dev ran the handed-over commands: compile errors: `TypeNameTest.scala:71` type mismatch; `TypeNameTest.scala:71` type mismatch;.)*

---

**[Dev]** Accept, the parser trees the header parameters next.

*(Pasted with it: `sbt test` green, 607 passed; GenDrake runs: 93 of 93 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 105 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 95 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `49fc734`.)*

**[Claude]** *(1 tool run behind this reply.)*

Green at 607, pushed as 49fc734, and the loss row takes the measured 5. For treeing the header parameters I first need to know which definitions carry compound or bounded parameters, in which roots, and how the one host-text argument (`Seq[Waypoint]`) is spelled on the surface today, because a reference argument becoming a tree normalizes that spelling.

The header-parameter step is written and recorded; nothing further is needed from the tree before your compile.

**What changed, six files.** Three wrap points in the parser that said `Json.fromString` now say `typeForm`: the header `type X(S <: DomainType, T)`, every reference argument such as `from Holon((S, T))`, and a foreign reference's members. A bound is a leaf with the parameter first, a tuple argument an Objective, a bare variable an Atomic string. The emitter and the engine already spelled a tree from the previous step, so nothing else moved there. TypeName's structural identity now compares the trees.

**Corpus.** Four src/main definitions carry a compound or bounded parameter: DomainTransform, TypeTransform, Holon and base/Coordinate. Their JSON re-canonicalizes from the drake next, with the compiled parser. The scenario has none, so its json-equals-parse assertion holds and the two-step shape works this time. The six drake-less sentient definitions with host-text arguments have no surface to canonicalize from and stay as they are.

**Tests.** The JSON gate compares each type parameter by its surface spelling so the string and tree eras agree; the loss report compares the parameter list whole and files it under "type form"; one structural test asserts the bounded header, the tuple argument and the round trip.

Run the suite. Expected 608 tests across 43 suites, all green, with the loss headline at 11 fields: type form 9, being the four definitions' six parameter fields plus the three standing exclusions, and expression form 2. The DRACO.md row says "expected" until you paste the line.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-17-1633 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1633 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1633 && git push
```

Once the classes are compiled I re-canonicalize the four JSON files and hand you that small commit. After it, the agreed order reaches the runtime symbols: `ifThenElse`, `Presence` and `fold` declared in `draco.drake`. That one puts new types into a domain, so I will surface its shape before building.

---

**[Dev]** Accept, re-canonicalize the four JSON files.

*(Pasted with it: `sbt test` green, 608 passed; GenDrake runs: 93 of 93 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 11 fields across 105 types [expression form (string vs tree): 2] [type form (string vs tree): 9]; Drake.parse scope: 95 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `4de5b8a`.)*

**[Claude]** *(2 tool runs behind this reply.)*

Green at 608 with the predicted 11, pushed as 4de5b8a. Running the four-file sweep with the freshly compiled parser, with the Scala pin on each, and a full-corpus pass to confirm nothing else moves.

The four-file sweep is done and recorded. Nothing else is needed from the tree; the next step is your run.

**What changed.** Exactly the four predicted definitions were rewritten from their drake by the compiled parser: DomainTransform, TypeTransform, Holon and base/Coordinate. Their header bounds are now bound leaves with the parameter first, and the Holon derivation's `(S, T)` an Objective. All 121 other definitions with a `.drake` were already what their surface says. Each of the four regenerates its committed Scala with the compiled engine, so no `.scala` moves.

**What this closes.** The type-form arc is complete for everything the surface covers: value types, header parameters and reference arguments are trees wherever the drake spells them. The parse test's type-form residue is the three standing exclusions. Outside the gate, the CLI check reports drift on eleven drake-less mods definitions, which now include the sentient host-text derivation arguments; they have no surface to canonicalize from.

Run the suite. Expected 608 tests across 43 suites, all green, with the loss headline at 5 fields: type form 3 and expression form 2. The table says "expected" until you paste the line.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-17-1726 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1726 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1726 && git push
```

Next in the agreed order is the runtime: `ifThenElse`, `Presence` with `Present` and `Absent`, and `fold`, declared in `draco.drake`. That puts new types into a domain and touches how the engine renders a conditional, so before building I will surface its shape as one recommendation for you to accept or correct.

---

**[Dev]** Accept, surface the shape of ifThenElse, Presence and fold.

*(Pasted with it: `sbt test` green, 608 passed; GenDrake runs: 93 of 93 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 105 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 95 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `20835f0`.)*

**[Claude]** Pushed as 20835f0, and the loss row can take the measured 5. Before shaping the runtime I want two facts from the tree: what `draco.drake`'s domain type holds today, and whether a parameterized `dyn` without a body already generates an abstract method anywhere, since the shape I have in mind leans on both.

The table row takes the measured 5 and rides the next commit. The two facts I needed are in: `draco.drake` holds one type today, `Drake from Source`, with nothing declared on it, and the engine already emits an abstract method for a parameterized `dyn` without a body. Here is the shape, as one recommendation.

**Presence is three types in `draco.drake`, and `fold` is a `dyn` on it.** `Presence(T)` is a family root with `modules [Present, Absent]`, the same sealed shape TypeElement uses. `Present(T) from Presence(T)` carries `par value T`. `Absent(T) from Presence(T)` carries nothing and stays parameterized, because variance is the later concept and an unparameterized Absent would need it. The eliminator is declared on the root as an abstract method and defined in each member in pure drake, with no host code and no conditional anywhere:

```text
Presence   dyn fold(R) R [ parameters par absent R par present (T -> R) ]
Present    … body fix value R present(value)
Absent     … body fix value R absent
```

That is exactly what a `dyn` was kept for: polymorphism. One consequence to flag once, because it departs from the agreed spelling: the call surface becomes `p.fold(absent, f)`, a method on the presence, rather than the prefix `fold(p, absent, f)`. The prefix form would need a symbol with a target-side rendering; the method needs nothing but drake.

**`ifThenElse` is a declared symbol, and a declared symbol is a bodiless global `dyn` on the runtime's domain type.** `draco.drake.Drake` gains, in its globals, `dyn ifThenElse(T) T` with parameters `condition Boolean`, `thenBranch T`, `elseBranch T` and no body. A call `ifThenElse(c, t, e)` is an ordinary call tree on the surface, so no new syntax and no new parser work. The engine renders a call to a declared symbol natively from its table, where `join` already lives, as `if (c) t else e`, which keeps the branches lazy; and it emits the symbol's own definition with by-name branches so the runtime also exists as a value. `join` moves from the spec's prose into the same globals, so there is one place symbols are declared. The Haskell-form `if … then … else` surface and its `"if"` tree node retire, and Value.drake converts to the call, leaving only its lambdas in the loss tail.

**Order.** Presence first, since it is pure drake and exercises `fold` as dispatch on its own. Then `ifThenElse` with the Value conversion. Presence is not for parameter defaults; its first use is the codec aspect, later.

Accept or correct, and the `p.fold` spelling with it.

---

**[Dev]** Don't jump ahead of me too far on this idea, but what if we just added ifThenElse as a second dyn on Presence after dyn fold?

**[Claude]** My reading of it: `ifThenElse` becomes dispatch, exactly as `fold` is. On Presence it is `dyn ifThenElse(R) R` with `thenBranch R` and `elseBranch R`; Present's body is `thenBranch`, Absent's is `elseBranch`. Then no symbol table and no native rendering are needed for the conditional, because a two-case sum type eliminated by dispatch is what a conditional is. The condition is a presence rather than a Boolean.

Is that the reading you mean?

---

**[Dev]** I think so. Would it be usable in any recursive function for execution termination?

**[Claude]** Not as written. A `dyn` parameter is strict: `p.ifThenElse(recurse(n - 1), base)` evaluates both arguments before the dispatch happens, so the recursive branch runs whether or not it is chosen and the recursion never bottoms out. The same is true of `fold`'s `absent R` today; only `present (T -> R)` is safe, because a function is not applied until the body applies it.

It terminates a recursion only if the branches are deferred. There are two places that deferral can live: in the definition, with the branches declared as functions of nothing, `thenBranch (Unit -> R)`, applied in the body as `thenBranch()`; or in the target, with the engine rendering those two parameters by-name so the drake stays `thenBranch R`. Which would you rather it be?

---

**[Dev]** What if  lazy evaluation was the drake default  and we had a way of overriding "lazy" with "eager" for source code generation into a target where "lazy" wasn't the default?

**[Claude]** That reads as Haskell's stance: every parameter, `fix` and `loc` is lazy unless the author says `eager`, and a target whose default is eager, Scala among them, renders the lazy ones as its deferred forms. `ifThenElse` then needs nothing special; its branches are lazy because everything is, and the generated companions already spell every `fix` as a `lazy val`, so part of that default is in place.

One point to settle before I take it further: which laziness. Haskell's is call-by-need, evaluated at most once and remembered. Scala's by-name parameter is call-by-name, evaluated every time it is used, so a lazy parameter used twice in a body would run twice. Call-by-need in Scala means the engine binds each lazy parameter to a `lazy val` on entry rather than passing it by-name. Is at-most-once the semantics you mean by "lazy"?

---

**[Dev]** I've always preferred Haskell over Scala for syntax snd semantics. I've used Scala forms  for referencing values and calling functions, but that was for readability for someone used to OO languages.

**[Claude]** *(1 tool run behind this reply.)*

Then call-by-need it is: a lazy parameter is evaluated at most once and remembered, which is what a Haskell reader expects and what a recursive `ifThenElse` needs. The Scala reading of a value or a call stays as a readability choice, not a semantic one. Recording both before I forget them.

Recorded, in the semantics note and in what I know about how you decide.

The next single point is where `eager` is written. The one precedent for a modifier on an element is `mut`, which you placed on the element rather than on its type. Read the same way, an eager parameter would be `eager par count Int`, and an eager binding `eager fix total Int …`. Is the element the place you have in mind for it?

---

**[Dev]** Yes, but let's call it "now" instead of "eager".

**[Claude]** *(1 tool run behind this reply.)*

`now` it is: `now par count Int`, `now fix total Int …`. It becomes a reserved word, which matters only if a value somewhere uses a bare `now`; checking the corpus for that while I record the decision.

No bare `now` anywhere in the corpus, so reserving it costs nothing today. Recorded as your word, on the element like `mut`.

Is there more of the shape you want to settle in discussion, or shall I write up what we have as the build proposal, with Presence and `fold` first?

---

**[Dev]** Write up the build proposal, Presence and fold first.

**[Claude]** *(1 tool run behind this reply.)*

Before the write-up, one grounding check: whether a parameterized type with `modules` already exists in the corpus, since Presence would be one, and how the `draco.drake` domain lists its members.

Nothing more is needed from the tree. Two facts from that check shape the order: no parameterized type in the corpus carries `modules`, so `Presence(T)` would be the first parameterized sealed family the generator emits; and no member anywhere overrides a parent `dyn`, so `fold` would be the first exercise of `dyn` as dispatch. Each is an untried generator path, so I keep them apart.

**Build proposal, in order. One concept per commit, corpus re-canonicalized with the compiled parser where a step trees something new.**

1. **Presence as a family, without fold.** Three definitions in `draco.drake`, authored drake-first: `Presence(T)` with `modules [Present, Absent]`, `Present(T) from Presence(T)` with `par value T`, `Absent(T) from Presence(T)`. The domain lists them under `types`. Gate: the family generates and its codec derives. This is the first parameterized sealed family, so the generator may need work at the discriminator and the `[T]` on the trait; that is the whole content of the step.

2. **fold as dispatch.** `dyn fold(R) R` with `absent R` and `present (T -> R)` declared abstract on Presence, defined as `present(value)` in Present and `absent` in Absent. First member override of a parent `dyn`. One test constructs a Present and an Absent and folds both. Call surface `p.fold(absent, f)`.

3. **Lazy by default, `now` to override.** The engine binds every parameter, `fix` and `loc` to a call-by-need form on entry unless the element carries `now`; `now` joins the reserved set, the parser and emitter carry it, the spec states the default. This step changes the generated signature of nearly every method in the corpus, so DracoGenTest moves across the board and the Scala regenerates wholesale. It is the largest step and I will size it before building.

4. **ifThenElse as the second dyn on Presence.** `dyn ifThenElse(R) R` with `thenBranch R` and `elseBranch R`, Present taking the first, Absent the second. Lazy by then, so a recursive branch terminates. Value.drake converts to it where a Boolean condition can become a presence; how a Boolean becomes a presence is the one design point still open, and I will surface it then rather than now.

The retired items follow from these: the Haskell-form `if … then … else` surface and its `"if"` node go with step 4, and the prose declaration of `join` in the spec moves into a definition when a home for declared symbols is settled.

Accept the order and step 1's scope, or correct either.

---

**[Dev]** Accept, Presence as a family first.

**[Claude]** *(12 tool runs behind this reply.)*

Building step 1. Two generator paths are untried here, so before authoring I want the precedents in front of me: how the TypeElement family is arranged and gated as one file, how a parameterized trait's codec is generated today, and what the `draco.drake` domain type's Scala looks like so it regenerates cleanly with a `types` list.

Step 1 is written and recorded; nothing further is needed from the tree before your compile.

**What landed, drake-first.** Three definitions in `draco.drake`: `Presence(T)` with `modules [Present, Absent]`, `Present(T)` carrying `par value T`, and `Absent(T)` bare and parameterized, since variance is a later concept. The runtime domain lists them. Their JSON came from the compiled parser, and the family lives in one Scala file, as TypeElement's does, gated by a new group test with the three excluded from the per-type sweep.

**What the untried path taught the generator.** Run over an overlay of the current resources, the compiled engine produced the family with three defects, two of them real:
- **No host codec for a parameterized family.** The root's discriminated codec named a `T` the companion cannot name, and the members' `Codec.sub` named `Presence` without its argument. The engine now emits no codec for a parameterized root or a member of one, the rule a parameterized plain type already lives by. Presence's codec is the codec aspect's business, in drake.
- **`Null` at `Any`, not `Nothing`, when a parameter is typed by the type's own variable.** A null cast to `Nothing` compiles and throws on first touch. Every existing `Null` stays at `Nothing`, so no committed line moves.
- The third, the member companions lacking `with DracoType`, was the stale resources in the compiled classes, not the engine. The overlay fixed the run.

**Not verified.** [Presence.scala](src/main/scala/draco/drake/Presence.scala) is the overlay output with the two fixes applied by hand, so the group test is the one place my hand stands in for the engine; read its diff first if anything fails.

Run the suite. Expected about 621 tests across 43 suites, all green: three types times the per-type gates plus one group test, with the run deciding the exact count. GenDrake should read 96 of 96, parse scope 98 draco plus 10 mods, and the loss headline 5 across 108 types.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-17-1818 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1818 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1818 && git push
```

Next, as agreed: `fold` as a dyn on Presence, defined by dispatch in Present and Absent, which is the first member override of a parent `dyn`.

---

**[Dev]** You did a lot of self correcting work in this response, mostly Codec and Presence related. The parameterized families mentioned is interesting to me because it was never clear to me how much abstract types (types with at least one unbound type parameter) need to implement support for the Codecs of concrete types. 

Regardless  of my interest, proceed correcting the errors:

*(Pasted with it: `sbt test` 617 passed, 1 failed (draco.DracoGenTest; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back.)*

**[Claude]** *(1 tool run behind this reply.)*

It compiled, and the one failure is where I said to look first: the group test comparing my hand-assembled family file with the fixed engine's output. Reading its diff.

Fixed: the stray blank line before the Presence companion's closing brace, the only difference the group test found. The test count is 618, not my guess of 621, and the DRACO.md paragraph and the record now say so.

Rerun the suite. Expected 618 tests across 43 suites, all green, with the headlines as they were on this run.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-17-1818 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1818 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-1818 && git push
```

On your question about abstract types and codecs: it is a real design question, not a generator detail, and today's answer is only the one this commit made by omission. I would rather take it up with you in discussion once this lands than settle it in passing here.

---

**[Dev]** Accept, fold as a dyn on Presence next.

**[Claude]** *(2 tool runs behind this reply.)*

Accepted. First a check of the tree state, then the three drake edits and a regeneration with the compiled engine, which now carries both family fixes. One precedent to confirm alongside: that a dyn whose body is only its `value` element renders as an inline expression, since `fold`'s body in Present is `present(value)` and `value` must resolve to the trait's field, not to a local.

`fold` is in and recorded; nothing further is needed from the tree before your compile.

**What landed.** The eliminator of draco's option is dispatch, all in drake: `dyn fold(R) R` with `absent R` and `present (T -> R)` declared abstract on Presence, defined as `present(value)` in Present and `absent` in Absent. No host code, no conditional. It is the first member override of a parent `dyn` in the corpus.

**Verified.** The compiled engine from your last build, run over the resource overlay, regenerated the family file, and the diff against the committed one is exactly the three `fold` lines, so no hand stood in for the engine this time. The CLI surface and JSON checks over `draco/drake` are clean.

One reading note for the drake: in Present, `fix value R present(value)` looks self-referential and is not. The body's result element is named `value` by the result convention, and it inlines as the method's expression, so `value` there is Present's field.

**Test.** A new two-test suite, [PresenceTest.scala](src/test/scala/draco/drake/PresenceTest.scala), folds a Present and an Absent directly and through the parent type with branches of a different type. The default branch is still strict, which is the laziness question you raised; that is the next step.

Run the suite. Expected 620 tests across 44 suites, all green, with every headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-17-2006 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-2006 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-17-2006 && git push
```

Next, as agreed: lazy by default with `now` to override. That step regenerates nearly every method in the corpus, so I will size it and surface the shape before building. Your question about abstract types and codecs is also still open, and I would take it whenever you want to discuss.

---

**[Dev]** Accept, size the lazy-by-default step and surface the shape.

**[Claude]** Sizing it from the corpus before proposing. What lazy-by-default touches in generated Scala is countable from the JSON: method parameters, factory parameters, bindings inside method and action bodies, and the mutable elements that have no lazy form. The one semantic hazard is a binding whose value has an effect and is never referenced afterwards, which laziness would silently drop; that is measurable too.

The measurement is in, and one result changes the risk picture: nothing in the corpus needs `now` today. The single binding my scan flagged, RootInterface's `session`, is an actor field used from a later action, not a dropped effect.

| what lazy-by-default touches | count |
|---|---|
| factory parameters | 127 |
| method parameters (28 methods) | 33 |
| bindings inside bodies (`fix` 47, `loc` 10) | 57 |
| mutable elements, which have no lazy form | 0 |
| definitions touched | 88, of which 56 in src/main regenerate their Scala |

**The shape, as one recommendation.**

The Scala rendering reuses a convention the corpus already has. A factory's `apply` takes `_name: => String` and its fields are already `lazy val name = _name`, so a factory parameter becomes call-by-need by adding the arrow alone. A method does the same: `def f(_x: => T): R` with `lazy val x = _x` as its first line, so bodies keep referencing `x`. A `fix` or `loc` inside a body becomes `lazy val`. A `mut` stays a `var`, which is the one element that is `now` by nature. Abstract methods carry the same by-name signatures, so `fold`'s `absent` becomes lazy and the recursion question closes. Effects in `mon` statements run in order as they do now; a lazy binding runs on first use, and one never used never runs, which is Haskell's rule and which nothing in the corpus relies on.

`now` is a Boolean on TypeElement, absent meaning false, which is Haskell's strictness annotation in draco's terms. The parser and emitter carry it as a prefix on the element, and the engine renders a `now` element in today's eager form.

**Build in three commits, smallest semantic risk first, each needing two runs.** I cannot compile the engine, so each commit is: you run once and DracoGenTest fails on the regenerated set, I regenerate that set with the compiled engine, you run again and it is green, with the regenerated Scala compiled for real.

1. Body bindings become `lazy val`, and `now` lands with its model flag, parser, emitter and a structural test. About 30 definitions regenerate.
2. Method parameters become by-name with the `lazy val` prelude. 28 methods.
3. Factory parameters become by-name. 127 parameters across the most files, and the step that touches every hand-written caller, all of which pass values and so need no change.

Accept the shape and the order, or correct either.

---

**[Dev]** Accept, body bindings become lazy val first.

**[Claude]** *(6 tool runs behind this reply.)*

Accepted. Before writing, two quick facts: whether any factory parameter in the corpus is already a Boolean, since `now` needs a false-elision codec rule that would then regenerate other codecs too, and the exact text of the engine's five body-binding render sites so the edit anchors hold.

Step 1 is written; this is the two-run commit we agreed, so what comes next is your first run and the paste of what it flags.

**What changed.** Seven engine render sites now emit a body `fix` or `loc` as `lazy val` unless the element is `now`; `mut` stays a `var`, and a field set by a factory body stays the trait's `lazy val`, since no strict val may override it. `now` rides TypeElement as a Boolean that elides when false; the binding kinds' factories take it as a trailing default so no caller moves. The family regenerated with the compiled tools plus one hand-applied encoder line, because the false-elision rule is new to the engine. The parser reserves `now` and rebuilds the member strict; the emitter prefixes it; one structural test asserts the round trip. The spec gains an EVALUATION section stating the default and the three steps.

**Run 1.** Everything compiles, the family included, and DracoGenTest fails on each src/main definition with a `fix` or `loc` statement in a body. The upper bound is 19 definitions; fewer in practice, since a body whose only binding is its `value` result inlines. Everything else should be green. Paste the `Failed tests` block, or the first failure's diff if something other than DracoGenTest fails.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Then I regenerate exactly the flagged Scala with the compiled engine, verify, and hand you run 2, which compiles the regenerated Scala for real. Expected at run 2: 621 tests across 44 suites, all green, every headline unchanged. The commit block is in the record for after that.

*(Delegated action — Dev ran the handed-over commands: `sbt test` 606 passed, 15 failed (draco.DracoGenTest, scenario.ScenarioGenTest; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 20 match, 28 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back.)*

*(Delegated action — Dev ran the handed-over commands: `sbt test` 619 passed, 2 failed (scenario.ScenarioGenTest; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back.)*

---

**[Dev]** Looks like we're at "What's the next step?". Here is the successful result of the previous exchange:

*(Pasted with it: `sbt test` green, 621 passed; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `550a14e`.)*

**[Claude]** Green at 621, pushed as 550a14e. The table's loss row can now read measured 5 across 108, and it rides the next commit.

**Next step, per the agreed order: lazy step 2, method parameters.** A `dyn` parameter becomes by-name with a call-by-need prelude, reusing the factory convention the corpus already has:

```text
def naturals(_start: => Int = 0, _step: => Int = 1): LazyList[Int] = {
  lazy val start: Int = _start
  lazy val step: Int = _step
  LazyList.from(start, step)
}
```

Bodies keep referencing the bare name. Abstract methods carry the same signature, so `fold`'s `absent` becomes lazy in Presence, Present and Absent alike, and the recursion question closes. Scope: 28 methods, 33 parameters, same two-run shape.

**One consequence to flag now, because it is the first real use of `now`.** TypeName's `equals(other Any)` overrides a host method whose parameter is by-value, and Scala will not let a by-name parameter override it. So TypeName.drake gains `now par other Any`, and the corpus needs `now` after all, exactly where the host's own signature is being met. I will check for any other host-overriding method before building; `hashCode` has no parameter, so it is unaffected.

Accept step 2 with that flag, or correct.

---

**[Dev]** Accept step 2

**[Claude]** *(6 tool runs behind this reply.)*

Accepted. Before writing I need the two engine functions that render a method's signature and body, their call sites, and a check for any other host-overriding method in the corpus besides TypeName's `equals`.

The restore worked but my anchor missed the JSON's indentation, so the flag is not in yet, which is exactly what the emitter check reports. Reading the parameter's lines as they are.

Fourteen spaces, not eighteen. Applying the flag, re-checking with the compiled emitter and engine, and writing the spec line, docs, CHANGELOG, record and memory for step 2 in the same pass.

Step 2 is written; this is again a two-run commit, so what comes next is your first run and the paste of what it flags.

**What changed.** A `dyn` parameter arrives as `_x: => T`, and the body opens with `lazy val x: T = _x`, so the parameter is evaluated at most once and never if unused; that is the factory convention applied to methods. Abstract methods carry the same signature, so `fold`'s default is lazy in all three Presence types, and PresenceTest gains a test that a Present never evaluates it and an Absent forces it exactly once. A parameters block now admits `now`.

**The first real `now`.** TypeName's `equals` overrides the host's by-value signature, so its drake says `now par other Any` and its generated Scala is unchanged. The compiled parser predates the admit-set change and left TypeName.json empty when I asked it to write that file; I restored it from HEAD with the one key the new parser writes and checked it with the compiled emitter and engine. The two `_source` parameters become `__source` under the uniform rule.

**Run 1.** DracoGenTest fails on the twelve definitions with a lazy-parameter method: CLI, ContentSink, Dictionary, TypeLoader, TypeName, format/Value, format/json/Value, DefinitionPath, Primes, and the Presence family through its group test. No scenario or mods definition has a parameterized method, so the scenario gate and the example-domain map should hold. Paste the `Failed tests` block.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Then I regenerate the flagged Scala with the compiled engine over the full overlay and hand you run 2, expected 622 tests across 44 suites, all green.

---

**[Dev]** A single command in our exchange seems to indicate errors are expected in execution:

*(Pasted with it: `sbt test` 610 passed, 12 failed (draco.drake.PresenceTest, draco.DracoGenTest, draco.GenerateAndCompileTest; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back.)*

**[Claude]** *(5 tool runs behind this reply.)*

Yes: run 1 of a two-run commit is expected to fail on the gate, and it did on exactly the ten predicted definitions. The PresenceTest failure is also expected at this point, since it runs against the committed family file whose `fold` is still strict until I regenerate it; I should have said so. The one failure I did not predict is the generate-and-compile test, which compiles the new output at runtime, so it may be holding a real compile error in the by-name rendering. Reading its log first.

Run 1 did its job and found two real defects beyond the predicted regeneration, both now fixed.

- **The engine dropped `now` at its entry.** The normalization pass that rebuilds every element for the target rebuilt them without the flag, so TypeName's `equals` came out by-name despite the JSON saying otherwise. That also means step 1's `now` on a binding would have been lost the same way; nothing in the corpus used it, so it went unseen. The rebuild now carries the flag.
- **Host signatures are found by derivation, not by name.** Dictionary implements Scala's own `Map` methods, whose parameters are by-value, so `get`, `removed` and `updated` needed `now`, and so did CLI's explicit `main`. My check had looked for host-like method names; the right test is a type deriving a foreign type or declaring `main`. Both drake files carry the flags, parsed by your fresh compiled parser, which admits `now` in a parameters block.

The PresenceTest failure was expected at run 1, since it ran against the committed strict `fold`; I should have said so. Regeneration used a refreshed overlay after my first pass reused stale classes and changed nothing. Dictionary's Scala is restored from HEAD and TypeName's is unchanged, since every parameter of theirs is strict; CLI's `main` is back to its strict one-liner, the form the fixed engine writes.

This is run 2. Expected 622 tests across 44 suites, all green, with the core group compiling and the laziness test passing.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-18-1038 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1038 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1038 && git push
```

---

**[Dev]** Since you gave me two commands in this last exchange, are the following errors unexpected?

*(Pasted with it: compile errors: `TypeLoader.scala:20` type mismatch; `TypeLoader.scala:32` type mismatch; `DracoGenerator.scala:2047` type mismatch; `DracoGenerator.scala:2047` missing argument list for method targetTypes in object DracoGenerator.)*

**[Claude]** *(2 tool runs behind this reply.)*

Unexpected, and they are a real consequence of by-name parameters in Scala: a method with a by-name parameter cannot be passed where a function value is expected. `readDefinition` is used as a function in TypeLoader's own drake value (`flatMap(readDefinition)`), and `rooted` is used as one by the hand-written engine. Same class as the host-signature cases: a target constraint forcing `now`. Reading the two methods as generated now and as committed at HEAD, to restore exactly their strict forms.

Unexpected, yes, and it is a third target-forced case for `now`: a Scala method with a by-name parameter cannot be passed where a function value is expected, because its eta-expansion has the wrong type. TypeLoader's `readDefinition` is used that way in its own drake value and `rooted` by the hand-written engine. Both parameters are now `now` in the drake, the JSON is written by your fresh parser, and the two methods are back to HEAD's strict one-liners, the form the fixed engine emits. The other TypeLoader methods stay lazy.

The rule is now stated three ways in the record: `now` is required where the target binds the signature, whether a host signature is met, as with `equals`, the `Map` operations and `main`, or the method is used as a function value. A scan shows TypeLoader's two are the only method-as-function uses in corpus values and hand-written code.

This is run 3 of the same commit. Expected 622 tests across 44 suites, all green.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

If green, commit with the record's message, path-scoped:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-18-1038 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1038 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1038 && git push
```

---

**[Dev]** This session has had a huge impact on the drake language itself, and the changes to the source code generation to support it. I suggest it might be time update draco-dev-jpurnal after you review these latest results:

*(Pasted with it: `sbt test` green, 622 passed; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `c60a9ed`.)*

**[Claude]** Green at 622 and pushed as c60a9ed; every headline unchanged, so the run confirms both the step and the three `now` cases it forced. Agreed on the journal: this session has moved the language itself, from the call syntax through type forms to laziness. Invoking the journal skill you have for that.

---

## Status

This chapter transcribes the September 16–18, 2026 session — the build session that chapter 77's discussion queued, opened from `a5d2f5b` with the new-session prompt written at that session's close. The session's Claude Code identifier changed three times as it ran, so the record arrived as four extracts sharing one start time; this chapter is built from the longest, which contains the others, and one retry artifact ("The prompt I just sent failed (trying 8/10)") in a shorter fork is omitted. The journal request and the skill's own instruction text that answered it are the session's last two prompts; the closing commit-confirmation exchange may be appended when recorded. **Opening state (carried from chapter 77's close):** `a5d2f5b`, 605 tests / 43 suites green, 95 draco types in scope / 105 measured, `GenDrake` 93 of 93, surface losses 15 (expression form 12, empty-collection 3), the agreed order: calls → type-form trees → `ifThenElse`/`Presence`/`fold` → `DomainAspect`. **Commits landed, eleven, every one green before its push:** **`a8afcd8` "A call is f(a, b), positional then name:value, and the parameters/par call form retires"** (606/43) — one glued token split at depth-0 parentheses, commas and dots; `par = name`, the `.member parameters` chain lines and the `[ ]` argument brackets retired; thirteen corpus files converted, JSON and Scala unchanged; the loss headline rose to 114 because every parenthesised call now trees while the JSON still held ~105 host-opaque call strings; first run 604/2 on one hand-converted line (`loc line DomainLine(…)` missing its value type). **`8acd9c7` "The JSON corpus is re-canonicalized from the drake"** (606/43) — 40 JSON files rewritten by the compiled `DrakeCLI parse` over the exported classpath, three exclusions stated (BodyElement, ActorAspect, format/json/Value under #61); two factory defaults `Seq()` → `Seq.empty`, closing the empty-collection loss; losses 114 → 2. **`d59fe20` "A valueType is a JSON node, on the same terms as a value"** (606/43) — the family, Factory, Variable and Case declare `valueType Json`; `TypeForm.text` and the engine's `targetType` seam; 90 engine reads through `.text`; first compile found four sites the sweeps missed. **`f4113d2` "The drake parser trees the four type forms"** (607/43) — `typeForm` reading outside-in, three renderers (neutral, drake, Scala), the Morphic arrow **right-associative** after Dev's correction ("I should go with Haskell's precedence… I showed the wrong example"), the corpus re-canonicalized in the same commit because `ScenarioGenTest` asserts json == parse(drake); the loss report taught to compare a valueType whole; the elided factory type made a tree. **`49fc734` "A type parameter is a JSON node"** (607/43) — `TypeName.typeParameters: Seq[Json]`, identity comparing the nodes, spelled at each render site because identity is not rewritten in place. **`4de5b8a` "The parser trees the header's type parameters and a reference's arguments"** (608/43) and **`20835f0`** (608/43) re-canonicalizing the four definitions with bounded or compound parameters — the type-form arc closed, losses 5 (ActorAspect and Value, the exclusions). **`f779578` "Presence, Present and Absent are the first members of draco.drake"** (618/43) — the first parameterized sealed family: no host codec for a parameterized family (Dev: "it was never clear to me how much abstract types… need to implement support for the Codecs of concrete types" — left open for discussion), `Null` at `Any` when a parameter is typed by the type's own variable, the resource-overlay workflow that finds `Presence.json` for the derivation chain. **`4cdefb3` "fold is a dyn on Presence, defined by dispatch"** (620/44) — the eliminator as polymorphism, first member override of a parent `dyn`, `PresenceTest`. **`550a14e` "Evaluation is lazy by default, and now overrides it: body bindings become lazy val"** (621/44) — the two-run shape (parser first, regenerate with the compiled engine, run again), 14 main + 3 scenario + 7 example-domain projections regenerated, the actor whose rules went missing under a stub domain. **`c60a9ed` "Lazy by default, step 2: a method parameter is by-name, bound once by a prelude"** (622/44) — three runs: `targetTypes` had dropped `now`; Dictionary's `Map` operations and CLI's `main` meet host signatures; `TypeLoader`'s `readDefinition` and `rooted` are used as function values — `now` required in all three cases. **The discussion, transcribed verbatim and conducted one point per reply:** Dev's "Don't jump ahead of me too far on this idea, but what if we just added ifThenElse as a second dyn on Presence after dyn fold?"; the recursion-termination question; "What if lazy evaluation was the drake default and we had a way of overriding 'lazy' with 'eager'"; Haskell over Scala for syntax and semantics; call-by-need confirmed; `now` chosen over `eager`, on the element like `mut`; the build proposal in four steps. **Counts across the arc:** 605/43 → 606 → 607 → 608 → 618 → 620/44 → 621 → 622/44; types 95/105 → 98/108; `GenDrake` 93 → 96 of 96; losses 15 → 114 → 2 → 8 → 5 → 11 → 5, with the 118/247 "unaccounted" spike at the type-form commit's first run before the report compared a valueType whole; GEN MAP 28/20 throughout, dipping to 20/28 for one run of the lazy step. **Queued at the close:** lazy step 3, factory parameters by-name (127 parameters); `ifThenElse` as the second dyn on Presence, with how a Boolean condition becomes a presence the one open design point; Dev's question about abstract types and codecs, deferred to discussion; `draketarget.DomainAspect` and the aspect-named target types; `mut` moving to the element (the `mut {T}` tail); #61's lambdas and Value's two form-carrying values; the eleven drake-less mods definitions that drift only under the CLI check; the `AGENTS.md` copy and chapter 76's standing items. The next session opens from `c60a9ed`.
