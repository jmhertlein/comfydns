package com.comfydns.resolver.resolve.rfc1035.message;

import com.comfydns.resolver.resolve.rfc1035.message.field.rr.RData;

@FunctionalInterface
public interface RDataConstructionFunction {
    RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException;
}
