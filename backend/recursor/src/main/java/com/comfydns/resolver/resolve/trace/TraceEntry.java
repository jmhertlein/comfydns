package com.comfydns.resolver.resolve.trace;

import com.comfydns.resolver.resolve.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolve.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;

public abstract class TraceEntry {
    private final TraceEntryType type;
    private final int index;

    protected TraceEntry(TraceEntryType type, int index) {
        this.type = type;
        this.index = index;
    }

    public TraceEntryType getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public static class AnswerAddedEntry extends TraceEntry {
        private final RR<?> answerRecord;
        public AnswerAddedEntry(int index, RR<?> rr) {
            super(TraceEntryType.ANSWER_ADDED, index);
            this.answerRecord = rr;
        }
    }

    public static class UpstreamQuerySentEntry extends TraceEntry {
        private final Message sent;
        private final String destHostname;

        public UpstreamQuerySentEntry(int index, Message m, String destination) {
            super(TraceEntryType.UPSTREAM_QUERY_SENT, index);
            this.sent = m;
            this.destHostname = destination;
        }
    }

    public static class UpstreamQueryResultEntry extends TraceEntry {
        private final Message result;
        private final String destHostname;
        private final Throwable error;

        public UpstreamQueryResultEntry(int index, Message m, String destination) {
            super(TraceEntryType.UPSTREAM_QUERY_RESULT, index);
            this.result = m;
            this.destHostname = destination;
            this.error = null;
        }

        public UpstreamQueryResultEntry(int index, Throwable t, String destination) {
            super(TraceEntryType.UPSTREAM_QUERY_RESULT, index);
            this.result = null;
            this.destHostname = destination;
            this.error = t;
        }
    }

    public static class SNameChangeEntry extends TraceEntry {
        private final String oldSName, newSName;

        public SNameChangeEntry(int index, String oldSName, String newSName) {
            super(TraceEntryType.SNAME_CHANGE, index);
            this.oldSName = oldSName;
            this.newSName = newSName;
        }
    }

    public static class SubquerySentEntry extends TraceEntry {
        private final Message sent;

        public SubquerySentEntry(int index, Message sent) {
            super(TraceEntryType.SUBQUERY_SENT, index);
            this.sent = sent;
        }
    }

    public static class SubqueryResultEntry extends TraceEntry {
        private final Message result;

        public SubqueryResultEntry(int index, Message result) {
            super(TraceEntryType.SUBQUERY_RESULT, index);
            this.result = result;
        }
    }

    public static class ResponseEntry extends TraceEntry {
        private final Message response;

        public ResponseEntry(int index, Message response) {
            super(TraceEntryType.RESPONSE, index);
            this.response = response;
        }
    }

    public static class ExceptionEntry extends TraceEntry {
        private final Throwable error;

        public ExceptionEntry(int index, Throwable error) {
            super(TraceEntryType.EXCEPTION, index);
            this.error = error;
        }
    }

    public static class RemarkEntry extends TraceEntry {
        private final String remark;

        public RemarkEntry(int index, String remark) {
            super(TraceEntryType.REMARK, index);
            this.remark = remark;
        }
    }

    public static class NegativeCacheUseEntry extends TraceEntry {
        private final String sName;
        private final int qType;
        private final int qClass;

        public NegativeCacheUseEntry(int index, String sName, QType qType, QClass qClass) {
            super(TraceEntryType.NEGATIVE_CACHE_USE, index);
            this.sName = sName;
            this.qType = qType.getIntValue();
            this.qClass = qClass.getIntValue();
        }
    }
}
