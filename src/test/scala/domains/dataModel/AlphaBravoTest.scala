package domains.dataModel

import domains.alpha._
import domains.bravo._
import draco._
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.BeforeAndAfterAll

class AlphaBravoTest extends AnyFunSuite with BeforeAndAfterAll {
  lazy val testKit: ActorTestKit = ActorTestKit()

  override def afterAll(): Unit = testKit.shutdownTestKit()

  test("Alpha to DataModel to Bravo assembly") {
    val bravoProbe = testKit.createTestProbe[Bravo]()

    val bravoActor = testKit.spawn(
      BravoActor.actorWithProbe(bravoProbe.ref).asInstanceOf[Actor[Bravo]],
      "bravoActor"
    )

    val dataModelActor = testKit.spawn(
      DataModelActor.actorWithSession(bravoActor).asInstanceOf[Actor[DataModel]],
      "dataModelActor"
    )

    // Send AlphaOne and AlphaTwo to the DataModel actor
    // AlphaOne extends Alpha with PartOne, AlphaTwo extends Alpha with PartTwo
    // The assembly rule matches on PartOne + PartTwo and produces BravoResult
    dataModelActor ! Alpha.one(42)
    dataModelActor ! Alpha.two("hello")

    val result = bravoProbe.receiveMessage()
    assert(result.isInstanceOf[BravoResult])
    val assembled = result.asInstanceOf[Assembled]
    assert(assembled.number == 42)
    assert(assembled.text == "hello")
    println(s"Assembly verified: number=${assembled.number}, text=${assembled.text}")
  }
}
