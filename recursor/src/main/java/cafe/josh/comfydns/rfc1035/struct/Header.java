package cafe.josh.comfydns.rfc1035.struct;

import cafe.josh.comfydns.rfc1035.InvalidHeaderException;
import cafe.josh.comfydns.rfc1035.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.field.header.OpCode;
import cafe.josh.comfydns.rfc1035.field.header.RCode;
import cafe.josh.comfydns.rfc1035.write.Writeable;

import java.util.Optional;

import static cafe.josh.comfydns.RangeCheck.*;

public class Header implements Writeable {
    public static final int FIXED_LENGTH_OCTETS = 12;
    private final byte[] content;

    public Header() {
        this.content = new byte[FIXED_LENGTH_OCTETS];
    }

    /**
     *
     * @param content
     * @throws IllegalArgumentException if content is not Header.FIXED_LENGTH_OCTETS long
     */
    public Header(byte[] content) {
        if(content.length != FIXED_LENGTH_OCTETS) {
            throw new IllegalArgumentException("Content length must be 12 octets, but was " + content.length);
        }

        this.content = content;
    }

    public void setId(int id) throws IllegalArgumentException {
        set16BitInteger(0, id, "id");
    }

    public int getId() {
        return get16BitInteger(0);
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

    private int extractOpCode() {
        byte tmp = content[2];
        tmp &= 0b0111_1000;
        tmp >>= 3;
        return tmp;
    }

    public OpCode getOpCode() {
        return OpCode.match(extractOpCode()).get();
    }

    public void setAA(boolean authoritative) {
        if(authoritative) {
            content[2] |= 0b00000100;
        } else {
            content[2] &= 0b11111011;
        }
    }

    public void setTC(boolean truncation) {
        if(truncation) {
            content[2] |= 0b00000010;
        } else {
            content[2] &= 0b11111101;
        }
    }

    public void setRD(boolean recursionDesired) {
        if(recursionDesired) {
            content[2] |= 0b00000001;
        } else {
            content[2] &= 0b11111110;
        }
    }

    public void setRA(boolean recursionAvailable) {
        if(recursionAvailable) {
            content[3] |= 0b10000000;
        } else {
            content[3] &= 0b01111111;
        }
    }

    public void setRCode(RCode code) {
        int val = code.getCode();
        content[3] &= 0b11110000;
        content[3] |= (byte) val;
    }

    private int extractRCode() {
        return content[3] & 0b00001111;
    }

    public RCode getRCode() {
        return RCode.match(extractRCode()).get();
    }

    public void setQDCount(int questionEntryCount) throws IllegalArgumentException {
        set16BitInteger(4, questionEntryCount, "QDCOUNT");
    }

    public int getQDCount() {
        return get16BitInteger(4);
    }

    public void setANCount(int answerRecordsCount) {
        set16BitInteger(6, answerRecordsCount, "ANCOUNT");
    }

    public int getANCount() {
        return get16BitInteger(6);
    }

    public void setNSCount(int authorityRecordsCount) {
        set16BitInteger(8, authorityRecordsCount, "NSCOUNT");
    }

    public int getNSCount() {
        return get16BitInteger(8);
    }

    public void setARCount(int additionalRecordsCount) {
        set16BitInteger(10, additionalRecordsCount, "ARCOUNT");
    }

    public int getARCount() {
        return get16BitInteger(10);
    }

    private void set16BitInteger(int index, int value, String fieldName) {
        if(!uint(16, value)) {
            throw new IllegalArgumentException("Value for " + fieldName + " \"" + value + "\" must be 16-bit unsigned int.");
        }

        content[index] = (byte) (value >> 8);
        content[index+1] = (byte) (value);
    }

    private int get16BitInteger(int index) {
        int ret = Byte.toUnsignedInt(content[index]);
        ret <<= 8;
        ret += Byte.toUnsignedInt(content[index+1]);
        return ret;
    }

    public byte[] getNetworkForm() {
        return this.content;
    }

    public void validate() throws InvalidHeaderException {
        if(OpCode.match(extractOpCode()).isEmpty()) {
            throw new InvalidHeaderException("OpCode is invalid: " + extractOpCode());
        }

        if(RCode.match(extractRCode()).isEmpty()) {
            throw new InvalidHeaderException("RCode is invalid: " + extractRCode());
        }
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        return getNetworkForm();
    }
}
