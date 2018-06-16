package com.ym.materials.unsafe;

import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GetSet {

    private String name = "name";

    private final static long nameOffset;

    private final static Unsafe UNSAFE;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
            nameOffset = UNSAFE.objectFieldOffset(GetSet.class.getDeclaredField("name"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final Object lock = new Object();
        final int[] count = {0};
        final AtomicInteger now = new AtomicInteger(0);
        for (int i = 0; i < 100; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (now.getAndAdd(1) == 99) {
                            lock.notifyAll();
                        } else {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        count[0] = count[0] + 1;
                    }
                }
            }).start();
        }
        TimeUnit.SECONDS.sleep(2);
        System.out.println(count[0]);
    }

}
