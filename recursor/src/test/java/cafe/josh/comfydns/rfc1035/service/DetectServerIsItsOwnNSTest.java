package cafe.josh.comfydns.rfc1035.service;

import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.ARData;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.NSRData;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.search.state.HandleResponseToZoneQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class DetectServerIsItsOwnNSTest {

    @Test
    public void testRRDetection() {
        Message m = new Message();
        m.setHeader(new Header());
        m.getHeader().setNSCount(1);
        m.getHeader().setQR(true);
        m.getAuthorityRecords().add(new RR<>("zdns.google", KnownRRType.NS, KnownRRClass.IN, 10800,
                new NSRData("ns2.zdns.google")));
        Assertions.assertFalse(HandleResponseToZoneQuery.filterNSDNamesInTheirOwnZoneWithoutARecords(m).isEmpty());
    }

    @Test
    public void testOKCase() throws UnknownHostException {
        Message m = new Message();
        m.setHeader(new Header());
        m.getHeader().setNSCount(1);
        m.getHeader().setQR(true);
        m.getAuthorityRecords().add(new RR<>("zdns.google", KnownRRType.NS, KnownRRClass.IN, 10800,
                new NSRData("ns2.zdns.google")));
        m.getAdditionalRecords().add(new RR<>("ns2.zdns.google", KnownRRType.A, KnownRRClass.IN, 10800,
                new ARData((Inet4Address) Inet4Address.getByName("192.168.1.24"))));
        Assertions.assertTrue(HandleResponseToZoneQuery.filterNSDNamesInTheirOwnZoneWithoutARecords(m).isEmpty());
    }
}
