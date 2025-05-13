package draco.domain.primes

trait PrimeOrdinal {
  val prime: PrimeOrdinal
  val power: PrimeOrdinal
  val product: PrimeOrdinal
  def apply() : Int = p(prime, power, product)

  def p(
         _prime: PrimeOrdinal,
         _power: PrimeOrdinal,
         _product: PrimeOrdinal
       ): Int = {
    val poValue = _prime.prime() ^ _power.power() * _product.product()
    poValue
  }
}

object PrimeOrdinal extends App {
  def apply(
             _prime: PrimeOrdinal,
             _power: PrimeOrdinal,
             _product: PrimeOrdinal
           ): PrimeOrdinal = new PrimeOrdinal {
    override val prime: PrimeOrdinal = _prime
    override val power: PrimeOrdinal = _power
    override val product: PrimeOrdinal = _product
  }
}