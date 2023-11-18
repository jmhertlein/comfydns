package com.comfydns.resolver.resolver.rfc1035.service.transport;

import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.function.Consumer;

public class UDPSyncTransport implements TruncatingSyncTransport {
    private static final Counter udpTimeouts = Counter.build()
            .name("udp_outbound_timeouts").help("Count of UDP packets where we sent a datagram expecting a response, but didn't receive one.")
            .register();

    private static final Logger log = LoggerFactory.getLogger(UDPSyncTransport.class);
    private static final int DNS_UDP_PORT = 53;
    private static final int DNS_MAX_UDP_DATAGRAM_LENGTH = 512;

    @Override
    public byte[] send(byte[] payload, InetAddress dest) throws Exception {
        try(DatagramSocket socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(payload, payload.length, dest, DNS_UDP_PORT));
            socket.setSoTimeout(1000);
            DatagramPacket p = new DatagramPacket(new byte[DNS_MAX_UDP_DATAGRAM_LENGTH],
                    DNS_MAX_UDP_DATAGRAM_LENGTH);
            try {
                socket.receive(p);
            } catch(SocketTimeoutException ste) {
                udpTimeouts.inc();
                throw ste;
            }
            byte[] output = new byte[p.getLength()];
            System.arraycopy(p.getData(), p.getOffset(), output, 0, p.getLength());
            return output;
        }
    }
}
