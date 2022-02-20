package com.comfydns.resolver.resolver.rfc1035.cache;

import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;

import java.util.List;
import java.util.Set;

public interface AuthorityRRSource extends RRSource {
    public boolean isAuthoritativeFor(String domain) throws CacheAccessException;
    public Set<String> getAuthoritativeForDomains() throws CacheAccessException;
    public List<RR<SOARData>> getSOAs() throws CacheAccessException;
    public List<RR<?>> getZoneTransferPayload(String zoneName) throws CacheAccessException;
    public List<String> getNames() throws CacheAccessException;

    @Override
    default boolean isAuthoritative() {
        return true;
    }
}
