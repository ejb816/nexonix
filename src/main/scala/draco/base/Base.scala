package draco.base

import draco.{DomainElement, DomainName, TypeDefinition, TypeName}
import org.evrete.KnowledgeService
import org.evrete.api.Knowledge
import org.nexonix.domains
import org.nexonix.domains.Domain

trait Base extends DomainElement {
  override val knowledge: Knowledge = knowledgeService.newKnowledge("Base")
}

object Base {
  val typeElementNames: Seq[String] = Seq (
    "Cardinal",
    "Cartesian",
    "Coordinates",
    "Cylindrical",
    "Distance",
    "Meters",
    "Nominal",
    "Ordinal",
    "Orientable",
    "Polar",
    "Radians",
    "Rectangular",
    "Rotation",
    "Spacetime",
    "Spherical",
    "Unit"
  )
  val domainName: DomainName = DomainName(TypeName(_name = "Base", _parent = "draco.base"), typeElementNames)
  val base: Base = new Base {
    override val knowledgeService: KnowledgeService = DomainElement.knowledgeService
    override val knowledge: Knowledge = knowledgeService.newKnowledge("Base Knowledge")
    override val domain: Domain[Base] = domains.Domain[Base] (domainName)
  }
}
