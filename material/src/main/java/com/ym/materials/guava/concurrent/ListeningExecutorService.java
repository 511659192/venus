package com.ym.materials.guava.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface ListeningExecutorService extends ExecutorService {

    <T> ListenableFuture<T> submit(Callable<T> task);

    ListenableFuture<?> submit(Runnable task);

    <T> ListenableFuture<T> submit(Runnable task, T result);

    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException;

    <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException;
}
