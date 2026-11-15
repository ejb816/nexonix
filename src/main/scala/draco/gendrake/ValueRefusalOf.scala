package draco.gendrake

import draco.draketarget.Fixity
import draco.format.dracon.Dracon

import draco._

trait ValueRefusalOf extends DracoType {
  val reasons: Seq[String]
}

object ValueRefusalOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ValueRefusalOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[ValueRefusalOf] = Type[ValueRefusalOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _node: => Dracon
  ) : ValueRefusalOf = new ValueRefusalOf {
    lazy val node: Dracon = _node
    lazy val lambda: String = "\\"
    lazy val structural: Seq[String] = Seq(".", "()", lambda, "(,)", "[]", "{}", "=")
    lazy val leaf: Boolean = node.textOrEmpty.fold(false, text => true)
    lazy val keys: Seq[String] = node.keys
    lazy val single: Boolean = keys.size == 1
    lazy val op: String = (if (single) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(keys.head, "")
    lazy val known: Boolean = structural.contains(op) || Fixity.table.contains(op)
    lazy val operands: Seq[Dracon] = node.field(op).itemsOrEmpty.fold(Seq(node.field(op)), members => members)
    lazy val nested: Dracon => Seq[String] = operand => ValueRefusalOf(operand).reasons
    lazy val unknown: Seq[String] = (if (known) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("Drake.expression: unknown operator '" ++ op ++ "' in " ++ node.compact))
    lazy val tree: Seq[String] = (if (single) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(unknown ++ operands.flatMap(nested), Seq("Drake.expression: unrenderable value " ++ node.compact))
    override lazy val reasons: Seq[String] = (if (leaf) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, tree)
    override lazy val typeDefinition: TypeDefinition = ValueRefusalOf.typeDefinition
  }

  lazy val Null: ValueRefusalOf = apply(
    _node = Dracon.Null
  )


}
