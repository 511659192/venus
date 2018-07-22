package com.ym.materials.guava.concurrent;

import java.beans.IntrospectionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class Uninterruptibles {

    public static <V> V getUninterruptibly(Future<V> future) throws ExecutionException {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 对中断标志的使用catch的方式 阻断中断异常的传播
     * 足够时间后 再触发后续的中断
     * @param micros
     * @param timeUnit
     */
    public static void sleepUninterruptibly(long micros, TimeUnit timeUnit) {
        boolean interrupted = false;
        try {
            long remain = timeUnit.toNanos(micros);
            long end = System.nanoTime() + remain;
            while (true) {
                try {
                    TimeUnit.NANOSECONDS.sleep(remain);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remain = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
