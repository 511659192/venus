package com.ym.materials.optimize.executor;

import com.google.common.base.Preconditions;

import java.util.concurrent.*;

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

    public FutrueTask(Callable<T> callable) {
        Preconditions.checkNotNull(callable);
        this.callable = callable;
        this.state = NEW;
    }

    @Override
    public void run() {

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
