package system.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import system.ast.SystemMessages._

/**
 * 직접 호출하지 말 것
 */
object SpawnTracker {

  def apply(): Behavior[TrackerRequest] = {
    saveActors(Map.empty)
  }

  private def saveActors(actorsMap: Map[String, ActorRef[_]]): Behavior[TrackerRequest] =
    Behaviors.receiveMessage {
      case AddActor(key, actor, replyTo) =>
        if (actorsMap.contains(key)) {
          replyTo ! AddActorAlreadyExists
          Behaviors.same
        } else {
          val updated = actorsMap + (key -> actor)
          replyTo ! AddActorSuccess
          saveActors(updated)
        }
      case AddActors(actorPairs, replyTo) =>
        var updatedMap = actorsMap
        val successes = scala.collection.mutable.ListBuffer[String]()
        val failures = scala.collection.mutable.ListBuffer[String]()

        actorPairs.foreach { case (key, ref) =>
          if (updatedMap.contains(key)) {
            failures += key
          } else {
            updatedMap += (key -> ref)
            successes += key
          }
        }

        replyTo ! AddActorsTrackerResponse(successes.toList, failures.toList)
        saveActors(updatedMap)

      case RemoveActor(key, replyTo) =>
        if (actorsMap.contains(key)) {
          val updated = actorsMap - key
          replyTo ! RemoveActorSuccess
          saveActors(updated)
        } else {
          replyTo ! RemoveActorNotFound
          Behaviors.same
        }

      case RemoveActors(keys, replyTo) =>
        val (found, notFound) = keys.partition(actorsMap.contains)

        val updated = actorsMap -- found
        replyTo ! RemoveActorsTrackerResponse(
          successes = found.toSeq,
          failures = notFound.toSeq
        )
        saveActors(updated)

      case GetActor(key, replyTo) =>
        val result = actorsMap.get(key)
        result match {
          case Some(value) =>
            replyTo ! GetActorSuccess(value)
          case None =>
            replyTo ! GetActorNotFound()
        }
        Behaviors.same

      case GetActors(keys, replyTo) =>
        val (found, notFound) = keys.partition(actorsMap.contains)

        val result = found.map(k => k -> actorsMap(k)).toMap
        replyTo ! GetActorsTrackerResponse(
          found = result,
          notFound = notFound
        )
        Behaviors.same
    }
}
