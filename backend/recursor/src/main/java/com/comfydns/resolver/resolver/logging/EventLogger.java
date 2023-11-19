package com.comfydns.resolver.resolver.logging;

import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.service.request.LiveRequest;
import com.comfydns.resolver.util.JsonArrayCollector;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class EventLogger {
    private static final Logger log = LoggerFactory.getLogger(EventLogger.class);
    private static final Gson gson = new Gson();

    public static void logRequestStart(LiveRequest req) {
        JsonObject logForm = new JsonObject();
        logForm.addProperty("eventType", EventLogLineType.REQUEST_IN.name());
        logForm.addProperty("eventTime", Instant.now().toString());
        logForm.addProperty("id", req.getId().toString());
        logForm.addProperty("class", req.getClass().getSimpleName());
        logForm.addProperty("numQuestions", req.getMessage().getQuestions().size());
        JsonArray arr = req
                .getMessage()
                .getQuestions()
                .stream()
                .map(Question::toJson)
                .collect(new JsonArrayCollector());
        logForm.add("questions", arr);
        log.info("[EVENT]: {}", gson.toJson(logForm));
    }

    public static void logRequestEnd(LiveRequest req, RCode rCode) {
        JsonObject logForm = new JsonObject();
        logForm.addProperty("eventType", EventLogLineType.REQUEST_OUT.name());
        logForm.addProperty("eventTime", Instant.now().toString());
        logForm.addProperty("id", req.getId().toString());
        logForm.addProperty("rCode", rCode.getCode());
        log.info("[EVENT]: {}", gson.toJson(logForm));
    }


}
