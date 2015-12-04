package ru.mipt.diht.samples.network.url;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author s.chebotarev
 * @since 04.12.2015
 */
@Slf4j
public class UrlConnectionTest {
    @Test
    public void testUrlConnection() throws Exception {
        URL url = new URL("http://ya.ru/robots.txt");
        URLConnection connection = url.openConnection();

        log.info("Page {} headers: {}", url, connection.getHeaderFields());

        assertTrue("type header specifies text", connection.getContentType().startsWith("text/plain"));
        assertTrue("last modified header set", connection.getLastModified() > 0);
        assertNotNull("date header specified", connection.getHeaderFields().get("Date"));

        // если нет уверенности во встроенных обработчиках ошибок и автоматическом закрытии потока при завершении чтения,
        // стоит заворачивать код в try-блок
        String content;
        try (InputStream in = connection.getInputStream()) {
            content = IOUtils.toString(in);
        }
        assertTrue("contains agent", content.contains("User-agent: "));
        assertTrue("contains allow", content.contains("Allow: "));
        assertTrue("contains disallow", content.contains("Disallow: "));
    }
}
