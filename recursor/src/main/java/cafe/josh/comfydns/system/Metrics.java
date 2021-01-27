package cafe.josh.comfydns.system;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Metrics {
    private static final Metrics instance = new Metrics();
    public static Metrics getInstance() {
        return instance;
    }

    private final AtomicInteger
            requestsReceived,
            requestsAnswered,
            requestsServerFailure,
            requestsNameError,
            tasksAlive;

    private Metrics() {
        requestsReceived = new AtomicInteger(0);
        requestsAnswered = new AtomicInteger(0);
        requestsServerFailure = new AtomicInteger(0);
        requestsNameError = new AtomicInteger(0);
        tasksAlive = new AtomicInteger();
    }

    public AtomicInteger getRequestsReceived() {
        return requestsReceived;
    }

    public AtomicInteger getRequestsAnswered() {
        return requestsAnswered;
    }

    public AtomicInteger getRequestsServerFailure() {
        return requestsServerFailure;
    }

    public AtomicInteger getRequestsNameError() {
        return requestsNameError;
    }

    public AtomicInteger getTasksAlive() {
        return tasksAlive;
    }

    public Object toJson() {
        Map<String, Object> ret = new HashMap<>();
        Map<String, Object> requests = new HashMap<>();
        requests.put("received", requestsReceived.intValue());
        requests.put("answered", requestsAnswered.intValue());
        requests.put("serverFailure", requestsServerFailure.intValue());
        requests.put("nameError", requestsNameError.intValue());
        ret.put("requests", requests);

        Map<String, Object> tasks = new HashMap<>();
        tasks.put("alive", tasksAlive.intValue());
        ret.put("tasks", tasks);
        return ret;
    }
}
