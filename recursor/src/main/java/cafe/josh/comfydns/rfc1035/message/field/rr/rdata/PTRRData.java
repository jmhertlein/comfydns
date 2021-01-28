package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.LabelMaker;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;

public class PTRRData implements RData {
    private final String ptrDName;

    public PTRRData(String ptrDName) {
        this.ptrDName = ptrDName;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.PTR;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] ret = LabelMaker.makeLabels(ptrDName, c);
        c.addSuffixes(ptrDName, index);
        return ret;
    }


    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        LabelMaker.ReadLabels readLabels = LabelMaker.readLabels(content, pos);
        return new PTRRData(readLabels.name);
    }

    @Override
    public String toString() {
        return "PTRDNAME: " + ptrDName;
    }
}


