package system

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import system.ast.SystemMessages.{AddActor, AddActorAlreadyExists, AddActorSuccess, AddActors, AddActorsResponse, GetActor, GetActorNotFound, GetActorResponse, GetActorSuccess, GetActors, GetActorsResponse, RemoveActor, RemoveActorNotFound, RemoveActorSuccess, RemoveActors, RemoveActorsResponse, SpawnTrackerRequest}

object SpawnTrackerActor {

  def apply(): Behavior[SpawnTrackerRequest] = {
    saveActors(Map.empty)
  }

  private def saveActors(actorsMap: Map[String, ActorRef[_]]): Behavior[SpawnTrackerRequest] =
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

        replyTo ! AddActorsResponse(successes.toList, failures.toList)
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
        replyTo ! RemoveActorsResponse(
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
        replyTo ! GetActorsResponse(
          found = result,
          notFound = notFound
        )
        Behaviors.same

    }
}
