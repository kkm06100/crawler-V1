package domain;

import akka.actor.typed.ActorRef;
import system.AbstractActorContext;

public class CrawlerActorContext extends AbstractActorContext {

    private final String contextId;

    public CrawlerActorContext(String contextId, ActorRef<?> spawnTracker) {
        super(spawnTracker);
        if (contextId == null || contextId.isBlank()) {
            throw new IllegalArgumentException("contextId must not be null or blank");
        }
        this.contextId = contextId;
    }

    @Override
    protected String contextId() {
        return contextId;
    }
}

