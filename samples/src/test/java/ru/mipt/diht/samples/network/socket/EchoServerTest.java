package ru.mipt.diht.samples.network.socket;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.mipt.diht.samples.utils.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static ru.mipt.diht.samples.network.socket.EchoClientsProcess.DEFAULT_ECHO_SERVER_PORT;

/**
 * @author s.chebotarev
 * @since 04.12.2015
 */
@Slf4j
public class EchoServerTest {
    private ExecutorService executor = Executors.newCachedThreadPool();
    private CompletionService<EchoClient> completion = new ExecutorCompletionService<>(executor);

    @Before
    public void init() {
        // не забываемкорректно инициализировать ресурсы
        executor = Executors.newCachedThreadPool();
        completion = new ExecutorCompletionService<>(executor);
    }

    @After
    public void destroy() {
        // ... и закрывать их после использования (init + destroy в данном случае можно было
        // сделать разово для всего класса через Before-/AfterClass)
        Utils.shutdownAndAwaitTermination(executor);
    }

    @Test
    public void testSimpleEchoServer() throws Exception {
        testEchoServerImpl(false);
    }

    @Test
    public void testMultiThreadedEchoServer() throws Exception {
        testEchoServerImpl(true);
    }

    public void testEchoServerImpl(boolean useMultiThreadedSrv) throws Exception {
        // Запускаем блокирующие сервер и клиенты в отдельных потоках
        SimpleSocketServer server = SimpleSocketServer.startSocketServer(DEFAULT_ECHO_SERVER_PORT,
                new EchoClientsProcess(), useMultiThreadedSrv, 2);
        Future<?> serverCompletion = executor.submit(serverRunner(server));

        try {
            // запуск клиентов
            Map<EchoClient, Collection<String>> clients = testClients();
            for (EchoClient client : clients.keySet()) {
                // по-хорошему подобный код (внешний вызов completion.submit) тоже должен быть обёрнут в try-finally,
                // чтобы не потерять незакрытый клиент в случае ошибок
                completion.submit(() -> {
                    // оборачиваем в try-with для автоматического закрытия
                    try (EchoClient _client = client) {
                        client.sendReceive(clients.get(_client));
                    } catch (Throwable t) {
                        log.info("client task failed: {}, e.msg={}", client, t.getMessage());
                        throw t;
                    }
                    return client;
                });
            }

            // Проверяем результаты работы клиентов
            for (int i = 0; i < clients.size(); i++) {
                EchoClient client = completion.take().get();
                assertEquals("sent & received lines", clients.get(client).size(), client.getLines());
            }
        } finally {
            // Thread.interrupt не работает на большинство блокирующих методов в java.net, в том числе и ServerSocket.accept
            // посылаем сигнал прерывания задаче сервера, чтобы после пробуждения она не пыталась продолжить принимать соединения
            serverCompletion.cancel(true);
            // явно закрываем серверный сокет, чтобы обеспечить шарантированный выход из while(!interrupted) { socket.accept } блока
            server.close();
        }
    }

    private static Callable<?> serverRunner(SimpleSocketServer _echoServer) {
        return () -> {
            // оборачиваем в try-with для автоматического закрытия
            try (SimpleSocketServer echoServer = _echoServer) {
                echoServer.processIncomingConnections();
                return echoServer;
            } catch (Throwable t) {
                log.info("server task failed, e.msg={}", t.getMessage());
                throw t;
            }
        };
    }

    private static Map<EchoClient, Collection<String>> testClients() {
        // В реальном коде разносить места создания соединения и их закрытия нужно с большой осторожностью. Лучше
        // выполнять эти операции в одной try-finally конструкции. Если это невозможно, то нужно гарантировать в коде
        // корректный вызов .close при любых потенциальных ошибках.
        Map<EchoClient, Collection<String>> clients = new HashMap<>();
        clients.put(new EchoClient("1", null, DEFAULT_ECHO_SERVER_PORT), asList("123", "asd"));
        clients.put(new EchoClient("2", null, DEFAULT_ECHO_SERVER_PORT), asList("1", "22", "333", "1", "22", "333", "1", "22", "333"));
        clients.put(new EchoClient("3", null, DEFAULT_ECHO_SERVER_PORT), asList("3", "a"));
        clients.put(new EchoClient("4", null, DEFAULT_ECHO_SERVER_PORT), asList("jadposkadkpa", "ubgdsagidaida", "asdadasdasd"));
        return clients;
    }
}
