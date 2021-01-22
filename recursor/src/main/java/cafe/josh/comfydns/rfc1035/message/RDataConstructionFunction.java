package cafe.josh.comfydns.rfc1035.message;

import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.rr.RData;

@FunctionalInterface
public interface RDataConstructionFunction {
    RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException;
}
