# Draco Dev Journal — Chapter 42

**Session date:** June 14, 2026 (evening; continues ch.41)
**Topic:** Closing the 6c blocker — a rule's RHS reaching a Pekko `ActorRef`. The Evrete **Environment** confirmed as the rule↔ref seam against the 4.0.3 sources, proven by a hand-written `Provider` relay, then the full `Creator → Provider → Consumer` Creation-phase chain driven by a single `FlightIntent` with algorithmic origination (VERIFIED 2/2). Transcript.

---

**[Dev]** Continue with the example domains that will support TransformBuilder

**[Claude]** I'll get oriented on the current state of the media-domains example work before continuing. *(reads memory, the generated Aerial Scala, the tests, and the framework actor/Knowledge plumbing)* The memory says 6c is blocked on a framework-design question: how a rule's RHS reaches an `ActorRef` via Evrete's Environment, a pattern that doesn't yet exist. The Aerial slice (6a/6b) is green — domain + shell + Consumer actor + ConsumeReport rule, all generated. Let me verify what Evrete's API actually offers here before recommending a design.

*(unzipped the Evrete 4.0.3 sources from the Coursier cache and read `RhsContext`/`Environment`/`RuleSession`)* I've now confirmed the framework facts that unblock 6c:

- `Knowledge` and `StatefulSession` both extend `Environment` (via `RuntimeContext`/`FluentEnvironment`) — so they have `set(property, value)` / `get(property)`.
- In a rule RHS, `RhsContext.getRuntime()` returns the `RuleSession`, which **is** an `Environment`. So `ctx.getRuntime().get[ActorRef[Json]]("provider")` works.

That confirms the **rule-RHS → ActorRef** half is clean and natural: the Evrete Environment is the seam that lets a *generated rule* emit a send without knowing anything about Pekko wiring — the actor injects the ref by role-name, the rule reads it back.

The remaining genuinely-open fork is the *other* half: **how the generated actor obtains the downstream `ActorRef`** to seed into that Environment. This permanently shapes the Generator's actor emission, and your memory shows you deliberately deferred it ("design that first"), so I want your call on it before I commit the framework. *(posed three shapes: (1) **Env seam + Pekko ctor refs** — the generated actor holds downstream refs as setup state, seeds them into the session Environment by role before `fire()`, the rule RHS reads them back — rule stays Pekko-agnostic, actor↔actor wiring stays explicit Pekko; (2) **static ActorRegistry** — a global role→ref map, simplest but a global mutable that ignores the Environment; (3) **message-envelope continuation** — carry the onward ref in the message, rejected because messages must stay pure `io.circe.Json` in this phase)*

---

**[Dev]** *(selected from the offered fork)* **Env seam + Pekko ctor refs**

**[Claude]** Approach locked: Evrete Environment as the rule↔ref seam, with downstream `ActorRef`s as explicit Pekko setup state. The chosen preview becomes the implementation contract:

```text
// generated actor receive()
val s = knowledge.newStatefulSession()
s.set("provider", provider)   // downstream ref → Environment
s.insert(Seq(msg): _*); s.fire(); s.close()

// generated rule RHS
val out = ctx.getRuntime().get[ActorRef[Json]]("provider")
out ! transform(report)
```

Following this project's established rhythm (hand-template the shape, get it green, *then* teach the Generator to emit it — exactly how 6a→6b went), I'll first prove the runtime pattern end-to-end with a hand-written **Provider** relay, then a **Creator**, then a wiring guardian test.

What landed: [Provider.scala] — forwarding actor; `actorType` is now a **factory** `def actorType(consumer: ActorRef[Json])` (vs. Consumer's no-arg val), and `receive` does `session.set("consumer", consumer)` before `fire()`. [RelayReportRule.scala] — RHS reads `ctx.getRuntime().get[ActorRef[Json]]("consumer") ! report`, the send-from-a-rule pattern that was the blocker. [Provider.json / RelayReport.rule.json] — twins so `loadType`/`loadRuleType` resolve. [AerialChainTest.scala] — a guardian spawns Consumer, then Provider wired to it, feeds a `PositionReport`, asserts it reaches `AerialSink` having crossed the seam.

`sbt "testOnly domains.aerial.AerialChainTest"` is the checkpoint. **One runtime risk I couldn't compile-verify:** that a property `set` on the session *before* `fire()` is visible via `ctx.getRuntime().get` *during* the RHS. The interface contract says yes, but if it comes back null the fallback is to set on the `knowledge` instead, or a static role→ref registry. Everything else mirrors the green 6b shapes. Deliberately not done yet, so you get a checkpoint first: Creator + FlightIntent + OriginateReport, then folding the new shapes into the Generator. I held the journal chapter until the test is verified green.

*(Delegated action — Dev ran `AerialChainTest`: "Provider relays a PositionReport to the Consumer via a rule-RHS send over the Environment seam" — **1 succeeded**, first run.)*

The Environment seam works — test green on the first run, so the runtime risk I flagged is resolved (a property `set` before `fire()` *is* visible via `ctx.getRuntime().get` in the RHS). I went ahead and completed the full **Creator→Provider→Consumer** chain while the pattern was hot:

| File | Role |
|------|------|
| Creator.scala | Head of the pipeline. Factory `actorType(provider)`, seeds `"provider"` into the session Environment — byte-for-byte the same membrane as Provider, only the role name differs |
| OriginateReportRule.scala | **Algorithmic origination** — `originate(intent)` synthesizes a `PositionReport` from a `FlightIntent` (FL390 → 39000 ft, callsign carried through, no JSON fixture), then the RHS sends to `"provider"` |
| FlightIntent.scala / FlightIntent.json | Seed message shell (`Aerial with Json`), now an Aerial member |
| Creator.json / OriginateReport.rule.json + Aerial.json updated | JSON twins |
| AerialChainTest.scala | 2nd test: one `FlightIntent` at the Creator → the originated report reaches the sink across **two** rule-RHS sends |

The whole chain rests on one new framework capability you approved: a rule RHS sends to a downstream `ActorRef` it reads from the Evrete Environment by role-name, with the actor (a factory keyed on its downstream ref) seeding it before `fire()`. Pure Pekko wiring stays explicit; the generated rule stays Pekko-agnostic. Run both tests — the relay is already verified; this confirms the Creator/origination half.

*(Delegated action — Dev ran `AerialChainTest`: relay + "Creator originates a report from a FlightIntent and it flows through Provider to Consumer" — **2 succeeded, 0 failed**.)*

Both tests green — the full Creation-phase pipeline works end to end. Memory marked verified; this chapter written (with one fidelity fix — the journal snippet implied a `PositionReport(...)` factory that doesn't exist; the real code uses an anonymous subclass).

**What's next (your call before I commit the Generator):** `Provider`/`Creator`/`RelayReport`/`OriginateReport` are hand-written and owe a Generator pass to stop being so. Folding them in needs two **schema decisions** that are genuinely yours: **(1)** how an actor declares its downstream role→type in JSON (so the Generator can emit `def actorType(provider: ActorRef[Json])` + the `session.set("provider", provider)` seed line); **(2)** how a rule expresses an origination helper body like `originate(intent)` in JSON (a `Dynamic` method element? a `Monadic` block?). I'd recommend tackling (1) first — smaller, more mechanical, and it unblocks regenerating the two relay actors immediately. Draft a concrete schema proposal for (1), or pause here?

## Status
The 6c blocker is closed: `AerialChainTest` **2/2** — Provider relay, then Creator origination across two rule-RHS sends, with the Evrete Environment as the rule↔ref seam and the rule layer fully decoupled from Pekko wiring. Hand-written and owing a Generator pass: `Provider`/`Creator` (factory `actorType(ref)` shape), the `session.set("<role>", ref)` seed line, and the RHS `getRuntime().get[ActorRef[...]]("<role>") ! ...`. Two open schema questions block the fold: (a) declaring an actor's downstream role→type in JSON, (b) expressing an origination helper body in rule JSON. Phase 2 remains the eventual home of TransformBuilder: the World super-domain, strong cross-domain element types, and the transform rules that let a message cross media — at which point the Streaming phase (loop-back through transforms) becomes expressible.
