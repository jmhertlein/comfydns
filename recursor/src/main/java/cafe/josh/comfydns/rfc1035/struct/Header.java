package cafe.josh.comfydns.rfc1035.struct;

import cafe.josh.comfydns.rfc1035.field.header.OpCode;

import static cafe.josh.comfydns.RangeCheck.*;

public class Header {
    private final byte[] content;

    public Header() {
        this.content = new byte[12];
    }

    public void setId(int id) throws IllegalArgumentException {
        if(!uint(16, id)) {
            throw new IllegalArgumentException("Value for id \"" + id + "\" must be 16-bit unsigned int.");
        }

        content[0] = (byte) (id >> 8);
        content[1] = (byte) (id & 0b00001111);
    }

    public void setQR(boolean isResponse) {
        if(isResponse) {
            content[2] |= 0b10000000;
        } else {
            content[2] &= 0b01111111;
        }
    }

    public void setOpCode(OpCode code) {
        content[2] &= 0b10000111;
        content[2] |= (((byte) code.getCode()) << 3);
    }

    public void setAA(boolean authoritative) {
        if(authoritative) {
            content[2] |= 0b00000100;
        } else {
            content[2] &= 0b11111011;
        }
    }

}
