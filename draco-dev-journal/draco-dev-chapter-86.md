# Chapter 86 - Rule-Owned Messaging and the Service Domain

**Session date:** 2026-09-28 through 2026-09-30 (UTC source dates)
**Topic:** Rule-Owned Messaging and the Service Domain.

**Provenance:** Original written messages recovered from this Codex task's local JSONL log.
Messages are in source order, with UTC timestamps and physical source-line references.
Codex final responses are reproduced verbatim; progress updates, tools and reasoning are
omitted. Responsive command output is folded into delegated-action notes; mixed prompts
retain their assertive opening verbatim. The complete selected message bodies, including
command output, are preserved in [the source extract](sources/codex-2026-09-23-to-30.jsonl).
See [the retrieval audit](sources/codex-2026-09-23-to-30-audit.md) for counts and limits.
Historical commands below are transcript evidence, not instructions to run again.

**[Dev]** *2026-09-28T21:29:52.012Z; source line 2020*

Before we start rule aspect, I want to discuss additions to actor aspect and code aspect, to see if they should be done ahead of the definition-backed aspect changes:

- I want to add a Zero MQ dependency to draco so I can create actors that can communicate externally with it and dispatch to internal domain and type actors
- I want to add to drake syntax/semantics to accommodate default and custom codec implementation definitions

---

**[Codex]** *2026-09-28T21:30:27.612Z; source line 2038*

Yes. These additions could change what the actor and codec aspects must express, so I would discuss their contracts **before further aspect-generation migrations**. I’m reading “code aspect” as “codec aspect.”

**ZeroMQ and Actors**
I would initially treat ZeroMQ as an external communication capability, not automatically as new fields in `ActorAspect`.

The proposed path is:

```text
ZeroMQ message
  → decode and validate
  → resolve destination
  → dispatch to an internal actor
  → encode any response
  → ZeroMQ
```

That separates three concerns: transport, representation and actor behavior. It also lets internal actors retain their existing message-oriented interface.

For the current JVM realization, **JeroMQ** is a candidate because it implements ZeroMQ in Java without requiring native `libzmq` installation. We should check required protocol and security features before selecting it over native bindings. [Official Java options](https://zeromq.org/languages/java/)

The first implementation must explicitly address socket ownership, bounded queues, shutdown and delivery expectations. An actor processing messages sequentially does not by itself establish the thread ownership needed by a ZeroMQ socket. [ZeroMQ threading guidance](https://zguide.zeromq.org/docs/chapter2/)

I’d prototype the adapter in `src/mods`, then use that experience to determine whether actor-aspect additions are actually necessary.

**Default and Custom Codecs**
This is a genuine syntax/metamodel addition, not just moving existing rendering into definitions.

The existing [codec sketch](/Users/edwardburke/IdeaProjects/nexonix/src/main/resources/draco/drake.dlt:578) already distinguishes:
- An absent encoder or decoder: suppress that direction.
- A present, empty direction: derive its default implementation.
- A populated direction: customize its implementation.

But that sketch is explicitly deferred. The implemented `CodecAspect` currently contains only `discriminator`; the sketch is not working functionality or a settled specification.

The important question is what **custom implementation** means. Field renaming, omission and defaults are narrower than an arbitrary transformation between a Draco value and an external representation. We should not implement the narrower sketch and accidentally rule out the broader capability.

We also need to distinguish a malformed input from a missing field with a default, and determine how a codec identifies its representation without assuming everything is JSON.

**Suggested Order**
1. Record and commit the verified Draco-aspect increment independently.
2. Agree on one external messaging scenario, including addressing and its wire representation.
3. Define the minimum codec semantics needed for that scenario.
4. Build an end-to-end ZeroMQ adapter and codec slice in `src/mods`.
5. Use those results to settle actor/codec additions, then resume definition-backed aspect generation.

The first distinction I’d settle with you: **should an external sender address a domain/type and let Draco select the actor, or address a particular running actor instance?** That determines what routing information must exist independently of the payload codec.

No changes made.

---

**[Dev]** *2026-09-29T14:00:46.065Z; source line 2047*

The messages will be transported using ZeroMQ and the message content will be text in JSON format. The domain for incoming messages will be mapped from a JSON value in the message into a domain in the active domain dictionary and through that, the input domain actor. An output domain actor will send the result JSON data to the ZeroMQ actor which will then wrap that domain JSON data to identify the source domain for the ZeroMQ destination. The input domain actor converts JSON data into draco typed data of the domain and the output domain actor converts domain typed data into JSON data for the outgoing paylod of a ZeroMQ message.

---

**[Codex]** *2026-09-29T14:01:03.943Z; source line 2050*

That establishes the boundary clearly: **ZeroMQ carries JSON text; domain actors own conversion between JSON and domain-typed data.**

The flow is:

```text
Incoming:
ZeroMQ actor
  → read destination-domain value from JSON
  → resolve domain through the active domain dictionary
  → dispatch JSON payload to that domain’s input actor
  → decode into domain-typed data

Outgoing:
Domain-typed result
  → domain’s output actor
  → encode as JSON payload
  → ZeroMQ actor adds source-domain identification
  → send to the ZeroMQ destination
```

The ZeroMQ actor therefore handles the **message envelope and transport**, not the domain payload’s interpretation. The domain input/output actors apply the relevant codecs.

This suggests two distinct contracts:

- **Domain routing:** how a dictionary-resolved domain leads to its input actor, and how an output actor identifies its source domain.
- **Domain codecs:** how incoming JSON becomes typed domain data and how outgoing typed data becomes JSON, using default or custom definitions.

I would settle those contracts before migrating actor/codec generation. We should first check whether existing actor and assembly definitions can express them; ZeroMQ alone does not require adding transport-specific fields to `ActorAspect`.

One distinction remains important: **does each domain have a single declared input message type, potentially a family of variants, or must the incoming payload identify which type within that domain to decode?** That determines how the input actor selects its decoder, separately from selecting the domain.

---

**[Dev]** *2026-09-29T15:39:39.861Z; source line 2057*

The input message type is just JSON as understood by the target language. That JSON type is created and sent by the rules executing in the ZeroMQ actor. The input domain is determined by executing the rules in the ZeroMQ actor that extract the domain information from the Json object created from the text payload of the message entered into the ZeroMQ actor working memory.

---

**[Codex]** *2026-09-29T15:39:55.565Z; source line 2060*

Understood. I conflated the input actor’s message type with the domain-typed data produced after decoding.

The input domain actor receives **the target language’s JSON value**, not a preselected Draco message type. The incoming flow is:

1. The ZeroMQ message’s text payload enters the ZeroMQ actor’s working memory.
2. Rules executing there parse the text into a JSON object.
3. Those rules extract the destination-domain information, resolve it through the active domain dictionary, and send the JSON value to the domain’s input actor.
4. Rules in the input domain actor convert that JSON into domain-typed data.

The outgoing flow reverses the representation boundary: the output domain actor converts typed data to JSON; rules in the ZeroMQ actor wrap that payload with source-domain identification and send it.

Thus, **parsing, routing and conversion are rule-driven behaviors**, not a separate dispatcher imposed outside the actor’s working memory. The transport adapter supplies receipt and transmission; it does not own domain selection or payload decoding.

This also removes the need to choose a domain-specific input message type at the transport boundary. Any selection needed within the domain belongs to its input actor’s rules.

---

**[Dev]** *2026-09-29T17:21:09.010Z; source line 2067*

If you have enough information please proceed with your earlier Suggested Order of activity.

---

**[Codex]** *2026-09-29T17:23:00.680Z; source line 2098*

The **669-test / 47-suite success** is now recorded, along with your rule-driven ZeroMQ flow. No ZeroMQ or codec implementation changes have been mixed into this increment.

To complete the first step, run:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix &&
paths=(
  DRACO.md README.md CHANGELOG.md DEVELOPMENT_HANDOFF.md
  src/main/resources/draco/draketarget/DrakeTarget.drake
  src/main/resources/draco/draketarget/DrakeTarget.json
  src/main/resources/draco/draketarget/DracoAspectText.drake
  src/main/resources/draco/draketarget/DracoAspectText.json
  src/main/resources/draco/gendrake/GenDrake.drake
  src/main/resources/draco/gendrake/GenDrake.json
  src/main/resources/draco/gendrake/DracoAspectOf.drake
  src/main/resources/draco/gendrake/DracoAspectOf.json
  src/main/scala/draco/draketarget/DrakeTarget.scala
  src/main/scala/draco/draketarget/DracoAspectText.scala
  src/main/scala/draco/gendrake/GenDrake.scala
  src/main/scala/draco/gendrake/DracoAspectOf.scala
  src/mods/scala/draco/Drake.scala
  src/test/scala/draco/draketarget/DracoAspectTest.scala
  src/test/resources/draco/draketarget/draco-aspect-baseline
  draco-git-record/git-record-2026-09-28-1634
) &&
sed '1,/^--- MESSAGE ---$/d' \
  draco-git-record/git-record-2026-09-28-1634 > /tmp/draco-commit-message &&
test -s /tmp/draco-commit-message &&
git diff --check &&
git add -A -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F /tmp/draco-commit-message -- "${paths[@]}" &&
git push &&
git status --short --branch
```

After that checkpoint, we’ll establish the concrete messaging and codec contracts, then build the first `src/mods` slice. The remaining choices concern envelope structure, endpoint association and failure handling—not who owns parsing, routing or domain conversion; you’ve settled those boundaries.

---

**[Dev]** *2026-09-29T18:36:23.920Z; source line 2105; assertive opening only*

Check command results and proceed, if ready:

*(Delegated action - Dev ran the supplied Draco-aspect commit/push command: fc2537f, 22 files changed, main pushed; status reported main...origin/main with no changes. Source line 2105, 2026-09-29T18:36:23.920Z.)*

---

**[Codex]** *2026-09-29T18:39:34.517Z; source line 2178*

Confirmed: `fc2537f` was pushed successfully; the tree was clean.

I’ve prepared the [ZeroMQ/codec contract draft](/Users/edwardburke/IdeaProjects/nexonix/src/mods/ZEROMQ_CODEC_CONTRACT.md), separating your accepted rule-driven flow from proposed implementation choices.

The key gap is connecting a dictionary-resolved domain to its live input actor. No runtime or dependency changes yet.

Before selecting socket wiring: **should results go to a configured output destination, or return to the peer that sent the input?**

---

**[Dev]** *2026-09-29T19:24:51.752Z; source line 2185*

Before we continue with adding ZeroMQ as a dependency there needs to be a home domain for the drake type definitions. I'm imagining draco.service.Service as a domain that is built to be the target language neutral domain that holds the types for the implementation wrappers that exist in the target language to wrap the direct references to the ZeroMQ dependency. Additionally, the Service domain would contain type definitions in drake that neutrally referenced configuration data needed to configure a service that could be supported by a domain dictionary. The Draco Domain Dictionary containing this Service domain could be used to build a reference implementation of a service that could grow into being the domain rules editor.

---

**[Codex]** *2026-09-29T19:25:04.081Z; source line 2188*

Yes. **`draco.service.Service` would give these definitions a framework-owned home**, rather than organizing them around ZeroMQ or JVM implementation names.

I understand two responsibilities for that domain:

- **Service capabilities:** target-neutral types whose target-language implementations wrap external facilities, initially ZeroMQ.
- **Service configuration:** definitions describing how a service uses its domain dictionary, domain input/output actors and communication endpoints.

The dependency-facing boundary would then be:

```text
Drake service definitions
  → target-language implementation wrappers
  → ZeroMQ dependency
```

ZeroMQ-specific APIs stay in those wrappers. Parsing, routing and conversion remain rule-driven as you specified; introducing Service does not move those responsibilities into the transport adapter.

One distinction matters: **the Service domain describes the capabilities and configuration; a running service realizes them using an active domain dictionary and live actors.** That avoids treating the dictionary itself as the running service.

For the first increment, I recommend:

1. Establish `draco.service.Service` and its initial definition trios in the early-access `src/mods` tier.
2. Define the minimum service configuration and capability contracts needed by the agreed JSON message flow.
3. Build a reference service using a dictionary containing Service and an application domain.
4. Add the target-language wrappers and ZeroMQ dependency against those contracts.

The reference service can then become the foundation for the domain rules editor without making editor-specific behavior part of every service.

I’ll defer the dependency addition. We should define Service’s initial members next, keeping **neutral service concepts** distinct from **ZeroMQ-specific realization details**.

---

**[Dev]** *2026-09-30T14:33:51.650Z; source line 2197*

Proceed with the first increment

---

**[Codex]** *2026-09-30T14:37:21.565Z; source line 2263*

Established [draco.service.Service](/Users/edwardburke/IdeaProjects/nexonix/src/mods/resources/draco/service/Service.drake) with its JSON and generated Scala in `src/mods`.

It loads, validates and participates in a domain dictionary. Membership is intentionally empty; configuration types, wrappers and ZeroMQ remain subsequent work.

**Verified:** 15 tests across 2 suites passed. Documentation updated; prior contract work preserved.

Please run the full suite, expected **674 tests / 48 suites**:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix &&
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
```

Nothing committed or pushed.

---

## Status

*Editorial closeout, not reconstructed dialogue:* Dev corrected the earlier inference about the input message type: rules in the ZeroMQ actor produce host JSON and route it through the active dictionary; domain actors own typed conversion. Envelope/socket and codec-selector details remain proposals. Service is only an empty domain anchor with a generated trio and five explicit tests under the early-access tier. No ZeroMQ dependency or codec syntax was added. The focused Service probe passed 15 tests / 2 suites; full verification follows in chapter 87.
