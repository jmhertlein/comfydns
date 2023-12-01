package com.comfydns.resolver.resolve.rfc1035.cache.impl;

import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public class CacheMetrics {
    public static final Counter cachedRecordsTotal = Counter.build()
            .name("cached_records_total")
            .help("Total number of records ever put into the cache.")
            .labelNames("rrtype")
            .register();
    public static final Counter cachedRecordsPrunedTotal = Counter.build()
            .name("cached_records_pruned_total")
            .help("Total number of records ever removed from the cache.")
            .register();
    public static final Gauge currentCachedRecords = Gauge.build()
            .name("cached_records_current")
            .help("Current number of cached records")
            .register();
    public static final Histogram cacheReadTimeSeconds = Histogram.build()
            .name("cache_read_time")
            .help("How long it takes to read the cache.")
            .buckets(0.001, 0.005, 0.01, 0.05, 0.1, 0.2, 0.3, 0.5, 1.0, 2, 5, 10)
            .register();
    public static final Histogram cacheWriteTimeSeconds = Histogram.build()
            .name("cache_write_time")
            .help("How long it takes to write to the cache.")
            .buckets(0.001, 0.005, 0.01, 0.05, 0.1, 0.2, 0.3, 0.5, 1.0, 2, 5, 10)
            .register();



    public static final Histogram authorityRRReadTimeSeconds = Histogram.build()
            .name("authority_rr_read_time")
            .help("How long it takes to read the authoritative rr table.")
            .buckets(0.001, 0.005, 0.01, 0.05, 0.1, 0.2, 0.3, 0.5, 1.0, 2, 5, 10)
            .register();



    public static final Counter cachedNegativeRecordsTotal = Counter.build()
            .name("cached_negative_records_total")
            .help("Total number of records ever put into the cache.")
            .register();
    public static final Counter cachedNegativeRecordsPrunedTotal = Counter.build()
            .name("cached_negative_records_pruned_total")
            .help("Total number of records ever removed from the cache.")
            .register();

    public static final Histogram negativeCacheReadTimeSeconds = Histogram.build()
            .name("negative_cache_read_time")
            .help("How long it takes to read the cache.")
            .buckets(0.001, 0.005, 0.01, 0.05, 0.1, 0.2, 0.3, 0.5, 1.0, 2, 5, 10)
            .register();
    public static final Histogram negativeCacheWriteTimeSeconds = Histogram.build()
            .name("negative_cache_write_time")
            .help("How long it takes to write to the cache.")
            .buckets(0.001, 0.005, 0.01, 0.05, 0.1, 0.2, 0.3, 0.5, 1.0, 2, 5, 10)
            .register();

    private CacheMetrics() {}

    public static void recordCache(RR<?> record) {
        cachedRecordsTotal.labels(
                record.getRrType().isWellKnown()
                        ? record.getRrType().getType().toLowerCase()
                        : "not_well_known"
        ).inc();
    }
}
