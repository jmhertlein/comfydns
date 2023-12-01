package com.comfydns.logprocess;

import com.comfydns.resolver.resolve.logging.EventLogLineType;
import com.comfydns.resolver.resolve.rfc1035.message.field.header.RCode;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class RequestOutEvent {
    /*
        logForm.addProperty("eventType", EventLogLineType.REQUEST_OUT.name());
        logForm.addProperty("eventTime", Instant.now().toString());
        logForm.addProperty("id", req.getId().toString());
        logForm.addProperty("rCode", rCode.getCode());
     */

    private final EventLogLineType eventType;
    private final Instant eventTime;
    private final UUID requestId;
    private final RCode rCode;

    public RequestOutEvent(JsonObject o) {
        eventType = EventLogLineType.valueOf(o.get("eventType").getAsString());
        if(eventType != EventLogLineType.REQUEST_OUT) {
            throw new IllegalArgumentException("The passed event is not a REQUEST_IN event.");
        }
        eventTime = Instant.parse(o.get("eventTime").getAsString());
        requestId = UUID.fromString(o.get("id").getAsString());
        rCode = RCode.match(o.get("rCode").getAsInt()).orElse(null);
    }

    public EventLogLineType getEventType() {
        return eventType;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public RCode getrCode() {
        return rCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestOutEvent that = (RequestOutEvent) o;
        return eventType == that.eventType && eventTime.equals(that.eventTime) && requestId.equals(that.requestId) && rCode == that.rCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, eventTime, requestId, rCode);
    }
}
