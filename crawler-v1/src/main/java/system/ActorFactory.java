package system;

import java.util.Optional;

public interface ActorFactory {

    void register(ActorDefinition definition);

    Optional<ActorDefinition> findById(String id);

    void removeActor(String actorId);
}
