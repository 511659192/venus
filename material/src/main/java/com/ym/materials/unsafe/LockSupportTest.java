package com.ym.materials.unsafe;

import org.junit.Test;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by ym on 2018/6/9.
 */
public class LockSupportTest {
    @Test
    public void test() throws InterruptedException {
        final String a = "a";
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("sleep");
                LockSupport.park(a);
                System.out.println("awake");
            }
        });

        thread.setName("a-name");
        thread.start();
        Thread.sleep(1000000000);
        System.out.println("mom");
        LockSupport.unpark(thread);
    }

    @Test
    public void test2() throws InterruptedException {
        final String a = "a";
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("sleep");
                LockSupport.park(a);
                System.out.println("awake");
                System.out.println("isInterrupted" + Thread.currentThread().isInterrupted());
            }
        });

        thread.setName("a-name");
        thread.start();
        thread.interrupt();
    }
}
