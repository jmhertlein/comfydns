package cafe.josh.comfydns.butil;

public class PrettyByte {
    private PrettyByte() {}

    public static String binString(byte b) {
        StringBuilder ret = new StringBuilder("0b");
        for(int mask = 0b10000000; mask > 0; mask >>= 1) {
            if((b & mask) > 0) {
                ret.append('1');
            } else {
                ret.append('0');
            }
        }
        return ret.toString();
    }

    public static String toString(byte ... bytes) {
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < bytes.length; i++) {
            ret.append(binString(bytes[i]));
            if(bytes[i] >= 46 && bytes[i] <= 125) {
                ret.append(" ").append((char) bytes[i]);
            }
            ret.append("\n");
        }

        return ret.toString();
    }

    public static String toComparison(byte[] expected, byte[] found) {
        int e = 0, f = 0;

        StringBuilder ret = new StringBuilder("Expected    Found\n");
        while(e < expected.length || f < found.length) {
            String eS, fS;
            if(e < expected.length) {
                eS = binString(expected[e]);
                e++;
            } else {
                eS = "          ";
            }

            if(f < found.length) {
                fS = binString(found[f]);
                f++;
            } else {
                fS = "          ";
            }

            ret.append(eS).append("  ").append(fS).append(" ").append(Math.max(e, f));
            if(!eS.isBlank() && !fS.isBlank() && eS.equals(fS) && expected[e-1] >= 46 && expected[e-1] <= 125) {
                ret.append(" ").append((char) expected[e-1]);
            }
            if(!eS.isBlank() && !fS.isBlank() && !eS.equals(fS)) {
                ret.append("  <-----");
            }

            ret.append('\n');
        }

        return ret.toString();
    }

    public static byte[] b(int ... input) {
        byte[] ret = new byte[input.length];
        for(int i = 0; i < input.length; i++) {
            ret[i] = (byte) input[i];
        }

        return ret;
    }

    /**
     *
     * @param l
     * @param bits
     * @param dest
     * @param pos index in dest at which to begin writing
     */
    public static void writeNBitUnsignedInt(long l, int bits, byte[] dest, int pos, int bytePos) {
        if(!RangeCheck.uint(bits, l)) {
            throw new IllegalArgumentException("Value " + l + " out of range for uint" + bits);
        }

        if((bytePos > 0 && bits % 8 != 8 - bytePos) || (bytePos == 0 && bits % 8 != 0)) {
            throw new IllegalArgumentException("Writing ints whose least significant bit is not byte-aligned is unsupported.");
        }

        if(bits % 8 > 0) {
            byte msb = (byte) (l >> (bits - (bits % 8)));
            dest[pos] &= (byte) (0xFF << (8 - bytePos)); //0b11000000
            dest[pos] |= msb;
            pos++;
            bits -= (bits % 8);
        }

        while(bits > 0) {
            dest[pos] = (byte) (l >> (bits - 8));
            pos++;
            bits -= 8;
        }
    }

    public static long readNBitUnsignedInt(int bits, byte[] src, int pos, int bytePos) {
        if((bytePos > 0 && bits % 8 != 8 - bytePos) || (bytePos == 0 && bits % 8 != 0)) {
            throw new IllegalArgumentException("Writing ints whose least significant bit is not byte-aligned is unsupported.");
        }

        long ret = 0;

        if(bits % 8 > 0) {
            byte msb = src[pos];
            msb &= (0xFF >> bytePos);
            pos++;
            bits -= (bits % 8);
            ret = msb;
        }

        while(bits > 0) {
            ret <<= 8;
            ret += Byte.toUnsignedInt(src[pos]);
            pos++;
            bits -= 8;
        }

        return ret;
    }

    public static void copyAll(byte[] dest, int destPos, byte[] ... srcs) {
        for(byte[] src : srcs) {
            System.arraycopy(src, 0, dest, destPos, src.length);
            destPos += src.length;
        }
    }
}
