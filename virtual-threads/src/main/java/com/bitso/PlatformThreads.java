package com.bitso;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class PlatformThreads {

    static class Transfer {
        public int amount;
        public Transfer(int amount) {
            this.amount = amount;
        }
    }

    public static void main(String[] args) {
        
        var timestamp = Instant.now().toEpochMilli();
        
        var atomicInt = new AtomicInteger(0);
        
        try (var executor = Executors.newFixedThreadPool(8)) {
            IntStream.range(0, 10_000).forEach(i ->
                executor.submit(() -> {
                    Thread.sleep(Duration.ofMillis(20));
                    System.out.printf("Thread name=%s | Thread id=%s\n", Thread.currentThread().getName(), Thread.currentThread().threadId());
                    return atomicInt.getAndIncrement();
                }));
        }
        
        System.out.printf("time elapsed=%s%n", Instant.now().toEpochMilli() - timestamp);
        System.out.println(atomicInt.get());
    }

}
