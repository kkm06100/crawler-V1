package system;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractActorRegistry implements ActorRegistry {

    // Context ID -> ActorContext 매핑
    protected final Map<String, ActorContext> contexts = new ConcurrentHashMap<>();

    // RoundRobin 인덱스
    protected final AtomicInteger rrIndex = new AtomicInteger(0);

    @Override
    public void registerContext(ActorContext context) {
        if (context == null || context.getContextId() == null) {
            throw new IllegalArgumentException("Context or contextId must not be null");
        }
        contexts.put(context.getContextId(), context);
    }

    @Override
    public Optional<ActorContext> findContextById(String contextId) {
        return Optional.ofNullable(contexts.get(contextId));
    }

    @Override
    public Collection<ActorContext> getAllContexts() {
        return contexts.values();
    }

    @Override
    public void removeContext(String contextId) {
        contexts.remove(contextId);
    }

    /**
     * 기본 RoundRobin 방식으로 Context 선택
     */
    @Override
    public ActorContext selectContext() {
        Collection<ActorContext> allContexts = getAllContexts();
        int size = allContexts.size();
        if (size == 0) {
            throw new IllegalStateException("No ActorContext registered");
        }
        int index = rrIndex.getAndUpdate(i -> (i + 1) % size);
        return allContexts.stream()
            .skip(index)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to select ActorContext"));
    }

    /**
     * updateContext는 기본 구현에서 noop
     * 필요하면 오버라이드하여 상태 갱신 처리 가능
     */
    @Override
    public void updateContext(ActorContext context) {
        // 기본 noop
    }
}

