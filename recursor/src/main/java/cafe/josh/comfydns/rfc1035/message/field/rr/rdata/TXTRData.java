package cafe.josh.comfydns.rfc1035.message.field.rr.rdata;

import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TXTRData txtrData = (TXTRData) o;
        return Objects.equals(text, txtrData.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return text;
    }

    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        StringBuilder b = new StringBuilder();
        for(int i = pos; i < pos + rdlength; i++) {
            b.appendCodePoint(content[i]);
        }

        return new TXTRData(b.toString());
    }
}
