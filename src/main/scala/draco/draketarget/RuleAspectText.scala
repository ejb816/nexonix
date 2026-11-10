package draco.draketarget

import draco._

trait RuleAspectText extends Surface

object RuleAspectText extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("RuleAspectText", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[RuleAspectText] = Type[RuleAspectText] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  def apply (
    _variables: => Seq[String] = Seq.empty,
    _conditions: => Seq[String] = Seq.empty,
    _action: => Seq[String] = Seq.empty,
    _present: => Boolean = true
  ) : RuleAspectText = new RuleAspectText {
    lazy val variableLines: Seq[String] = (if (_variables.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("    variables") ++ _variables)
    lazy val conditionLines: Seq[String] = (if (_conditions.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("    conditions") ++ _conditions)
    lazy val patternLines: Seq[String] = (if (variableLines.isEmpty && conditionLines.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  pattern") ++ variableLines ++ conditionLines)
    lazy val actionLines: Seq[String] = (if (_action.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  action") ++ _action)
    override lazy val value: String = (if (_present) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse((Seq("rule") ++ patternLines ++ actionLines).mkString("\n"), "")
    override lazy val typeDefinition: TypeDefinition = RuleAspectText.typeDefinition
  }

  lazy val Null: RuleAspectText = apply()


}
