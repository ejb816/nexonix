package draco.gendrake

import draco.format.dracon.Dracon

import draco._
import draco.draketarget._

trait FormalOf extends Surface

object FormalOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("FormalOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[FormalOf] = Type[FormalOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _formal: => draco.TypeParameter
  ) : FormalOf = new FormalOf {
    lazy val formal: draco.TypeParameter = _formal
    lazy val text: Dracon => String = form => TypeFormOf(form).value
    lazy val bound: (String, Dracon) => String = (op, form) => (if (form.isNull) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", " " ++ op ++ " " ++ text(form))
    override lazy val value: String = formal.name ++ bound(">:", formal.lowerBound) ++ bound("<:", formal.upperBound)
    override lazy val typeDefinition: TypeDefinition = FormalOf.typeDefinition
  }

  lazy val Null: FormalOf = apply(
    _formal = null.asInstanceOf[draco.TypeParameter]
  )


}
