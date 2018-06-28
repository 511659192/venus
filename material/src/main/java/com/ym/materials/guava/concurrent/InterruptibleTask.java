package com.ym.materials.guava.concurrent;

import java.util.concurrent.atomic.AtomicReference;

abstract class InterruptibleTask<T> extends AtomicReference<Runnable> implements Runnable {

    private final static class DoNothingRunnable implements Runnable {
        @Override
        public void run() {
        }
    }

    private final static Runnable DONE = new DoNothingRunnable();
    private final static Runnable INTERRUPTING = new DoNothingRunnable();

    @Override
    public void run() {
        Thread thread = Thread.currentThread();
        if (!compareAndSet(null, thread)) {
            return;
        }

        boolean run = !isDone();
        T result = null;
        Throwable error = null;
        try {
            if (run) {
                result = runInterruptibly();
            }
        } catch (Throwable throwable) {
            error = throwable;
        } finally {
            if (!compareAndSet(thread, DONE)) {
                while (get() == INTERRUPTING) {
                    Thread.yield();
                }
            }
            if (run) {
                afterRanInterruptibly(result, error);
            }
        }
    }

    abstract boolean isDone();

    abstract T runInterruptibly() throws Exception;

    abstract void afterRanInterruptibly(T result, Throwable error);

    final void interruptTask() {
        Runnable runnable = get();
        if (runnable instanceof Thread && compareAndSet(runnable, INTERRUPTING)) {
            ((Thread) runnable).interrupt();
            set(DONE);
        }
    }
}
