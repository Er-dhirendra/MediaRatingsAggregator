package mytest

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object DummySender {

  // message protocol for dummy actor (only internal use)
  sealed trait Command
  case object Start extends Command

  def apply(ratingAggregator: ActorRef[RatingMessage],
            replyTo: ActorRef[Option[Aggregates]]): Behavior[Command] =
    Behaviors.receive { (_, msg) =>
      msg match {
        case Start =>
          val ratings = Seq(
            Rating("Viewer1", 8.0),
            Rating("Viewer2", 9.0),
            Rating("Viewer1", 7.0),
            Rating("Viewer3", 10.0)
          )

          ratings.foreach { rating =>
            ratingAggregator ! AddRating("The Nielsen Interview", rating)
            ratingAggregator ! AddRating("The Nielsen", rating.copy(score = rating.score + 1.0))
          }

          ratingAggregator ! ComputeAggregates("The Nielsen Interview", replyTo)
          ratingAggregator ! ComputeAggregates("The Nielsen", replyTo)

          Behaviors.same
      }
    }
}
