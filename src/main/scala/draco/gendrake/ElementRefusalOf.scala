package draco.gendrake

import draco.draketarget.Keyword
import draco.format.dracon.Dracon

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
    lazy val spelled: Dracon => String = node => ExpressionOf(node).value
    lazy val branch: Boolean = kind == "Case"
    lazy val block: Boolean = kind == "Dynamic" && (element.parameters.nonEmpty || element.body.nonEmpty)
    lazy val result: String = " carries a value outside its body — the result is the body element named `value`"
    lazy val opener: String = (if (branch) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("Drake.emit: case-branch", "Drake.emit: block dyn '" ++ element.name ++ "'")
    lazy val misplaced: Seq[draco.Problem] = (if ((branch || block) && spelled(element.value) != "") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq(draco.Problem(subject, opener ++ result)), Seq.empty)
    lazy val nested: draco.TypeElement => Seq[draco.Problem] = member => ElementRefusalOf(member, subject).problems
    override lazy val problems: Seq[draco.Problem] = unkeyed ++ misplaced ++ element.parameters.flatMap(nested) ++ element.body.flatMap(nested)
    override lazy val typeDefinition: TypeDefinition = ElementRefusalOf.typeDefinition
  }

  lazy val Null: ElementRefusalOf = apply(
    _element = null.asInstanceOf[draco.TypeElement],
    _subject = null.asInstanceOf[draco.TypeName]
  )


}
