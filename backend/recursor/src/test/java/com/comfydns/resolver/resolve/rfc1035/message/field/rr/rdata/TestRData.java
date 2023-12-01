package com.comfydns.resolver.resolve.rfc1035.message.field.rr.rdata;

import com.comfydns.resolver.resolve.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolve.rfc1035.message.UnsupportedRRTypeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestRData {
    @Test
    public void testTXTRData() throws InvalidMessageException, UnsupportedRRTypeException {
        TXTRData d = new TXTRData("hello, world");
        byte[] write = d.write(null, 0);
        TXTRData read = TXTRData.read(write, 0, write.length);
        Assertions.assertEquals("hello, world", read.getText());

        // larger string
        d = new TXTRData("\"k=rsa; p=MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDGoQCNwAQdJBy23MrShs1EuHqK/dtDC33QrTqgWd9CJmtM3CK2ZiTYugkhcxnkEtGbzg+IJqcDRNkZHyoRezTf6QbinBB2dbyANEuwKI5DVRBFowQOj9zvM3IvxAEboMlb0szUjAoML94HOkKuGuCkdZ1gbVEi3GcVwrIQphal1QIDAQAB\"");
        write = d.write(null, 0);
        read = TXTRData.read(write, 0, write.length);
        Assertions.assertEquals(d.getText(), read.getText());
    }
}
