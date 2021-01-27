package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.butil.PrettyByte;
import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.LabelMaker;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;

import java.util.Objects;

public class SOARData implements RData {
    private final String mName, rName;
    private final long serial, refresh, retry, expire, minimum;

    public SOARData(String mName, String rName, long serial, long refresh, long retry, long expire, long minimum) {
        this.mName = mName;
        this.rName = rName;
        this.serial = serial;
        this.refresh = refresh;
        this.retry = retry;
        this.expire = expire;
        this.minimum = minimum;
    }

    public String getMName() {
        return mName;
    }

    public String getrName() {
        return rName;
    }

    public long getSerial() {
        return serial;
    }

    public long getRefresh() {
        return refresh;
    }

    public long getRetry() {
        return retry;
    }

    public long getExpire() {
        return expire;
    }

    public long getMinimum() {
        return minimum;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.SOA;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] MNAME = LabelMaker.makeLabels(mName, c);
        c.addSuffixes(mName, index);
        index += MNAME.length;

        byte[] RNAME = LabelMaker.makeLabels(rName, c);
        c.addSuffixes(rName, index);
        index += RNAME.length;

        byte[] ret = new byte[MNAME.length + RNAME.length + (4 * 5)];

        int pos = 0;
        System.arraycopy(MNAME, 0, ret, pos, MNAME.length);
        pos += MNAME.length;
        System.arraycopy(RNAME, 0, ret, pos, RNAME.length);
        pos += RNAME.length;

        for(long l : new long[]{serial, refresh, retry, expire, minimum}) {
            PrettyByte.writeNBitUnsignedInt(l, 32, ret, index, 0);
            pos += 4;
        }

        if(pos != ret.length) {
            throw new RuntimeException("pos (" + pos + ") != ret.length (" + ret.length + ")");
        }

        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SOARData SOARData = (SOARData) o;
        return serial == SOARData.serial && refresh == SOARData.refresh && retry == SOARData.retry && expire == SOARData.expire && minimum == SOARData.minimum && mName.equals(SOARData.mName) && rName.equals(SOARData.rName);
    }

    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        LabelMaker.ReadLabels readMName = LabelMaker.readLabels(content, pos);
        pos += readMName.length;
        LabelMaker.ReadLabels readRName = LabelMaker.readLabels(content, pos);

        long serial = PrettyByte.readNBitUnsignedInt(32, content, pos, 0);
        pos += 4;
        long refresh = PrettyByte.readNBitUnsignedInt(32, content, pos, 0);
        pos += 4;
        long retry = PrettyByte.readNBitUnsignedInt(32, content, pos, 0);
        pos += 4;
        long expire = PrettyByte.readNBitUnsignedInt(32, content, pos, 0);
        pos += 4;
        long minimum = PrettyByte.readNBitUnsignedInt(32, content, pos, 0);
        pos += 4;

        return new SOARData(readMName.name, readRName.name, serial, refresh, retry, expire, minimum);
    }

    @Override
    public String toString() {
        return String.format("MNAME: %s, RNAME: %s, SERIAL: %s, \nREFRESH: %s, RETRY: %s, EXPIRE: %s, MINIMUM: %s",
                mName, rName, serial, refresh, retry, expire, minimum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mName, rName, serial, refresh, retry, expire, minimum);
    }
}
