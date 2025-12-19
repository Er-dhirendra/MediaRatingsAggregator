import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import mytest.{AddRating, Aggregates, ComputeAggregates, Rating, RatingAggregator, RatingMessage}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RatingAggregatorSpec extends AnyWordSpec
  with BeforeAndAfterAll
  with Matchers {

  // Initialize the Akka TestKit
  val testKit = ActorTestKit()

  // Clean up resources after all tests
  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  "RatingAggregator" should {

    "add ratings and compute aggregates correctly" in {
      // Create the RatingAggregator actor
      val ratingAggregator: ActorRef[RatingMessage] = testKit.spawn(RatingAggregator(), "ratingAggregator")

      // Test data
      val showName = "Show1"
      val ratings = List(
        Rating("viewer1", 4.0),
        Rating("viewer2", 3.0),
        Rating("viewer3", 5.0),
        Rating("viewer1", 4.5) 
      )

      // Add ratings
      ratings.foreach { rating =>
        ratingAggregator ! AddRating(showName, rating)
      }

      // Create a TestProbe to receive the aggregates
      val probe = testKit.createTestProbe[Option[Aggregates]]()

      // Send ComputeAggregates message
      ratingAggregator ! ComputeAggregates(showName, probe.ref)

      // Validate the aggregates
      val expectedAverage = (4.0 + 3.0 + 5.0 + 4.5) / 4
      val expectedReach = 3 // Unique viewers: viewer1, viewer2, viewer3
      val expectedEngagementScore = (expectedAverage * expectedReach) / 4

      val expectedAggregates = Some(Aggregates(expectedAverage, expectedReach, expectedEngagementScore))
      probe.expectMessage(expectedAggregates)
    }

    "return None for a show with no ratings" in {
      // Create the RatingAggregator actor
      val ratingAggregator: ActorRef[RatingMessage] = testKit.spawn(RatingAggregator(), "ratingAggregatorEmpty")

      // Create a TestProbe to receive the aggregates
      val probe = testKit.createTestProbe[Option[Aggregates]]()

      // Send ComputeAggregates message for a non-existent show
      ratingAggregator ! ComputeAggregates("NonExistentShow", probe.ref)

      // Validate that None is returned
      probe.expectMessage(None)
    }
  }
}
