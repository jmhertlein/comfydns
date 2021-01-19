package cafe.josh.comfydns.rfc1035;

import cafe.josh.comfydns.RangeCheck;

import java.util.Optional;

public class LabelMaker {
    private LabelMaker() {}

    public static byte[] makeLabel(String domainName, LabelCache cache) {
        Optional<LabelCache.LabelPointer> bestIndex = cache.findBestIndex(domainName);
        if(bestIndex.isPresent()) {
            LabelCache.LabelPointer p = bestIndex.get();
            if(domainName.equalsIgnoreCase(p.name)) {
                return makePointer(p.index);
            } else {
                domainName = domainName.substring(0, domainName.length() - p.name.length() - 1); // - 1 to remove the trailing dot
            }

            // render out the domain name and then put the pointer in
            byte[] labels = makePointerlessLabel(domainName);
            byte[] ptr = makePointer(p.index);
            byte[] ret = new byte[labels.length + 2];
            System.arraycopy(labels, 0, ret, 0, labels.length);
            System.arraycopy(ptr, 0, ret, labels.length, 2);
            return ret;
        } else {
            // render out the domain name and end with a null octet
            byte[] labels = makePointerlessLabel(domainName);
            byte[] ret = new byte[labels.length+1];
            System.arraycopy(labels, 0, ret, 0, labels.length);
            ret[ret.length-1] = 0; // this is already 0 but the rfc specifies it must be 0 so I'm doing this explicitly
            return ret;
        }
    }

    private static byte[] makePointerlessLabel(String domainName) {
        byte[] ret = new byte[domainName.length()+1];
        int i = 0;
        for (String part : domainName.split("\\.")) {
            if(part.length() > 63) {
                throw new IllegalArgumentException("A domain name label must be 63 octets or less.");
            }
            ret[i] = (byte) part.length();
            i++;
            for (char c : part.toCharArray()) {
                if(c >= (1 << 8)) {
                    throw new IllegalArgumentException("Domains must be entirely 8-bit chars. Offending char: " + c + " in " + domainName);
                }
                ret[i] = (byte) c;
                i++;
            }
        }
        if(i != ret.length) {
            throw new RuntimeException("This should never happen: finished making labels but output arr is not full. i=" + i + " and arr.length is " + ret.length);
        }
        return ret;
    }

    private static byte[] makePointer(int index) {
        if(!RangeCheck.uint(14, index)) {
            throw new IllegalArgumentException("Pointer offsets aren't allowed to use the top 2 bits of the 16 total the pointer gets.");
        }

        byte msb = 0;
        msb |= 0b1100_0000;
        byte indexMsb = (byte) (index >> 8);
        indexMsb &= 0b0011_1111; // shouldn't be necessary but can't be too safe
        msb |= indexMsb;
        byte lsb = (byte) index;

        return new byte[]{msb, lsb};
    }
}
