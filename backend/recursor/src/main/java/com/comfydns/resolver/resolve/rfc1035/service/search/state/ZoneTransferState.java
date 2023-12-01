package com.comfydns.resolver.resolve.rfc1035.service.search.state;

import com.comfydns.resolver.resolve.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolve.rfc1035.service.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

public class ZoneTransferState implements RequestState {
    private static final Logger log = LoggerFactory.getLogger(ZoneTransferState.class);
    @Override
    public RequestState run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        log.info("AXFR starting.");
        if(!rCtx.getRecursiveResolver().getAllowZoneTransferToAddresses()
            .contains(sCtx.getRequest().getRemoteAddress().get())) {
            log.info("Zone transfer refused from {}", sCtx.getRequest().getRemoteAddress().orElse(InetAddress.getLoopbackAddress()));
            return new ResponseReadyState(sCtx.prepareRefusedResponse("Zone transfer refused."));
        }

        if(sCtx.getRequest().transportIsTruncating()) {
            log.info("Refused to AXFR over UDP.");
            return new ResponseReadyState(sCtx.prepareRefusedResponse("Please only do AXFR's over TCP."));
        }

        List<RR<SOARData>> soas = rCtx.getAuthorityZones().getSOAs().stream().filter(soa -> soa.getName().equals(sCtx.getSName()))
                .collect(Collectors.toList());
        if(soas.size() > 1) {
            throw new NameResolutionException("Found multiple soas for AXFR query for " + sCtx.getSName());
        } else if(soas.size() < 1) {
            throw new NameResolutionException("We are not authoritative for the zone " + sCtx.getSName());
        }

        sCtx.addAnswerRR(soas.get(0));
        rCtx.getAuthorityZones().getZoneTransferPayload(sCtx.getSName())
                .forEach(sCtx::addAnswerRR);
        sCtx.addAnswerRR(soas.get(0));

        Message resp = sCtx.buildResponse();
        log.info("ZONE TRANSFER RESPONSE:\n{}", resp);
        log.info("{}Zone transfer complete.", sCtx.getRequestLogPrefix());
        return new ResponseReadyState(resp);
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.ZONE_TRANSFER_STATE;
    }
}
