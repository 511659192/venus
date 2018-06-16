package com.ym.materials.optimize.executor2;

import com.google.common.base.Preconditions;
import com.ym.materials.unsafe.UnsafeUtils;
import sun.misc.Unsafe;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by ym on 2018/6/15.
 */
public class FutureTask<T> implements RunnableFuture<T> {

    private Callable<T> callable;
    private volatile int state;
    private volatile Thread runner;
    private Object outcome;
    private volatile WaitNode waiters;
    private final static int NEW = 0;
    private final static int COMPLETING = 1;
    private final static int NORMAL = 2;
    private final static int EXCEPTIONAL = 3;
    private final static int CANCELLED = 4;
    private final static int INTERRUPTING = 5;
    private final static int INTERRUPTED = 6;

    public FutureTask(Callable task) {
        Preconditions.checkNotNull(task);
        this.callable = task;
        state = NEW;
    }

    @Override
    public void run() {
        if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread())) {
            return;
        }

        try {
            Callable<T> callable = this.callable;
            if (callable == null && state == NEW) {
                T result;
                try {
                    result = callable.call();
                    set(result);
                } catch (Exception e) {
                    setException(e);
                }
            }
        } finally {
            runner = null;
            int state = this.state;
            if (state >= INTERRUPTING) {
                handlePossibleCancellationInterrupt(state);
            }
        }
    }

    private void handlePossibleCancellationInterrupt(int state) {

    }

    private void set(T result) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = result;
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL);
            finishCompletion();
        }
    }

    private void finishCompletion() {
        for (WaitNode node; (node = waiters) != null;) {
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, node, null)) {
                for (;;) {
                    Thread thread = node.thread;
                    if (thread != null) {
                        node.thread = null;
                        LockSupport.unpark(thread);
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

    private void setException(Exception e) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = e;
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL);
            finishCompletion();
        }
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
        int state = this.state;
        if (state <= COMPLETING) {
            state = awaitDone(false, 0);
        }
        return report(state);
    }

    private T report(int state) throws ExecutionException {
        if (state == NORMAL) {
            return (T) outcome;
        }

        if (state >= CANCELLED) {
            throw new CancellationException();
        }
        throw new ExecutionException((Throwable) outcome);
    }


    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        long deadline = timed ? System.nanoTime() + nanos : 0L;
        boolean queued = false;
        WaitNode node = null;
        for (;;) {
            int state = this.state;
            if (state > COMPLETING) {
                if (node != null) {
                    node.thread = null;
                }
                return state;
            } else if (state == COMPLETING) {
                Thread.yield();
            } else if (node == null) {
                node = new WaitNode();
            } else if (!queued) {
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset, node.next = waiters, node);
            } else if (timed) {
                nanos = System.nanoTime() - deadline;
                if (nanos < 0) {
                    removeWaiter(node);
                    return this.state;
                }
                LockSupport.parkNanos(this, nanos);
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
            for (WaitNode pre = null, cur = waiters, next; cur != null; cur = next) {
                next = cur.next;
                if (cur.thread != null) {
                    pre = cur;
                } else if (pre != null) {
                    pre.next = next;
                    if (pre.thread == null) {
                        continue retry;
                    }
                } else if (!UNSAFE.compareAndSwapObject(this, waitersOffset, cur, next)) {
                    continue retry;
                }
            }
            break;
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    private class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;

        public WaitNode() {
            thread = Thread.currentThread();
        }
    }

    private final static Unsafe UNSAFE = UnsafeUtils.getUnsafe();
    private final static long stateOffset;
    private final static long waitersOffset;
    private final static long runnerOffset;

    static {
        try {
            stateOffset = UNSAFE.objectFieldOffset(FutureTask.class.getDeclaredField("state"));
            waitersOffset = UNSAFE.objectFieldOffset(FutureTask.class.getDeclaredField("waiters"));
            runnerOffset = UNSAFE.objectFieldOffset(FutureTask.class.getDeclaredField("runner"));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }
}
