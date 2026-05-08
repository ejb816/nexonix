package draco

trait Draco extends Extensible {
  val superDomain: DomainType = DomainType.Null
}

object Draco extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Draco", _namePackage = Seq ("draco")))
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
