# Getting Started — Target: Haskell

**Status: stub.** There is no Haskell projection yet. This file holds the structure and
the open questions, to be filled in as we gain experience with the toolchain.

One of a family — see also `GETTING_STARTED_TARGET_SCALA.md` (the realized target) and
`GETTING_STARTED_TARGET_TYPESCRIPT.md`. All three share one structure, because **only
sections 1, 4, 5 and 6 are genuinely target-specific.** Section 0 is the same everywhere
and is already true for Haskell — nothing about authoring a definition depends on the
target.

---

## 0. What is the same for every target

Authoring is target-independent. You write a **definition**; a **target** projects it.

```text
  author X.json          the definition — normative, the only form loaded at runtime
     |
     +-- X.drake         the surface — the same content, written for humans
     |
     +-- project ------> X.hs        source in a programming language
     |
  register X in its domain's elementTypeNames
     |
  verify                 the gates hold definition, surface and projection together
```

The definition format is described in `README.md`; the surface language is specified in
`src/main/resources/draco/drake.dlt`. Neither changes for Haskell. What a Haskell target
must supply is a toolchain (§1), a projection (§4), a way to run the result (§5), and a
command set (§6).

**Haskell is the reason this separation matters.** It is the target most unlike Scala, so
it is the one that will expose any place where a "neutral" description is quietly Scala's.
Every such discovery belongs in `README.md`'s *Language-specific residues* table.

---

## 1. Toolchain

*To be written.* Expected shape: **GHC**, **Cabal** or **Stack**, and **HLS** for editor
support — most likely installed through **GHCup**. A version must be pinned, the way the
Scala target pins JDK 17, and the reason recorded.

---

## 2. Clone

Identical to every target:

```bash
git clone https://github.com/ejb816/nexonix.git
cd nexonix
```

---

## 3. Build and test

*To be written.* The question this section answers is what "green" means for a target
whose projection is not the language the framework itself is written in. The Scala target
gets checked by compiling the framework's own source; a Haskell target has no such
self-hosting shortcut and needs its own gate — most likely projecting the corpus and
compiling the result.

---

## 4. Project a definition into Haskell

*To be written.* This is the substantive section, and it is where the open questions live.

### Open questions this target must answer

Each of these is a place where the current projection is shaped by Scala and the neutral
answer is not yet known. They are the reason this file exists.

| Question | Why it is open |
|---|---|
| **How does a type project?** | Scala uses a trait plus a companion object. Haskell has no companion — module-level bindings, or a record, or a typeclass? The answer determines what `globals` means. |
| **How does derivation project?** | Scala's `extends`/`with` is subtyping. Haskell has no subtyping: a derivation might become a typeclass constraint, a record with an embedded parent, or a sum. This is the single biggest structural question. |
| **What carries `typeDefinition`?** | Scala attaches it to the companion. In Haskell it might be a typeclass method, or a top-level value per type. |
| **How are `Primal` and `Holon` expressed?** | `newtype` is the obvious fit for `Primal(T)`. `Holon(T)` over a tuple is less obvious. |
| **Structural identity** | Scala authors equality members explicitly. Haskell would `deriving (Eq)` — which is exactly the argument for making identity a *declared property* rather than authored members (a residue in `README.md`). |
| **Codec** | Scala derives from a JSON library's encoder/decoder pair. Haskell's equivalent is a different shape, which is the argument for a serialization capability domain. |
| **Rule evaluation** | The Scala target binds a RETE engine. There is no drop-in Haskell equivalent, so this is where `draco.rete` has to become *evaluation discipline* rather than one engine's configuration. |
| **Actors** | Same problem: the Scala target binds a specific actor runtime. Haskell has several concurrency models and no single obvious mapping. |
| **Type expressions** | `[T]`, `{T}`, `{K, V}`, `(A, B)`, `A -> B` are already close to Haskell's own spelling — arguably closer than to Scala's. This target should be cheap here, and is a good early check that the neutral forms are genuinely neutral. |

### What is already known to be favourable

The surface language was designed with Haskell-form spellings in mind: one arrow token
`->` for both function types and function values, `\p -> body` for abstraction,
`if c then t else e`. Those choices were made for neutrality and should project directly.

---

## 5. Run and use

*To be written.* Needs an answer to §4 first — how a projected type is constructed and
used follows from how it is projected.

---

## 6. Command reference

*To be written.* The projection CLI is expected to take a target selector rather than
gaining a parallel command set; that is the point of a target-parameterized generator.

---

## Troubleshooting

*To be written as problems are actually encountered.* Writing this section speculatively
would be guessing.

---

## Optional: an IDE

*To be written.* Likely **VS Code** with the Haskell extension backed by HLS.
