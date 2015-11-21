package ru.mipt.diht.samples.concurrency;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNull;

/**
 * @author s.chebotarev
 * @since 21.11.2015
 */
@Slf4j
public class ThreadLocalTest {
    // это значение доступно всем потокам и при модификации будет доступно всем другим потокам
    public static String SOME_VALUE = "123";
    // этот же объект выдаёт разным потокам общее изначальное значение 123, но при модификации изменения будут видны только меняющему потоку
    // получаем поведение, аналогичное конструкции Map<Thread, String>, где для доступа по ключу всегда используется Thread.currentThread
    private static ThreadLocal<String> SOME_THREADED_VALUE = ThreadLocal.withInitial(() -> "123");

    @Test
    public void testThreadLocal() throws Exception {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        List<Thread> workers = Helper.newWorkers(threadLocal, "");
        Helper.start(workers);
        Helper.join(workers);
    }

    @Test
    public void testThreadLocalWithInitial() throws Exception {
        ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "INIT-VAL");
        List<Thread> workers = Helper.newWorkers(threadLocal, "");
        Helper.start(workers);
        Helper.join(workers);
    }

    @Test
    public void testInheritableThreadLocal() throws Exception {
        ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();
        //ThreadLocal<String> threadLocal = new ThreadLocal<>();
        List<Thread> workers = Helper.newComplexWorkers(threadLocal);
        Helper.start(workers);
        Helper.join(workers);
    }

    private static class SampleWorkerThread extends Thread {
        public SampleWorkerThread(String name, ThreadLocal<String> threadLocal) {
            super(() -> {
                for (int i = 0; i < 5; i++) {
                    randomSleep(1000, 1000);
                    String value = threadLocal.get();
                    if (value == null) {
                        value = "<NULL>";
                    }
                    String nextValue = value + "-" + i;
                    if (!nextValue.startsWith(name)) {
                        nextValue = name + "-" + nextValue;
                    }
                    threadLocal.set(nextValue);
                    log.info("{} worker: tl.value={}, next={}", name, value, nextValue);
                }
            });
        }

        @SneakyThrows
        private static void randomSleep(long fixedMs, long maxExtraMs) {
            Thread.sleep(fixedMs + (long) (Math.random() * maxExtraMs));
        }
    }

    private static class ComplexWorkerThread extends Thread {
        public ComplexWorkerThread(String name, ThreadLocal<String> threadLocal) {
            super(() -> {
                String value = threadLocal.get();
                assertNull(value);

                value = "<" + name + ">";
                threadLocal.set(value);
                log.info("{} worker initial value: {}", name, threadLocal.get());

                List<Thread> subWorkers = Helper.newWorkers(threadLocal, name);
                Helper.start(subWorkers);
                Helper.join(subWorkers);

                log.info("{} worker final value: {}", name, threadLocal.get());
            });
        }
    }

    private static class Helper {
        public static List<Thread> newWorkers(ThreadLocal<String> threadLocal, String prefix) {
            prefix = prefix != null ? prefix + "-" : "";
            return asList(
                    new SampleWorkerThread(prefix + "AAA", threadLocal),
                    new SampleWorkerThread(prefix + "BBB", threadLocal),
                    new SampleWorkerThread(prefix + "CCC", threadLocal)
            );
        }

        public static List<Thread> newComplexWorkers(ThreadLocal<String> threadLocal) {
            return asList(
                    new ComplexWorkerThread("W1", threadLocal),
                    new ComplexWorkerThread("w2", threadLocal)
            );
        }

        public static void start(List<Thread> threads) {
            for (Thread thread : threads) {
                thread.start();
            }
        }

        @SneakyThrows
        public static void join(List<Thread> threads) {
            // ожидаем завершения
            for (Thread thread : threads) {
                thread.join();
            }
        }
    }
}
