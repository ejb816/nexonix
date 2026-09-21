# Draco Dev Journal — Chapter 80

**Session date:** September 20–21, 2026 (continues ch.79)
**Topic:** The second host-head group closing one accepted shape at a time, opening with the run that confirmed the ActorRef consumer form built at chapter 79's close (`2fcf58e`) — Java's `Consumer` leaving the two rule types for `(Knowledge -> Unit)` and `(RhsContext -> Unit)` with the literal `execute (action(_))` at the one call Evrete owns, after Dev stated the constraint and asked only that the correct Scala be generated (`d2b9cf6`); `[T]` confirmed as the lazy sequence with unboundedness in the value, Primes' `LazyList` becoming the declared symbols `progression` and `cons` (`8e04ee7`); the Java lists, where Dev corrected a consumer reading to "a sequenced values container for post hoc sorting", restated `mut` as the element kind, saw the gap that only a `par`'s type crosses a call, and proposed the mirrored-glyph form `[]+`, `{}+` with `()+` held for mutable trees (`c7c3f19`, two runs, the one miss Draco's import placement); `ifThenElse` as the second dyn on Presence with a Boolean reaching it as a presence of nothing through `guard` (`2897ee8`, 624 tests); TypeName's two paths as joins (`14e969a`, its push retried after a network failure); and the discussion of the empty package — the derivation slot carrying a type form for a foreign parent, the empty package being draco's default package, the nameless domain anchored by `.drake` and `.json` at the resource root (`5fd801d`, after a compile stop on the named-argument constructions the search had missed). Transcript.

---

**[Dev]** Accept, surface the shape for Consumer next

*(Pasted with it: `sbt test` green, 623 passed; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `2fcf58e` "An actor's reference parameter is a consumer of its message type, and sending is application", 40 files changed.)*

**[Claude]** 2fcf58e is in. Before shaping `Consumer` I need to see what the two rule types do with it: how a generated rule fills `pattern` and `action`, and whether anything hands them to Evrete as Java's interface or draco applies them itself.

Landed. Here is the shape for `Consumer`, for accept or correct.

**What it is.** `Rule` and `RuleType` type two members with Java's functional interface: `pattern Consumer(Knowledge)` and `action Consumer(RhsContext)`. Both arguments are already draco.rete wrap types, so the only host term in either slot is the interface itself. The generated rule fills each with a Scala lambda, and everything that uses them applies them: the actor's knowledge block and eleven test sites call `pattern.accept(knowledge)`, and the pattern hands `action` to Evrete's `execute`.

**The form.** The same one as the actor's parameter: `fix pattern (Knowledge -> Unit)` and `fix action (RhsContext -> Unit)`, in the two definitions and Rule's factory. Application replaces `accept`: `pattern(knowledge)`. The rule template's two lambdas are already lambdas, so their declared types change and nothing else in the body does.

**The one boundary.** Evrete's `execute` takes Java's `Consumer`, and a Scala function value does not convert to it on its own; a lambda literal does. So the template writes `.execute(action(_))`, once, inside the pattern, which is the same place the assembly converts a ref. No symbol is needed, and the `java.util.function.Consumer` import drops out of every rule projection.

| moves | count |
|---|---|
| definitions | 2 |
| engine template lines | 3 |
| rule projections | 23 |
| call sites, engine knowledge block and tests | 1 + 11 |

The projections are mechanical, so I would hand-apply them for one run with the two-run shape as the fallback, as in lazy step 3. Accept, and I build it.

---

**[Dev]** Consumer is used because evrete is written in java, and it is called from Scala, and evrete does not accept Scala idioms for lambdas in the calls to .execute()

**[Claude]** The generated rule already relies on that conversion in one place: its `action` is a Scala lambda literal assigned to Java's `Consumer[RhsContext]`, which Scala 2.12 and later convert at a Java functional interface, and it compiles today. What does not convert is a function *value* handed to `execute`. Rather than argue it, I'll type-check the exact boundary line against Evrete's `RuleBuilder` with the compiler from the classpath.

`RuleBuilder` lives elsewhere in Evrete's packages. I'll take the import from a generated rule and rerun the two probes separately, so the literal form's result stands on its own.

The literal form compiles against Evrete's builder. The value-form probe hit a shell quirk in the heredoc, not a Scala result, so one rerun for the record.

Both halves are now measured, and they say your point and my shape are the same fact seen from two sides.

Evrete's `execute` wants Java's `Consumer`, and a Scala function *value* is refused at that call, exactly as you say:

```text
found   : org.evrete.api.RhsContext => Unit
required: java.util.function.Consumer[org.evrete.api.RhsContext]
```

A lambda *literal* at that same call is accepted, because Scala converts a literal at a Java functional interface. The generated rule already depends on that: its `action` is a Scala lambda assigned to `Consumer[RhsContext]` today. So the draco form declares `action` as `(RhsContext -> Unit)` and the template writes `.execute(action(_))`, a literal, at the one call Evrete owns. That probe compiled clean against `RuleBuilder`.

What changes is where the Java interface is named. Today it is in the definition, in both rule types and every rule projection. Under the form it is named nowhere in drake and appears only at the Evrete call, in the engine's template, which is a Scala target matter. That is the same split as the actor's parameter: the member sees a function, the boundary converts.

Is the reservation about the boundary itself, or about the definition naming the host interface?

---

**[Dev]** I just need the correct source code to be generated into ScalaTarget, and I would prefer that Consumer not appear in drake or json as part of the type definition

**[Claude]** Understood: the target gets the correct code, and `Consumer` leaves the definition. Building it. First the drake and JSON for the two rule types, their regenerated projections, and the engine's knowledge-block template that emits `accept`.

Engine and sweep now. The template lines I have; the import list entry at 1757 I'll read before touching, and do it with the docs in the next pass.

Built and ready for your run.

**What changed.** `Rule` and `RuleType` declare `pattern (Knowledge -> Unit)` and `action (RhsContext -> Unit)`, both arguments draco.rete's own wrap types, so nothing in either slot is the host's. A pattern is applied, `pattern(knowledge)`, in the engine's knowledge block and in the tests. `Consumer` appears in no drake and no JSON.

**The Scala target does what you described.** The rule template writes `execute (action(_))`, a lambda literal at the one call Evrete owns, in [DracoGenerator.scala:1347](src/mods/scala/draco/DracoGenerator.scala:1347). The two probes are the reason it is written that way: a function value at `RuleBuilder.execute` is refused with "required java.util.function.Consumer", and the literal compiles. The `Consumer` import drops out of every rule projection.

| moved | count |
|---|---|
| definitions, re-parsed and regenerated by the compiled tools | 2 |
| rule projections, hand-applied to the template's form | 20 |
| call sites, generated actors and tests | 20 |

**Run.** Expected: 623 tests / 44 suites, all green, GEN MAP 28 match / 20 differ, every other headline unchanged. The engine template changed, so if `DracoGenTest` names a rule projection my hand-applied form differs from the engine's there, and the two-run shape follows.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-20-1020](draco-git-record/git-record-2026-09-20-1020).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-20-1020 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1020 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1020 && git push
```

Two heads remain in the second group: `LazyList` in Primes and the Java lists in Draco, Emitter and the two received-message types. Each gets surfaced before it is built.

*(Delegated action — Dev ran the handed-over commands: `sbt test` green, 623 passed; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `d2b9cf6` "A rule's pattern and action are functions, and Java's Consumer leaves the definition", 51 files changed.)*

---

**[Dev]** Accept, surface the shape for LazyList next

**[Claude]** d2b9cf6 is in. Reading the five `LazyList` uses in Primes before shaping, since they are the only ones and the value expressions matter as much as the type slots.

Landed. Recording that, then the shape's one deciding point.

All five `LazyList` uses are in one definition, `Primes`, and they are one thing seen from five slots: an unbounded sequence built lazily and consumed by `take`. The sieve `filter` conses a prime onto a recursive call over the tail, `naturals(start, step)` is the arithmetic sequence from a start, and the three finite results take from it into `[Int]`.

The point that decides the shape is what `[T]` already means. Drake evaluates by need, and Haskell's list is exactly that: `[Int]` holds `[2, 3 ..]` without a second type, and unboundedness is a property of the value, not the type. On that reading the definition never needed a second sequence form. All five slots are `[Int]`, and what remains is the Scala target's residue alone, because its `Seq` is strict. That residue is two boundaries, both already host text in the value lines: the unbounded arithmetic sequence, `LazyList.from(start, step)`, and the cons, `p #:: ...`. Each would become a declared symbol beside `join` and `presence`, spelled by the Scala target with `LazyList`, whose values are Seqs, so `head`, `tail`, `filter` and `take` on a `[Int]` keep working and stay lazy at run time. Nothing else in the corpus builds a sequence lazily, so nothing else moves.

The other reading is that draco owns an explicit lazy-sequence family in `draco.drake`, as it owns `Presence`, with `head`, `tail` and cons as dyns. That is a runtime decision of its own, and it would be the first place drake gave a type to something Haskell gives to `[T]` for free.

Is `[T]` the lazy sequence, with unboundedness in the value?

---

**[Dev]** Yes, `[T]` is the lazy sequence, unboundedness in the value

**[Claude]** Building. The sequence symbols need names, and `from` is drake's derivation keyword, so I'm calling the unbounded arithmetic sequence `progression(start, step)` and the cons `cons(x, xs)`. Both are yours to rename, once, before they harden. Drake first, JSON by the compiled parser, Scala by the compiled engine with the two symbols hand-rendered, and a compile probe of the result against the classpath.

The run probe threw and the tail of the trace hides the cause. I need its head.

Built, probed and ready for your run.

**What changed.** `[T]` is the lazy sequence, so Primes' five `LazyList(Int)` slots are `[Int]` and the definition names no host sequence. The two ways its values build a sequence lazily are declared symbols beside `join` and `presence`: `progression(start, step)`, the unbounded arithmetic sequence, and `cons(x, xs)`, whose tail is by need. The Scala target spells them with `LazyList` in [DracoGenerator.scala:205](src/mods/scala/draco/DracoGenerator.scala:205). Because a `LazyList` is a `Seq`, `head`, `tail`, `filter` and `take` on a `[Int]` dispatch to the lazy implementations at run time, and `LazyList.from` returns a lazy sequence unchanged, so the sieve's recursive tail is never forced by the constructor.

**Names, flagged once.** Both symbol names are mine. `from` was the natural name for the first and is drake's derivation keyword, so I chose `progression`; `cons` is the universal name for the second. Rename either before it hardens.

**Probe.** The new `Primes.scala` compiled against the exported classpath and ran. `nPrimes(10)` yields `2, 3, 5, 7, 11, 13, 17, 19, 23, 29`, and `naturals(2).take(5)` and `composites` behave as before. One definition and one projection move, with two rows in the engine's symbol table.

**Run.** Expected: 623 tests / 44 suites, all green, every headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-20-1042](draco-git-record/git-record-2026-09-20-1042).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-20-1042 | sed '1d' > /tmp/msg && git add -A -- src CHANGELOG.md draco-git-record/git-record-2026-09-20-1042 && git commit -F /tmp/msg -- src CHANGELOG.md draco-git-record/git-record-2026-09-20-1042 && git push
```

The last head in the second group is `java.util.List`, in Draco, Emitter and the two received-message types. I will surface its shape before building.

*(Delegated action — Dev ran the handed-over commands: `sbt test` green, 623 passed; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `8e04ee7` "[T] is the lazy sequence, unboundedness in the value: LazyList leaves the definition", 7 files changed.)*

---

**[Dev]** Accept, surface the shape for java.util.List next

**[Claude]** 8e04ee7 is in. Reading the Java list uses before shaping: what each one collects, who adds to it, and who reads it back.

Landed and recorded. The shape for the Java lists turns on one reading.

**What the lists are.** Four of the five are the same thing: a collector the actor's caller supplies, seeded into the rule session by name, that a rule action fetches back and `add`s to, and that the caller reads after firing. `Draco` collects `Problem`s and `CollectProblems` adds them; `Emitter` collects `Emission`s and `EmissionReceived` adds them; the scenario's `RootInterface` and `BirchAlarmReceived` do the same for jasmonates. The list is Java's because it is mutable and shared by identity across the session boundary.

**The reading.** Seen from the rule, a collector is not a list. It is somewhere to send what the rule produced, which is exactly what an actor's peer was: a consumer of T. So `Draco`'s parameter is `par problems (Problem -> Unit)`, the rule's local is `loc received (Emission -> Unit)` and its action is `received(emission)`, and the caller keeps the buffer and hands in `buffer += _`, the same boundary conversion the assembly does with a ref. No new symbol, no host name in any of the four definitions, and the same form as `ActorRef` because it is the same concept: a sink for a message type.

**The fifth is different.** Aerial `Consumer` owns its collector as a member, `fix consumed java.util.ArrayList(String)`, initialized in the definition rather than supplied. That is a mutable container the type itself holds, which is what `mut` on an element was agreed to mean on 2026-09-13 and has not been built. I would leave it for that step rather than force it into the sink form.

Is a collector a consumer of T, the caller keeping the buffer?

---

**[Dev]** No not a consumer, a sequenced values container for post hoc sorting into to an indexed table.

**[Claude]** Understood, and that changes the form. A sequenced container: values arrive in firing order, are kept in that order, and are sorted into an indexed table afterwards, outside the rule. So the definition holds a sequence, `[T]`, and the action appends to it; nothing in the definition is a function.

What the Java list adds to a sequence is only that it can grow in place across the session boundary. That is the property the agreement of 2026-09-13 gave to `mut` on an element: an immutable value type becomes a reassignable binding, a container type becomes the target's mutable container. On that reading the four collectors are `mut [T]`, the action's `received.add(x)` is an append to a `mut [T]`, and the caller reads the sequence after firing and sorts it. `mut` on a container is agreed but not built, and `mut {T}` still rides as host text, so this head would be the step that builds it, for `[T]` first.

Is the container `mut [T]`, with append as its one operation in the rules?

---

**[Dev]** I thought "mut" was an type element where the semantics of mutability was determined by whether the value type was a collection type or just a reference type for the name.

**[Claude]** Yes, and that corrects my phrasing. `mut` is the element kind, `Mutable`, alongside `fix`, `dyn` and `loc`. The value type never carries it. `mut count Int` is a reassignable binding, because `Int` is a reference to a value; `mut received [Emission]` is the target's mutable sequence, because `[T]` is a collection. The kind decides that a thing can change, and the value type decides what changing means. The `mut {T}` still riding as host text in Accumulator is the leftover from before that reading, and it retires when the element form is built.

That puts one question in front of the collectors. Under the element reading, the container is mutable because the element that *declares* it is `mut`. Today no drake element declares it: the caller builds a Java list, hands it in through a `par`, and a rule's `loc` picks it up from the session and appends. `par` and `loc` are references to something declared elsewhere, and elsewhere is currently a test.

So the question is which element owns the container. One answer is the actor: `Draco` declares `mut problems [Problem]` as its own element, empty by default, its rules append, and whoever spawned it reads the sequence afterwards. The other is that the container stays the caller's and `par problems [Problem]` is a reference to a mutable sequence the caller declared, with the mutability visible only where it is declared.

Which element declares the collector?

---

**[Dev]** I don't know if drake provides enough information for the generator to know what to generate if some caller declares a mut element and a completely independent type definition that generates the function being called knows that the "par problems [Problem]" is a mutable collection, unless mutable sequence derives from collection mixed with mutable.

**[Claude]** That is the gap, and I think it is real. A `par` is typed, and only its type crosses the call. `par problems [Problem]` says a sequence, and nothing in `[Problem]` tells the generator, or a reader, that the callee will append to it. The `mut` on the caller's element is invisible from inside the callee. So for a collector to be handed in, its mutability has to be in the type, not only in the kind of the element that declared it.

Your closing clause is the way through, and it is also what Haskell does. There, a name can never be reassigned, and mutability lives entirely in types: `IORef a` for a cell, a mutable vector for a container. So a mutable sequence is a type in its own right, and your derivation is exactly its definition: it derives from the sequence and mixes in mutable. The Scala target already agrees, because its mutable buffer *is* a `Seq`, so a `par` typed by the mutable sequence can be read as a sequence everywhere and appended to where the type allows.

On that reading `mut` splits into two things that were one word. The element kind `mut x Int` remains the reassignable name, which is a binding matter and stays with the element. A mutable container is a type in `draco.drake`, beside `Presence`, deriving `[T]` with a mutable mixin, and a `mut x [T]` element would then be shorthand for declaring `x` with that type. The collectors become `par problems <that type>(Problem)`, the rule's `loc received` names the same type, and `append` is the type's one operation.

Does the mutable container belong in the type, as a `draco.drake` member deriving the sequence with a mutable mixin?

---

**[Dev]** So far,  in drake, all fundamental collection types are represented by pairs of mirrored glyphs: "()", "[]", "{}" and maybe "<>", later. When we want to indicate mutable versions of those types we could use "()+", "[]+, "{}+" where ever those collection types are declared. Then we can continue to use  "fix" and "mut" for the generator to determine when the target language equivalent of "val" and "var" needs to be generated.

**[Claude]** That reads cleanly, and it puts the two things where they belong. Let me restate it with the case in hand before building on it.

The bracket pair names the collection, and a trailing `+` names its mutable version, wherever a type is written: `[T]+` a mutable sequence, `{T}+` a mutable set, `{K, V}+` a mutable map. The kind keywords say nothing about the container any more: `fix x [T]+` is a name bound once to a sequence that can grow, `mut n Int` is a name that can be rebound, and `mut x [T]+` is both. So the collectors become `par problems [Problem]+` in Draco, `loc received [Emission]+` in the rule, and `append` is what the `+` grants. The Scala target spells `[T]+` as its mutable buffer, which is a `Seq`, so a `[Problem]+` still reads as a `[Problem]` wherever only reading is needed; a Haskell target spells it as whatever mutable container it has, and the definition does not care which. Accumulator's `mut {Int}` host text becomes `{Int}+` and the last host-text type form leaves the corpus with it.

In the carrier, the `+` rides the node key the encoding fix introduced: `{"[]+": [T]}`, `{"{}+": [K, V]}`. No new head, no host name, and the parser and emitter learn one character.

The one thing I would leave undefined for now is `()+`. A tuple is a fixed shape, not a container one appends to, and neither target has a mutable tuple. Does `+` apply to the collection brackets only, with `()` left out until a use appears?

---

**[Dev]** Yes, collection brackets only, leave () out for now. However ()+ gives me the idea of saving that for mutable trees with the potential of having mixed mutable and immutable tree types and tree type instance values.

**[Claude]** Recorded as your idea for later: `()+` held for mutable trees, with mixed mutable and immutable tree types and instances. Before I put the build shape in front of you I need the one `mut {T}` that exists and how the tests supply and read the collectors.

I have what I need. Here is the build shape, for accept or correct.

**Surface and carrier.** `[T]+`, `{T}+` and `{K, V}+` wherever a type is written; `()` untouched. The parser reads the `+` after the closing bracket, the emitter writes it back, and the node key carries it: `{"[]+": [T]}`, `{"{}+": [K, V]}`. The Scala target spells them as its mutable buffer, set and map, and an empty literal `[]` or `{}` in a `+` slot as the mutable empty. No host name is needed anywhere.

**The one operation.** A declared symbol `add(xs, x)` beside `join`, `presence`, `progression` and `cons`, spelled by the Scala target as `xs += x`, which is the same call on a buffer and on a set. The name is mine, chosen because `append` already means Haskell's `++` in drake, which is concatenation and not in place; rename it if you prefer.

**What moves in the corpus.**

| definition | now | becomes |
|---|---|---|
| Draco | `par problems java.util.List(Problem)` | `par problems [Problem]+` |
| Emitter | `par received java.util.List(Emission)` | `par received [Emission]+` |
| EmissionReceived, BirchAlarmReceived | `loc received java.util.List(...)`, `received.add(x)` | `loc received [...]+`, `add(received, x)` |
| CollectProblems | one host-text line reading the list and adding | `loc problems [Problem]+ ...get("problems")`, `add(problems, p)` |
| Accumulator | four `mut {Int}` host-text slots with `mutable.Set[Int] ()` values | `{Int}+` with `{}` |
| aerial Consumer, ConsumeReport | `java.util.ArrayList(String)` and its host-text add | `[String]+` with `[]`, `add(consumed, ...)` |

Every Java collection name leaves the corpus. The four tests that build a collector build a buffer and read it as a sequence, without the Java conversion.

**Engine.** Parser, emitter and `TypeForm` learn the `+`; the target's type spelling gains three rows and the empty-literal rule; the symbol table gains `add`. That is an engine change, so the two-run shape applies, with the projections hand-applied first as before.

Accept, and I build it.

---

**[Dev]** Accept, build the mutable collection form

**[Claude]** Building. First the reads I need before touching anything: how the engine renders an initializer and adds the `mutable` import, the two rule actions that read collectors through host text, the test that seeds aerial Consumer's collector, and an empirical check of how the compiled tokenizer treats `[String]+`.

Placing the `mutable` import in each of the seven projections that gain one, after the framework imports where the engine's external block sits, then the docs, record and memory.

Built and ready for your run. This one is the two-run shape: the engine changed in several places and eight projections are hand-applied.

**The form.** `[T]+`, `{T}+` and `{K, V}+` wherever a type is written, carried as the node keys `[]+` and `{}+`; `()+` reserved for your mutable trees. `fix` and `mut` speak only of the name. The Scala target spells a mutable buffer, set or map, an empty `[]` or `{}` in a `+` slot as the mutable empty, and the one operation, `add(xs, x)`, as an in-place add. The name `add` is mine, chosen because `append` is already Haskell's concatenation in drake; rename it if you prefer.

**Corpus.** Draco, Emitter, EmissionReceived, CollectProblems and the scenario's BirchAlarmReceived carry `[T]+` collectors and `add`; Accumulator's four `mut {T}` slots are `{Int}+` and `{(Long, String)}+` with `{}` values, which retires the last host-text type form and the parser's `mut {` branch with it; aerial Consumer and ConsumeReport follow. No `java.util` name remains anywhere in the corpus. The four tests that build a collector build a buffer and read it as a sequence.

**Engine.** Parser, emitter and `TypeForm` read and write the `+`; the type spelling gains three rows; the empty-literal rule and the `add` symbol are new; and two import paths learned to see a `mutable` type inside a rule or actor action body, which no path did before. The `mutable` import is placed in each projection where the engine's sorted block will put it, but that is the one guess a second run would correct.

**Run.** Expected: 623 tests / 44 suites, all green, every headline unchanged, GEN MAP 28 match / 20 differ. If `DracoGenTest` names a file, paste the Failed block and I regenerate those with the compiled engine.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-20-1329](draco-git-record/git-record-2026-09-20-1329).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-20-1329 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1329 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1329 && git push
```

With this, every host head in the second group has a drake form. What remains host-spelled in the corpus is the leaves: `String`, `Int`, `Json`, `URI`, and the evrete and pekko types. The parked thread is `ifThenElse` and its Boolean-to-Presence bridge, and behind it Dictionary's foreign `Map` reference from the scratch-module discussion.

*(Delegated action — Dev ran run 1: `sbt test` 622 passed, 1 failed — draco.DracoGenTest, `draco/Draco.json: Generator output matches draco/Draco.scala (whitespace-normalized) *** FAILED ***`; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48); drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back.)*

---

**[Dev]** That was a very complex change. I really appreciate the way it failed, intentionally.

**[Claude]** One file, exactly the case the two-run shape exists for. The compiled engine from this run is the new one, so I regenerate `Draco.scala` over the full overlay and look at what differed from my hand-applied form.

Run 1 did what it was for. Everything compiled and matched except the one thing I had to guess: where the composed actor-and-domain projection places its import block. Draco puts the external import first, before the pekko imports, where a plain actor sorts it after the evrete one. `Draco.scala` is now the engine's own output, and I verified the other seven hand-applied projections are byte-equal to what the compiled engine writes.

**Run 2.** Expected: 623 tests / 44 suites, all green, every headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record carries run 1's result and is [git-record-2026-09-20-1329](draco-git-record/git-record-2026-09-20-1329).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-20-1329 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1329 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1329 && git push
```

*(Delegated action — Dev ran run 2: `sbt test` green, 623 passed; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `c7c3f19` "Mutable collections are [T]+, {T}+ and {K, V}+, and the Java lists leave the definition", 35 files changed.)*

---

**[Dev]** Accept, surface the Boolean-to-Presence bridge for ifThenElse next.

**[Claude]** c7c3f19 is in. Recording it, then the corpus's remaining conditionals as they stand now, since the loader path has changed under them.

Five conditionals remain, and they fall in two groups. TypeName's two, in `namePath` and `resourcePath`, are the empty-path special case, which dissolves into a `join` under your reading that the empty path is the scratch module; they never reach `ifThenElse`. The other three are pure predicates: `rooted` tests a derivation with `exists` and `||`, and the two in `hostRoots` test a file name's suffix. Each yields a truth and carries nothing else.

That fixes what the bridge has to do. `ifThenElse` dispatches on a Presence, so a condition that carries nothing must be a presence of nothing: `Present(())` when the predicate holds, `Absent` when it does not, a `Presence(Unit)`. Haskell has exactly this function, `guard`, which takes a truth and yields the empty presence or the absence. So the bridge is one declared symbol beside `presence`, `progression`, `cons` and `add`, spelled by each target from its own Boolean, and `rooted` would read `guard(<predicate>).ifThenElse(td, TypeDefinition(...))`, with both branches lazy because the parameters are. No keyword, no second dyn beyond the one you proposed, and the host predicate stays host text behind the symbol, as the circe read stays behind `presence`.

Is the Boolean's presence a presence of nothing, `Presence(Unit)`, bridged by one symbol from the truth?

---

**[Dev]** Yes, a presence of nothing, bridged by guard

**[Claude]** Agreed and building. Reading the Presence family and every place the dlt already speaks of `ifThenElse`, then the trio.

Compiles and round-trips. What remains is independent: the dlt symbol and status, DRACO.md's count and domain line, the CHANGELOG entry, the record and memory.

Built, probed and ready for your run.

**What changed.** `ifThenElse` is the second dyn on the family: `dyn ifThenElse(R) R` with a then-branch and an else-branch, abstract on `Presence`, the then-branch in `Present`, the else-branch in `Absent`. No keyword and no conditional anywhere in it. Both parameters are by need, so the branch not taken is never evaluated, which is what the whole laziness arc was for; `PresenceTest` asserts it and the suite grows to 624.

**The bridge.** `guard(b)` is a declared symbol beside `presence`, `progression`, `cons` and `add`: a truth as a presence of nothing, spelled by the Scala target as an `if` into `Present[Unit]` or `Absent[Unit]`, both explicit so the expression types as a `Presence[Unit]`. `TypeLoader.rooted` is the first use, `guard(<predicate>).ifThenElse(td, TypeDefinition(...))`, with the predicate staying host text behind the symbol as the circe read stays behind `presence`. Its `now par td` stays, since `rooted` is still passed to `fold` as a function value.

**The other four conditionals.** TypeName's two are the empty-path special case and dissolve into a `join` under the scratch-module reading, which is the natural next step together with Dictionary's foreign `Map` reference. DefinitionPath's two sit inside a lambda the parser does not tree and wait on the parser.

**Trio.** Drake first, JSON by the compiled parser, the Presence family by the compiled engine, `TypeLoader.scala` by the compiled engine with `guard` hand-rendered to the new row, and both compiled together against the classpath before handover.

**Run.** Expected: 624 tests / 44 suites, all green, every headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-20-1339](draco-git-record/git-record-2026-09-20-1339).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-20-1339 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1339 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1339 && git push
```

*(Delegated action — Dev ran the handed-over commands: `sbt test` green, 624 passed; GenDrake runs: 96 of 96 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; drake surface losses: 5 fields across 108 types [expression form (string vs tree): 2] [type form (string vs tree): 3]; Drake.parse scope: 98 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back; committed and pushed as `2897ee8` "ifThenElse is dispatch on Presence, and a Boolean reaches it as a presence of nothing through guard", 16 files changed.)*

---

**[Dev]** Accept, surface the shape for TypeName's namePath as join next.

**[Claude]** 2897ee8 is in. The two lines as authored, before I shape them.

Landed. The shape, with the one thing it touches beyond TypeName.

**The two lines.** Both spell the empty path as a special case, which under your reading it is not:

```text
fix namePath String if (namePackage.isEmpty) name else s"${namePackage.mkString(".")}.${name}"
fix resourcePath String if (namePackage.isEmpty) s"/$name.json" else s"/${namePackage.mkString("/")}/$name.json"
```

With the segments and the name as one sequence, each is a single `join`, in drake's own notation and with nothing host-opaque left in the value:

```text
fix namePath String join(".", namePackage ++ [name])
fix resourcePath String "/" ++ join("/", namePackage ++ [name ++ ".json"])
```

An empty path contributes no segments, so `namePath` is the bare name and `resourcePath` is `/Name.json`, exactly what the special case produced. The two conditionals go, the two interpolated host strings go, and the values become trees the parser already builds: a call, a `++` run and a `[]` literal.

**One engine detail.** `join` renders its sequence as `xs.mkString(sep)`, which binds wrongly when `xs` is a `++` run. The row parenthesizes a compound operand, which is the whole engine change.

**The one thing to flag.** Nothing in this step changes what an empty package *means*, only how it is spelled. Eight sites still read an empty package as "foreign, outside every draco domain": `rooted` in the loader, the emitter's choice of a foreign reference for Dictionary's `{K, V}`, and six in the engine's reference spelling. Under the scratch-module reading those are the next discussion, because a bare reference into the scratch module and a reference to a host type would then need telling apart. This step leaves them exactly as they are.

Accept, and I build it as one run.

---

**[Dev]** Accept, build the namePath join

**[Claude]** Building. First an empirical check the shape depends on: whether the parser already trees a non-empty `[name]` in value position, since `join(".", namePackage ++ [name])` needs it, and the parser's `treed` where the branch would go.

The parser returns `[name]` as an opaque leaf, so a non-empty sequence literal in value position is the one parser addition this needs. Reading `value` and `treed` to place it.

Built, probed and ready for your run.

**What changed.** TypeName's two paths are joins: `namePath` is `join(".", namePackage ++ [name])` and `resourcePath` is `"/" ++ join("/", namePackage ++ [name ++ ".json"])`. An empty path contributes no segments, so the two conditionals and their interpolated host strings are gone and both values are trees. I compiled the new `TypeName.scala` against the classpath and ran it: a bare name gives `X` and `/X.json`, a qualified one gives `draco.format.json.Value` and `/draco/format/json/Value.json`, so every resource the loader resolves is unchanged. TypeName round-trips under the compiled checker.

**Two small pieces underneath.** The parser returned a non-empty literal like `[name]` as an opaque leaf, which I found by probing before authoring, so `treed` now reads a token that is one bracket group with nothing before it as the `[]` or `{}` node with its members. And `join` parenthesizes a compound operand, since `mkString` on a `++` run would bind to its last piece alone. The structural type-form test asserts the literal and its Scala spelling; the count stays at 624.

**Not touched, deliberately.** Eight sites still read an empty package as "foreign, outside every draco domain": `rooted`, the emitter's foreign spelling of Dictionary's `{K, V}`, and six in the engine's reference spelling. This step changed how the empty path is spelled, not what it means. Telling a scratch-module reference from a host-type reference is the next discussion.

**Run.** Expected: 624 tests / 44 suites, all green, every headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-20-1347](draco-git-record/git-record-2026-09-20-1347).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-20-1347 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1347 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1347 && git push
```

*(Delegated action — Dev ran the handed-over commands: `sbt test` green, 624 passed, every headline unchanged; the commit landed as `14e969a` "TypeName's paths are joins: the empty package is a module like any other, not a special case", 10 files changed, but the push failed — `fatal: unable to access 'https://github.com/ejb816/nexonix.git/': Could not resolve host: github.com`; a second attempt of the same chain reported "Your branch is ahead of 'origin/main' by 1 commit … nothing to commit, working tree clean".)*

---

**[Dev]** I did the second command before I got network access. Please give me a new command to fix what was missed in the second attempt of the git commands, as shown in the output:

**[Claude]** The commit itself landed the first time: 14e969a is on your local main, and the only thing that failed was the push, because there was no network. The second attempt then correctly found nothing to commit, and because the chain is joined with `&&`, the failed commit step stopped it before the push. Nothing is missing or duplicated; let me confirm the state.

Exactly one commit is waiting, 14e969a, and the working tree is clean. All that was missed is the push:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git push
```

Once that reports `2897ee8..14e969a  main -> main`, the tree is where the record says it is, and the next discussion is what an empty package means to the eight sites that read it as foreign.

---

**[Dev]** Accept, surface the shape for the empty package as foreign next

**[Claude]** Let me measure how many references actually carry an empty package before shaping anything, since the answer decides whether this is a corpus problem or a single case.

Zero references with an empty package, which contradicts what I said about Dictionary. I need to see how its `from {K, V}` is actually carried, and what `operatorCarried` and `isRoot` test.

The count was wrong because the carrier elides an empty `namePackage` rather than writing it, and my check looked for the key. Corrected, the corpus holds exactly one reference with an empty package: Dictionary's parent, `Map`. That makes the shape small, and it turns on one observation.

**On the surface, the two are already told apart.** A draco parent is written as a name, `from Presence(T)`. A foreign parent is written as a type expression, `from {K, V}`, and the emitter's `operatorCarried` decides "foreign" by exactly that: the reference spells as an operator, not a name. Nothing on the surface says "empty package"; a bare name into the scratch module would simply be a name, distinct from `{K, V}` by its shape.

**Only the carrier conflates them.** `derivation` is a list of TypeNames, so the foreign parent is stored as a TypeName called `Map` with no package, and that absence is what the eight sites test. The name `Map` is a host name in the normative corpus, the same defect the collection sugar had, and the empty package is doing the work the shape already does.

So the shape is that a foreign parent is carried as what it is on the surface, a type form: Dictionary's derivation becomes the node `{"{}": ["K", "V"]}`, the same node the encoding fix gave every `{K, V}` value type, and a draco parent stays a TypeName. "Foreign" then means "a form, not a name", the eight sites test the shape instead of the package, and an empty package is free to mean the scratch module. The cost is structural: `derivation` holds either a name or a form, which is a change to a TypeDefinition field, and I am surfacing it rather than building it for that reason.

Should the derivation slot carry a type form for a foreign parent, with the package saying nothing about foreignness?

---

**[Dev]** Yes, the derivation slot carries a type form for a foreign parent. I may be misinterpreting "foreign parent", however. The interpretation for "empty package"  in draco is what is (or was) called the default package in Java.
In draco the empty package would be the the package of the empty domain. If I were to give the empty domain a type name that could be the name part of a Scala object declaration, that name would hav to be "double backquote" (or ``). I'm not sure if that (or a lexical equivalent) works for other languages beyond Scala, or if would work for file name.

**[Claude]** Agreed on the derivation slot, and your reading of "foreign parent" is the one I meant. Foreign is a parent that lives in no draco domain at all, the host's `Map` behind Dictionary's `{K, V}`. A type in the empty domain is not foreign; it is a draco type whose path has no segments, and after this change nothing will confuse the two, because a foreign parent is a form and a draco parent, wherever it lives, is a name.

The empty domain, on your reading, then has exactly the shape Java's default package has, and I think the naming question answers itself the same way it does there. Java never names the default package; a class in it is referred to by its bare name and the package is the absence of a prefix. Draco's empty domain is the domain whose path is the empty sequence of segments, and its identity is that path, not a name. It needs no companion object, so the backquoted empty identifier never has to exist, and Scala would not accept it anyway, since a backquoted identifier cannot be empty. It needs no definition file either, so there is no empty file stem to fight, and the same holds for any other target and for the file system. Its members are named and projected as any type is; only their container is anonymous, which is what makes it a scratch module rather than a real one: nothing permanent can refer to a domain that has no name, and that is a property to keep, not a gap to fill.

So the shape has two halves. Dictionary's derivation becomes a type form, and the eight sites stop reading the empty package as foreign. And the empty domain stays nameless, with `domain` and no segments as its spelling, and no dictionary entry, no companion and no file for the container.

Does the empty domain stay nameless, with the empty path as its whole identity?

---

**[Dev]** Yes, the empty domain stays nameless, build the derivation form. Files name ".drake" and  ".json" should exist in the empty package directory to support the type definition for the empty domain, but that's is just an anchor for the generator and emitter.

**[Claude]** Building the derivation form first; the anchor files come with it once I know what the tools will do with them. The derivation slot is read in many places, so I'm measuring its consumers before choosing how a form sits beside the names.

Applying everything I have exact text for now, and reading the two spots I still need for the nameless anchor: how the emitter assembles the header line, and the reference reader's foreign branch.

All that remains is the nameless anchor in the parser and emitter, the dlt's references section, and a last compile probe of the emitter; none depends on another, so one pass.

Built, probed and ready for your run. This one is the two-run shape.

**What changed.** The derivation slot carries each parent as what the surface writes: a draco parent as its name, a foreign one as a type form. `DracoAspect.derivation` is `[Json]`, and two drake globals on DracoAspect, `parents` and `foreignParents`, read the two apart. Every reader of a parent's name now goes through `parents`: the loader's `rooted`, the rule `DerivationResolvable`, the engine's chain walk, extends clause, codec gate, actor message type, imports and composed merge, the domain builder, the assembly validator, three tests and four scripts. Dictionary's parent is the node `{"{}": ["K", "V"]}` and its projection still reads `extends Map[K, V]`, because the form's Scala spelling is the old spelling. No site reads an empty package as foreign any more, and the emitter's `foreignReference` and `operatorCarried` are gone with that reading.

**The nameless domain.** `src/main/resources/.drake` holds the one line `domain`, and `.json` holds what the encoder writes for a nameless definition, `{"typeName": {"name": ""}}`, since an empty self-loop elides like any absence. The parser accepts a definition whose first word is `domain`, the emitter writes it back, and I ran both through the new code: `domain` parses to the empty name and emits as `domain`, and Dictionary parses to the form and emits `from {K, V}`. No test walker reaches the resource root, so the anchor is inert to the gates, as you intended.

**Probe.** DracoAspect, TypeLoader, DerivationResolvable, Drake, DracoGenerator, DomainBuilder and AssemblyValidator compiled together against the exported classpath, clean. The compiled checker cannot judge this commit, since its decoder and header rule are the old ones, so the run is the judge.

**Run.** Expected: 624 tests / 44 suites, all green, every headline unchanged. If `DracoGenTest` names a file, paste the Failed block and I regenerate it with the compiled engine.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-20-1942](draco-git-record/git-record-2026-09-20-1942). The two anchor files are under `src`, so the pathspec carries them.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-20-1942 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1942 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1942 && git push
```

With this, every host head in the corpus has a drake form and the empty package has its meaning. What remains host-spelled is the leaves, `String`, `Int`, `Json`, `URI` and the evrete and pekko types, and the two conditionals inside DefinitionPath's lambda, which wait on the parser treeing lambdas.

*(Delegated action — Dev ran run 1, at 22:20 local: the compile stopped at `src/main/scala/draco/Generated.scala:17:18: type mismatch; found: draco.TypeName, required: io.circe.Json` on `TypeName ("Main", _namePackage = Seq ("draco"))`, one error, `(Compile / compileIncremental) Compilation failed`.)*

One hand-written file constructs a `DracoAspect` with `_derivation = Seq(TypeName(...))`, which my search missed because it uses the named argument rather than reading the slot. Fixing it and searching for every other construction of that kind.

Run 1 failed to compile, and the cause was my search, not the design. I looked for every place that reads the derivation slot and missed every place that constructs it: Generated.scala, hand-written core infrastructure with no definition, and twenty-one sites in four tests all pass a `Seq` of TypeNames by named argument. Each now passes their encodings. `TypeDefinitionTest`'s hand-spelled `Seq[TypeName]` slots describe a definition it only encodes and generates, never compares with the loaded one, so they stand. The lesson is in the record and in memory: when a slot's type changes, grep its named-argument construction as well as its reads.

**Run 2.** Expected: 624 tests / 44 suites, all green, every headline unchanged. The two-run shape still applies for `DracoGenTest` beyond this.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-20-1942](draco-git-record/git-record-2026-09-20-1942).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-20-1942 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1942 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1942 && git push
```

*(Delegated action — Dev ran run 2 on the morning of September 21: the main stage compiled, and the test stage stopped on `ScenarioDrakeTest.scala:91`, "not found: value DracoAspect".)*

One more compile stop, in the test stage this time: `ScenarioDrakeTest` lives in package `scenario` and does not import `draco.DracoAspect`. Checking both scenario tests' imports and fixing.

The main stage compiled this time, and the test stage stopped on one file: `ScenarioDrakeTest` sits in package `scenario` and imports only `Drake` and `TypeDefinition` from draco, so its `DracoAspect.parents` is now qualified. I checked every other use, and each is either in package `draco` or under `import draco._`.

**Run 3.** Expected: 624 tests / 44 suites, all green, every headline unchanged. From here the only thing left for a run to catch is a projection `DracoGenTest` names, which the two-run shape covers.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-20-1942](draco-git-record/git-record-2026-09-20-1942) and carries both compile stops.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-20-1942 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1942 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-20-1942 && git push
```

*(Delegated action — Dev ran run 3: 624 tests / 44 suites green, every headline unchanged; committed and pushed as `5fd801d`, 32 files, `src/main/resources/.drake` and `.json` created.)*

Landed at 5fd801d. Recording it and correcting the one memory line that still said an empty package means foreign.

5fd801d is in, 624 tests / 44 suites, main level with origin, and the memory notes carry it.

That closes the arc that began with `Map` in Dictionary's slot on the 18th. Every host head that stood in the corpus now has a drake form: the collection sugar and its mutable versions, Presence for the host's option, a consumer for an actor's peer, functions for a rule's pattern and action, the lazy sequence, and now a type form for a foreign parent, with the empty package meaning the nameless domain rather than "foreign". What remains host-spelled is the leaves, `String`, `Int`, `Json`, `URI` and the evrete and pekko types, and the two conditionals inside DefinitionPath's lambda, which wait on the parser treeing lambdas and infix operators under GitHub #61.

Two small residues are worth naming so they are not forgotten. The parser still carries `foreignRef` and `opensTypeExpression`, the former now only building names no one reads; it can go when the parse branch is next touched. And the three rule conditions de-treed by the September sweeps are still strings, which the same parser work would restore.

If you want to continue, the natural next step is the parser, since three open threads now wait on it. Otherwise this is a clean stopping point for the journal.

---

## Status

This chapter transcribes the September 20–21, 2026 session, the build session that continued chapter 79's closing of the host-head groups. It opens with the run that confirmed the ActorRef consumer form built at chapter 79's close, and runs to the commit that gave the empty package its meaning. Every increment was surfaced as one recommendation, accepted or corrected by Dev in discussion, and built as a trio drake-first with the JSON by the compiled parser and the Scala by the compiled engine, hand-applied projections taking the two-run shape where the engine itself changed. **Opening state:** `20778f3` plus the ActorRef consumer form awaiting its run, 623 tests / 44 suites, 98 draco types in scope / 108 measured, `GenDrake` 96 of 96, surface losses 5 (expression form 2, type form 3). **Commits landed, seven, every one green before its push:** **`2fcf58e` "An actor's reference parameter is a consumer of its message type, and sending is application"** (10:08, 623/44, 40 files) — the six actor-minting parameters typed by the host's `ActorRef(M)` become `(M -> Unit)`, a send is application, the assembly converts each ref once at its boundary; built at chapter 79's close, confirmed here. **`d2b9cf6` "A rule's pattern and action are functions, and Java's Consumer leaves the definition"** (10:36, 623/44, 51 files) — `pattern (Knowledge -> Unit)`, `action (RhsContext -> Unit)`; Dev stated the constraint ("evrete does not accept Scala idioms for lambdas in the calls to .execute()") and two compiler probes measured it: a function value is refused at `RuleBuilder.execute`, a lambda literal converts, so the Scala target writes `execute (action(_))` at the one call Evrete owns; twenty projections lose the import, twenty call sites apply instead of accept. **`8e04ee7` "[T] is the lazy sequence, unboundedness in the value: LazyList leaves the definition"** (10:43, 623/44, 7 files) — Dev confirmed `[T]` as Haskell's list; Primes' five slots become `[Int]`, with `progression(start, step)` and `cons(x, xs)` as declared symbols the Scala target spells with `LazyList`; names flagged once as Claude's, `from` being the derivation keyword; `nPrimes(10)` run against the classpath before handover. **`c7c3f19` "Mutable collections are [T]+, {T}+ and {K, V}+, and the Java lists leave the definition"** (13:35, 623/44, 35 files, two runs) — the discussion that shaped it, one point per reply: Dev corrected the consumer reading ("a sequenced values container for post hoc sorting into to an indexed table"), restated `mut` as the element kind whose meaning the value type decides, saw that only a `par`'s type crosses a call ("unless mutable sequence derives from collection mixed with mutable"), and proposed the mirrored-glyph form `()+`, `[]+`, `{}+` with `fix` and `mut` left to decide `val` and `var`; `()+` reserved for mutable trees of mixed mutable and immutable types and instances; `add(xs, x)` the one operation; Accumulator's `mut {T}` host text retired with the parser's `mut {` branch; run 1 failed exactly one file, Draco's import placement in the composed actor-and-domain projection, and the other seven hand-applied projections were byte-equal to the engine's. **`2897ee8` "ifThenElse is dispatch on Presence, and a Boolean reaches it as a presence of nothing through guard"** (13:41, **624/44**, 16 files) — the second dyn on the family, then-branch in Present, else-branch in Absent, both by need so the branch not taken is never evaluated; `guard(b)` a declared symbol into `Present[Unit]` or `Absent[Unit]`; `TypeLoader.rooted` the first use, its `now par td` kept because it is still passed as a function value; `PresenceTest` grows by one. **`14e969a` "TypeName's paths are joins: the empty package is a module like any other, not a special case"** (18:36, 624/44, 10 files) — `namePath` is `join(".", namePackage ++ [name])`, `resourcePath` is `"/" ++ join("/", namePackage ++ [name ++ ".json"])`; the parser learned a non-empty `[]`/`{}` literal in value position, `join` parenthesizes a compound operand; the push failed for want of a network and was retried as a bare `git push` after Claude confirmed one commit waiting and a clean tree. **`5fd801d` "A foreign parent is a type form, and an empty package is the nameless domain, not a foreign mark"** (September 21, 09:31, 624/44, three runs, two compile stops) — the discussion: the corpus holds exactly one empty-package reference, Dictionary's `Map`, and the surface already tells a name from a form; Dev read the empty package as Java's default package and asked about naming the empty domain (the backquoted empty identifier); Claude's answer that the empty path is the whole identity and no companion, dictionary entry or name is needed; Dev: "the empty domain stays nameless … Files name '.drake' and '.json' should exist in the empty package directory … just an anchor for the generator and emitter". `DracoAspect.derivation` is `[Json]` with `parents` and `foreignParents` reading the two apart; Dictionary's parent is the node `{"{}": ["K", "V"]}`; the emitter's `foreignReference` and `operatorCarried` are gone; `src/main/resources/.drake` (the one line `domain`) and `.json` anchor the nameless domain. The first run stopped at `Generated.scala:17`, a `DracoAspect` constructed with `_derivation = Seq(TypeName(...))` by named argument, plus twenty-one such sites in four tests — the lesson recorded: when a slot's type changes, grep its named-argument constructions as well as its reads. **Closing state:** `5fd801d` on main, 624 tests / 44 suites green, 98 draco types in scope / 108 measured, `GenDrake` 96 of 96, GEN MAP 28 match / 20 differ, surface losses 5 across 108 types (expression form 2, type form 3), parse scope 98 draco + 10 mods with 0 held back, scenario 23 files clean, PON 80 / 550 / 42 discrepancies, canonical 7 differ. **Every host HEAD in the corpus has a drake form:** Option → Presence, ActorRef → `(M -> Unit)`, Consumer → `(Knowledge -> Unit)` / `(RhsContext -> Unit)`, LazyList → `[T]` with `progression` and `cons`, the Java lists → `[T]+` with `add`, Dictionary's `Map` → the `{K, V}` form. **The remaining host residue is the leaves** — `String`, `Int`, `Json`, `URI`, the evrete and pekko types — and the two conditionals inside DefinitionPath's lambda, which wait on the parser treeing lambdas (GitHub #61). **Queued at the close:** the parser's `foreignRef` / `opensTypeExpression` kept for the parse branch; the three rule conditions de-treed by the September sweeps (`PrimesFromNaturalSequence`, `RemoveCompositeNumbers`, `SelfDeclaration`) still strings until #61; the symbol names `progression`, `cons` and `add`, flagged once and Dev's to rename; `()+` held for mutable trees; the nameless domain anchored by `src/main/resources/.drake` and `.json`, inert to the gates; Dev's open question about abstract types and codecs from chapter 78; `draketarget.DomainAspect` and the aspect-named target types. The next session opens from `5fd801d`.
