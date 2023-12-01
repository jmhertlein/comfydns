package com.comfydns.resolver.resolve.rfc1035.service.transport;

import java.net.InetAddress;

public interface SyncMessageTransport {
    public byte[] send(byte[] payload, InetAddress dest) throws Exception;
}
