package draco

/** The scope every test that generates corpus Scala supplies explicitly (2026-10-21): the
  * identity-only ontology of the canonical domains — stage one of two-stage loading, composed
  * from anchors alone — and the ScalaTarget's declared host vocabulary. Nothing defaults to it;
  * a test names it, as a caller of the loader or of Generate does. */
object CanonicalScope {
  val domains: Seq[TypeName] = Seq(
    "draco.dreams.editor.Editor", "draco.dreams.user.User", "draco.dreams.Dreams", "draco.service.Service",
    "draco.gendrake.GenDrake", "draco.genscala.GenScala", "draco.scalatarget.ScalaTarget",
    "draco.draketarget.DrakeTarget", "draco.generator.carrier.Carrier", "draco.generator.Generator",
    "draco.drake.Drake", "draco.rete.Rete", "draco.format.xml.XML", "draco.format.dracon.Dracon",
    "draco.format.Format", "draco.primes.Primes", "draco.base.Base", "draco.Draco"
  ).map { p => val parts = p.split('.').toSeq; TypeName(parts.last, parts.dropRight(1)) }

  lazy val scope: NameResolution =
    new NameResolution(DomainBuilder.identities(domains), draco.scalatarget.HostBindings.vocabulary)
}
