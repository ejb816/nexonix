package draco.base

import org.nexonix.domains.Primal

trait Ordinal extends Unit with Primal[Enumeration] {
  override val name: String = "Ordinal"
  override val description: String = "Values associated with an ordered sequence"
}
