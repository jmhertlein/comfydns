package cafe.josh.comfydns.rfc1035.message.field.rr;

import cafe.josh.comfydns.rfc1035.message.write.Writeable;

public interface RData extends Writeable {
    public KnownRRType getRRType();
}
