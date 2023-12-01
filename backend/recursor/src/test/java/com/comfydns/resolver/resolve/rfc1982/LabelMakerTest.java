package com.comfydns.resolver.resolve.rfc1982;

import com.comfydns.resolver.resolve.butil.PrettyByte;
import com.comfydns.resolver.resolve.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolve.rfc1035.message.LabelMaker;
import com.comfydns.resolver.resolve.rfc1035.message.MalformedLabelException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LabelMakerTest {
    @Test
    public void testPlainLabels() {
        LabelCache c = new LabelCache();
        byte[] labels = LabelMaker.makeLabels("maps.google.com", c);
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

        byte[] labels = LabelMaker.makeLabels("maps.google.com", c);
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

        byte[] labels = LabelMaker.makeLabels("maps.google.com", c);
        byte[] expected = PrettyByte.b(
                0b1100_0000, 100
        );

        Assertions.assertArrayEquals(expected, labels);
    }

    @Test
    public void testRead() throws MalformedLabelException {
        byte[] labels = LabelMaker.makeLabels("stripe.com", new LabelCache());
        LabelMaker.ReadLabels s = LabelMaker.readLabels(labels, 0);
        Assertions.assertEquals("stripe.com", s.name);
        Assertions.assertEquals(labels.length-1, s.zeroOctetPosition);
    }

    @Test
    public void testReadWithPointer() throws MalformedLabelException {
        LabelCache c = new LabelCache();
        byte[] labels = LabelMaker.makeLabels("stripe.com", c);
        c.addSuffixes("stripe.com", 0);

        byte[] pointer = LabelMaker.makeLabels("stripe.com", c);
        Assertions.assertEquals(2, pointer.length);

        byte[] both = new byte[labels.length + pointer.length];
        System.arraycopy(labels, 0, both, 0, labels.length);
        System.arraycopy(pointer, 0, both, labels.length, pointer.length);

        LabelMaker.ReadLabels first = LabelMaker.readLabels(both, 0);
        LabelMaker.ReadLabels second = LabelMaker.readLabels(both, first.zeroOctetPosition+1);

        Assertions.assertEquals("stripe.com", first.name);
        Assertions.assertEquals("stripe.com", second.name);
        Assertions.assertEquals(both.length-1, second.zeroOctetPosition);
    }

    @Test
    public void testReadWithMultiplePointers() throws MalformedLabelException {
        LabelCache c = new LabelCache();
        byte[] labels = LabelMaker.makeLabels("stripe.com", c);
        c.addSuffixes("stripe.com", 0);

        byte[] pointer1 = LabelMaker.makeLabels("status.stripe.com", c);
        c.addSuffixes("status.stripe.com", labels.length);

        byte[] pointer2 = LabelMaker.makeLabels("something.status.stripe.com", c);


        byte[] all = new byte[labels.length + pointer1.length + pointer2.length];
        System.arraycopy(labels, 0, all, 0, labels.length);
        System.arraycopy(pointer1, 0, all, labels.length, pointer1.length);
        System.arraycopy(pointer2, 0, all, labels.length + pointer1.length, pointer2.length);
        //System.out.println(PrettyByte.toString(all));
        LabelMaker.ReadLabels first = LabelMaker.readLabels(all, 0);
        LabelMaker.ReadLabels second = LabelMaker.readLabels(all, first.zeroOctetPosition+1);
        LabelMaker.ReadLabels third = LabelMaker.readLabels(all, second.zeroOctetPosition+1);

        Assertions.assertEquals("stripe.com", first.name);
        Assertions.assertEquals("status.stripe.com", second.name);
        Assertions.assertEquals("something.status.stripe.com", third.name);
        Assertions.assertEquals(all.length-1, third.zeroOctetPosition);
    }
}
