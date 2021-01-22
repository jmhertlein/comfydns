package cafe.josh.comfydns.rfc1035.struct;

import cafe.josh.comfydns.rfc1035.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.UnsupportedRRTypeException;
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

    public static Message read(byte[] bytes) throws InvalidMessageException, UnsupportedRRTypeException {
        if(bytes.length < Header.FIXED_LENGTH_OCTETS) {
            throw new InvalidMessageException("Message too short to have valid header. Must be " + Header.FIXED_LENGTH_OCTETS + " octets but only found " + bytes.length);
        }

        byte[] headerContents = new byte[Header.FIXED_LENGTH_OCTETS];
        System.arraycopy(bytes, 0, headerContents, 0, Header.FIXED_LENGTH_OCTETS);
        Header h = new Header(headerContents);
        h.validate();

        int pos = Header.FIXED_LENGTH_OCTETS;

        List<Question> questions = new ArrayList<>();
        for(int i = 0; i < h.getQDCount(); i++) {
            Question.ReadQuestion read = Question.read(bytes, pos);
            pos += read.length;
            questions.add(read.read);
        }

        List<RR<?>> answers = new ArrayList<>(), authorities = new ArrayList<>(), additional = new ArrayList<>();
        for(int i = 0; i < h.getANCount(); i++) {
            RR.ReadRR<?> readRR = RR.read(bytes, pos);
            answers.add(readRR.read);
            pos += readRR.length;
        }

        for(int i = 0; i < h.getNSCount(); i++) {
            RR.ReadRR<?> readRR = RR.read(bytes, pos);
            authorities.add(readRR.read);
            pos += readRR.length;
        }

        for(int i = 0; i < h.getARCount(); i++) {
            RR.ReadRR<?> readRR = RR.read(bytes, pos);
            additional.add(readRR.read);
            pos += readRR.length;
        }

        Message m = new Message();
        m.setHeader(h);
        m.getQuestions().addAll(questions);
        m.getAnswerRecords().addAll(answers);
        m.getAuthorityRecords().addAll(authorities);
        m.getAdditionalRecords().addAll(additional);

        return m;
    }
}
