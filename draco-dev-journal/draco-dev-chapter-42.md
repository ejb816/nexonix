# Draco Dev Journal — Chapter 42

**Session date:** June 14, 2026
**Topic:** Closing the 6c blocker — a rule's RHS reaching a Pekko `ActorRef`. The session resolved the one framework gap that stood between the green single-actor Aerial Consumer (chapter 41) and a real Creation-phase pipeline: how a *rule action* sends a message to the next actor. The answer is the Evrete **Environment** as the rule↔ref seam. Landed and green: a `Provider` relay, then the full `Creator → Provider → Consumer` chain driven by a single `FlightIntent`, with algorithmic origination.

---

## Opening — continue the example

> **Dev:** Continue with the example domains that will support TransformBuilder

Chapter 41 left a clean marker: the Aerial slice was green (domain + `PositionReport` shell + a Generator-produced `Consumer` actor + `ConsumeReport` rule), but the next sub-step — 6c, the Creator→Provider cross-actor chain — was flagged **blocked on framework design**:

> *Creator→Provider cross-actor chain needs a rule-RHS-reaches-ActorRef-via-Evrete-Environment pattern that **does not exist** in the codebase (grep found only `actorSystem ! msg` in test code, never from a rule action). Design that first.*

So the work began as a design question, not a typing exercise: a `Consumer[RhsContext]` rule action runs *inside* an Evrete firing. It has `ctx` (the `RhsContext`), the matched facts, and nothing else. How does it get hold of the downstream actor's `ActorRef` to forward a message?

## Confirming the seam against the Evrete sources

Rather than guess, I read the Evrete 4.0.3 API sources out of the Coursier cache. Two facts decided the design:

- `RhsContext.getRuntime()` returns the `RuleSession` driving the firing — *"the runtime context (session)."*
- The session **is** an `Environment`: `StatefulSession → RuleSession → RuleSetContext → RuntimeContext → FluentEnvironment → Environment`, which gives `Object set(String, Object)` and `<T> T get(String)`. `Knowledge` reaches `Environment` the same way.

That closes the half that was genuinely uncertain: a rule RHS *can* read an arbitrary property by name via `ctx.getRuntime().get[...](...)`. The Environment is the channel that lets a **generated rule stay completely ignorant of Pekko** while still emitting a send — the actor injects the ref by role-name, the rule consumes it by the same name.

## The one fork that was the Dev's to make

The *other* half — how the actor obtains the downstream ref to put into the Environment — permanently shapes the Generator's actor emission, and chapter 41 had explicitly deferred it. So I put the fork to the Dev rather than committing the framework unilaterally. Three shapes:

1. **Env seam + Pekko ctor refs** — generated actor holds downstream refs as setup state, seeds them into the session Environment by role before `fire()`; rule RHS reads them back. Rule stays Pekko-agnostic, actor↔actor wiring stays explicit Pekko.
2. **Static ActorRegistry** — a global role→ref map (like `AerialSink`); simplest, but a global mutable and it ignores the Environment.
3. **Message-envelope continuation** — carry the onward ref in the message; rejected because messages must stay pure `io.circe.Json` in this phase.

> **Dev:** [selected] **Env seam + Pekko ctor refs**

The chosen preview, verbatim, became the implementation contract:

```text
// generated actor receive()
val s = knowledge.newStatefulSession()
s.set("provider", provider)   // downstream ref → Environment
s.insert(Seq(msg): _*); s.fire(); s.close()

// generated rule RHS
val out = ctx.getRuntime().get[ActorRef[Json]]("provider")
out ! transform(report)
```

## Provider relay first — prove the seam, then build on it

Following the project's discovery rhythm (6a hand-templated, 6b generated), the new shapes were hand-written first to nail them before teaching the Generator. The minimal proof is a *relay*: a `Provider` actor whose rule forwards an existing `PositionReport` to the terminal `Consumer`.

The shape that distinguishes a forwarding actor from a terminal one: its `actorType` is no longer a no-arg `lazy val` but a **factory** keyed on the downstream ref —

```text
def actorType(consumer: ActorRef[draco.format.json.Json]): ActorType = new Actor[...] {
  override def receive(ctx, msg) = {
    val session = knowledge.newStatefulSession()
    session.set("consumer", consumer)   // seed the seam
    session.insert(Seq(msg): _*); session.fire(); session.close()
    Behaviors.same
  }
}
```

— and `RelayReport`'s RHS reads it back:

```text
val report   = ctx.get[PositionReport]("$report")
val consumer = ctx.getRuntime().get[ActorRef[draco.format.json.Json]]("consumer")
consumer ! report
```

A guardian-based test wires the chain *downstream-first* (spawn Consumer, then Provider wired to it), feeds a `PositionReport` to the Provider, and asserts it lands in `AerialSink`. The one thing I couldn't verify without running: that a property `set` on the session *before* `fire()` is visible via `getRuntime().get` *during* the RHS. I flagged it as the runtime risk and handed off.

It passed on the first run:

> **Dev:** [`testOnly domains.aerial.AerialChainTest`] *Provider relays a PositionReport to the Consumer via a rule-RHS send over the Environment seam* — **1 succeeded**

Risk resolved: the Environment seam works exactly as the interface contract promised.

## Completing the pipeline — Creator, FlightIntent, algorithmic origination

With the seam proven, the full Creation-phase chain followed in the same shape:

- **`FlightIntent`** — a new seed shell (`Aerial with Json`), added to `Aerial.elementTypeNames`. The message that enters at the head of the pipeline.
- **`Creator`** — head actor, factory `actorType(provider)`, seeds `"provider"` — byte-for-byte the same membrane as `Provider`, only the role name differs.
- **`OriginateReport`** — the rule that makes the Creation phase *real*. Its RHS does no fixture lookup; it **originates** algorithmically:

```text
private def originate(intent: FlightIntent): PositionReport = {
  val callsign = intent.value.hcursor.get[String]("callsign").getOrElse("UNKNOWN")
  val fl       = intent.value.hcursor.get[Int]("flightLevel").getOrElse(0)
  // unit transform: flight level (hundreds of feet) → altitude in feet
  val payload  = Json.obj("msg" -> ..., "callsign" -> callsign, "altFt" -> fl * 100)
  new PositionReport { override val value = payload; override lazy val typeDefinition = PositionReport.typeDefinition }
}
```

Tiny, but honest: a flight level FL390 becomes 39000 ft, callsign carried through, the synthesized report forwarded to `"provider"`. This is the seam where a real medium would synthesize its native representation.

The chain test gained a second case: a guardian wires `Consumer ← Provider ← Creator` downstream-first, drops **one** `FlightIntent` at the Creator, and asserts the originated report reaches the sink — having crossed **two** rule-RHS sends.

> **Dev:** [`testOnly domains.aerial.AerialChainTest`]
> *Provider relays a PositionReport to the Consumer via a rule-RHS send over the Environment seam*
> *Creator originates a report from a FlightIntent and it flows through Provider to Consumer*
> **Tests: succeeded 2, failed 0**

## Where this leaves things

The blocker that gated 6c is gone, and with it the last "does the framework even allow this?" question on the message-domain example. The Creation-phase pipeline runs end to end on a generated-actor-shaped membrane, with the rule layer fully decoupled from Pekko wiring by the Environment seam.

What is **hand-written** and still owes a Generator pass: `Provider`/`Creator` (the factory `actorType(ref)` shape), the `session.set("<role>", ref)` seed line, and the rule RHS `getRuntime().get[ActorRef[...]]("<role>") ! ...`. Two open schema questions block the fold and are the Dev's to weigh in on next: (a) how an actor declares its downstream role→type in JSON, and (b) how a rule expresses an origination helper body. Beyond that, Phase 2 remains the eventual home of TransformBuilder: the World super-domain, strong cross-domain element types, and the transform *rules* that let a message cross from one medium to another — at which point the Streaming phase (loop-back through transforms) becomes expressible.
