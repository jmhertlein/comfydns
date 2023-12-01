package com.comfydns.logprocess;

import com.comfydns.resolver.resolve.logging.EventLogLineType;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Question;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RequestInEvent {
    private final EventLogLineType eventType;
    private final Instant eventTime;
    private final UUID requestId;
    private final String className;
    private final int numQuestions;
    private final List<Question> questions;

    public RequestInEvent(JsonObject o) {
        eventType = EventLogLineType.valueOf(o.get("eventType").getAsString());
        if(eventType != EventLogLineType.REQUEST_IN) {
            throw new IllegalArgumentException("The passed event is not a REQUEST_IN event.");
        }
        eventTime = Instant.parse(o.get("eventTime").getAsString());
        requestId = UUID.fromString(o.get("id").getAsString());
        className = o.get("class").getAsString();
        numQuestions = o.get("numQuestions").getAsInt();
        questions = StreamSupport.stream(o.get("questions").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(Question::new)
                .collect(Collectors.toList());

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

    public String getClassName() {
        return className;
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestInEvent that = (RequestInEvent) o;
        return numQuestions == that.numQuestions && eventType == that.eventType && eventTime.equals(that.eventTime) && requestId.equals(that.requestId) && className.equals(that.className) && questions.equals(that.questions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, eventTime, requestId, className, numQuestions, questions);
    }
}
