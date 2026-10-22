package draco.dreams.editor

import draco.generator.Emission

import draco.dreams._
import draco._
import scala.collection.mutable

trait TransformBinding extends DracoType {
  val domain: draco.DomainType
  val supers: Seq[draco.DomainType]
  val rules: Seq[(draco.TypeDefinition, draco.RuleType)]
  val nameBindings: Seq[(String, draco.TypeName)]
  def domains: Seq[draco.DomainType] = supers.prepended(domain)
  def definitions: Seq[draco.TypeDefinition] = domains.flatMap(d => d.typeDictionary.elementTypes.prepended(d.typeDefinition))
  def matches(_accepted: => draco.DomainOntology): Boolean = {
    lazy val accepted: draco.DomainOntology = _accepted
    lazy val aspect: draco.DomainAspect = domain.typeDefinition.domainAspect
    lazy val expected: Seq[io.circe.Json] = definitions.filter(td => !draco.RuleAspect.isEmpty(td.ruleAspect)).map(td => draco.TypeDefinition.encoder(td))
    lazy val actual: Seq[io.circe.Json] = rules.map(r => draco.TypeDefinition.encoder(r._1))
    aspect.source.namePath == draco.TypeDefinition.typeDefinition.typeName.namePath && aspect.target.name.nonEmpty && domains.map(d => d.typeDefinition.typeName.namePath).distinct.size == domains.size && domains.forall(d => accepted.holding(d.typeDefinition.typeName).fold(false, held => draco.TypeDefinition.encoder(held.typeDefinition) == draco.TypeDefinition.encoder(d.typeDefinition) && held.typeDictionary.elementTypes.map(td => draco.TypeDefinition.encoder(td)).toSet == d.typeDictionary.elementTypes.map(td => draco.TypeDefinition.encoder(td)).toSet)) && domains.forall(d => d.typeDefinition.dracoAspect.superDomain.name.isEmpty || domains.exists(s => s.typeDefinition.typeName.namePath == d.typeDefinition.dracoAspect.superDomain.namePath)) && actual.distinct.size == actual.size && actual.toSet == expected.toSet
  }
  def generate(_subject: => draco.TypeName, _transform: => draco.TypeName, _definitions: => Seq[draco.TypeDefinition], _ontology: => draco.DomainOntology): draco.dreams.editor.Generated = {
    lazy val subject: draco.TypeName = _subject
    lazy val transform: draco.TypeName = _transform
    lazy val definitions: Seq[draco.TypeDefinition] = _definitions
    lazy val ontology: draco.DomainOntology = _ontology
    try { val emissions = run(definitions, ontology); draco.dreams.editor.Generated(subject, transform, emissions) } catch { case scala.util.control.NonFatal(error) => draco.dreams.editor.Generated(subject, transform, Seq.empty, Seq(draco.Problem(transform, "generation failed: " + error.getClass.getSimpleName + ": " + Option(error.getMessage).getOrElse("")))) }
  }
  def run(_definitions: => Seq[draco.TypeDefinition], _ontology: => draco.DomainOntology): Seq[Emission] = {
    lazy val definitions: Seq[draco.TypeDefinition] = _definitions
    lazy val ontology: draco.DomainOntology = _ontology
    require(nameBindings.map(_._1).distinct.size == nameBindings.size, "duplicate name binding")
    lazy val resolutionScope: draco.NameResolution = new draco.NameResolution(ontology, nameBindings.toMap)
    val resolved: Seq[draco.TypeDefinition] = definitions.map(resolutionScope.resolve)
    lazy val knowledge: org.evrete.api.Knowledge = draco.Rule.knowledgeService.newKnowledge("Generate")
    rules.foreach(r => r._2.pattern(knowledge))
    lazy val received: mutable.Buffer[Emission] = scala.collection.mutable.ArrayBuffer()
    lazy val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
    try { session.set("received", received); session.set("nameResolution", resolutionScope); session.insert(resolved.toSeq: _*); session.fire(); require(received.size == definitions.size, "transform did not emit once per definition"); received.toSeq } finally session.close()
  }
}

object TransformBinding extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TransformBinding", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[TransformBinding] = Type[TransformBinding] (typeDefinition)
  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)

  def apply (
    _domain: => draco.DomainType,
    _supers: => Seq[draco.DomainType],
    _rules: => Seq[(draco.TypeDefinition, draco.RuleType)],
    _nameBindings: => Seq[(String, draco.TypeName)] = Seq.empty
  ) : TransformBinding = new TransformBinding {
    override lazy val domain: draco.DomainType = _domain
    override lazy val supers: Seq[draco.DomainType] = _supers
    override lazy val rules: Seq[(draco.TypeDefinition, draco.RuleType)] = _rules
    override lazy val nameBindings: Seq[(String, draco.TypeName)] = _nameBindings
    override lazy val typeDefinition: TypeDefinition = TransformBinding.typeDefinition
  }

  lazy val Null: TransformBinding = apply(
    _domain = null.asInstanceOf[draco.DomainType],
    _supers = Seq.empty,
    _rules = Seq.empty,
    _nameBindings = Seq.empty
  )


}
