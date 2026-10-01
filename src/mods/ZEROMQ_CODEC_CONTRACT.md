# ZeroMQ and Domain Codec Contract

Status: design checkpoint, September 29, 2026. The accepted flow below comes from
Dev; the concrete envelope and staging choices are proposals for the first slice.
No ZeroMQ dependency, transport adapter or new codec syntax is implemented yet.
The prior Draco-aspect increment is committed as fc2537f and full-suite verified
at 669 tests / 47 suites.

September 30 sequencing correction from Dev: establish draco.service.Service before
adding the dependency. Its initial domain trio now exists under mods, with no members
yet. Neutral service capability/configuration definitions belong there; target-language
wrappers implement the dependency-facing behavior. The reference service is intended
to be able to grow into a domain rules editor. This precedes the socket/envelope choices
below, which remain proposals rather than accepted configuration semantics.

## Accepted Responsibilities

1. ZeroMQ transports text containing JSON. Received text enters the ZeroMQ actor's
   working memory. Its rules parse that text into the target language's JSON value.
2. Rules in that actor extract destination-domain information from the JSON, resolve
   it through the active domain dictionary and dispatch JSON to the domain input actor.
3. The input actor's message type is JSON (Circe Json in the Scala target). Rules in
   that actor convert the JSON into typed domain data. The transport does not choose
   the domain payload type or its decoder.
4. The domain output actor converts typed results to JSON. Rules in the ZeroMQ actor
   wrap that JSON with source-domain identification and send the outgoing message.

Socket receipt/transmission belongs to a host adapter. JSON parsing, dictionary
resolution, actor selection and envelope creation remain rule actions, not a hidden
dispatcher inside that adapter. Domain input/output rules own the codec calls.

## Existing Support and Gaps

- DomainDictionary maps DomainType to TypeDictionary. It does not store live actors.
  Resolve a wire domain identity against the active dictionary first, then find the
  input endpoint associated with that resolved domain. A separate live endpoint table
  cannot authorize a domain absent from the active dictionary.
- Match complete TypeName identity, including package and type parameters. namePath
  alone omits parameters. Reject missing or ambiguous matches; do not load arbitrary
  classes or resources named by untrusted incoming text.
- Assembly describes members and bindings; AssemblySpawner bridges them to live
  Pekko actors. It does not currently define input/output roles per domain. Prototype
  those roles as explicit wiring, not inferred class names or new transport fields
  on every ActorAspect. The existing spawner also keys by namePath: parameterized
  identities must not silently collide in any new endpoint association.
- CodecAspect currently contains only discriminator. Encoder/decoder selectors in
  drake.dlt are a deferred sketch, not implemented declarations.
- Host Codec[T] requires both encoder and decoder. One-way codec declarations cannot
  simply be implemented by constructing a Codec with a null or dummy opposite side.
- The current Scala generator infers codecs for eligible definitions. Moving to
  explicitly declared codecs is a separate corpus migration, not a silent side effect
  of adding transport support.

## Proposed Envelope

Use a transport-independent JSON object, serialized as UTF-8 text. Field names below
are illustrative proposals, not reserved Drake words or accepted wire compatibility.

```json
{
  "destinationDomain": {
    "name": "Example",
    "namePackage": ["example"],
    "typeParameters": []
  },
  "payload": {"value": "hello"}
}
```

Outbound envelopes carry sourceDomain using the same identity form, plus payload.
Source identity comes from the configured domain output association, not a caller's
unverified claim inside the payload. An identity claim is not authentication.

For the first slice, propose forwarding the entire parsed JSON object to the input
actor, unchanged, so its rules can use payload and any envelope metadata. This does
not change its JSON message type. Domain conversion operates on the payload selected
by those rules. Whether full envelope or payload alone is the reusable convention
must be settled explicitly, not buried in adapter code.

Destination-domain identity and ZeroMQ destination are different concepts. The first
selects a local dictionary domain; the second selects a transport endpoint/peer.
The remaining scenario choice is whether output goes to a configured destination or
back to the originating peer. Do not assume request/reply merely because an output
is called a result. Returning to a peer additionally requires transport identity and
correlation to survive asynchronous domain processing without relying on response order.

## Codec Semantics to Exercise

Default and custom are ways to supply each conversion direction, not properties of
the ZeroMQ socket. The domain's rules choose and invoke a conversion explicitly.

- Default: use the existing derived codec for an eligible fixture type first, with
  its present missing/null/default behavior documented and tested. Do not silently
  tighten or weaken existing decoder behavior during the transport experiment.
- Custom: a definition of a conversion, not merely field renaming. The first fixture
  should demonstrate a value transformation in both directions and a decoding failure.
  Do not evaluate arbitrary host code received over the wire.
- Independently declared directions: absent suppresses that direction; present with
  no implementation requests derivation; present with an implementation uses it.
  This follows the earlier sketch as a proposal for review, not implemented semantics.
- Missing required data, invalid types and malformed input need explicit failures.
  A default value must not accidentally hide arbitrary decoder errors. Whether JSON
  null is equivalent to missing must be stated per codec policy.
- A custom decoder returns typed data or a structured failure, including the failing
  path where possible. A failing conversion must not dispatch partial domain data.

First prove default and custom conversion at the rules/host boundary in src/mods.
Then settle the declaration representation and add parser, emitter, JSON and Scala
projection support together. Never introduce a surface declaration that silently
falls back to inferred codecs. The existing field-level codec sketch does not settle
the representation of a complete custom conversion body.

## First Executable Slice

Use one external sender, one active dictionary domain with distinct JSON-input and
typed-output actors, and one external receiver. Exercise a small typed value with
both default and custom JSON representations. Keep routing configuration explicit.
Additional peers and delivery policies follow after the first path is proven.

Proposed JVM realization: JeroMQ, added to root build dependencies so root-compiled
mods code ships with the library. The mods script subproject gains no private dependency.
Implementation and definitions belong under src/mods/{scala,resources}/draco, tests
under src/test. Choose and pin a published version after confirming required transport
features. The project's [JeroMQ documentation](https://github.com/zeromq/jeromq)
describes a pure-Java implementation and transport limitations; TCP is the initial
interoperability candidate, not an assumption of native IPC compatibility.

Use explicit socket ownership and a bounded handoff between I/O and actor execution;
do not block the ordinary actor dispatcher in recv. Actor serialization alone does
not establish socket thread ownership. Shut down through the owning I/O loop with
bounded waits, rather than abandoning sockets. Socket patterns must match the selected
scenario; see the [ZeroMQ guide](https://zguide.zeromq.org/docs/chapter3/).

## Acceptance Checks

- A real local ZeroMQ exchange enters working memory as text; a fired rule parses it.
- Routing uses the active dictionary and exact identity; unknown/ambiguous domains and
  missing live endpoints produce observable failures with no domain dispatch.
- The input actor receives JSON; its rules, not the socket adapter, decode typed data.
- Default/custom codecs both work; invalid payloads never create typed domain facts.
- Output actor rules encode JSON; ZeroMQ actor rules add the correct source identity.
- Malformed text, non-object envelopes, absent routing fields and unsupported frames
  fail explicitly. Validate before dispatch and constrain message size and queue growth.
- Multiple messages do not reuse stale routing facts or produce duplicate sends when
  sessions fire again. Avoid the known subtype co-declaration ambiguity in working memory.
- Bounded queues, unavailable peers, shutdown and restart do not hang tests or leak sockets.
  Do not claim durable delivery, automatic retries or exactly-once processing.
- First bind only to loopback. Network exposure requires a separate authentication,
  authorization and resource-limit decision; dictionary membership is not access control.
- Run existing corpus gates to prove the prototype did not silently change codec
  generation or the definition model. Dev runs sbt and commit/push commands.
