package domain.crawling.core

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import domain.{CrawlerActorContext, CrawlerActorRegistry}
import system.{ActorDefinition, ActorRegistry}

object ActorBootStrap {

  // 더미 spawnTracker actor 정의
  object SpawnTrackerActor {
    sealed trait Command
    case class Track(actorId: String) extends Command

    def apply(): Behavior[Command] = Behaviors.receive { (context, message) =>
      message match {
        case Track(id) =>
          context.log.info(s"[SpawnTracker] Tracking actor: $id")
          Behaviors.same
      }
    }
  }

  // 더미 도메인 actor 정의
  object DomainActor {
    sealed trait Command
    case object Start extends Command

    def apply(): Behavior[Command] = Behaviors.receive { (context, message) =>
      message match {
        case Start =>
          context.log.info(s"[DomainActor] Started!")
          Behaviors.same
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val system = ActorSystem[Nothing](Behaviors.setup[Nothing] { context =>

      // 1. SpawnTracker 생성
      val spawnTracker: ActorRef[SpawnTrackerActor.Command] =
        context.spawn(SpawnTrackerActor(), "spawn-tracker")

      // 2. DomainActor 생성
      val domainActor: ActorRef[DomainActor.Command] =
        context.spawn(DomainActor(), "domain-actor-1")

      // 3. Context 생성
      val actorContextId = "crawler-context-1"
      val actorContext = new CrawlerActorContext(actorContextId, spawnTracker)

      // 4. ActorDefinition 생성
      val actorDef = new ActorDefinition(
        "domain-actor-1",              // actorId
        domainActor,                   // actorRef
        spawnTracker.path.toString,   // spawnTrackerId
        "guardian-actor-id",          // guardianId
      )

      // 5. Registry 생성 및 등록
      val actorRegistry: ActorRegistry = new CrawlerActorRegistry()

      actorContext.register(actorDef)
      actorRegistry.registerContext(actorContext)

      // 6. 등록 정보 출력
      context.log.info(s"[BootStrap] SpawnTracker Path: ${spawnTracker.path}")
      context.log.info(s"[BootStrap] DomainActor Path: ${domainActor.path}")
      context.log.info(s"[BootStrap] ActorContext ID: ${actorContext.getContextId}")
      context.log.info(s"[BootStrap] Actor Registered: ${actorDef.actorId()}")

      Behaviors.empty
    }, "crawler-actor-system")
  }
}
