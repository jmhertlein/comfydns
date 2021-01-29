package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.internet.DNSRootZone;
import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.ARData;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.NSRData;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.*;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This should look at LOCAL information and populate the SList, then
 * transition to SEND_SERVER_QUERY
 */
public class FindBestServerToAsk implements RequestState {
    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, StateTransitionCountLimitExceededException {
        Question q = sCtx.getCurrentQuestion();
        List<String> domains = LabelCache.genSuffixes(sCtx.getSName());
        List<RR<?>> search = null;
        String zone = null;
        for (String d : domains) {
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
                SList.SListServer sls = sList.newServerEntry(rs.getName());
                sls.setIp(rs.getAddress());
                return sls;
            }).collect(Collectors.toList());
            Collections.shuffle(tmp);
            sList.getServers().addAll(tmp);
        } else {
            sList.setZone(zone);
            sList.getServers().clear();
            for (RR<?> rr : search) {
                NSRData tData = (NSRData) rr.getTData();
                SList.SListServer s = sList.newServerEntry(tData.getNsDName());
                List<RR<?>> aSearch = sCtx.getOverlay().search(tData.getNsDName(), KnownRRType.A, q.getqClass(), OffsetDateTime.now());
                if(!aSearch.isEmpty()) {
                    ARData nsIp = (ARData) aSearch.get((int) (Math.random() * aSearch.size())).getTData();
                    s.setIp(nsIp.getAddress());
                }
                sList.getServers().add(s);
            }
        }

        self.setState(new SendServerQuery(false));
        self.run();
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.FIND_BEST_SERVER_TO_ASK;
    }
}
