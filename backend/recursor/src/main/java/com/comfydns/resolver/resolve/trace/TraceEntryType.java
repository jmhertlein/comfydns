package com.comfydns.resolver.resolve.trace;

public enum TraceEntryType {
    ANSWER_ADDED,
    UPSTREAM_QUERY_SENT,
    UPSTREAM_QUERY_RESULT,
    SNAME_CHANGE,
    SUBQUERY_SENT,
    SUBQUERY_RESULT,
    RESPONSE,
    EXCEPTION,
    REMARK,
    NEGATIVE_CACHE_USE,
}
