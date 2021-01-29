package cafe.josh.comfydns.rfc1035.cache;

import cafe.josh.comfydns.rfc1035.message.field.query.QClass;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;
import cafe.josh.comfydns.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthoritativeRecordsContainer implements RRSource {
    private final Map<String, Map<RR2Tuple, List<RR<?>>>> zoneRecords;
    public AuthoritativeRecordsContainer(List<RR<?>> records) {
        this();
        records.forEach(this::cache);
    }

    public AuthoritativeRecordsContainer() {
        zoneRecords = new HashMap<>();
    }

    @Override
    public List<RR<?>> search(String name, QType qType, QClass qClass, OffsetDateTime now) {
        List<RR<?>> ret = new ArrayList<>();
        Map<RR2Tuple, List<RR<?>>> rr2TupleRRMap = zoneRecords.get(name);
        if (rr2TupleRRMap == null) {
            return List.of();
        }

        for (Map.Entry<RR2Tuple, List<RR<?>>> e : rr2TupleRRMap.entrySet()) {
            if (qType.queryMatches(e.getKey().rrType) && qClass.queryMatches(e.getKey().rrClass)) {
                ret.addAll(e.getValue());
            }
        }

        return ret;
    }

    private void cache(RR<?> record) {
        Map<RR2Tuple, List<RR<?>>> records = zoneRecords.computeIfAbsent(record.getName(), k -> new HashMap<>());
        List<RR<?>> rrs = records.computeIfAbsent(record.getClassAndType(),
                k -> new ArrayList<>());
        rrs.add(record);
    }

    @Override
    public boolean isAuthoritative() {
        return true;
    }
}
