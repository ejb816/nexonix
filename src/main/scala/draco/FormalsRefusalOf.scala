package draco

import draco.format.dracon.Dracon

trait FormalsRefusalOf extends DracoType {
  val reasons: Seq[String]
}

object FormalsRefusalOf extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("FormalsRefusalOf", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[FormalsRefusalOf] = Type[FormalsRefusalOf] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _element: => TypeElement
  ) : FormalsRefusalOf = new FormalsRefusalOf {
    lazy val element: TypeElement = _element
    lazy val method: Boolean = element.typeDefinition.typeName.name == "Dynamic"
    lazy val formals: Seq[TypeParameter] = element.typeParameters
    lazy val names: Seq[String] = formals.map(formal => formal.name)
    lazy val bracketed: Boolean = element.name.contains("[") || element.name.contains("]") || element.name.contains("(") || element.name.contains(")")
    lazy val embedded: Seq[String] = (if (bracketed) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq("Method '" ++ element.name ++ "' must declare type parameters separately"), Seq.empty)
    lazy val repeated: Seq[String] = (if (names.distinct.size == names.size) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("Duplicate type parameters on '" ++ element.name ++ "'"))
    lazy val unnamed: TypeParameter => Seq[String] = formal => (if (formal.name.matches("[A-Za-z_][A-Za-z0-9_]*")) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("Invalid type parameter '" ++ formal.name ++ "'"))
    lazy val marked: String => Boolean = leaf => leaf.contains("<:") || leaf.contains(">:") || leaf.contains(": ")
    lazy val malformed: String => Seq[String] = owner => Seq("Invalid type-form bound for '" ++ owner ++ "'")
    lazy val mistaken: (String, Dracon) => Seq[String] = (owner, form) => leaves(form).filter(marked).map(leaf => "Invalid bound for '" ++ owner ++ "': " ++ leaf)
    lazy val bounded: (String, Dracon) => Seq[String] = (owner, form) => (if (form.isNull) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, (if (wellFormed(form)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(mistaken(owner, form), malformed(owner)))
    lazy val judged: TypeParameter => Seq[String] = formal => unnamed(formal) ++ bounded(formal.name, formal.lowerBound) ++ bounded(formal.name, formal.upperBound)
    override lazy val reasons: Seq[String] = (if (method) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(embedded ++ repeated ++ formals.flatMap(judged), Seq.empty)
    override lazy val typeDefinition: TypeDefinition = FormalsRefusalOf.typeDefinition
  }

  lazy val Null: FormalsRefusalOf = apply(
    _element = null.asInstanceOf[TypeElement]
  )

  def wellFormed(_form: => Dracon): Boolean = {
    lazy val form: Dracon = _form
    lazy val keys: Seq[String] = form.keys
    lazy val op: String = (if (keys.size == 1) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(keys.head, "")
    lazy val operands: Seq[Dracon] = form.field(op).itemsOrEmpty.fold(Seq(form.field(op)), members => members)
    lazy val count: Int = operands.size
    lazy val sequences: Seq[String] = Seq("[]", "[]+")
    lazy val sets: Seq[String] = Seq("{}", "{}+")
    lazy val products: Seq[String] = Seq("()", "(,)")
    lazy val arity: Boolean = (if (sequences.contains(op)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(count == 1, (if (sets.contains(op)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(count == 1 || count == 2, (if (op == "->") draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(count == 2, (if (products.contains(op)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(count >= 2, false))))
    lazy val leaf: Boolean = form.textOrEmpty.fold(false, text => text.trim.nonEmpty)
    lazy val nested: Dracon => Boolean = operand => wellFormed(operand)
    leaf || arity && operands.forall(nested)
  }
  def leaves(_form: => Dracon): Seq[String] = {
    lazy val form: Dracon = _form
    lazy val items: String => Seq[Dracon] = key => form.field(key).itemsOrEmpty.fold(Seq(form.field(key)), members => members)
    lazy val operands: Seq[Dracon] = form.keys.flatMap(items)
    lazy val nested: Dracon => Seq[String] = operand => leaves(operand)
    form.textOrEmpty.fold(operands.flatMap(nested), text => Seq(text))
  }
}
