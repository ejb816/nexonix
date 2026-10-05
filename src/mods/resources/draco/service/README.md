# Service Domain

`draco.service.Service` is the early-access, target-language-neutral home for service
capability and configuration definitions. Its generated companion lives in
`src/mods/scala/draco/service/Service.scala`; both trees compile/load through root's
existing mods wiring, without adding a new build project or dependency.

The September 30 increment established the self-declaring domain at 604aff2.
The October 5 bridge increment adds its first member, TextOutput: a generated neutral
capability with send: Text -> Boolean. True means accepted into a bounded queue,
not peer receipt. The current Scala wrapper realizes Text as String.

The handwritten staging engine under src/mods/scala/draco/service/zeromq contains
PairTextTransport (socket ownership and text queues), BridgeRules (JSON and dictionary
routing rules) and ZeroMqBridge (the Draco actor membrane). This is an executable
transport fixture, not the primordial authoring service. ServiceConfiguration,
DomainInterface, authoring operations and the initialization client remain unimplemented.

## Agreed direction (recorded October 5, 2026)

This section summarizes Dev's decisions in the current Codex task after checkpoint
604aff2; it is a planning record, not reconstructed dialogue or implemented behavior.

The primordial service lets an external client create and modify domain/type
definitions and create and modify a DomainDictionary. The first authoring increment
covers authoring, validation and dictionary composition. Generation and activation
follow as separate, explicit operations; accepting a definition does not execute it.

ServiceConfiguration contains Assembly. The remaining configuration fields, capability
names and operation/message definitions are still to be designed. The earlier
TextTransport and DomainInterface names were proposals, not accepted definitions.

A Draco-endogenous initialization client builds the initial Draco domain dictionary
from a supplied set of canonical JSON type definitions. It uses existing Draco actors
and actor messages, needs no external communication, and can be developed in parallel
with ZeroMQ integration. Both clients use the shared authoring, validation and
composition capabilities. A prebuilt dictionary is therefore not the only intended
initialization path. The bootstrap ordering and minimal initial actor/domain set
still need design; do not bypass dictionary-based dispatch to conceal that dependency.

Persistence belongs to the consuming project that uses Draco as its primary binary
executable dependency. The framework does not choose storage paths, a database or
an automatic save policy. Supplying canonical JSON to the local client does not
imply a filesystem-specific initialization API.

Wire format and transport details belong to ZeroMQ integration into the core framework,
with neutral service definitions and target-language dependency wrappers kept distinct.
Rule-owned JSON parsing, dictionary-based routing and domain-owned conversion remain
as described in the [transport/codec contract](../../../ZEROMQ_CODEC_CONTRACT.md).
The reference service can grow into the domain rules editor; that remains a later goal.

## Development tracks

1. Shared service authoring operations and the endogenous initialization client.
   Verify definition creation/modification, validation and dictionary composition
   through local actor messages, without ZeroMQ or persistence requirements.
2. ZeroMQ integration for the non-Draco client. The bounded actor/transport bridge is now staged
   as described in the contract. It is tested
   with an existing fixture domain while authoring operations are being developed.
3. Join the tracks for external definition authoring. Add generation and activation
   only as subsequent explicit operations, retaining project-owned persistence.

Only the initial bridge is implemented; no parallel agent work was launched.

`draco.service.ServiceTest` explicitly checks this staged trio and dictionary
integration: the main-corpus gates do not discover draco definitions in src/mods.
Seven tests now cover Service membership plus TextOutput projection and send behavior. Do not register Service
as a member of draco.Draco just to make it discoverable: domains are dictionary peers.
