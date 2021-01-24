package cafe.josh.comfydns.rfc1035.service;

import cafe.josh.comfydns.net.DNSRootZone;
import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.header.OpCode;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.ARData;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.NSRData;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.search.*;
import cafe.josh.comfydns.rfc1035.service.search.state.TryToAnswerWithLocalInformation;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.List;

public class RecursiveResolverTask implements Runnable {
    private final SearchContext sCtx;
    private final ResolverContext rCtx;
    private RequestState state;
    private int stateTransitionCount;

    public RecursiveResolverTask(SearchContext sCtx, ResolverContext rCtx) {
        this.sCtx = sCtx;
        this.rCtx = rCtx;
        this.state = new TryToAnswerWithLocalInformation();
        this.stateTransitionCount = 0;
    }

    @Override
    public void run() {
        try {
            this.state.run(rCtx, sCtx, this);
        } catch (CacheAccessException | NameResolutionException e) {
            sCtx.sendOops(e.getMessage());
        } catch (NameErrorException e) {
            sCtx.sendNameError();
        }
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        stateTransitionCount++;
        this.state = state;
    }
}
