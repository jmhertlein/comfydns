package cafe.josh.comfydns.rfc1035.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.field.rr.RData;
import cafe.josh.comfydns.rfc1035.field.rr.RRType;

public class TXTRData implements RData {
    private final String text;

    public TXTRData(String text) {
        if(text.length() > 255) {
            throw new IllegalArgumentException("Illegal length of text. Must be < 255 8-bit characters.");
        }
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public RRType getRRType() {
        return RRType.TXT;
    }
}
