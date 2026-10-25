package domains

import draco._

/** Explicit example caller vocabulary; the observation sink and geodesy helper are owned by this caller. */
object ExampleScope {
  val domains: Seq[TypeName] = Seq(
    "domains.world.World", "domains.aerial.Aerial", "domains.terrestrial.Terrestrial",
    "domains.marine.Marine", "domains.ethereal.Ethereal", "domains.sentient.Sentient"
  ).map { path => val parts = path.split('.').toSeq; TypeName(parts.last, parts.init) }
  val declarations: Seq[TypeName] = CanonicalScope.domains ++ domains
  val sink: TypeName = TypeName("AerialSink", Seq("domains", "aerial"))
  val geodesy: TypeName = TypeName("Geodesy", Seq("domains", "world"))
  lazy val bindings: Map[String, TypeName] =
    draco.scalatarget.HostBindings.vocabulary + (sink.namePath -> sink) + (geodesy.name -> geodesy)
}
