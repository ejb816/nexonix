package draco.draketarget

import draco._

trait DomainAspectText extends Surface

object DomainAspectText extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DomainAspectText", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[DomainAspectText] = Type[DomainAspectText] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  def apply (
    _domainReference: => String,
    _parentReference: => String = "",
    _sourceReference: => String = "",
    _targetReference: => String = "",
    _members: => Seq[String] = Seq.empty,
    _nameless: => Boolean = false
  ) : DomainAspectText = new DomainAspectText {
    lazy val parentLines: Seq[String] = (if (_parentReference.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  super " ++ _parentReference))
    lazy val sourceLines: Seq[String] = (if (_sourceReference.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  source " ++ _sourceReference))
    lazy val targetLines: Seq[String] = (if (_targetReference.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  target " ++ _targetReference))
    lazy val memberLines: Seq[String] = (if (_members.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  types [") ++ _members.map(member => "    " ++ member) ++ Seq("  ]"))
    override lazy val value: String = (if (_nameless) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("domain", (if (_domainReference.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", (Seq("domain " ++ _domainReference) ++ parentLines ++ sourceLines ++ targetLines ++ memberLines).mkString("\n")))
    override lazy val typeDefinition: TypeDefinition = DomainAspectText.typeDefinition
  }

  lazy val Null: DomainAspectText = apply(
    _domainReference = "",
    _parentReference = "",
    _sourceReference = "",
    _targetReference = "",
    _members = Seq.empty,
    _nameless = false
  )


}
