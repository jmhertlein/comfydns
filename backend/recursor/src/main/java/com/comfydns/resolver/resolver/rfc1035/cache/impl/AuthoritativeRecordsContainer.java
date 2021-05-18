package com.comfydns.resolver.resolver.rfc1035.cache.impl;

import com.comfydns.resolver.resolver.rfc1035.cache.RR2Tuple;
import com.comfydns.resolver.resolver.rfc1035.cache.RRSource;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.ARData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.NSRData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.PTRRData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An RRSource that is immutable and specifically holds RRs that the server is authoritative for.
 */

public class AuthoritativeRecordsContainer implements RRSource {
    private static final Logger log = LoggerFactory.getLogger(AuthoritativeRecordsContainer.class);

    private final Map<String, Map<RR2Tuple, List<RR<?>>>> zoneRecords;
    private final Set<String> authoritativeForDomains;

    public AuthoritativeRecordsContainer(List<RR<?>> records) {
        zoneRecords = new HashMap<>();
        records.forEach(this::cache);
        authoritativeForDomains = getSOAs().stream().map(RR::getName).collect(Collectors.toSet());
    }

    public AuthoritativeRecordsContainer() {
        zoneRecords = new HashMap<>();
        authoritativeForDomains = Set.of();
    }

    public static AuthoritativeRecordsContainer load(Connection c) throws SQLException, InvalidMessageException, UnsupportedRRTypeException {
        List<RR<?>> records = new ArrayList<>();

        List<RR<SOARData>> soaRecords = new ArrayList<>();
        Map<UUID, Boolean> zoneToGenPtrs = new HashMap<>();

        List<String> nsDNames = new ArrayList<>();
        // load servers
        try (PreparedStatement ps = c.prepareStatement("select distinct hostname from server where hostname is not null");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                nsDNames.add(rs.getString("hostname"));
            }
        }

        // load SOAs
        try (PreparedStatement ps = c.prepareStatement("select z.name, z.gen_ptrs, soa.* from zone z join start_of_authority soa on (z.id=soa.zone_id)")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    zoneToGenPtrs.put(rs.getObject("zone_id", UUID.class), rs.getBoolean("gen_ptrs"));
                    soaRecords.add(new RR<>(
                            rs.getString("name"),
                            KnownRRType.SOA,
                            KnownRRClass.IN,
                            rs.getInt("minimum"),
                            new SOARData(
                                    rs.getString("mname"),
                                    rs.getString("rname"),
                                    rs.getLong("serial"),
                                    rs.getLong("refresh"),
                                    rs.getLong("retry"),
                                    rs.getLong("expire"),
                                    rs.getLong("minimum")
                            )
                    ));
                }
            }
        }

        records.addAll(soaRecords.stream().map(soa -> {
            SOARData rData = soa.getRData();
            return new RR<>(
                    soa.getName(),
                    KnownRRType.SOA,
                    KnownRRClass.IN,
                    soa.getTtl(),
                    new SOARData(
                            rData.getMName() == null ? "unset.example.com" : rData.getMName(),
                            rData.getrName() == null ? "unset@example.com" : rData.getrName(),
                            rData.getSerial(),
                            rData.getRefresh(),
                            rData.getRetry(),
                            rData.getExpire(),
                            rData.getMinimum()
                    )
            );
        }).collect(Collectors.toList()));

        // generate NS records
        records.addAll(soaRecords.stream().flatMap(soa ->
                nsDNames.stream().map(nsDName -> new RR<>(
                        soa.getName(),
                        KnownRRType.NS,
                        KnownRRClass.IN,
                        (int) soa.getRData().getMinimum(),
                        new NSRData(nsDName)))
        ).collect(Collectors.toList()));

        List<RR<?>> normalRecords = new ArrayList<>();
        List<RR<?>> genPtrsFor = new ArrayList<>();
        // load defined records
        try (PreparedStatement ps = c.prepareStatement("select * from rr")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RR<?> rr = RR.read(rs);
                    normalRecords.add(rr);
                    if (zoneToGenPtrs.get(rs.getObject("zone_id", UUID.class))) {
                        genPtrsFor.add(rr);
                    }
                }
            }
        }

        records.addAll(normalRecords);

        // gen generated records (if requested)
        List<RR<PTRRData>> ptrRecords = genPtrsFor.stream()
                .filter(rr -> rr.getRrType() == KnownRRType.A)
                .map(rr -> new RR<>(
                        PTRRData.ipToInAddrArpa(((ARData) rr.getRData()).getAddress().getHostAddress()),
                        KnownRRType.PTR,
                        rr.getRrClass(),
                        rr.getTtl(),
                        new PTRRData(rr.getName())
                )).collect(Collectors.toList());
        records.addAll(ptrRecords);

        log.debug("Loaded {} records from db info", records.size());

        return new AuthoritativeRecordsContainer(records);
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

    public boolean isAuthoritativeFor(String domain) {
        return authoritativeForDomains.contains(domain);
    }

    public Set<String> getAuthoritativeForDomains() {
        return authoritativeForDomains;
    }

    private void cache(RR<?> record) {
        Map<RR2Tuple, List<RR<?>>> records = zoneRecords.computeIfAbsent(record.getName(), k -> new HashMap<>());
        List<RR<?>> rrs = records.computeIfAbsent(record.getClassAndType(),
                k -> new ArrayList<>());
        rrs.add(record);
    }

    public List<String> getNames() {
        return new ArrayList<>(zoneRecords.keySet());
    }

    public List<RR<SOARData>> getSOAs() {
        List<RR<SOARData>> ret = new ArrayList<>();
        for (Map.Entry<String, Map<RR2Tuple, List<RR<?>>>> e : zoneRecords.entrySet()) {
            for (RR2Tuple key : e.getValue().keySet()) {
                if (KnownRRType.SOA.queryMatches(key.rrType)) {
                    if (e.getValue().get(key).size() > 1) {
                        log.warn("Weird: Found more than one SOA record for {}", e.getKey());
                    }
                    for (RR<?> rr : e.getValue().get(key)) {
                        ret.add((RR<SOARData>) rr);
                    }
                }
            }
        }

        return ret;
    }

    @Override
    public boolean isAuthoritative() {
        return true;
    }

    public List<RR<?>> getZoneTransferPayload(String zoneName) {
        return zoneRecords.values().stream()
                .flatMap(m -> m.values().stream()
                        .flatMap(Collection::stream)
                        .filter(rr -> rr.getName().contains(".") ? rr.getName().split("\\.", 2)[1].equals(zoneName)
                                : rr.getName().equals(zoneName))
                        .filter(rr -> rr.getRrType() != KnownRRType.SOA)
                ).collect(Collectors.toList());
    }
}
