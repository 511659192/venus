package com.ym.materials.guava.cache;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.*;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.sql.Time;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ym on 2018/8/5.
 */
public interface ValueReference<K, V> {

    V get();

    boolean isLoading();

    boolean isActive();

    V waitForValue() throws ExecutionException;

    void notifyNewValue(V newValue);

    ReferenceEntry<K, V> getEntry();

    static final ValueReference<Object, Object> UNSET = new ValueReference<Object, Object>() {
        @Override
        public Object get() {
            return null;
        }

        @Override
        public boolean isLoading() {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public Object waitForValue() throws ExecutionException {
            return null;
        }

        @Override
        public void notifyNewValue(Object newValue) {

        }

        @Override
        public ReferenceEntry<Object, Object> getEntry() {
            return null;
        }
    };

    class StrongValueReference<K, V> implements ValueReference<K, V> {
        final V referent;

        StrongValueReference(V referent) {
            this.referent = referent;
        }

        @Override
        public V get() {
            return referent;
        }

        @Override
        public ReferenceEntry<K, V> getEntry() {
            return null;
        }

        @Override
        public boolean isLoading() {
            return false;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public V waitForValue() throws ExecutionException {
            return get();
        }

        @Override
        public void notifyNewValue(V newValue) {

        }
    }

    class SoftValueReference<K, V> extends SoftReference<V> implements ValueReference<K, V> {

        final ReferenceEntry<K, V> entry;

        SoftValueReference(ReferenceQueue<V> queue, V referent, ReferenceEntry<K, V> entry) {
            super(referent, queue);
            this.entry = entry;
        }

        @Override
        public ReferenceEntry<K, V> getEntry() {
            return entry;
        }

        @Override
        public boolean isLoading() {
            return false;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public V waitForValue() throws ExecutionException {
            return get();
        }

        @Override
        public void notifyNewValue(V newValue) {

        }
    }

    class WeakValueReference<K, V> extends WeakReference<V> implements ValueReference<K, V> {

        final ReferenceEntry<K, V> entry;

        WeakValueReference(ReferenceQueue<V> queue, V referent, ReferenceEntry<K, V> entry) {
            super(referent, queue);
            this.entry = entry;
        }

        @Override
        public ReferenceEntry<K, V> getEntry() {
            return entry;
        }

        @Override
        public boolean isLoading() {
            return false;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public V waitForValue() throws ExecutionException {
            return get();
        }

        @Override
        public void notifyNewValue(V newValue) {

        }
    }

    class LoadingValueReference<K, V> implements ValueReference<K, V> {

        volatile ValueReference<K, V> oldValue;
        final SettableFuture<V> futureValue = SettableFuture.create();
        final Stopwatch stopwatch = Stopwatch.createUnstarted();

        public LoadingValueReference() {
            this(null);
        }

        public LoadingValueReference(ValueReference<K, V> oldValue) {
            this.oldValue = oldValue == null ? (ValueReference<K, V>) UNSET : oldValue;
        }

        @Override
        public V get() {
            return oldValue.get();
        }

        @Override
        public boolean isLoading() {
            return true;
        }

        @Override
        public boolean isActive() {
            return oldValue.isActive();
        }

        @Override
        public V waitForValue() throws ExecutionException {
            return Uninterruptibles.getUninterruptibly(futureValue);
        }

        @Override
        public void notifyNewValue(V newValue) {
            if (newValue != null) {
                set(newValue);
            } else {
                oldValue = (ValueReference<K, V>) UNSET;
            }
        }

        private boolean set(V newValue) {
            return newValue == null ? false : futureValue.set(newValue);
        }

        @Override
        public ReferenceEntry<K, V> getEntry() {
            return null;
        }

        public ListenableFuture<V> loadFuture(K key, CacheLoader<? super K, V> loader) {
            try {
                stopwatch.start();
                V previousValue = oldValue.get();
                if (previousValue == null) {
                    V newValue = loader.load(key);
                    return set(newValue) ? futureValue : Futures.immediateFuture(null);
                }
                ListenableFuture<V> newValue = loader.reload(key, previousValue);
                if (newValue == null) {
                    return Futures.immediateCheckedFuture(null);
                }

                Function<V, V> function = new Function<V, V>() {
                    @Override
                    public V apply(V newValue) {
                        set(newValue);
                        return newValue;
                    }
                };

                return Futures.transform(newValue, function, MoreExecutors.directExecutor());
            } catch (Throwable throwable) {
                ListenableFuture<V> listenableFuture = setException(throwable) ? futureValue : Futures.immediateFailedFuture(throwable);
                if (throwable instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                return listenableFuture;
            }
        }

        public boolean setException(Throwable exception) {
            return futureValue.setException(exception);
        }

        public long elapsedNanos() {
            return stopwatch.elapsed(TimeUnit.NANOSECONDS);
        }
    }

}
