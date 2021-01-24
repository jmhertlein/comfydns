package cafe.josh.comfydns.rfc1035.service.search;

import cafe.josh.comfydns.rfc1035.message.field.header.RCode;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.TXTRData;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.Request;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchContext {
    private final Request request;
    private final AtomicInteger questionIndex;
    private final ConcurrentLinkedQueue<RR<?>> answer, authority, additional;

    private final SList sList;


    public SearchContext(Request req) {
        this.request = req;
        this.questionIndex = new AtomicInteger(0);
        this.answer = new ConcurrentLinkedQueue<>();
        this.authority = new ConcurrentLinkedQueue<>();
        this.additional = new ConcurrentLinkedQueue<>();

        sList = new SList();
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

    public AtomicInteger getQuestionIndex() {
        return questionIndex;
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
        m.setHeader(h);
        request.answer(m);
    }

    public void sendOops(String message) {
        Message m = new Message();
        Header h = new Header(request.getMessage().getHeader());
        h.setRCode(RCode.SERVER_FAILURE);
        h.setARCount(1);
        m.setHeader(h);
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
}
