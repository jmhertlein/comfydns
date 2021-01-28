package cafe.josh.comfydns.rfc1035.service.request;

import cafe.josh.comfydns.rfc1035.message.struct.Message;
import io.prometheus.client.Counter;

import java.util.UUID;

public abstract class Request {
    protected static final Counter requestsIn = Counter.build()
            .name("requests_in").help("All internet requests received")
            .labelNames("protocol").register();
    protected static final Counter requestsOut = Counter.build()
            .name("requests_out").help("All internet requests received")
            .labelNames("protocol", "rcode").register();

    protected final UUID id;

    public Request() {
        id = UUID.randomUUID();
    }

    public abstract Message getMessage();

    public abstract void answer(Message m);

    protected void recordAnswer(Message m, String requestProtocol) {
        requestsOut.labels(requestProtocol, m.getHeader().getRCode().name().toLowerCase())
                .inc();
    }

    public UUID getId() {
        return id;
    }

    public boolean isInternal() {
        return false;
    }
}
