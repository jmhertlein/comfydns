package com.comfydns.logprocess;

import java.util.*;

public class RequestMatcher {
    private final Map<UUID, RequestInEvent> liveEvents;
    private int noIn, matched;

    public RequestMatcher() {
        this.liveEvents = new HashMap<>();
        noIn = 0;
        matched = 0;
    }

    public void process(RequestInEvent e) {
        liveEvents.put(e.getRequestId(), e);
    }

    public void process(RequestOutEvent e) {
        if(liveEvents.containsKey(e.getRequestId())) {
            liveEvents.remove(e.getRequestId());
            matched++;
        } else {
            noIn++;
        }
    }

    public List<RequestInEvent> getLive() {
        return new ArrayList<>(liveEvents.values());
    }

    public int getNoIn() {
        return noIn;
    }

    public int getMatched() {
        return matched;
    }
}
