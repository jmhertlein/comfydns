package cafe.josh.comfydns.rfc1035.service.request;

import cafe.josh.comfydns.rfc1035.message.struct.Message;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import java.util.UUID;

public abstract class Request {
    protected static final Counter requestsIn = Counter.build()
            .name("requests_in").help("All internet requests received")
            .labelNames("protocol").register();
    protected static final Counter requestsOut = Counter.build()
            .name("requests_out").help("All internet requests received")
            .labelNames("protocol", "rcode").register();
    protected static final Histogram requestDurations = Histogram.build()
            .linearBuckets(0, 0.5, 20)
            .name("request_duration").help("How long requests take, from receipt to response.")
            .labelNames("source").register();

    protected final UUID id;
    private final Histogram.Timer requestTimer;

    public Request() {
        id = UUID.randomUUID();
        requestTimer = requestDurations.labels(isInternal() ? "internal" : "external")
        .startTimer();
    }

    public abstract Message getMessage();

    public abstract void answer(Message m);

    protected void recordAnswer(Message m, String requestProtocol) {
        requestTimer.observeDuration();
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
