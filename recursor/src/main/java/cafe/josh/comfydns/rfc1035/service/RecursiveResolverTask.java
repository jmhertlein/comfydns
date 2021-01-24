package cafe.josh.comfydns.rfc1035.service;

import cafe.josh.comfydns.net.DNSRootZone;
import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.cache.DNSCache;
import cafe.josh.comfydns.rfc1035.cache.OverlayCache;
import cafe.josh.comfydns.rfc1035.cache.TemporaryDNSCache;
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
import cafe.josh.comfydns.rfc1035.service.transport.NonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.TruncatingTransport;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class RecursiveResolverTask implements Runnable {
    private final RequestProgress progress;

    private final ExecutorService pool;
    private final DNSCache cache, taskCache, overlayCache;
    private final TruncatingTransport primary;
    private final NonTruncatingTransport fallback;
    private Message lastReceived, lastSent;
    private InetAddress lastSentAddr;
    private Throwable lastException;

    public RecursiveResolverTask(RequestProgress progress, ExecutorService pool, DNSCache cache, TruncatingTransport primary, NonTruncatingTransport fallback) {
        this.progress = progress;
        this.pool = pool;
        this.cache = cache;
        this.primary = primary;
        this.fallback = fallback;

        taskCache = new TemporaryDNSCache();
        overlayCache = new OverlayCache(taskCache, cache);
    }

    @Override
    public void run() {

    }

    public void setLastReceived(Message lastReceived) {
        this.lastReceived = lastReceived;
    }

    public void setLastException(Throwable lastException) {
        this.lastException = lastException;
    }

    private void recurse() throws CacheAccessException {
        // first process any messages we've received
        if(this.lastReceived != null) {
            if(lastReceived.getHeader().getTC() && lastSent != null && lastSentAddr != null) {
                // retry with TCP
                fallback.send(lastSent.write(), lastSentAddr, arr -> {
                    try {
                        this.setLastReceived(Message.read(arr));
                    } catch (InvalidMessageException e) {
                        e.printStackTrace();
                    } catch (UnsupportedRRTypeException e) {
                        e.printStackTrace();
                    }
                    pool.submit(this);
                });
                return;
            }
            for (RR<?> r : lastReceived.getAnswerRecords()) {
                if(r.getTtl() == 0) {
                    this.taskCache.cache(r, OffsetDateTime.now());
                } else {
                    this.cache.cache(r, OffsetDateTime.now());
                }
            }
            for (RR<?> r : lastReceived.getAuthorityRecords()) {
                if(r.getTtl() == 0) {
                    this.taskCache.cache(r, OffsetDateTime.now());
                } else {
                    this.cache.cache(r, OffsetDateTime.now());
                }
            }
            for (RR<?> r : lastReceived.getAdditionalRecords()) {
                if(r.getTtl() == 0) {
                    this.taskCache.cache(r, OffsetDateTime.now());
                } else {
                    this.cache.cache(r, OffsetDateTime.now());
                }
            }
            this.lastReceived = null;
        }

        Question q = progress.getRequest().getMessage().getQuestions().get(progress.getQuestionIndex().get());
        // now try to answer the question
        List<RR<?>> potentialAnswer = overlayCache.search(q.getQName(), q.getqType(), q.getqClass(), OffsetDateTime.now());
        if(!potentialAnswer.isEmpty()) {
            progress.getAnswer().addAll(potentialAnswer);
            int questionIndex = progress.getQuestionIndex().incrementAndGet();
            if(questionIndex >= progress.getRequest().getMessage().getHeader().getQDCount()) {
                progress.sendAnswer();
            } else {
                recurse();
            }
            return;
        }

        // We can't answer the question. We need to find the best server to ask.
        InetAddress bestServerToAsk;
        List<String> domains = LabelCache.genSuffixes(q.getQName());
        List<RR<?>> search = null;
        for (String d : domains) {
            search = overlayCache.search(d, KnownRRType.NS, q.getqClass(), OffsetDateTime.now());
            if(!search.isEmpty()) {
                break;
            }
        }

        if(search == null || search.isEmpty()) {
            // we must ask the root dns servers.
            bestServerToAsk = DNSRootZone.getRandomRootServer().getAddress();
        } else {
            // pick a random NS to contact of the ones referred to
            RR<?> rr = search.get((int) (Math.random() * search.size()));
            NSRData tData = (NSRData) rr.getTData();
            String nsDName = tData.getNsDName();
            List<RR<?>> result = overlayCache.search(nsDName, KnownRRType.A, q.getqClass(), OffsetDateTime.now());
            if(result.isEmpty()) {
                Message req = new Message();
                Header h = new Header();
                h.setQDCount(1);
                req.setHeader(h);
                req.getQuestions().add(new Question(nsDName, KnownRRType.A, q.getqClass()));
                InternalRequest r = new InternalRequest(req, m -> {
                    this.setLastReceived(m);
                    pool.submit(this);
                });
                RecursiveResolverTask t = new RecursiveResolverTask(new RequestProgress(r), pool, cache,
                        primary, fallback);
                pool.submit(t);
                return;
            } else {
                RR<?> nsARecord = result.get((int) (Math.random() * result.size()));
                ARData nsARData = (ARData) nsARecord.getTData();
                bestServerToAsk = nsARData.getAddress();
            }
        }
        Message m = new Message();
        Header h = new Header();
        h.setRD(false);
        h.setQDCount(1);
        h.setIdRandomly();
        h.setOpCode(OpCode.QUERY);
        m.setHeader(h);
        m.getQuestions().add(new Question(q.getQName(), q.getqType(), q.getqClass()));
        primary.send(m.write(), bestServerToAsk, arr -> {
            try {
                this.setLastReceived(Message.read(arr));
            } catch (InvalidMessageException | UnsupportedRRTypeException e) {
                this.setLastException(e);
            } finally {
                pool.submit(this);
            }

        }, e -> {
            this.setLastException(e);
            pool.submit(this);
        });
    }
}
