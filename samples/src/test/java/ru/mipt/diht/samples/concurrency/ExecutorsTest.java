package ru.mipt.diht.samples.concurrency;

import org.junit.Test;
import ru.mipt.diht.samples.utils.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * @author s.chebotarev
 * @since 14.11.2015
 */
public class ExecutorsTest {
    public static final long ONE_SECOND_MS = 1000;

    @Test(timeout = 2 * ONE_SECOND_MS)
    public void testMultiThreadedExecution() throws Exception {
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            service.submit(() -> Utils.sleep(ONE_SECOND_MS));
        }
        service.shutdown();
        assertTrue("all tasks complete", service.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    public void testSingleThreadedExecution() throws Exception {
        long startTs = System.currentTimeMillis();

        ExecutorService service = Executors.newFixedThreadPool(1);
        for (int i = 0; i < 4; i++) {
            service.submit(() -> Utils.sleep(ONE_SECOND_MS));
        }
        service.shutdown();
        assertTrue(service.awaitTermination(10, TimeUnit.SECONDS));

        long endTs = System.currentTimeMillis();
        assertTrue("duration >= 4 sec", (endTs - startTs) >= 4 * ONE_SECOND_MS);
    }

    // Пример из документации к классу ExecutorService
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
