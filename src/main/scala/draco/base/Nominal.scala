package draco.base

import org.nexonix.domains.Primal

trait Nominal extends Unit with Primal[String] {
  override val name: String = "Nominal"
  override val description: String = "Sequence of glyphs signifying name or identity"
}
