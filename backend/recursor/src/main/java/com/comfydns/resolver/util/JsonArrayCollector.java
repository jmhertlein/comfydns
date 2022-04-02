package com.comfydns.resolver.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class JsonArrayCollector implements Collector<JsonElement, JsonArray, JsonArray> {
    static final Set<Collector.Characteristics> CH_ID = Collections
            .unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));

    @Override
    public Supplier<JsonArray> supplier() {
        return JsonArray::new;
    }

    @Override
    public BiConsumer<JsonArray, JsonElement> accumulator() {
        return JsonArray::add;
    }

    @Override
    public BinaryOperator<JsonArray> combiner() {
        return (left, right) -> {
            JsonArray ret = new JsonArray();
            ret.addAll(left);
            ret.addAll(right);
            return ret;
        };
    }

    @Override
    public Function<JsonArray, JsonArray> finisher() {
        return (o) -> o;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return CH_ID;
    }
}
