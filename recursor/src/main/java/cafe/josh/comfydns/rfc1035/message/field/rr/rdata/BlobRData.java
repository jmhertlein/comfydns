package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;

import java.util.Arrays;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlobRData blobRData = (BlobRData) o;
        return Arrays.equals(data, blobRData.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
