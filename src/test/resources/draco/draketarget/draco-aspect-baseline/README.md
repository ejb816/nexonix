# Pre-Migration Draco Sections

ActorAspect and BodyElement have deliberately authored-ahead Drake sources. These
two fixtures preserve the type sections emitted from their JSON by the pre-migration
Drake emitter (implementation checkpoint 7de07ba, still current at 79b363f).
They were captured on 2026-09-28 before comparing that emitter with DracoAspectOf.

DracoAspectTest uses these fixtures instead of excluding the definitions or pretending
their authored Drake is emitter-canonical. Every other corpus type is compared directly
with its authored type section. Do not regenerate these snapshots from the new emitter
to make a failure pass. If the authored-ahead discrepancy is resolved, replace the
snapshot comparison with the actual authored surface as part of that explicit change.
