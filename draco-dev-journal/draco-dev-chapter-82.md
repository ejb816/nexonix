# Chapter 82 - Text and Definition-Backed Domain-Aspect Generation

**Session date:** September 22, 2026
**Topic:** Draco-owned text through the generator and the first complete definition-backed aspect section.

**Provenance:** Selected exchanges from the Codex continuity conversation, including
its voice transcript handoffs and Dev's pasted test and push output. Voice repetitions,
UI troubleshooting and an accidental recording of an unrelated conversation are omitted.
The opening Text implementation was in another task: its detailed dialogue is not
reconstructed here. The explicitly labeled summary below uses the committed tree and
the September 22 git records at 1231, 1331, 1339 and 1453. No model variant is inferred.

## From Text to Emission

*Editorial checkpoint, not reconstructed dialogue:* Text was introduced as a
definition-backed member of draco.drake, with the qualified identity realized as
Scala String rather than a wrapper. Nominal was its first use. Four focused TextTest
tests covered the mapping, Nominal, Unicode, equality and joining, alongside the
per-definition gates. The recorded full suite passed 636 tests across 45 suites.

**[Dev]**

Tell me about surface, domain line, and domain line of, 'cause I have questions about that. Those were, um, I believe, transitional names that I would like to replace

**[Codex]**

They're small definition-backed building blocks right now. Surface holds rendered text
and derives from Primal of Scala String. DomainLine derives from Surface; its factory
takes a package and domain name and formats a Drake domain declaration. DomainLineOf
derives from DomainLine; it takes a type definition, extracts its containing domain
reference, and passes that package and name to DomainLine. One correction, though:
the main Drake emission rule still calls the handwritten emitter, so these aren't
the main path yet. The names don't need to be treated as settled.

---

**[Dev]**

Well, the, um... type definition is divided up into five aspects. There's the fundamental type aspect, which is referred to as Draco aspect. Then there's a domain aspect. Then there's a- rule aspect, and an actor aspect, and then finally a codec aspect, and all of those are what need to be generated for... can be generated for a complete definition, although there can be any subset of those as a complete definition

**[Codex]**

That puts the aspects at the center, not individual output lines. DomainLine is just
one piece of domain-aspect generation, not the abstraction we should build the design
around. I got ahead of that by focusing on migrating those line types.

**[Dev]**

So, uh, one constraint is that... um, type... and... domain are in all definitions. Um it's not domain is an identical in all definitions. It's in domains, it is the reference to the domain itself with the package name, and in another type that's not a domain, it's the reference to the domain that that type is a member of

**[Codex]**

That corrects my "any subset" statement. Type and domain are required aspects; rule,
actor, and codec are optional. For a domain definition the domain reference identifies
itself, including its package; for a non-domain type it identifies the containing domain.
Generation has to keep that distinction intact, even if the text form looks the same.

---

**[Dev]**

So, given that, is there a reason to replace domain line and domain line of... and promote it up to, talking about domain aspect?

**[Codex]**

Yes, domain aspect is the better public abstraction, but more than a rename. The
current pair only renders the domain reference; full domain-aspect generation also
needs membership and, for transform domains, source and target. I'd keep the separation
between the target representation and the transformation that produces it, but name
both around the domain aspect, leaving the line formatter internal.

**[Dev]**

Sounds good. Is there anything we need to do to change those definitions now, or are we just going to queue that up for when there's an appropriate change to be made

**[Codex]**

I'd queue it up. Renaming now would suggest coverage those types don't yet provide.
For now, keep the existing types and tests intact. When we implement full domain-aspect
generation, that change should update the Drake definitions, JSON, generated Scala,
and tests together. Nothing's been changed or filed as an issue.

---

*Editorial bridge:* Dev accepted the narrower migration first. Surface, DomainLine and
DomainLineOf were switched to Text without renaming; Emission followed as a separate
accepted increment. The generated Scala stayed byte-identical. The records distinguish
generation checks from full-suite verification; no commit or push was performed by Codex.

## The Complete Domain Section

**[Dev]**

Can you determine what the next most important change in draco should be?

**[Codex]**

My recommendation is to complete definition-backed domain-aspect generation and connect
it to the running generator, including self versus containing-domain references, type
parameters, membership, and transform source and target, then prove no output change.
The super-domain data lives in DracoAspect, so we should preserve that ownership explicitly.
I also found a newer full-suite run, all passing. I haven't made any new implementation changes.

**[Dev]**

Yes, let's proceed with your recommendation

*Editorial implementation summary:* DomainAspectText and DomainAspectOf were added as
definition trios. Drake.emit now delegates its complete domain section to DomainAspectOf;
the existing Emit rule still calls Drake.emit, so the transform is reached by the running
rule and actor flow. This is not a replacement of all handwritten generation.
Type-parameter spelling remains an explicit Json-to-Text callback to the Drake renderer.
The section includes the super-domain declaration without moving its ownership out of
DracoAspect. Membership order, parameterized references, transform direction, nameless
domains and missing-aspect compatibility behavior are covered. DomainLine and DomainLineOf
remain available rather than being renamed into an abstraction they did not implement.

*(Delegated action - Dev ran the full sbt suite: 652 tests passed, zero failed,
canceled, ignored or pending. The inspected run covered 46 suites. DomainAspect
reproduced 101 complete sections including 1 parameterized section, skipping none;
GenDrake emitted 99 of 99 definitions; parse scope was 101 draco + 10 mods. The
one known surface loss remained across 111 types. Example generation stayed 28
match / 20 differ / 0 error / 0 missing; PON stayed 42 discrepancies and 7 canonical
differences; all 23 scenario files were clean. The Evrete co-declaration limitation
remained visible. The file-only mods-actor authoring baseline stayed zero.)*

---

*Editorial handoff note:* Dev supplied an old commit command as a format example,
explicitly not as the desired paths or message for this increment. Codex prepared
a file-backed commit-message command for the combined work and four records; Dev
executed it. This distinction matters: the example was not authority to commit
the earlier record or to execute git on Dev's behalf.

*(Delegated action - Dev's pasted output confirms commit `7de07ba`, "Introduce Draco
Text and definition-backed domain-aspect generation": 42 files changed, 1158 insertions,
63 deletions, followed by a successful push of main from `8e59cbe` to `7de07ba`.)*

## Status

Implementation committed and pushed by Dev as `7de07ba`; full verification is 652 tests
/ 46 suites, with the report-only baselines above unchanged apart from intended corpus
growth. Text now serves Nominal, Surface, DomainLine, DomainLineOf and Emission.
The live Drake path has definition-backed domain-section generation, not complete
definition-backed generation of all five aspects. Type and domain are required in
Dev's intended complete definitions; compatibility with incomplete loader stubs is
not a reversal of that design. No next feature is authorized by this chapter.

The initial Text task and omitted voice exchanges are not represented as a complete
transcript. The committed records supply the technical checkpoint; chapter 83 establishes
how both development agents carry it forward without treating summaries as Dev's words.
