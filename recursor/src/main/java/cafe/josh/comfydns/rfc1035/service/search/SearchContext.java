package cafe.josh.comfydns.rfc1035.service.search;

import cafe.josh.comfydns.rfc1035.cache.OverlayCache;
import cafe.josh.comfydns.rfc1035.cache.RRSource;
import cafe.josh.comfydns.rfc1035.cache.TemporaryDNSCache;
import cafe.josh.comfydns.rfc1035.message.field.header.RCode;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.TXTRData;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.request.Request;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchContext {
    private final Request request;
    private final AtomicInteger questionIndex;
    private final ConcurrentLinkedQueue<RR<?>> answer, authority, additional;

    private String sName;
    private final SList sList;

    private final TemporaryDNSCache requestCache;
    private final RRSource overlay;

    public SearchContext(Request req, RRSource globalCache) {
        this.request = req;
        this.questionIndex = new AtomicInteger(0);
        this.answer = new ConcurrentLinkedQueue<>();
        this.authority = new ConcurrentLinkedQueue<>();
        this.additional = new ConcurrentLinkedQueue<>();

        sList = new SList();
        sName = getCurrentQuestion().getQName();


        this.requestCache = new TemporaryDNSCache();
        this.overlay = new OverlayCache(requestCache, globalCache);
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

    public void sendAnswer() {
        Message m = new Message();
        Header h = new Header(request.getMessage().getHeader());
        h.setQR(true);
        h.setRA(true);
        h.setQDCount(request.getMessage().getHeader().getQDCount());
        h.setANCount(getAnswer().size());
        h.setNSCount(getAuthority().size());
        h.setARCount(getAdditional().size());
        m.setHeader(h);
        m.getQuestions().addAll(request.getMessage().getQuestions());
        m.getAnswerRecords().addAll(getAnswer());
        m.getAuthorityRecords().addAll(getAuthority());
        m.getAdditionalRecords().addAll(getAdditional());
        request.answer(m);
    }

    public void sendNameError() {
        Message m = new Message();
        Header h = new Header(request.getMessage().getHeader());
        h.setRCode(RCode.NAME_ERROR);
        h.setQR(true);
        h.setRA(true);
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
        m.setHeader(h);
        m.getQuestions().addAll(request.getMessage().getQuestions());
        m.getAdditionalRecords().add(
                new RR<>(
                        getCurrentQuestion().getQName(),
                        KnownRRType.TXT,
                        KnownRRClass.IN,
                        0,
                        new TXTRData(message)
                )
        );
        request.answer(m);
    }

    public void setsName(String sName) {
        this.sName = sName;
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
}
