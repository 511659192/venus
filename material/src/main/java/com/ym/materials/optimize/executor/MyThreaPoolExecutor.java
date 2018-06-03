package com.ym.materials.optimize.executor;

import com.google.common.base.Preconditions;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ym on 2018/6/3.
 */
public class MyThreaPoolExecutor {

    private final int corePoolSize;
    private final int maximumPoolSize;
    private final BlockingQueue<Runnable> workQueue;
    private final long keepAliveTime;
    private final ThreadFactory threadFactory = new DefaultThreadFactory();
    private final RejectedExecutionHandler handler = new AbortPolicy();

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
    }

    public MyThreaPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        if (corePoolSize < 0 ||
                maximumPoolSize <= 0 ||
                maximumPoolSize < corePoolSize ||
                keepAliveTime < 0)
            throw new IllegalArgumentException();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        Preconditions.checkNotNull(callable);


        return null;
    }

    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private static final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final String namePrefix;

        public DefaultThreadFactory() {
            this.group = Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndAdd(1), 0);
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    interface ThreadFactory {
        Thread newThread(Runnable runnable);
    }

    interface RejectedExecutionHandler {
        void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
    }

    static class AbortPolicy implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // do nothing
        }
    }

    interface Callable<V> {
        V call() throws Exception;
    }

    interface Future<V> {

        boolean cancel(boolean mayInterruptIfRunning);

        boolean isCancelled();

        boolean isDone();

        V get() throws InterruptedException, ExecutionException;

        V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
    }

    interface RunnableFuture<V> extends Runnable, Future<V> {
        void run();
    }
}
