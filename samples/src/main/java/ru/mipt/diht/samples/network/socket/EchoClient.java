package ru.mipt.diht.samples.network.socket;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.mipt.diht.samples.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * Клиент к {@linkplain EchoClientsProcess}. Пример использования показан в методе {@linkplain #main(String[]) EchoClient.main}
 *
 * @author s.chebotarev
 * @since 04.12.2015
 */
@Slf4j
@Getter
@ToString
public class EchoClient implements AutoCloseable {
    private String id;
    private Socket socket;
    private int lines = 0;

    @SneakyThrows
    public EchoClient(String id, String host, int port) {
        log.info("client#{} connecting to server: {}:{}", id, host, port);
        this.id = id;
        this.socket = new Socket(host, port);
    }

    @SneakyThrows
    public void sendReceive(Collection<String> output) {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            for (String line : output) {
                out.write(line);
                out.newLine();
                out.flush();

                String echo = in.readLine();
                if (echo == null) {
                    throw new IllegalStateException("server closed connection too early");
                }
                lines++;
                log.info("client#{}: sent={} chars, received={} chars, echo.endWith(line)={}",
                        id, line.length(), echo.length(), echo.endsWith(line));

                // для наглядности тестов добавляем задержку 100-200ms
                Utils.sleep(100 + (int) (Math.random() * 100));
            }
        }
        log.info("client#{} sent & received {} lines", id, lines);
    }

    @Override
    @SneakyThrows
    public void close() {
        log.info("closing the client: {}", id);
        socket.close();
        socket = null;
    }

    public static void main(String[] args) {
        // пример ручного запуска
        try (EchoClient client = new EchoClient("demo", null/*localhost used*/, EchoClientsProcess.DEFAULT_ECHO_SERVER_PORT)) {
            client.sendReceive(asList("213", "qawe", "asd"));
        }
    }
}
