package com.comfydns.resolver.resolve.rfc1035.message.field.rr.rdata;

import com.comfydns.resolver.resolve.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolve.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.RData;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Objects;

public class ARData implements RData {
    private final Inet4Address address;

    public ARData(Inet4Address address) {
        this.address = address;
    }

    public Inet4Address getAddress() {
        return address;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.A;
    }

    @Override
    public JsonObject writeJson() {
        JsonObject o = new JsonObject();
        o.add("address", new JsonPrimitive(address.getHostAddress()));
        return o;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        return address.getAddress();
    }

    public static ARData read(byte[] content, int pos, int rdlength) throws InvalidMessageException {
        byte[] addr = new byte[4];
        System.arraycopy(content, pos, addr, 0, 4);
        try {
            return new ARData((Inet4Address) Inet4Address.getByAddress(addr));
        } catch (UnknownHostException e) {
            throw new InvalidMessageException("IPv4 address too many bits", e);
        }
    }

    public static ARData read(JsonObject o) throws InvalidMessageException {
        try {
            return new ARData((Inet4Address) Inet4Address.getByName(o.get("address").getAsString()));
        } catch (UnknownHostException e) {
            throw new InvalidMessageException("IPv4 address invalid");
        }
    }

    @Override
    public String toString() {
        return "address: " + address.getHostAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ARData arData = (ARData) o;
        return address.equals(arData.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
