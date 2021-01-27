package cafe.josh.comfydns.system;

import com.google.gson.Gson;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
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
            tasksAlive,
            recordsPruned,
            recordsCached,
            maxSuccessfulStateTransitions;

    private Metrics() {
        requestsReceived = new AtomicInteger(0);
        requestsAnswered = new AtomicInteger(0);
        requestsServerFailure = new AtomicInteger(0);
        requestsNameError = new AtomicInteger(0);
        tasksAlive = new AtomicInteger();
        recordsPruned = new AtomicInteger();
        recordsCached = new AtomicInteger();
        maxSuccessfulStateTransitions = new AtomicInteger();
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

    public AtomicInteger getMaxSuccessfulStateTransitions() {
        return maxSuccessfulStateTransitions;
    }

    public AtomicInteger getTasksAlive() {
        return tasksAlive;
    }

    public AtomicInteger getRecordsPruned() {
        return recordsPruned;
    }

    public AtomicInteger getRecordsCached() {
        return recordsCached;
    }

    public Object toJson(NioEventLoopGroup bossGroup, NioEventLoopGroup workerGroup, ThreadPoolExecutor executor) {
        Map<String, Object> ret = new HashMap<>();

        {
            Map<String, Object> requests = new HashMap<>();
            requests.put("received", requestsReceived.intValue());
            requests.put("answered", requestsAnswered.intValue());
            requests.put("serverFailure", requestsServerFailure.intValue());
            requests.put("nameError", requestsNameError.intValue());
            ret.put("requests", requests);
        }

        {
            int cached = recordsCached.get();
            int pruned = recordsPruned.get();
            Map<String, Object> rrStats = new HashMap<>();
            rrStats.put("cached", cached);
            rrStats.put("alive", cached - pruned);
            rrStats.put("pruned", pruned);
            ret.put("rr", rrStats);
        }

        {
            Map<String, Object> tasks = new HashMap<>();
            tasks.put("alive", tasksAlive.intValue());
            ret.put("tasks", tasks);
        }

        {
            Map<String, Object> queues = new HashMap<>();
            queues.put("taskQueue", Map.of(
                    "completed", executor.getCompletedTaskCount(),
                    "running", executor.getActiveCount(),
                    "pending", executor.getTaskCount() - executor.getCompletedTaskCount() - executor.getActiveCount()
            ));
            ret.put("queues", queues);
        }

        {
            ret.put("stateMachine", Map.of(
                    "maxSuccessfulQueryTransitionCount", maxSuccessfulStateTransitions.get()
            ));
        }

        {
            ret.put("jvm", Map.of(
                    "freeMemoryMB", Runtime.getRuntime().freeMemory()/(1 << 20),
                    "maxMemoryMB", Runtime.getRuntime().maxMemory()/(1 << 20),
                    "totalMemoryMB", Runtime.getRuntime().totalMemory()/(1 << 20)
            ));
        }

        return ret;
    }
}
