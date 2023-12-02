package com.comfydns.resolver.resolve.system;

import io.prometheus.client.Counter;

public class JavaNetMetrics {
    protected static final Counter javaNetErrors = Counter.build()
            .name("javanet_socket_errors").help("Errors that happen OUTSIDE the resolver - purely transport-based errors.")
            .labelNames("protocol", "error_side", "error_type").register();
}
