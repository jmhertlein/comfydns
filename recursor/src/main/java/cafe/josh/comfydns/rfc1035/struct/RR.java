package cafe.josh.comfydns.rfc1035.struct;

import cafe.josh.comfydns.PrettyByte;
import cafe.josh.comfydns.RangeCheck;
import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.LabelMaker;
import cafe.josh.comfydns.rfc1035.field.rr.RRClass;
import cafe.josh.comfydns.rfc1035.field.rr.RRType;
import cafe.josh.comfydns.rfc1035.write.Writeable;

public class RR<T> implements Writeable {
    private final String name;
    private final RRType rrType;
    private final RRClass rrClass;
    private final long ttl;
    private final int rdLength;
    private final T tData;

    public RR(String name, RRType rrType, RRClass rrClass, long ttl, int rdLength, T tData) {
        this.name = name;
        this.rrType = rrType;
        this.rrClass = rrClass;
        if(!RangeCheck.uint(32, ttl)) {
            throw new IllegalArgumentException("ttl must be 32-bit unsigned int");
        }
        this.ttl = ttl;
        if(!RangeCheck.uint(16, rdLength)) {
            throw new IllegalArgumentException("rdLength must be 16-bit unsigned int.");
        }
        this.rdLength = rdLength;
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

    public long getTtl() {
        return ttl;
    }

    public int getRdLength() {
        return rdLength;
    }

    public T gettData() {
        return tData;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] NAME = LabelMaker.makeLabel(name, c);
        c.addSuffixes(name, index);
        index += NAME.length;

        byte[] RRTYPE = rrType.getValue();
        byte[] RRCLASS = rrClass.getValue();
        byte[] TTL = new byte[4];
        PrettyByte.writeNBitUnsignedInt(ttl, 32, TTL, 0);
        // TODO why the fuck is TTL a signed int


    }
}
