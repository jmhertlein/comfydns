package com.comfydns.resolver.resolver.rfc1982;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.comfydns.resolver.resolver.rfc1982.SequenceSpaceArithmetic.*;

/**
 * Tests taken (metaphorically) verbatim from https://tools.ietf.org/html/rfc1982
 */
public class SequenceSpaceArithmeticTest {
    @Test
    public void testComparisons() {
        Assertions.assertEquals(1, add(0, 1, 2));
        Assertions.assertEquals(2, add(1, 1, 2));
        Assertions.assertEquals(3, add(2, 1, 2));
        Assertions.assertEquals(0, add(3, 1, 2));

        Assertions.assertTrue(greaterThan(1, 0, 2));
        Assertions.assertTrue(greaterThan(2, 1, 2));
        Assertions.assertTrue(greaterThan(3, 2, 2));
        Assertions.assertTrue(greaterThan(0, 3, 2));
    }

    @Test
    public void testLargerComparisons() {
        Assertions.assertEquals(0, add(255, 1, 8));
        Assertions.assertEquals(200, add(100, 100, 8));
        Assertions.assertEquals(44, add(200, 100, 8));

        Assertions.assertTrue(greaterThan(1, 0, 8));
        Assertions.assertTrue(greaterThan(44, 0, 8));
        Assertions.assertTrue(greaterThan(100, 0, 8));
        Assertions.assertTrue(greaterThan(100, 44, 8));
        Assertions.assertTrue(greaterThan(200, 100, 8));
        Assertions.assertTrue(greaterThan(255, 200, 8));
        Assertions.assertTrue(greaterThan(100, 255, 8));
        Assertions.assertTrue(greaterThan(0, 200, 8));
        Assertions.assertTrue(greaterThan(44, 200, 8));

        Assertions.assertFalse(lessThan(1, 0, 8));
        Assertions.assertFalse(lessThan(44, 0, 8));
        Assertions.assertFalse(lessThan(100, 0, 8));
        Assertions.assertFalse(lessThan(100, 44, 8));
        Assertions.assertFalse(lessThan(200, 100, 8));
        Assertions.assertFalse(lessThan(255, 200, 8));
        Assertions.assertFalse(lessThan(100, 255, 8));
        Assertions.assertFalse(lessThan(0, 200, 8));
        Assertions.assertFalse(lessThan(44, 200, 8));
    }
}
