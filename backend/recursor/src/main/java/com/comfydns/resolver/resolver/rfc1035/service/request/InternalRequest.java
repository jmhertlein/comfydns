package com.comfydns.resolver.resolver.rfc1035.service.request;

import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.search.QSet;

import java.util.function.Consumer;

public class InternalRequest extends LiveRequest {
    private final Message request;
    private final LiveRequest parent;
    private final int subqueryDepth;
    private final QSet parentQSet;

    public InternalRequest(Message request, LiveRequest parent,
                           QSet parentQSet) {
        this.request = request;
        this.parent = parent;
        subqueryDepth = parent.getSubqueryDepth() + 1;

        this.parentQSet = parentQSet;
    }

    @Override
    public Message getMessage() {
        return request;
    }

    @Override
    protected String getRequestProtocolMetricsTag() {
        return "internal";
    }

    @Override
    public boolean isSubquery() {
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

    public LiveRequest getParent() {
        return parent;
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
