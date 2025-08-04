package system;

import akka.actor.typed.ActorRef;

public interface ActorContext extends ActorFactory, TaskExecutor {

    String getContextId();

    boolean isBusy();

    long lastActiveTimestamp();

    void updateLastActive();

    ActorRef<?> getSpawnTracker();
}
