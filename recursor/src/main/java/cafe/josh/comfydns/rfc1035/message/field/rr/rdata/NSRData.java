package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.LabelMaker;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;

public class NSRData implements RData {
    private final String nsDName;

    public NSRData(String nsDName) {
        this.nsDName = nsDName;
    }

    public String getNsDName() {
        return nsDName;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.NS;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] ret = LabelMaker.makeLabels(nsDName, c);
        c.addSuffixes(nsDName, index);
        return ret;
    }

    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        LabelMaker.ReadLabels readLabels = LabelMaker.readLabels(content, pos);
        return new NSRData(readLabels.name);
    }

    @Override
    public String toString() {
        return "NSDNAME: " + nsDName;
    }
}
