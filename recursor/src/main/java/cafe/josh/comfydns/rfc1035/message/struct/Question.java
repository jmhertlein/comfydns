package cafe.josh.comfydns.rfc1035.message.struct;

import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.LabelMaker;
import cafe.josh.comfydns.rfc1035.message.MalformedLabelException;
import cafe.josh.comfydns.rfc1035.message.field.query.QClass;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;
import cafe.josh.comfydns.rfc1035.message.write.Writeable;

public class Question implements Writeable {
    private final String qName;
    private final QType qType;
    private final QClass qClass;

    public Question(String qName, QType qType, QClass qClass) {
        this.qName = qName;
        this.qType = qType;
        this.qClass = qClass;
    }

    public String getQName() {
        return qName;
    }

    public QType getqType() {
        return qType;
    }

    public QClass getqClass() {
        return qClass;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] QNAME = LabelMaker.makeLabels(qName, c);
        c.addSuffixes(qName, index);
        byte[] ret = new byte[QNAME.length + 4];
        System.arraycopy(QNAME, 0, ret, 0, QNAME.length);
        System.arraycopy(qType.getValue(), 0, ret, QNAME.length, 2);
        System.arraycopy(qClass.getValue(), 0, ret, QNAME.length+2, 2);
        return ret;
    }

    @Override
    public String toString() {
        return String.format("QNAME: %s, QTYPE: %s, QCLASS: %s", qName, qType, qClass);
    }

    public static ReadQuestion read(byte[] content, int startPos) throws MalformedLabelException {
        int pos = startPos;
        LabelMaker.ReadLabels readLabels = LabelMaker.readLabels(content, pos);
        pos = readLabels.zeroOctetPosition + 1;
        QType QTYPE = QType.match(content, pos);
        pos += 2;
        QClass QCLASS = QClass.match(content, pos);
        pos += 2;

        return new ReadQuestion(new Question(readLabels.name, QTYPE, QCLASS), pos - startPos);
    }

    public static class ReadQuestion {
        public final Question read;
        public final int length;

        public ReadQuestion(Question read, int length) {
            this.read = read;
            this.length = length;
        }
    }
}
