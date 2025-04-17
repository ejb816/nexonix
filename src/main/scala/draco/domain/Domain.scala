package draco.domain

trait Domain {
  val typeNames: Seq[TypeName]
  val domains: Map[TypeName, TypeDefinition]
}
