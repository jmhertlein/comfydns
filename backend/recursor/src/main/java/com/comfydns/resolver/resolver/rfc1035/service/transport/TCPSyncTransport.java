package com.comfydns.resolver.resolver.rfc1035.service.transport;

import com.comfydns.resolver.resolver.butil.PrettyByte;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TCPSyncTransport implements NonTruncatingSyncTransport {
    private static final int DNS_TCP_PORT = 53;

    @Override
    public byte[] send(byte[] payload, InetAddress dest) throws Exception {
        try (
                Socket socket = new Socket(dest, DNS_TCP_PORT);
                InputStream is = socket.getInputStream()
        ) {
            socket.setSoTimeout(1000);
            byte[] lenBytes = is.readNBytes(2);
            int msgLen = (int) PrettyByte.readNBitUnsignedInt(16, lenBytes, 0, 0);

            return is.readNBytes(msgLen);
        }
    }
}
