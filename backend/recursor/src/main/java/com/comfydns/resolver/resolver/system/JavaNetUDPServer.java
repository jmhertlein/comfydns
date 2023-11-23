package com.comfydns.resolver.resolver.system;

import com.comfydns.resolver.resolver.rfc1035.DNS;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import com.comfydns.util.config.EnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

public class JavaNetUDPServer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(JavaNetUDPServer.class);

    private final RecursiveResolver resolver;

    private final ExecutorService workerPool;

    public JavaNetUDPServer(RecursiveResolver resolver, ExecutorService workerPool) {
        this.resolver = resolver;
        this.workerPool = workerPool;
    }

    @Override
    public void run() {
        int port = EnvConfig.getDnsServerPort();
        while(true) { // keep re-opening socket
            try (DatagramSocket s = new DatagramSocket(port)) {
                while (true) {
                    DatagramPacket p = new DatagramPacket(new byte[DNS.MAX_UDP_DATAGRAM_LENGTH], DNS.MAX_UDP_DATAGRAM_LENGTH);
                    try {
                        s.receive(p);
                        workerPool.submit(() -> {
                            Message response = resolver.resolve(() -> new JavaNetUDPRequest(p));
                            byte[] buf = response.write();
                            try {
                                s.send(new DatagramPacket(buf, buf.length));
                            } catch (IOException e) {
                                log.warn("Error while writing response to transport", e);
                            }
                        });
                    } catch (IOException e) {
                        log.warn("Error while listening on UDP socket", e);
                    }
                }
            } catch (SocketException e) {
                log.error("Error while opening UDP socket", e);
            }
        }
    }
}
