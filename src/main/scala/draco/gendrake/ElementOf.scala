package draco.gendrake

import draco.draketarget.Keyword
import draco.format.dracon.Dracon

import draco._

trait ElementOf extends DracoType {
  val lines: Seq[String]
}

object ElementOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ElementOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[ElementOf] = Type[ElementOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _element: => draco.TypeElement,
    _level: => Int
  ) : ElementOf = new ElementOf {
    lazy val element: draco.TypeElement = _element
    lazy val level: Int = _level
    lazy val formal: draco.TypeParameter => String = p => FormalOf(p).value
    lazy val given: Seq[draco.TypeParameter] = element.typeParameters
    lazy val name: String = element.name ++ (if (given.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", "(" ++ given.map(formal).mkString(", ") ++ ")")
    lazy val kind: String = element.typeDefinition.typeName.name
    lazy val step: String = "  "
    lazy val pad: Int => String = depth => step * depth
    lazy val indent: String = pad(level)
    lazy val keyword: String = (if (element.now) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Keyword.strict ++ " ", "") ++ Keyword.table.getOrElse(kind, "")
    lazy val slotted: Dracon => String = form => SlotOf(form).value
    lazy val slot: String = slotted(element.valueType)
    lazy val spelled: Dracon => String = node => ExpressionOf(node).value
    lazy val rendered: String = spelled(element.value)
    lazy val valued: String => String = prefix => (if (rendered == "") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(prefix, prefix ++ " " ++ rendered)
    lazy val bare: Boolean = kind == "Monadic" || kind == "Condition"
    lazy val leaf: Seq[String] = Seq(valued((if (bare) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(indent ++ keyword, indent ++ keyword ++ " " ++ name ++ " " ++ slot)))
    lazy val nested: (draco.TypeElement, Int) => Seq[String] = (member, depth) => ElementOf(member, depth).lines
    lazy val section: (String, Seq[draco.TypeElement]) => Seq[String] = (head, members) => (if (members.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(indent ++ step ++ head) ++ members.flatMap(member => nested(member, level + 2)))
    lazy val method: Seq[String] = Seq(indent ++ keyword ++ " " ++ name ++ " " ++ slot ++ " [") ++ section("parameters", element.parameters) ++ section("body", element.body) ++ Seq(indent ++ "]")
    lazy val head: Seq[String] = Seq(indent ++ keyword) ++ (if (element.name == "") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(element.name)) ++ (if (slot == "") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(slot)) ++ Seq("[")
    lazy val branch: Seq[String] = Seq(head.mkString(" ")) ++ element.body.flatMap(member => nested(member, level + 1)) ++ Seq(indent ++ "]")
    lazy val block: Boolean = kind == "Dynamic" && (element.parameters.nonEmpty || element.body.nonEmpty)
    override lazy val lines: Seq[String] = (if (kind == "Case") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(branch, (if (block) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(method, leaf))
    override lazy val typeDefinition: TypeDefinition = ElementOf.typeDefinition
  }

  lazy val Null: ElementOf = apply(
    _element = null.asInstanceOf[draco.TypeElement],
    _level = 0
  )


}
