# Example-domain resolution audit — October 22, 2026

Read-only audit at 375c513, following Dev’s acceptance. No definition, membership,
Scala source or test changed. Temporary compiled Scala probes called DomainBuilder,
NameResolution and DracoGenerator directly using the current compiled classes.
This is measurement, not a new full-suite result; verified baseline remains 861 / 61.

## Explicit scope

CanonicalScope's 18 domains plus domains.world.World, domains.aerial.Aerial,
domains.terrestrial.Terrestrial, domains.marine.Marine, domains.ethereal.Ethereal,
and domains.sentient.Sentient; HostBindings.vocabulary supplied explicitly.
The 48 JSON files were enumerated only to inventory and compare, never to populate
ontology membership. Existing anchors alone supplied the identity ontology.
Comparison uses ExampleDomainsGenTest's whitespace normalization.

## Findings

- 32 of 48 identities held: six domains and 26 declared members. Sixteen omitted members
  nevertheless point at those domains as their owners.
- Unscoped output reproduces the established 28 match / 20 differ / 0 error / 0 missing.
- 45 resolve and generate scoped; three stop at the first unknown name.
- Of the 45 scoped outputs, 34 equal unscoped output and 11 differ. All 11 differences
  reduce exactly to imports and qualification of Dracon, LocationReport or World.
- Scoped versus committed Scala: 23 match, 22 differ, 3 resolution errors. The reduction
  in matches is four Creator projections acquiring Dracon imports, plus Aerial Consumer
  failing resolution; it is not evidence of a runtime regression.

| Owning domain | Omitted members |
|---|---|
| Aerial | Consumer, Creator, Input, Position |
| Ethereal | Consumer, Creator |
| Marine | Consumer, Creator |
| Terrestrial | Consumer, Creator, Location, Output |
| World | Cartesian, Consumer, Observable, Provider |
| Sentient | None |

Initial resolution failures: aerial.Consumer names domains.aerial.AerialSink;
aerial.Input names io.circe.HCursor; world.Observable names Cartesian.
A diagnostic-only second scope supplied explicit bindings for those three identities
and domains.aerial.Position (also omitted). All 48 then resolved and generated. These
bindings were temporary probe inputs, not an accepted host vocabulary or a membership
repair; Cartesian and Position belong to their domains, not the host table. Resolution
is fail-fast, and successful resolution does not inspect host-opaque expression bodies.

The 20 established unscoped differences are not uniformly naming differences. Examples:
the four OriginateReport projections omit hand-written originate helpers; world.Consumer
and Provider omit actor factories/behavior; Observable omits geodetic helpers; Cartesian
omits its tuple factory. Position and Location gain inferred codecs. Terrestrial.Output
loses the hand-written message match and emits a reference to location without that binder.
Domain companions and three ConsumeReport objects also differ in companion/import conventions.
No generated examples were compiled or installed by this audit; those defects/differences
are source comparisons, not new runtime findings.

## Recommendation (accepted October 23)

Dev accepted this scope on October 23. First make membership reciprocal for the 16 existing definitions that already name these
owners, and add a membership consistency check. Preserve behavior and follow the trio rule;
resolve host bindings and projection differences as later decisions. Do not sweep the example
bodies or replace their hand-written Scala merely because name resolution succeeds.

## Per-file results

`held` means the declared identity scope contains the name. `resolve` is strict resolution.
`same` compares scoped to unscoped generation; `match` compares scoped to committed Scala.

```text
domains.aerial.Aerial	held=true	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
domains.aerial.ConsumeReport	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.aerial.Consumer	held=false	unscopedMatch=true	resolve=ERROR: Unknown type 'domains.aerial.AerialSink' in the supplied ontology and bindings	scoped=ERROR: Unknown type 'domains.aerial.AerialSink' in the supplied ontology and bindings
domains.aerial.Creator	held=false	unscopedMatch=true	resolve=OK	scoped=same=false,match=false
domains.aerial.FlightIntent	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.aerial.Input	held=false	unscopedMatch=false	resolve=ERROR: Unknown type 'io.circe.HCursor' in the supplied ontology and bindings	scoped=ERROR: Unknown type 'io.circe.HCursor' in the supplied ontology and bindings
domains.aerial.OriginateReport	held=true	unscopedMatch=false	resolve=OK	scoped=same=false,match=false
domains.aerial.Position	held=false	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
domains.aerial.PositionReport	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.ethereal.ConsumeReport	held=true	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
domains.ethereal.Consumer	held=false	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.ethereal.Creator	held=false	unscopedMatch=true	resolve=OK	scoped=same=false,match=false
domains.ethereal.EphemerisReport	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.ethereal.Ethereal	held=true	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
domains.ethereal.LaunchIntent	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.ethereal.OriginateReport	held=true	unscopedMatch=false	resolve=OK	scoped=same=false,match=false
domains.marine.ConsumeReport	held=true	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
domains.marine.Consumer	held=false	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.marine.Creator	held=false	unscopedMatch=true	resolve=OK	scoped=same=false,match=false
domains.marine.FixReport	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.marine.Marine	held=true	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
domains.marine.OriginateReport	held=true	unscopedMatch=false	resolve=OK	scoped=same=false,match=false
domains.marine.VoyageIntent	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Course	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Direction	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Distance	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Effect	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Ego	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Gaze	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Lean	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Path	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Percept	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Sentient	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.sentient.Waypoint	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.terrestrial.ConsumeReport	held=true	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
domains.terrestrial.Consumer	held=false	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.terrestrial.Creator	held=false	unscopedMatch=true	resolve=OK	scoped=same=false,match=false
domains.terrestrial.Location	held=false	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
domains.terrestrial.LocationReport	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.terrestrial.MarchIntent	held=true	unscopedMatch=true	resolve=OK	scoped=same=true,match=true
domains.terrestrial.OriginateReport	held=true	unscopedMatch=false	resolve=OK	scoped=same=false,match=false
domains.terrestrial.Output	held=false	unscopedMatch=false	resolve=OK	scoped=same=false,match=false
domains.terrestrial.Terrestrial	held=true	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
domains.world.Cartesian	held=false	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
domains.world.Consumer	held=false	unscopedMatch=false	resolve=OK	scoped=same=false,match=false
domains.world.Observable	held=false	unscopedMatch=false	resolve=ERROR: Unknown type 'Cartesian' in the supplied ontology and bindings	scoped=ERROR: Unknown type 'Cartesian' in the supplied ontology and bindings
domains.world.Provider	held=false	unscopedMatch=false	resolve=OK	scoped=same=false,match=false
domains.world.World	held=true	unscopedMatch=false	resolve=OK	scoped=same=true,match=false
```

The measurements above remain the historical audit of 375c513. The subsequent membership
increment is recorded in DEVELOPMENT_HANDOFF.md and git-record-2026-10-23-1048.
