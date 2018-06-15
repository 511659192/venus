package com.ym.materials.optimize.executor2;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ym on 2018/6/15.
 */
public class ThreadPoolExecutor {

    private final static int BIT_COUNT = Integer.SIZE - 3;
    private final static int CAPACITY = (1 << BIT_COUNT) - 1;
    private final static int STATE_MASK = ~CAPACITY;

    private final static int RUNNING = -1 << BIT_COUNT;
    private final static int SHUTDOWN = 0;
    private final static int STOP = 1 << BIT_COUNT;
    private final static int TIDYING = 2 << BIT_COUNT;
    private final static int TERMINATED = 3 << BIT_COUNT;

    private AtomicInteger ctl = new AtomicInteger(RUNNING);

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int keepAliveTime;
    private final TimeUnit timeUnit;
    private final ThreadFactory threadFactory = new DefaultThreadFactory();
    private final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1024);
    private final RejectedExecutionHandler handler = new DiscardPolicy();
    private final List<Worker> workers = Lists.newArrayList();
    private boolean allowCoreTimeout = false;


    private final ReentrantLock mainLock = new ReentrantLock();

    public ThreadPoolExecutor(int corePoolSize, int maxPoolSize, int keepAliveTime, TimeUnit timeUnit) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
    }

    private int runStateOf(int c) {
        return c & STATE_MASK;
    }

    private int workerCountOf(int c) {
        return c & CAPACITY;
    }

    private int ctlOf(int rs, int wc) {
        return rs | wc;
    }

    private int currentWokerCount() {
        return workerCountOf(ctl.get());
    }

    private int currentRunState() {
        return runStateOf(ctl.get());
    }

    private boolean runStateChanged(int pre) {
        return ctl.get() != pre;
    }

    public <T> Future<T> submit(Callable<T> task) {
        Preconditions.checkNotNull(task);
        RunnableFuture<T> future = newtaskFor(task);
        execute(future);
        return future;
    }

    private void execute(Runnable task) {
        Preconditions.checkNotNull(task);
        int c = ctl.get();
        int wc = currentWokerCount();
        if (wc < corePoolSize) {
            if (addWorker(task, true)) {
                return;
            }
        }

        int rs = runStateOf(c);
        if (isRunning(rs) && queue.offer(task)) {
            int reCheck = ctl.get();
            if (!isRunning(reCheck) && queue.remove(task)) {
                handler.rejectedExecution(task, this);
                return;
            }
            if (workerCountOf(reCheck) == 0) {
                addWorker(null, false);
            }
            return;
        }

        if (!addWorker(task, false)) {
            handler.rejectedExecution(task, this);
        }
    }

    private boolean isRunning(int rs) {
        return rs < SHUTDOWN;
    }

    private boolean addWorker(Runnable task, boolean isCore) {

        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            if (rs >= STOP) {
                return false;
            }

            if (rs == SHUTDOWN && (task != null || queue.isEmpty())) {
                return false;
            }

            for (;;) {
                int wc = workerCountOf(c);
                int limit = isCore ? corePoolSize : maxPoolSize;
                if (wc > limit) {
                    return false;
                }

                if (ctl.compareAndSet(c, c + 1)) {
                    break retry;
                }

                if (runStateChanged(rs)) {
                    continue retry;
                }
            }
        }

        boolean workerAdded = false;
        boolean workerStarted = false;
        Worker worker = null;

        try {
            worker = new Worker(task);
            Thread thread = worker.thread;
            if (thread != null) {
                ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    int rs = currentRunState();
                    if (isRunning(rs) || (runStateOf(rs) == SHUTDOWN && task == null)) {
                        if (thread.isAlive()) throw new IllegalThreadStateException();
                        workers.add(worker);
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    thread.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (!workerStarted) {
                addWorkerFail(worker);
            }
        }
        return workerStarted;
    }

    private void addWorkerFail(Worker worker) {

    }

    private <T> RunnableFuture<T> newtaskFor(Callable<T> task) {
        return new FutrureTask<T>(task);
    }

    public Runnable getTask() {
        boolean timeout = false;

        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);
            if (rs >= STOP || (rs == SHUTDOWN && queue.isEmpty())) {
                ctl.decrementAndGet();
                return null;
            }

            int wc = workerCountOf(c);
            boolean timed = allowCoreTimeout || wc > corePoolSize;
            if (wc > maxPoolSize || (timed && timeout && (wc > 1 || queue.isEmpty()))) { // 超时状态需要保证至少有一个worker能够处理余下的任务
                if (ctl.compareAndSet(c, c - 1)) {
                    return null;
                }
                continue;
            }

            try {
                Runnable task = timed ? queue.poll(keepAliveTime, timeUnit) : queue.take();
                if (task != null) {
                    return task;
                }
                timeout = true;
            } catch (InterruptedException retry) {
                timeout = false;
            }
        }
    }

    private final class Worker extends AbstractQueuedLongSynchronizer implements Runnable {

        Runnable task;
        final Thread thread;
        volatile long completedTasks;

        public Worker(Runnable task) {
            setState(-1);
            this.task = task;
            thread = threadFactory.newThread(task);
        }

        @Override
        protected boolean tryAcquire(long unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(thread);
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(long unused) {
            setState(0);
            setExclusiveOwnerThread(null);
            return true;
        }

        public void lock() {
            acquire(1);
        }

        public void unLock() {
            release(1);
        }

        public boolean tryLock() {
            return tryAcquire(1);
        }

        @Override
        public void run() {
            runWorker(this);
        }
    }

    private void runWorker(Worker worker) {
        Thread thread = Thread.currentThread();
        Runnable task = worker.task;
        worker.task = null;
        worker.unLock();
        boolean completedAbruptly = true;
        try {
            while (task != null || getTask() != null) {
                worker.lock();
                if ((currentRunState() >= STOP || (Thread.interrupted() && currentRunState() >= STOP)) && !thread.isInterrupted()) {
                    thread.interrupt();
                }
                try {
                    task.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    worker.unLock();
                }
            }
        } finally {
            processWorkerExit(worker, completedAbruptly);
        }
    }

    private void processWorkerExit(Worker worker, boolean completedAbruptly) {
        if (completedAbruptly) {
            ctl.decrementAndGet();
        }

        
    }

    public final class DefaultThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger poolNum = new AtomicInteger();
        private final AtomicInteger threadNum = new AtomicInteger();
        private final String prex;

        public DefaultThreadFactory() {
            group = Thread.currentThread().getThreadGroup();
            prex = "pool-" + poolNum.getAndAdd(1) + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(group, r, prex + threadNum.getAndAdd(1), 0);
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    private interface RejectedExecutionHandler {
        void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
    }

    private final class DiscardPolicy implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

        }
    }
}
