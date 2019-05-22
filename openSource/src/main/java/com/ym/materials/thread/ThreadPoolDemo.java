// Copyright (C) 2019 Meituan
// All rights reserved
package com.ym.materials.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-02-21 22:16
 **/
public class ThreadPoolDemo {

    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                LockSupport.parkNanos(1000000000);
                System.out.println(TimeUnit.SECONDS.toNanos(1));
                executor.execute(r);
            }
        });

        for (int i = 0; i < 100; i++) {
            executor.execute(new Work(i));
        }

    }

    static class Work implements Runnable {

        private int i;

        public Work(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            System.out.println("working on " + i);
        }
    }
}