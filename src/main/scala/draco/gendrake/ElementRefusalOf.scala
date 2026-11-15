package draco.gendrake

import draco.draketarget.Keyword

import draco._

trait ElementRefusalOf extends DracoType {
  val problems: Seq[draco.Problem]
}

object ElementRefusalOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ElementRefusalOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[ElementRefusalOf] = Type[ElementRefusalOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _element: => draco.TypeElement,
    _subject: => draco.TypeName
  ) : ElementRefusalOf = new ElementRefusalOf {
    lazy val element: draco.TypeElement = _element
    lazy val subject: draco.TypeName = _subject
    lazy val kind: String = element.typeDefinition.typeName.name
    lazy val reason: String = "Drake.emit: no drake keyword for element '" ++ element.name ++ "' (" ++ kind ++ ")"
    lazy val unkeyed: Seq[draco.Problem] = (if (Keyword.table.contains(kind)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(draco.Problem(subject, reason)))
    lazy val nested: draco.TypeElement => Seq[draco.Problem] = member => ElementRefusalOf(member, subject).problems
    override lazy val problems: Seq[draco.Problem] = unkeyed ++ element.parameters.flatMap(nested) ++ element.body.flatMap(nested)
    override lazy val typeDefinition: TypeDefinition = ElementRefusalOf.typeDefinition
  }

  lazy val Null: ElementRefusalOf = apply(
    _element = null.asInstanceOf[draco.TypeElement],
    _subject = null.asInstanceOf[draco.TypeName]
  )


}
