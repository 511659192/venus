package com.ym.materials.optimize.executor;

import com.google.common.base.Preconditions;
import org.junit.internal.runners.statements.RunAfters;

import javax.swing.*;
import javax.swing.plaf.TableHeaderUI;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ym on 2018/6/3.
 */
public class MyThreaPoolExecutor {
    private final AtomicInteger ctl = new AtomicInteger(RUNNING);
    private static final int BIT_COUNT = Integer.SIZE - 3;
    private static final int CAPACITY = 1 << BIT_COUNT - 1;
    private static final int RUN_STATE_MASK = ~CAPACITY;
    private static final int RUNNING = -1 << BIT_COUNT; //  正常态 可以接受新的任务 也可以处理队列
    private static final int SHUTDOWN = 0; // 不接受新任务 可以处理队列
    private static final int STOP = 1 << BIT_COUNT; // 不接受新任务 不处理队列 中断正在处理的任务
    private static final int TIDYING = 2 << BIT_COUNT; // 过度状态 任务执行完毕 当前线程没有有效的线程 需要调用terminate方法
    private static final int TERMINATED = 3 << BIT_COUNT; // 终止状态

    private final ReentrantLock mainLock = new ReentrantLock();
    private final Condition termination = mainLock.newCondition();

    private final HashSet<Worker> workers = new HashSet<Worker>();
    private int largestPoolSize;

    private long completedTaskCount;

    private static int runStateOf(int c) {
        return c & RUN_STATE_MASK;
    }

    private static int workerCountOf(int c) {
        return c & CAPACITY;
    }

    private static int ctlOf(int rs, int sc) {
        return rs | sc;
    }

    public static void main(String[] args) {
        MyThreaPoolExecutor executor = new MyThreaPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1024));
        System.out.println(executor.ctl.get());
        System.out.println(MyThreaPoolExecutor.RUNNING);
    }

    private volatile boolean allowCoreThreadTimeOut;
    private final long keepAliveTime;
    private final int corePoolSize;
    private final int maximumPoolSize;
    private final BlockingQueue<Runnable> workQueue;
    private final ThreadFactory threadFactory = new DefaultThreadFactory();
    private final RejectedExecutionHandler handler = new RejectedExecutionHandler.AbortPolicy();

    public MyThreaPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        Preconditions.checkArgument(corePoolSize >= 0);
        Preconditions.checkArgument(maximumPoolSize > 0);
        Preconditions.checkArgument(maximumPoolSize >= corePoolSize);
        Preconditions.checkArgument(keepAliveTime >= 0);
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
    }

    public <T> Future<T> submit(Callable<T> task) {
        Preconditions.checkNotNull(task);
        RunnableFuture<T> futureTask = newTaskFor(task);
        execute(futureTask);
        return futureTask;
    }

    private <T> void execute(Runnable command) {
        System.out.println("-------------------execute--------------------");
        Preconditions.checkNotNull(command);
        int c = ctl.get();
        int workerCount = workerCountOf(c);
        if (workerCount < corePoolSize) { // 未达到低保线程数
            if (addWorker(command, true)) { // 添加一个核心工作线程
                return;
            }
        }
        c = ctl.get();
        if (isRunning(c) && workQueue.offer(command)) { // 核心线程已经全部启动 优先放入队列中
            int recheck = ctl.get();
            if (!isRunning(recheck) && remove(command)) { // 线程可能被终止 删除成功 调用拒绝策略
                reject(command);
            } else if (workerCountOf(recheck) == 0) { // 添加成功 但是所有线程已经被回收 添加一个工作线程
                addWorker(null, false);
            }
        } else if (!addWorker(command, false)){ // 核心线程满载 队列也满载的情况下 尝试开辟一个新的线程
            reject(command);
        }
    }

    private void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    private boolean remove(Runnable command) {
        System.out.println("-------------------remove--------------------");
        boolean removed = workQueue.remove(command);
        tryTerminate(); // In case SHUTDOWN and now empty
        return removed;
    }

    private void tryTerminate() {
        System.out.println("-------------------tryTerminate--------------------");
        for (;;) {
            int c = ctl.get();
            if (isRunning(c) || c >= TIDYING || (runStateOf(c) == SHUTDOWN && !workQueue.isEmpty())) {
                return;
            }

            if (workerCountOf(c) != 0) {
                interruptIdleWorkers(true);
                return;
            }

            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, TIDYING)) {
                    try {
                        tryTerminate();
                    } finally {
                        ctl.set(TERMINATED);
                        termination.signalAll();
                    }
                }
                return;
            } finally {
                mainLock.unlock();
            }
        }
    }

    private void interruptIdleWorkers(boolean onlyone) {
        System.out.println("-------------------interruptIdleWorkers--------------------");
        mainLock.lock();
        try {
            for (Worker worker : workers) {
                Thread thread = worker.thread;
                if (!thread.isInterrupted() && worker.tryLock()) {
                    try {
                        thread.interrupt();
                    } finally {
                        worker.unLock();
                    }
                    if (onlyone) {
                        break;
                    }
                }
            }
        } finally {
            mainLock.unlock();
        }
    }

    private boolean isRunning(int c) {
        return c < SHUTDOWN; // 初始化后 结束之前返回true
    }

    private boolean addWorker(Runnable command, boolean isCore) {
        System.out.println("-------------------addWorker--------------------");
        retry:
        for(;;) {
            int c = ctl.get();
            int rs = runStateOf(c);
            if (rs > SHUTDOWN) return false; // stop 以上状态 直接返回
            if (rs == SHUTDOWN && (command != null || workQueue.isEmpty())) { // 线程关闭中 仅仅允许添加工作线程 不允许添加任务
                return false;
            }
            for (;;) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY || wc >= (isCore ? corePoolSize : maximumPoolSize)) { // 有超过的可能
                    return false;
                }
                if (ctl.compareAndSet(c, c + 1)) {
                    break retry;
                }
                c = ctl.get();
                if (runStateOf(c) != rs) { // 状态变化 循环外层
                    continue retry;
                }
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;

        try {
            w = new Worker(command);
            final Thread thread = w.thread;
            if (thread == null) {
                return workerStarted;
            }

            try {
                mainLock.lock();
                int rs = runStateOf(ctl.get());
                if (rs < SHUTDOWN || (rs == SHUTDOWN && command == null)) {
                    if (thread.isAlive()) {
                        throw new IllegalThreadStateException();
                    }
                    workers.add(w);
                    int size = workers.size();
                    if (size > largestPoolSize) {
                        largestPoolSize = size;
                    }
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            if (workerAdded) {
                thread.start();
                workerStarted = true;
            }
        } finally {
            if (!workerStarted) {
                addWorkerFailed(w);
            }
        }
        return workerStarted;
    }

    private void addWorkerFailed(Worker w) {
        System.out.println("-------------------addWorkerFailed--------------------");
        mainLock.lock();
        try {
            if (w != null) {
                workers.remove(w);
                ctl.decrementAndGet();
                tryTerminate();
            }
        } finally {
            mainLock.unlock();
        }
    }

    private <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
        return new FutrueTask<>(task);
    }


    private final class Worker extends AbstractQueuedSynchronizer implements Runnable {

        private Runnable task;
        private Thread thread;
        volatile long completedTasks;

        public Worker(Runnable task) {
            setState(-1);
            this.task = task;
            this.thread = threadFactory.newThread(this);
        }

        @Override
        public void run() {
            runWorker(this);
        }

        @Override
        protected boolean tryAcquire(int arg) {
            System.out.println("-------------------tryAcquire--------------------");
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            System.out.println("-------------------tryRelease--------------------");
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void unLock() {
            release(1);
        }

        public void lock() {
            acquire(1);
        }

        public boolean tryLock() {
            return tryAcquire(1);
        }
    }

    private final void runWorker(Worker w) {
        System.out.println("-------------------runWorker--------------------");
        Thread thread = Thread.currentThread();
        Runnable task = w.task;
        w.task = null;
        w.unLock();
        boolean completedAbruptly = true;
        try {
            System.out.println(task == null);
            while (task != null || (task = getTask()) != null) {
                w.lock();
                if ((runStateOf(ctl.get()) >= STOP || (thread.interrupted() && runStateOf(ctl.get()) >= STOP)) && !thread.isInterrupted()) {
                    thread.interrupt();
                }
                try {
                    Throwable throwable = null;
                    try {
                        System.out.println("====================");
                        task.run();
                    } catch (Exception e) {
                        throwable = e;
                        throw e;
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unLock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }

    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        System.out.println("-------------------processWorkerExit--------------------");
        if (completedAbruptly) {
            decrementWorkerCount();
        }

        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {

            completedTaskCount += w.completedTasks;
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }

        tryTerminate();

        int c = ctl.get();
        if (runStateOf(c) < STOP) {
            if (!completedAbruptly) {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                min = min == 0 && !workQueue.isEmpty() ? 1 : min;
                if (workerCountOf(c) > min) {
                    return;
                }
            }
            addWorker(null, false);
        }
    }

    private Runnable getTask() {
        System.out.println("-------------------getTask--------------------");
        boolean timeout = false;
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);
            if (rs >= STOP || workQueue.isEmpty()) {
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
            if ((wc > maximumPoolSize || (timed && timeout)) && (wc > 1 || workQueue.isEmpty())) { // 超过最大限额 怎么处理呢
                if (compareAndDecrementWorkerCount(c)) {
                    return null;
                }
                continue;
            }

            try {
                Runnable r = timed ? workQueue.poll(keepAliveTime, TimeUnit.MICROSECONDS) : workQueue.take();
                if (r != null) {
                    return r;
                }
                timeout = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                timeout = false;
            }
        }
    }

    private boolean compareAndDecrementWorkerCount(int c) {
        return ctl.compareAndSet(c, c - 1);
    }

    private void decrementWorkerCount() {
        ctl.decrementAndGet();
    }
}
