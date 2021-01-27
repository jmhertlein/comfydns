package cafe.josh.comfydns.rfc1035.service.request;

import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;

import java.util.UUID;

public abstract class Request {
    protected final UUID id;

    public Request() {
        id = UUID.randomUUID();
    }

    public abstract Message getMessage();

    public abstract void answer(Message m);

    public UUID getId() {
        return id;
    }
}
