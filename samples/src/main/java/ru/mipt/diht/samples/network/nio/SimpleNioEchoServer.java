package ru.mipt.diht.samples.network.nio;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import ru.mipt.diht.samples.utils.Utils;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author s.chebotarev
 * @since 05.12.2015
 */
@Slf4j
public class SimpleNioEchoServer {
    public static final int DEFAULT_NIO_SERVER_PORT = 11002;
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final byte[] ECHO_PREFIX = "echo: ".getBytes(Utils.UTF8);

    public static void main(String[] args) {
        new SimpleNioEchoServer().run();
    }

    @SneakyThrows
    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {

            // запускаем серверное соединение-канал и переводи его в неблокирующий режим
            serverSocketChannel.bind(new InetSocketAddress(DEFAULT_NIO_SERVER_PORT));
            serverSocketChannel.configureBlocking(false);
            // регистрируем в
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            int clients = 0;

            while (!Thread.interrupted()) {
                try {
                    // проверяем есть ли новые события для обработки
                    if (selector.selectNow() > 0) {
                        for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); it.remove()) {
                            SelectionKey key = it.next();
                            // для каждого события нужно проверить его тип
                            if (key.isAcceptable()) {
                                acceptNewClient(selector, key, ++clients);
                            } else if (key.isReadable()) {
                                readAndReply(key);
                            } else {
                                log.warn("strange key selected: {}", key);
                            }
                        }
                    }

                    // в цилке можно добавить небольшую задержк, чтобы не занимать постоянно CPU, но уменьшится отзывчивость
                } catch (Exception e) {
                    log.warn("something failed", e);
                }
            }
        }
    }

    @SneakyThrows
    private void acceptNewClient(Selector selector, SelectionKey key, int id) {
        // принимаем новое подключение
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
        log.info("new client #{}: {}", id, clientChannel);
        // пореводим его в неблокирующий режим и регистрируем в selector-е для отслеживания доступных событий чтения
        clientChannel.configureBlocking(false);
        // в дополнение регистрируем в ключе объект для хранения индивидуальных данных соединения
        clientChannel.register(selector, SelectionKey.OP_READ, new ClientData(id));

        // в реально приложении может иметь смысл распределять входящие соединения по n потокам
        // или ставить новые соединения в очередь при перегрузке
    }

    @SneakyThrows
    private void readAndReply(SelectionKey key) {
        // восстанавливаем контекст запроса с помощью ключа
        ClientData data = (ClientData) key.attachment();
        SocketChannel channel = ((SocketChannel) key.channel());
        ByteBuffer buffer = data.getBuffer();

        // проверяем пришли ли новые данные или соединение закрыто и мы получили -1 (EOF)
        int read = channel.read(buffer);
        if (read > 0) {
            // подгатавлием буфер, в который были считаны данные, к чтению для их выгрузки обратно
            buffer.flip();
            log.info("c[{}].in.bytes: {}", data.getId(), buffer.remaining());

            // так можно было бы считать данные из буфера, однако, следует добавить проверку их целостности, т.к. возможно полусостояние
            //byte[] bytes = new byte[buffer.remaining()];
            //buffer.get(bytes);
            //String input = new String(bytes);

            // эту часть по-хорошему тоже стоило сделать асинхронной, но мы упростим
            ByteBuffer prefix = ByteBuffer.wrap(ECHO_PREFIX);
            while (prefix.hasRemaining()) {
                // для гарантии полной записи важно выполнять операции в цикле
                channel.write(prefix);
            }
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            // очищаем буффер для последующей записи
            buffer.clear();
        } else if (read == -1) {
            // соединение закрыто, высвобождаем ресурсы
            log.info("closing the client #{}", data.getId());
            channel.close();
            key.cancel();
        }
    }

    @Value
    private class ClientData {
        private final int id;
        private final ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    }
}
