package com.comfydns.resolver.resolve.rfc1035.service.transport;

import com.comfydns.resolver.resolve.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;

import java.net.InetAddress;
import java.util.Map;

public class TestTruncatingTransport implements TruncatingSyncTransport {
    private final Map<InetAddress, Message> responses;

    public TestTruncatingTransport(Map<InetAddress, Message> responses) {
        this.responses = responses;
    }

    @Override
    public byte[] send(byte[] payload, InetAddress dest) throws Exception {
        Message message = responses.get(dest);
        Message read;
        try {
            read = Message.read(payload);
        } catch (InvalidMessageException e) {
            throw new RuntimeException("oops");
        }
        message.getHeader().setId(read.getHeader().getId());
        return message.write();
    }
}
