package com.ym.materials.optimize.executor;

import com.google.common.base.Preconditions;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ym on 2018/6/3.
 */
public class MyThreaPoolExecutor {
    private final AtomicInteger ctl = new AtomicInteger(RUNNING);
    private static final int BIT_COUNT = Integer.SIZE -3;
    private static final int CAPACITY = 1 << BIT_COUNT - 1;
    private static final int RUN_STATE_MASK = ~CAPACITY;
    private static final int RUNNING = -1 << BIT_COUNT;
    private static final int SHUTDOWN = 0;
    private static final int STOP = 1 << BIT_COUNT;
    private static final int TIDYING = 2 << BIT_COUNT;
    private static final int TERMINATED = 3 << BIT_COUNT;

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

    private final int corePoolSize;
    private final int maximumPoolSize;
    private final BlockingQueue<Runnable> workQueue;
    private final long keepAliveTime;
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
        Preconditions.checkNotNull(command);
        int c = ctl.get();
        int workerCount = workerCountOf(c);
        if (workerCount < corePoolSize) { // 未达到低保线程数
            if (addWorker(command, true)) { // 添加一个核心工作线程
                return;
            }
        }
        c = ctl.get(); // 重新获取一次当前状态
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (!isRunning(c) && remove(command)) { // 线程可能被终止 删除失败说明当前任务正在执行
                reject(command);
            } else if (workerCountOf(recheck) == 0) { // 所有线程被回收 但是还有任务在队列中 添加一个线程去执行
                addWorker(null, false);
            }
        } else if (!addWorker(command, false)){
            reject(command);
        }



    }

    private void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    private boolean remove(Runnable command) {
        boolean removed = workQueue.remove(command);
        tryTerminate(); // In case SHUTDOWN and now empty
        return removed;
    }

    private void tryTerminate() {

    }

    private boolean isRunning(int c) {
        return c < SHUTDOWN; // 初始化后 结束之前返回true
    }

    private boolean addWorker(Runnable command, boolean isCore) {

        retry:
        for(;;) {
            int c = ctl.get();
            int rs = runStateOf(c);
            if (rs > SHUTDOWN) return false;
            if (rs == SHUTDOWN && (command != null || workQueue.isEmpty())) {
                return false;
            }
            for (;;) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY || wc >= (isCore ? corePoolSize : maximumPoolSize)) {
                    return false;
                }
                boolean success = ctl.compareAndSet(c, c + 1);
                if (success) {
                    break retry;
                }
                c = ctl.get();
                if (runStateOf(c) != rs) { // 状态变化 循环外层
                    continue retry;
                }
            }
        }
        return false;
    }

    private <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
        return new FutrueTask<>(task);
    }

}
