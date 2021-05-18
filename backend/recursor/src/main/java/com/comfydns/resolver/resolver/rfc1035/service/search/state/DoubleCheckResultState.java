package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolverTask;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class DoubleCheckResultState implements RequestState {
    private static final Logger log = LoggerFactory.getLogger(DoubleCheckResultState.class);

    public static final Counter doubleCheckResults = Counter.build()
            .name("double_check_results")
            .labelNames("status")
            .help("Number of double check failures since startup.")
            .register();

    private final Message ourResponse;
    private final byte[] theirResponse;

    public DoubleCheckResultState(Message ourResponse, byte[] theirResponse) {
        this.ourResponse = ourResponse;
        this.theirResponse = theirResponse;
    }

    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        Message theirs;
        try {
            theirs = Message.read(theirResponse);
        } catch (InvalidMessageException | UnsupportedRRTypeException e) {
            throw new NameResolutionException("Error parsing double-check response", e);
        }

        // SIGH: not all SOA-name-errors have rcode=name_error.
        if(countDirectlyAnsweringRecords(theirs) == 0 &&
                theirs.getAuthorityRecords().stream().allMatch(rr -> rr.getRrType() == KnownRRType.SOA)) {
            theirs.getHeader().setRCode(RCode.NAME_ERROR);
        }

        if((ourResponse.getHeader().getRCode() == theirs.getHeader().getRCode() && ourResponse.getHeader().getRCode() == RCode.NAME_ERROR) ||
                (ourResponse.getHeader().getRCode() == theirs.getHeader().getRCode() && (theirs.getHeader().getANCount() > 0 == ourResponse.getHeader().getANCount() > 0))) {
            log.debug("Double-check passed.");
            doubleCheckResults.labels("pass").inc();
            self.setState(new SendResponseState(ourResponse));
        } else {
            log.warn("Double-check failed: OURS:\n{}\nTHEIRS:\n{}\n", ourResponse, theirs);
            self.setState(new SendResponseState(theirs));
            doubleCheckResults.labels("fail").inc();
        }
        self.run();
        return;
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.DOUBLE_CHECK_RESULT_STATE;
    }

    private int countDirectlyAnsweringRecords(Message m) {
        Set<RR<?>> directAnswers = new HashSet<>();
        for (Question q : m.getQuestions()) {
            m.getAnswerRecords().stream().filter(r -> q.getqType().queryMatches(r.getRrType().getValue())).forEach(directAnswers::add);
        }

        return directAnswers.size();
    }
}
