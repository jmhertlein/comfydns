package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;

import java.net.Inet6Address;
import java.net.UnknownHostException;

public class AAAARData implements RData {
    private final Inet6Address address;

    public AAAARData(Inet6Address address) {
        this.address = address;
    }

    public Inet6Address getAddress() {
        return address;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.AAAA;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        return address.getAddress();
    }

    @Override
    public String toString() {
        return "address: " + address.getHostAddress();
    }

    public static AAAARData read(byte[] content, int pos, int rdlength) throws InvalidMessageException {
        byte[] addr = new byte[16];
        System.arraycopy(content, pos, addr, 0, 16);
        try {
            return new AAAARData((Inet6Address) Inet6Address.getByAddress(addr));
        } catch (UnknownHostException e) {
            throw new InvalidMessageException("IPv6 address too many bits", e);
        }
    }
}
