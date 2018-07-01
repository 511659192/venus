package com.ym.materials.guava.concurrent;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;

public abstract class AbstractListeningExecutorService extends AbstractExecutorService implements ListeningExecutorService {

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return TrustedListenableFutureTask.create(runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return TrustedListenableFutureTask.create(callable);
    }

    @Override
    public <T> ListenableFuture<T> submit(Callable<T> task) {
        return (ListenableFuture) super.submit(task);
    }

    @Override
    public ListenableFuture<?> submit(Runnable task) {
        return (ListenableFuture) super.submit(task);
    }

    @Override
    public <T> ListenableFuture<T> submit(Runnable task, T result) {
        return (ListenableFuture) super.submit(task, result);
    }
}
