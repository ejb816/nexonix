package draco

trait RootType {
  val typeName: TypeName
  val elementNames: Seq[String]
  val typeDictionary: TypeDictionary
}

object RootType extends App {
  def apply (
      _typeName: TypeName,
      _elementNames: Seq[String]
    ) : RootType = new RootType {
    override val typeName: TypeName = _typeName
    override val elementNames: Seq[String] = Seq(_typeName.name) ++ _elementNames
    override val typeDictionary: TypeDictionary = TypeDictionary(_typeName, _elementNames)
  }
}
