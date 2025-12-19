package mytest
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors

object RatingAggregatorApp extends App {
  implicit val system: ActorSystem[RatingMessage] = ActorSystem(RatingAggregator(), "RatingAggregatorSystem")

  // Adding ratings
  val ratings = Seq(
    Rating("Viewer1", 8.0),
    Rating("Viewer2", 9.0),
    Rating("Viewer1", 7.0), // Same viewer
    Rating("Viewer3", 10.0)
  )

  val ratingAggregator: ActorRef[RatingMessage] = system

  ratings.foreach { rating =>
    ratingAggregator ! AddRating("The Nielsen Interview", rating)
    ratingAggregator ! AddRating("The Nielsen", rating.copy(score = rating.score + 1.0))
  }

  // Compute aggregates
  val replyTo = ActorSystem(Behaviors.receiveMessage[Option[Aggregates]] { aggregates =>
    aggregates match {
      case Some(agg) =>
        println(s"Average Rating: ${agg.averageRating}")
        println(s"Reach: ${agg.reach}")
        println(s"Engagement Score: ${agg.engagementScore}")
      case None =>
        println("No ratings available for this show.")
    }
    Behaviors.stopped
  }, "ReplyHandler")

  ratingAggregator ! ComputeAggregates("The Nielsen Interview", replyTo)
  ratingAggregator ! ComputeAggregates("The Nielsen", replyTo)

  // Shutdown the actor system
  system.terminate()
}
