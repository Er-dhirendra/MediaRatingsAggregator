package mytest

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Guardian {

  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      // child: RatingAggregator
      val ratingAggregator =
        context.spawn(RatingAggregator(), "rating-aggregator")

      // child: ReplyActor
      val replyActor =
        context.spawn(ReplyActor(), "reply-actor")

      // child: DummySender that talks to RatingAggregator
      val dummySender =
        context.spawn(DummySender(ratingAggregator, replyActor), "dummy-sender")

      // kick off dummy sender
      dummySender ! DummySender.Start

      Behaviors.empty
    }
  }
