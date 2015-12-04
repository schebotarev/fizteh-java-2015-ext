package ru.mipt.diht.samples.utils;

import lombok.SneakyThrows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author s.chebotarev
 * @since 05.11.2015
 */
public class Utils {
    /*
    * Возвращает объект Date с указанной в формате "yyyy.MM.dd" датой.
    **/
    public static Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("yyyy.MM.dd").parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("illegal date: " + dateStr, e);
        }
    }

    @SneakyThrows
    public static void sleep(long ms) {
        Thread.sleep(ms);
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
