package draco

trait Transform {
  val source: Domain
  val sink:  Domain
}

object Transform {
  def apply(
             _source: Domain,
             _sink: Domain
           ) : Transform = new Transform {
    override val source: Domain = _source
    override val sink: Domain = _sink
  }
}
