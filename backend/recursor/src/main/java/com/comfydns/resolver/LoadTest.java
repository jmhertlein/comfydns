package com.comfydns.resolver;

import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTest {

    public static void main(String ... args) throws InterruptedException {
        int targetQPS = 5000;
        double msBetweenRequests = 1000.0 / targetQPS;

        Message m = new Message();
        Header h = new Header();
        m.setHeader(h);
        h.setQDCount(1);
        h.setIdRandomly();
        m.getQuestions().add(new Question("josh.cafe", KnownRRType.A, KnownRRClass.IN));
        byte[] buf = m.write();

        AtomicInteger finished = new AtomicInteger(0);
        AtomicInteger sent = new AtomicInteger(0);

        long lastPrint = 0;

        try (ExecutorService runner = Executors.newVirtualThreadPerTaskExecutor()) {
            while(true) {
                runner.submit(() -> workRequest(buf, sent, finished));
                Thread.sleep((long) msBetweenRequests);
                if(System.currentTimeMillis() - lastPrint > 1000) {
                    System.out.println((sent.get() - finished.get()) + " requests pending, " + finished.get() + " finished");
                    lastPrint = System.currentTimeMillis();
                }
            }
        }
    }

    public static void workRequest(byte[] buf, AtomicInteger sent, AtomicInteger finished) {
        try (DatagramSocket ds = new DatagramSocket()) {
            ds.send(new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), 53));
            sent.incrementAndGet();
            DatagramPacket resp = new DatagramPacket(new byte[512], 512);
            ds.receive(resp);
            finished.incrementAndGet();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
