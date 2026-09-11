package draco.gendrake

import draco._
import draco.draketarget._

trait DomainLineOf extends DomainLine

object DomainLineOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DomainLineOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[DomainLineOf] = Type[DomainLineOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: TypeDefinition
  ) : DomainLineOf = new DomainLineOf {
    val line: DomainLine = DomainLine(_definition.domainAspect.typeName.namePackage, _definition.domainAspect.typeName.name)
    override lazy val value: String = line.value
    override lazy val typeDefinition: TypeDefinition = DomainLineOf.typeDefinition
  }

  lazy val Null: DomainLineOf = apply(
    _definition = null.asInstanceOf[TypeDefinition]
  )


}
