package com.comfydns.resolver.resolver.trace;

import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.request.LiveRequest;

import java.util.function.Consumer;

public class TracingInternalRequest extends LiveRequest {
    private final Message request;
    private Consumer<Message> onAnswer;
    private final Tracer tracer;

    public TracingInternalRequest(Message request) {
        this.request = request;
        this.onAnswer = (m) -> {};
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
    protected void writeToTransport(Message m) {
        onAnswer.accept(m);
    }

    @Override
    protected String getRequestProtocolMetricsTag() {
        return "trace";
    }

    public void setOnAnswer(Consumer<Message> onAnswer) {
        this.onAnswer = onAnswer;
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
