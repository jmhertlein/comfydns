package cafe.josh.comfydns;

import cafe.josh.comfydns.rfc1035.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.field.rr.RData;

@FunctionalInterface
public interface RDataConstructionFunction {
    RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException;
}
