package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.internet.DNSRootZone;
import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.ARData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.NSRData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This should look at LOCAL information and populate the SList, then
 * transition to SEND_SERVER_QUERY
 */
public class FindBestServerToAsk implements RequestState {
    private final Logger log = LoggerFactory.getLogger(FindBestServerToAsk.class);
    @Override
    public Optional<RequestState> run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, StateTransitionCountLimitExceededException {
        Question q = sCtx.getCurrentQuestion();
        List<String> domains = LabelCache.genSuffixes(sCtx.getSName());
        List<RR<?>> search = null;
        String zone = null;
        for (String d : domains) {
            /*
            If we're authoritative for the domain and we got to FindBestServerToAsk, it's a nameerror.
             */
            if(rCtx.getAuthorityZones().isAuthoritativeFor(d)) {
                log.debug("{}We're authoritative for {}, returning NAME_ERROR for {}", sCtx.getRequestLogPrefix(), d, sCtx.getSName());
                return Optional.of(new DoubleCheckSendState(sCtx.buildNameErrorResponse()));
            }

            search = sCtx.getOverlay().search(d, KnownRRType.NS, q.getqClass(), OffsetDateTime.now());
            if(!search.isEmpty()) {
                zone = d;
                break;
            }
        }


        SList sList = sCtx.getSList();
        if(search == null || search.isEmpty()) {
            sList.setZone("");
            sList.getServers().clear();
            List<SList.SListServer> tmp = DNSRootZone.ROOT_SERVERS.stream().map(rs -> {
                SList.SListServer sls = sList.newServerEntry(rs.getName(), null);
                sls.setIp(rs.getAddress());
                return sls;
            }).collect(Collectors.toList());
            Collections.shuffle(tmp);
            sList.getServers().addAll(tmp);
        } else {
            sList.setZone(zone);
            sList.getServers().clear();
            for (RR<?> rr : search) {
                NSRData rData = (NSRData) rr.getRData();
                SList.SListServer s = sList.newServerEntry(rData.getNsDName(), (RR<NSRData>) rr);
                List<RR<?>> aSearch = sCtx.getOverlay().search(rData.getNsDName(), KnownRRType.A, q.getqClass(), OffsetDateTime.now());
                if(!aSearch.isEmpty()) {
                    ARData nsIp = (ARData) aSearch.get((int) (Math.random() * aSearch.size())).getRData();
                    s.setIp(nsIp.getAddress());
                }
                sList.getServers().add(s);
            }
        }

        return Optional.of(new SendServerQuery(false));
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.FIND_BEST_SERVER_TO_ASK;
    }
}
