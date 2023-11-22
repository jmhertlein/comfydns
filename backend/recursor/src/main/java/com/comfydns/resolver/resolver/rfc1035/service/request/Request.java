package com.comfydns.resolver.resolver.rfc1035.service.request;

import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.MessageReadingException;

/**
 * This is a functional interface / provider because it allows us to defer
 * as much loading as possible (e.g. some implementations can do Message.read in here)
 * so that we can keep as much error handling "inside" the resolver, in one place, as possible.
 */
@FunctionalInterface
public interface Request {
    public LiveRequest begin() throws MessageReadingException, InvalidMessageException;
}
