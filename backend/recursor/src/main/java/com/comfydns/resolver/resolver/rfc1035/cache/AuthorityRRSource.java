package com.comfydns.resolver.resolver.rfc1035.cache;

import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;

import java.util.List;
import java.util.Set;

public interface AuthorityRRSource extends RRSource {
    public boolean isAuthoritativeFor(String domain);
    public Set<String> getAuthoritativeForDomains();
    public List<RR<SOARData>> getSOAs();
    public List<RR<?>> getZoneTransferPayload(String zoneName);
    public List<String> getNames();

    @Override
    default boolean isAuthoritative() {
        return true;
    }
}
