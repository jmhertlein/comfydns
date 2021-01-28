package cafe.josh.comfydns.rfc1035.service.request;

import cafe.josh.comfydns.rfc1035.message.struct.Message;

import java.util.function.Consumer;

public class InternalRequest extends Request {
    private final Message request;
    private final Consumer<Message> onAnswer;

    public InternalRequest(Message request, Consumer<Message> onAnswer) {
        this.request = request;
        this.onAnswer = onAnswer;
        requestsIn.labels("internal").inc();
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
}
