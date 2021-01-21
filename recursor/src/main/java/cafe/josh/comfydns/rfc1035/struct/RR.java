package cafe.josh.comfydns.rfc1035.struct;

import cafe.josh.comfydns.PrettyByte;
import cafe.josh.comfydns.RangeCheck;
import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.LabelMaker;
import cafe.josh.comfydns.rfc1035.field.rr.RData;
import cafe.josh.comfydns.rfc1035.field.rr.RRClass;
import cafe.josh.comfydns.rfc1035.field.rr.RRType;
import cafe.josh.comfydns.rfc1035.write.Writeable;

import java.nio.ByteBuffer;

public class RR<T extends RData> implements Writeable {
    private final String name;
    private final RRType rrType;
    private final RRClass rrClass;
    private final int ttl;
    private final T tData;

    public RR(String name, RRType rrType, RRClass rrClass, int ttl, T tData) {
        this.name = name;
        this.rrType = rrType;
        this.rrClass = rrClass;
        if(!RangeCheck.uint(32, ttl)) {
            throw new IllegalArgumentException("ttl must be 32-bit unsigned int");
        }
        this.ttl = ttl;
        this.tData = tData;
    }

    public String getName() {
        return name;
    }

    public RRType getRrType() {
        return rrType;
    }

    public RRClass getRrClass() {
        return rrClass;
    }

    public int getTtl() {
        return ttl;
    }

    public T getTData() {
        return tData;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] NAME = LabelMaker.makeLabels(name, c);
        c.addSuffixes(name, index);
        index += NAME.length;

        // TODO maybe consolidate all these small fixed-length buffers into one and write directly into it?
        byte[] RRTYPE = rrType.getValue();
        byte[] RRCLASS = rrClass.getValue();
        byte[] TTL;
        {
            ByteBuffer ttlBuffer = ByteBuffer.allocate(4);
            ttlBuffer.putInt(ttl);
            TTL = ttlBuffer.array();
        }
        byte[] RDLENGTH = new byte[2];

        index += RRTYPE.length + RRCLASS.length + TTL.length + RDLENGTH.length;

        byte[] RDATA = this.tData.write(c, index);

        PrettyByte.writeNBitUnsignedInt(RDATA.length, 16, RDLENGTH, 0, 0);

        int length = NAME.length + RRTYPE.length + RRCLASS.length + TTL.length + RDLENGTH.length + RDATA.length;
        byte[] ret = new byte[length];
        PrettyByte.copyAll(ret, 0, NAME, RRTYPE, RRCLASS, TTL, RDLENGTH, RDATA);
        return ret;
    }

    public static RR<?> read(byte[] bytes, int pos) {

        return null;
    }
}
