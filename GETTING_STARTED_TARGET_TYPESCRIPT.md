# Getting Started — Target: TypeScript

**Status: stub.** There is no TypeScript projection yet. This file holds the structure and
the open questions, to be filled in as we gain experience with the toolchain.

One of a family — see also `GETTING_STARTED_TARGET_SCALA.md` (the realized target) and
`GETTING_STARTED_TARGET_HASKELL.md`. All three share one structure, because **only
sections 1, 4, 5 and 6 are genuinely target-specific.** Section 0 is the same everywhere
and is already true for TypeScript.

---

## 0. What is the same for every target

Authoring is target-independent. You write a **definition**; a **target** projects it.

```text
  author X.json          the definition — normative, the only form loaded at runtime
     |
     +-- X.drake         the surface — the same content, written for humans
     |
     +-- project ------> X.ts        source in a programming language
     |
  register X in its domain's elementTypeNames
     |
  verify                 the gates hold definition, surface and projection together
```

The definition format is described in `README.md`; the surface language is specified in
`src/main/resources/draco/drake.dlt`. Neither changes for TypeScript.

**TypeScript tests a different assumption than Haskell does.** Haskell stresses whether
the *structure* is neutral. TypeScript stresses whether *identity* is — because its type
system is structural, while draco's is nominal.

---

## 1. Toolchain

*To be written.* Expected shape: **Node.js** (LTS), a package manager, and the
**TypeScript compiler**, with a version pinned and the reason recorded — the way the
Scala target pins JDK 17.

---

## 2. Clone

Identical to every target:

```bash
git clone https://github.com/ejb816/nexonix.git
cd nexonix
```

---

## 3. Build and test

*To be written.* As with Haskell, this target has no self-hosting shortcut: the framework
is not written in TypeScript, so "green" means projecting the corpus and type-checking the
result, not compiling the framework's own source.

One thing already worth knowing: TypeScript's compiler erases types at runtime. A gate
that only type-checks proves less here than compilation proves for Scala, so this target
likely needs runtime checks that the Scala target gets for free.

---

## 4. Project a definition into TypeScript

*To be written.* This is the substantive section, and it is where the open questions live.

### Open questions this target must answer

| Question | Why it is open |
|---|---|
| **Nominal versus structural identity** | The deepest one. Draco's `TypeName` identity is nominal — `Meters` and `Radians` are different types even where both carry one number. TypeScript's type system is structural and would consider them interchangeable. Branding, private markers, or runtime tags are the usual workarounds, and the choice affects every projected type. |
| **How does a type project?** | `interface` (erased, no runtime presence) or `class` (present at runtime, but forces a constructor shape)? A type must carry its own `typeDefinition`, which argues for a runtime presence. |
| **How does derivation project?** | `extends` on interfaces is structural widening, not the nominal derivation draco means. Multiple derivation compounds this. |
| **Where does `typeDefinition` live?** | There is no companion. A module-level constant, a static member, or an entry in a registry — and the answer interacts with how definitions are loaded. |
| **How are definitions loaded?** | The Scala target resolves through a `DefinitionPath` over classpath roots. TypeScript has no classpath: module resolution, a bundled asset directory, or fetch. `DefinitionPath` was designed to be constructible from given roots precisely so this is answerable. |
| **Codec** | The one place TypeScript is *easier*: JSON is native. But erasure means decoding cannot be checked by the type system, so validation is a runtime concern. |
| **Rule evaluation** | No RETE engine in the ecosystem comparable to the Scala target's. Same conclusion as Haskell: `draco.rete` has to express discipline, not an engine. |
| **Actors** | No native actor runtime. Async and message-passing exist; the mapping does not. |
| **Type expressions** | `[T]` maps to an array type, `{K, V}` to a record or `Map`, `A -> B` to a function type, `(A, B)` to a tuple — all close. The friction is not in the constructors but in identity. |

### What is already known to be unfavourable

The nominal/structural mismatch is not a detail. Draco's central claim is that a `Meters`
value carries its lineage and a transformation can check semantic compatibility at the type
level. In a structural system that check does not hold by default. Whatever this target
does about branding *is* the semantic-preservation story for TypeScript, and it should be
decided deliberately rather than fallen into.

---

## 5. Run and use

*To be written.* Follows from §4.

---

## 6. Command reference

*To be written.* Expected to be the projection CLI with a target selector, not a parallel
command set.

---

## Troubleshooting

*To be written as problems are actually encountered.*

---

## Optional: an IDE

*To be written.* **VS Code** is the obvious default, with the TypeScript service built in.
