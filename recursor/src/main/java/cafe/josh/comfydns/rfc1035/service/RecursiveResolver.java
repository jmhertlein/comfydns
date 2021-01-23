package cafe.josh.comfydns.rfc1035.service;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.cache.DNSCache;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.transport.NonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.TruncatingTransport;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RecursiveResolver {
    private final ExecutorService pool;
    private final DNSCache cache;
    private final TruncatingTransport primary;
    private final NonTruncatingTransport fallback;

    public RecursiveResolver(DNSCache cache, TruncatingTransport primary, NonTruncatingTransport fallback) {
        this.cache = cache;
        this.primary = primary;
        this.fallback = fallback;

        this.pool = Executors.newCachedThreadPool();
    }

    public void resolve(Request r) {
        pool.submit(() -> {
            try {
                r.answer(doResolve(r));
            } catch (CacheAccessException e) {
                // todo return error message
            }
        });
    }

    private Message doResolve(Request r) throws CacheAccessException {
        for(Question q : r.getMessage().getQuestions()) {
            List<RR<?>> fastPath = cache.search(q.getQName(), q.getqType(), q.getqClass(), OffsetDateTime.now());
            if(fastPath.isEmpty()) {
                // recursion time
                List<String> domains = LabelCache.genSuffixes(q.getQName());
                List<RR<?>> search = null;
                for (String d : domains) {
                    search = cache.search(d, q.getqType(), q.getqClass(), OffsetDateTime.now());
                    if(!search.isEmpty()) {
                        break;
                    }
                }
            } else {
                Message ret = new Message();
                Header h = new Header(r.getMessage().getHeader());
                h.setQR(true);
                h.setRA(true);
                ret.setHeader(h);

                ret.getAnswerRecords().addAll(fastPath);

                return ret;
            }

        }

        return null;
    }
}
