package cafe.josh.comfydns.rfc1035.field.rr.rdata;

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
}
