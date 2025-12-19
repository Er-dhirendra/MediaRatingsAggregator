# Media Ratings Aggregator

A scalable Akka Typed actor system for collecting TV show ratings and computing engagement metrics.

## Architecture Overview

```
Guardian
├── RatingAggregator (stores ratings per show)
├── ReplyActor (prints aggregates)
└── DummySender (sends test ratings + triggers computation)
```

## Key Components

### Domain Models
- **`Rating`**: `viewerId: String`, `score: Double`
- **`Aggregates`**: `averageRating`, `reach` (unique viewers), `engagementScore = (avg * reach) / ratingsCount`

### Actors

| Actor | Purpose | Protocol |
|-------|---------|----------|
| `Guardian` | System root, spawns children | `Nothing` |
| `RatingAggregator` | Stores/computes show ratings | `RatingMessage` |
| `DummySender` | Sends test ratings | `Command` |
| `ReplyActor` | Prints aggregate results | `Option[Aggregates]` |

## Messages

```scala
sealed trait RatingMessage
case class AddRating(showName: String, rating: Rating) extends RatingMessage
case class ComputeAggregates(showName: String, replyTo: ActorRef[Option[Aggregates]]) extends RatingMessage
```

## Features

- **Immutable State**: `TrieMap[String, Vector[Rating]]` for thread-safe concurrent access
- **Efficient Updates**: `updateWith` for O(1) appends
- **Real-time Computation**: Aggregates computed on-demand
- **Unique Viewer Tracking**: `distinct` deduplication
- **Supervisor Hierarchy**: Clean child actor lifecycle

## Running the Application

```bash
sbt run
```

**Expected Output**:
```
Average Rating: 8.5
Reach: 3
Engagement Score: 7.58333

Average Rating: 9.5
Reach: 3
Engagement Score: 8.45833
```

**Test Data**:
- "The Nielsen Interview": [8.0, 9.0, 7.0, 10.0] → avg=8.5, reach=3
- "The Nielsen": [9.0, 10.0, 8.0, 11.0] → avg=9.5, reach=3

## Test Coverage

```bash
sbt test
```

- Single/multi-show rating aggregation
- Unique viewer counting
- Empty show handling
- Async message processing

## Project Structure

```
├── RatingAggregator.scala     # Core actor logic
├── Guardian.scala            # System root
├── DummySender.scala         # Test data generator
├── ReplyActor.scala          # Result printer
├── RatingAggregatorApp.scala # Main entry point
└── RatingAggregatorSpec.scala # Unit tests
```

## Scaling Considerations

- **Sharding**: Spawn per-show aggregators for high volume
- **Persistence**: Add Akka Persistence for durability
- **Clustering**: Akka Cluster Sharding for distribution
- **Streaming**: Akka Streams for real-time analytics

## Build

```bash
sbt compile test run
```

**Dependencies**:
```
"com.typesafe.akka" %% "akka-actor-typed" % "2.8.5"
"com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.8.5" % Test
"org.scalatest" %% "scalatest" % "3.2.18" % Test
```

## Akka Typed Best Practices Applied

✅ Single responsibility per actor  
✅ Immutable message protocol  
✅ Type-safe ActorRef passing  
✅ Proper resource cleanup  
✅ TestKit integration  
✅ Concurrent collection usage  

***

*Built with Scala 3 + Akka Typed 2.8*
