package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.service.request.InternalRequest;
import com.comfydns.resolver.resolver.rfc1035.service.request.LiveRequest;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class SendNSDNameLookup implements RequestState {
    private final int MAX_SUBQUERY_DEPTH = 10;
    private static final Logger log = LoggerFactory.getLogger(SendNSDNameLookup.class);
    private final List<SList.SListServer> servers;

    public SendNSDNameLookup(List<SList.SListServer> servers) {
        this.servers = servers;
    }

    @Override
    public RequestState run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException {
        sCtx.incrementSubQueriesMade();
        if(sCtx.getSubQueriesMade() > SearchContext.SUB_QUERY_COUNT_LIMIT) {
            log.debug("[{}] Reached max subquery count while trying to answer {}",
                    sCtx.getRequest().getId(), sCtx.getCurrentQuestion());
            throw new NameResolutionException("Reached max subquery count, refusing to continue.");
        }
        Message m = new Message();
        m.setHeader(new Header());
        m.getHeader().setQDCount(servers.size());
        List<Question> questions = servers.stream().map(s -> new Question(s.getHostname(), KnownRRType.A, KnownRRClass.IN)).collect(Collectors.toList());
        m.getQuestions().addAll(questions);
        m.getHeader().setRD(true);
        m.getHeader().setIdRandomly();

        // first check to see if we're going to loop...
        LiveRequest tmp = sCtx.getRequest();
        while(tmp.isSubquery()) {
            tmp = ((InternalRequest) tmp).getParent();
            for(Question q : questions) {
                if(tmp.getMessage().getQuestions().contains(q)) {
                    log.debug("Detected an NSDNAME loop.");
                    throw new NameResolutionException("Detected an NSDNAME loop.");
                }
            }
        }

        log.debug("[{}]: Trying to answer |{}|, sending internal request: \n{}",
                sCtx.getRequest().getId(), sCtx.getCurrentQuestion(), m);

        InternalRequest req = new InternalRequest(m, sCtx.getRequest(), sCtx.getQSet());
        if(req.getSubqueryDepth() > MAX_SUBQUERY_DEPTH) {
            log.debug("[{}] Query hit max subquery depth while trying to answer: {}",
                    sCtx.getRequest().getId(), sCtx.getCurrentQuestion());
            throw new NameResolutionException("Hit max subquery depth. Failing.");
        }

        sCtx.forEachListener(l -> l.onSubquerySent(m));
        Message response = rCtx.getRecursiveResolver().resolve(() -> req);
        return new HandleResponseToNSDNameLookup(response, servers);
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.SEND_NSDNAME_LOOKUP;
    }
}
