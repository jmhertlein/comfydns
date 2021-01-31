package cafe.josh.comfydns.rfc1035.cache;

import cafe.josh.comfydns.rfc1035.message.field.query.QClass;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;
import cafe.josh.comfydns.rfc1035.message.field.rr.RRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.RRType;
import cafe.josh.comfydns.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.util.List;

public interface RRCache extends RRContainer {
    public void prune(OffsetDateTime now);
    public void expunge(List<RR<?>> records);
}
