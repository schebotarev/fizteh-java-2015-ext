package ru.mipt.diht.samples.network.socket;

import lombok.extern.slf4j.Slf4j;
import ru.mipt.diht.samples.utils.Utils;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author s.chebotarev
 * @since 04.12.2015
 */
@Slf4j
public class MultiThreadedSocketServer<T> extends SimpleSocketServer<T> {
    private ExecutorService tasksExecutor;
    private AtomicInteger activeTasks = new AtomicInteger(0);
    private AtomicInteger pendingTasks = new AtomicInteger(0);

    public MultiThreadedSocketServer(int port, SocketProcessor<T> processor, ExecutorService tasksExecutor) {
        super(port, processor);
        this.tasksExecutor = tasksExecutor;
    }

    @Override
    protected T processConnection(Socket connection) {
        // т.к. не все задачи могут активно выполняться в один момент времени, будем вести отдельный счётчик ожидания для вновь поступающих
        int pending = pendingTasks.incrementAndGet();
        log.info("incoming connection: {}, queue=(active={}, pending={})", connection, activeTasks, pending);
        try {
            tasksExecutor.submit(() -> {
                T result = null;
                try {
                    // ожидающий выполнения таск стал активным, т.к. мы находимся в теле запущенной runnable-задачи
                    int pendingSize = pendingTasks.decrementAndGet();
                    int tasks = activeTasks.incrementAndGet();
                    log.info("new active task, total: {} + {} pending", tasks, pendingSize);
                    return (result = super.processConnection(connection));
                } finally {
                    int tasks = activeTasks.decrementAndGet();
                    log.info("task complete: {}, active tasks: {}, pending: {}", result, tasks, pendingTasks);
                }
            });
        } catch (Throwable e) {
            // не удалось добавить задачу в очередь
            pendingTasks.decrementAndGet();
            throw e;
        }
        // нарушаем контракт супер-класса во избежание усложенния примеров (обычно пользуются Future-ми)
        return null;
    }

    @Override
    public void close() {
        super.close();
        Utils.shutdownAndAwaitTermination(tasksExecutor);
    }
}
