package cafe.josh.comfydns.rfc1035.service.search;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

public class SList {
    public static final int FAILURE_LIMIT = 3;
    private String zone;
    private List<SListServer> servers;
    private final Map<String, Integer> failureCounts;


    public SList() {
        this.zone = "";
        this.servers = new ArrayList<>();
        failureCounts = new HashMap<>();
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getZone() {
        return zone;
    }

    public void removeServer(SListServer s) {
        this.servers.remove(s);
    }

    public List<SListServer> getServers() {
        return servers;
    }

    public void incrementFailureCount(SListServer s) {
        int failures = failureCounts.computeIfAbsent(s.hostname, k -> 0);
        failures++;
        failureCounts.put(s.hostname, failures);
    }

    public Optional<SListServer> getBestServer() {
        List<SListServer> eligible = servers.stream().filter(s -> s.getFailureCount() < FAILURE_LIMIT).collect(Collectors.toList());
        Optional<SListServer> best = eligible.stream().filter(s -> s.ip != null).sorted().findFirst();
        if(best.isPresent()) {
            return best;
        }

        best = eligible.stream().sorted().findFirst();
        return best;
    }

    public SListServer newServerEntry(String hostname) {
        return new SListServer(hostname);
    }

    public SListServer newServerEntry(String hostname, InetAddress ip) {
        return new SListServer(hostname, ip);
    }

    public class SListServer implements Comparable<SListServer> {
        private final String hostname;

        private InetAddress ip;

        public SListServer(String hostname) {
            this.hostname = hostname;
        }

        public SListServer(String hostname, InetAddress ip) {
            this.hostname = hostname;
            this.ip = ip;
        }

        public void setIp(InetAddress ip) {
            this.ip = ip;
        }

        public String getHostname() {
            return hostname;
        }

        public int getFailureCount() {
            return failureCounts.getOrDefault(hostname, 0);
        }

        public void incrementFailureCount() {
            SList.this.incrementFailureCount(this);
        }

        public InetAddress getIp() {
            return ip;
        }

        @Override
        public int compareTo(SListServer o) {
            return Integer.compare(this.getFailureCount(), o.getFailureCount());
        }
    }
}
