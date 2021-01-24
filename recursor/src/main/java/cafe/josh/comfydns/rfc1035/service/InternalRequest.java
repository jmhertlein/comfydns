package cafe.josh.comfydns.rfc1035.service;

import cafe.josh.comfydns.rfc1035.message.struct.Message;

import java.util.function.Consumer;

public class InternalRequest implements Request {
    private final Message request;
    private final Consumer<Message> onAnswer;

    public InternalRequest(Message request, Consumer<Message> onAnswer) {
        this.request = request;
        this.onAnswer = onAnswer;
    }

    @Override
    public Message getMessage() {
        return request;
    }

    @Override
    public void answer(Message m) {
        onAnswer.accept(m);
    }
}
