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
        int wc = workerCountOf(c);
        if (wc < corePoolSize) {
            if (addWorker(task, true)) {
                return;
            }
            c = ctl.get();
        }

        if (isRunning(c) && queue.offer(task)) {
            int recheck = ctl.get();
            if (!isRunning(recheck) && queue.remove(task)) {
                handler.rejectedExecution(task, this);
            } else if (workerCountOf(recheck) == 0) {
                addWorker(null, false);
            }
        } else if (!addWorker(task, false)) {
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
            if (rs >= STOP || !(rs == SHUTDOWN && task == null && !queue.isEmpty())) {
                return false;
            }

            for (;;) {
                int wc = workerCountOf(c);
                int limit = isCore ? corePoolSize : maxPoolSize;
                if (wc >= CAPACITY || wc >= limit) {
                    return false;
                }
                if (ctl.compareAndSet(c, c + 1)) {
                    break retry;
                }
                if (runStateChanged(c)) {
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
                    if (isRunning(rs) || (rs == SHUTDOWN && task == null)) {
                        if (thread.isAlive()) {
                            throw new IllegalStateException();
                        }
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
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            workers.remove(worker);
            ctl.decrementAndGet();
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }

    private <T> RunnableFuture<T> newtaskFor(Callable<T> task) {
        return new FutureTask<T>(task);
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
            if (wc > CAPACITY || (timed && timeout && (wc > 1 || queue.isEmpty()))) {
                if (ctl.compareAndSet(c, c - 1)) {
                    return null;
                }
                continue;
            }

            try {
                Runnable runnable = timed ? queue.poll(keepAliveTime, timeUnit) : queue.take();
                if (runnable != null) {
                    return runnable;
                }
                timeout = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                timeout = false;
            }
        }
    }

    public void shutdown() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            advanceRunState(SHUTDOWN);
            interruptIdleWorkers(false);
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            advanceRunState(STOP);
            interruptWorkers();
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

    private List<Runnable> drainQueue() {
        List<Runnable> tasks = Lists.newArrayList();
        queue.drainTo(tasks);
        if (!queue.isEmpty()) {
            for (Runnable runnable : queue.toArray(new Runnable[0])) {
                if (queue.remove(runnable)) {
                    tasks.add(runnable);
                }
            }
        }
        return tasks;
    }

    private void interruptWorkers() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker worker : workers) {
                worker.interruptIfStart();
            }
        } finally {
            mainLock.unlock();
        }
    }

    private void advanceRunState(int targetState) {
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);
            int wc = workerCountOf(c);
            if (rs >= targetState || ctl.compareAndSet(c, ctlOf(targetState, wc))) {
                break;
            }
        }
    }

    private void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);
            if (isRunning(rs) || rs == TIDYING || (rs == SHUTDOWN && !queue.isEmpty())) {
                return;
            }
            if (workerCountOf(c) != 0) {
                interruptIdleWorkers(true);
                return;
            }

            ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, TIDYING)) {
                    try {
                        terminate();
                    } finally {
                        ctl.set(TERMINATED);
                    }
                }
            } finally {
                mainLock.unlock();
            }
        }
    }

    private void terminate() {

    }

    private void interruptIdleWorkers(boolean onlyOne) {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker worker : workers) {
                Thread thread = worker.thread;
                if (!thread.isInterrupted() && worker.tryLock()) {
                    try {
                        thread.interrupt();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        worker.unLock();
                    }
                }
                if (onlyOne) {
                    return;
                }
            }
        } finally {
            mainLock.unlock();
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

        public void interruptIfStart() {
            Thread thread;
            if (getState() >= 0 && (thread = this.thread) != null && !thread.isInterrupted()) {
                try {
                    thread.interrupt();
                } catch (Exception e) {

                }
            }
        }
    }

    private void runWorker(Worker worker) {
        Thread thread = Thread.currentThread();
        Runnable task = worker.task;
        worker.task = null;
        worker.unLock();
        boolean completedAbruptly = false;
        try {
            while (task != null || (task = getTask()) != null) {
                worker.lock();
                if (currentRunState() >= STOP || (Thread.interrupted() && currentRunState() >= STOP) && !thread.isInterrupted()) {
                    thread.interrupt();
                }
                try {
                    task.run();
                } catch (Exception ex) {
                    throw new Error(ex);
                } finally {
                    task = null;
                    worker.completedTasks++;
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
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            workers.remove(worker);
        } finally {
            mainLock.unlock();
        }

        tryTerminate();

        int c = ctl.get();
        int rs = runStateOf(c);
        if (rs < STOP) {
            if (!completedAbruptly) {
                int min = allowCoreTimeout ? 0 : corePoolSize;
                min = min == 0 && !queue.isEmpty() ? 1 : min;
                if (workerCountOf(c) >= min) {
                    return;
                }
            }
            addWorker(null, false);
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
