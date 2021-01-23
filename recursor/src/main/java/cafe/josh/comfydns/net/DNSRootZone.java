package cafe.josh.comfydns.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DNSRootZone {
    private static final Logger log = LoggerFactory.getLogger(DNSRootZone.class);
    //private static final Pattern PING_PATTERN = Pattern.compile("^rtt min\\/avg\\/max\\/mdev = (?<min>[\\d.]+)\\/(?<avg>[\\d.]+)\\/(?<max>[\\d.]+)\\/(?<mdev>[\\d.]+) ms$");
    public static final List<Server> ROOT_SERVERS;
    //public static final List<Server> ROOT_SERVER_BY_PING_TIME;
    static {
        ROOT_SERVERS = List.of(
                new Server("198.41.0.4", "a", "Verisign, Inc."),
                new Server("199.9.14.201", "b", "University of Southern California, Information Sciences Institute"),
                new Server("192.33.4.12", "c", "Cogent Communications"),
                new Server("199.7.91.13", "d", "University of Maryland"),
                new Server("192.203.230.10", "e", "NASA (Ames Research Center)"),
                new Server("192.5.5.241", "f", "Internet Systems Consortium, Inc."),
                new Server("192.112.36.4", "g", "US Department of Defense (NIC)"),
                new Server("198.97.190.53", "h", "US Army (Research Lab)"),
                new Server("192.36.148.17", "i", "Netnod"),
                new Server("192.58.128.30", "j", "Verisign, Inc."),
                new Server("193.0.14.129", "k", "RIPE NCC"),
                new Server("199.7.83.42", "l", "ICANN"),
                new Server("202.12.27.33", "m", "WIDE Project")
        );

//        // TODO I like this but it needs to be done on a bg thread in
//        // a singleton that atomically swaps the unsorted list for the sorted
//        // when the process is done.
          // or maybe just gather this in realtime from live data
//        List<Server> sorted;
//        try {
//            sorted = sortByPingTime(ROOT_SERVERS);
//        } catch (Throwable t) {
//            log.warn("Unable to sort root dns servers.", t);
//            sorted = ROOT_SERVERS;
//        }
//
//        ROOT_SERVER_BY_PING_TIME = sorted;
    }

//    public static List<Server> sortByPingTime(List<Server> servers) throws IOException, InterruptedException {
//        servers = new ArrayList<>(servers);
//        Map<Server, Double> times = new HashMap<>();
//        for (Server s : servers) {
//            ProcessBuilder b = new ProcessBuilder("ping", "-c", "3", s.getAddress().getHostAddress());
//            Process start = b.start();
//            start.waitFor();
//            try(InputStreamReader r = new InputStreamReader(start.getInputStream());
//                BufferedReader reader = new BufferedReader(r)) {
//                String line = reader.readLine();
//                while(line != null) {
//                    Matcher m = PING_PATTERN.matcher(line);
//                    if(m.matches()) {
//                        times.put(s, Double.parseDouble(m.group("avg")));
//                        log.info(line);
//                    }
//                    line = reader.readLine();
//                }
//            }
//        }
//
//        servers.sort(Comparator.comparingDouble(l -> times.getOrDefault(l, Double.MAX_VALUE)));
//        return servers;
//    }

    public static Server getRandomRootServer() {
        return ROOT_SERVERS.get((int) (Math.random()*ROOT_SERVERS.size()));
    }

    public static class Server {
        private final Inet4Address address;
        private final String name, operator;

        public Server(String address, String name, String operator) {
            try {
                this.address = (Inet4Address) Inet4Address.getByName(address);
            } catch (UnknownHostException e) {
                throw new RuntimeException("Someone messed up while populating the root servers list. It's like they didn't even test it.");
            }
            this.name = name + ".root-servers.net";
            this.operator = operator;
        }

        public Inet4Address getAddress() {
            return address;
        }

        public String getName() {
            return name;
        }

        public String getOperator() {
            return operator;
        }
    }
}
