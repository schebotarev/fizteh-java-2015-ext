package ru.mipt.diht.samples.network.socket;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author s.chebotarev
 * @since 04.12.2015
 */
@Slf4j
@Getter
public class EchoClientsProcess implements SocketProcessor<String> {
    public static final int DEFAULT_ECHO_SERVER_PORT = 11001;

    private AtomicInteger clients = new AtomicInteger(0);
    private AtomicInteger linesProcessed = new AtomicInteger(0);

    @Override
    public String process(Socket socket) throws Exception {
        int clientNum = clients.incrementAndGet();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            int lines = 0;
            log.info("New client #{}", clientNum);
            for (String t; (t = in.readLine()) != null; ) {
                lines++;
                log.info("client[{}].line[{}].size: {}", clientNum, lines, t.length());
                out.write("echo: " + t);
                out.newLine();
                out.flush();
            }
            int totalLines = linesProcessed.addAndGet(lines);
            log.info("Client #{} processed (lines={}/total={})", clientNum, lines, totalLines);
        }
        return "client#" + clientNum;
    }

    public static void main(String[] args) {
        // пример ручного запуска
        SocketProcessor<?> logicImpl = new EchoClientsProcess();
        try (SimpleSocketServer echoServer = SimpleSocketServer.startSocketServer(DEFAULT_ECHO_SERVER_PORT, logicImpl, true, 2)) {
            echoServer.processIncomingConnections();
        }
    }
}
