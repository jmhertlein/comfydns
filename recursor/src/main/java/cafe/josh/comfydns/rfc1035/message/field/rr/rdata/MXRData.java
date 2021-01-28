package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.butil.PrettyByte;
import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.LabelMaker;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;

public class MXRData implements RData {
    private final int preference;
    private final String exchange;

    public MXRData(int preference, String exchange) {
        this.preference = preference;
        this.exchange = exchange;
    }

    public int getPreference() {
        return preference;
    }

    public String getExchange() {
        return exchange;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.MX;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] exchange = LabelMaker.makeLabels(this.exchange, c);
        c.addSuffixes(this.exchange, index+2);

        byte[] ret = new byte[2 + exchange.length];
        System.arraycopy(exchange, 0, ret, 2, exchange.length);
        PrettyByte.writeNBitUnsignedInt(preference, 16, ret, 0, 0);

        return ret;
    }

    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        int preference = (int) PrettyByte.readNBitUnsignedInt(16, content, pos, 0);
        LabelMaker.ReadLabels readLabels = LabelMaker.readLabels(content, pos + 2);

        return new MXRData(preference, readLabels.name);
    }
}
