package com.ym.materials.lock;

import java.util.Random;
/**
 * 回退算法，降低锁争用的几率
 * **/
public class Backoff {
     private final int minDelay, maxDelay;

     private int limit;

     final Random random;

    public Backoff(int minDelay, int maxDelay) {
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        limit = minDelay;
        random = new Random();
    }

    public void backoff() throws InterruptedException {
        int delay = random.nextInt(limit);
        limit = Math.min(maxDelay, 2 * limit);
        Thread.sleep(delay);
    }
}
