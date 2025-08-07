package system.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import system.ast.SystemMessages._
import akka.util.Timeout
import akka.actor.typed.scaladsl.AskPattern._

import scala.concurrent.duration._
import scala.util.{Try, Failure => ScalaFailure, Success => ScalaSuccess}
import akka.actor.typed.Scheduler

object SpawnManager {

  private sealed trait InternalCommand extends Command
  private final case class WrappedResponse(replyTo: ActorRef[ManagerResponse], result: Try[TrackerResponse]) extends InternalCommand
  private final case class MultipleAddActorsResult[T](
                                                       replyTo: ActorRef[ManagerResponse],
                                                       behaviors: Map[String, Behavior[T]],
                                                       successes: Seq[String],
                                                       failures: Seq[String]
                                                     ) extends InternalCommand

  def apply(spawnTracker: ActorRef[TrackerRequest]): Behavior[Command] = Behaviors.setup { context =>
    implicit val timeout: Timeout = 3.seconds
    implicit val scheduler: Scheduler = context.system.scheduler

    Behaviors.receiveMessage {

      case RegisterSingle(key, behavior, replyTo) =>
        val name = key
        val actorRef = context.spawn(behavior, name)

        context.ask(spawnTracker, AddActor(key, actorRef, _)) {
          case ScalaSuccess(r) => WrappedResponse(replyTo, ScalaSuccess(r))
          case ScalaFailure(e) => WrappedResponse(replyTo, ScalaFailure(e))
        }
        Behaviors.same

      case RegisterMultiple(behaviors, replyTo) =>
        val placeholderRefs = behaviors.view.mapValues(_ => context.system.ignoreRef).toMap

        context.ask(spawnTracker, AddActors(placeholderRefs, _)) {
          case ScalaSuccess(AddActorsTrackerResponse(successes, failures)) =>
            MultipleAddActorsResult(replyTo, behaviors, successes, failures)
          case ScalaSuccess(other) =>
            WrappedResponse(replyTo, ScalaSuccess(other))
          case ScalaFailure(ex) =>
            WrappedResponse(replyTo, ScalaFailure(ex))
        }
        Behaviors.same

      case MultipleAddActorsResult(replyTo, behaviors, successes, failures) =>
        if (failures.nonEmpty) {
          replyTo ! Failure(s"일부 실패: ${failures.mkString(", ")}")
          Behaviors.same
        } else {
          // 성공한 키들만 실제 액터 스폰
          val actualSpawned = successes.map { key =>
            val behavior = behaviors(key)
            val ref = context.spawn(behavior, key)
            key -> ref
          }.toMap

          context.pipeToSelf(spawnTracker.ask[AddActorsTrackerResponse](ref => AddActors(actualSpawned, ref))) {
            case ScalaSuccess(r) => WrappedResponse(replyTo, ScalaSuccess(r))
            case ScalaFailure(e) => WrappedResponse(replyTo, ScalaFailure(e))
          }
          Behaviors.same
        }

      case LookupActor(key, replyTo) =>
        context.ask(spawnTracker, GetActor(key, _)) {
          case ScalaSuccess(r) => WrappedResponse(replyTo, ScalaSuccess(r))
          case ScalaFailure(e) => WrappedResponse(replyTo, ScalaFailure(e))
        }
        Behaviors.same

      case LookupActors(keys, replyTo) =>
        context.ask(spawnTracker, GetActors(keys, _)) {
          case ScalaSuccess(r) => WrappedResponse(replyTo, ScalaSuccess(r))
          case ScalaFailure(e) => WrappedResponse(replyTo, ScalaFailure(e))
        }
        Behaviors.same

      case DeregisterSingle(key, replyTo) =>
        context.ask(spawnTracker, RemoveActor(key, _)) {
          case ScalaSuccess(r) => WrappedResponse(replyTo, ScalaSuccess(r))
          case ScalaFailure(e) => WrappedResponse(replyTo, ScalaFailure(e))
        }
        Behaviors.same

      case DeregisterMultiple(keys, replyTo) =>
        context.ask(spawnTracker, RemoveActors(keys, _)) {
          case ScalaSuccess(r) => WrappedResponse(replyTo, ScalaSuccess(r))
          case ScalaFailure(e) => WrappedResponse(replyTo, ScalaFailure(e))
        }
        Behaviors.same

      case WrappedResponse(replyTo, ScalaSuccess(response)) =>
        response match {
          case AddActorSuccess           => replyTo ! Success("등록 성공")
          case AddActorAlreadyExists     => replyTo ! Failure("이미 존재")

          case AddActorsTrackerResponse(ok, fail) =>
            replyTo ! (if (fail.nonEmpty) Failure(s"일부 실패: ${fail.mkString(", ")}") else Success(s"등록 성공: ${ok.mkString(", ")}"))

          case GetActorSuccess(ref)        => replyTo ! FoundActor(ref)
          case GetActorNotFound()          => replyTo ! Failure("찾을 수 없음")

          case GetActorsTrackerResponse(found, nf) =>
            replyTo ! (if (nf.nonEmpty) Failure(s"일부 없음: ${nf.mkString(", ")}") else FoundActors(found))

          case RemoveActorSuccess           => replyTo ! Success("삭제 완료")
          case RemoveActorNotFound          => replyTo ! Failure("없음")

          case RemoveActorsTrackerResponse(ok, fail) =>
            replyTo ! (if (fail.nonEmpty) Failure(s"일부 실패: ${fail.mkString(", ")}") else Success(s"삭제 성공: ${ok.mkString(", ")}"))
        }
        Behaviors.same

      case WrappedResponse(replyTo, ScalaFailure(ex)) =>
        replyTo ! Failure(s"예외 발생: ${ex.getMessage}")
        Behaviors.same
    }
  }
}