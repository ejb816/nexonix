package domains.ethereal

import draco._
import domains._
import draco.format.dracon._

trait LaunchIntent extends Ethereal with Dracon

object LaunchIntent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("LaunchIntent", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[LaunchIntent] = Type[LaunchIntent] (typeDefinition)
  lazy val domainType: Domain[Ethereal] = Domain[Ethereal] (typeDefinition)
}
