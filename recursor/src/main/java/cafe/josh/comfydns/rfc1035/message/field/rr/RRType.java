package cafe.josh.comfydns.rfc1035.message.field.rr;

import cafe.josh.comfydns.rfc1035.message.RDataConstructionFunction;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;

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
