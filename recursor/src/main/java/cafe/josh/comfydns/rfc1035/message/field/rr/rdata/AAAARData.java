package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AAAARData aaaarData = (AAAARData) o;
        return address.equals(aaaarData.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
