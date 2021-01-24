package cafe.josh.comfydns.rfc1035.service;

import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.RR;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestProgress {
    private final Request request;
    private final AtomicInteger questionIndex;
    private final ConcurrentLinkedQueue<RR<?>> answer, authority, additional;

    public RequestProgress(Request req) {
        this.request = req;
        this.questionIndex = new AtomicInteger(0);
        this.answer = new ConcurrentLinkedQueue<>();
        this.authority = new ConcurrentLinkedQueue<>();
        this.additional = new ConcurrentLinkedQueue<>();
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
}
