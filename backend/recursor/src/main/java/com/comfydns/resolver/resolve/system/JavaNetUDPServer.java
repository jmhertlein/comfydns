package com.comfydns.resolver.resolve.system;

import com.comfydns.resolver.resolve.rfc1035.DNS;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.service.RecursiveResolver;
import com.comfydns.util.config.EnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class JavaNetUDPServer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(JavaNetUDPServer.class);

    private final RecursiveResolver resolver;

    private final ExecutorService workerPool;

    private final AtomicBoolean shutdown;

    public JavaNetUDPServer(RecursiveResolver resolver, ExecutorService workerPool) {
        this.resolver = resolver;
        this.workerPool = workerPool;
        shutdown = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        int port = EnvConfig.getDnsServerPort();
        while(!shutdown.get()) {
            try (DatagramSocket s = new DatagramSocket(port)) {
                while (!shutdown.get()) {
                    DatagramPacket p = new DatagramPacket(new byte[DNS.MAX_UDP_DATAGRAM_LENGTH], DNS.MAX_UDP_DATAGRAM_LENGTH);
                    try {
                        s.receive(p);
                        workerPool.submit(() -> {
                            Message response = resolver.resolve(() -> new JavaNetUDPRequest(p));
                            byte[] buf = response.write();
                            try {
                                DatagramPacket responsePacket = new DatagramPacket(buf, buf.length);
                                responsePacket.setAddress(p.getAddress());
                                responsePacket.setPort(p.getPort());
                                s.send(responsePacket);
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

    public void setShutdown() {
        this.shutdown.set(true);
    }
}
