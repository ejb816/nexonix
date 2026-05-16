# Draco Dev Journal — Chapter 35

**Session date:** May 16, 2026
**Topic:** Quick follow-on from chapter 34: closed #24 by adding the explicit `isLeaf` predicate to `Generator.scala` and restructuring `generate()`'s dispatcher to read as a flat 5-way table with a defensive exhaustivity guard. One mid-flight reversal when the naive `else if` rewrite failed type-checking — corrected by adding a throw-only final `else`. The dev then surfaced IntelliJ warnings unrelated to #24 but in the same file; spawned a follow-on issue #27 and closed it in-session.

---

## Pickup

Chapter 34 closed with the suggestion of #24 as a quick win. The dev came back the next prompt asking for an explanation, accepted, and gave the go-ahead:

> **Dev:** Proceed with changes to close #24

The agent moved `priority-next` from "unclaimed" onto #24 via `gh issue edit` and looked up the dispatch site.

---

## Reading the dispatcher

`Generator.scala` had four named structural predicates — `isDomain`, `isRule`, `isActor`, `isObjectOnly` — at lines 904–919. The `generate()` dispatcher at 1043–1097 used three of them as explicit branches and lumped leaves and actors together in the final `else`:

```text
if (isRule(td))            { rule emission }
else if (isDomain(td))     { domain emission }
else if (isObjectOnly(td)) { object emission }
else {
  val isActorType = isActor(td)
  val nameSuffix = if (td.typeName.name.endsWith(".actor")) "Actor" else ""
  ...
}
```

So `isActor` was *used* (to set `instanceType` and indirectly `nameSuffix`) but didn't appear as a top-level dispatch case. Leaves had no predicate at all — they were inferred from "fell through everything else."

The issue's ask was small: name the negative case so the dispatch table reads as a flat list.

---

## First attempt — and the type-check trap

The agent defined `isLeaf` next to the others:

```text
private def isLeaf (td: TypeDefinition) : Boolean =
  !isDomain(td) && !isRule(td) && !isActor(td) && !isObjectOnly(td)
```

Then changed the final `else { ... }` to `else if (isLeaf(td) || isActor(td)) { ... }`. The intent was to make every branch explicit — no silent fall-through.

The agent caught the trap before writing anything to disk that would fail compilation: with the final `else` gone, Scala's if-chain type-checker can no longer guarantee a `String` return. The expression's result type would degrade to `AnyVal` (String or Unit), and `generate()` is declared to return `String`. Compile error.

Two ways out:
1. **Comment-only fix**: keep `else { ... }` and just reference `isLeaf || isActor` in a comment above. Satisfies the issue's "or comments why it's a fall-through" acceptance, but leaves `isLeaf` as essentially dead code only invoked in a textual comment.
2. **Explicit + defensive else**: `else if (isLeaf(td) || isActor(td)) { ... } else { throw ... }`. The throw is unreachable but tells the type-checker the chain is total.

The agent first wrote option 1 and reflected aloud:

> **Agent:** `isLeaf` would be dead code with only a comment reference. Let me make the dispatcher genuinely use it, with a defensive final `else` to keep type-checking happy.

…then committed to option 2. The unreachable throw isn't decorative — it's an exhaustivity guard. If a future change to one of the predicates breaks the partition (e.g., adds a 6th category without updating the dispatcher), the throw fires loudly at runtime instead of silently dropping the type.

```text
} else if (isLeaf(td) || isActor(td)) {
  // Leaves and actors share the unified trait+global emission ...
} else {
  // Unreachable: the four predicates above partition the type space.
  // This branch exists only to satisfy if-chain exhaustivity; if it fires,
  // a predicate has drifted and Generator.generate has lost a case.
  throw new IllegalStateException(s"Generator.generate: no branch matched ${td.typeName.name}")
}
```

> **Dev:** passed

246/246 still green.

---

## Skipped: TypeDefinition exposure

The issue's optional clause — "expose `isLeaf` as a method on `TypeDefinition` if it reads more naturally there" — was deferred. None of the existing predicates (`isDomain`/`isRule`/`isActor`/`isObjectOnly`) live on `TypeDefinition`; adding only `isLeaf` there would be inconsistent. Exposing all five would be a separate, larger reshuffle and outside #24's scope.

---

## Adjacent IDE warnings — issue #27

After #24's edits compiled and the chapter-35 markdown errors were fixed (the IntelliJ Scala-injection trap, captured in `feedback_markdown_scala_fence.md`), the dev surfaced six IDE warnings in `Generator.scala` — all unrelated to the dispatcher work but visible because the file was open.

> **Dev:** Make a new issue for fixing those errors and warnings, fix the remaining warnings and update chapter 35 about the changes while we are still in this session.

The agent filed [#27](https://github.com/ejb816/nexonix/issues/27) ("Generator.scala IDE warnings: redundant blocks, dead branch, constant param"), labels `cleanup,generator,priority-next`, with all six warnings catalogued. `priority-next` moved off #24 onto #27 to keep the queue marker honest.

### Catalog and fixes

1. **Line 60** — `loadFromResource(resourcePath(typeName, aspect, "json"))` passed `"json"` explicitly, which is already the default of `resourcePath`'s `ext` parameter. Dropped the explicit arg.

2. **Line 271** — `s"${bodyPad}${line}"` simplified to `s"$bodyPad$line"`. Both interpolation blocks contained simple identifiers; the braces added noise.

3. **Line 281** — `case other => s"${bodyPad}${other.value}"` simplified to `s"$bodyPad${other.value}"`. The `${other.value}` braces had to stay (member access requires them); the `${bodyPad}` braces were redundant.

4. **Line 283** — `s"{\n${init.mkString(...)}\n${last}\n${bracePad}}"` simplified to `s"{\n${init.mkString(...)}\n$last\n$bracePad}"`. Same pattern: `init.mkString(...)` needs braces, `last` and `bracePad` don't.

5. **`factoryBody()`** — both branches of the inner `if (factory.body.nonEmpty)` ended with the **identical** template `s"{\n${(overrides ++ instanceOverrides).mkString("\n")}\n  }"`. IntelliJ correctly observed the function "always returns" that shape. Refactored: compute `overrides` once via if-else (different shape per branch), then emit the template at the bottom. Strictly equivalent; the dead-branch warning is gone.

6. **`fieldElisionEncoder`** — `instanceVar: String` parameter was always called with `"x"` (one caller at line 523). Dropped the parameter; inlined `"x"` directly. Caller updated to `fieldElisionEncoder(params)` instead of `fieldElisionEncoder(params, "x")`.

7. **`encoderFieldLines`** — same pattern, surfaced after the first round of fixes when the dev re-ran inspections. `instanceVar: String` was the first parameter, always called with `"x"` (one caller at line 548). Same fix: dropped the parameter; inlined `"x"`. The agent had missed this on the initial sweep because the grep used to find the pattern only caught the `fieldElisionEncoder` site — `encoderFieldLines` has the same warning but a different surrounding signature.

8. **`elisionCheck`** — surfaced *after* fixes 6 and 7 propagated. Before the cascade, the two callers (`fieldElisionEncoder` and `encoderFieldLines`) both passed their own `instanceVar` parameter through to `elisionCheck`. IntelliJ's inspection couldn't conclude `elisionCheck`'s parameter was always `"x"` because each caller's value was an opaque variable. Once 6 and 7 inlined `"x"` at the call sites, the data-flow analysis caught up: now `elisionCheck` is always invoked with `"x"`. Same fix again: dropped the parameter, inlined `"x"` in all 7 case branches and the two call sites. The grep-and-replace was mechanical (`elisionCheck(p, "x")` → `elisionCheck(p)`).

> **Dev:** [tests passed]

All eight warnings resolved. The factoryBody dedup is the only one with structural weight — it cuts a duplicated template. The others are pure noise reduction.

### Cascade lesson

The `instanceVar`-always-`"x"` warning had three landings (#6 `fieldElisionEncoder`, #7 `encoderFieldLines`, #8 `elisionCheck`) because the IDE's data-flow analysis is layered. Fixing the leaf-level functions (those whose `instanceVar` came directly from a caller's literal) doesn't immediately surface the next layer up — IntelliJ has to re-evaluate after the parameter is inlined to discover that the next function's parameter is also constant. So the user's iterative paste-the-warning workflow turned out to be necessary: there was no single grep that would have caught all three at the same time, because the second and third were *masked* by the first.

Next time a "param always X" warning surfaces, after fixing it, mentally walk one layer up: do any *callers* of the just-fixed function now pass a literal? If so, those will become the next warning.

### Note on scope

These warnings were pre-existing; they had nothing to do with #24. The agent had flagged them as out-of-scope in the previous turn ("not chasing them in scope") — which was procedurally correct (don't bundle unrelated cleanup into a feature commit) but the user reasonably preferred to land them in the same session while the file was hot. Filing a separate issue (#27) preserves the audit trail without forcing them into the #24 commit.

---

## Status at close

- **Closed**: (pending) #24 Add explicit `isLeaf` predicate to Generator, #27 Generator.scala IDE warnings
- **`priority-next`**: unclaimed
- **Tests**: 246/246 green
- **Files touched**: `src/main/scala/draco/Generator.scala` (isLeaf def + dispatcher restructure for #24; 6 warning fixes for #27)
- **Memory added**: `feedback_markdown_scala_fence.md` (IntelliJ Scala-injection on markdown fences)
- **Suggested next pickup** (still per the chapter-34 redirect):
  - [#11](https://github.com/ejb816/nexonix/issues/11) Generator domain (`Generator[L]`) — the larger endogenous move
  - [#18](https://github.com/ejb816/nexonix/issues/18) `value` field loss on YAML round-trip — concrete latent bug
  - [#9](https://github.com/ejb816/nexonix/issues/9) `draco-gen discover`/`verify` modes — tooling usability

Natural commit message: `Add isLeaf predicate; restructure dispatcher; clean Generator IDE warnings (closes #24, closes #27)`. Default branch is `main`, so the `closes` footers should auto-fire on push.

---

## Note for future readers

The trap in this chapter — "removing `else` from an if-chain quietly degrades the return type" — is the kind of thing where the agent's *type-checking instinct* beats the issue's literal text. The issue said "use isLeaf explicitly," and the most explicit reading was "make it a branch." Doing that naively breaks the chain. The defensive-throw form is the small piece of structure that lets the explicit dispatch coexist with the type-checker's exhaustivity expectation.

Worth remembering when reading similar "small restructure" tickets: an if-else chain isn't the same shape as a `match` expression, and the compiler's tolerance for non-exhaustive `match` doesn't extend to non-final `else if`.
