package com.comfydns.resolver.resolver.rfc1035.service.transport;

import java.net.InetAddress;
import java.util.function.Consumer;

public interface SyncMessageTransport {
    public byte[] send(byte[] payload, InetAddress dest) throws Exception;
}
