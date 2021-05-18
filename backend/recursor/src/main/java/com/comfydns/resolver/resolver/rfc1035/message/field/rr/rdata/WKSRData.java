package com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata;

import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.RData;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public class WKSRData implements RData {
    private final Inet4Address address;
    private final int protocol;
    private final byte[] bitMap;

    public WKSRData(Inet4Address address, int protocol, byte[] bitMap) {
        this.address = address;
        this.protocol = protocol;
        this.bitMap = bitMap;
    }

    public WKSRData(JsonObject o) throws InvalidMessageException {
        try {
            address = (Inet4Address) Inet4Address.getByName(o.get("address").getAsString());
        } catch (UnknownHostException e) {
            throw new InvalidMessageException("Invalid IP address format", e);
        }
        protocol = o.get("protocol").getAsInt();
        bitMap = Base64.getDecoder().decode(o.get("bitmap").getAsString());
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.WKS;
    }

    @Override
    public JsonObject writeJson() {
        JsonObject o = new JsonObject();
        o.add("address", new JsonPrimitive(address.getHostAddress()));
        o.add("protocol", new JsonPrimitive(protocol));
        o.add("bitmap", new JsonPrimitive(Base64.getEncoder().encodeToString(bitMap)));
        return o;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] out = new byte[4 + 1 + bitMap.length];
        System.arraycopy(address.getAddress(), 0, out, 0, 4);
        out[4] = (byte) protocol;
        System.arraycopy(bitMap, 0, out, 5, bitMap.length);
        return out;
    }

    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        byte[] ip = new byte[4];
        System.arraycopy(content, pos, ip, 0, 4);
        pos += 4;
        int protocol = Byte.toUnsignedInt(content[pos]);
        pos++;
        byte[] bitMap = new byte[rdlength - 4 - 1];
        System.arraycopy(content, pos, bitMap, 0, bitMap.length);

        try {
            return new WKSRData((Inet4Address) Inet4Address.getByAddress(ip), protocol, bitMap);
        } catch (UnknownHostException e) {
            throw new InvalidMessageException("", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WKSRData wksrData = (WKSRData) o;
        return protocol == wksrData.protocol && address.equals(wksrData.address) && Arrays.equals(bitMap, wksrData.bitMap);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(address, protocol);
        result = 31 * result + Arrays.hashCode(bitMap);
        return result;
    }
}
