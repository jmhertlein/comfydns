package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.net.DNSRootZone;
import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.ARData;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.NSRData;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.*;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This should look at LOCAL information and populate the SList, then
 * transition to SEND_SERVER_QUERY
 */
public class FindBestServerToAsk implements RequestState {
    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException {
        Question q = sCtx.getCurrentQuestion();
        List<String> domains = LabelCache.genSuffixes(q.getQName());
        List<RR<?>> search = null;
        String zone = null;
        for (String d : domains) {
            search = rCtx.getOverlay().search(d, KnownRRType.NS, q.getqClass(), OffsetDateTime.now());
            if(!search.isEmpty()) {
                zone = d;
                break;
            }
        }


        SList sList = sCtx.getSList();
        if(search == null || search.isEmpty()) {
            sList.setZone("");
            sList.getServers().clear();
            sList.getServers().addAll(DNSRootZone.ROOT_SERVERS.stream().map(rs -> {
                SList.SListServer sls = new SList.SListServer(rs.getName());
                sls.setIp(rs.getAddress());
                return sls;
            }).collect(Collectors.toList()));
        } else {
            sList.setZone(zone);
            sList.getServers().clear();
            for (RR<?> rr : search) {
                SList.SListServer s = new SList.SListServer(rr.getName());
                NSRData tData = (NSRData) rr.getTData();
                List<RR<?>> aSearch = rCtx.getOverlay().search(tData.getNsDName(), KnownRRType.A, q.getqClass(), OffsetDateTime.now());
                if(!aSearch.isEmpty()) {
                    ARData nsIp = (ARData) aSearch.get((int) (Math.random() * aSearch.size())).getTData();
                    s.setIp(nsIp.getAddress());
                }
                sList.getServers().add(s);
            }
        }


    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.FIND_BEST_SERVER_TO_ASK;
    }
}
