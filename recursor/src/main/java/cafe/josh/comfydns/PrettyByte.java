package cafe.josh.comfydns;

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

    public static byte[] b(int ... input) {
        byte[] ret = new byte[input.length];
        for(int i = 0; i < input.length; i++) {
            ret[i] = (byte) input[i];
        }

        return ret;
    }

    public static void writeNBitUnsignedInt(long l, int bits, byte[] dest, int pos) {
        if(!RangeCheck.uint(bits, l)) {
            throw new IllegalArgumentException("Value " + l + " out of range for uint" + bits);
        }


        if(bits % 8 > 0) {
            byte msb = (byte) (l >> (bits - (bits % 8)));
            dest[pos] = msb;
            pos++;
            bits -= (bits % 8);
        }

        while(bits > 0) {
            dest[pos] = (byte) (l >> (bits - 8));
            bits -= 8;
        }
    }
}
