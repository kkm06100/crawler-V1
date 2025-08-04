package system;

import java.util.Collection;
import java.util.Optional;

public interface ActorRegistry {

    // ActorContext 등록
    void registerContext(ActorContext context);

    // Context ID로 조회
    Optional<ActorContext> findContextById(String contextId);

    // 전체 등록된 Context 목록 조회
    Collection<ActorContext> getAllContexts();

    // Context 제거
    void removeContext(String contextId);

    // 작업 배정 (Task 분배 위한 Context 선택)
    ActorContext selectContext();

    // Context 상태 업데이트 (필요하면)
    void updateContext(ActorContext context);
}

