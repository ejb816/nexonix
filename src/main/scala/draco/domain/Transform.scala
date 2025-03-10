package draco.domain

trait Transform {
  val source: Domain[TypeData]
  val sink:  Domain[TypeData]
}

object Transform {
  def apply(
            dictionary: Dictionary[Domain[TypeData]],
            sourceTypeName: TypeName,
            sinkTypeName: TypeName
           ) : Transform = new Transform {
    override val source: Domain[TypeData] = dictionary(sourceTypeName.name)
    override val sink: Domain[TypeData] = dictionary(sinkTypeName.name)
  }
}
