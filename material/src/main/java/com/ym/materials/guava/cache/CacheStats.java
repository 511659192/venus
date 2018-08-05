package com.ym.materials.guava.cache;

/**
 * Created by ym on 2018/8/5.
 */
public class CacheStats {
    private final long hitCount;
    private final long missCount;
    private final long loadSuccessCount;
    private final long loadExceptionCount;
    private final long totalLoadTime;
    private final long evictionCount;

    public CacheStats(long hitCount, long missCount, long loadSuccessCount, long loadExceptionCount, long totalLoadTime, long evictionCount) {
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.loadSuccessCount = loadSuccessCount;
        this.loadExceptionCount = loadExceptionCount;
        this.totalLoadTime = totalLoadTime;
        this.evictionCount = evictionCount;
    }

    static final CacheStats EMPTY_STATS = new CacheStats(0, 0, 0, 0, 0, 0);
}
