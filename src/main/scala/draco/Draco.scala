package draco

trait Draco

object Draco extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Draco", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Draco] = Type[Draco] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Action", "Actor", "ActorAspect", "ActorType", "Aspects", "Assembly", "Binding", "BodyElement", "CLI", "CodecAspect", "Condition", "ContentSink", "Dictionary", "Domain", "DomainAspect", "DomainDictionary", "DomainTransform", "DomainType", "DracoAspect", "DracoType", "Dynamic", "Factory", "Fixed", "Holon", "Main", "Monadic", "Mutable", "Parameter", "Pattern", "Primal", "REPL", "Rule", "RuleAspect", "RuleType", "SourceContent", "Test", "Type", "TypeDefinition", "TypeDictionary", "TypeElement", "TypeName", "TypeTransform", "Value", "Variable")

  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
