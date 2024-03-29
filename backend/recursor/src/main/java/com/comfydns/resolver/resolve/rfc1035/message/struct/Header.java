package com.comfydns.resolver.resolve.rfc1035.message.struct;

import com.comfydns.resolver.resolve.rfc1035.message.InvalidHeaderException;
import com.comfydns.resolver.resolve.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolve.rfc1035.message.field.header.OpCode;
import com.comfydns.resolver.resolve.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolve.rfc1035.message.write.Writeable;

import static com.comfydns.resolver.resolve.butil.RangeCheck.*;

public class Header implements Writeable {
    public static final int FIXED_LENGTH_OCTETS = 12;
    private final byte[] content;

    public Header() {
        this.content = new byte[FIXED_LENGTH_OCTETS];
    }

    public Header(Header deepCopy) {
        this();
        System.arraycopy(deepCopy.content, 0, this.content, 0, FIXED_LENGTH_OCTETS);
        content[3] &= 0b10001111;
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

    public void setIdRandomly() {
        setId((int) (Math.random() * ((1 << 16)-1)));
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

    public boolean getQR() {
        return (content[2] & (byte) 0b1000_0000) != 0;
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

    public boolean getAA() {
        return (content[2] & 0b00000100) != 0;
    }

    public void setTC(boolean truncation) {
        setTCBit(truncation, this.content);
    }

    private static void setTCBit(boolean truncation, byte[] content) {
        if(truncation) {
            content[2] |= 0b00000010;
        } else {
            content[2] &= 0b11111101;
        }
    }

    public boolean getTC() {
        return (content[2] & 0b00000010) != 0;
    }

    public void setRD(boolean recursionDesired) {
        if(recursionDesired) {
            content[2] |= 0b00000001;
        } else {
            content[2] &= 0b11111110;
        }
    }

    public boolean getRD() {
        return (content[2] & 0b00000001) != 0;
    }

    public void setRA(boolean recursionAvailable) {
        if(recursionAvailable) {
            content[3] |= 0b10000000;
        } else {
            content[3] &= 0b01111111;
        }
    }

    public boolean getRA() {
        return (content[3] & 0b10000000) != 0;
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
    public String toString() {
        return String.format("ID: %s, QR: %s, OPCODE: %s, AA: %s, TC: %s, RD: %s, RA: %s, RCODE: %s\n"
        + "QDCOUNT: %s, ANCOUNT: %s, NSCOUNT: %s, ARCOUNT: %s",
                getId(), getQR() ? "response" : "query", getOpCode().name(), getAA(), getTC(), getRD(), getRA(), getRCode().name(),
                getQDCount(), getANCount(), getNSCount(), getARCount());
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        return getNetworkForm();
    }

    public static void setTruncated(byte[] content) {
        setTCBit(true, content);
    }
}
