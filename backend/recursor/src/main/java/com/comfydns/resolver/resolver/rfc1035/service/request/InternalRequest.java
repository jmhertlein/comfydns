package com.comfydns.resolver.resolver.rfc1035.service.request;

import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.search.QSet;

import java.util.function.Consumer;

public class InternalRequest extends Request {
    private final Message request;
    private final Consumer<Message> onAnswer;
    private final Request parent;
    private final int subqueryDepth;
    private final QSet parentQSet;

    public InternalRequest(Message request, Consumer<Message> onAnswer, Request parent,
                           QSet parentQSet) {
        this.request = request;
        this.onAnswer = onAnswer;
        this.parent = parent;
        requestsIn.labels("internal").inc();
        subqueryDepth = parent.getSubqueryDepth() + 1;

        this.parentQSet = parentQSet;
    }

    @Override
    public Message getMessage() {
        return request;
    }

    @Override
    public void answer(Message m) {
        onAnswer.accept(m);
        this.recordAnswer(m, "internal");
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public int getSubqueryDepth() {
        return subqueryDepth;
    }

    @Override
    public QSet getParentQSet() {
        return parentQSet;
    }

    public Request getParent() {
        return parent;
    }

    @Override
    public boolean transportIsTruncating() {
        return false;
    }
}
