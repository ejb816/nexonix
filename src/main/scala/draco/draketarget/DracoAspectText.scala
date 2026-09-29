package draco.draketarget

import draco._

trait DracoAspectText extends Surface

object DracoAspectText extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DracoAspectText", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[DracoAspectText] = Type[DracoAspectText] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  def apply (
    _name: => String,
    _parents: => Seq[String] = Seq.empty,
    _modules: => Seq[String] = Seq.empty,
    _extensible: => String = "",
    _elements: => Seq[String] = Seq.empty,
    _factoryPresent: => Boolean = false,
    _factoryResult: => String = "",
    _factoryParameters: => Seq[String] = Seq.empty,
    _factoryBody: => Seq[String] = Seq.empty,
    _globals: => Seq[String] = Seq.empty
  ) : DracoAspectText = new DracoAspectText {
    lazy val header: Seq[String] = (if (_name.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("type " ++ _name ++ (if (_parents.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", " from " ++ _parents.mkString(" "))))
    lazy val moduleLines: Seq[String] = (if (_modules.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  modules [") ++ _modules.map(member => "    " ++ member) ++ Seq("  ]"))
    lazy val extensibleLines: Seq[String] = (if (_extensible.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  extensible " ++ _extensible))
    lazy val elementLines: Seq[String] = (if (_elements.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  elements") ++ _elements)
    lazy val parameterLines: Seq[String] = (if (_factoryParameters.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("    parameters") ++ _factoryParameters)
    lazy val bodyLines: Seq[String] = (if (_factoryBody.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("    body") ++ _factoryBody)
    lazy val factoryLines: Seq[String] = (if (_factoryPresent) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq("  factory" ++ (if (_factoryResult.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", " " ++ _factoryResult)) ++ parameterLines ++ bodyLines, Seq.empty)
    lazy val globalLines: Seq[String] = (if (_globals.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  globals") ++ _globals)
    override lazy val value: String = (header ++ moduleLines ++ extensibleLines ++ elementLines ++ factoryLines ++ globalLines).mkString("\n")
    override lazy val typeDefinition: TypeDefinition = DracoAspectText.typeDefinition
  }

  lazy val Null: DracoAspectText = apply(
    _name = "",
    _parents = Seq.empty,
    _modules = Seq.empty,
    _extensible = "",
    _elements = Seq.empty,
    _factoryPresent = false,
    _factoryResult = "",
    _factoryParameters = Seq.empty,
    _factoryBody = Seq.empty,
    _globals = Seq.empty
  )


}
