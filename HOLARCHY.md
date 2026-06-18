# HOLARCHY.md — The Holarchy of Perspectives

> **Status:** Conceptual reference, not yet implemented. This document characterizes
> an external model that informs draco's `Holon` / perspective / transform design.
> It is expected to expand and be refined incrementally as it becomes relevant to
> implementation — treat the specifics as provisional.

## Source

The model is the **eight-level holarchy** described in *Corpus Lucis Terrae* at
[wuwei.org/corpus-lucis-terrae](https://wuwei.org/corpus-lucis-terrae) (the Wuwei
Foundation). At time of capture (2026-06-14) that page is under construction; the
levels below and the surrounding framing (covenants, Bā Guà association, the
"Holonic Structure of Human Social Forms", "Guilds of the Holons", "Sicut Supra –
Sic Infra") are recorded from it but will be elaborated there over time.

## The eight levels

A **holarchy of perspectives**: nested holons from the innermost individual
perspective outward to the cosmic. **Egocentric is the root (innermost)
perspective** — the only true `Holon` in draco's current vocabulary, here placed as
level 1.

| Level | Holon | Perspective / scale |
|------:|-------|---------------------|
| 1 (root) | **Source · Soul** | Egocentric / the individual |
| 2 | **Soul · Kin** | Family |
| 3 | **Kin · Hold** | Extended family |
| 4 | **Hold · Clan** | Clan |
| 5 | **Clan · Tribe** | Tribe |
| 6 | **Tribe · Terrain** | Terrain / region |
| 7 | **Terrain · World** | World / planet |
| 8 | **World · Star** | Cosmic / all worlds |

Each level is associated with a **trigram from the Bā Guà** (eight trigrams ↔ eight
levels), and the relationships *between* levels are governed by **covenants** that
"describe the right relationships between levels."

## Why this matters to draco

Three structural correspondences make this more than thematic — the holarchy and
draco's transform machinery appear to be the *same shape*:

1. **Holon naming = the transform naming convention.** Each level is named by the
   *boundary between two adjacent perspectives* — `Source·Soul`, `Soul·Kin`,
   `Kin·Hold`, … A holon *is* the membrane between an inner and an outer
   perspective. This is exactly `DomainTransform[S, T]` and the
   `domains.<inner>.<outer>` / `<Source><Target>` convention. A holon and a
   single-hop transform are the same construct viewed two ways.

2. **Egocentric is the root.** Confirms draco's standing rule that **Holon =
   perspective** (interiority/exteriority of sense and effect within a reference
   frame), with `Ego` as the sole, innermost Holon. The astronomical reference
   frames (Egocentric → Geocentric → Heliocentric → Galactocentric → Cosmocentric)
   were a *placeholder* holarchy — perspective-by-origin — that this scale-based,
   covenant-governed holarchy supersedes and refines. See the decision to retire the
   centric corpus (chapter 41 follow-on) keeping only Egocentric's endogenous
   vocabulary.

3. **Covenants = inter-level transform rules.** "Right relationships between levels"
   read as the rules that govern transforms across holon boundaries — the analogue
   of the World super-domain's cross-domain transform rules. "Sicut Supra – Sic
   Infra" (As Above, So Below) is the recursive self-similarity already in draco's
   vocabulary as "holons all the way up and down."

## Relationship to the media domains

The current example media — `Aerial` / `Terrain` / `Marine` / `Ethereal` under the
`World` super-domain — draw two of their names (`Terrain`, level 6; `World`, level
7) directly from this holarchy. The media are *physical transport media*, while the
holarchy levels are *social/cosmic scales*; how the two axes reconcile (whether the
media are perspectives at particular scales, or an orthogonal axis that intersects
the holarchy) is **open** and deferred until it bears on implementation.

## Open questions (for incremental refinement)

- How the eight levels map onto concrete draco `Holon` / perspective types.
- Whether each adjacent pair becomes a transform domain (`level-N → level-N+1`) and
  the covenants become its transform rules.
- How the physical-media axis (Aerial/Terrain/Marine/Ethereal) intersects the
  scale-axis of the holarchy, given the `Terrain` / `World` name overlap.
- The role of the Bā Guà trigram association per level.
