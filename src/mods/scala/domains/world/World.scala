package domains.world

import draco._

/** The super-domain the four media derive — the shared semantic ground of the
  * transformation service (the Eagle's emanations / the invariant against which
  * meaning is preserved). A *message domain*: every contained type is a subtype of
  * `World`. For now that discipline is carried implicitly by `Domain[T]` + the
  * media's derivation; a future explicit `Message[Domain]` will name and enforce it.
  *
  * Not a Format domain: `World`'s messages are typed values (subdomain subtypes),
  * not `Format[Json]` shells. Its transform machinery (input/output adapter actors
  * and the meaning-preserving transform rules) is added in the next slice. */
trait World extends DracoType

object World extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("World", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[World] = Type[World] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[World] = Domain[World] (typeDefinition)
}
