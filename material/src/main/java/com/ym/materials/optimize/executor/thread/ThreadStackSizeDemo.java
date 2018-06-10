package com.ym.materials.optimize.executor.thread;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by ym on 2018/6/10.
 */
public class ThreadStackSizeDemo {

    @Test
    public void testStackSize() throws Exception {
        new Thread(Thread.currentThread().getThreadGroup(), new Runnable() {
            @Override
            public void run() {
                loop(10000);
            }
        }, "thread", 0).start();
        TimeUnit.SECONDS.sleep(1);
    }

    private void loop(int i) {
        if (i > 1) {
            loop(i - 1);
        }
        System.out.println(i);
    }
}
