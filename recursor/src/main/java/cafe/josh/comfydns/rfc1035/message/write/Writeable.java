package cafe.josh.comfydns.rfc1035.message.write;

import cafe.josh.comfydns.rfc1035.message.LabelCache;

public interface Writeable {
    public byte[] write(LabelCache c, int index);
}
