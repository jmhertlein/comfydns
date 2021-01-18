package cafe.josh.comfydns.rfc1035.struct;

import cafe.josh.comfydns.RangeCheck;
import cafe.josh.comfydns.rfc1035.field.rr.RRClass;
import cafe.josh.comfydns.rfc1035.field.rr.RRType;

public class RR<T> {
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
}
