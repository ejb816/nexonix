package draco.drake

import draco._

sealed trait Presence[T] extends DracoType {
  def fold[R](_absent: => R, _present: => T => R): R
  def ifThenElse[R](_thenBranch: => R, _elseBranch: => R): R
}

object Presence extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Presence", _namePackage = Seq ("draco", "drake")))
  lazy val dracoType: Type[Presence[_]] = Type[Presence[_]] (typeDefinition)
  lazy val domainType: Domain[Drake] = Domain[Drake] (typeDefinition)
}

trait Present[T] extends Presence[T] {
  val value: T
  def fold[R](_absent: => R, _present: => T => R): R = {
    lazy val absent: R = _absent
    lazy val present: T => R = _present
    present(value)
  }
  def ifThenElse[R](_thenBranch: => R, _elseBranch: => R): R = {
    lazy val thenBranch: R = _thenBranch
    lazy val elseBranch: R = _elseBranch
    thenBranch
  }
}

object Present extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Present", _namePackage = Seq ("draco", "drake")))
  lazy val dracoType: Type[Present[_]] = Type[Present[_]] (typeDefinition)
  lazy val domainType: Domain[Drake] = Domain[Drake] (typeDefinition)

  def apply[T] (
    _value: => T
  ) : Present[T] = new Present[T] {
    override lazy val value: T = _value
    override lazy val typeDefinition: TypeDefinition = Present.typeDefinition
  }

  lazy val Null: Present[_] = apply[Any](
    _value = null.asInstanceOf[Any]
  )


}

trait Absent[T] extends Presence[T] {
  def fold[R](_absent: => R, _present: => T => R): R = {
    lazy val absent: R = _absent
    lazy val present: T => R = _present
    absent
  }
  def ifThenElse[R](_thenBranch: => R, _elseBranch: => R): R = {
    lazy val thenBranch: R = _thenBranch
    lazy val elseBranch: R = _elseBranch
    elseBranch
  }
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
