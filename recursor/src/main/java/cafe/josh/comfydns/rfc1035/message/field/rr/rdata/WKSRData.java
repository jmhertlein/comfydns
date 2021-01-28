package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class WKSRData implements RData {
    private final Inet4Address address;
    private final int protocol;
    private final byte[] bitMap;

    public WKSRData(Inet4Address address, int protocol, byte[] bitMap) {
        this.address = address;
        this.protocol = protocol;
        this.bitMap = bitMap;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.WKS;
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
}
