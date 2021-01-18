package cafe.josh.comfydns.rfc1035.struct;

import cafe.josh.comfydns.rfc1035.field.query.QClass;
import cafe.josh.comfydns.rfc1035.field.query.QType;

public class Question {
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
}
