package com.comfydns.resolver.resolve.rfc1035.service.transport;

import com.comfydns.resolver.resolve.butil.PrettyByte;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TCPSyncTransport implements NonTruncatingSyncTransport {
    private static final int DNS_TCP_PORT = 53;

    @Override
    public byte[] send(byte[] payload, InetAddress dest) throws Exception {
        try (
                Socket socket = new Socket(dest, DNS_TCP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()
        ) {
            socket.setSoTimeout(1000);
            byte[] payloadLen = new byte[2];
            PrettyByte.writeNBitUnsignedInt(payload.length, 16, payloadLen, 0, 0);
            os.write(payloadLen);
            os.write(payload);
            os.flush();
            byte[] lenBytes = is.readNBytes(2);
            int msgLen = (int) PrettyByte.readNBitUnsignedInt(16, lenBytes, 0, 0);

            return is.readNBytes(msgLen);
        }
    }
}
