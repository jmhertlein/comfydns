package cafe.josh.comfydns.rfc1035.write;

import cafe.josh.comfydns.rfc1035.LabelCache;

import java.util.List;

public interface Writeable {
    public byte[] write(LabelCache c, int index);
}
