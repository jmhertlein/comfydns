package com.comfydns.resolver.resolve.trace;

import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Question;
import com.comfydns.resolver.util.JsonArrayCollector;
import com.google.gson.*;

import java.lang.reflect.Type;

public class MessageCodec implements JsonSerializer<Message> {
    @Override
    public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
        return serialize(src);
    }

    public static JsonObject serialize(Message src) {
        JsonObject ret = new JsonObject();
        ret.add("header", HeaderCodec.serialize(src.getHeader()));
        ret.add("question",
                src.getQuestions()
                        .stream()
                        .map(Question::toJson)
                        .collect(new JsonArrayCollector())
        );
        ret.add("answer",
                src.getAnswerRecords()
                        .stream()
                        .map(RRCodec::serialize)
                        .collect(new JsonArrayCollector())
        );
        ret.add("authority",
                src.getAuthorityRecords()
                        .stream()
                        .map(RRCodec::serialize)
                        .collect(new JsonArrayCollector())
        );
        ret.add("additional",
                src.getAdditionalRecords()
                        .stream()
                        .map(RRCodec::serialize)
                        .collect(new JsonArrayCollector())
        );
        return ret;
    }
}
