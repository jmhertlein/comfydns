package com.comfydns.resolver.resolver.system;

import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.MessageReadingException;
import com.comfydns.resolver.resolver.rfc1035.service.request.LiveRequest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Optional;

public class JavaNetUDPRequest extends LiveRequest {
    private final DatagramPacket packet;
    private final Message m;

    public JavaNetUDPRequest(DatagramPacket p) throws InvalidMessageException, MessageReadingException {
        this.packet = p;

        byte[] content = new byte[p.getLength()];
        System.arraycopy(p.getData(), p.getOffset(), content, 0, p.getLength());
        m = Message.read(content);
    }
    @Override
    public Message getMessage() {
        return m;
    }

    @Override
    @Deprecated
    protected String getRequestProtocolMetricsTag() {
        return "udp";
    }

    @Override
    public boolean transportIsTruncating() {
        return true;
    }

    @Override
    public Optional<InetAddress> getRemoteAddress() {
        return Optional.of(packet.getAddress());
    }
}
