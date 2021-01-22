package cafe.josh.comfydns.rfc1035.field.rr;

import cafe.josh.comfydns.rfc1035.field.query.QClass;

import java.util.Optional;

public interface RRClass extends QClass {
    public boolean isWellKnown();

    public static RRClass match(byte[] content, int pos) {
        for (KnownRRClass v : KnownRRClass.values()) {
            byte[] value = v.getValue();
            if(value[0] == content[pos] && value[1] == content[pos+1]) {
                return v;
            }
        }

        return new UnknownRRClass(new byte[]{content[pos], content[pos+1]});
    }
}
