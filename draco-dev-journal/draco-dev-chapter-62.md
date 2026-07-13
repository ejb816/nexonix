# Draco Dev Journal — Chapter 62

**Session date:** July 13, 2026
**Topic:** Value-expression syntax — operator-ness as a property of the symbol, not the grammar → Dev's Action.json prototype (S-expression trees in JSON, and a misspelled `RHSContext` the opaque string never caught) → `TypeElement.value: String → Json` (197/197 on the first run) → incremental tree conversion (`Seq.empty`, the Primes infix conditions, the application node) → Haskell-form constructs (`\` lambda, `if-then-else`) dissolving Value's `foldLeft`, and the `->` arrow re-key: form is drake's, atoms may be the target's. Transcript.

---

**[Dev]** We need to work on the expression syntax for value expressions. Can we simultaneously allow both pure function composition expressions and infix operator expressions, using the symbol references embedded in the definitions?

**[Claude]** *(grounded first in `drake.dlt`, the corpus's richest value expressions, and a journal sweep — no prior fixity design exists, only ch.53's line-continuation convention)* Yes — and there's a well-trodden mechanism for exactly this. Here's my assessment, grounded in the current corpus.

**The key move: operator-ness is a property of the symbol, not the grammar.** Both styles coexist cleanly if the grammar itself knows nothing about precedence, and every identifier in an expression is a **reference to a declared symbol** — a `par`, `loc`, `fix`, `dyn`, `var`, a global, or a dictionary member. This is Haskell's actual architecture: the parser produces a *flat* operator sequence, and a separate **fixity-resolution pass** reassociates it using fixity read from each symbol's declaration in scope. Agda does the same for mixfix. The definitions *are* the disambiguator — which is precisely "using the symbol references embedded in the definitions."

Two-layer grammar, context-free:

```text
expr    :=  app ( opsym app )*        -- operator layer: FLAT, no precedence in the grammar
app     :=  atom atom*                -- application layer: juxtaposition, binds tightest, left-assoc
atom    :=  name | literal | ( expr ) | ( opsym ) | `name`
```

Then the resolution pass:

- **Lexical class picks the default surface.** Symbolic names (`*`, `==`, `#::`, `∘`) apply infix; alphanumeric names apply prefix by juxtaposition. The crossovers come free: `` `f` `` uses an identifier infix, `(*)` uses an operator as a prefix value — so operators are first-class and point-free style works.
- **Fixity lives on the declaration.** An infix symbol is just a 2-parameter `dyn`; its declaration optionally carries precedence + associativity, defaulting (à la Haskell) to `infixl 9`. This passes the Haskell test — fixity is a genuinely Haskell-native concept, not a Scala leak — and it fits the INFERENCE principle in drake.dlt: a fixity is a free authored choice, not reconstructable, so it's explicit when non-default and elidable (`~`) otherwise.
- **Composition needs no special syntax.** `∘` is just another declared 2-ary symbol in a base/prelude domain. Once operators are symbol references, pure-composition expressions fall out with zero extra grammar.

Concretely, the corpus's existing simple expressions are *already* drake-native under this model: `con i1 * i2 == i3` in PrimesFromNaturalSequence.drake parses flat as `i1, *, i2, ==, i3` and resolves correctly given `*` at precedence 7 and `==` at 4 from a numeric prelude.

**The honest catch.** Today's `<value>` fields are verbatim Scala, and much of it won't parse under this grammar: member selection (`accumulator.primeSet.addOne(i)`), lambdas (`(e, a) => …`), `s"…"` interpolation, `_` placeholders — Value.drake's `foldLeft` is the extreme case. Two specific consequences: (1) **don't overload `.`** as the composition glyph — the corpus is saturated with member-selection dots; use `∘` (or juxtaposition + parens only) for composition. (2) The recognizer should classify each value as **drake-native** (parses under the expression grammar, every symbol resolves) vs **host-opaque** (carried verbatim). That's your validation-via-projection pattern again: a positive presence predicate, and the host-opaque set becomes the *measured* remaining tail rather than an invisible one.

**Recommended increment:** add an `EXPRESSIONS` section to `drake.dlt` specifying the two-layer grammar, the lexical infix/prefix rule with backtick/paren crossovers, fixity-on-declaration, flat-parse-then-resolve semantics, and the drake-native vs host-opaque classification. Expressions stay strings in JSON; the AST-structuring and per-target rendering defer to Generator[L] like everything else in NOTES — so the procedural Generator is untouched, and this is a pure template/spec step with no code or corpus impact. If that matches your intent, I'll draft the section.

---

**[Dev]** Look at Action.{json,drake} - I changed Action.json Fixed valueType String  to Json and value  to:

```
 {
  "=>": [
    {
      ".": ["org","evrete","api","RHSContext"]
    },
    "Unit"
  ]
}

...and left Action.drake unchanged for comparison.
```

**[Claude]** *(read Action.json/.drake, the generated Action.scala, and traced what the Fixed feeds)* This prototype lands the normative half of the expression design, and it's the right shape. You picked a telling spot for it, too — that Fixed feeds `TypeElement.scala:351`, where a type expression currently lives as an opaque host string.

**What the encoding settles.** `{"=>": [{".": ["org","evrete","api","RHSContext"]}, "Unit"]}` is an S-expression in JSON: a single-key object is an operator application, the **key is the operator symbol — a symbol reference**, the array holds operands, and a bare string leaf is an atom reference. Two structural consequences fall out:

1. **JSON carries the *resolved* tree, so fixity never appears in the normative form.** Precedence and associativity become purely drake-surface concerns: the parser uses fixity from symbol declarations to build the tree; the pretty-printer uses the same fixity to reinsert minimal parens. Your unchanged Action.drake line 15 vs the tree in Action.json is exactly that surface↔normative pair.
2. **"Simultaneously allow both" is answered structurally**: infix (`A => B`) and composition/application are the *same node shape* in the tree — the distinction only exists at the surface, chosen per symbol by lexical class. Your n-ary `.` (four operands, flat rather than nested binary) is also a good canonical-form rule: associative operators flatten; others stay binary.

And a concrete payoff of symbol-references-in-expressions, sitting right in your example: **`RHSContext` is misspelled** — the real Evrete class is `RhsContext` (see `RuleType.scala:5`). The misspelling lives in Action.drake, Action.json, and generated `TypeElement.scala:351`, and nothing ever caught it because the string is opaque — never compiled, never resolved. Resolution over expression trees catches exactly this class of rot.

**Three things I noticed in the edit:**

1. **The JSON is currently invalid** — the `values` Fixed at Action.json:59 is missing its closing `},` before the next `{`, and its value says `"_value"` where drake and the Scala factory say `_values`.
2. **The tree won't decode yet**: the Fixed decoder reads `value` as `String`, so `Generator.loadType("Action")` will reject an object there. The enabling migration is gentle, though: `TypeElement.value: String → Json`. Every existing string value is already valid Json, so the corpus is untouched, and `isString` vs `isObject` gives you the host-opaque vs drake-native classification for free — the measurable migration tail, presence-detected. (Full blast radius via test feedback, not prediction — but one thing is certain: the procedural Generator must render this one tree back to its infix string, since `TypeElement.scala` regeneration has to round-trip with `comparisonOnlyExcluded` empty.)
3. **One design correction I'd recommend**: keep the Fixed's *declared* `valueType` as `"String"`, not `"Json"`. That field types the generated val — the trait declares `valueType: String`, so a `Json`-typed override breaks it. What changed is the *authoring encoding* of the value, not the runtime type of the element; those are orthogonal. The tree is a type expression whose rendering becomes the String initializer, and the existing corpus convention already disambiguates leaves: embedded quotes = literal (`"\"ctx\""`), bare string = reference (`"Unit"`).

One sovereignty note to decide consciously: with the operator as the key, `"=>"` becomes drake's canonical arrow that Generator[L] renders per target (`=>` for Scala, `->` for Haskell). Adopting Scala's spelling as drake-canonical is fine — but it's drake's symbol from then on, not Scala's.

If this matches your intent, the increment I'd do next: fix the two JSON defects, then write the normative-tree spec (single-key operator node, flat-variadic for associative ops, leaf conventions, fixity-only-at-surface) into drake.dlt alongside an EXPRESSIONS section for the surface grammar — spec only, no Scala change yet.

---

**[Dev]** I fixed the json errors in Action.json and changed Json back to String. Is that what you meant?

**[Claude]** *(checked the file, made two small corrective edits)* Done. The final state of that Fixed is now:

- `"valueType": "String"` — unchanged semantics: the generated val is still `override lazy val valueType: String`, because that field types the *runtime element*, not the authoring encoding.
- `"value": { "=>": [ { ".": [...] }, "Unit" ] }` — a genuine JSON object: the structured expression tree, distinguishable from every legacy string value by `isObject` alone.

The two dimensions stay orthogonal: *what type the value has* (declared valueType, `String`) versus *how the value expression is authored* (opaque string vs resolved tree).

One expectation to be explicit about: Action.json is now deliberately ahead of the implementation. The current `Fixed` decoder does `downField("value").as[Option[String]]`, so `Generator.loadType("Action")` will fail on the object — meaning DracoGenTest (and anything touching the Draco domain load) breaks until `TypeElement.value` migrates `String → Json`. That's the enabling increment this prototype motivates. If you'd rather keep the suite green while we spec first, I can park the tree form in a scratch copy and restore the string here — but if you're ready to drive the migration by test feedback, this is exactly the right failing input to start from.

---

**[Dev]** How do you want to update the json definitions? Incrementally at first and then batch them once Generator is updated correctly?

**[Claude]** Incremental first, yes — but I'd refine the second half: the batch step shouldn't be a hand-edit sweep, it should be **mechanized by the expression parser itself**. Here's the sequence I recommend:

**Increment 1 — make the one existing tree work (Scala-side, test-driven).** Migrate `TypeElement.value: String → Json` (trait, decoder, encoder), and teach the procedural Generator to render a tree back to its Scala expression string — only the two node kinds Action.json uses: n-ary `.` (dot-join) and binary `=>` (infix arrow). Since a string is valid Json, all 63 other definitions decode unchanged; Action.json is the single failing input driving the change, and the suite is green again when `TypeElement.scala` regenerates identically with the tree rendering to `"org.evrete.api.RHSContext => Unit"`. Per your test-feedback principle, whatever else breaks (Generator call sites treating `value` as String — `.nonEmpty` guards, emission) surfaces here rather than being predicted. Alongside it, write the normative-tree spec into drake.dlt.

**Increment 2 — a few hand-converted definitions, one new node kind each.** Convert two or three JSON values covering distinct shapes — e.g. a selection like `Seq.empty`, an infix condition like `i1 * i2 == i3` (adds `*`, `==`), a literal-bearing expression. Each one extends the renderer by exactly one concept. Hand-authoring stays cheap here because the corpus is the syntax-discovery medium, same as the .drake buildout was.

**Increment 3 — the batch pass, done by a tool.** Write the surface expression parser (string → tree, the fixity-resolution design) and run it over the whole corpus — as a draco tool (a test or a `bin/draco-gen` subcommand), not scripts. Everything that parses converts to a tree; everything that doesn't **stays a string** and gets reported. That report is the measured host-opaque tail — lambdas, method chains, interpolations — which becomes the follow-on work queue instead of a blocking prerequisite. No definition is ever converted by hand at scale, and nothing is forced.

Two properties make this safe throughout: `.drake` files don't change at all (trees are JSON-normative; drake stays the surface), and string-vs-object coexistence means there's never a flag-day — a half-converted corpus is a valid corpus at every commit.

One side decision to fold in early: the `RHSContext` → `RhsContext` misspelling. I'd fix it as its own tiny step (drake + json + regenerated Scala together) before increment 3, so the parser's symbol table never learns the wrong name.

Want me to start increment 1 — the `TypeElement.value` migration and the two-node renderer — and let your test run tell us what else it touches?

---

**[Dev]** I just did the RHS to Rhs fix but it looked like the json string was reverted to just json in the Action.json file. Which should it be? Embedded as a string, or just json?

**[Claude]** Just JSON — a raw object, not an escaped string. The current file has it embedded as a string again (line 81), which is the doubly-encoded form: JSON-in-a-string. That form is the worst of both worlds — it's opaque to the `isString`/`isObject` classification (it would misfile as a legacy host-string), and the `TypeElement.value: String → Json` migration wouldn't recognize it as a tree.

The reason the raw object is right: the value's *structure* is the signal. Every legacy value in the corpus is a JSON string; the tree is a JSON object. That one structural distinction is what makes the whole incremental migration safe — no flag, no version field, just `value.isObject`.

*(Restored the object form keeping the `RhsContext` fix, then verified the spelling across the trio.)* To summarize the state: **just JSON** is the answer — the value now sits in Action.json as a raw object tree (with your `RhsContext` spelling), and I extended the spelling fix to Action.drake:15 and TypeElement.scala:351, which your fix hadn't reached.

The embedded-string form you saw is worth recognizing as a trap, since it can reappear whenever a tool re-serializes: a string containing JSON is still a *string* — it decodes fine under today's `String`-typed decoder, which makes it look harmless, but it's invisible as a tree. If it ever shows up again after an IDE reformat or copy-paste, the raw-object form is always the intended one.

Current corpus state is intentionally red: Action.json now carries the one object-valued `value` in the corpus, which the current `Fixed` decoder rejects — the failing input for increment 1 (`TypeElement.value: String → Json` plus the two-node renderer for `.` and `=>`). Say the word and I'll start it.

---

**[Dev]** I explicitly re-reverted the json string so I don't think it will reappear on its own. Go ahead with your suggested changes.

**[Claude]** *(implemented increment 1 across four layers — surveyed every `TypeElement.value` touch point first, since the trait extends `Primal[String]` and `value` is the Primal slot, then swept definitions, Generator, generated Scala, and consumers)* Increment 1 is fully in place. Here's what changed, layer by layer:

**Definitions (drake + json, 8 types).** `TypeElement` now derives `Primal(Json)` with `fix value Json Json.Null`; the seven value-carrying subtypes (Fixed, Mutable, Dynamic, Parameter, Monadic, Condition, Local) declare `par value Json` (defaults `Json.Null` where `""` was), and the three factory bodies say `fix value Json _value`. Your Action.json tree is untouched and is now the one object-valued `value` in the corpus.

**Generator (`Generator.scala`).** Two new helpers: `expression(Json)` renders a value — strings pass through verbatim, `Json.Null` → `""`, and a single-key tree renders by operator (`.` dot-joins its operands, `=>` joins with the arrow); `initializer(valueType, value)` adds the one subtle rule — a *tree* in a String-typed slot emits as a quoted string literal, which is exactly what regenerates `override lazy val valueType: String = "org.evrete.api.RhsContext => Unit"` from your tree. All ~41 emission sites now go through these two, and the codec emission grew `"Json"` cases (zero = `Json.Null`, wire elision = null-or-empty-string, decode = `as[Option[Json]]`).

**Generated file (`TypeElement.scala`).** Hand-synced to the new emission: trait extends `Primal[Json]`, all seven `_value` parameters and overrides are `Json`, decoder lines and Null instances updated, encoder's value elision is the Json check. This is the byte-for-byte target for DracoGenTest.

**Other consumers.** `Generated.scala`, `TypeDefinitionTest.scala`, `TestTypeModule.scala`, and the `inspect-type` script now construct with `Json.fromString(...)`/`Json.Null`. The drake.dlt gained an EXPRESSIONS section spec'ing the tree form, the leaf conventions, and the future surface grammar with fixity resolution.

One deliberate semantic note: `Json.Null` is the new "absent" (old `""`), and a JSON string value is bit-identical on the wire before and after — so the other 63 definitions round-trip unchanged, and only Action.json exercises the tree path.

This is ready for your test run — `sbt test` will be the real verdict, and per your test-feedback principle I expect any misses to be in call sites I couldn't see from grep (interpolations of `.value` in code I didn't touch would now print Json AST instead of failing to compile, so watch DracoGenTest diffs especially). Send me the failures and I'll work through them.

*(Delegated action — Dev ran `sbt test`: **197/197, all pass — green on the first run.**)*

197/197 green on the first run — increment 1 is complete and verified. The full loop works end-to-end: Action.json's expression tree decodes through the new `Json`-typed codec, `Generator.expression` renders it back to `org.evrete.api.RhsContext => Unit`, `initializer` quotes it into the String slot, and the TypeElement-family multi-type generation matches `TypeElement.scala` — while all 63 legacy string-valued definitions round-tripped untouched. The `GenerateAndCompileTest` passes also confirm the regenerated family actually compiles, not just matches.

So the corpus now officially carries its first drake-native expression tree, and the `isString`/`isObject` classification is live in the model. This is a good commit point. When you're ready for increment 2, the natural next conversions — one new node kind each:

1. **`Seq.empty`** (everywhere) — a pure `.` selection: `{".": ["Seq","empty"]}` — zero new operators, just proves `.` in value position.
2. **An infix condition** — `i1 * i2 == i3` in the Primes rules introduces `*` and `==` as operator nodes (and makes the tree↔surface pair vivid, since the `.drake` conditions already read infix).
3. **A literal-bearing value** — e.g. `Fixed name String "\"ctx\""` as a tree leaf, pinning down the embedded-quotes-literal convention inside trees.

My recommendation is #1 first — it's the smallest step and converts the single most common expression in the corpus.

---

**[Dev]** Please proceed as you recommend.

**[Claude]** *(inventoried and converted all string-form `Seq.empty` values in place)* The `Seq.empty` conversion is done: all 20 occurrences across 10 metamodel definitions (Action, Assembly, Condition, DomainAspect, DracoAspect, Dynamic, Factory, Pattern, RuleAspect, TypeElement) now carry the inline tree `{ ".": ["Seq", "empty"] }` instead of the opaque string, and every file validates as JSON.

Two properties of this step worth stating: **the generated Scala should be byte-identical** — `expression` renders the tree back to `Seq.empty`, and since these sit in `Seq[...]`-typed slots (not String), `initializer` doesn't quote — so DracoGenTest's comparisons should pass with no source changes at all. That's the point of the step: it exercises `.`-trees in *value* position (factory parameter defaults) purely on the normative side. And **the drake surface is untouched** — `par parameters [Parameter] []` still reads as the empty-Seq literal; only the JSON projection got structured.

The corpus tree count just went from 1 to 21, and the host-opaque tail shrank by the most common single expression. Please run `sbt test` — if it's green, the natural step 2 of this increment is the infix conditions.

*(Delegated action — Dev ran `sbt test`: **197/197, all pass.**)*

197/197 again — the 20 `.`-trees are live. Proceeding to step 2: the infix conditions. Both Primes rule conditions now carry the first genuinely *nested* trees in the corpus:

```json
"value": {
  "==": [
    { "*": ["i1", "i2"] },
    "i3"
  ]
}
```

with three coordinated changes around them:

- **Renderer** (`Generator.scala`): `*` and `==` join operands with the spaced operator, so the nested tree renders `i1 * i2 == i3` — flat, no parens, matching the committed `def w0(...)` condition functions in both rule sources exactly.
- **Spec** (`drake.dlt`): `*`/`==` added to OPERATORS with the nested example, and the RENDERING paragraph now states the flat-rendering contract explicitly — until the fixity table exists, an authored tree must read back correctly under the target's precedence, and anything that wouldn't stays inside a host-opaque leaf.
- The `.drake` surfaces (`con i1 * i2 == i3`) are untouched, as always.

This step is a stronger test than the `Seq.empty` one: the condition value flows through a different path (`conditionFunctions` → Evrete's runtime-compiled `where` functions), so a green run confirms trees survive the rule-compilation pipeline, not just source emission — `PrimesRulesTest` actually fires these rules against working memory.

*(Delegated action — Dev ran `sbt test`: **197/197, all pass.**)*

197/197 — the nested infix trees survive the whole pipeline, including actual rule firing in `PrimesRulesTest`. Proceeding with step 3. The right corpus site for a literal-bearing tree is Primes.json's `knowledge` element — `Rule.knowledgeService.newKnowledge("Primes")` — which became the corpus's first **application tree**:

```json
{ "()": [ { ".": ["Rule", "knowledgeService", "newKnowledge"] }, "\"Primes\"" ] }
```

This one value pins down two things at once: the `"()"` application node (first operand applied to the rest, comma-joined), and the **literal-leaf convention inside trees** — `"\"Primes\""` keeps its embedded quotes and passes through verbatim, exactly as bare-string leaves like `"i3"` pass through as references. The renderer, its doc comment, and the drake.dlt OPERATORS list all gained the `"()"` entry with this example.

The rendered result is `Rule.knowledgeService.newKnowledge("Primes")`, byte-identical to what `Primes.scala` declares, so DracoGenTest's Primes comparison should stay green — and since `knowledge` is the live Evrete Knowledge that `PrimesTest`/`PrimesRulesTest` actually build sessions from, a green run again proves the tree feeds real execution, not just emission.

*(Delegated action — Dev ran `sbt test`: **197/197, all pass.**)*

197/197 — increment 2 closes green. The corpus now holds 24 expression trees exercising every node kind introduced so far — path join (`.`), function arrow (`=>`), nested infix (`*` inside `==`), application (`()`), and a quoted literal leaf — and each one feeds real execution: the application tree builds the live Evrete `Knowledge`, and the infix trees compile into the RETE `where` conditions that `PrimesRulesTest` fires.

This is the commit boundary. What the two sessions of work established, in one paragraph: expressions now have a normative structured form where the operator symbol is the discriminator, string-vs-object structure alone separates the host-opaque tail from drake-native trees, fixity is confined to the future authoring surface, and the procedural Generator renders trees flat with the one subtle rule (trees in String-typed slots emit as quoted literals). The remaining tail — lambdas, string interpolations, `#::`, `_` placeholders — is exactly what increment 3's parser will measure and convert mechanically, and that's a fresh session-sized piece whenever you want to pick it up.

---

**[Dev]** Try using the Haskell syntactic form in place of the scala lambda tail, with the assumption that Generator[ScalaSource] will know how to convert that to scala.

**[Claude]** Good — this targets the biggest host-opaque value in the corpus. The plan: introduce two Haskell-form nodes — `"\\"` (lambda: `\e a -> body`, params then body) and `"if"` (`if c then t else e`) — author Value's expressions as trees in that form, and the procedural Generator (today's stand-in for Generator[ScalaSource]) renders them to Scala (`(e, a) => body`, `if (c) t else e`). *(Implemented across Value.json, Value.drake, Generator, and drake.dlt.)* The Haskell-form conversion of Value is complete — the corpus's single largest host-opaque expression is now a fully structured tree. What changed:

**Value.drake** now reads in Haskell form:
```text
loc pathValue Json pathElements.foldLeft(Option(_source))(\e a -> e.flatMap(\j -> if j.isArray then j.hcursor.downN(a.toInt).focus else j.hcursor.downField(a).focus)).orNull
= if pathValue != null then pathValue.as[T].getOrElse(null.asInstanceOf[T]) else null.asInstanceOf[T]
```

**Value.json** carries both values as trees — the `foldLeft` chain decomposes into nested `()` applications, two `"\\"` lambda nodes (`["e","a", body]` and `["j", body]`), and an `"if"` node; the method result is an `"if"` over a `"!="`.

**The renderer** gained the ScalaSource projections: `"\\"` → `(e, a) => body` (bare param when single), `"if"` → `if (c) t else e`, `"!="` joins infix. Both trees render back to Value.scala:10-11 character-for-character, so the committed source is untouched.

The design principle this step establishes (now in drake.dlt's HASKELL-FORM SURFACE note): **the form is drake's, the atoms may be the target's.** Drake adopts Haskell's spellings for expression *constructs* — `\params -> body`, `if c then t else e` — because the tree is form-neutral and each `Generator[L]` target renders its own concrete syntax. But host-bound *vocabulary* inside those constructs (`null`, `asInstanceOf[T]`, `hcursor`) passes through verbatim: those are JVM/circe atoms, not syntax, and a Haskell target would need genuinely different bindings there, not different spelling.

Please run `sbt test`. This exercises the deepest tree yet (five levels of nesting) through the Local-statement and dyn-result emission paths. If green, the operator set stands at `.` `=>` `()` `\` `if` `*` `==` `!=`, and the remaining lambda-tail item is Primes' `_ % p != 0` — whose Haskell form forces the underscore-placeholder question (it becomes a named lambda `\n -> n % p != 0`), a tidy next step.

---

**[Dev]** Remember in Action.json:


```
{
  "=>": [
    {
      ".": ["org", "evrete", "api", "RhsContext"]
    },
    "Unit"
  ]
}
```


Before I run sbt test, should we look at replacing "=>"with "->" since we just put "->" in the lambda expression?

**[Claude]** Yes — and now is the cheapest moment it will ever be: exactly one tree in the corpus uses `"=>"` (Action.json), so the rename is a single-key edit plus one renderer case. It's also the principled call, not just consistency housekeeping: we just established that *constructs take Haskell's form and each target renders its own spelling*. `=>` is Scala's spelling of the arrow; Haskell's is `->` — for the type arrow, the lambda, and case alike. The tree key is drake's canonical symbol, so it should be `->`, with ScalaSource rendering ` => ` exactly as it renders `\e a -> body` as `(e, a) => body`. Note there's no collision: the lambda node's key is `"\\"` (structure separates params from body, no arrow token in the tree), so `"->"` is free for the arrow.

One consequence falls out nicely: the String-slot quoting rule already quotes the *rendered target form*, so the generated Scala stays `"org.evrete.api.RhsContext => Unit"` untouched — the `=>` in the committed source is ScalaSource's spelling of drake's `->`.

*(Applied the four pieces.)* The re-key is complete — `"->"` is now drake's canonical arrow:

- **Action.json**: the tree key is `"->"`; no `"=>"` key remains anywhere in the corpus (verified).
- **Renderer**: `case "->"` renders ` => ` — the ScalaSource spelling of drake's arrow, same pattern as the lambda.
- **Action.drake**: the value is now the *unquoted* expression `org.evrete.api.RhsContext -> Unit` — an expression rather than a quoted literal, matching the tree it corresponds to; the String-typed slot is what supplies the quoting at render time.
- **drake.dlt**: the OPERATORS entry documents `"->"` as Haskell-form with the ScalaSource rendering, including the String-slot example showing how the quoted rendering carries the target spelling.

One related item I deliberately left alone: Pattern.json still carries `"org.evrete.api.Knowledge => Unit"` as a quoted host-opaque *string* (never converted to a tree). It keeps Scala's `=>` because it's inside a literal — it will get the same `->` tree treatment as Action's when it converts, and nothing needs to change there now.

The operator set is now fully drake-canonical: `.` `->` `()` `\` `if` `*` `==` `!=` — with ScalaSource as the one implemented projection. Ready for your `sbt test`.

*(Delegated action — Dev ran `sbt test`: **197/197, all pass** — every run this session was green on the first try.)*

197/197 — the Haskell-form work is verified end-to-end: the five-level Value tree renders through Local-statement and dyn-result emission, `Generate Value` and the Value comparison both match, and `TestValue` exercises the actual JSON-path extraction the tree defines.

Green across the board, and this closes a genuinely significant piece of design work. Where things stand:

**The corpus now demonstrates the full three-layer model on real code.** Value's `foldLeft`-with-nested-lambdas — the gnarliest expression in the metamodel — exists as: a Haskell-form drake surface (`\e a -> …`, `if … then … else …`), a form-neutral JSON tree (`\`, `if`, `()`, `.`, `!=` nodes), and a ScalaSource rendering that reproduces the committed source character-for-character. The arrow re-key made the operator vocabulary fully drake-canonical: `.` `->` `()` `\` `if` `*` `==` `!=`, with Scala's `=>` now existing only as ScalaSource's *rendering* of drake's `->`.

**The principle that emerged today** — form is drake's, atoms may be the target's — is the piece that makes a future HaskellSource projection credible: the same trees render `\e a -> e.flatMap …` there with zero re-authoring, while genuinely host-bound atoms (`null`, `hcursor`) are exactly the residue a port would have to rebind anyway. The trees make that residue enumerable.

This is the commit point. Suggested message:

```
Expression trees: TypeElement.value String → Json; Haskell-form constructs
(\ lambda, if-then-else, -> arrow) with ScalaSource rendering; Value fully
tree-native; Seq.empty/Primes conditions/knowledge as trees; RhsContext fix;
drake.dlt EXPRESSIONS spec
```

The remaining host-opaque tail, in rough order of interest: Primes' `_ % p != 0` (forces the underscore-placeholder decision — Haskell form makes it a named lambda `\n -> n % p != 0`), the `#::` cons operator, the `s"…"` interpolations in rule actions, and the big Monadic codec blocks. Those are next-session material — as is increment 3's parser, which now has eight operator kinds of ground truth to parse back.

## Status
Corpus stands at **64 JSON / 64 `.drake`** with the expression model now structural: `TypeElement.value` migrated `String → Json`, and value expressions carry a normative S-expression tree form — a single-key JSON object whose **key is the operator symbol** (`{op: [operands]}`), with bare-string leaves as references, embedded-quote leaves as literals, and legacy strings coexisting as the measured host-opaque tail (`value.isObject` alone separates them; no flag-day). New this chapter: the fixity design (flat parse + declaration-resolved fixity, Haskell/Agda style — surface-only; JSON carries the resolved tree), `Generator.expression`/`initializer` (trees in String-typed slots emit as quoted literals), the `RhsContext` spelling fix the opaque string had hidden, drake.dlt's EXPRESSIONS spec and HASKELL-FORM SURFACE note, and the principle **form is drake's, atoms may be the target's** — constructs take Haskell's spelling (`\` lambda, `if-then-else`, `->` arrow; Scala's `=>` is now only ScalaSource's rendering), while host-bound vocabulary passes through verbatim. Tree buildout: Action's arrow tree → 20 `Seq.empty` `.`-trees → the Primes nested infix conditions (`*` inside `==`, firing through Evrete) → the `()` application tree building the live Knowledge → Value's five-level Haskell-form trees (the corpus's gnarliest expression, now fully tree-native). Drake-canonical operator set: `.` `->` `()` `\` `if` `*` `==` `!=`. Full suite **197/197** — five runs this session, every one green on the first try. Queued: the host-opaque tail (`_ % p != 0` and the underscore-placeholder decision, `#::`, `s"…"` interpolations, Monadic codec blocks, Pattern.json's quoted arrow string), increment 3's surface expression parser (mechanized batch conversion + tail report), the two lints carried from ch.61 (`Monadic`-bare-expression; host-reserved domain names), and the **JSON → `.drake` emitter** still waiting from ch.61's plan.
