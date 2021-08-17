package com.comfydns.resolver.resolver.rfc1035.service.request;

import com.comfydns.resolver.resolver.rfc1035.message.field.query.QOnlyType;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.search.QSet;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class Request {
    protected static final Counter requestsIn = Counter.build()
            .name("requests_in").help("All internet requests received")
            .labelNames("protocol").register();
    protected static final Counter requestsOut = Counter.build()
            .name("requests_out").help("All internet requests received")
            .labelNames("protocol", "rcode", "rrtype").register();
    protected static final Histogram requestDurations = Histogram.build()
            .buckets(0.005, 0.01, 0.1, 0.2, 0.3, 0.4, 0.5, 0.75, 1, 1.5, 2, 3, 4, 5, 10)
            .name("request_duration").help("How long requests take, from receipt to response.")
            .labelNames("source").register();

    protected final UUID id;
    private final Histogram.Timer requestTimer;

    private final List<RequestListener> listeners;

    public Request() {
        id = UUID.randomUUID();
        listeners = new ArrayList<>();
        requestTimer = requestDurations.labels(isLocal() ? isSubquery() ? "internal" : "trace" : "external")
        .startTimer();
    }

    public abstract Message getMessage();

    public abstract void answer(Message m);

    public Optional<InetAddress> getRemoteAddress() {
        return Optional.empty();
    }

    public void addListener(RequestListener l) {
        listeners.add(l);
    }

    protected void recordAnswer(Message m, String requestProtocol) {
        requestTimer.observeDuration();
        QType qType = m.getQuestions().get(0).getqType();
        String type;
        if(m.getQuestions().size() > 1) {
            type = "<mult>";
        } else if(qType instanceof KnownRRType) {
            type = qType.getType();
        } else if(qType instanceof QOnlyType) {
            type = qType.getType();
        } else {
            type = "<unk>";
        }
        requestsOut.labels(requestProtocol, m.getHeader().getRCode().name().toLowerCase(), type)
                .inc();
    }

    public UUID getId() {
        return id;
    }

    public boolean isSubquery() {
        return false;
    }

    public boolean isLocal() { return false; }

    public int getSubqueryDepth() {
        return 0;
    }

    public QSet getParentQSet() {
        return new QSet();
    }

    public abstract boolean transportIsTruncating();

    public void forEachListener(Consumer<? super RequestListener> action) {
        listeners.forEach(action);
    }
}
