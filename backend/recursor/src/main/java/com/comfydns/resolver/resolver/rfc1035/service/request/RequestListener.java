package com.comfydns.resolver.resolver.rfc1035.service.request;

import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.search.SList;

import java.util.Optional;

public interface RequestListener {
    public default void onAnswerAdded(RR<?> rr) {}
    public default void onUpstreamQuerySent(Message m, SList.SListServer bestServer) {}
    public default void onUpstreamQueryResult(SList.SListServer serverQueried, Optional<Message> m, Optional<Throwable> t) {}
    public default void onSNameChange(String oldSName, String newSName) {}
    public default void onSubquerySent(Message m) {}
    public default void onSubqueryResult(Message m) {}

    /**
     * Called exactly once when the Message with the answer is actually being sent to the request.
     * @param m
     */
    public default void onResponse(Message m) {}

    /**
     * Called when processing the request encounters an exception. Usually only gets called once.
     * @param t
     */
    public default void onException(Throwable t) {}
    public default void remark(String s) {}
    public default void onNegativeCacheUse(String sName, QType qType, QClass qClass) {}
}
