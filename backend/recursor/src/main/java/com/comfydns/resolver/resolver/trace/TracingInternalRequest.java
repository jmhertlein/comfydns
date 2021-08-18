package com.comfydns.resolver.resolver.trace;

import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.request.Request;
import com.comfydns.resolver.resolver.rfc1035.service.search.QSet;

import java.util.function.Consumer;

public class TracingInternalRequest extends Request {
    private final Message request;
    private final Consumer<Message> onAnswer;
    private final Tracer tracer;

    public TracingInternalRequest(Message request, Consumer<Message> onAnswer) {
        this.request = request;
        this.onAnswer = onAnswer;
        requestsIn.labels("trace").inc();
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
    public void answer(Message m) {
        onAnswer.accept(m);
        this.recordAnswer(m, "trace");
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