package com.comfydns.resolver.resolve.rfc1035.message.struct;

import com.comfydns.resolver.resolve.rfc1035.message.field.header.RCode;

import java.util.List;

/**
 * Indicates an error occurred while reading a message, but there
 * was enough information to return partial data.
 */
public class MessageReadingException extends Exception {
    private final Header readHeader;
    private final List<Question> readQuestions;

    private final RCode suggestedRCode;


    public MessageReadingException(Header readHeader, List<Question> readQuestions, RCode rCode) {
        this.readHeader = readHeader;
        this.readQuestions = readQuestions;
        this.suggestedRCode = rCode;
    }

    public MessageReadingException(Throwable cause, Header readHeader, List<Question> readQuestions, RCode rCode) {
        super(cause);
        this.readHeader = readHeader;
        this.readQuestions = readQuestions;
        this.suggestedRCode = rCode;
    }

    public Header getReadHeader() {
        return readHeader;
    }

    public List<Question> getReadQuestions() {
        return readQuestions;
    }

    public RCode getSuggestedRCode() {
        return suggestedRCode;
    }

    /**
     *
     * @return a message with an unset rcode that is otherwise appropriate to send to respond to the sender whose message failed to read.
     */
    public Message buildResponse() {
        Message m = new Message();
        Header h = new Header(readHeader);
        h.setRA(true);
        m.getQuestions().addAll(readQuestions);
        h.setQDCount(readQuestions.size());
        h.setRCode(suggestedRCode);
        return m;
    }
}
