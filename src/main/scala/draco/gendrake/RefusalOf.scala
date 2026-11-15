package draco.gendrake

import draco._

trait RefusalOf extends DracoType {
  val problems: Seq[draco.Problem]
}

object RefusalOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("RefusalOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[RefusalOf] = Type[RefusalOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: => draco.TypeDefinition
  ) : RefusalOf = new RefusalOf {
    lazy val definition: draco.TypeDefinition = _definition
    lazy val subject: draco.TypeName = definition.typeName
    lazy val payload: Seq[String] = definition.codecAspect.payload
    lazy val wellFormed: Boolean = payload.forall(field => field.matches("[A-Za-z_][A-Za-z0-9_]*"))
    lazy val refusal: (Boolean, String) => Seq[draco.Problem] = (accepted, reason) => (if (accepted) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(draco.Problem(subject, reason ++ subject.name)))
    override lazy val problems: Seq[draco.Problem] = refusal(definition.codecAspect.discriminator.isEmpty, "Drake.emit: codec discriminator not yet emitted: ") ++ refusal(payload.distinct.size == payload.size, "Drake.emit: duplicate payload field in ") ++ refusal(wellFormed, "Drake.emit: invalid payload field in ")
    override lazy val typeDefinition: TypeDefinition = RefusalOf.typeDefinition
  }

  lazy val Null: RefusalOf = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition]
  )


}
