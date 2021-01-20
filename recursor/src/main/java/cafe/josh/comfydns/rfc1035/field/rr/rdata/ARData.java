package cafe.josh.comfydns.rfc1035.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.field.rr.RData;
import cafe.josh.comfydns.rfc1035.field.rr.RRType;

import java.net.Inet4Address;

public class ARData implements RData {
    private final Inet4Address address;

    public ARData(Inet4Address address) {
        this.address = address;
    }

    public Inet4Address getAddress() {
        return address;
    }

    @Override
    public RRType getRRType() {
        return RRType.A;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        return address.getAddress();
    }
}
