package system.ast

import akka.actor.typed.{ActorRef, Behavior}
import system.SpawnManagerFacade

object SystemMessages {

  trait Command

  sealed trait TrackerRequest extends Command

  final case class AddActor(
                             key: String,
                             actor: ActorRef[_],
                             replyTo: ActorRef[AddActorTrackerResponse]
                           ) extends TrackerRequest

  final case class AddActors(
                              actors: Map[String, ActorRef[_]],
                              replyTo: ActorRef[AddActorsTrackerResponse]
                            ) extends TrackerRequest

  final case class RemoveActor(
                                key: String,
                                replyTo: ActorRef[RemoveActorTrackerResponse]
                              ) extends TrackerRequest

  final case class RemoveActors(
                                 key: Set[String],
                                 replyTo: ActorRef[RemoveActorsTrackerResponse]
                               ) extends TrackerRequest

  final case class GetActor(
                             key: String,
                             replyTo: ActorRef[GetActorTrackerResponse]
                           ) extends TrackerRequest

  final case class GetActors(
                                 key: Set[String],
                                 replyTo: ActorRef[GetActorsTrackerResponse]
                               ) extends TrackerRequest

  sealed trait TrackerResponse extends Command

  sealed trait AddActorTrackerResponse extends TrackerResponse
  case object AddActorSuccess extends AddActorTrackerResponse
  case object AddActorAlreadyExists extends AddActorTrackerResponse

  sealed trait RemoveActorTrackerResponse extends TrackerResponse
  case object RemoveActorSuccess extends RemoveActorTrackerResponse
  case object RemoveActorNotFound extends RemoveActorTrackerResponse

  sealed trait GetActorTrackerResponse extends TrackerResponse
  final case class GetActorSuccess(result: ActorRef[_]) extends GetActorTrackerResponse
  final case class GetActorNotFound() extends GetActorTrackerResponse

  final case class AddActorsTrackerResponse(
                                      successes: Seq[String],
                                      failures: Seq[String]
                                    ) extends TrackerResponse

  final case class RemoveActorsTrackerResponse(
                                      successes: Seq[String],
                                      failures: Seq[String]
                                    ) extends TrackerResponse

  final case class GetActorsTrackerResponse(
                                      found: Map[String, ActorRef[_]],
                                      notFound: Set[String]
                                    ) extends TrackerResponse

  sealed trait Manager extends Command

  sealed trait ManagerRequest extends Manager

  final case class RegisterSingle(
                                   key: String,
                                   behavior: Behavior[_],
                                   replyTo: ActorRef[ManagerResponse]
                                 ) extends ManagerRequest

  final case class RegisterMultiple(
                                     behaviors: Map[String, Behavior[Any]],
                                     replyTo: ActorRef[ManagerResponse]
                                   ) extends ManagerRequest

  final case class LookupActor(
                                key: String,
                                replyTo: ActorRef[ManagerResponse]
                              ) extends ManagerRequest

  final case class LookupActors(
                                 keys: Set[String],
                                 replyTo: ActorRef[ManagerResponse]
                               ) extends ManagerRequest

  final case class DeregisterSingle(
                                     key: String,
                                     replyTo: ActorRef[ManagerResponse]
                                   ) extends ManagerRequest

  final case class DeregisterMultiple(
                                       keys: Set[String],
                                       replyTo: ActorRef[ManagerResponse]
                                     ) extends ManagerRequest

  sealed trait ManagerResponse extends Manager
  final case class Success(message: String) extends ManagerResponse
  final case class Failure(message: String) extends ManagerResponse
  final case class FoundActor(ref: ActorRef[_]) extends ManagerResponse
  final case class FoundActors(refs: Map[String, ActorRef[_]]) extends ManagerResponse

  final class ActorContextPair(
                                     managerRef: ActorRef[Manager],
                                     facade: SpawnManagerFacade
                                   )
}
