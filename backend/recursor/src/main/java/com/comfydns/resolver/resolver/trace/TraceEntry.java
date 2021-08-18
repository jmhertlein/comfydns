package com.comfydns.resolver.resolver.trace;

import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;

public abstract class TraceEntry {
    private final TraceEntryType type;

    protected TraceEntry(TraceEntryType type) {
        this.type = type;
    }

    public static class AnswerAddedEntry extends TraceEntry {
        private final RR<?> answerRecord;
        public AnswerAddedEntry(RR<?> rr) {
            super(TraceEntryType.ANSWER_ADDED);
            this.answerRecord = rr;
        }
    }

    public static class UpstreamQuerySentEntry extends TraceEntry {
        private final Message sent;
        private final String destHostname;

        public UpstreamQuerySentEntry(Message m, String destination) {
            super(TraceEntryType.UPSTREAM_QUERY_SENT);
            this.sent = m;
            this.destHostname = destination;
        }
    }

    public static class UpstreamQueryResultEntry extends TraceEntry {
        private final Message result;
        private final String destHostname;
        private final Throwable error;

        public UpstreamQueryResultEntry(Message m, String destination) {
            super(TraceEntryType.UPSTREAM_QUERY_RESULT);
            this.result = m;
            this.destHostname = destination;
            this.error = null;
        }

        public UpstreamQueryResultEntry(Throwable t, String destination) {
            super(TraceEntryType.UPSTREAM_QUERY_RESULT);
            this.result = null;
            this.destHostname = destination;
            this.error = t;
        }
    }

    public static class SNameChangeEntry extends TraceEntry {
        private final String oldSName, newSName;

        public SNameChangeEntry(String oldSName, String newSName) {
            super(TraceEntryType.SNAME_CHANGE);
            this.oldSName = oldSName;
            this.newSName = newSName;
        }
    }

    public static class SubquerySentEntry extends TraceEntry {
        private final Message sent;

        public SubquerySentEntry(Message sent) {
            super(TraceEntryType.SUBQUERY_SENT);
            this.sent = sent;
        }
    }

    public static class SubqueryResultEntry extends TraceEntry {
        private final Message result;

        public SubqueryResultEntry(Message result) {
            super(TraceEntryType.SUBQUERY_RESULT);
            this.result = result;
        }
    }

    public static class ResponseEntry extends TraceEntry {
        private final Message response;

        public ResponseEntry(Message response) {
            super(TraceEntryType.RESPONSE);
            this.response = response;
        }
    }

    public static class ExceptionEntry extends TraceEntry {
        private final Throwable error;

        public ExceptionEntry(Throwable error) {
            super(TraceEntryType.EXCEPTION);
            this.error = error;
        }
    }

    public static class RemarkEntry extends TraceEntry {
        private final String remark;

        public RemarkEntry(String remark) {
            super(TraceEntryType.REMARK);
            this.remark = remark;
        }
    }

    public static class NegativeCacheUseEntry extends TraceEntry {
        private final String sName;
        private final QType qType;
        private final QClass qClass;

        public NegativeCacheUseEntry(String sName, QType qType, QClass qClass) {
            super(TraceEntryType.NEGATIVE_CACHE_USE);
            this.sName = sName;
            this.qType = qType;
            this.qClass = qClass;
        }
    }
}
