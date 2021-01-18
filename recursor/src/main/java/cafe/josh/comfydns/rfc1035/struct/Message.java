package cafe.josh.comfydns.rfc1035.struct;

import java.util.ArrayList;
import java.util.List;

public class Message {
    private Header header;
    private final List<Question> questions;
    private final List<RR<?>> answerRecords;
    private final List<RR<?>> authorityRecords;
    private final List<RR<?>> additionalRecords;

    public Message() {
        this.questions = new ArrayList<>();
        this.answerRecords = new ArrayList<>();
        this.authorityRecords = new ArrayList<>();
        this.additionalRecords = new ArrayList<>();
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<RR<?>> getAnswerRecords() {
        return answerRecords;
    }

    public List<RR<?>> getAuthorityRecords() {
        return authorityRecords;
    }

    public List<RR<?>> getAdditionalRecords() {
        return additionalRecords;
    }
}
