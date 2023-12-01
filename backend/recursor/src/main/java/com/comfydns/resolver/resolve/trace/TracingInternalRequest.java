package com.comfydns.resolver.resolve.trace;

import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.service.request.LiveRequest;

public class TracingInternalRequest extends LiveRequest {
    private final Message request;
    private final Tracer tracer;

    public TracingInternalRequest(Message request) {
        this.request = request;
        tracer = new Tracer();
        addListener(tracer);
    }

    public Tracer getTracer() {
        return tracer;
    }

    @Override
    public Message getMessage() {
        return request;
    }

    @Override
    protected String getRequestProtocolMetricsTag() {
        return "trace";
    }


    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean transportIsTruncating() {
        return false;
    }
}
