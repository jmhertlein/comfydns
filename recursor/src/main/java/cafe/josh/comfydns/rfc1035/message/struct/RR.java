package cafe.josh.comfydns.rfc1035.message.struct;

import cafe.josh.comfydns.butil.PrettyByte;
import cafe.josh.comfydns.butil.RangeCheck;
import cafe.josh.comfydns.rfc1035.cache.RR2Tuple;
import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.LabelMaker;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.rr.*;
import cafe.josh.comfydns.rfc1035.message.write.Writeable;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class RR<T extends RData> implements Writeable {
    private final String name;
    private final RRType rrType;
    private final RRClass rrClass;
    private final int ttl;
    private final T tData;

    private final RR2Tuple classAndType;

    public RR(String name, RRType rrType, RRClass rrClass, int ttl, T tData) {
        this.name = name;
        this.rrType = rrType;
        this.rrClass = rrClass;
        if(!RangeCheck.uint(32, ttl)) {
            throw new IllegalArgumentException("ttl must be 32-bit unsigned int");
        }
        this.ttl = ttl;
        this.tData = tData;
        this.classAndType = new RR2Tuple(rrClass.getValue(), rrType.getValue());
    }

    public RR<T> adjustTTL(OffsetDateTime cachedAt, OffsetDateTime now) {
        long age = cachedAt.until(now, ChronoUnit.SECONDS);
        int newTTL;
        if(age > Integer.MAX_VALUE) {
            newTTL = 0;
        } else {
            newTTL = ttl - ((int) age);
        }
        return new RR<>(name, rrType, rrClass, newTTL, tData);
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

    public RR2Tuple getClassAndType() {
        return classAndType;
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

    @Override
    public String toString() {
        return String.format("NAME: %s, TYPE: %s, CLASS: %s, TTL: %s, RDATA:\n %s",
                name, rrType.getType(), rrClass.getType(), ttl, this.tData.toString());
    }

    public static ReadRR<?> read(byte[] bytes, int startPos) throws InvalidMessageException, UnsupportedRRTypeException {
        int pos = startPos;
        LabelMaker.ReadLabels NAME = LabelMaker.readLabels(bytes, pos);
        pos = NAME.zeroOctetPosition+1;

        RRType TYPE = RRType.match(bytes, pos);
        pos += 2;

        RRClass CLASS = RRClass.match(bytes, pos);
        pos += 2;

        int TTL;
        {
            ByteBuffer tmp = ByteBuffer.wrap(bytes);
            tmp.position(pos);
            TTL = tmp.getInt();
        }
        pos += 4;

        int RDLENGTH = (int) PrettyByte.readNBitUnsignedInt(16, bytes, pos, 0);
        pos += 2;

        RData RDATA;
        if(TYPE.isSupported()) {
            RDATA = TYPE.getCtor().read(bytes, pos, RDLENGTH);
        } else {
            throw new UnsupportedRRTypeException(TYPE.getType());
        }

        pos += RDLENGTH;

        return new ReadRR<>(new RR<>(NAME.name, TYPE, CLASS, TTL, RDATA), pos - startPos);
    }

    public static class ReadRR<T extends RData> {
        public final RR<T> read;
        public final int length;

        public ReadRR(RR<T> read, int length) {
            this.read = read;
            this.length = length;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RR<?> rr = (RR<?>) o;
        return name.equals(rr.name) && rrType.equals(rr.rrType) && rrClass.equals(rr.rrClass) && tData.equals(rr.tData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rrType, rrClass, tData);
    }
}
