package com.comfydns.resolver.resolve.system;

import com.comfydns.resolver.resolve.butil.PrettyByte;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.service.RecursiveResolver;
import com.comfydns.util.config.EnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.ExecutorService;

public class JavaNetTCPServer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(JavaNetTCPServer.class);
    private final RecursiveResolver resolver;

    private final ExecutorService workerPool;

    public JavaNetTCPServer(RecursiveResolver resolver, ExecutorService workerPool) {
        this.resolver = resolver;
        this.workerPool = workerPool;
    }

    @Override
    public void run() {
        int port = EnvConfig.getDnsServerPort();
        while(true) {
            try (ServerSocket s = new ServerSocket(port)) {
                while (true) {
                    Socket client = s.accept();
                    workerPool.submit(() -> {
                        try (client; InputStream in = client.getInputStream();
                             OutputStream out = client.getOutputStream()) {
                            byte[] rawLen = in.readNBytes(2);
                            int msgLen = (int) PrettyByte.readNBitUnsignedInt(16, rawLen, 0, 0);
                            byte[] payload = in.readNBytes(msgLen);
                            Message response = resolver.resolve(() -> {
                                Message read = Message.read(payload);
                                return new JavaNetTCPRequest(client.getInetAddress(), read);
                            });
                            byte[] respBuf = response.write();
                            byte[] len = new byte[2];
                            PrettyByte.writeNBitUnsignedInt(respBuf.length, 16, len, 0, 0);
                            out.write(len);
                            out.write(respBuf);
                        } catch (IOException e) {
                            log.warn("IO error handling client", e);
                        }
                    });
                }
            } catch (IOException e) {
                log.error("Error listening on tcp socket", e);
            }
        }
    }
}
