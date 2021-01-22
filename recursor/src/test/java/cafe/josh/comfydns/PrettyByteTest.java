package cafe.josh.comfydns;

import cafe.josh.comfydns.butil.PrettyByte;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PrettyByteTest {
    @Test
    public void testByteAlignedWrite() {
        byte[] dest = new byte[2];
        PrettyByte.writeNBitUnsignedInt(312, 16, dest, 0, 0);
        Assertions.assertArrayEquals(PrettyByte.b(0b0000_0001, 0b0011_1000), dest);
    }

    @Test
    public void testNonByteAlignedWrite() {
        byte[] dest = new byte[2];
        dest[0] = (byte) 0b1100_0000;
        PrettyByte.writeNBitUnsignedInt(312, 14, dest, 0, 2);
        Assertions.assertArrayEquals(PrettyByte.b(0b1100_0001, 0b0011_1000), dest);
    }

    @Test
    public void testByteAlignedRead() {
        byte[] input = PrettyByte.b(0b0000_0001, 0b0011_1000);
        Assertions.assertEquals(312, PrettyByte.readNBitUnsignedInt(16, input, 0, 0));
    }

    @Test
    public void testNonByteAlignedRead() {
        byte[] input = PrettyByte.b(0b1100_0001, 0b0011_1000);
        Assertions.assertEquals(312, PrettyByte.readNBitUnsignedInt(14, input, 0, 2));
    }
}
