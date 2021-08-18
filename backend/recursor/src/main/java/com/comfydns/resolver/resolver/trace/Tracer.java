package com.comfydns.resolver.resolver.trace;

import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.request.RequestListener;
import com.comfydns.resolver.resolver.rfc1035.service.search.SList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Tracer implements RequestListener {
    private final List<TraceEntry> entries;

    public Tracer() {
        this.entries = new ArrayList<>();
    }

    @Override
    public void onAnswerAdded(RR<?> rr) {
        entries.add(new TraceEntry.AnswerAddedEntry(rr));
    }

    @Override
    public void onUpstreamQuerySent(Message m, SList.SListServer bestServer) {
        entries.add(new TraceEntry.UpstreamQuerySentEntry(m, bestServer.getHostname()));
    }

    @Override
    public void onUpstreamQueryResult(SList.SListServer serverQueried, Optional<Message> m, Optional<Throwable> t) {
        if(m.isPresent()) {
            entries.add(new TraceEntry.UpstreamQueryResultEntry(m.get(), serverQueried.getHostname()));
        } else if (t.isPresent()){
            entries.add(new TraceEntry.UpstreamQueryResultEntry(t.get(), serverQueried.getHostname()));
        } else {
            throw new RuntimeException("Both message and throwable were null.");
        }
    }

    @Override
    public void onSNameChange(String oldSName, String newSName) {
        entries.add(new TraceEntry.SNameChangeEntry(oldSName, newSName));
    }

    @Override
    public void onSubquerySent(Message m) {
        entries.add(new TraceEntry.SubquerySentEntry(m));
    }

    @Override
    public void onSubqueryResult(Message m) {
        entries.add(new TraceEntry.SubqueryResultEntry(m));
    }

    @Override
    public void onResponse(Message m) {
        entries.add(new TraceEntry.ResponseEntry(m));
    }

    @Override
    public void onException(Throwable t) {
        entries.add(new TraceEntry.ExceptionEntry(t));
    }

    @Override
    public void remark(String s) {
        entries.add(new TraceEntry.RemarkEntry(s));
    }

    @Override
    public void onNegativeCacheUse(String sName, QType qType, QClass qClass) {
        entries.add(new TraceEntry.NegativeCacheUseEntry(sName, qType, qClass));
    }

    public List<TraceEntry> getEntries() {
        return entries;
    }
}
