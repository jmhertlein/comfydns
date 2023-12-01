package com.comfydns.resolver.resolve.rfc1035.message.struct;

import com.comfydns.resolver.resolve.butil.PrettyByte;
import com.comfydns.resolver.resolve.rfc1035.cache.RR2Tuple;
import com.comfydns.resolver.resolve.rfc1035.message.*;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.RRClass;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.rdata.BlobRData;
import com.comfydns.resolver.resolve.rfc1035.message.write.Writeable;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.RData;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.RRType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class RR<T extends RData> implements Writeable {
    private final String name;
    private final RRType rrType;
    private final RRClass rrClass;
    private final int ttl;
    private final T rData;

    private final RR2Tuple classAndType;

    public RR(String name, RRType rrType, RRClass rrClass, int ttl, T rData) {
        this.name = name.toLowerCase();
        this.rrType = rrType;
        this.rrClass = rrClass;
        this.ttl = ttl;
        this.rData = rData;
        this.classAndType = new RR2Tuple(rrClass.getValue(), rrType.getValue());

        assert rrType.getRDataClass().equals(rData.getClass());
    }

    private RR(Question q, RRType rrType, RRClass rrClass, int ttl, T rData) {
        this.name = q.getQName();
        this.rrType = rrType;
        this.rrClass = rrClass;
        this.ttl = ttl;
        this.rData = rData;
        this.classAndType = new RR2Tuple(rrClass.getValue(), rrType.getValue());

        assert rrType.getRDataClass().equals(rData.getClass());
    }

    public RR<T> adjustTTL(OffsetDateTime cachedAt, OffsetDateTime now) {
        long age = cachedAt.until(now, ChronoUnit.SECONDS);
        int newTTL;
        if(age > Integer.MAX_VALUE) {
            newTTL = 0;
        } else {
            newTTL = ttl - ((int) age);
        }
        return new RR<>(name, rrType, rrClass, newTTL, rData);
    }

    public RR<T> changeNamesToQuestionCase(Question q) {
        if(name.equalsIgnoreCase(q.getQName()) && !name.equals(q.getQName())) {
            return new RR<>(q, rrType, rrClass, ttl, rData);
        } else {
            return this;
        }
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

    public T getRData() {
        return rData;
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

        byte[] RDATA = this.rData.write(c, index);

        PrettyByte.writeNBitUnsignedInt(RDATA.length, 16, RDLENGTH, 0, 0);

        int length = NAME.length + RRTYPE.length + RRCLASS.length + TTL.length + RDLENGTH.length + RDATA.length;
        byte[] ret = new byte[length];
        PrettyByte.copyAll(ret, 0, NAME, RRTYPE, RRCLASS, TTL, RDLENGTH, RDATA);
        return ret;
    }

    @Override
    public String toString() {
        return String.format("NAME: %s, TYPE: %s, CLASS: %s, TTL: %s, RDATA:\n %s",
                name, rrType.getType(), rrClass.getType(), ttl, this.rData.toString());
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

    public static RR<?> read(ResultSet rs) throws SQLException, UnsupportedRRTypeException, InvalidMessageException {
        String name = rs.getString("name");
        int rrTypeValue = rs.getInt("rrtype");
        int rrClassValue = rs.getInt("rrclass");
        int ttl = rs.getInt("ttl");
        String rData = rs.getString("rdata");
        Gson gson = new Gson();
        JsonObject o = gson.fromJson(rData, JsonObject.class);
        RRType rrType = RRType.match(rrTypeValue);

        RDataFromJsonFunction jsonCtor;
        if(rrType.isWellKnown()) {
            KnownRRType kType = (KnownRRType) rrType;
            jsonCtor = kType.getJsonCtor();
        } else {
            jsonCtor = BlobRData::new;
        }

        if(jsonCtor == null) {
            throw new RuntimeException("Unsupported known RR type in database.");
        }
        RData rDataObject = jsonCtor.read(o);

        RRClass rrClass = RRClass.match(rrClassValue);

        return new RR<>(name, rrType, rrClass, ttl, rDataObject);
    }

    public RR<T> zeroTTL() {
        return new RR<>(name, rrType, rrClass, 0, rData);
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
        return name.equals(rr.name) && rrType.equals(rr.rrType) && rrClass.equals(rr.rrClass) && rData.equals(rr.rData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rrType, rrClass, rData);
    }

    @SuppressWarnings("unchecked")
    public <RDT extends RData> RR<RDT> cast(Class<RDT> rDataClass) {
        if(rDataClass.equals(this.rrType.getRDataClass()) && rDataClass.equals(this.getRData().getClass())) {
            return (RR<RDT>) this;
        } else {
            throw new RuntimeException("We almost poisoned the heap.");
        }
    }
}
