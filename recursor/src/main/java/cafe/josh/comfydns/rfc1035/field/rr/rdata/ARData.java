package cafe.josh.comfydns.rfc1035.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.field.rr.RData;

import java.net.Inet4Address;
import java.net.UnknownHostException;

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
}
