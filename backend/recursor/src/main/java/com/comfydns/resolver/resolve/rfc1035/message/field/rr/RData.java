package com.comfydns.resolver.resolve.rfc1035.message.field.rr;

import com.comfydns.resolver.resolve.rfc1035.message.write.Writeable;
import com.google.gson.JsonObject;

public interface RData extends Writeable {
    public KnownRRType getRRType();
    public JsonObject writeJson();
}
