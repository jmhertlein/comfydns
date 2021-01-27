package cafe.josh.comfydns.rfc1035.message;

import cafe.josh.comfydns.butil.PrettyByte;
import cafe.josh.comfydns.butil.RangeCheck;

import java.util.Optional;

public class LabelMaker {
    private LabelMaker() {}

    public static ReadLabels readLabels(byte[] content, int startPos) throws MalformedLabelException {
        int pos = startPos;
        StringBuilder b = new StringBuilder();
        Integer endOfCurrentLabel = null;
        byte cur;
        do {
            cur = content[pos];
            if(cur == 0) {
                continue;
            } else if((cur & (byte) 0b1100_0000) == (byte) 0b1100_0000) {
                // it's a pointer.
                if(endOfCurrentLabel == null) {
                    endOfCurrentLabel = pos + 1;
                }
                if(pos+1 >= content.length) {
                    throw new MalformedLabelException("Malformed pointer had no second octet, unable to follow.");
                }
                pos = (int) PrettyByte.readNBitUnsignedInt(14, content, pos, 2);
                if(pos >= content.length) {
                    throw new MalformedLabelException("Pointer described invalid jump index: " + pos + "(length is " + content.length + ")");
                }
                continue;
            }

            int length = content[pos];
            pos++;
            for(int i = pos; i < pos+length &&  i < content.length; i++) {
                b.append((char) content[i]);
            }
            b.append('.');
            pos += length;

        } while(cur != 0 && pos < content.length);

        if(cur != 0) {
            throw new MalformedLabelException("Label does not end in a 0 octet. Label start position was " + startPos + " and end position was " + pos + " and message length was " + content.length);
        }

        if(b.length() > 0) {
            b.deleteCharAt(b.length()-1);
        }

        if(endOfCurrentLabel == null ) {
            return new ReadLabels(b.toString(), pos, (pos+1) - startPos);
        } else {
            return new ReadLabels(b.toString(), endOfCurrentLabel, (endOfCurrentLabel+1) - startPos);
        }

    }

    public static byte[] makeLabels(String domainName, LabelCache cache) {
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

    public static class ReadLabels {
        public final String name;
        public final int zeroOctetPosition;
        public final int length;

        public ReadLabels(String name, int zeroOctetPosition, int length) {
            this.name = name;
            this.zeroOctetPosition = zeroOctetPosition;
            this.length = length;
        }
    }
}
