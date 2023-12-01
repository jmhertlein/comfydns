package com.comfydns.resolver.resolve.rfc1035.message.write;

import com.comfydns.resolver.resolve.rfc1035.message.LabelCache;

public interface Writeable {
    public byte[] write(LabelCache c, int index);
}
