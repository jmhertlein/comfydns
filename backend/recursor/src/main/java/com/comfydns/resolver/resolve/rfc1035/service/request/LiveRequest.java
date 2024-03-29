package com.comfydns.resolver.resolve.rfc1035.service.request;

import com.comfydns.resolver.resolve.logging.EventLogger;
import com.comfydns.resolver.resolve.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolve.rfc1035.message.field.query.QOnlyType;
import com.comfydns.resolver.resolve.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.service.search.QSet;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class LiveRequest {
    private static final Logger log = LoggerFactory.getLogger(LiveRequest.class);
    private static final Counter requestsIn = Counter.build()
            .name("requests_in").help("All internet requests received")
            .labelNames("protocol").register();
    protected static final Counter requestsOut = Counter.build()
            .name("requests_out").help("All internet requests received")
            .labelNames("protocol", "rcode", "rrtype").register();
    protected static final Histogram requestDurations = Histogram.build()
            .buckets(0.005, 0.01, 0.1, 0.2, 0.3, 0.4, 0.5, 0.75, 1, 1.5, 2, 3, 4, 5, 10)
            .name("request_duration").help("How long requests take, from receipt to response.")
            .labelNames("source").register();
//    protected static final Counter requestsLost = Counter.build()
//            .name("requests_lost").help("All internet requests lost (request received, but we didn't manage to send a response).")
//            .labelNames("protocol", "rcode", "rrtype").register();

    protected final UUID id;
    private Histogram.Timer requestTimer;

    private final List<RequestListener> listeners;
    
    private boolean started, answered;

    public LiveRequest() {
        id = UUID.randomUUID();
        listeners = new ArrayList<>();
        answered = false;
        started = false;
    }

    private void checkStarted() {
        if(this.started) {
            throw new IllegalStateException(String.format("Request %s already started, refusing to allow updating.", id));
        }
    }

    public void initialize() {
        this.checkStarted();
        EventLogger.logRequestStart(this);
        this.started = true;
        requestsIn.labels(this.getRequestProtocolMetricsTag()).inc();
        requestTimer = requestDurations.labels(isLocal() ? isSubquery() ? "internal" : "trace" : "external")
                .startTimer();
    }

    public abstract Message getMessage();

    private void checkAnswered() {
        if(this.answered) {
            throw new IllegalStateException(String.format("Request %s already answered, refusing to allow updating to re-answer.", id));
        }
    }
    
    protected void setAnswered() {
        this.checkAnswered();
        this.answered = true;
    }
    
    protected boolean isAnswered() {
        return this.answered;
    }

    public void onAnswer(Message m) {
        if(!this.started) {
            throw new IllegalStateException("We tried to answer a request that was never started: " + this.getId());
        }

        this.checkAnswered();
        this.setAnswered();
        this.recordAnswer(m);
    }

    public Optional<InetAddress> getRemoteAddress() {
        return Optional.empty();
    }

    public void addListener(RequestListener l) {
        listeners.add(l);
    }

    protected abstract String getRequestProtocolMetricsTag();

    private void recordAnswer(Message m) {
        if(m.getHeader().getRCode() == RCode.SERVER_FAILURE) {
            log.info("[R] [{}]: {} | {}", getRemoteAddress(), m.getHeader().getRCode(), id);
        }
        EventLogger.logRequestEnd(this, m.getHeader().getRCode());
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
        requestsOut.labels(this.getRequestProtocolMetricsTag(), m.getHeader().getRCode().name().toLowerCase(), type)
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
