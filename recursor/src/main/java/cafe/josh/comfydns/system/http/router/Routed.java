package cafe.josh.comfydns.system.http.router;

import java.util.Collections;
import java.util.Map;

public class Routed {
    private final Route route;
    private final Map<String, String> variables;

    public Routed(Route route) {
        this.route = route;
        this.variables = Collections.emptyMap();
    }

    public Routed(Route route, Map<String, String> variables) {
        this.route = route;
        this.variables = variables;
    }

    public Route getRoute() {
        return route;
    }

    public Map<String, String> getVariables() {
        return variables;
    }
}
