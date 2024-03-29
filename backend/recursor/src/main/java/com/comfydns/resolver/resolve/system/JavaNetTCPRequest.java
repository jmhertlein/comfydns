package com.comfydns.resolver.resolve.system;

import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.service.request.LiveRequest;

import java.net.InetAddress;
import java.util.Optional;

public class JavaNetTCPRequest extends LiveRequest {
    private final InetAddress clientAddr;
    private final Message m;

    public JavaNetTCPRequest(InetAddress clientAddr, Message m) {
        this.clientAddr = clientAddr;
        this.m = m;
    }

    @Override
    public Message getMessage() {
        return m;
    }

    @Override
    @Deprecated
    protected String getRequestProtocolMetricsTag() {
        return "tcp";
    }

    @Override
    public boolean transportIsTruncating() {
        return false;
    }

    @Override
    public Optional<InetAddress> getRemoteAddress() {
        return Optional.of(clientAddr);
    }
}
