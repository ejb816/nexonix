package draco.base

trait Unit extends draco.DracoType {
  val name: String = "Unit"
  val description: String = "Abstract Unit root"
}

object Unit extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Unit", _namePackage = Seq("draco", "base")))
  lazy val typeInstance: draco.Type[Unit] = draco.Type[Unit] (typeDefinition)
}
