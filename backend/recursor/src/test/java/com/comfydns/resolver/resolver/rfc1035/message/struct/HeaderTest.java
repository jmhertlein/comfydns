package com.comfydns.resolver.resolver.rfc1035.message.struct;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.OpCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.comfydns.resolver.resolver.butil.PrettyByte.b;

public class HeaderTest {
    @Test
    public void test() {
        Header h = new Header();
        h.setId(2345);
        h.setQR(true);
        h.setOpCode(OpCode.QUERY);
        h.setAA(false);
        h.setTC(false);
        h.setRD(true);
        h.setRA(true);
        h.setRCode(RCode.NO_ERROR);

        h.setQDCount(1);
        h.setANCount(1);
        h.setNSCount(0);
        h.setARCount(0);

        byte[] expected = b(
                0b0000_1001, 0b0010_1001,
                0b1000_0001, 0b1000_0000,
                0b0000_0000, 0b0000_0001,
                0b0000_0000, 0b0000_0001,
                0b0000_0000, 0b0000_0000,
                0b0000_0000, 0b0000_0000
        );

        Assertions.assertEquals(expected.length, h.getNetworkForm().length);
        for(int i = 0; i < expected.length; i++) {
            if(expected[i] != h.getNetworkForm()[i]) {
                Assertions.fail("Expected byte " + i + " to be " + PrettyByte.binString(expected[i]) + " but it was " + PrettyByte.binString(h.getNetworkForm()[i]));
            }
        }

        Assertions.assertEquals(2345, h.getId());

        h = new Header();
        h.setId(0b1111111111111111);
        Assertions.assertEquals(0b1111111111111111, h.getId());
    }
}
