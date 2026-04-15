package draco.primes

import draco._

import scala.collection.mutable


trait Accumulator extends Primes {
  val primeSet: mutable.Set[Int]
  val compositeSet: mutable.Set[Int]
  val naturalSet: mutable.Set[Int]
  val intervalTextSet: mutable.Set[(Long,String)]
}

object Accumulator extends App with TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Accumulator", _namePackage = Seq("draco", "primes")))

  lazy val typeInstance: DracoType = Type[Accumulator] (typeDefinition)

  def apply () : Accumulator = new Accumulator {
    override val domainInstance: DomainType = Primes.domainInstance
    override val typeDefinition: TypeDefinition = Accumulator.typeDefinition
    override val typeInstance: DracoType = Accumulator.typeInstance
    override val primeSet: mutable.Set[Int] = mutable.Set[Int] ()
    override val compositeSet: mutable.Set[Int] = mutable.Set[Int] ()
    override val naturalSet: mutable.Set[Int] = mutable.Set[Int] ()
    override val intervalTextSet: mutable.Set[(Long, String)] = mutable.Set[(Long, String)] ()
  }
}
