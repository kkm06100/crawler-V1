package system;

import java.util.Optional;

public interface TaskExecutor {

    void enqueue(Runnable task);

    Optional<Runnable> dequeue();

    boolean hasPendingTasks();

    int queueSize();
}
