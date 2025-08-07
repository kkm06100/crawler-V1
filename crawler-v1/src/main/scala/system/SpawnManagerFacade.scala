package system

import akka.actor.typed.ActorRef
import akka.util.Timeout
import system.ast.SystemMessages._
import akka.actor.typed.Scheduler
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.Askable

import scala.concurrent.duration.DurationInt
import scala.concurrent.Future

class SpawnManagerFacade(
                          spawnManager: ActorRef[Command]
                        )(implicit system: ActorSystem[_]) {

  // 기본 설정
  private implicit val timeout: Timeout = Timeout(3.seconds)
  private implicit val scheduler: Scheduler = system.scheduler

  def registerSingle[T](
                         key: String,
                         behavior: akka.actor.typed.Behavior[T]
                       ): Future[ManagerResponse] = {
    spawnManager.ask(ref => RegisterSingle(key, behavior, ref))(timeout, scheduler)
  }

  def registerMultiple[T](
                           behaviors: Map[String, akka.actor.typed.Behavior[T]]
                         ): Future[ManagerResponse] = {
    // Type erasure로 인해 Map[String, Behavior[Any]]로 캐스팅
    val anyBehaviors: Map[String, akka.actor.typed.Behavior[Any]] =
      behaviors.asInstanceOf[Map[String, akka.actor.typed.Behavior[Any]]]

    spawnManager.ask(ref => RegisterMultiple(anyBehaviors, ref))(timeout, scheduler)
  }

  def lookupActor(key: String): Future[ManagerResponse] = {
    spawnManager.ask(ref => LookupActor(key, ref))(timeout, scheduler)
  }

  def lookupActors(keys: Seq[String]): Future[ManagerResponse] = {
    spawnManager.ask(ref => LookupActors(keys.toSet, ref))(timeout, scheduler)
  }

  def deregisterSingle(key: String): Future[ManagerResponse] = {
    spawnManager.ask(ref => DeregisterSingle(key, ref))(timeout, scheduler)
  }

  def deregisterMultiple(keys: Seq[String]): Future[ManagerResponse] = {
    spawnManager.ask(ref => DeregisterMultiple(keys.toSet, ref))(timeout, scheduler)
  }
}