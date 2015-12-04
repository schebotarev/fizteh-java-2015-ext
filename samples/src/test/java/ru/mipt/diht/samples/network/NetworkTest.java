package ru.mipt.diht.samples.network;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Пример получения списка сетевых интерфейсов и их свойств.
 *
 * @author s.chebotarev
 * @since 04.12.2015
 */
@Slf4j
public class NetworkTest {
    @Test
    public void fakeNetworkInterfaceTest() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            log.info("network interface: {}, addresses: {}", iface, iface.getInterfaceAddresses());
            log.info("options: loopback={}, pointToPoint={}, up={}, virtual={}",
                    iface.isLoopback(), iface.isPointToPoint(), iface.isUp(), iface.isVirtual());
        }
    }
}
