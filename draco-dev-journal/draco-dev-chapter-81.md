# Chapter 81 - Expression Trees, Conditionals and Bracket

**Session date:** September 21-22, 2026
**Topic:** Infix and lambda parsing, minimal parentheses, conditional retirement and resource scope.

**Provenance:** Transcribed on September 23 from Claude Code session
`3df62d0b-dcac-4979-9085-52eac92d20dd`, its local JSONL record. Natural-language
turns are retained; tool calls, thinking and raw command results are omitted.
Responsive command results are folded into delegated-action notes. Relative links
to git records are adjusted for this chapter's directory. Model variant is not
established by this transcription. Historical proposals and commands below are
dialogue, not current instructions.

This resumes after chapter 80's `5fd801d`. Supporting commits are `665926e`,
`309ea4d`, `0dc4900` and `8e59cbe`; their records are dated September 21 at
1107, 1136 and 1312, and September 22 at 1110.

---

**[Dev]**

Let's continue with the update to the drake parser, as you suggested in the previous session, unless you remember something that might have higher importance than that.

---

**[Claude]**

I'll start by reading the latest git record and journal status to recover what the previous session proposed for the parser.

---

**[Claude]**

Reading the parser's value-tree section, the three de-treed conditions, DefinitionPath's lambda, and the dlt's expression spec, all in one go.

---

**[Claude]**

The picture is complete enough to surface one shape. I'm not building yet.

**Where the previous session left it.** Three threads wait on the parser, all under GitHub #61: the three rule conditions the September sweeps de-treed back to strings, the two conditionals inside DefinitionPath's lambda, and the `foreignRef` / `opensTypeExpression` residue in the parse branch. Nothing in memory outranks this. The one item worth mentioning is that the docs sync committed yesterday, so the tree is at `912e4e8` with a clean status.

**Recommendation: the infix operator layer, as one step.** Today `value` splits a token run on `++` alone and every other run of two or more tokens is an opaque leaf. The step generalizes that to a declared operator table with fixities, read the way drake.dlt already sketches under EXPRESSIONS: the run is split flat on operator tokens, then reassociated by precedence and associativity. Haskell's Prelude fixities, since that is the semantics you chose:

| precedence | operators | associativity |
|---|---|---|
| 7 | `*` `/` `%` | left |
| 6 | `+` `-` | left |
| 5 | `++` | right, flattened |
| 4 | `==` `!=` `<` `<=` `>` `>=` | none, binary |
| 3 | `&&` | right, flattened |
| 2 | `\|\|` | right, flattened |

**The rule that keeps it safe.** A run trees only when it alternates single-token operands and declared operators from end to end. Any run with a multi-token operand stays the opaque leaf it is today. That is what keeps every lambda body in the corpus opaque, because `ra => ra.x && ...` puts three tokens before the first operator, and keeps format/json/Value's `if … then … else` opaque, since `if pathValue` is two tokens. A parenthesized operand stays what `treed` makes of it now, a tuple or an opaque `(…)` leaf. So TypeLoader's guard argument trees as `||` over a call and an opaque parenthesized right operand, which is legitimate under the tree language. Reading `( expr )` as a sub-expression, and with it minimal parenthesization in the two renderers, is the next step, not this one.

**What moves.** The corpus survey found the runs this rule catches: the three de-treed conditions, TypeName's `equals`, the arguments of `take` in Numbers and Primes, Primes' `_ % p != 0`, DefinitionPath's `found.size <= 1`, and the two example-domain values in AshBirchAlarm and Potency. Their JSON gains trees; their drake and their generated Scala do not change a character, because the flat rendering reproduces the spaced spelling. `Condition.parameters` for the three rules comes off the tree instead of the lexical scan, which is point one of #61. `++` leaves the reserved set and becomes a table row, so `concatenation` and `operand` in the cursor go, and an expression like `a == b ++ c` associates correctly rather than by where the cursor happened to stop. Both renderers learn the new rows. One structural test in `DrakeParseTest` asserts `i1 * i2 == i3` and the `||` over `!=` shape.

Accept, or correct the table or the rule?

---

**[Dev]**

Does your rule imply that lambda body expressions will be a single string in json rather than an expression tree?

---

**[Claude]**

Yes. Under this step a lambda stays one host-opaque string, body included. The rule refuses any run whose operand spans more than one token, and a lambda's parameter and arrow are two tokens ahead of its body's first operator, so the whole run stays the leaf it is today.

That is the step's boundary, not its verdict on lambdas. The tree language already has the lambda node: `\` with the parameters as leading operands and the body as the last, drake surface `\p1 p2 -> body` in Haskell form. The corpus writes its lambdas in the host's form, `ra => ...`, except format/json/Value, which is already in the Haskell form and is the standing exclusion the parser does not tree. The lambda step is the parser reading the `\` form: parameters up to the arrow, then the body parsed as an expression. The body reuses the operator layer, so RuleAspect's `isEmpty` becomes a `\` node over an `&&` tree, and DefinitionPath's two `if` expressions become reachable.

That is why I put the operator layer first: a lambda step landing before it would tree the shell and leave the body a string, then re-tree the body one step later. With the operators in place the lambda step trees the body in one move, and the corpus's `=>` lambdas convert to the `\` form as part of it.

Does that ordering hold for you, or do you want the lambda read to land first?

---

**[Dev]**

I know that I want the generator to see an expression tree where ever one exists in drake, rather than require the generator parse arbitrarily complex expressions. What am I missing in what you telling me about the opacity of the expressions?

---

**[Claude]**

Nothing about the destination. What you want is the end state of #61: every expression drake carries reaches the generator as a tree, and the string form survives only for text that is genuinely host-bound, a Scala symbol or a literal. The opacity I described is the interim, not the design.

The one thing to make explicit is what an opaque string does today. The generator never parses it. It copies it into the Scala verbatim, which works only because every opaque value in the corpus was authored as Scala. So opacity is not a parsing burden on the generator. It is a projection limit: an opaque value cannot reach a second target at all, and cannot be read for its free names, which is why Condition's parameters currently come from a lexical scan. That is the tail the loss report measures, and it shrinks as the parser learns forms.

The whole-run rule exists to keep that tail honest while it shrinks. A run the parser only half understands, `ra => a && b` treed as `&&` over the leaf `ra => a`, would give the generator a tree with the wrong scope, and a wrong tree is worse than a string because nothing measures it. A string is a declared gap. So each step trees exactly the forms it fully reads, and the lambda step, landing next, trees the whole lambda including its body, because its body is read by the operator layer this step adds.

Is the concern that the interim leaves lambdas as strings, or did you want the two steps as one so no interim exists?

---

**[Dev]**

None of the arrows that can be used in drake seem to be given operator precedence in your table. For me that would solve the tree-form error example you gave.

---

**[Claude]**

You're right, and it collapses the two steps into one. The arrow belongs in the table, and it belongs at the bottom: below `||`, right-associative, which is how Haskell reads a lambda body, as far right as it can go. Then `\ra -> ra.x.isEmpty && ra.y.isEmpty` is not a run the parser half understands. The arrow is the loosest operator, so `&&` binds first and the arrow takes the whole conjunction as its right operand. The scope error I used as the reason for the whole-run rule cannot arise, and the rule shrinks to its honest remainder: a run containing a token the table does not know stays opaque.

The lambda then reads in the same pass. A run whose first token opens with `\` is a lambda: the tokens before the arrow are its parameters, one or several, and the arrow's right operand is its body, already a tree. That yields the `\` node the tree language has carried since Value's exemplar, and the engine already renders it as the host's `ra => body`, bare parameter when single, so the generated Scala does not move.

What it asks of the corpus is the surface conversion. Every lambda in the corpus today is written in the host's form with `=>`, and `=>` is deliberately not in the table, so those runs stay opaque until they are rewritten as `\ra -> body`. The three aspect `isEmpty` values, DefinitionPath's lambdas and DerivationResolvable's `anc => …` are the ones I know of. I would survey for the rest and convert them in this same commit, so the step lands with no host arrow left in any value position.

One row of the table to confirm before I fix it: `->` goes below `||` as the loosest operator, and `=>` stays out. Yes?

---

**[Dev]**

Yes, if by "stays out" you mean it is generated for Scala based on whet is in the expression tree in json.

---

**[Claude]**

Yes, exactly that. `=>` is the Scala target's spelling of the lambda node, written by the generator from the tree, and never a drake token. Building now: parser, both renderers, the corpus conversion, one structural test, then the dlt and DRACO.md.

---

**[Claude]**

Reading the parser and renderer regions I'll edit, all at once.

---

**[Claude]**

Compile probe is clean. Next I'm building the overlay classpath with the new parser, sweeping every drake through it to re-canonicalize the JSON, and updating DRACO.md's baseline and caveat text.

---

**[Claude]**

The test derives the repo root from the compiled class location, so the probe classes must sit under the repo's own target tree. Moving them there and re-running.

---

**[Claude]**

The operator layer and the lambda are built, probed, and recorded. The tree is ready for your run.

**What landed** (36 files, record at [draco-git-record/git-record-2026-09-21-1107](../draco-git-record/git-record-2026-09-21-1107)):

- **Parser.** A value's token run splits flat at every declared operator and is reassociated by Haskell's Prelude fixities, with `->` below every other row, so a lambda `\p1 p2 -> body` takes the whole run right of its arrow. `++`, `&&` and `||` are flat variadic nodes. A chained non-associative operator such as `a == b == c` is refused. `++` left the reserved set, and the cursor-level concatenation code is gone.
- **The whole-run rule, kept for its remainder.** A run with a several-token operand, a host `if`, a `new`, a block, stays one opaque string. The parser never hands the engine a tree with the wrong scope.
- **Both renderers** spell every table operator. `Expression.rootNames` treats a lambda's parameters as bound.
- **Corpus.** Every host-form lambda became the `\` form, and CLI's command map became tuples, because `->` in a value is the arrow. Eighteen JSON files were re-canonicalized by the compiled parser. The three de-treed rule conditions are trees again. Generated Scala is byte-identical everywhere except CLI's map.
- **One structural test** in `DrakeParseTest`, so the count moves to 625.
- **Docs.** drake.dlt carries the fixity table and the rule. DRACO.md, README.md, CHANGELOG.md updated.

**Deliberately left in host form:** DerivationResolvable's condition and TypeLoader's source read. Each body is a `{ … }` block, which the parser would read as a set literal. A block form needs its own discussion. ActorAspect and Value keep their standing exceptions.

**Verified before handover.** The engine files and the regenerated CLI.scala compile clean against the exported classpath. Beyond the usual CLI probes, I ran the suites themselves on the compiled classes with the scalatest runner: DrakeParseTest, DrakeGenTest, DracoGenTest, both scenario suites, GenDrakeTest, ExampleDomainsGenTest, PresenceTest, the rete visibility test and the primes suites. All green, every headline unchanged: losses 5 across 108 types, scope 98 + 10, GenDrake 96 of 96, gen map 28 / 20. That recipe is now in memory. Your `sbt test` is still the gate.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Expected: 625 tests / 44 suites, all green. Paste the grep lines. Once green, the commit block is at the foot of the record's comment section:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-21-1107 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-21-1107 && git commit -F /tmp/msg -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-21-1107 && git push
```

Next parser step, when you want it: the parenthesized sub-expression with minimal parenthesization in both renderers. After that the two forms still opaque in the corpus are the host `if` and the block.

---

*(Delegated action - Dev ran the full suite: 625 tests passed, then committed and pushed 665926e.)*

---

**[Dev]**

Next parser steps as you suggest.

---

**[Claude]**

Run green at 625 and pushed as `665926e`. Building the next step: the parenthesized sub-expression in the parser, with minimal parenthesization in both renderers. First a survey of where parenthesized groups stand in the corpus today.

---

**[Claude]**

All differences the sweep surfaced predate this change, and the record, CHANGELOG and memory are written. Nothing further is needed from the tree; handing over.

The parenthesized sub-expression step is built, probed, and ready for your run. Record at [draco-git-record/git-record-2026-09-21-1136](../draco-git-record/git-record-2026-09-21-1136).

**What landed** (11 files):

- **Parser.** A `( )` group holding one expression the parser reads dissolves into that tree. The pair is not information once the tree is associated. A group the parser reads as a leaf, such as a typed binder or a host `if`, keeps its parentheses.
- **Two renderers, two tables.** The drake emitter writes back only the pairs drake's own fixity needs. The Scala target writes only the pairs Scala's precedence needs, by first character with every operator grouping left, where `++` ranks with `+` rather than below it. So `xs ++ (n + 1)` keeps its pair in Scala and loses it in drake, `(a || b) && c` keeps it on both, and `a || (b && c)` loses it on both. A lambda as an operand takes a pair in Scala always, in drake only when something follows it.
- **Corpus.** Exactly one value in all three resource trees held a group the parser now reads: the guard in `TypeLoader.rooted`. Its trio moved, the JSON to a tree by the compiled parser, the drake and the Scala to the pair-less line by the compiled emitter and engine. The regenerated Scala compiles.
- **One structural test** in `DrakeParseTest`, so the count moves to 626. drake.dlt, DRACO.md, README and CHANGELOG updated.

**Verified before handover.** The engine files and TypeLoader.scala compiled clean against the exported classpath. Every main type was regenerated and diffed. The suites ran on the compiled classes: DrakeParseTest 223, DrakeGenTest 107, DracoGenTest 164, both scenario suites, GenDrakeTest and ExampleDomainsGenTest, all green with every headline unchanged. The leftover differences the sweep showed, a mods Output.drake the emitter would rewrite and a handful of generated files that differ from the hand files by a blank line or a hand-held family, are identical under the old engine and predate this change.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Expected: 626 tests / 44 suites, all green, headlines unchanged. Paste the grep lines. Once green:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-21-1136 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-21-1136 && git commit -F /tmp/msg -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-21-1136 && git push
```

What remains opaque in the corpus after this: the host `if` inside Value and DefinitionPath's lambdas, and the two block bodies in DerivationResolvable and TypeLoader's source read. The conditional is the natural next step, and it is a design question rather than a parser one, since `ifThenElse` on a Presence already exists as the dispatch form. I'd surface that shape before building it.

---

*(Delegated action - Dev ran the full suite: 626 tests passed, then committed and pushed 309ea4d.)*

---

**[Claude]**

Green at 626 and pushed as `309ea4d`. Memory records it. The conditional is next, and it is a shape question rather than a parser one, so here is the one recommendation.

**The corpus holds four conditionals, in two files.** Value has two in the Haskell `if … then … else` form, one inside its fold lambda and one as the `value` result. DefinitionPath has two in the host's `if (…) … else …` form, nested inside the `hostRoots` lambda. All four are opaque today, and they are what keeps Value on the exclusion list.

**Recommendation: no parser step. Convert the four to the form you already decided on.** On 2026-09-17 you made `ifThenElse` a dyn on Presence, with `guard` as the bridge from a truth, and the Haskell-form `if` surface was retired as a plan at the same time. TypeLoader's `rooted` already reads `guard(c).ifThenElse(t, e)`. That is a call on a path, which the parser has treed since the call syntax landed, and both branches are by need, so the semantics are the ones `if` had. Value's result becomes `guard(pathValue != null).ifThenElse(pathValue.as[T].getOrElse(null.asInstanceOf[T]), null.asInstanceOf[T])`, and DefinitionPath's nested pair becomes two nested guards with `++` in place of the host's `+`.

**What it retires.** The `if` node in the tree language, which only Value.json ever carried, leaves both renderers and drake.dlt. Value comes off the exclusion list and its JSON is re-canonicalized whole, so the loss headline should drop from five fields to the ActorAspect remainder alone. The generated Scala changes in the two files, since a guard renders as a Present or Absent of Unit, so both are compiled before handover.

**The one thing to weigh.** DefinitionPath's inner conditional is host text through and through, a file name and a URI, so the guards wrap Scala atoms. That is the leaves residue we already accept, and it is what lets the two lambdas tree completely.

Accept, or would you rather keep a conditional surface form and have the parser tree it into the `if` node?

---

**[Dev]**

Accept your recommendation.

---

**[Claude]**

Building it. First the facts the conversion depends on: the `ifThenElse` signature, how `guard` spells in Scala, where the `if` node still appears, and where Value's exclusion is written down.

---

**[Claude]**

All green on the probe, and the loss headline drops from five fields to one. Now the baseline table, CHANGELOG, record and memory, all in one pass.

---

**[Claude]**

The conditional step is built, probed and recorded. Ready for your run. Record at [draco-git-record/git-record-2026-09-21-1312](../draco-git-record/git-record-2026-09-21-1312).

**What landed** (13 files, no test changes):

- **Corpus.** Value's two Haskell-form conditionals and DefinitionPath's two Scala-form ones are now `guard(c).ifThenElse(t, e)`, the dispatch form you decided on 2026-09-17 and the one TypeLoader already used. Every value in both files is a tree. Value's JSON was re-canonicalized whole by the compiled parser, type forms included, so it comes off the exclusion list.
- **Tree language.** The `if` node, which only Value.json ever carried, is gone from both renderers, their parenthesization clauses, and drake.dlt. A conditional is a call like any other.
- **Docs.** DRACO.md's exception list shrinks to the two authored-ahead aspects, and its loss baseline row moves. README and CHANGELOG follow.

**Verified before handover.** The engine compiled clean, and the two regenerated Scala files were compiled into the probe overlay, so the suites exercised the new Value at runtime. On the compiled classes: DrakeParseTest 223, DrakeGenTest 107, DracoGenTest 164, TestValue 2, TypeNameTest 6, GenDrakeTest 96 of 96, ScenarioGenTest 5, all green.

**One headline moves, by design.** The surface-loss line should now read one field across 108 types, the ActorAspect type form alone, down from five. Everything else should be unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Expected: 626 tests / 44 suites, all green. Paste the grep lines. Once green:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-21-1312 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-21-1312 && git commit -F /tmp/msg -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-21-1312 && git push
```

After this, the only host-opaque expression forms left in the corpus are the two block bodies, DerivationResolvable's condition and TypeLoader's source read, plus the typed-binder leaves. The block is a design question, a local binding inside an expression, so I'd surface a shape for it before building anything.

---

*(Delegated action - The first full conditional-step run failed two SourceContractTest tests. The scoped probe had not covered those tests; this was not a green full-suite result.)*

---

**[Claude]**

Two tests I did not run on the probe, both in `SourceContractTest`, and both hit the renderers' unknown-operator error. That test almost certainly feeds every operator including the retired `if`. Let me look.

---

**[Claude]**

The two failures were in the factored-renderer prototype and its contract test, a third copy of the operator set that my grep of the two production renderers missed. Its corpus fed an `if` tree to renderers that no longer know the node.

**Fixed.** The prototype in `SourceContract.scala` lost its `conditional` slot and its `if` case, and the test corpus lost its two `if` trees, the nested one now a lambda over a call. Compiled and run on the probe: all three tests in `SourceContractTest` pass. The record carries a RUN 1 paragraph with the lesson, the CHANGELOG entry mentions the prototype, and the lesson is saved to memory so any future change to a renderer's operator set runs that suite.

Same run and commit blocks as before. Expected: 626 tests / 44 suites, all green, losses at one field.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Once green:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-21-1312 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-21-1312 && git commit -F /tmp/msg -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-21-1312 && git push
```

---

*(Delegated action - Dev reran the full suite after the correction: 626 tests passed, then committed and pushed 0dc4900.)*

---

**[Claude]**

Green at 626 with losses at one field, pushed as `0dc4900`, and memory records it. That closes every conditional in the corpus.

**What is left, and the one recommendation for it.** Two values still hold a host block, and they are different things wearing the same braces.

DerivationResolvable's condition binds a name to reuse it: load the ancestor once, then ask four aspects of it. That is a local binding inside an expression, which in Haskell is `let` and in drake already exists as a dyn-with-body, whose `loc` is exactly a lazy local binding. The condition renders as a method on the rule object, so a global dyn on the same type is callable from it. The block becomes a named dyn, `resolvesBare(anc)` say, with one `loc` and a `value`, and the condition becomes a lambda over a conjunction with a call, which the parser already trees in full.

TypeLoader's source read is not a binding but a resource scope: open, read, close on the way out whatever happens. Turning that into `loc` and `mon` statements would need `now` on the read so it precedes the close, and would lose the close-on-failure guarantee the `finally` gives. The honest form is the host's resource-scope function as a leaf, `scala.util.Using(source)(\s -> s.mkString)`, which yields a Try the `presence` symbol already knows how to bring across. That leaves no block, no `now`, and the guarantee intact, at the cost of one more host name in the leaves residue you have already accepted.

So: no new expression form and no parser change. A block is either a dyn-with-body or a host boundary, and the corpus has one of each. After it, every value in the corpus is a tree down to its leaves, and the surface-loss line measures only ActorAspect.

Accept, or would you rather have a `let` form on the surface?

---

**[Dev]**

Is there Haskell equivalent to scala.util.Using and would drake need a new syntactic form for it?

---

**[Claude]**

Yes. Haskell's counterpart is `bracket` in Control.Exception: `bracket acquire release use`, with `withFile` and friends as specializations of it. It is an ordinary function, not syntax. The resource scope is application, and the close-on-failure guarantee lives inside the function, exactly where `finally` lives in Scala.

So drake needs no new form. It needs one more declared symbol on the dlt's list, beside `presence`, `guard` and `join`, in Haskell's name and argument order: `bracket(acquire, release, use)`, where `use` is a lambda over the resource. The Scala target spells it once, as a block with `try … finally`, which is the target's business, the way it spells `guard` as an `if` into Present or Absent. TypeLoader's read then becomes a call the parser already trees: `bracket(scala.io.Source.fromURL(u), \s -> s.close(), \s -> io.circe.parser.parse(s.mkString).flatMap(_.as[TypeDefinition]).toOption)`, brought across by `presence`. Better than `Using`, too, since `Using` is a Scala name in the definition where `bracket` is drake's own.

Accept `bracket` as the symbol, with the dyn-with-body for DerivationResolvable as recommended?

---

**[Dev]**

Yes, accept bracket given the Generator can use it for multiple target languages and not add any more host names to drake.

---

**[Claude]**

Building it: `bracket` as a declared symbol, the Scala target spelling it as a block with try and finally, TypeLoader's read as a call, and DerivationResolvable's binding as a dyn-with-body. First the facts the edits depend on.

---

**[Claude]**

All green on the probe, with the regenerated TypeLoader compiled in, so every suite that loads a definition ran through `bracket`. Writing the CHANGELOG entry, record and memory now.

---

**[Claude]**

The `bracket` step is built, probed and recorded. Ready for your run. Record at [draco-git-record/git-record-2026-09-22-1110](../draco-git-record/git-record-2026-09-22-1110).

**What landed** (13 files):

- **The symbol.** `bracket(acquire, release, use)` joins `presence`, `guard` and `join` on the dlt's declared list, in Haskell's name and argument order. No new form: the resource scope is a call with two lambdas over the resource.
- **The Scala spelling.** The engine spells it from the trees, not the rendered arguments, as the block an author writes: the resource bound to the `use` lambda's parameter, its body under `try`, the release body under `finally`. A release parameter named differently is bound inside the finally.
- **TypeLoader.** The source read is that call. The null guard on the URL went with the block, since the only caller folds a Presence and never passes null. A new global dyn `isStub` holds the binding DerivationResolvable used to carry as a block. The name is mine, after DRACO.md's "stub", and yours to rename.
- **DerivationResolvable.** The condition is a lambda over a conjunction with a call, treed whole.
- **One test** in `DrakeParseTest`, so the count moves to 627. drake.dlt, DRACO.md, README and CHANGELOG updated.

**Verified before handover.** The regenerated TypeLoader was compiled into the probe overlay, so every suite that loads a definition ran through the new read. Twelve suites green on the compiled classes, DrakeParseTest at 224, losses unchanged at one field. One gotcha on the way, saved to memory: a local function named `lambda1` crashes scalac at the emit phase, since it collides with the compiler's synthetic lambda naming.

**Where this leaves the corpus.** No expression form is host-opaque any more in either resource tree. What remains host-spelled is the leaves, the primitive and library names, and a lambda's typed binder carried as a leaf. The parser thread of GitHub #61 is closed by this commit.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Expected: 627 tests / 44 suites, all green, every headline unchanged. Paste the grep lines. Once green:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-22-1110 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-22-1110 && git commit -F /tmp/msg -- src DRACO.md README.md CHANGELOG.md draco-git-record/git-record-2026-09-22-1110 && git push
```

---

## Status

The source ends with bracket built and scoped probes green, a full run handed to
Dev and an expected count of 627 tests / 44 suites. It does **not** contain Dev's
subsequent bracket full-run output. Git independently establishes the resulting
commit `8e59cbe`; chapter 82 records the later full-suite verification of its successor.
The last pasted full run within this source was 626 passing tests after the
SourceContract correction. Do not turn the expected 627 into a witnessed run.

Infix operators and lambda bodies are trees, parenthesized subexpressions render
by target fixity, conditionals use guard/ifThenElse, and bracket is a declared
cross-target symbol rather than new syntax. The remaining surface-loss field is
ActorAspect's known type-form discrepancy; no corpus expression-form loss remains.
A scoped probe missed the third operator vocabulary in SourceContract and its
tests: future expression changes must account for that consumer as well as the
two production renderers. No second target implementation is established here.
