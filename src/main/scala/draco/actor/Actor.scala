package draco.actor

trait Actor {}

object Actor extends App {
  def apply () : Actor = new Actor {}
}