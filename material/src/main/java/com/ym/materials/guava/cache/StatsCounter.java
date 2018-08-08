package com.ym.materials.guava.cache;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Created by ym on 2018/8/5.
 */
public interface StatsCounter {

    CacheStats snapshot();

    void recordEvicion();

    void recordHits(int i);

    Supplier<? extends StatsCounter> NULL_STATS_COUNTER = Suppliers.ofInstance(new StatsCounter()
    {
        @Override
        public CacheStats snapshot() {
            return CacheStats.EMPTY_STATS;
        }

        @Override
        public void recordEvicion() {

        }

        @Override
        public void recordHits(int i) {

        }

        @Override
        public void recordMisses(int i) {

        }

        @Override
        public void recordLoadSuccess(long elapsedNanos) {

        }

        @Override
        public void recordLoadException(long elapsedNanos) {

        }
    });

    void recordMisses(int i);

    void recordLoadSuccess(long elapsedNanos);

    void recordLoadException(long elapsedNanos);
}
