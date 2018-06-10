package com.ym.materials.optimize.executorWithPool2;

import com.google.common.collect.Sets;
import sun.misc.Unsafe;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by ym on 2018/6/10.
 */
public class ThreadPoolExecutor {

    private final static Unsafe UNSAFE;

    static {
        UNSAFE = Unsafe.getUnsafe();
    }

    private final static int COUNT_BITS = Integer.SIZE - 3;
    private final static int CAPACITY = (1 << COUNT_BITS) - 1;
    private final static int RUNNING = -1 << COUNT_BITS;
    private final static int SHUTDONW = 0;
    private final static int STOP = 1 << COUNT_BITS;
    private final static int TIDYING = 2 << COUNT_BITS;
    private final static int TERMINATED = 3 << COUNT_BITS;

    private final AtomicInteger ctl = new AtomicInteger(RUNNING);
    private final ReentrantLock lock = new ReentrantLock();

    private int corePoolSize;
    private int maximumPoolSize;
    private int keepAliveTime;
    private TimeUnit unit;
    private ThreadFactory threadFactory = new DefaultThreadFactory();
    private RejectedExecutionHandler handler = new AbortPolicy();
    private BlockingQueue<Runnable> workerQueue;
    private final Set<Worker> workers = Sets.newHashSet();

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workerQueue) {
        checkArgument(corePoolSize > 0);
        checkArgument(maximumPoolSize >= corePoolSize);
        checkArgument(keepAliveTime >= 0);
        checkNotNull(unit);
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.workerQueue = workerQueue;
    }

    private static int runStateOf(int c) {
        return c & ~CAPACITY;
    }

    private static int workerCountOf(int c) {
        return c & CAPACITY;
    }

    private static int ctlOf(int rs, int wc) {
        return rs | wc;
    }

    private int currentWorkerCount() {
        return workerCountOf(ctl.get());
    }

    private int currentRunState() {
        return runStateOf(ctl.get());
    }

    private boolean workerQueueIsEmpty() {
        return workerQueue.isEmpty();
    }

    private boolean workerQueueIsNotEmpty() {
        return !workerQueue.isEmpty();
    }

    private boolean compareAndIncrementWorkerCount(int c) {
        return ctl.compareAndSet(c, c + 1);
    }

    private boolean compareAndDecrementWorkerCount(int c) {
        return ctl.compareAndSet(c, c - 1);
    }

    private boolean isRunning(int c) {
        return c < SHUTDONW;
    }

    public <T> Future<T> submit(Callable<T> command) {
        checkNotNull(command);
        RunnableFuture<T> future = newTaskFor(command);
        execute(future);
        return future;
    }

    private void execute(Runnable command) {
        checkNotNull(command);
        int c = ctl.get();
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true)) {
                return;
            }
        }
    }

    private boolean addWorker(Runnable command, boolean isCore) {
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);
            if (rs > SHUTDONW) {
                return false;
            }

            if (rs == SHUTDONW && !(command == null && workerQueueIsNotEmpty())) {
                return false;
            }

            for (;;) {
                int wc = workerCountOf(c);
                if (wc > CAPACITY) {
                    return false;
                }
                int limit = isCore ? corePoolSize : maximumPoolSize;
                if (wc > limit) {
                    return false;
                }

                if (compareAndIncrementWorkerCount(c)) {
                    break retry;
                }

                if (rs != currentRunState()) {
                    continue retry;
                }
            }
        }

        boolean workerAdded = false;
        boolean workerStarted = false;
        Worker worker = null;
        try {
            worker = new Worker(command);
            Thread thread = worker.thread;
            if (thread != null) {
                ReentrantLock lock = this.lock;
                lock.lock();
                try {
                    int rs = currentRunState();
                    if (rs < SHUTDONW || (rs == SHUTDONW && command == null)) {
                        if (thread.isAlive()) {
                            throw new IllegalThreadStateException();
                        }
                        workers.add(worker);
                        workerAdded = true;
                    }
                } finally {
                    lock.unlock();
                }

                if (workerAdded) {
                    thread.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (!workerStarted) {
                addWorkerFailed(worker);
            }
        }
        return workerStarted;
    }

    private void addWorkerFailed(Worker worker) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (worker != null) {
                workers.remove(worker);
            }
            ctl.decrementAndGet();
            tryTerminate();
        } finally {
            lock.unlock();
        }
    }

    private void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);
            if (isRunning(c) || rs >= TIDYING || (rs == SHUTDONW && workerQueueIsNotEmpty())) {
                return;
            }
            if (workerCountOf(c) != 0) {
                interruptIdleWorkers(true);
                return;
            }



        }

    }

    private void interruptIdleWorkers(boolean onlyOne) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Worker worker : workers) {
                Thread thread = worker.thread;
                if (!thread.isInterrupted() && worker.tryLock()) {
                    try {
                        thread.interrupt();
                    } finally {
                        worker.unLock();
                    }
                }
                if (onlyOne) {
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private final class Worker extends AbstractQueuedLongSynchronizer implements Runnable {

        private Thread thread;
        private Runnable command;
        private volatile long completedCount = 0;

        public Worker(Runnable command) {
            setState(-1);
            this.command = command;
            this.thread = threadFactory.newThread(this);
        }

        @Override
        protected boolean tryAcquire(long arg) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(long arg) {
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

        @Override
        public void run() {

        }

        public boolean tryLock() {
            return tryAcquire(1);
        }
    }

    private <T> RunnableFuture<T> newTaskFor(Callable<T> command) {
        return new FutureTask<T>(command);
    }

    public static class AbortPolicy implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            throw new RejectedExecutionException("Task " + r.toString() +
                    " rejected from " +
                    executor.toString());
        }
    }

}
