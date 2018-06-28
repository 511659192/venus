package com.ym.materials.guava.concurrent;


import java.util.concurrent.*;

public class TrustedListenableFutureTask<T> extends AbstractFuture.TrustedFuture<T> implements RunnableFuture<T> {

    private volatile InterruptibleTask<T> task;

    TrustedListenableFutureTask(Callable<T> callable) {
        this.task = new TrustedFutureInterruptibleTask(callable);
    }

    static <V> TrustedListenableFutureTask<V> create(Runnable runnable, V result) {
        return new TrustedListenableFutureTask<V>(Executors.callable(runnable, result));
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {

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

    private final class TrustedFutureInterruptibleTask extends InterruptibleTask<T> {
        private final Callable<T> callable;

        TrustedFutureInterruptibleTask(Callable<T> callable) {
            this.callable = callable;
        }

        @Override
        boolean isDone() {
            return TrustedFutureInterruptibleTask.this.isDone();
        }

        @Override
        T runInterruptibly() throws Exception {
            return callable.call();
        }

        @Override
        void afterRanInterruptibly(T result, Throwable error) {
            if (error == null) {
                TrustedListenableFutureTask.this.set(result);
            } else {
                setException(error);
            }
        }
    }

}
