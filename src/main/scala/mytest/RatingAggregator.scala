package mytest

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.concurrent.TrieMap

// Case class for Rating
case class Rating(viewerId: String, score: Double)

// Case class for Aggregate metrics
case class Aggregates(averageRating: Double, reach: Int, engagementScore: Double)

// Messages for the Actor
sealed trait RatingMessage
case class AddRating(showName: String, rating: Rating) extends RatingMessage
case class ComputeAggregates(showName: String, replyTo: ActorRef[Option[Aggregates]]) extends RatingMessage

// RatingAggregator Actor
class RatingAggregator(context: ActorContext[RatingMessage]) extends AbstractBehavior[RatingMessage](context) {
  // Store ratings per show using an immutable data structure
  private val ratingsByShow: TrieMap[String, Vector[Rating]] = TrieMap.empty[String, Vector[Rating]]

  override def onMessage(msg: RatingMessage): Behavior[RatingMessage] = msg match {
    case AddRating(showName, rating) =>
      ratingsByShow.updateWith(showName) {
        case Some(existingRatings) => Some(existingRatings :+ rating)
        case None => Some(Vector(rating))
      }
      Behaviors.same

    case ComputeAggregates(showName, replyTo) =>
      replyTo ! computeAggregates(showName)
      Behaviors.same
  }

  private def computeAggregates(showName: String): Option[Aggregates] = {
    ratingsByShow.get(showName).map { ratings =>
      val totalScore = ratings.map(_.score).sum
      val uniqueViewers = ratings.map(_.viewerId).distinct.size
      val numberOfRatings = ratings.size

      if (numberOfRatings > 0) {
        val averageRating = totalScore / numberOfRatings
        val engagementScore = (averageRating * uniqueViewers) / numberOfRatings
        Aggregates(averageRating, uniqueViewers, engagementScore)
      } else {
        Aggregates(0.0, 0, 0.0)
      }
    }
  }
  
}

object RatingAggregator {
  def apply(): Behavior[RatingMessage] = Behaviors.setup(context => new RatingAggregator(context))
}
