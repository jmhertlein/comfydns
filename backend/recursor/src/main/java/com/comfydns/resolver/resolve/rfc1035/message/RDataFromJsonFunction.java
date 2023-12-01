package com.comfydns.resolver.resolve.rfc1035.message;

import com.comfydns.resolver.resolve.rfc1035.message.field.rr.RData;
import com.google.gson.JsonObject;

@FunctionalInterface
public interface RDataFromJsonFunction {
    RData read(JsonObject o) throws InvalidMessageException, UnsupportedRRTypeException;
}
