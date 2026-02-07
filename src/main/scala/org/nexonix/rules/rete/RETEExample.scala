package org.nexonix.rete
// Example usage
object RETEExample extends App {
  private val engine = new RETEEngine

  // Rule 1: If there is a person named Alice
  private val personConditions = List(
    EqualsCondition("type", "person"),
    EqualsCondition("name", "Alice")
  )
  private val personCompositeCondition = CompositeCondition(personConditions)
  engine.addRule(List(personCompositeCondition), token => {
    println(s"Rule 1 fired: Found person named Alice: ${token.facts}")
  })

  // Rule 2: If there is a pet owned by Alice
  private val petConditions = List(
    EqualsCondition("type", "pet"),
    EqualsCondition("owner", "Alice")
  )
  private val petCompositeCondition = CompositeCondition(petConditions)
  engine.addRule(List(petCompositeCondition), token => {
    println(s"Rule 2 fired: Found pet owned by Alice: ${token.facts}")
  })

  // Rule 3: If there is a person and a pet they own
  private val personConditions2 = List(
    EqualsCondition("type", "person"),
    VariableCondition("name", "personName")
  )
  private val personCompositeCondition2 = CompositeCondition(personConditions2)

  private val petConditions2 = List(
    EqualsCondition("type", "pet"),
    VariableCondition("owner", "personName")
  )
  private val petCompositeCondition2 = CompositeCondition(petConditions2)

  // Additional Condition for Rule 4
  private val petNameCondition = EqualsCondition("name", "Fluffy")

  // Rule 4: If there is a person, a pet they own, and the pet's name is Fluffy
  engine.addRule(List(personCompositeCondition2, petCompositeCondition2, petNameCondition), token => {
    println(s"Rule 4 fired: Found person and their pet named Fluffy: ${token.facts}")
  })

  engine.addRule(List(personCompositeCondition2, petCompositeCondition2), token => {
    println(s"Rule 3 fired: Found person and their pet: ${token.facts}")
  })

  // Assert facts
  private val fact1 = Fact("1", Map("type" -> "person", "name" -> "Alice"))
  private val fact2 = Fact("2", Map("type" -> "pet", "name" -> "Fluffy", "owner" -> "Alice"))
  private val fact3 = Fact("3", Map("type" -> "person", "name" -> "Bob"))
  private val fact4 = Fact("4", Map("type" -> "pet", "name" -> "Spike", "owner" -> "Bob"))

  engine.assertFact(fact1) // Should trigger Rule 1
  engine.assertFact(fact2) // Should trigger Rule 2, Rule 3, and Rule 4
  engine.assertFact(fact3) // Should not trigger Rule 1
  engine.assertFact(fact4) // Should trigger Rule 3 (but not Rule 4)
}
