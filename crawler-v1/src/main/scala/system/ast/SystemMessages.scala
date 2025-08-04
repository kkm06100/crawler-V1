package system.ast

import akka.actor.typed.ActorRef

object SystemMessages {

  sealed trait SpawnTrackerRequest

  final case class AddActor(
                             key: String,
                             actor: ActorRef[_],
                             replyTo: ActorRef[AddActorResponse]
                           ) extends SpawnTrackerRequest

  final case class AddActors(
                              actors: Map[String, ActorRef[_]],
                              replyTo: ActorRef[AddActorsResponse]
                            ) extends SpawnTrackerRequest

  final case class RemoveActor(
                                key: String,
                                replyTo: ActorRef[RemoveActorResponse]
                              ) extends SpawnTrackerRequest

  final case class RemoveActors(
                                 key: Set[String],
                                 replyTo: ActorRef[RemoveActorsResponse]
                               ) extends SpawnTrackerRequest

  final case class GetActor(
                             key: String,
                             replyTo: ActorRef[GetActorResponse]
                           ) extends SpawnTrackerRequest

  final case class GetActors(
                                 key: Set[String],
                                 replyTo: ActorRef[GetActorsResponse]
                               ) extends SpawnTrackerRequest

  sealed trait SpawnTrackerResponse

  sealed trait AddActorResponse extends SpawnTrackerResponse
  case object AddActorSuccess extends AddActorResponse
  case object AddActorAlreadyExists extends AddActorResponse

  sealed trait RemoveActorResponse extends SpawnTrackerResponse
  case object RemoveActorSuccess extends RemoveActorResponse
  case object RemoveActorNotFound extends RemoveActorResponse

  sealed trait GetActorResponse extends SpawnTrackerResponse
  final case class GetActorSuccess(result: ActorRef[_]) extends GetActorResponse
  final case class GetActorNotFound() extends GetActorResponse

  final case class AddActorsResponse(
                                      successes: Seq[String],
                                      failures: Seq[String]
                                    ) extends SpawnTrackerResponse

  final case class RemoveActorsResponse(
                                      successes: Seq[String],
                                      failures: Seq[String]
                                    ) extends SpawnTrackerResponse

  final case class GetActorsResponse(
                                      found: Map[String, ActorRef[_]],
                                      notFound: Set[String]
                                    ) extends SpawnTrackerResponse
}
