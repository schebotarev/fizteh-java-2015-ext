package ru.mipt.diht.samples.network.socket;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.net.URL;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * @author s.chebotarev
 * @since 04.12.2015
 */
public class MockHttpServer {
    public static final int MOCK_HTTP_SERVER_PORT = 8080;

    @Test
    public void testMockHttpServer() throws Exception {
        //noinspection unused
        try (SimpleSocketServer server = runServer()) {
            String content = IOUtils.toString(new URL("http://localhost:" + MOCK_HTTP_SERVER_PORT));
            assertTrue("content is http body", content.startsWith("Time: "));
        }
    }

    private static SimpleSocketServer runServer() {
        // пример ручного запуска
        SocketProcessor<?> logicImpl = socket -> {
            String responseTemplate = "HTTP/1.1 200 OK\nConnection: close\nContent-Length: {len}\n\n{body}";
            String body = "Time: " + new Date() + "\n";
            String response = responseTemplate.replace("{len}", String.valueOf(body.getBytes().length)).replace("{body}", body);
            socket.getOutputStream().write(response.getBytes());
            return null;
        };
        SimpleSocketServer echoServer = SimpleSocketServer.startSocketServer(MOCK_HTTP_SERVER_PORT, logicImpl, false, null);
        // не забываем в остальных случаях правильно запускать и управлять потоками, корректно обрабытвать ресурсы (server)
        new Thread(echoServer::processIncomingConnections).start();
        return echoServer;
    }

    public static void main(String[] args) {
        // ручной запуск сервера для внешних тестов
        runServer();
    }
}
