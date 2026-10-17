package draco.format.dracon

import draco.format._
import draco._

trait Decoder[T] extends DracoType {
  val decode: Dracon => draco.drake.Presence[T]
}

object Decoder extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Decoder", _namePackage = Seq ("draco", "format", "dracon")))
  lazy val dracoType: Type[Decoder[_]] = Type[Decoder[_]] (typeDefinition)
  lazy val domainType: Domain[Dracon] = Domain[Dracon] (typeDefinition)

  def apply[T] (
    _decode: => Dracon => draco.drake.Presence[T]
  ) : Decoder[T] = new Decoder[T] {
    override lazy val decode: Dracon => draco.drake.Presence[T] = _decode
    override lazy val typeDefinition: TypeDefinition = Decoder.typeDefinition
  }

  lazy val Null: Decoder[_] = apply[Any](
    _decode = null.asInstanceOf[Dracon => draco.drake.Presence[Any]]
  )


}
