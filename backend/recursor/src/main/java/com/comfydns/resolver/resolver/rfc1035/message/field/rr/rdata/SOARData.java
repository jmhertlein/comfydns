package com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.LabelMaker;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.RData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class SOARData implements RData {
    private final String mName, rName;
    private final long serial, refresh, retry, expire, minimum;

    /**
     *
     * @param mName The domain-name of the name server that was the original or primary source of data for this zone.
     * @param rName A domain-name which specifies the mailbox of the person responsible for this zone.
     * @param serial The unsigned 32 bit version number of the original copy of the zone.  Zone transfers preserve this value.  This value wraps and should be compared using sequence space arithmetic.
     * @param refresh A 32 bit time interval before the zone should be refreshed.
     * @param retry A 32 bit time interval that should elapse before a failed refresh should be retried.
     * @param expire A 32 bit time value that specifies the upper limit on the time interval that can elapse before the zone is no longer authoritative.
     * @param minimum The unsigned 32 bit minimum TTL field that should be exported with any RR from this zone.
     */
    public SOARData(String mName, String rName, long serial, long refresh, long retry, long expire, long minimum) {
        this.mName = mName;
        this.rName = rName;
        this.serial = serial;
        this.refresh = refresh;
        this.retry = retry;
        this.expire = expire;
        this.minimum = minimum;
    }

    public SOARData(JsonObject o) {
        this.mName = o.get("mname").getAsString();
        this.rName = o.get("rname").getAsString();
        this.serial = o.get("serial").getAsLong();
        this.refresh = o.get("refresh").getAsLong();
        this.retry = o.get("retry").getAsLong();
        this.expire = o.get("expire").getAsLong();
        this.minimum = o.get("minimum").getAsLong();
    }

    @Override
    public JsonObject writeJson() {
        JsonObject o = new JsonObject();
        o.add("mname", new JsonPrimitive(mName));
        o.add("rname", new JsonPrimitive(rName));
        o.add("serial", new JsonPrimitive(serial));
        o.add("refresh", new JsonPrimitive(refresh));
        o.add("retry", new JsonPrimitive(retry));
        o.add("expire", new JsonPrimitive(expire));
        o.add("minimum", new JsonPrimitive(minimum));
        return o;
    }

    public String getMName() {
        return mName;
    }

    public String getRName() {
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
        String cleanMName = mName == null ? "" : mName;
        byte[] MNAME = LabelMaker.makeLabels(cleanMName, c);
        c.addSuffixes(cleanMName, index);
        index += MNAME.length;

        String cleanRName = rName == null ? "" : rName;
        byte[] RNAME = LabelMaker.makeLabels(cleanRName, c);
        c.addSuffixes(cleanRName, index);
        index += RNAME.length;

        byte[] ret = new byte[MNAME.length + RNAME.length + (4 * 5)];

        int pos = 0;
        System.arraycopy(MNAME, 0, ret, pos, MNAME.length);
        pos += MNAME.length;
        System.arraycopy(RNAME, 0, ret, pos, RNAME.length);
        pos += RNAME.length;

        for(long l : new long[]{serial, refresh, retry, expire, minimum}) {
            PrettyByte.writeNBitUnsignedInt(l, 32, ret, pos, 0);
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
        pos += readRName.length;

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
