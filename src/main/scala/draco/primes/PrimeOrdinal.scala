package draco.primes

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

object PrimeOrdinal extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "PrimeOrdinal",
      _namePackage = Seq ("draco", "primes")
    ),
    _elements = Seq (
      draco.Fixed ("prime", "PrimeOrdinal"),
      draco.Fixed ("power", "PrimeOrdinal"),
      draco.Fixed ("product", "PrimeOrdinal")
    ),
    _factory = draco.Factory (
      "PrimeOrdinal",
      _parameters = Seq (
        draco.Parameter ("prime", "PrimeOrdinal", ""),
        draco.Parameter ("power", "PrimeOrdinal", ""),
        draco.Parameter ("product", "PrimeOrdinal", "")
      )
    )
  )
  lazy val typeInstance: draco.Type[PrimeOrdinal] = draco.Type[PrimeOrdinal] (typeDefinition)

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