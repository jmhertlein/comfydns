package cafe.josh.comfydns.rfc1035.service.search;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

public class SList {
    public static final int FAILURE_LIMIT = 3;
    private String zone;
    private List<SListServer> servers;


    public SList() {
        this.zone = "";
        this.servers = new ArrayList<>();
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

    public Optional<SListServer> getBestServer() {
        List<SListServer> eligible = servers.stream().filter(s -> s.failureCount < FAILURE_LIMIT).collect(Collectors.toList());
        Optional<SListServer> best = eligible.stream().filter(s -> s.ip != null).sorted().findFirst();
        if(best.isPresent()) {
            return best;
        }

        best = eligible.stream().sorted().findFirst();
        return best;
    }

    public static class SListServer implements Comparable<SListServer> {
        private final String hostname;

        private InetAddress ip;
        private int failureCount;

        public SListServer(String hostname) {
            this.hostname = hostname;
            this.failureCount = 0;
        }

        public SListServer(String hostname, InetAddress ip) {
            this.hostname = hostname;
            this.ip = ip;
            this.failureCount = 0;
        }

        public void setIp(InetAddress ip) {
            this.ip = ip;
        }

        public String getHostname() {
            return hostname;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void incrementFailureCount() {
            this.failureCount++;
        }

        public InetAddress getIp() {
            return ip;
        }

        @Override
        public int compareTo(SListServer o) {
            return Integer.compare(this.failureCount, o.failureCount);
        }
    }
}
