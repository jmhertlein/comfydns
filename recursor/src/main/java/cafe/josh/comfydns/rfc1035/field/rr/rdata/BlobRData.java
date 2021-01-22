package cafe.josh.comfydns.rfc1035.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.field.rr.RData;

public class BlobRData implements RData {
    private final byte[] data;

    public BlobRData(byte[] data) {
        this.data = data;
    }

    @Override
    public KnownRRType getRRType() {
        return null;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        return data;
    }

    public static BlobRData read(byte[] content, int pos, int rdlength) {
        byte[] d = new byte[rdlength];
        System.arraycopy(content, pos, d, 0, rdlength);
        return new BlobRData(d);
    }
}
