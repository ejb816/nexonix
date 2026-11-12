package draco.gendrake

import draco.format.dracon.Dracon

import draco._
import draco.draketarget._

trait TypeFormOf extends Surface

object TypeFormOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeFormOf", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[TypeFormOf] = Type[TypeFormOf] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _form: => Dracon
  ) : TypeFormOf = new TypeFormOf {
    lazy val form: Dracon = _form
    lazy val items: String => Seq[Dracon] = key => form.field(key).itemsOrEmpty.fold(Seq.empty, members => members)
    lazy val text: Dracon => String = member => TypeFormOf(member).value
    lazy val list: String => String = key => items(key).map(text).mkString(", ")
    lazy val source: String = (if (items("->").head.has("->")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("(" ++ text(items("->").head) ++ ")", text(items("->").head))
    lazy val bound: String => String = op => text(items(op).head) ++ " " ++ op ++ " " ++ text(items(op).last)
    lazy val applied: String = text(items("()").head) ++ "(" ++ items("()").tail.map(text).mkString(", ") ++ ")"
    override lazy val value: String = (if (form.has("->")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(source ++ " -> " ++ text(items("->").last), (if (form.has("(,)")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("(" ++ list("(,)") ++ ")", (if (form.has("[]")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("[" ++ list("[]") ++ "]", (if (form.has("{}")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("{" ++ list("{}") ++ "}", (if (form.has("[]+")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("[" ++ list("[]+") ++ "]+", (if (form.has("{}+")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("{" ++ list("{}+") ++ "}+", (if (form.has("()")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(applied, (if (form.has("<:")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(bound("<:"), (if (form.has(">:")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(bound(">:"), form.textOrEmpty.fold("", leaf => leaf))))))))))
    override lazy val typeDefinition: TypeDefinition = TypeFormOf.typeDefinition
  }

  lazy val Null: TypeFormOf = apply(
    _form = Dracon.Null
  )


}
