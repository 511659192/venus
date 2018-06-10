package com.ym.materials.optimize.executorWithPool2;

import com.google.common.base.Preconditions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by ym on 2018/6/10.
 */
public class FutureTask<T> implements RunnableFuture<T> {

    private Callable<T> callable;
    private int state;
    private Thread runner;

    private final static int NEW = 0;

    public FutureTask(Callable<T> callable) {
        checkNotNull(callable);
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
