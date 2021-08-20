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
    private int curIndex;

    public Tracer() {
        this.entries = new ArrayList<>();
        this.curIndex = 0;
    }

    @Override
    public void onAnswerAdded(RR<?> rr) {
        entries.add(new TraceEntry.AnswerAddedEntry(curIndex, rr));
        curIndex++;
    }

    @Override
    public void onUpstreamQuerySent(Message m, SList.SListServer bestServer) {
        entries.add(new TraceEntry.UpstreamQuerySentEntry(curIndex, m, bestServer.getHostname()));
        curIndex++;
    }

    @Override
    public void onUpstreamQueryResult(SList.SListServer serverQueried, Optional<Message> m, Optional<Throwable> t) {
        if(m.isPresent()) {
            entries.add(new TraceEntry.UpstreamQueryResultEntry(curIndex, m.get(), serverQueried.getHostname()));
            curIndex++;
        } else if (t.isPresent()){
            entries.add(new TraceEntry.UpstreamQueryResultEntry(curIndex, t.get(), serverQueried.getHostname()));
            curIndex++;
        } else {
            throw new RuntimeException("Both message and throwable were null.");
        }
    }

    @Override
    public void onSNameChange(String oldSName, String newSName) {
        entries.add(new TraceEntry.SNameChangeEntry(curIndex, oldSName, newSName));
        curIndex++;
    }

    @Override
    public void onSubquerySent(Message m) {
        entries.add(new TraceEntry.SubquerySentEntry(curIndex, m));
        curIndex++;
    }

    @Override
    public void onSubqueryResult(Message m) {
        entries.add(new TraceEntry.SubqueryResultEntry(curIndex, m));
        curIndex++;
    }

    @Override
    public void onResponse(Message m) {
        entries.add(new TraceEntry.ResponseEntry(curIndex, m));
        curIndex++;
    }

    @Override
    public void onException(Throwable t) {
        entries.add(new TraceEntry.ExceptionEntry(curIndex, t));
        curIndex++;
    }

    @Override
    public void remark(String s) {
        entries.add(new TraceEntry.RemarkEntry(curIndex, s));
        curIndex++;
    }

    @Override
    public void onNegativeCacheUse(String sName, QType qType, QClass qClass) {
        entries.add(new TraceEntry.NegativeCacheUseEntry(curIndex, sName, qType, qClass));
        curIndex++;
    }

    public List<TraceEntry> getEntries() {
        return entries;
    }
}
