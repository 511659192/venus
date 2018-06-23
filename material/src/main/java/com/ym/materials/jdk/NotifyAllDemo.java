package com.ym.materials.jdk;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ym on 2018/6/14.
 */
public class NotifyAllDemo {

    /**
     * notifyAll后 程序将依次执行
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        final Object lock = new Object();
        final AtomicInteger cnt = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            final int now = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                            System.out.println("now is " + now + " enter");
                            int c = cnt.getAndAdd(1);
                            if (c == 4) {
                                lock.notifyAll();
                            } else {
                                lock.wait();
                            }
                            TimeUnit.SECONDS.sleep(1);
                            System.out.println("now is " + now + " running");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        System.out.println("done");
    }
}
