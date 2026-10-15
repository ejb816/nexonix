package draco.dreams.user

import draco.dreams._
import draco._

trait Latent extends Request {
  val definition: draco.TypeDefinition
  val members: Seq[draco.TypeDefinition]
}

object Latent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Latent", _namePackage = Seq ("draco", "dreams", "user")))
  lazy val dracoType: Type[Latent] = Type[Latent] (typeDefinition)
  lazy val domainType: Domain[User] = Domain[User] (typeDefinition)
}
