package com.comfydns.resolver.resolve.rfc1035;

import com.comfydns.resolver.util.DName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DNameTest {
    @Test
    public void testDName() {
        Assertions.assertEquals("b.a", DName.chop("c.b.a", 1));
        Assertions.assertEquals("c.b.a", DName.chop("c.b.a", 2));
        Assertions.assertEquals("a", DName.chop("a", 3));
    }
}
