package draco.gendrake

import draco.format.dracon.Dracon

import draco._
import draco.draketarget._

trait SlotOf extends Surface

object SlotOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("SlotOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[SlotOf] = Type[SlotOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _form: => Dracon
  ) : SlotOf = new SlotOf {
    lazy val form: Dracon = _form
    lazy val typed: Dracon => String = node => TypeFormOf(node).value
    lazy val text: String = typed(form)
    override lazy val value: String = (if (form.has("->")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("(" ++ text ++ ")", text)
    override lazy val typeDefinition: TypeDefinition = SlotOf.typeDefinition
  }

  lazy val Null: SlotOf = apply(
    _form = Dracon.Null
  )


}
