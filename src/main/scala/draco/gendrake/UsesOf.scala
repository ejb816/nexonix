package draco.gendrake

import draco._
import draco.draketarget._

trait UsesOf extends UsesText

object UsesOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("UsesOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[UsesOf] = Type[UsesOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => draco.TypeDefinition
  ) : UsesOf = new UsesOf {
    lazy val definition: draco.TypeDefinition = _definition
    lazy val rendered: draco.draketarget.UsesText = draco.draketarget.UsesText(definition.uses.map(name => (name.namePackage ++ Seq(name.name)).mkString(" ")))
    override lazy val value: String = rendered.value
    override lazy val typeDefinition: TypeDefinition = UsesOf.typeDefinition
  }

  lazy val Null: UsesOf = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition]
  )


}
