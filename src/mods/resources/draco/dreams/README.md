# Dreams Name Skeleton

Established October 7, 2026, before the next authoring increment. Three empty,
definition-backed domains name the layer surrounding the Draco core:

| Domain | Intended direction; no members implemented yet |
|---|---|
| draco.dreams.Dreams | Construction and evolution of Draco-defined systems. |
| draco.dreams.editor.Editor | Definition-authoring operations and editing workflows. |
| draco.dreams.user.User | User-facing participation and interaction; specific concepts remain open. |

Each domain has a canonical Drake/JSON/generated Scala trio under
src/mods/{resources,scala}/draco/dreams. The build's existing staging paths already
compile and load them; no new dependency or build project is needed.

The domains are dictionary peers. Package nesting adds neither a super-domain edge
nor membership in Dreams or Draco. Compose them explicitly with Draco and Service
when building a dictionary. All three have empty membership and no rule, actor or
codec aspects. This increment adds no editing behavior, user model, authentication,
persistence, activation or changes to the ZeroMQ bridge.

The former handwritten src/main/scala/draco/dreams/Dreams.scala scaffold is replaced
by the generated Dreams in src/mods, preserving its fully qualified name without a
duplicate declaration. The old draco.dreams.Service trait and draco.dreams.orion
scaffolds remain in src/main. They have no corresponding domain trios or registration;
draco.dreams.Service is not an alias, parent or replacement for draco.service.Service.
No behavior or dependency on those legacy scaffolds is introduced here.

The next authoring design can use Editor for application operations while retaining
reusable capabilities in draco.service.Service and validation/dictionary primitives
in core. Detailed message, actor and configuration shapes still need implementation;
the [agreed Service plan](../service/README.md) remains the planning reference.

DreamsTest explicitly covers the three trios because the main-corpus gates do not
scan staged draco definitions: five tests per domain plus one combined composition
test with Draco and Service. The direct compiled-class probe passed 555 tests / 7
suites with unchanged parse scope, surface loss and GenDrake counts. Dev's October 7
full sbt run passed 701 tests / 50 suites with zero failures/aborts, unchanged
report-only baselines and zero pending mods actors in the file-only Drake report.
