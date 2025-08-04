package system;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractActorFactory implements ActorFactory{

    private final Map<String, ActorDefinition> actors = new ConcurrentHashMap<>();

    public AbstractActorFactory() {
    }

    @Override
    public void register(ActorDefinition definition) {
        // actorId 혹은 actorPath(풀 경로) 기준으로 key 설정
        actors.put(definition.actorId(), definition);
    }

    @Override
    public Optional<ActorDefinition> findById(String id) {
        return Optional.ofNullable(actors.get(id));
    }

    @Override
    public void removeActor(String actorId) {
        actors.remove(actorId);
    }
}
