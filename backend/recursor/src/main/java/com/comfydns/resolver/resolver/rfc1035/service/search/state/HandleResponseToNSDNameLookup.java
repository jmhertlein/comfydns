package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.ARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolverTask;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HandleResponseToNSDNameLookup implements RequestState {
    private static final Logger log = LoggerFactory.getLogger(HandleResponseToNSDNameLookup.class);
    private final Message m;
    private final List<SList.SListServer> serversInQuestion;

    public HandleResponseToNSDNameLookup(Message m, List<SList.SListServer> serversInQuestion) {
        this.m = m;
        this.serversInQuestion = serversInQuestion;
    }

    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException {
        if(m.getHeader().getRCode() != RCode.NO_ERROR) {
            log.debug("A IN record search for " + serversInQuestion.stream().map(SList.SListServer::getHostname).collect(Collectors.joining(",")) +
                    " yielded an RCode of " + m.getHeader().getRCode().name() + ": " + m.getHeader().getRCode().getExplanation());

        }

        if(m.getHeader().getRCode() == RCode.SERVER_FAILURE) {
            throw new NameResolutionException("Internal query resulted in SERVER_FAILURE");
        }

        log.debug("An internal query finished.");

        List<RR<?>> records = new ArrayList<>();
        Set<String> serversWithAnswerRecords = new HashSet<>();
        m.forEach(records::add);
        for (RR<?> rr : records) {
            if(rr.getTtl() == 0) {
                sCtx.getRequestCache().cache(rr, OffsetDateTime.now());
            } else {
                rCtx.getGlobalCache().cache(rr, OffsetDateTime.now());
            }
            if(KnownRRType.A.queryMatches(rr.getRrType().getValue())) {
                serversWithAnswerRecords.add(rr.getName());
                log.debug("Internal query found that {} has ip {}", rr.getName(), ((ARData) rr.getRData()).getAddress());
            }
        }

        for (SList.SListServer s : serversInQuestion) {
            if(!serversWithAnswerRecords.contains(s.getHostname())) {
                log.debug("[{}]: SList server failure: {}. Sent an InternalRequest but it came back as: {}", sCtx.getRequest().getId(), s.getHostname(), m.toString());
                s.incrementFailureCount();
                for (Question q : m.getQuestions()) {
                    sCtx.getQSet().remove(s.getIp(), q);
                }
            }
        }

        self.setState(new FindBestServerToAsk());
        self.run();
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.HANDLE_RESPONSE_TO_NSDNAME_LOOKUP;
    }
}