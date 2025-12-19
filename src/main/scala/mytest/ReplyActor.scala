package mytest

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

object ReplyActor {
  def apply(): Behavior[Option[Aggregates]] =
    Behaviors.receiveMessage { aggregatesOpt =>
      aggregatesOpt match {
        case Some(agg) =>
          println(s"Average Rating: ${agg.averageRating}")
          println(s"Reach: ${agg.reach}")
          println(s"Engagement Score: ${agg.engagementScore}")
        case None =>
          println("No ratings available for this show.")
      }
      Behaviors.same
    }
}
