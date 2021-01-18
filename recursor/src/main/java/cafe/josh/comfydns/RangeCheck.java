package cafe.josh.comfydns;

public class RangeCheck {
    public static boolean uint(int bits, int i) {
        return i >= 0 && i < (1 << bits);
    }

    public static boolean uint(int bits, long i) {
        return i >= 0 && i < (1L << bits);
    }
}
