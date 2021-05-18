package com.comfydns.resolver.resolver.rfc1035.service.transport;

import java.net.InetAddress;
import java.util.function.Consumer;

public interface MessageTransport {
    public void send(byte[] payload, InetAddress dest, Consumer<byte[]> cb, Consumer<Throwable> onError);
}
