package cafe.josh.comfydns.rfc1035.struct;

import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.LabelMaker;
import cafe.josh.comfydns.rfc1035.field.query.QClass;
import cafe.josh.comfydns.rfc1035.field.query.QType;
import cafe.josh.comfydns.rfc1035.write.Writeable;

public class Question implements Writeable {
    private final String qName;
    private final QType qType;
    private final QClass qClass;

    public Question(String qName, QType qType, QClass qClass) {
        this.qName = qName;
        this.qType = qType;
        this.qClass = qClass;
    }

    public String getqName() {
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
}
