package com.comfydns.resolver.resolve.rfc1035.cache;

import com.comfydns.resolver.resolve.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;

public class CachedNegative {
    private final RR<SOARData> soaRR;
    private final RCode rCode;

    public CachedNegative(RR<SOARData> soaRR, RCode rCode) {
        this.soaRR = soaRR;
        this.rCode = rCode;
    }

    public RR<SOARData> getSoaRR() {
        return soaRR;
    }

    public RCode getRCode() {
        return rCode;
    }
}
