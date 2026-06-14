package domains.aerial

import draco._

trait Aerial

object Aerial extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Aerial", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[Aerial] = Type[Aerial] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("PositionReport")

  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)
}
