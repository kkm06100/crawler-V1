package system

import akka.actor.typed.{ActorSystem, Behavior}
import system.actors.{SpawnManager, SpawnTracker}
import system.ast.SystemMessages.ActorContextPair

final case class ActorManagerGenerator(
                                                private val contexts: Map[String, ActorContextPair]
                                              ) {
  def getContext(id: String): Option[ActorContextPair] =
    contexts.get(id)

  def allContexts: Map[String, ActorContextPair] =
    contexts
}

object ActorManagerGenerator {

  def initialize(
                  registryId: String,
                  contextCount: Int,
                  actors: Map[String, Behavior[_]]
                )(implicit system: ActorSystem[_]): ActorManagerGenerator = {

    val contextMap: Map[String, ActorContextPair] = (1 to contextCount).map { idx =>
      val contextId = s"$registryId-$idx"

      val tracker = system.systemActorOf(SpawnTracker(), s"${contextId}-tracker")
      val manager = system.systemActorOf(SpawnManager(tracker), s"${contextId}-manager")
      val facade = new SpawnManagerFacade(manager)
      facade.registerMultiple(actors)

      contextId -> new ActorContextPair(manager, facade)
    }.toMap

    new ActorManagerGenerator(contextMap)
  }
}
