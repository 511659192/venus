package com.ym.materials.lock;

import java.io.PipedReader;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * 回退自旋锁，在测试-测试-设置自旋锁的基础上增加了线程回退，降低锁的争用
 * 优点是在锁高争用的情况下减少了锁的争用，提高了执行的性能
 * 缺点是回退的时间难以控制，需要不断测试才能找到合适的值，而且依赖底层硬件的性能，扩展性差
 * **/
public class BackoffLock implements Lock {

    private final int minDelay, maxDelay;

    public BackoffLock(int minDelay, int maxDelay) {
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
    }

    private AtomicBoolean mutex = new AtomicBoolean(false);

    @Override
    public void lock() {
        Backoff backoff = new Backoff(minDelay, maxDelay);
        for (;;) {
            while (mutex.get()) {

            }

            if (!mutex.getAndSet(true)) {
                return;
            }

            try {
                backoff.backoff();
            } catch (InterruptedException ignore) {

            }
        }
    }

    @Override
    public void unlock() {
        mutex.set(false);
    }
}
