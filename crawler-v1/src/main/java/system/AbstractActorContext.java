package system;

import akka.actor.typed.ActorRef;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractActorContext extends AbstractActorFactory implements ActorContext {

    private final ActorRef<?> spawnTracker;

    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    private final AtomicLong lastActiveTime = new AtomicLong(System.currentTimeMillis());

    protected abstract String contextId();

    public AbstractActorContext(ActorRef<?> spawnTracker) {
        super();
        this.spawnTracker = spawnTracker;
    }

    @Override
    public String getContextId() {
        return contextId();
    }

    @Override
    public boolean isBusy() {
        return !taskQueue.isEmpty();
    }

    @Override
    public long lastActiveTimestamp() {
        return lastActiveTime.get();
    }

    @Override
    public void updateLastActive() {
        lastActiveTime.set(System.currentTimeMillis());
    }

    @Override
    public ActorRef<?> getSpawnTracker() {
        return spawnTracker;
    }

    @Override
    public void enqueue(Runnable task) {
        if (task != null) {
            taskQueue.offer(task);
            updateLastActive();
        }
    }

    @Override
    public Optional<Runnable> dequeue() {
        Runnable task = taskQueue.poll();
        if (task != null) {
            updateLastActive();
            return Optional.of(task);
        }
        return Optional.empty();
    }

    @Override
    public boolean hasPendingTasks() {
        return !taskQueue.isEmpty();
    }

    @Override
    public int queueSize() {
        return taskQueue.size();
    }

    public void executeOne() {
        dequeue().ifPresent(Runnable::run);
    }

    public void executeAll() {
        Runnable task;
        while ((task = taskQueue.poll()) != null) {
            task.run();
            updateLastActive();
        }
    }
}
