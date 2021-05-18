package com.comfydns.resolver.resolver.rfc1035.message.write;

import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;

public interface Writeable {
    public byte[] write(LabelCache c, int index);
}
