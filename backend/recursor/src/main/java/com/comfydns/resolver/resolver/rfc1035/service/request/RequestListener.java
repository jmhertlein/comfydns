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
    public default void onSNameChange(String sName) {}
    public default void onSubquerySent(Message m) {}
    public default void onSubqueryResult(Message m) {}
    public default void onResponse(Message m) {}
    public default void onException(Throwable t) {}
    public default void remark(String s) {}
    public default void onNegativeCacheUse(String sName, QType qType, QClass qClass) {}
}
