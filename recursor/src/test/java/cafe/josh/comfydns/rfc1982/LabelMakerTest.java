package cafe.josh.comfydns.rfc1982;

import cafe.josh.comfydns.PrettyByte;
import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.LabelMaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LabelMakerTest {
    @Test
    public void testPlainLabels() {
        LabelCache c = new LabelCache();
        byte[] labels = LabelMaker.makeLabel("maps.google.com", c);
        byte[] expected = PrettyByte.b(
                4, 'm', 'a', 'p', 's',
                6, 'g', 'o', 'o', 'g', 'l', 'e',
                3, 'c', 'o', 'm',
                0
        );

        Assertions.assertArrayEquals(expected, labels);
    }

    @Test
    public void testPointerLabel() {
        LabelCache c = new LabelCache();
        c.addSuffixes("google.com", 100);

        byte[] labels = LabelMaker.makeLabel("maps.google.com", c);
        byte[] expected = PrettyByte.b(
                4, 'm', 'a', 'p', 's',
                0b1100_0000, 100
        );

        Assertions.assertArrayEquals(expected, labels);
    }

    @Test
    public void testOnlyPointer() {
        LabelCache c = new LabelCache();
        c.addSuffixes("maps.google.com", 100);

        byte[] labels = LabelMaker.makeLabel("maps.google.com", c);
        byte[] expected = PrettyByte.b(
                0b1100_0000, 100
        );

        Assertions.assertArrayEquals(expected, labels);
    }
}
