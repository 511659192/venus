package com.ym.materials.optimize.executor2;

import com.google.common.base.Preconditions;

import java.util.concurrent.*;

/**
 * Created by ym on 2018/6/15.
 */
public class FutrureTask<T> implements RunnableFuture<T> {

    private Callable<T> callable;
    private volatile int state;
    private final static int NEW = 0;
    private final static int COMPLETING = 1;
    private final static int NORMAL = 2;
    private final static int EXECUTION = 3;

    public FutrureTask(Callable task) {
        Preconditions.checkNotNull(task);
        this.callable = task;
        state = NEW;
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
