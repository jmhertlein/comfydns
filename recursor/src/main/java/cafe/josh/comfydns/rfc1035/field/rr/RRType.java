package cafe.josh.comfydns.rfc1035.field.rr;

import cafe.josh.comfydns.RDataConstructionFunction;
import cafe.josh.comfydns.rfc1035.field.query.QType;

import java.util.Optional;
import java.util.function.BiFunction;

public interface RRType extends QType {
    public boolean isWellKnown();
    public RDataConstructionFunction getCtor();

    public static RRType match(byte[] content, int pos) {
        for (KnownRRType v : KnownRRType.values()) {
            byte[] value = v.getValue();
            if(value[0] == content[pos] && value[1] == content[pos+1]) {
                return v;
            }
        }

        return new UnknownRRType(new byte[]{content[pos], content[pos+1]});
    }

}
