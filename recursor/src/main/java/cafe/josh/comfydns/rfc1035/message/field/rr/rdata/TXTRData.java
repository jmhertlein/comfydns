package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;

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
    public KnownRRType getRRType() {
        return KnownRRType.TXT;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] ret = new byte[text.length()+1];
        ret[0] = (byte) text.length();
        int pos = 1;
        for(char ch : text.toCharArray()) {
            if(ch > 255) {
                throw new IllegalArgumentException("Non-ascii character is illegal: " + ch);
            }
            ret[pos] = (byte) ch;
            pos++;
        }

        return ret;
    }
}