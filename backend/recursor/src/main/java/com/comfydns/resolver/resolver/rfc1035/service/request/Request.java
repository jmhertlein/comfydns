package com.comfydns.resolver.resolver.rfc1035.service.request;

import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.MessageReadingException;

@FunctionalInterface
public interface Request {
    public LiveRequest begin() throws MessageReadingException, InvalidMessageException;
}
