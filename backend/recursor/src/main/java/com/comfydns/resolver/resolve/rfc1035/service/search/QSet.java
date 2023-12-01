package com.comfydns.resolver.resolve.rfc1035.service.search;

import com.comfydns.resolver.resolve.rfc1035.message.struct.Question;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QSet {
    private final Map<InetAddress, Set<Question>> qSets;

    public QSet() {
        qSets = new HashMap<>();
    }

    public synchronized boolean contains(InetAddress a, Question q) {
        return qSets.containsKey(a) && qSets.get(a).contains(q);
    }

    public synchronized void add(InetAddress a, Question q) {
        qSets.computeIfAbsent(a, k -> new HashSet<>()).add(q);
    }

    public void remove(InetAddress ip, Question question) {
        if(qSets.containsKey(ip)) {
            qSets.get(ip).remove(question);
        }
    }
}
