package com.ym.materials.unsafe;

import com.google.common.base.Stopwatch;
import org.junit.Test;
import sun.misc.Unsafe;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by ym on 2018/6/9.
 */
public class LockSupportTest {

    private volatile String name;
    private final static Unsafe UNSAFE = UnsafeUtils.getUnsafe();
    private final static long nameOffset;
    private int loop = 10000000;

    static {
        try {
            nameOffset = UNSAFE.objectFieldOffset(LockSupportTest.class.getDeclaredField("name"));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

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
    
    @Test
    public void testVolatile() throws Exception {
        name = "name";
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i = 0; i < loop; i++) {
            name = "name";
        }
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }

    @Test
    public void testPutOrderInt() throws Exception {
        UNSAFE.putOrderedObject(this, nameOffset, "name");
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i = 0; i < loop; i++) {
            UNSAFE.putOrderedObject(this, nameOffset, "name");
        }
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }
}
