package system.ast

import akka.actor.typed.ActorRef

object SystemMessages {

  sealed trait SpawnRequest

  final case class AddActor(
                             key: String,
                             actor: ActorRef[_],
                             replyTo: ActorRef[AddActorResponse]
                           ) extends SpawnRequest

  final case class AddActors(
                              actors: Map[String, ActorRef[_]],
                              replyTo: ActorRef[AddActorsResponse]
                            ) extends SpawnRequest

  final case class RemoveActor(
                                key: String,
                                replyTo: ActorRef[RemoveActorResponse]
                              ) extends SpawnRequest

  final case class RemoveActors(
                                 key: Set[String],
                                 replyTo: ActorRef[RemoveActorsResponse]
                               ) extends SpawnRequest

  final case class GetActor(
                             key: String,
                             replyTo: ActorRef[GetActorResponse]
                           ) extends SpawnRequest

  final case class GetActors(
                                 key: Set[String],
                                 replyTo: ActorRef[GetActorsResponse]
                               ) extends SpawnRequest

  sealed trait SpawnResponse

  sealed trait AddActorResponse extends SpawnResponse
  case object AddActorSuccess extends AddActorResponse
  case object AddActorAlreadyExists extends AddActorResponse

  sealed trait RemoveActorResponse extends SpawnResponse
  case object RemoveActorSuccess extends RemoveActorResponse
  case object RemoveActorNotFound extends RemoveActorResponse

  sealed trait GetActorResponse extends SpawnResponse
  final case class GetActorSuccess(result: ActorRef[_]) extends GetActorResponse
  final case class GetActorNotFound() extends GetActorResponse

  final case class AddActorsResponse(
                                      successes: Seq[String],
                                      failures: Seq[String]
                                    ) extends SpawnResponse

  final case class RemoveActorsResponse(
                                      successes: Seq[String],
                                      failures: Seq[String]
                                    ) extends SpawnResponse

  final case class GetActorsResponse(
                                      found: Map[String, ActorRef[_]],
                                      notFound: Set[String]
                                    ) extends SpawnResponse

  trait Command

  final case class RegisterSingle(
                                   key: String,
                                   actor: ActorRef[_],
                                   replyTo: ActorRef[ManagerResponse]
                                 ) extends Command

  final case class RegisterMultiple(
                                     actors: Map[String, ActorRef[_]],
                                     replyTo: ActorRef[ManagerResponse]
                                   ) extends Command

  final case class LookupActor(
                                key: String,
                                replyTo: ActorRef[ManagerResponse]
                              ) extends Command

  final case class LookupActors(
                                 keys: Set[String],
                                 replyTo: ActorRef[ManagerResponse]
                               ) extends Command

  final case class DeregisterSingle(
                                     key: String,
                                     replyTo: ActorRef[ManagerResponse]
                                   ) extends Command

  final case class DeregisterMultiple(
                                       keys: Set[String],
                                       replyTo: ActorRef[ManagerResponse]
                                     ) extends Command

  sealed trait ManagerResponse extends Command
  final case class Success(message: String) extends ManagerResponse
  final case class Failure(message: String) extends ManagerResponse
  final case class FoundActor(ref: ActorRef[_]) extends ManagerResponse
  final case class FoundActors(refs: Map[String, ActorRef[_]]) extends ManagerResponse
}
