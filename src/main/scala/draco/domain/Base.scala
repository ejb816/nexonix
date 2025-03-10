package draco.domain

trait Base extends Domain[TypeData]

object Base {
  val dictionary: Dictionary[Domain[TypeData]] = Dictionary[Domain[TypeData]]()
  val base: Base = new Base {
    val superDomain: Domain[TypeData] = this
    override val subDomains: Dictionary[Domain[TypeData]] = dictionary
    override val base: Domain[TypeData] = this
  }
}
