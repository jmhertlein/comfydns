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
}
