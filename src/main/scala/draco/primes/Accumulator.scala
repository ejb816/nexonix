package draco.primes

import draco._
import scala.collection.mutable

trait Accumulator extends Primes {
  val primeSet: mutable.Set[Int]
  val compositeSet: mutable.Set[Int]
  val naturalSet: mutable.Set[Int]
  val intervalTextSet: mutable.Set[(Long, String)]
}

object Accumulator extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Accumulator", _namePackage = Seq ("draco", "primes")))
  lazy val dracoType: Type[Accumulator] = Type[Accumulator] (typeDefinition)
  lazy val domainType: Domain[Primes] = Domain[Primes] (typeDefinition)

  def apply () : Accumulator = new Accumulator {
    override lazy val primeSet: mutable.Set[Int] = mutable.Set[Int] ()
    override lazy val compositeSet: mutable.Set[Int] = mutable.Set[Int] ()
    override lazy val naturalSet: mutable.Set[Int] = mutable.Set[Int] ()
    override lazy val intervalTextSet: mutable.Set[(Long, String)] = mutable.Set[(Long, String)] ()
  }

  lazy val Null: Accumulator = apply()

}
