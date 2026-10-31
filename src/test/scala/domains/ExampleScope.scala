package domains

import draco._

/** Explicit example caller vocabulary; the four observation sinks and the geodesy helper are owned by this caller. */
object ExampleScope {
  val domains: Seq[TypeName] = Seq(
    "domains.world.World", "domains.aerial.Aerial", "domains.terrestrial.Terrestrial",
    "domains.marine.Marine", "domains.ethereal.Ethereal", "domains.sentient.Sentient"
  ).map { path => val parts = path.split('.').toSeq; TypeName(parts.last, parts.init) }
  val declarations: Seq[TypeName] = CanonicalScope.domains ++ domains
  val sink: TypeName = TypeName("AerialSink", Seq("domains", "aerial"))
  /** The four media's sinks, each a hand-written helper the caller binds by its qualified name. AerialSink was
    * bound on 2026-10-23 (Consumer's action is a call tree resolution reads); the other three joined on 2026-10-31,
    * when the ConsumeReport actions were re-canonicalized from their new drake surfaces into call trees too. */
  val sinks: Seq[TypeName] = sink +: Seq("ethereal" -> "EtherealSink", "marine" -> "MarineSink", "terrestrial" -> "TerrestrialSink")
    .map { case (medium, name) => TypeName(name, Seq("domains", medium)) }
  val geodesy: TypeName = TypeName("Geodesy", Seq("domains", "world"))
  lazy val bindings: Map[String, TypeName] =
    draco.scalatarget.HostBindings.vocabulary ++ sinks.map(s => s.namePath -> s) + (geodesy.name -> geodesy)
}
