package draco.gendrake

import draco._
import draco.draketarget._

trait CodecAspectOf extends CodecAspectText

object CodecAspectOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("CodecAspectOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[CodecAspectOf] = Type[CodecAspectOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => draco.TypeDefinition
  ) : CodecAspectOf = new CodecAspectOf {
    lazy val definition: draco.TypeDefinition = _definition
    lazy val rendered: draco.draketarget.CodecAspectText = draco.draketarget.CodecAspectText(definition.codecAspect.payload)
    override lazy val value: String = rendered.value
    override lazy val typeDefinition: TypeDefinition = CodecAspectOf.typeDefinition
  }

  lazy val Null: CodecAspectOf = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition]
  )


}
