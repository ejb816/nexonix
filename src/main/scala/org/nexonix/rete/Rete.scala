package org.nexonix.rete
// Define a Fact, which is a piece of data in the working memory
case class Fact(id: String, attributes: Map[String, Any])

// Define a Condition, representing a test or pattern to match against facts
trait Condition {
  // Evaluates a fact and returns an Option of variable bindings if it matches
  def evaluate(fact: Fact): Option[Map[String, Any]]
}

// EqualsCondition with optional variable binding
case class EqualsCondition(attribute: String, value: Any, variable: Option[String] = None) extends Condition {
  override def evaluate(fact: Fact): Option[Map[String, Any]] = {
    fact.attributes.get(attribute) match {
      case Some(attrValue) if attrValue == value =>
        variable match {
          case Some(varName) => Some(Map(varName -> attrValue))
          case None => Some(Map.empty)
        }
      case _ => None
    }
  }
}

// VariableCondition that binds a variable to an attribute value
case class VariableCondition(attribute: String, variable: String) extends Condition {
  override def evaluate(fact: Fact): Option[Map[String, Any]] = {
    fact.attributes.get(attribute) match {
      case Some(attrValue) => Some(Map(variable -> attrValue))
      case None => None
    }
  }
}

// CompositeCondition evaluates multiple conditions on the same fact
case class CompositeCondition(conditions: List[Condition]) extends Condition {
  override def evaluate(fact: Fact): Option[Map[String, Any]] = {
    var allBindings = Map[String, Any]()
    for (condition <- conditions) {
      condition.evaluate(fact) match {
        case Some(bindings) =>
          // Check for conflicting bindings
          val conflicts = allBindings.keySet.intersect(bindings.keySet).filter { key =>
            allBindings(key) != bindings(key)
          }
          if (conflicts.nonEmpty) return None
          allBindings ++= bindings
        case None => return None
      }
    }
    Some(allBindings)
  }
}

// Token represents partial matches (bindings)
case class Token(facts: List[Fact], bindings: Map[String, Any])

// AlphaNode processes conditions on facts
class AlphaNode(condition: Condition) {
  private var leftChildren: List[BetaNode] = List()
  private var rightChildren: List[BetaNode] = List()
  private var productionNodes: List[ProductionNode] = List()

  def activate(fact: Fact): Unit = {
    condition.evaluate(fact) match {
      case Some(bindings) =>
        val token = Token(List(fact), bindings)
        // Propagate to beta nodes via left and right inputs
        leftChildren.foreach(_.leftActivate(token))
        rightChildren.foreach(_.rightActivate(token))
        // If connected directly to a production node (single-condition rule)
        productionNodes.foreach(_.activate(token))
      case None => // Do nothing
    }
  }

  def addLeftChild(betaNode: BetaNode): Unit = {
    leftChildren ::= betaNode
  }

  def addRightChild(betaNode: BetaNode): Unit = {
    rightChildren ::= betaNode
  }

  def addProductionNode(prodNode: ProductionNode): Unit = {
    productionNodes ::= prodNode
  }
}

// BetaNode joins tokens from left and right inputs
// BetaNode joins tokens from left and right inputs
class BetaNode {
  private var leftTokens: List[Token] = List()
  private var rightTokens: List[Token] = List()
  private var leftChildren: List[BetaNode] = List()
  private var rightChildren: List[BetaNode] = List()
  private var productionNodes: List[ProductionNode] = List()

  def leftActivate(token: Token): Unit = {
    leftTokens ::= token
    joinTokens(token, rightTokens)
  }

  def rightActivate(token: Token): Unit = {
    rightTokens ::= token
    joinTokens(token, leftTokens)
  }

  private def joinTokens(newToken: Token, existingTokens: List[Token]): Unit = {
    existingTokens.foreach { token =>
      // Check for overlapping facts
      val factsOverlap = token.facts.exists(f => newToken.facts.contains(f))

      if (!factsOverlap && variableBindingsCompatible(token.bindings, newToken.bindings)) {
        // Merge bindings
        val combinedBindings = token.bindings ++ newToken.bindings
        val allFacts = token.facts ++ newToken.facts
        val combinedToken = Token(allFacts, combinedBindings)
        // Propagate to child beta nodes
        leftChildren.foreach(_.leftActivate(combinedToken))
        rightChildren.foreach(_.rightActivate(combinedToken))
        // Activate production nodes if any
        productionNodes.foreach(_.activate(combinedToken))
      }
    }
  }

  // Adjusted function to check variable compatibility
  private def variableBindingsCompatible(bindings1: Map[String, Any], bindings2: Map[String, Any]): Boolean = {
    val commonKeys = bindings1.keySet intersect bindings2.keySet
    commonKeys.forall { key =>
      bindings1(key) == bindings2(key)
    }
  }

  def addLeftChild(betaNode: BetaNode): Unit = {
    leftChildren ::= betaNode
  }

  def addRightChild(betaNode: BetaNode): Unit = {
    rightChildren ::= betaNode
  }

  def addProductionNode(prodNode: ProductionNode): Unit = {
    productionNodes ::= prodNode
  }
}


// ProductionNode represents the action to be taken when a rule fires
class ProductionNode(action: Token => Unit) {
  def activate(token: Token): Unit = {
    action(token)
  }
}

// RETEEngine manages the network and facts
class RETEEngine {
  private var alphaNodesMap: Map[Condition, AlphaNode] = Map()

  // Add a rule to the network
  def addRule(conditions: List[Condition], action: Token => Unit): Unit = {
    if (conditions.isEmpty) return

    // Create or reuse alpha nodes for each condition
    val alphaNodes = conditions.map { condition =>
      alphaNodesMap.getOrElse(condition, {
        val node = new AlphaNode(condition)
        alphaNodesMap += condition -> node
        node
      })
    }

    // Build the network of beta nodes
    var previousNodes: List[Either[AlphaNode, BetaNode]] = alphaNodes.map(Left(_))

    while (previousNodes.length > 1) {
      val newPreviousNodes = previousNodes.grouped(2).map {
        case List(node1, node2) =>
          val betaNode = new BetaNode()
          node1 match {
            case Left(alpha)  => alpha.addLeftChild(betaNode)
            case Right(beta)  => beta.addLeftChild(betaNode)
          }
          node2 match {
            case Left(alpha)  => alpha.addRightChild(betaNode) // Use of addRightChild in BetaNode
            case Right(beta)  => beta.addRightChild(betaNode)  // Use of addRightChild in BetaNode
          }
          Right(betaNode)
        case List(singleNode) => singleNode
      }.toList
      previousNodes = newPreviousNodes
    }

    // Create production node
    val prodNode = new ProductionNode(action)

    previousNodes.head match {
      case Left(node)  => node.addProductionNode(prodNode)
      case Right(beta) => beta.addProductionNode(prodNode)
    }
  }

  // Assert a fact into the working memory
  def assertFact(fact: Fact): Unit = {
    alphaNodesMap.values.foreach(_.activate(fact))
  }
}
