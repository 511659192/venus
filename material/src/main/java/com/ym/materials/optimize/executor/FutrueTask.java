package com.ym.materials.optimize.executor;

import com.google.common.base.Preconditions;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by ym on 2018/6/3.
 */
public class FutrueTask<T> implements RunnableFuture<T> {

    private volatile int state;
    private static final int NEW          = 0;
    private static final int COMPLETING   = 1;
    private static final int NORMAL       = 2;
    private static final int EXCEPTIONAL  = 3;
    private static final int CANCELLED    = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED  = 6;

    private Callable<T> callable;
    private volatile WaitNode header;
    private volatile Thread runner;
    private Object outcome;

    private static final Unsafe UNSAFE;
    private static final long runnerOffset;
    private static final long stateOffset;
    private static final long headerOffset;

    static {
        try {
            UNSAFE = getUnsafe();
            Class<FutrueTask> futrueTaskClass = FutrueTask.class;
            runnerOffset = UNSAFE.objectFieldOffset(futrueTaskClass.getDeclaredField("runner"));
            stateOffset = UNSAFE.objectFieldOffset(futrueTaskClass.getDeclaredField("state"));
            headerOffset = UNSAFE.objectFieldOffset(futrueTaskClass.getDeclaredField("header"));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    public static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe"); // 应用类加载器 需要反射获取unsafe
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
        }
        return null;
    }

    public FutrueTask(Callable<T> callable) {
        Preconditions.checkNotNull(callable);
        this.callable = callable;
        this.state = NEW;
    }

    @Override
    public void run() {
        if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread())) {
            return;
        }

        try {
            Callable<T> callable = this.callable;
            if (callable != null && state == NEW) {
                T result;
                try {
                    result = callable.call();
                    set(result);
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            runner = null;
            int state = this.state;
            if (state >= INTERRUPTING) { // 出让线程资源
                handlePossibleCancellationInterrupt(state);
            }
        }

    }

    private void handlePossibleCancellationInterrupt(int state) {
        if (state != INTERRUPTING) {
            return;
        }

        while (state == INTERRUPTING) {
            Thread.yield();
        }
    }

    private void set(T result) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            System.out.println("===========set===========");
            outcome = result;
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // 设置内存屏蔽
            finishCompletion();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        int targetState = mayInterruptIfRunning ? INTERRUPTING : CANCELLED;
        if (!(state == NEW && UNSAFE.compareAndSwapObject(this, stateOffset, NEW, targetState))) {
            return false;
        }

        try {
            if (mayInterruptIfRunning) {
                try {
                    Thread runner = this.runner;
                    if (runner != null) {
                        runner.interrupt();
                    }
                } finally {
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return state >= CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state != NEW;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        int state = this.state;
        if (state <= COMPLETING) {
            state = awaitDone(false, 0);
        }
        return report(state);
    }

    private T report(int state) throws ExecutionException {
        Object outcome = this.outcome;
        if (state == NORMAL) {
            return (T) outcome;
        }
        if (state >= CANCELLED) {
            throw new CancellationException();
        }
        throw new ExecutionException((Throwable) outcome);
    }

    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        WaitNode node = null;
        boolean queued = false;
        for (;;) {
            if (Thread.interrupted()) { // 线程是否中断
                removeWaiter(node);
                throw new InterruptedException();
            }
            int state = this.state;
            if (state > COMPLETING) { // 已经完成 或者取消
                if (node != null) {
                    node.thread = null;
                }
                return state;
            } else if (state == COMPLETING) { // 正在处理 出让线程资源
                Thread.yield();
            } else if (node == null) { // 正在执行 添加一个等待节点
                node = new WaitNode();
            } else if (!queued) { // 放置到等待队列中
                queued = UNSAFE.compareAndSwapObject(this, headerOffset, node.next = header, node);
            } else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    removeWaiter(node);
                    return state;
                }
                LockSupport.parkNanos(this, nanos); // 阻塞等待完成
            } else {
                LockSupport.park(this);
            }
        }
    }

    private void removeWaiter(WaitNode node) {
        if (node == null) {
            return;
        }
        node.thread = null;
        retry:
        for (;;) {
            for (WaitNode pred = null, current = header, next; current != null; current = next) {
                next = current.next;
                if (current.thread != null) {
                    pred = current;
                } else if (pred != null) { // 删除中间节点 或者尾节点
                    pred.next = next; // 排除掉了thread=null的节点 交由gc
                    if (pred.thread == null) {
                        continue retry;
                    }
                    current = null; // 设置当前节点为空 加快gc
                } else if (!UNSAFE.compareAndSwapObject(this, headerOffset, current, next)) { // 删除的是头结点
                    continue retry;
                }
            }
            break;
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Preconditions.checkNotNull(unit);
        int state = this.state;
        if (state <= COMPLETING && (state = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING) {
            throw new TimeoutException();
        }
        return report(state);
    }

    public void setException(Throwable exception) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = exception;
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL);
            finishCompletion();
        }
    }

    private void finishCompletion() {
        for (WaitNode node; (node = header) != null; ) {
            if (UNSAFE.compareAndSwapObject(this, headerOffset, node, null)) { // 重置等待队列
                for (;;) {
                    Thread thread = node.thread;
                    if (thread != null) {
                        node.thread = null;
                        LockSupport.unpark(thread); //
                    }
                    WaitNode next = node.next;
                    if (next == null) {
                        break;
                    }
                    node.next = null;
                    node = next;
                }
                break;
            }
        }
        done();
        callable = null;
    }

    private void done() {

    }

    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;
        WaitNode() { thread = Thread.currentThread(); }
    }
}
