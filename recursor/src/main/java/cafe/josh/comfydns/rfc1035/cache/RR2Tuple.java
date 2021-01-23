package cafe.josh.comfydns.rfc1035.cache;

import java.util.Arrays;

public class RR2Tuple {
    public final byte[] rrClass, rrType;

    public RR2Tuple(byte[] rrClass, byte[] rrType) {
        this.rrClass = new byte[]{rrClass[0], rrClass[1]};
        this.rrType = new byte[]{rrType[0], rrType[1]};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RR2Tuple rr2Tuple = (RR2Tuple) o;
        return Arrays.equals(rrClass, rr2Tuple.rrClass) && Arrays.equals(rrType, rr2Tuple.rrType);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(rrClass);
        result = 31 * result + Arrays.hashCode(rrType);
        return result;
    }
}
