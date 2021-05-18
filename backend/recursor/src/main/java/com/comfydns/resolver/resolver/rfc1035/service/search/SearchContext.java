package com.comfydns.resolver.resolver.rfc1035.service.search;

import com.comfydns.resolver.resolver.rfc1035.cache.impl.OverlayCache;
import com.comfydns.resolver.resolver.rfc1035.cache.RRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.TemporaryDNSCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.TXTRData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.request.Request;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchContext {
    public static final int SUB_QUERY_COUNT_LIMIT = 100;
    private final Request request;
    private final AtomicInteger questionIndex;
    private final List<RR<?>> answer, authority, additional;
    private Boolean answerAuthoritative;
    private final QSet qSet;

    private int subQueriesMade;

    private String sName;
    private final SList sList;

    private final TemporaryDNSCache requestCache;
    private final RRSource overlay;

    private final String requestLogPrefix;

    public SearchContext(Request req, RRSource globalCache, QSet parentQSet) {
        this.request = req;
        this.questionIndex = new AtomicInteger(0);
        this.answer = new ArrayList<>();
        this.authority = new ArrayList<>();
        this.additional = new ArrayList<>();

        this.qSet = parentQSet;

        sList = new SList();
        setsName(getCurrentQuestion().getQName());
        subQueriesMade = 0;


        this.requestCache = new TemporaryDNSCache();
        this.overlay = new OverlayCache(requestCache, globalCache);
        answerAuthoritative = true;

        requestLogPrefix = "[" + req.getId() + "]: ";
    }

    public void updateAnswerAuthoritative(boolean aa) {
        // null -> false OK
        // null -> true OK
        // false -> true NO
        // true -> false OK
        if(answerAuthoritative != null && !answerAuthoritative && aa) {
            return;
        }

        answerAuthoritative = aa;
    }

    public boolean isInQset(InetAddress server, Question q) {
        return qSet.contains(server, q);
    }

    public void addToQSet(InetAddress server, Question q) {
        qSet.add(server, q);
    }

    public void incrementSubQueriesMade() {
        subQueriesMade++;
    }

    public int getSubQueriesMade() {
        return subQueriesMade;
    }

    public Question getCurrentQuestion() {
        return request.getMessage().getQuestions().get(questionIndex.get());
    }

    public SList getSList() {
        return sList;
    }

    public Request getRequest() {
        return request;
    }

    public TemporaryDNSCache getRequestCache() {
        return requestCache;
    }

    public RRSource getOverlay() {
        return overlay;
    }

    public AtomicInteger getQuestionIndex() {
        return questionIndex;
    }

    public void nextQuestion() {
        questionIndex.incrementAndGet();
        if(!allQuestionsAnswered()) {
            sName = getCurrentQuestion().getQName();
        } else {
            sName = null;
        }
    }

    public boolean allQuestionsAnswered() {
        return questionIndex.get() >= request.getMessage().getHeader().getQDCount();
    }

    public Collection<RR<?>> getAnswer() {
        return answer;
    }

    public Collection<RR<?>> getAuthority() {
        return authority;
    }

    public Collection<RR<?>> getAdditional() {
        return additional;
    }

    public void addAnswerRR(RR<?> rr) {
        answer.add(rr.changeNamesToQuestionCase(getCurrentQuestion()));
    }

    public Message buildResponse() {
        Message m = new Message();
        Header h = new Header(request.getMessage().getHeader());
        h.setQR(true);
        h.setRA(true);
        h.setAA(answerAuthoritative);
        h.setQDCount(request.getMessage().getHeader().getQDCount());
        h.setANCount(getAnswer().size());
        h.setNSCount(getAuthority().size());
        h.setARCount(getAdditional().size());
        m.setHeader(h);
        m.getQuestions().addAll(request.getMessage().getQuestions());
        m.getAnswerRecords().addAll(getAnswer());
        m.getAuthorityRecords().addAll(getAuthority());
        m.getAdditionalRecords().addAll(getAdditional());
        return m;
    }

    public Message buildNameErrorResponse() {
        Message m = new Message();
        Header h = new Header(request.getMessage().getHeader());
        h.setRCode(RCode.NAME_ERROR);
        h.setQR(true);
        h.setRA(true);
        h.setARCount(0);
        h.setNSCount(0);
        h.setANCount(0);
        m.setHeader(h);
        m.getQuestions().addAll(request.getMessage().getQuestions());
        return m;
    }

    public void sendNameError() {
        Message m = new Message();
        Header h = new Header(request.getMessage().getHeader());
        h.setRCode(RCode.NAME_ERROR);
        h.setQR(true);
        h.setRA(true);
        h.setARCount(0);
        h.setNSCount(0);
        h.setANCount(0);
        m.setHeader(h);
        m.getQuestions().addAll(request.getMessage().getQuestions());
        request.answer(m);
    }

    public void sendOops(String message) {
        Message m = new Message();
        Header h = new Header(request.getMessage().getHeader());
        h.setRCode(RCode.SERVER_FAILURE);
        h.setQR(true);
        h.setRA(true);
        h.setARCount(1);
        h.setANCount(0);
        h.setNSCount(0);
        m.setHeader(h);
        m.getQuestions().addAll(request.getMessage().getQuestions());
        m.getAdditionalRecords().add(
                new RR<>(
                        request.getMessage().getQuestions().get(0).getQName(),
                        KnownRRType.TXT,
                        KnownRRClass.IN,
                        0,
                        new TXTRData(message)
                )
        );
        request.answer(m);
    }

    public void setsName(String sName) {
        this.sName = sName.toLowerCase();
    }

    public String getSName() {
        return sName;
    }

    public void sendNotImplemented() {
        Message m = new Message();
        Header h = new Header(request.getMessage().getHeader());
        h.setRCode(RCode.NOT_IMPLEMENTED);
        h.setQR(true);
        h.setRA(true);
        m.setHeader(h);
        m.getQuestions().addAll(request.getMessage().getQuestions());
        request.answer(m);
    }

    public QSet getQSet() {
        return qSet;
    }

    public void removeFromQSet(InetAddress ip, Question question) {
        qSet.remove(ip, question);
    }

    public String getRequestLogPrefix() {
        return requestLogPrefix;
    }

    public void sendRefusedResponse(String reason) {
        Message m = new Message();
        Header h = new Header(request.getMessage().getHeader());
        h.setRCode(RCode.REFUSED);
        h.setQR(true);
        h.setRA(true);
        m.setHeader(h);
        m.getQuestions().addAll(request.getMessage().getQuestions());
        h.setARCount(1);
        m.getAdditionalRecords().add(
                new RR<>(
                        getSName(),
                        KnownRRType.TXT,
                        KnownRRClass.IN,
                        0,
                        new TXTRData(reason)
                )
        );
        request.answer(m);
    }
}
