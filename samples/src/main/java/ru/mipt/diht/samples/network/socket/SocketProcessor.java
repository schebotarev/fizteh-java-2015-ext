package ru.mipt.diht.samples.network.socket;

import java.net.Socket;

/**
 * @author s.chebotarev
 * @since 04.12.2015
 */
public interface SocketProcessor<T> {
    T process(Socket socket) throws Exception;
}
