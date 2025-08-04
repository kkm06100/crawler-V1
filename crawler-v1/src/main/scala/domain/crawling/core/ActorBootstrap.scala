import system.{ActorDefinition, ActorRegistry, ActorContext}
import akka.actor.typed.ActorRef

import java.util.{Collection, Optional, Collections}
import java.util.concurrent.ConcurrentHashMap

// 최소 동작하는 ActorRegistry 구현 (Java 인터페이스 기반)
class SimpleActorRegistry extends ActorRegistry {

  // Context 저장 (id → ActorContext)
  private val contexts = new ConcurrentHashMap[String, ActorContext]()

  // ActorDefinition 저장 (actorId → ActorDefinition)
  private val actors = new ConcurrentHashMap[String, ActorDefinition]()

  // 단순 등록: Context 저장
  override def registerContext(context: ActorContext): Unit = {
    contexts.put(context.getContextId, context)
  }

  // Context 조회
  override def findContextById(contextId: String): Optional[ActorContext] =
    Optional.ofNullable(contexts.get(contextId))

  // 전체 Context 반환
  override def getAllContexts(): Collection[ActorContext] =
    Collections.list(contexts.elements())

  // Context 제거
  override def removeContext(contextId: String): Unit =
    contexts.remove(contextId)

  // ActorContext 단순 RoundRobin 선택 (없으면 예외)
  private val rrIndex = new java.util.concurrent.atomic.AtomicInteger(0)
  override def selectContext(): ActorContext = {
    val all = getAllContexts()
    if (all.isEmpty) throw new IllegalStateException("No contexts registered")
    val index = rrIndex.getAndUpdate(i => (i + 1) % all.size())
    all.stream().skip(index).findFirst().get()
  }

  // Context 업데이트 (noop)
  override def updateContext(context: ActorContext): Unit = ()

  // ActorDefinition 등록 (추가)
  def register(actorDef: ActorDefinition): Unit = {
    actors.put(actorDef.actorId(), actorDef)
  }
}

// 예제 사용법 일부

object ActorBootstrap {

  def main(args: Array[String]): Unit = {
    // ... ActorSystem, spawnTracker, domainActor 생성 가정

    // 임시 ActorRef[?]를 null로 두고, 실제는 적절히 생성해야 함
    val spawnTracker: ActorRef[Any] = null.asInstanceOf[ActorRef[Any]]
    val domainActor: ActorRef[Any] = null.asInstanceOf[ActorRef[Any]]

    val actorDef = new ActorDefinition(
      "domainActor1",
      domainActor,
      "spawnTracker1",
      "guardianIdExample"
    )

    val actorRegistry = new SimpleActorRegistry()

    // ActorContext는 null로 두거나 실제 생성해도 됨
    val dummyContext = new ActorContext {
      override def getContextId(): String = "dummyContext"
      // ActorFactory 메소드 등은 적당히 구현하거나 빈 구현으로 처리
      override def register(definition: ActorDefinition): Unit = ()
      override def findById(id: String): Optional[ActorDefinition] = Optional.empty()
      override def removeActor(actorId: String): Unit = ()
      override def getSpawnTracker(): ActorRef[_] = spawnTracker
      override def enqueue(task: Runnable): Unit = ()
      override def dequeue(): Optional[Runnable] = Optional.empty()
      override def hasPendingTasks(): Boolean = false
      override def queueSize(): Int = 0
      override def isBusy(): Boolean = false
      override def lastActiveTimestamp(): Long = 0L
      override def updateLastActive(): Unit = ()
    }

    actorRegistry.registerContext(dummyContext)

    actorRegistry.register(actorDef)

    println("Actor 및 SpawnTracker 정상 등록 완료")
  }
}
