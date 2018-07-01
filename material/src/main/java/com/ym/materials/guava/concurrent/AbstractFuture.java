package com.ym.materials.guava.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.ym.materials.guava.concurrent.Futures.getDone;
import static java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractFuture<T> extends FluentFuture<T> {

    private final static Logger log = LoggerFactory.getLogger(AbstractFuture.class);

    private static final AtomicHelper ATOMIC_HELPER;

    private volatile Object value;
    private volatile Listener listeners;
    private volatile Waiter waiters;

    private static final Object NULL = new Object();

    static {
        AtomicHelper helper;
        Throwable thrownUnsafeFailure = null;
        Throwable thrownAtomicReferenceFieldUpdaterFailure = null;
        try {
            helper = new UnsafeAtomicHelper();
        } catch (Throwable unsafeFailure) {
            thrownUnsafeFailure = unsafeFailure;
            try {
                helper = new SafeAtomicHelper(
                        newUpdater(Waiter.class, Thread.class, "thread"),
                        newUpdater(Waiter.class, Waiter.class, "next"),
                        newUpdater(AbstractFuture.class, Waiter.class, "waiters"),
                        newUpdater(AbstractFuture.class, Listener.class, "listeners"),
                        newUpdater(AbstractFuture.class, Object.class, "value")
                );
            } catch (Throwable atomicReferenceFieldUpdaterFailure) {
                thrownAtomicReferenceFieldUpdaterFailure = atomicReferenceFieldUpdaterFailure;
                helper  = new SynchronizedHelper();
            }
        }

        ATOMIC_HELPER = helper;

        if (thrownAtomicReferenceFieldUpdaterFailure != null) {
            if (thrownAtomicReferenceFieldUpdaterFailure != null) {
                log.error("UnsafeAtomicHelper is broken!", thrownUnsafeFailure);
                log.error("SafeAtomicHelper is broken!", thrownAtomicReferenceFieldUpdaterFailure);
            }
        }
    }

    protected boolean setException(Throwable throwable) {
        Object value = new Failure(throwable);
        if (ATOMIC_HELPER.casValue(this, null, value)) {
            complete(this);
            return true;
        }
        return false;
    }

    private static void complete(AbstractFuture<?> future) {
        Listener next = null;
        out:
        for (;;) {
            future.releaseWaiters();
            future.afterDone();
            next = future.clearListeners(next);
            future = null;
            while (next != null) {
                Listener current = next;
                next = next.next;
                Runnable task = current.task;
                if (task instanceof SetFuture) {
                    SetFuture<?> setFuture = (SetFuture<?>) task;
                    future = setFuture.owner;
                    if (future.value == setFuture) {
                        Object valueToSet = getFutureValue(setFuture.future);
                        if (ATOMIC_HELPER.casValue(future, setFuture, valueToSet)) {
                            continue out;
                        }
                    }
                } else {
                    executeListener(task, current.executor);
                }
            }
            break;
        }
    }

    private Listener clearListeners(Listener next) { // 这里传入的是空
        Listener head;
        do {
            head = listeners;
        } while (!ATOMIC_HELPER.casListeners(this, head, Listener.TOMBSTONE));
        Listener reversedList = next;
        for (;head != null; head = head.next) {
            Listener tmp = head;
            tmp.next = reversedList;
            reversedList = tmp;
        }
        return reversedList;
    }

    private void afterDone() {
    }

    private void releaseWaiters() {
        Waiter head;
        do {
            head = waiters;
        } while (!ATOMIC_HELPER.casWaiters(this, head, Waiter.TOMBSTONE));
        for (Waiter current = head; current != null; current  = current.next) {
            current.unpark();
        }
    }

    protected boolean set(T value) {
        Object valueToSet = value == null ? NULL : value;
        if (ATOMIC_HELPER.casValue(this, null, valueToSet)) {
            complete(this);
            return true;
        }
        return false;
    }

    public void addListener(Runnable listener, Executor executor) {
        assertNotNull(listener);
        assertNotNull(executor);
        Listener oldHead = this.listeners;
        if (oldHead != Listener.TOMBSTONE) {
            Listener newNode = new Listener(listener, executor);
            do {
                newNode.next = oldHead;
                if (ATOMIC_HELPER.casListeners(this, oldHead, newNode)) {
                    return;
                }
                oldHead = listeners;
            } while (oldHead != Listener.TOMBSTONE);
        }
        executeListener(listener, executor);
    }

    private static void executeListener(Runnable runnable, Executor executor) {
        try {
            executor.execute(runnable);
        } catch (RuntimeException e) {
            log.error("RuntimeException while executing runnable " + runnable + " with executor " + executor, e);
        }
    }

    abstract static class TrustedFuture<T> extends AbstractFuture<T> {

        public final void addListener(Runnable listener, Executor executor) {
            super.addListener(listener, executor);
        }
    }

    private final static class Failure {
        final Throwable throwable;

        public Failure(Throwable throwable) {
            this.throwable = throwable;
        }

        static final Failure FALLBACK_INSTANCE = new Failure(new Throwable("failure occured while trying to finish a future."){
            @Override
            public synchronized Throwable fillInStackTrace() {
                return this;
            }
        });
    }

    private abstract static class AtomicHelper {
        abstract void putThread(Waiter waiter, Thread newValue);

        abstract void putNext(Waiter waiter, Waiter newValue);

        abstract boolean casWaiters(AbstractFuture<?> future, Waiter expect, Waiter update);

        abstract boolean casListeners(AbstractFuture<?> future, Listener expect, Listener update);

        abstract boolean casValue(AbstractFuture<?> future, Object expect, Object update);
    }

    private static final class UnsafeAtomicHelper extends AtomicHelper {
        final static Unsafe UNSAFE;
        final static long LISTENERS_OFFSET;
        final static long WAITERS_OFFSET;
        final static long VALUE_OFFSET;
        final static long WAITER_THREAD_OFFSET;
        final static long WAITER_NEXT_OFFSET;

        static {
            Unsafe unsafe;
            try {
                unsafe = Unsafe.getUnsafe();
            } catch (SecurityException tryReflectionInstead) {
                try {
                    unsafe = AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>() {
                        @Override
                        public Unsafe run() throws Exception {
                            Class<Unsafe> unsafeClass = Unsafe.class;
                            for (Field field : unsafeClass.getDeclaredFields()) {
                                field.setAccessible(true);
                                Object x = field.get(null);
                                if (unsafeClass.isInstance(x)) {
                                    return unsafeClass.cast(x);
                                }
                            }
                            throw new NoSuchFieldException("the unsafe");
                        }
                    });
                } catch (PrivilegedActionException e) {
                    throw new RuntimeException("Could not initialize intrinsics", e.getCause());
                }
            }

            try {
                Class<?> abstractFuture = AbstractFuture.class;
                WAITERS_OFFSET = unsafe.objectFieldOffset(abstractFuture.getDeclaredField("waiters"));
                LISTENERS_OFFSET = unsafe.objectFieldOffset(abstractFuture.getDeclaredField("listeners"));
                VALUE_OFFSET = unsafe.objectFieldOffset(abstractFuture.getDeclaredField("value"));
                WAITER_THREAD_OFFSET = unsafe.objectFieldOffset(Waiter.class.getDeclaredField("thread"));
                WAITER_NEXT_OFFSET = unsafe.objectFieldOffset(Waiter.class.getDeclaredField("next"));
                UNSAFE = unsafe;
            } catch (Exception e) {
                throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }

        @Override
        void putThread(Waiter waiter, Thread newValue) {
            UNSAFE.putObject(waiter, WAITER_THREAD_OFFSET, newValue);
        }

        @Override
        void putNext(Waiter waiter, Waiter newValue) {
            UNSAFE.putObject(waiter, WAITER_NEXT_OFFSET, newValue);
        }

        @Override
        boolean casWaiters(AbstractFuture<?> future, Waiter expect, Waiter update) {
            return UNSAFE.compareAndSwapObject(future, WAITERS_OFFSET, expect, update);
        }

        @Override
        boolean casListeners(AbstractFuture<?> future, Listener expect, Listener update) {
            return UNSAFE.compareAndSwapObject(future, LISTENERS_OFFSET, expect, update);
        }

        @Override
        boolean casValue(AbstractFuture<?> future, Object expect, Object update) {
            return UNSAFE.compareAndSwapObject(future, VALUE_OFFSET, expect, update);
        }
    }

    private static final class SafeAtomicHelper extends AtomicHelper {
        final AtomicReferenceFieldUpdater<Waiter, Thread> waiterThreadUpdater;
        final AtomicReferenceFieldUpdater<Waiter, Waiter> waiterNextUpdater;
        final AtomicReferenceFieldUpdater<AbstractFuture, Waiter> waitersUpdater;
        final AtomicReferenceFieldUpdater<AbstractFuture, Listener> listenersUpdater;
        final AtomicReferenceFieldUpdater<AbstractFuture, Object> valueUpdater;

        public SafeAtomicHelper(AtomicReferenceFieldUpdater<Waiter, Thread> waiterThreadUpdater,
                                AtomicReferenceFieldUpdater<Waiter, Waiter> waiterNextUpdater,
                                AtomicReferenceFieldUpdater<AbstractFuture, Waiter> waitersUpdater,
                                AtomicReferenceFieldUpdater<AbstractFuture, Listener> listenersUpdater,
                                AtomicReferenceFieldUpdater<AbstractFuture, Object> valueUpdater) {
            this.waiterThreadUpdater = waiterThreadUpdater;
            this.waiterNextUpdater = waiterNextUpdater;
            this.waitersUpdater = waitersUpdater;
            this.listenersUpdater = listenersUpdater;
            this.valueUpdater = valueUpdater;
        }


        @Override
        void putThread(Waiter waiter, Thread newValue) {
            waiterThreadUpdater.lazySet(waiter, newValue);
        }

        @Override
        void putNext(Waiter waiter, Waiter newValue) {
            waiterNextUpdater.lazySet(waiter, newValue);
        }

        @Override
        boolean casWaiters(AbstractFuture<?> future, Waiter expect, Waiter update) {
            return waitersUpdater.compareAndSet(future, expect, update);
        }

        @Override
        boolean casListeners(AbstractFuture<?> future, Listener expect, Listener update) {
            return listenersUpdater.compareAndSet(future, expect, update);
        }

        @Override
        boolean casValue(AbstractFuture<?> future, Object expect, Object update) {
            return valueUpdater.compareAndSet(future, expect, update);
        }
    }

    private static final class SynchronizedHelper extends AtomicHelper {

        @Override
        void putThread(Waiter waiter, Thread newValue) {
            waiter.thread = newValue;
        }

        @Override
        void putNext(Waiter waiter, Waiter newValue) {
            waiter.next = newValue;
        }

        @Override
        boolean casWaiters(AbstractFuture<?> future, Waiter expect, Waiter update) {
            synchronized (future) {
                if (future.waiters == expect) {
                    future.waiters  = update;
                    return true;
                }
            }
            return false;
        }

        @Override
        boolean casListeners(AbstractFuture<?> future, Listener expect, Listener update) {
            synchronized (future) {
                if (future.listeners == expect) {
                    future.listeners = update;
                    return true;
                }
            }
            return false;
        }

        @Override
        boolean casValue(AbstractFuture<?> future, Object expect, Object update) {
            synchronized (future) {
                if (future.value == expect) {
                    future.value = update;
                    return true;
                }
            }
            return false;
        }
    }

    private static final class Waiter {
        static final Waiter TOMBSTONE = new Waiter(false);
        volatile Thread thread;
        volatile Waiter next;

        Waiter(boolean unused) {
        }

        void setNext(Waiter next) {
            ATOMIC_HELPER.putNext(this, next);
        }

        void unpark() {
            Thread thread = this.thread;
            if (thread != null) {
                thread = null;
                LockSupport.unpark(thread);
            }
        }
    }

    private static final class Listener {
        final static Listener TOMBSTONE = new Listener(null, null);
        final Runnable task;
        final Executor executor;
        Listener next;

        public Listener(Runnable task, Executor executor) {
            this.task = task;
            this.executor = executor;
        }
    }

    private static final class SetFuture<T> implements Runnable {
        final AbstractFuture<T> owner;
        final ListenableFuture<? extends T> future;

        public SetFuture(AbstractFuture<T> owner, ListenableFuture<? extends T> future) {
            this.owner = owner;
            this.future = future;
        }

        @Override
        public void run() {
            if (owner.value != this) {
                return;
            }
            Object valueToSet = getFutureValue(future);
            if (ATOMIC_HELPER.casValue(owner, this, valueToSet)) {
                complete(owner);
            }
            
        }
    }

    private static Object getFutureValue(ListenableFuture<?> future) {
        Object valueToSet;
        if (future instanceof TrustedFuture) {
            Object v = ((AbstractFuture) future).value;
            if (v instanceof Cancellation) {
                Cancellation cancellation = (Cancellation) v;
                if (cancellation.wasInterrupted) {
                    v = cancellation.cause != null ? new Cancellation(false, cancellation.cause) : Cancellation.CAUSELESS_CANCELLED;
                }
            }
            return v;
        } else {
            try {
                Object v = getDone(future);
                valueToSet = v == null ? NULL : v;
            } catch (ExecutionException e) {
                valueToSet = new Failure(e.getCause());
            } catch (CancellationException e) {
                valueToSet = new Cancellation(false, e);
            } catch (Throwable t) {
                valueToSet = new Failure(t);
            }
        }
        return valueToSet;
    }

    private static final boolean GENERATE_CANCELLATION_CAUSES =
            Boolean.parseBoolean(
                    System.getProperty("guava.concurrent.generate_cancellation_cause", "false"));

    private static final class Cancellation {
        static final Cancellation CAUSELESS_INTERRUPTED;
        static final Cancellation CAUSELESS_CANCELLED;

        static {
            if (GENERATE_CANCELLATION_CAUSES) {
                CAUSELESS_INTERRUPTED = null;
                CAUSELESS_CANCELLED = null;
            } else {
                CAUSELESS_INTERRUPTED = new Cancellation(false, null);
                CAUSELESS_CANCELLED = new Cancellation(true, null);
            }
        }

        final boolean wasInterrupted;
        final Throwable cause;

        public Cancellation(boolean wasInterrupted, Throwable cause) {
            this.wasInterrupted = wasInterrupted;
            this.cause = cause;
        }
    }
}
