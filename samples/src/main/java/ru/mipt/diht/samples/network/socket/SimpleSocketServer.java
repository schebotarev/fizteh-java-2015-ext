package ru.mipt.diht.samples.network.socket;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;

/**
 * Простой однопоточный сервер.
 * <br/>
 * Пример использования:
 * <pre>{@code
 * int serverPort = 11001;
 * SocketProcessor<?> logicImpl = new EchoServer();
 * try (SimpleSocketServer server = new SimpleSocketServer<>(serverPort, logicImpl)) {
 *     server.processIncomingConnections();
 * }}</pre>
 *
 * @author s.chebotarev
 * @since 28.11.2015
 * @see MultiThreadedSocketServer
 */
@Slf4j
public class SimpleSocketServer<T> implements AutoCloseable {
    private final ServerSocket socket;
    // В этом параметре будет реализация обработчика соединений. Примеры: echo-server, http-date-server
    private final SocketProcessor<T> processor;

    @SneakyThrows
    public SimpleSocketServer(int port, SocketProcessor<T> processor) {
        log.info("Starting new server @ localhost:{}", port);
        this.socket = new ServerSocket(port);
        this.processor = processor;
    }

    @SneakyThrows
    public void processIncomingConnections() {
        // пока поток не прерван принимаем в цикле входящие соединения
        while (!Thread.interrupted()) {
            // socket.accept() игнорирует прерывания (by design), так что для корректного завершения
            // нужно явно вызывать .close из другого потока, будет генерироваться ConnectException
            // (другая альтернатива - установить у сокета soTimeout и обрабатывать его в данном цикле, внешний вызов close не потребуется)
            try {
                processConnection(socket.accept());
            } catch (SocketException e) {
                if (socket.isClosed()) {
                    // был вызван close, завершаем работу
                    break;
                }
            }
        }
        log.info("server has been interrupted");
    }

    @SneakyThrows
    protected T processConnection(Socket _connection) {
        // оборачиваем соединение в try-with, чтобы оно было автоматически закрыто после обработки
        try (Socket connection = _connection) {
            log.info("processing connection: {}", connection);
            return processor.process(connection);
        } finally {
            log.info("connection closed: {}", _connection);
        }
    }

    @Override
    @SneakyThrows
    public void close() {
        // из try-with конструкции в обрабатывающем соединения потоке и необходимости во внешнем прерывании данного потока
        // (т.е. из другого потока) вызов close может производиться из обоих
        if (!socket.isClosed()) {
            log.info("closing server: {}", socket.getLocalPort());
            socket.close();
        }
    }

    @SneakyThrows
    public static SimpleSocketServer startSocketServer(int port, SocketProcessor<?> logicImpl, boolean multiThreaded, Integer threads) {
        return multiThreaded ?
                new MultiThreadedSocketServer<>(port, logicImpl, Executors.newFixedThreadPool(threads)) :
                new SimpleSocketServer<>(port, logicImpl);
    }
}
