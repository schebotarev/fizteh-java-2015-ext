package ru.mipt.diht.samples.network.url;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Документации: <a href="https://docs.oracle.com/javase/tutorial/networking/urls/index.html">
 *     https://docs.oracle.com/javase/tutorial/networking/urls/index.html</a>
 *
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

    @Test
    // copy-pasted from https://docs.oracle.com/javase/tutorial/networking/urls/urlInfo.html
    public void testUrlComponents() throws Exception {
        URL url = new URL("http://user:pw@example.com:8080/docs/books/tutorial/index.html?name=networking#DOWNLOADING");

        assertEquals("protocol", "http", url.getProtocol());
        assertEquals("userInfo", "user:pw", url.getUserInfo());
        assertEquals("authority", "user:pw@example.com:8080", url.getAuthority());
        assertEquals("host", "example.com", url.getHost());
        assertEquals("port", 8080, url.getPort());
        assertEquals("defaultPort", 80, url.getDefaultPort());
        assertEquals("path", "/docs/books/tutorial/index.html", url.getPath());
        assertEquals("query", "name=networking", url.getQuery());
        assertEquals("file", "/docs/books/tutorial/index.html?name=networking", url.getFile());
        assertEquals("ref", "DOWNLOADING", url.getRef());
    }
}
