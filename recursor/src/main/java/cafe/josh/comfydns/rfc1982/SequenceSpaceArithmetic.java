package cafe.josh.comfydns.rfc1982;

/**
 * https://tools.ietf.org/html/rfc1982
 * A faithful implementation of IETF RFC 1982.
 */
public class SequenceSpaceArithmetic {
    private SequenceSpaceArithmetic() {}

    /**
     * s' = (s + n) modulo (2 ^ SERIAL_BITS)
     * @param s the serial number
     * @param n the value to add
     * @return s'
     */
    public static long add(long s, long n, int serialBits) {
        return (s + n) % (1L << serialBits);
    }

    public static boolean lessThan(long s1, long s2, int SERIAL_BITS) {
        return (s1 < s2 && s2 - s1 < (1L << SERIAL_BITS - 1)) ||
                (s1 > s2 && s1 - s2 > (1L << SERIAL_BITS - 1));
    }

    public static boolean greaterThan(long s1, long s2, int SERIAL_BITS) {
        return  (s1 < s2 && s2 - s1 > (1L << (SERIAL_BITS - 1))) ||
                (s1 > s2 && s1 - s2 < (1L << (SERIAL_BITS - 1)));
    }
}
