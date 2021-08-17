package com.comfydns.resolver;

import com.comfydns.resolver.util.JsonArrayCollector;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class JsonArrayCollectorTest {
    @Test
    public void test() {
        JsonArray expectedArray = new JsonArray();
        List<Integer> l = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            expectedArray.add(i);
            l.add(i);
        }

        JsonArray actual = l.stream().map(JsonPrimitive::new).collect(new JsonArrayCollector());

        Assertions.assertEquals(expectedArray, actual);
    }
}
