package cafe.josh.comfydns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComfyNameDaemon {
    final static Logger log = LoggerFactory.getLogger(ComfyNameDaemon.class);

    public static void main(String ... args) {
        ComfyDNSServer d = new ComfyDNSServer();
        d.run();
    }
}