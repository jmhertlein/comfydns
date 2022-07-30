package com.comfydns.resolver.resolver.rfc7816;

import org.junit.jupiter.api.*;

public class QNameMinimizerTest {
    @Test
    public void testMinimizeName() {
        Assertions.assertEquals("comfydns.com",
                QNameMinimizer.minimizeQName("test.a.comfydns.com", "com")
        );

        Assertions.assertEquals("com",
                QNameMinimizer.minimizeQName("test.a.comfydns.com", "")
        );

        Assertions.assertEquals("a.comfydns.com",
                QNameMinimizer.minimizeQName("test.a.comfydns.com", "comfydns.com")
        );

        Assertions.assertEquals("test.a.comfydns.com",
                QNameMinimizer.minimizeQName("test.a.comfydns.com", "a.comfydns.com")
        );
    }

    @Test
    public void testShouldSendActualQuery() {
        Assertions.assertFalse(QNameMinimizer.shouldSendActualQuestion("test.a.comfydns.com", ""));
        Assertions.assertFalse(QNameMinimizer.shouldSendActualQuestion("test.a.comfydns.com", "com"));
        Assertions.assertFalse(QNameMinimizer.shouldSendActualQuestion("test.a.comfydns.com", "comfydns.com"));
        Assertions.assertTrue(QNameMinimizer.shouldSendActualQuestion("test.a.comfydns.com", "a.comfydns.com"));
    }
}
