package cafe.josh.comfydns.rfc1035.field.rr;

import cafe.josh.comfydns.rfc1035.write.Writeable;

public interface RData extends Writeable {
    public KnownRRType getRRType();
}
