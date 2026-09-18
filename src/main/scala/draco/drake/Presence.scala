package draco.drake

import draco._

sealed trait Presence[T] extends DracoType {
  def fold[R](absent: R, present: T => R): R
}

object Presence extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Presence", _namePackage = Seq ("draco", "drake")))
  lazy val dracoType: Type[Presence[_]] = Type[Presence[_]] (typeDefinition)
  lazy val domainType: Domain[Drake] = Domain[Drake] (typeDefinition)
}

trait Present[T] extends Presence[T] {
  val value: T
  def fold[R](absent: R, present: T => R): R = present(value)
}

object Present extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Present", _namePackage = Seq ("draco", "drake")))
  lazy val dracoType: Type[Present[_]] = Type[Present[_]] (typeDefinition)
  lazy val domainType: Domain[Drake] = Domain[Drake] (typeDefinition)

  def apply[T] (
    _value: T
  ) : Present[T] = new Present[T] {
    override lazy val value: T = _value
    override lazy val typeDefinition: TypeDefinition = Present.typeDefinition
  }

  lazy val Null: Present[_] = apply[Any](
    _value = null.asInstanceOf[Any]
  )


}

trait Absent[T] extends Presence[T] {
  def fold[R](absent: R, present: T => R): R = absent
}

object Absent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Absent", _namePackage = Seq ("draco", "drake")))
  lazy val dracoType: Type[Absent[_]] = Type[Absent[_]] (typeDefinition)
  lazy val domainType: Domain[Drake] = Domain[Drake] (typeDefinition)

  def apply[T] () : Absent[T] = new Absent[T] {
    override lazy val typeDefinition: TypeDefinition = Absent.typeDefinition
  }

  lazy val Null: Absent[_] = apply[Nothing]()


}
