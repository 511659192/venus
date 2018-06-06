package com.ym.materials.optimize.executor;

import com.google.common.base.Preconditions;
import sun.misc.Unsafe;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by ym on 2018/6/3.
 */
public class FutrueTask<T> implements RunnableFuture<T> {

    private volatile int state;
    private static final int NEW          = 0;
    private static final int COMPLETING   = 1;
    private static final int NORMAL       = 2;
    private static final int EXCEPTIONAL  = 3;
    private static final int CANCELLED    = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED  = 6;
    private Callable<T> callable;

    private volatile Thread runner;
    private AtomicReference<Thread> threadAtomicReference;
    private static final long runnerOffset;
    private static final Unsafe UNSAFE;
    static {
        try {UNSAFE = Unsafe.getUnsafe();
            Class<FutrueTask> futrueTaskClass = FutrueTask.class;
            runnerOffset = UNSAFE.objectFieldOffset(futrueTaskClass.getDeclaredField("runner"));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    public FutrueTask(Callable<T> callable) {
        Preconditions.checkNotNull(callable);
        this.callable = callable;
        this.state = NEW;
    }

    @Override
    public void run() {
        if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread())) {
            return;
        }

        Callable<T> callable = this.callable;
        if (callable != null && state == NEW) {
            T result;
            boolean ran;
            try {
                result = callable.call();
                ran = true;
            } catch (Throwable ex) {
                result = null;
                ran = false;

            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
