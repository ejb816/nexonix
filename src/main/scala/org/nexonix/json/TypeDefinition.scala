package org.nexonix.json

trait TypeDefinition {
  val name: String
  val prefix: String = ""
  val suffix: String = ""
  val typeArguments: Seq[String]
  val packageName: Seq[String]
  val dependsOn: Seq[String]
  val derivesFrom: Seq[String]
}
