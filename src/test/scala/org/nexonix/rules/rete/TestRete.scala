package org.nexonix.rules.rete

import org.scalatest.funsuite.AnyFunSuite
//import org.nexonix.rete.Rete.{Network, addFact, addProduction, fireRules}
class TestRete  extends AnyFunSuite {
  test("Rete Test") {
    def action(fact: Fact): Unit = {
      println(fact)
    }
//    val testPattern: Pattern = Map[String,String] (("Hello", "World"))
//    val testFact: Fact = Map[String,String] (("Hello", "World"))
//    val pNode: ProductionNode = ProductionNode(List(testPattern), action)
//    val network0: Rete.Network = Map[Int, Node]()
//    val network1: Network = addProduction(network0, pNode)
//    val network2: Network = addFact(network1, testFact)
//    fireRules(network2)
  }
}
