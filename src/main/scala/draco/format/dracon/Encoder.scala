package draco.format.dracon

import draco.format._
import draco._

trait Encoder[T] extends DracoType {
  val encode: T => Dracon
}

object Encoder extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Encoder", _namePackage = Seq ("draco", "format", "dracon")))
  lazy val dracoType: Type[Encoder[_]] = Type[Encoder[_]] (typeDefinition)
  lazy val domainType: Domain[Dracon] = Domain[Dracon] (typeDefinition)

  def apply[T] (
    _encode: => T => Dracon
  ) : draco.format.dracon.Encoder[T] = new draco.format.dracon.Encoder[T] {
    override lazy val encode: T => Dracon = _encode
    override lazy val typeDefinition: TypeDefinition = draco.format.dracon.Encoder.typeDefinition
  }

  lazy val Null: Encoder[_] = apply[Any](
    _encode = null.asInstanceOf[Any => Dracon]
  )


}
