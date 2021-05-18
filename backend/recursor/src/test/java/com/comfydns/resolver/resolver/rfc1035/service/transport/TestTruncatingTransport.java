package com.comfydns.resolver.resolver.rfc1035.service.transport;

import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;

import java.net.InetAddress;
import java.util.Map;
import java.util.function.Consumer;

public class TestTruncatingTransport implements TruncatingTransport {
    private final Map<InetAddress, Message> responses;

    public TestTruncatingTransport(Map<InetAddress, Message> responses) {
        this.responses = responses;
    }

    @Override
    public void send(byte[] payload, InetAddress dest, Consumer<byte[]> cb, Consumer<Throwable> onError) {
        try {
            Message message = responses.get(dest);
            Message read;
            try {
                read = Message.read(payload);
            } catch (InvalidMessageException | UnsupportedRRTypeException e) {
                throw new RuntimeException("oops");
            }
            message.getHeader().setId(read.getHeader().getId());
            cb.accept(message.write());
        } catch(Throwable t) {
            onError.accept(t);
        }
    }
}
