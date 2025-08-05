package system;

import java.util.Collection;
import java.util.Optional;

public interface ActorRegistry {

    void registerContext(ActorContext context);

    Optional<ActorContext> findContextById(String contextId);

    Collection<ActorContext> getAllContexts();

    void removeContext(String contextId);

    ActorContext selectContext();

    void updateContext(ActorContext context);
}

