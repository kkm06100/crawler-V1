package system.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import system.ast.SystemMessages._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.Try

object SpawnManager {

  // 내부 핸들링용
  private sealed trait InternalCommand extends Command
  private final case class HandleAddActorResponse(response: Try[AddActorResponse], replyTo: ActorRef[ManagerResponse]) extends InternalCommand
  private final case class HandleAddActorsResponse(response: Try[AddActorsResponse], replyTo: ActorRef[ManagerResponse]) extends InternalCommand
  private final case class HandleGetActorResponse(response: Try[GetActorResponse], replyTo: ActorRef[ManagerResponse]) extends InternalCommand
  private final case class HandleGetActorsResponse(response: Try[GetActorsResponse], replyTo: ActorRef[ManagerResponse]) extends InternalCommand
  private final case class HandleRemoveActorResponse(response: Try[RemoveActorResponse], replyTo: ActorRef[ManagerResponse]) extends InternalCommand
  private final case class HandleRemoveActorsResponse(response: Try[RemoveActorsResponse], replyTo: ActorRef[ManagerResponse]) extends InternalCommand

  // 외부에서 사용할 메시지
  final case class CreateActor[T](
                                   key: String,
                                   behavior: Behavior[T],
                                   nameHint: String,
                                   replyTo: ActorRef[ManagerResponse]
                                 ) extends Command

  def apply(spawnTracker: ActorRef[SpawnRequest]): Behavior[Command] = Behaviors.setup { context =>
    implicit val timeout: Timeout = 3.seconds

    Behaviors.receiveMessage {

      // 1. Spawn & 등록
      case CreateActor(key, behavior, nameHint, replyTo) =>
        val name = s"${nameHint}_$key"
        val actorRef = context.spawn(behavior, name)

        context.ask(spawnTracker, (ref: ActorRef[AddActorResponse]) => AddActor(key, actorRef, ref)) {
          case scala.util.Success(AddActorSuccess) =>
            HandleAddActorResponse(scala.util.Success(AddActorSuccess), replyTo)
          case scala.util.Success(AddActorAlreadyExists) =>
            HandleAddActorResponse(scala.util.Success(AddActorAlreadyExists), replyTo)
          case scala.util.Failure(ex) =>
            HandleAddActorResponse(scala.util.Failure(ex), replyTo)
        }

        Behaviors.same

      case HandleAddActorResponse(response, replyTo) =>
        response match {
          case scala.util.Success(AddActorSuccess) =>
            replyTo ! Success("생성 및 등록 완료")
          case scala.util.Success(AddActorAlreadyExists) =>
            replyTo ! Failure("이미 존재하는 키입니다")
          case scala.util.Failure(ex) =>
            replyTo ! Failure(s"예외 발생: ${ex.getMessage}")
        }
        Behaviors.same

      // 2. 기존 actor 등록
      case RegisterSingle(key, actor, replyTo) =>
        context.ask(spawnTracker, (ref: ActorRef[AddActorResponse]) => AddActor(key, actor, ref)) {
          HandleAddActorResponse(_, replyTo)
        }
        Behaviors.same

      case RegisterMultiple(actors, replyTo) =>
        context.ask(spawnTracker, (ref: ActorRef[AddActorsResponse]) => AddActors(actors, ref)) {
          HandleAddActorsResponse(_, replyTo)
        }
        Behaviors.same

      case HandleAddActorsResponse(response, replyTo) =>
        response match {
          case scala.util.Success(AddActorsResponse(successes, failures)) =>
            if (failures.nonEmpty)
              replyTo ! Failure(s"일부 실패 - 성공: ${successes.mkString(", ")}, 실패: ${failures.mkString(", ")}")
            else
              replyTo ! Success(s"모두 등록 성공: ${successes.mkString(", ")}")
          case scala.util.Failure(ex) =>
            replyTo ! Failure(s"예외 발생: ${ex.getMessage}")
        }
        Behaviors.same

      // 3. 조회
      case LookupActor(key, replyTo) =>
        context.ask(spawnTracker, (ref: ActorRef[GetActorResponse]) => GetActor(key, ref)) {
          HandleGetActorResponse(_, replyTo)
        }
        Behaviors.same

      case HandleGetActorResponse(response, replyTo) =>
        response match {
          case scala.util.Success(GetActorSuccess(actorRef)) =>
            replyTo ! FoundActor(actorRef)
          case scala.util.Success(GetActorNotFound()) =>
            replyTo ! Failure(s"찾을 수 없음")
          case scala.util.Failure(ex) =>
            replyTo ! Failure(s"예외 발생: ${ex.getMessage}")
        }
        Behaviors.same

      case LookupActors(keys, replyTo) =>
        context.ask(spawnTracker, (ref: ActorRef[GetActorsResponse]) => GetActors(keys, ref)) {
          HandleGetActorsResponse(_, replyTo)
        }
        Behaviors.same

      case HandleGetActorsResponse(response, replyTo) =>
        response match {
          case scala.util.Success(GetActorsResponse(found, notFound)) =>
            if (notFound.nonEmpty)
              replyTo ! Failure(s"일부 누락 - 발견: ${found.keySet.mkString(", ")}, 없음: ${notFound.mkString(", ")}")
            else
              replyTo ! FoundActors(found)
          case scala.util.Failure(ex) =>
            replyTo ! Failure(s"예외 발생: ${ex.getMessage}")
        }
        Behaviors.same

      // 4. 삭제
      case DeregisterSingle(key, replyTo) =>
        context.ask(spawnTracker, (ref: ActorRef[RemoveActorResponse]) => RemoveActor(key, ref)) {
          HandleRemoveActorResponse(_, replyTo)
        }
        Behaviors.same

      case HandleRemoveActorResponse(response, replyTo) =>
        response match {
          case scala.util.Success(RemoveActorSuccess) =>
            replyTo ! Success(s"삭제 완료")
          case scala.util.Success(RemoveActorNotFound) =>
            replyTo ! Failure(s"없음")
          case scala.util.Failure(ex) =>
            replyTo ! Failure(s"예외 발생: ${ex.getMessage}")
        }
        Behaviors.same

      case DeregisterMultiple(keys, replyTo) =>
        context.ask(spawnTracker, (ref: ActorRef[RemoveActorsResponse]) => RemoveActors(keys, ref)) {
          HandleRemoveActorsResponse(_, replyTo)
        }
        Behaviors.same

      case HandleRemoveActorsResponse(response, replyTo) =>
        response match {
          case scala.util.Success(RemoveActorsResponse(successes, failures)) =>
            if (failures.nonEmpty)
              replyTo ! Failure(s"일부 실패 - 삭제: ${successes.mkString(", ")}, 없음: ${failures.mkString(", ")}")
            else
              replyTo ! Success(s"모두 삭제 성공: ${successes.mkString(", ")}")
          case scala.util.Failure(ex) =>
            replyTo ! Failure(s"예외 발생: ${ex.getMessage}")
        }
        Behaviors.same
    }
  }
}
