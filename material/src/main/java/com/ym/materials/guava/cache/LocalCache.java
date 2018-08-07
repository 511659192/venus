package com.ym.materials.guava.cache;

import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;
import com.google.common.cache.*;
import com.google.common.collect.ImmutableSet;
import com.sun.org.apache.regexp.internal.RE;
import com.ym.materials.guava.cache.RemovalListener.NullListener;
import com.ym.materials.guava.cache.ValueReference.LoadingValueReference;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ym.materials.guava.cache.Queues.DISCARDING_QUEUE;
import static com.ym.materials.guava.cache.Queues.discardingQueue;

/**
 * Created by ym on 2018/8/4.
 */
class LocalCache<K, V> {

    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int MAX_SEGMENTS = 1 << 16;
    static final int DRAIN_MAX = 16;

    final int concurrencyLevel;
    final CacheLoader<? super K, V> loader;
    final Equivalence<Object> keyEquivalence;
    final Equivalence<Object> valueEquivalence;
    final Strength keyStrength;
    final Strength valueStrength;
    final int segmentMask;
    final int segmentShift;
    final Segment<K, V>[] segments;
    final long maxSize;
    final long expireAfterAccessNanos;
    final long expireAfterWriteNanos;
    final long refreshNanos;
    final Ticker ticker;
    final StatsCounter globalStatsCounter;
    final EntryFactory entryFactory;

    final RemovalListener<K, V> removalListener;
    final Queue<RemovalNotification<K, V>> removalNotificationQueue;

    LocalCache(CacheBuilder<? super K, ? super V> builder, CacheLoader<? super K, V> loader) {
        this.loader = loader;
        this.concurrencyLevel = builder.getConcurrencyLevel();
        this.maxSize = builder.getMaximumSize();

        this.keyStrength = builder.getKeyStrength();
        this.keyEquivalence = builder.getKeyEquivalence();
        this.valueStrength = builder.getValueStrength();
        this.valueEquivalence = builder.getValueEquivalence();

        this.expireAfterAccessNanos = builder.getExpireAfterAccessNanos();
        this.expireAfterWriteNanos = builder.getExpireAfterWriteNanos();
        this.refreshNanos = builder.getRefreshNanos();

        removalListener = builder.getRemovalListener();
        removalNotificationQueue = removalListener == NullListener.INSTANCE ?
                discardingQueue() : new ConcurrentLinkedDeque<RemovalNotification<K, V>>();

        int initialCapacity = Math.min(builder.getInitialCapacity(), MAXIMUM_CAPACITY);
        this.ticker = builder.getTicker(recordsTime());
        this.globalStatsCounter = builder.getStatsCounterSupplier().get();
        entryFactory = EntryFactory.getFactory(keyStrength, usesAccessQueue(), usesWriteQueue());

        int segmentShift = 0;
        int segmentCount = 1;
        while (segmentCount < concurrencyLevel && (!evictsBySize() || segmentCount * 20 <= maxSize)) {
            ++segmentShift;
            segmentCount <<= 1;
        }

        this.segmentShift = Integer.SIZE - segmentShift;
        segmentMask = segmentCount - 1;
        this.segments = new Segment[segmentCount];

        int initSegmentCapacity = initialCapacity / segmentCount;
        if (initSegmentCapacity * segmentCount < initialCapacity) {
            ++initSegmentCapacity;
        }

        int initSegmentSize = 1;
        while (initSegmentSize < initSegmentCapacity) {
            initSegmentSize <<= 1;
        }

        if (evictsBySize()) {
            long maxSegmentSize = maxSize / segmentCount + 1;
            long remainder = maxSize % segmentCount;
            for (int i = 0; i < segmentCount; i++) {
                if (i == remainder) {
                    maxSegmentSize--;
                }
                this.segments[i] = createSegment(initSegmentSize, maxSegmentSize, globalStatsCounter);
            }

        } else {
            for (int i = 0; i < segmentCount; i++) {
                this.segments[i] = createSegment(initSegmentSize, CacheBuilder.UNSET_INT, globalStatsCounter);
            }
        }
    }

    private Segment<K, V> createSegment(int initSegmentSize, long maxSegmentSize, StatsCounter statsCounter) {
        return new Segment<K, V>(this, initSegmentSize, maxSegmentSize, statsCounter);
    }

    boolean recordsTime() {
        return recordsWrite() || recordsAccess();
    }

    private boolean recordsAccess() {
        return expiresAfterAccess();
    }

    private boolean expiresAfterAccess() {
        return expireAfterAccessNanos > 0;
    }

    private boolean recordsWrite() {
        return expiresAfterWrite() || refreshes();
    }

    private boolean refreshes() {
        return refreshNanos > 0;
    }

    private boolean expiresAfterWrite() {
        return expireAfterWriteNanos > 0;
    }

    boolean evictsBySize() {
        return maxSize >= 0;
    }

    V getOrLoad(K key) throws ExecutionException {
        return get(key, loader);
    }

    V get(K key, CacheLoader<? super K, V> loader) throws ExecutionException {
        int hash = hash(checkNotNull(key));
        return segmentFor(hash).get(key, hash, loader);
    }

    Segment<K, V> segmentFor(int hash) {
        return segments[(hash >> segmentShift) & segmentMask];
    }

    private int hash(K key) {
        int h = keyEquivalence.hash(key);
        return rehash(h);
    }

    static int rehash(int h) {
        h += (h << 15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h << 3);
        h ^= (h >>> 6);
        h += (h << 2) + (h << 14);
        return h ^ (h >>> 16);
    }

    boolean usesKeyReferences() {
        return keyStrength != Strength.STRONG;
    }

    boolean usesValueReferences() {
        return valueStrength != Strength.STRONG;
    }

    boolean usesAccessQueue() {
        return expiresAfterAccess() || evictsBySize();
    }

    boolean usesWriteQueue() {
        return expiresAfterWrite();
    }

    static class LocalManualCache<K, V> implements Cache<K, V>, Serializable {

        final LocalCache<K, V> localCache;

        private LocalManualCache(LocalCache<K, V> localCache) {
            this.localCache = localCache;
        }
    }

    static class LocalLoadingCache<K, V> extends LocalManualCache<K, V> implements LoadingCache<K, V> {
        LocalLoadingCache(CacheBuilder<? super K, ? super V> builder, CacheLoader<? super K, V> loader) {
            super(new LocalCache<K, V>(builder, checkNotNull(loader)));
        }

        @Override
        public V get(K key) throws ExecutionException {
            return localCache.getOrLoad(key);
        }
    }

    static class Segment<K, V> extends ReentrantLock {

        private final LocalCache<K, V> localCache;
        private final long maxSegmentSize;
        private final StatsCounter statsCounter;
        int threshold;
        volatile AtomicReferenceArray<ReferenceEntry<K, V>> table;
        final ReferenceQueue<K> keyReferenceQueue;
        final ReferenceQueue<V> valueReferenceQueue;
        final Queue<ReferenceEntry<K, V>> recencyQueue;
        final Queue<ReferenceEntry<K, V>> writeQueue;
        final Queue<ReferenceEntry<K, V>> accessQueue;
        volatile int count;
        int modCount;
        final AtomicInteger readCount = new AtomicInteger();
        long totalSize;

        public Segment(LocalCache<K, V> localCache, int initSegmentSize, long maxSegmentSize, StatsCounter statsCounter) {
            this.localCache = localCache;
            this.maxSegmentSize = maxSegmentSize;
            this.statsCounter = statsCounter;

            this.table = new AtomicReferenceArray<ReferenceEntry<K, V>>(initSegmentSize);
            this.threshold = initSegmentSize * 3 / 4;

            this.keyReferenceQueue = localCache.usesKeyReferences() ? new ReferenceQueue<K>() : null;
            this.valueReferenceQueue = localCache.usesValueReferences() ? new ReferenceQueue<V>() : null;

            this.recencyQueue = localCache.usesAccessQueue() ? new ConcurrentLinkedDeque<ReferenceEntry<K, V>>() : discardingQueue();
            this.writeQueue = localCache.usesWriteQueue() ? new ConcurrentLinkedDeque<ReferenceEntry<K, V>>() : discardingQueue();
            this.accessQueue = null;

        }

        public V get(K key, int hash, CacheLoader<? super K, V> loader) {
            checkNotNull(key);
            checkNotNull(loader);
            try {
                if (count == 0) {
                    return lockedGetOrLoad(key, hash, loader);
                }

            } catch (ExecutionException e) {

            } finally {

            }

            return null;

        }

        private V lockedGetOrLoad(K key, int hash, CacheLoader<? super K, V> loader) throws ExecutionException {
            ReferenceEntry<K, V> e;
            ValueReference<K, V> valueReference = null;
            LoadingValueReference loadingValueReference = null;
            boolean createNewEntry = true;
            lock();
            try {
                long now = localCache.ticker.read();
                preWriteCleanUp(now);
                int newCount = this.count - 1;
                AtomicReferenceArray<ReferenceEntry<K, V>> table = this.table;
                int index = hash & (table.length() - 1);
                ReferenceEntry<K, V> first = table.get(index);
                for (e = first; e != null; e = e.getNext()) {
                    K entryKey = e.getKey();
                    if (e.getHash() == hash && entryKey != null && localCache.keyEquivalence.equivalent(key, entryKey)) {
                        valueReference = e.getValueReference();
                        if (valueReference.isLoading()) {
                            createNewEntry = false;
                        } else {
                            V value = valueReference.get();
                            if (value == null) {
                                enqueueNotification(entryKey, hash, value, RemovalCause.COLLECTED);
                            } else if (localCache.isExpired(e, now)) {
                                enqueueNotification(entryKey, hash, value, RemovalCause.EXPIRED);
                            } else {
                                recordLockedRead(e, now);
                                statsCounter.recordHits(1);
                                return value;
                            }
                            writeQueue.remove(e);
                            accessQueue.remove(e);
                            this.count = newCount;
                        }
                        break;
                    }
                }

                if (createNewEntry) {
                    loadingValueReference = new LoadingValueReference();
                    if (e == null) {
                        e = newEntry(key, hash, first);
                        e.setValueReference(loadingValueReference);
                        table.set(index, e);
                    } else {
                        e.setValueReference(loadingValueReference);
                    }
                }

            } finally {
                unlock();
                postWriteCleanUp();
            }

            if (createNewEntry) {
                try {
                    synchronized (e) {
                        return loadSync(key, hash, loadingValueReference, loader);
                    }
                } finally {
                    statsCounter.recordMisses(1);
                }
            } else {
                return waitForLoadingValue(e, key, valueReference);
            }
        }

        private V waitForLoadingValue(ReferenceEntry<K, V> e, K key, ValueReference<K, V> valueReference) {
            return null;
        }

        private V loadSync(K key, int hash, LoadingValueReference loadingValueReference, CacheLoader<? super K, V> loader) {
            loadingValueReference.loadFuture(key, loader);

            return null;
        }

        private ReferenceEntry<K,V> newEntry(K key, int hash, ReferenceEntry<K, V> next) {
            return localCache.entryFactory.newEntry(this, key, hash, next);
        }

        private void recordLockedRead(ReferenceEntry<K, V> entry, long now) {
            if (localCache.recordsAccess()) {
                entry.setAccessTime(now);
            }
            accessQueue.add(entry);
        }

        private ReferenceEntry<K, V> getFirst(int hash) {
            AtomicReferenceArray<ReferenceEntry<K, V>> table = this.table;
            int index = hash & (table.length() - 1);
            ReferenceEntry<K, V> first = table.get(index);
            return first;
        }

        private void preWriteCleanUp(long now) {
            runLockedCleanUp(now);
        }

        private void runLockedCleanUp(long now) {
            if (tryLock()) {
                try {
                    drainReferenceQueues();
                    expireEntries(now);
                    readCount.set(0);
                } finally {
                    unlock();
                }
            }
        }

        private void expireEntries(long now) {

        }

        private void drainReferenceQueues() {
            if (localCache.usesKeyReferences()) {
                drainKeyReferenceQueue();
            }
            if (localCache.usesValueReferences()) {
                drainValueReferenceQueue();
            }
        }

        private void drainKeyReferenceQueue() {
            Reference<? extends K> ref;
            int i = 0;
            while ((ref = keyReferenceQueue.poll()) != null) {
                ReferenceEntry<K, V> entry = (ReferenceEntry<K, V>) ref;
                localCache.reclaimKey(entry);
                if (++i == DRAIN_MAX) {
                    break;
                }
            }
        }

        private void drainValueReferenceQueue() {
            Reference<? extends V> ref;
            int i = 0;
            while ((ref = valueReferenceQueue.poll()) != null) {
                ValueReference<K, V> valueReference = (ValueReference<K, V>) ref;
                localCache.reclaimValue(valueReference);
                if (++i == DRAIN_MAX) {
                    break;
                }
            }
        }

        public boolean reclaimKey(ReferenceEntry<K, V> entry, int hash) {
            lock();
            try {
                int newCount = count - 1;
                AtomicReferenceArray<ReferenceEntry<K, V>> table = this.table;
                int index = hash & (table.length() - 1);
                ReferenceEntry<K, V> first = table.get(index);
                for (ReferenceEntry<K, V> e = first; e != null; e = e.getNext()) {
                    if (e == entry) {
                        ++modCount;
                        ReferenceEntry<K, V> newFirst = removeValueFromChain(
                                first,
                                e,
                                e.getKey(),
                                hash,
                                e.getValueReference().get(),
                                e.getValueReference(),
                                RemovalCause.COLLECTED);
                        newCount = this.count - 1;
                        table.set(index, newFirst);
                        this.count = newCount;
                        return true;
                    }
                }
                return false;
            } finally {
                unlock();
                postWriteCleanUp();
            }
        }

        private void postWriteCleanUp() {

        }

        private ReferenceEntry<K,V> removeValueFromChain(
                ReferenceEntry<K, V> first,
                ReferenceEntry<K, V> entry,
                K key, int hash,
                V value,
                ValueReference<K, V> valueReference,
                RemovalCause removalCause) {
            enqueueNotification(key, hash, value, removalCause);
            writeQueue.remove(entry);
            accessQueue.remove(entry);

            if (valueReference.isLoading()) {
                valueReference.notifyNewValue(null);
                return first;
            } else {
                return removeEntryFromChain(first, entry);
            }
        }

        private ReferenceEntry<K, V> removeEntryFromChain(ReferenceEntry<K, V> first, ReferenceEntry<K, V> entry) {
            return null;
        }

        private void enqueueNotification(K key, int hash, V value, RemovalCause removalCause) {
            totalSize--;
            if (removalCause.wasEvicted()) {
                statsCounter.recordEvicion();
            }
            if (localCache.removalNotificationQueue != DISCARDING_QUEUE) {
                RemovalNotification<K, V> removalNotification = RemovalNotification.create(key, value, removalCause);
                localCache.removalNotificationQueue.offer(removalNotification);
            }
        }

        public void reclaimValue(K key, int hash, ValueReference<K, V> valueReference) {
            lock();
            try {
                int newCount = this.count - 1;
                AtomicReferenceArray<ReferenceEntry<K, V>> table = this.table;
                int index = hash & (table.length() - 1);
                ReferenceEntry<K, V> first = table.get(index);
                for (ReferenceEntry<K, V> e = first; e != null; e = e.getNext()) {
                    K entryKey = e.getKey();
                    if (e.getHash() == hash && entryKey != null && localCache.keyEquivalence.equivalent(key, entryKey)) {

                    }
                }


            } finally {
                unlock();
            }
        }
    }

    private boolean isExpired(ReferenceEntry<K, V> entry, long now) {
        checkNotNull(entry);
        if (expiresAfterAccess() && (now - entry.getAccessTime() >= expireAfterAccessNanos)) {
            return true;
        }

        if (expiresAfterWrite() && (now - entry.getWriteTime() >= expireAfterWriteNanos)) {
            return true;
        }
        return false;
    }

    private void reclaimValue(ValueReference<K, V> valueReference) {
        ReferenceEntry<K, V> entry = valueReference.getEntry();
        int hash = entry.getHash();
        segmentFor(hash).reclaimValue(entry.getKey(), hash, valueReference);
    }

    private void reclaimKey(ReferenceEntry<K, V> entry) {
        int hash = entry.getHash();
        segmentFor(hash).reclaimKey(entry, hash);
    }

}
