package cafe.josh.comfydns.rfc1035.struct;

import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.write.Writeable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public byte[] write(LabelCache c) {
        ArrayList<byte[]> parts = new ArrayList<>();
        int index = 0;
        parts.add(getHeader().write(c, index));
        index += parts.get(parts.size()-1).length;

        if(getHeader().getQDCount() != getQuestions().size()) {
            throw new IllegalArgumentException("Header QDCOUNT (" + getHeader().getQDCount() + ") =/= questions length (" + getQuestions().size() + ")");
        }

        for(Question q : getQuestions()) {
            parts.add(q.write(c, index));
            index += parts.get(parts.size()-1).length;
        }

        for(RR<?> rr : getAnswerRecords()) {
            parts.add(rr.write(c, index));
            index += parts.get(parts.size()-1).length;
        }

        for(RR<?> rr : getAuthorityRecords()) {
            parts.add(rr.write(c, index));
            index += parts.get(parts.size()-1).length;
        }

        for(RR<?> rr : getAdditionalRecords()) {
            parts.add(rr.write(c, index));
            index += parts.get(parts.size()-1).length;
        }

        int len = parts.stream().mapToInt(a -> a.length).sum();
        byte[] ret = new byte[len];
        int i = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, ret, i, part.length);
            i += part.length;
        }

        return ret;
    }
}
