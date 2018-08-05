package com.ym.materials.guava.cache;

import com.google.common.base.Equivalence;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Ticker;
import com.ym.materials.guava.cache.RemovalListener.NullListener;

import static com.ym.materials.guava.cache.StatsCounter.NULL_STATS_COUNTER;

/**
 * Created by ym on 2018/8/4.
 */
public final class CacheBuilder<K, V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int DEFAULT_CONCURRENCY_LEVEL = 4;
    private static final int DEFAULT_EXPIRATION_NANOS = 0;
    private static final int DEFAULT_REFRESH_NANOS = 0;

    static final int UNSET_INT = -1;

    int initialCapacity = UNSET_INT;
    int concurrencyLevel = UNSET_INT;

    Strength keyStrength;
    Strength valueStrength;
    Equivalence<Object> keyEquivalence;
    Equivalence<Object> valueEquivalence;

    long expireAfterWriteNanos = UNSET_INT;
    long expireAfterAccessNanos = UNSET_INT;
    long refreshNanos = UNSET_INT;
    long maximumSize = UNSET_INT;

    Ticker ticker;


    Supplier<? extends StatsCounter> statsCounterSupplier = NULL_STATS_COUNTER;
    RemovalListener<K, V> removalListener;

    private CacheBuilder() {
    }

    public static CacheBuilder<Object, Object> newBuilder() {
        return new CacheBuilder<>();
    }

    public <K1 extends K, V1 extends V> LoadingCache<K1, V1> build(CacheLoader<? super K1, V1> loader) {
        return new LocalCache.LocalLoadingCache<K1, V1>(this, loader);
    }

    Strength getKeyStrength() {
        return keyStrength != null ? keyStrength : Strength.STRONG;
    }

    Strength getValueStrength() {
        return valueStrength != null ? valueStrength : Strength.STRONG;
    }

    Equivalence<Object> getKeyEquivalence() {
        return keyEquivalence == null ? Strength.STRONG.defaultEquivalence() : keyEquivalence;
    }

    Equivalence<Object> getValueEquivalence() {
        return valueEquivalence == null ? Strength.STRONG.defaultEquivalence() : valueEquivalence;
    }

    int getConcurrencyLevel() {
        return concurrencyLevel == UNSET_INT ? DEFAULT_CONCURRENCY_LEVEL : concurrencyLevel;
    }

    long getMaximumSize() {
        if (expireAfterAccessNanos == 0 || expireAfterWriteNanos == 0) {
            return 0;
        }
        return maximumSize;
    }

    long getExpireAfterAccessNanos() {
        return expireAfterAccessNanos == UNSET_INT ? DEFAULT_EXPIRATION_NANOS : expireAfterAccessNanos;
    }

    long getExpireAfterWriteNanos() {
        return expireAfterWriteNanos == UNSET_INT ? DEFAULT_EXPIRATION_NANOS : expireAfterWriteNanos;
    }

    long getRefreshNanos() {
        return refreshNanos == UNSET_INT ? DEFAULT_REFRESH_NANOS : refreshNanos;
    }

    int getInitialCapacity() {
        return initialCapacity == UNSET_INT ? DEFAULT_INITIAL_CAPACITY : initialCapacity;
    }

    Ticker getTicker(boolean recordsTime) {
        if (ticker != null) {
            return ticker;
        }
        return recordsTime ? Ticker.systemTicker() : new Ticker() {
            @Override
            public long read() {
                return 0;
            }
        };
    }

    Supplier<? extends StatsCounter> getStatsCounterSupplier() {
        return statsCounterSupplier;
    }

    <K1 extends K, V1 extends V> RemovalListener<K1, V1> getRemovalListener() {
        return removalListener == null ? (RemovalListener<K1, V1>) NullListener.INSTANCE : (RemovalListener<K1, V1>) removalListener;
    }
}
