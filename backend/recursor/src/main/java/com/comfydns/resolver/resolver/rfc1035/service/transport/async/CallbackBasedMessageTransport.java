package com.comfydns.resolver.resolver.rfc1035.service.transport.async;

import java.net.InetAddress;
import java.util.function.Consumer;

public interface CallbackBasedMessageTransport {
    public void send(byte[] payload, InetAddress dest, Consumer<byte[]> cb, Consumer<Throwable> onError);
}
