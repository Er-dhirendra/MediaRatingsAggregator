package mytest
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors

object RatingAggregatorApp extends App {
val system: ActorSystem[Nothing] = ActorSystem[Nothing](Guardian(), "RatingAggregatorSystem")
}
