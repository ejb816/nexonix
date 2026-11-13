package draco.gendrake

import draco.format.dracon.Dracon

import draco._
import draco.draketarget._

trait ExpressionOf extends Surface

object ExpressionOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ExpressionOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[ExpressionOf] = Type[ExpressionOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _node: => Dracon,
    _parent: => String = "",
    _index: => Int = 0,
    _count: => Int = 1
  ) : ExpressionOf = new ExpressionOf {
    lazy val node: Dracon = _node
    lazy val parent: String = _parent
    lazy val index: Int = _index
    lazy val count: Int = _count
    lazy val lambda: String = "\\"
    lazy val keys: Seq[String] = Seq(".", "()", lambda, "(,)", "[]", "{}", "=") ++ draco.draketarget.Fixity.table.keys.toSeq
    lazy val op: String = (keys.find(key => node.has(key)) match { case Some(v) => draco.drake.Present(v); case None => draco.drake.Absent() }).fold("", key => key)
    lazy val items: Seq[Dracon] = node.field(op).itemsOrEmpty.fold(Seq(node.field(op)), members => members)
    lazy val args: Seq[String] = items.indices.map(i => ExpressionOf(items(i), op, i, items.size).value)
    lazy val infix: String => Boolean = key => draco.draketarget.Fixity.table.contains(key)
    lazy val tight: String => Int = key => draco.draketarget.Fixity.table(key).precedence
    lazy val side: Boolean = (if (draco.draketarget.Fixity.table(parent).associativity == "left") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(index > 0, (if (draco.draketarget.Fixity.table(parent).associativity == "right") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(index < count - 1, true))
    lazy val looser: Boolean = tight(op) < tight(parent) || tight(op) == tight(parent) && side
    lazy val paired: Boolean = (if (infix(parent)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse((if (infix(op)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(looser, op == lambda && index < count - 1), (parent == "." || parent == "()") && index == 0 && (infix(op) || op == lambda))
    lazy val text: String = (if (op == ".") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(args.mkString("."), (if (op == "()") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(args.head ++ "(" ++ args.tail.mkString(", ") ++ ")", (if (op == lambda) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(lambda ++ args.init.mkString(" ") ++ " -> " ++ args.last, (if (op == "(,)") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("(" ++ args.mkString(", ") ++ ")", (if (op == "[]") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("[" ++ args.mkString(", ") ++ "]", (if (op == "{}") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("{" ++ args.mkString(", ") ++ "}", (if (op == "=") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(args.head ++ ":" ++ args.last, (if (infix(op)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(args.mkString(" " ++ op ++ " "), node.textOrEmpty.fold("", leaf => leaf)))))))))
    override lazy val value: String = (if (paired) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("(" ++ text ++ ")", text)
    override lazy val typeDefinition: TypeDefinition = ExpressionOf.typeDefinition
  }

  lazy val Null: ExpressionOf = apply(
    _node = Dracon.Null,
    _parent = "",
    _index = 0,
    _count = 1
  )


}
