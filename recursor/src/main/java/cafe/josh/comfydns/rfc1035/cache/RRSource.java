package cafe.josh.comfydns.rfc1035.cache;

import cafe.josh.comfydns.rfc1035.message.field.query.QClass;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;
import cafe.josh.comfydns.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.util.List;

public interface RRSource {
    List<RR<?>> search(String name, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException;
    default boolean isAuthoritative() {
        return false;
    }
}
