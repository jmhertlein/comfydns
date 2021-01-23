package cafe.josh.comfydns.rfc1035.service.transport;

import cafe.josh.comfydns.rfc1035.message.struct.Message;

import java.net.InetAddress;
import java.util.function.Consumer;

public interface MessageTransport {
    public void send(byte[] payload, InetAddress dest, Consumer<byte[]> cb, Consumer<Throwable> onError);
}
