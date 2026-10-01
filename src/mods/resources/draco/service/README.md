# Service Domain

`draco.service.Service` is the early-access, target-language-neutral home for service
capability and configuration definitions. Its generated companion lives in
`src/mods/scala/draco/service/Service.scala`; both trees compile/load through root's
existing mods wiring, without adding a new build project or dependency.

The first increment establishes only the self-declaring domain. It intentionally
has no members, actor aspect, codec aspect or service instance factory. It is not a
running server or a transport adapter. A concrete Domain[Service] and its empty
TypeDictionary can already participate in a DomainDictionary; DomainBuilder can
load, validate and generate it.

Next, define the minimum neutral capabilities and configuration. Target-language
implementations will wrap dependency APIs such as ZeroMQ; the neutral definitions
must not expose library socket classes. Configuration should describe a service
supported by an active domain dictionary, without confusing domain identity with
live actor endpoints. No endpoint schema, delivery pattern or custom codec syntax
is selected by this initial domain declaration.

The intended reference service may grow into the domain rules editor. That is a
direction, not an editor implementation or an editor-specific requirement on all
services. Rule-owned JSON parsing, routing and conversion remain as described in
the [transport/codec contract](../../../ZEROMQ_CODEC_CONTRACT.md).

`draco.service.ServiceTest` explicitly checks this staged trio and dictionary
integration: the main-corpus gates do not discover draco definitions in src/mods.
Add corresponding coverage when members are introduced. Do not register Service
as a member of draco.Draco just to make it discoverable: domains are dictionary peers.
