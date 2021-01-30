package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.LabelMaker;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;
import cafe.josh.comfydns.rfc1035.message.struct.Question;

public class CNameRData implements RData {
    private final String domainName;

    public CNameRData(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.CNAME;
    }

    public String getDomainName() {
        return domainName;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] ret = LabelMaker.makeLabels(domainName, c);
        c.addSuffixes(domainName, index);
        return ret;
    }

    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        LabelMaker.ReadLabels readLabels = LabelMaker.readLabels(content, pos);
        return new CNameRData(readLabels.name);
    }

    @Override
    public String toString() {
        return "CNAME='" + domainName + "'";
    }
}
