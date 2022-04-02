package com.comfydns.resolver.resolver.rfc1035.service;

import com.comfydns.resolver.resolver.block.DomainBlocker;
import com.comfydns.resolver.resolver.block.NoOpDomainBlocker;
import com.comfydns.resolver.resolver.rfc1035.cache.AuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.NegativeCache;
import com.comfydns.resolver.resolver.rfc1035.cache.RRCache;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryAuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryDNSCache;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryNegativeCache;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.service.request.Request;
import com.comfydns.resolver.resolver.rfc1035.service.search.ResolverContext;
import com.comfydns.resolver.resolver.rfc1035.service.search.SearchContext;
import com.comfydns.resolver.resolver.rfc1035.service.transport.NonTruncatingTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.TruncatingTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class RecursiveResolver {
    private static final Logger log = LoggerFactory.getLogger(RecursiveResolver.class);
    private final ExecutorService stateMachinePool;
    private final RRCache cache;
    private final NegativeCache negativeCache;
    private final TruncatingTransport primary;
    private final NonTruncatingTransport fallback;
    private final AuthorityRRSource authorityZones;
    private volatile DomainBlocker domainBlocker;
    private final Set<InetAddress> allowZoneTransferToAddresses;

    public RecursiveResolver(
            ExecutorService stateMachinePool,
            TruncatingTransport primary,
            NonTruncatingTransport fallback,
            Set<InetAddress> allowZoneTransferToAddresses
    ) {
        this.stateMachinePool = stateMachinePool;
        this.cache = new InMemoryDNSCache();
        this.authorityZones = new InMemoryAuthorityRRSource();
        this.negativeCache = new InMemoryNegativeCache();
        this.primary = primary;
        this.fallback = fallback;
        this.domainBlocker = new NoOpDomainBlocker();
        this.allowZoneTransferToAddresses = allowZoneTransferToAddresses;
    }

    public RecursiveResolver(
            ExecutorService stateMachinePool,
            RRCache cache,
            AuthorityRRSource authorityRecords,
            NegativeCache negativeCache,
            TruncatingTransport primary,
            NonTruncatingTransport fallback,
            Set<InetAddress> allowZoneTransferToAddresses
    ) {
        this.stateMachinePool = stateMachinePool;
        this.cache = cache;
        this.negativeCache = negativeCache;
        this.primary = primary;
        this.fallback = fallback;
        this.allowZoneTransferToAddresses = allowZoneTransferToAddresses;
        this.authorityZones = authorityRecords;
        this.domainBlocker = new NoOpDomainBlocker();
    }

    public Set<InetAddress> getAllowZoneTransferToAddresses() {
        return allowZoneTransferToAddresses;
    }

    public void setDomainBlocker(DomainBlocker domainBlocker) {
        this.domainBlocker = domainBlocker;
    }

    /**
     * Submits a Request to the resolver. This method returns immediately and does not block.
     * @param r
     */
    public void resolve(Request r) {
        RecursiveResolverTask t = new RecursiveResolverTask(
                new SearchContext(r, cache, r.getParentQSet()),
                new ResolverContext(this, cache, stateMachinePool, primary, fallback, authorityZones, negativeCache, domainBlocker)
        );
        stateMachinePool.submit(t);
    }
}
