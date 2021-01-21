package cafe.josh.comfydns.rfc1035.field.rr.rdata;

import cafe.josh.comfydns.PrettyByte;
import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.LabelMaker;
import cafe.josh.comfydns.rfc1035.field.rr.RData;
import cafe.josh.comfydns.rfc1035.field.rr.RRType;

public class SOARecord implements RData {
    private final String mName, rName;
    private final long serial, refresh, retry, expire, minimum;

    public SOARecord(String mName, String rName, long serial, long refresh, long retry, long expire, long minimum) {
        this.mName = mName;
        this.rName = rName;
        this.serial = serial;
        this.refresh = refresh;
        this.retry = retry;
        this.expire = expire;
        this.minimum = minimum;
    }

    public String getmName() {
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
    public RRType getRRType() {
        return RRType.SOA;
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
}
